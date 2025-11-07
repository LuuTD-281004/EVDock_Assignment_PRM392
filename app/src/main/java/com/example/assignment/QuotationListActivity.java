package com.example.assignment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.appbar.MaterialToolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.assignment.data.remote.ApiServiceFactory;
import com.example.assignment.data.remote.dto.quotation.QuotationSummaryDto;
import com.example.assignment.data.repository.QuotationRepository;
import com.example.assignment.data.session.SessionManager;
import com.example.assignment.data.session.UserRole;
import com.example.assignment.data.session.UserSession;
import com.example.assignment.ui.quotation.QuotationListAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QuotationListActivity extends AppCompatActivity implements QuotationListAdapter.OnQuotationActionListener {

    public static final String EXTRA_QUOTATION_ID = "extra_quotation_id";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private SessionManager sessionManager;
    private QuotationRepository repository;
    private UserSession userSession;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private TextView resultCount;
    private EditText searchInput;
    private Spinner statusSpinner;
    private Spinner typeSpinner;
    private FloatingActionButton fabCreate;
    private QuotationListAdapter adapter;

    private final List<QuotationSummaryDto> allQuotations = new ArrayList<>();

    private boolean isLoading = false;
    private MaterialToolbar topAppBar;
    private final SimpleDateFormat createDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quotation_list);

        sessionManager = new SessionManager(getApplicationContext());
        ApiServiceFactory factory = new ApiServiceFactory(sessionManager);
        repository = new QuotationRepository(factory);
        userSession = sessionManager.getUserSession();

        if (userSession == null || userSession.getAgencyId() == null || userSession.getRole() != UserRole.DEALER_STAFF) {
            Toast.makeText(this, "Chỉ dành cho Dealer Staff", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        bindViews();
        setupRecycler();
        setupSpinners();
        setupListeners();

        loadQuotations();
    }

    private void bindViews() {
        topAppBar = findViewById(R.id.topAppBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        recyclerView = findViewById(R.id.quotationRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
        resultCount = findViewById(R.id.resultCount);
        searchInput = findViewById(R.id.searchInput);
        statusSpinner = findViewById(R.id.statusSpinner);
        typeSpinner = findViewById(R.id.typeSpinner);
        fabCreate = findViewById(R.id.fabCreate);
    }

    private void setupRecycler() {
        adapter = new QuotationListAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupSpinners() {
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"All", "DRAFT", "PENDING", "APPROVED", "REJECTED", "EXPIRED"});
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(statusAdapter);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"All", "AT_STORE", "ORDER", "PRE_ORDER"});
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);
    }

    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(this::loadQuotations);

        fabCreate.setOnClickListener(v -> {
            Intent intent = new Intent(this, QuotationFormActivity.class);
            intent.putExtra(QuotationFormActivity.EXTRA_MODE, QuotationFormActivity.MODE_CREATE);
            startActivity(intent);
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

        statusSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                filterAndDisplay();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        typeSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                filterAndDisplay();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

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

    private void loadQuotations() {
        if (isLoading) {
            return;
        }

        setLoading(true);
        executor.execute(() -> {
            try {
                String status = getSelectedStatus();
                String type = getSelectedType();
                String query = searchInput.getText().toString().trim();
                List<QuotationSummaryDto> quotations = repository.getQuotations(userSession.getAgencyId(), status, type, query);
                runOnUiThread(() -> {
                    allQuotations.clear();
                    allQuotations.addAll(quotations);
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
        String status = getSelectedStatus();
        String type = getSelectedType();

        List<QuotationSummaryDto> filtered = new ArrayList<>();
        for (QuotationSummaryDto quotation : allQuotations) {
            boolean matchesStatus = status.equals("ALL") || (quotation.getStatus() != null && quotation.getStatus().equalsIgnoreCase(status));
            boolean matchesType = type.equals("ALL") || (quotation.getType() != null && quotation.getType().equalsIgnoreCase(type));
            boolean matchesQuery = query.isEmpty() || (quotation.getQuoteCode() != null && quotation.getQuoteCode().toLowerCase(Locale.getDefault()).contains(query));
            if (matchesStatus && matchesType && matchesQuery) {
                filtered.add(quotation);
            }
        }

        filtered.sort((a, b) -> Long.compare(getQuotationSortKey(b), getQuotationSortKey(a)));

        adapter.submitList(filtered);
        emptyView.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        resultCount.setText(String.format(Locale.getDefault(), "%d results", filtered.size()));
    }

    private String getSelectedStatus() {
        String value = (String) statusSpinner.getSelectedItem();
        return value != null ? value.toUpperCase(Locale.getDefault()) : "ALL";
    }

    private String getSelectedType() {
        String value = (String) typeSpinner.getSelectedItem();
        return value != null ? value.toUpperCase(Locale.getDefault()) : "ALL";
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        swipeRefreshLayout.setRefreshing(loading);
    }

    private void performLogout() {
        sessionManager.clearSession();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private long getQuotationSortKey(QuotationSummaryDto quotation) {
        String value = quotation.getCreateDate();
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
            Date date = createDateFormat.parse(value);
            if (date != null) {
                return date.getTime();
            }
        } catch (ParseException ignored) {
        }
        return Long.MIN_VALUE;
    }

    @Override
    public void onView(QuotationSummaryDto quotation) {
        Intent intent = new Intent(this, QuotationDetailActivity.class);
        intent.putExtra(EXTRA_QUOTATION_ID, quotation.getId());
        startActivity(intent);
    }

    @Override
    public void onEdit(QuotationSummaryDto quotation) {
        Intent intent = new Intent(this, QuotationFormActivity.class);
        intent.putExtra(QuotationFormActivity.EXTRA_MODE, QuotationFormActivity.MODE_EDIT);
        intent.putExtra(EXTRA_QUOTATION_ID, quotation.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(QuotationSummaryDto quotation) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this quotation?")
                .setPositiveButton("Delete", (dialog, which) -> performDelete(quotation))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performDelete(QuotationSummaryDto quotation) {
        setLoading(true);
        executor.execute(() -> {
            try {
                repository.deleteQuotation(quotation.getId());
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, "Đã xóa báo giá", Toast.LENGTH_LONG).show();
                    loadQuotations();
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        loadQuotations();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}


