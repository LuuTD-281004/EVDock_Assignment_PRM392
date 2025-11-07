package com.example.assignment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.content.Intent;
import android.widget.ArrayAdapter;
import com.google.android.material.appbar.MaterialToolbar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.assignment.data.remote.ApiServiceFactory;
import com.example.assignment.data.remote.dto.stock.AgencyStockDetailDto;
import com.example.assignment.data.remote.dto.stock.AgencyStockItemDto;
import com.example.assignment.data.remote.dto.stock.motorbike.MotorbikeDto;
import com.example.assignment.data.repository.MotorbikeCatalogRepository;
import com.example.assignment.data.repository.StockRepository;
import com.example.assignment.data.session.SessionManager;
import com.example.assignment.data.session.UserRole;
import com.example.assignment.data.session.UserSession;
import com.example.assignment.ui.catalog.VehicleCardAdapter;
import com.example.assignment.ui.catalog.VehicleItem;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CatalogActivity extends AppCompatActivity implements VehicleCardAdapter.OnVehicleClickListener {

    private static final int LIMIT = 1000;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private SessionManager sessionManager;
    private StockRepository stockRepository;
    private MotorbikeCatalogRepository motorbikeRepository;
    private UserSession userSession;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private TextView headerSubtitle;
    private TextView resultCount;
    private EditText searchInput;
    private Spinner versionSpinner;
    private Button btnOpenQuotations;
    private VehicleCardAdapter adapter;

    private final List<VehicleItem> allVehicles = new ArrayList<>();
    private Map<String, String> versionsMap = new HashMap<>();

    private final ActivityResultLauncher<Intent> orderLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadVehicles();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        sessionManager = new SessionManager(getApplicationContext());
        ApiServiceFactory factory = new ApiServiceFactory(sessionManager);
        stockRepository = new StockRepository(factory);
        motorbikeRepository = new MotorbikeCatalogRepository(factory);
        userSession = sessionManager.getUserSession();

        bindViews();
        setupRecycler();
        setupVersionSpinner();
        setupSearch();

        loadVehicles();
    }

    private void bindViews() {
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        recyclerView = findViewById(R.id.vehicleRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
        headerSubtitle = findViewById(R.id.headerSubtitle);
        resultCount = findViewById(R.id.resultCount);
        searchInput = findViewById(R.id.searchInput);
        versionSpinner = findViewById(R.id.versionSpinner);
        btnOpenQuotations = findViewById(R.id.btnOpenQuotations);
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);

        String role = userSession != null ? userSession.getRole().name() : "unknown";
        headerSubtitle.setText(String.format(Locale.getDefault(), "Signed in as %s", role));

        swipeRefreshLayout.setOnRefreshListener(this::loadVehicles);

        btnOpenQuotations.setOnClickListener(v -> {
            Intent intent = new Intent(this, QuotationListActivity.class);
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
    }

    private void setupRecycler() {
        adapter = new VehicleCardAdapter(this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);
    }

    private void setupVersionSpinner() {
        versionsMap.clear();
        versionsMap.put("All Versions", "ALL");
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new ArrayList<>(versionsMap.keySet()));
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        versionSpinner.setAdapter(spinnerAdapter);
        versionSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                filterAndDisplay();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });
    }

    private void setupSearch() {
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

    private void loadVehicles() {
        setLoading(true);
        executor.execute(() -> {
            try {
                List<VehicleItem> items;
                if (userSession != null && userSession.getRole() == UserRole.DEALER_STAFF && userSession.getAgencyId() != null) {
                    items = loadFromStockAPI(userSession.getAgencyId());
                } else {
                    items = loadFromMotorbikeCatalog();
                }
                runOnUiThread(() -> {
                    allVehicles.clear();
                    allVehicles.addAll(items);
                    updateVersions(items);
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

    private List<VehicleItem> loadFromStockAPI(long agencyId) throws IOException {
        List<VehicleItem> items = new ArrayList<>();
        List<AgencyStockItemDto> stocks = stockRepository.getStocks(agencyId, null, null);
        Map<Long, MotorbikeDto> motorbikeMap = new HashMap<>();
        List<MotorbikeDto> motorbikes = stockRepository.getMotorbikes(1000);
        for (MotorbikeDto motorbike : motorbikes) {
            motorbikeMap.put(motorbike.getId(), motorbike);
        }
        for (AgencyStockItemDto stock : stocks) {
            MotorbikeDto motorbike = motorbikeMap.get(stock.getMotorbikeId());
            if (motorbike == null) {
                continue;
            }
            AgencyStockDetailDto detail = stockRepository.getStockDetail(stock.getId());
            VehicleItem item = VehicleItem.fromStock(stock, motorbike, detail);
            items.add(item);
        }
        return items;
    }

    private List<VehicleItem> loadFromMotorbikeCatalog() throws IOException {
        List<VehicleItem> items = new ArrayList<>();
        List<MotorbikeDto> motorbikes = motorbikeRepository.getMotorbikes(LIMIT);
        for (MotorbikeDto motorbike : motorbikes) {
            VehicleItem item = VehicleItem.fromMotorbike(motorbike);
            items.add(item);
        }
        return items;
    }

    private void updateVersions(List<VehicleItem> items) {
        versionsMap.clear();
        versionsMap.put("All Versions", "ALL");
        for (VehicleItem item : items) {
            String version = item.getVersion();
            if (version != null && !version.trim().isEmpty()) {
                versionsMap.put(version, version);
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new ArrayList<>(versionsMap.keySet()));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        versionSpinner.setAdapter(adapter);
    }

    private void filterAndDisplay() {
        String query = searchInput.getText().toString().trim().toLowerCase(Locale.getDefault());
        String selectedVersion = versionsMap.getOrDefault((String) versionSpinner.getSelectedItem(), "ALL");

        List<VehicleItem> filtered = new ArrayList<>();
        for (VehicleItem item : allVehicles) {
            boolean matchesVersion = selectedVersion.equals("ALL") || (item.getVersion() != null && item.getVersion().equalsIgnoreCase(selectedVersion));
            boolean matchesQuery = query.isEmpty() || item.matchesQuery(query);
            if (matchesVersion && matchesQuery) {
                filtered.add(item);
            }
        }

        adapter.submitList(filtered);
        emptyView.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        resultCount.setText(String.format(Locale.getDefault(), "%d results", filtered.size()));
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        swipeRefreshLayout.setRefreshing(loading);
    }

    @Override
    public void onVehicleClick(VehicleItem item) {
        if (userSession == null || userSession.getRole() != UserRole.DEALER_MANAGER) {
            Toast.makeText(this, "Chức năng chỉ dành cho Dealer Manager", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(this, OrderRestockManagerCreateActivity.class);
        intent.putExtra(OrderRestockManagerCreateActivity.EXTRA_PRESELECTED_VEHICLE, item);
        orderLauncher.launch(intent);
    }

    private void performLogout() {
        sessionManager.clearSession();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
