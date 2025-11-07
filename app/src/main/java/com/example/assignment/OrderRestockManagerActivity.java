package com.example.assignment;

import android.content.Intent;
import android.os.Build;
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
import com.example.assignment.data.remote.dto.order.manager.ManagerOrderItemDto;
import com.example.assignment.data.remote.dto.order.manager.ManagerOrderSummaryDto;
import com.example.assignment.data.repository.OrderRestockManagerRepository;
import com.example.assignment.data.session.SessionManager;
import com.example.assignment.data.session.UserRole;
import com.example.assignment.data.session.UserSession;
import com.example.assignment.ui.order.ManagerOrderAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrderRestockManagerActivity extends AppCompatActivity implements ManagerOrderAdapter.OnManagerOrderListener {

    public static final String EXTRA_ORDER = "extra_order";
    public static final String EXTRA_FIRST_ITEM_ID = "extra_first_item_id";

    private static final String STATUS_ALL = "ALL";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<ManagerOrderSummaryDto> allOrders = new ArrayList<>();

    private SessionManager sessionManager;
    private OrderRestockManagerRepository repository;
    private UserSession userSession;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private TextView totalOrdersText;
    private TextView pendingOrdersText;
    private TextView approvedOrdersText;
    private EditText searchInput;
    private Spinner statusSpinner;
    private ManagerOrderAdapter adapter;

    private boolean isLoading = false;
    private Map<String, String> statusMap;
    private MaterialToolbar topAppBar;
    private final SimpleDateFormat orderDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

    private final ActivityResultLauncher<Intent> detailLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadOrders();
                }
            });

    private final ActivityResultLauncher<Intent> createLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadOrders();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_order_restock);

        sessionManager = new SessionManager(getApplicationContext());
        ApiServiceFactory factory = new ApiServiceFactory(sessionManager);
        repository = new OrderRestockManagerRepository(factory);
        userSession = sessionManager.getUserSession();

        if (userSession == null || sessionManager.getAccessToken() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        if (userSession.getRole() != UserRole.DEALER_MANAGER) {
            Toast.makeText(this, "Không đủ quyền truy cập", Toast.LENGTH_LONG).show();
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
        approvedOrdersText = findViewById(R.id.approvedOrdersText);
        searchInput = findViewById(R.id.searchInput);
        statusSpinner = findViewById(R.id.statusSpinner);

        FloatingActionButton fabCreate = findViewById(R.id.fabCreate);
        fabCreate.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrderRestockManagerCreateActivity.class);
            createLauncher.launch(intent);
        });

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
        adapter = new ManagerOrderAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupStatusSpinner() {
        statusMap = new HashMap<>();
        statusMap.put("All", STATUS_ALL);
        statusMap.put("Draft", "DRAFT");
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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

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

        if (userSession.getAgencyId() == null) {
            Toast.makeText(this, "Không tìm thấy thông tin đại lý", Toast.LENGTH_LONG).show();
            return;
        }

        setLoading(true);
        executor.execute(() -> {
            try {
                String status = getSelectedStatusValue();
                List<ManagerOrderSummaryDto> orders = repository.getOrders(userSession.getAgencyId(), status);
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
        String query = searchInput.getText().toString().trim().toLowerCase(Locale.getDefault());
        String status = getSelectedStatusValue();

        List<ManagerOrderSummaryDto> filtered = new ArrayList<>();
        for (ManagerOrderSummaryDto order : allOrders) {
            boolean matchesStatus = STATUS_ALL.equals(status) || (order.getStatus() != null && order.getStatus().equalsIgnoreCase(status));
            boolean matchesQuery = query.isEmpty()
                    || String.valueOf(order.getId()).contains(query)
                    || (order.getStatus() != null && order.getStatus().toLowerCase(Locale.getDefault()).contains(query));
            if (matchesStatus && matchesQuery) {
                filtered.add(order);
            }
        }

        filtered.sort((a, b) -> Long.compare(getOrderSortKey(b), getOrderSortKey(a)));

        adapter.submitList(filtered);
        emptyView.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        updateStats(filtered);
    }

    private void updateStats(List<ManagerOrderSummaryDto> orders) {
        totalOrdersText.setText(String.format(Locale.getDefault(), "Total: %d", orders.size()));
        int pending = 0;
        int approved = 0;
        for (ManagerOrderSummaryDto order : orders) {
            String status = order.getStatus() != null ? order.getStatus().toUpperCase(Locale.getDefault()) : "";
            if (status.equals("PENDING")) {
                pending++;
            } else if (status.equals("APPROVED")) {
                approved++;
            }
        }
        pendingOrdersText.setText(String.format(Locale.getDefault(), "Pending: %d", pending));
        approvedOrdersText.setText(String.format(Locale.getDefault(), "Approved: %d", approved));
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
    public void onOrderSelected(ManagerOrderSummaryDto order) {
        Intent intent = new Intent(this, OrderRestockManagerDetailActivity.class);
        intent.putExtra(OrderRestockManagerDetailActivity.EXTRA_ORDER, order);
        long firstItemId = 0;
        List<ManagerOrderItemDto> items = order.getOrderItems();
        if (items != null && !items.isEmpty()) {
            firstItemId = items.get(0).getId();
        }
        intent.putExtra(OrderRestockManagerDetailActivity.EXTRA_FIRST_ITEM_ID, firstItemId);
        detailLauncher.launch(intent);
    }

    @Override
    public void onAccept(ManagerOrderSummaryDto order) {
        confirmAndRun("Xác nhận đơn hàng", "Bạn có chắc muốn xác nhận đơn hàng này?", () -> performAccept(order));
    }

    @Override
    public void onCancel(ManagerOrderSummaryDto order) {
        confirmAndRun("Hủy đơn hàng", "Bạn có chắc muốn hủy đơn hàng này?", () -> performCancel(order));
    }

    private void performAccept(ManagerOrderSummaryDto order) {
        setLoading(true);
        executor.execute(() -> {
            try {
                repository.acceptOrder(order.getId());
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, "Đã xác nhận đơn hàng", Toast.LENGTH_LONG).show();
                    loadOrders();
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void performCancel(ManagerOrderSummaryDto order) {
        setLoading(true);
        executor.execute(() -> {
            try {
                repository.deleteOrder(order.getId());
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, "Đã hủy đơn hàng", Toast.LENGTH_LONG).show();
                    loadOrders();
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void confirmAndRun(String title, String message, Runnable action) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Đồng ý", (dialog, which) -> action.run())
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    private long getOrderSortKey(ManagerOrderSummaryDto order) {
        String value = order.getOrderAt();
        if (value == null) {
            return Long.MIN_VALUE;
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return Instant.parse(value).toEpochMilli();
            }
        } catch (Exception ignored) {
        }
        try {
            Date date = orderDateFormat.parse(value);
            if (date != null) {
                return date.getTime();
            }
        } catch (ParseException ignored) {
        }
        return Long.MIN_VALUE;
    }
}


