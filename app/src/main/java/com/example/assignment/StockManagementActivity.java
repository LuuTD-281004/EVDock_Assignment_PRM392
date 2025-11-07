package com.example.assignment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import com.example.assignment.data.remote.dto.stock.AgencyStockItemDto;
import com.example.assignment.data.remote.dto.stock.motorbike.MotorbikeDto;
import com.example.assignment.data.repository.StockRepository;
import com.example.assignment.data.session.SessionManager;
import com.example.assignment.data.session.UserSession;
import com.example.assignment.ui.stock.adapter.StockListAdapter;
import com.example.assignment.ui.stock.model.StockListItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StockManagementActivity extends AppCompatActivity implements StockListAdapter.OnStockClickListener {

    private SessionManager sessionManager;
    private StockRepository stockRepository;
    private UserSession userSession;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private TextView totalStockText;
    private TextView totalQuantityText;
    private TextView outOfStockText;
    private EditText searchInput;
    private StockListAdapter adapter;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<StockListItem> allItems = new ArrayList<>();
    private boolean isLoading = false;
    private boolean hasLoadedOnce = false;
    private final ActivityResultLauncher<Intent> detailLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadStocks();
                }
            });

    private final ActivityResultLauncher<Intent> createLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadStocks();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_management);

        sessionManager = new SessionManager(getApplicationContext());
        ApiServiceFactory factory = new ApiServiceFactory(sessionManager);
        stockRepository = new StockRepository(factory);
        userSession = sessionManager.getUserSession();

        if (userSession == null || sessionManager.getAccessToken() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        bindViews();
        setupRecycler();
        setupListeners();

        loadStocks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasLoadedOnce) {
            loadStocks();
        }
    }

    private void bindViews() {
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        recyclerView = findViewById(R.id.stockRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
        totalStockText = findViewById(R.id.totalStockText);
        totalQuantityText = findViewById(R.id.totalQuantityText);
        outOfStockText = findViewById(R.id.outOfStockText);
        searchInput = findViewById(R.id.searchInput);
        FloatingActionButton fab = findViewById(R.id.fabAddStock);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateStockActivity.class);
            createLauncher.launch(intent);
        });

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
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
        adapter = new StockListAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(this::loadStocks);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                filterList(s.toString());
            }
        });
    }

    private void loadStocks() {
        if (isLoading) {
            return;
        }

        Long agencyId = userSession.getAgencyId();
        if (agencyId == null) {
            Toast.makeText(this, "Không có thông tin đại lý", Toast.LENGTH_LONG).show();
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        setLoading(true);
        executor.execute(() -> {
            try {
                List<AgencyStockItemDto> stocks = stockRepository.getStocks(agencyId, null, null);
                List<MotorbikeDto> motorbikes = stockRepository.getMotorbikes(1000);

                Map<Long, MotorbikeDto> motorbikeMap = new HashMap<>();
                for (MotorbikeDto motorbike : motorbikes) {
                    motorbikeMap.put(motorbike.getId(), motorbike);
                }

                List<StockListItem> mapped = new ArrayList<>();
                for (AgencyStockItemDto stock : stocks) {
                    MotorbikeDto motorbike = motorbikeMap.get(stock.getMotorbikeId());
                    String name = motorbike != null && motorbike.getName() != null ? motorbike.getName() : "Xe chưa xác định";
                    String model = motorbike != null && motorbike.getModel() != null ? motorbike.getModel() : "";
                    String version = motorbike != null && motorbike.getVersion() != null ? motorbike.getVersion() : "";
                    String imageUrl = null;
                    if (motorbike != null && motorbike.getImages() != null && !motorbike.getImages().isEmpty()) {
                        imageUrl = motorbike.getImages().get(0).getImageUrl();
                    }
                    mapped.add(new StockListItem(
                            stock.getId(),
                            name,
                            model,
                            version,
                            imageUrl,
                            stock.getQuantity(),
                            stock.getPrice()
                    ));
                }

                runOnUiThread(() -> {
                    allItems.clear();
                    allItems.addAll(mapped);
                    filterList(searchInput.getText().toString());
                    updateStats(mapped);
                    hasLoadedOnce = true;
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

    private void updateStats(List<StockListItem> items) {
        totalStockText.setText(String.format(Locale.getDefault(), "%d mục", items.size()));
        int totalQuantity = 0;
        int outOfStock = 0;
        for (StockListItem item : items) {
            totalQuantity += item.getQuantity();
            if (item.getQuantity() == 0) {
                outOfStock++;
            }
        }
        totalQuantityText.setText(String.format(Locale.getDefault(), "Số lượng: %d", totalQuantity));
        outOfStockText.setText(String.format(Locale.getDefault(), "Hết hàng: %d", outOfStock));
    }

    private void filterList(String query) {
        String normalizedQuery = normalize(query);
        List<StockListItem> filtered = new ArrayList<>();
        for (StockListItem item : allItems) {
            String combined = normalize(item.getMotorbikeName() + " " + item.getModel() + " " + item.getVersion());
            if (combined.contains(normalizedQuery)) {
                filtered.add(item);
            }
        }
        adapter.submitList(filtered);
        emptyView.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private String normalize(String input) {
        if (input == null) return "";
        String normalized = Normalizer.normalize(input.toLowerCase(Locale.getDefault()), Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
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
    public void onStockClick(StockListItem item) {
        Intent intent = new Intent(this, StockDetailActivity.class);
        intent.putExtra(StockDetailActivity.EXTRA_STOCK_ID, item.getId());
        detailLauncher.launch(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}


