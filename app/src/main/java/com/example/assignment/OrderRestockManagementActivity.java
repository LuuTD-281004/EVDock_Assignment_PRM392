package com.example.assignment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.appbar.MaterialToolbar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.assignment.data.remote.ApiServiceFactory;
import com.example.assignment.data.remote.dto.order.OrderRestockSummaryDto;
import com.example.assignment.data.repository.OrderRestockRepository;
import com.example.assignment.data.session.SessionManager;
import com.example.assignment.data.session.UserSession;
import com.example.assignment.ui.order.OrderRestockAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrderRestockManagementActivity extends AppCompatActivity implements OrderRestockAdapter.OnOrderInteractionListener {

    private static final String STATUS_ALL = "ALL";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<OrderRestockSummaryDto> allOrders = new ArrayList<>();

    private SessionManager sessionManager;
    private OrderRestockRepository repository;
    private UserSession userSession;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private TextView totalOrdersText;
    private TextView pendingOrdersText;
    private TextView deliveredOrdersText;
    private EditText searchInput;
    private Spinner statusSpinner;
    private OrderRestockAdapter adapter;
    private Map<String, String> statusMap;

    private MaterialToolbar topAppBar;

    private boolean isLoading = false;

    private final ActivityResultLauncher<Intent> detailLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadOrders();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_restock_management);

        sessionManager = new SessionManager(getApplicationContext());
        ApiServiceFactory factory = new ApiServiceFactory(sessionManager);
        repository = new OrderRestockRepository(factory);
        userSession = sessionManager.getUserSession();

        if (userSession == null || sessionManager.getAccessToken() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        bindViews();
        setupRecycler();
        setupStatusSpinner();
        setupListeners();

        loadOrders();
    }

    private void bindViews() {
        topAppBar = findViewById(R.id.topAppBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        recyclerView = findViewById(R.id.ordersRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
        totalOrdersText = findViewById(R.id.totalOrdersText);
        pendingOrdersText = findViewById(R.id.pendingOrdersText);
        deliveredOrdersText = findViewById(R.id.deliveredOrdersText);
        searchInput = findViewById(R.id.searchInput);
        statusSpinner = findViewById(R.id.statusSpinner);

        if (topAppBar != null) {
            topAppBar.setNavigationIcon(R.drawable.ic_arrow_back);
            topAppBar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
            topAppBar.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_logout) {
                    performLogout();
                    return true;
                }
                return false;
            });
        }
    }

    private void setupRecycler() {
        adapter = new OrderRestockAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupStatusSpinner() {
        statusMap = new HashMap<>();
        statusMap.put("All", STATUS_ALL);
        statusMap.put("Pending", "PENDING");
        statusMap.put("Approved", "APPROVED");
        statusMap.put("Delivered", "DELIVERED");
        statusMap.put("Canceled", "CANCELED");

        StatusSpinnerAdapter spinnerAdapter = new StatusSpinnerAdapter(this, new ArrayList<>(statusMap.keySet()));
        statusSpinner.setAdapter(spinnerAdapter);
        statusSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                filterAndDisplay();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(this::loadOrders);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                filterAndDisplay();
            }
        });
    }

    private void loadOrders() {
        if (isLoading) {
            return;
        }

        setLoading(true);
        executor.execute(() -> {
            try {
                String selectedStatus = getSelectedStatusValue();
                List<OrderRestockSummaryDto> orders = repository.getOrders(selectedStatus.equals(STATUS_ALL) ? null : selectedStatus);
                runOnUiThread(() -> {
                    allOrders.clear();
                    allOrders.addAll(orders);
                    filterAndDisplay();
                    setLoading(false);
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void filterAndDisplay() {
        String search = searchInput.getText().toString().trim().toLowerCase(Locale.getDefault());
        String statusFilter = getSelectedStatusValue();

        List<OrderRestockSummaryDto> filtered = new ArrayList<>();
        for (OrderRestockSummaryDto order : allOrders) {
            boolean matchesStatus = STATUS_ALL.equals(statusFilter) || (order.getStatus() != null && order.getStatus().equalsIgnoreCase(statusFilter));
            boolean matchesSearch = search.isEmpty()
                    || String.valueOf(order.getId()).contains(search)
                    || (order.getAgency() != null && order.getAgency().getName() != null && order.getAgency().getName().toLowerCase(Locale.getDefault()).contains(search))
                    || (order.getAgencyId() != null && String.valueOf(order.getAgencyId()).contains(search));

            if (matchesStatus && matchesSearch) {
                filtered.add(order);
            }
        }

        adapter.submitList(filtered);
        emptyView.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        updateStats(filtered);
    }

    private void updateStats(List<OrderRestockSummaryDto> filtered) {
        totalOrdersText.setText(String.format(Locale.getDefault(), "Total: %d", filtered.size()));
        int pending = 0;
        int delivered = 0;
        for (OrderRestockSummaryDto order : filtered) {
            String status = order.getStatus() != null ? order.getStatus().toUpperCase(Locale.getDefault()) : "";
            if (status.equals("PENDING")) {
                pending++;
            }
            if (status.equals("DELIVERED")) {
                delivered++;
            }
        }
        pendingOrdersText.setText(String.format(Locale.getDefault(), "Pending: %d", pending));
        deliveredOrdersText.setText(String.format(Locale.getDefault(), "Delivered: %d", delivered));
    }

    private String getSelectedStatusValue() {
        String label = (String) statusSpinner.getSelectedItem();
        if (label == null) {
            return STATUS_ALL;
        }
        return statusMap.getOrDefault(label, STATUS_ALL);
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (!loading) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void performLogout() {
        sessionManager.clearSession();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onOrderSelected(OrderRestockSummaryDto order) {
        Intent intent = new Intent(this, OrderRestockDetailActivity.class);
        intent.putExtra(OrderRestockDetailActivity.EXTRA_ORDER_ID, order.getId());
        detailLauncher.launch(intent);
    }

    @Override
    public void onAdvanceStatus(OrderRestockSummaryDto order) {
        Intent intent = new Intent(this, OrderRestockDetailActivity.class);
        intent.putExtra(OrderRestockDetailActivity.EXTRA_ORDER_ID, order.getId());
        detailLauncher.launch(intent);
    }

    @Override
    public void onCancel(OrderRestockSummaryDto order) {
        Intent intent = new Intent(this, OrderRestockDetailActivity.class);
        intent.putExtra(OrderRestockDetailActivity.EXTRA_ORDER_ID, order.getId());
        detailLauncher.launch(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}


