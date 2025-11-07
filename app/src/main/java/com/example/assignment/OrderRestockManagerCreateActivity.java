package com.example.assignment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.android.material.appbar.MaterialToolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.assignment.data.remote.ApiServiceFactory;
import com.example.assignment.data.remote.dto.order.manager.CreateManagerOrderRequest;
import com.example.assignment.data.remote.dto.order.manager.ManagerOrderSummaryDto;
import com.example.assignment.data.remote.dto.stock.motorbike.MotorbikeColorDto;
import com.example.assignment.data.remote.dto.stock.motorbike.MotorbikeDto;
import com.example.assignment.data.repository.OrderRestockManagerRepository;
import com.example.assignment.data.repository.StockRepository;
import com.example.assignment.data.repository.WarehouseRepository;
import com.example.assignment.data.remote.service.WarehouseService;
import com.example.assignment.data.session.SessionManager;
import com.example.assignment.data.session.UserRole;
import com.example.assignment.data.session.UserSession;
import com.example.assignment.ui.catalog.VehicleItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrderRestockManagerCreateActivity extends AppCompatActivity {

    public static final String EXTRA_PRESELECTED_VEHICLE = "extra_preselected_vehicle";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private SessionManager sessionManager;
    private OrderRestockManagerRepository managerRepository;
    private StockRepository stockRepository;
    private WarehouseRepository warehouseRepository;
    private UserSession userSession;

    private VehicleItem preselectedVehicle;
    private Long pendingColorId = null;
    private boolean preselectionApplied = false;

    private Spinner spinnerOrderType;
    private Spinner spinnerMotorbike;
    private Spinner spinnerColor;
    private Spinner spinnerWarehouse;
    private EditText inputQuantity;
    private EditText inputDiscountId;
    private EditText inputPromotionId;
    private ProgressBar progressBar;
    private Button btnCreate;
    private MaterialToolbar topAppBar;

    private List<MotorbikeDto> motorbikes = new ArrayList<>();
    private List<MotorbikeColorDto> motorbikeColors = new ArrayList<>();
    private List<WarehouseService.WarehouseDto> warehouses = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_create_order_restock);

        sessionManager = new SessionManager(getApplicationContext());
        ApiServiceFactory factory = new ApiServiceFactory(sessionManager);
        managerRepository = new OrderRestockManagerRepository(factory);
        stockRepository = new StockRepository(factory);
        warehouseRepository = new WarehouseRepository(factory);
        userSession = sessionManager.getUserSession();

        if (userSession == null || userSession.getRole() != UserRole.DEALER_MANAGER) {
            Toast.makeText(this, "Không đủ quyền", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        preselectedVehicle = (VehicleItem) getIntent().getSerializableExtra(EXTRA_PRESELECTED_VEHICLE);

        bindViews();
        setupOrderTypeSpinner();
        setupListeners();
        loadData();
    }

    private void bindViews() {
        spinnerOrderType = findViewById(R.id.spinnerOrderType);
        spinnerMotorbike = findViewById(R.id.spinnerMotorbike);
        spinnerColor = findViewById(R.id.spinnerColor);
        spinnerWarehouse = findViewById(R.id.spinnerWarehouse);
        inputQuantity = findViewById(R.id.inputQuantity);
        inputDiscountId = findViewById(R.id.inputDiscountId);
        inputPromotionId = findViewById(R.id.inputPromotionId);
        progressBar = findViewById(R.id.progressBar);
        btnCreate = findViewById(R.id.btnCreate);
        topAppBar = findViewById(R.id.topAppBar);

        inputQuantity.setText("1");

        if (topAppBar != null) {
            topAppBar.setNavigationIcon(R.drawable.ic_arrow_back);
            topAppBar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        }
    }

    private void setupOrderTypeSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"FULL", "DEFERRED"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrderType.setAdapter(adapter);
    }

    private void setupListeners() {
        spinnerMotorbike.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                loadColorsForMotorbike();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        btnCreate.setOnClickListener(v -> createOrder());
    }

    private void loadData() {
        setLoading(true);
        executor.execute(() -> {
            try {
                motorbikes = stockRepository.getMotorbikes(200);
                warehouses = warehouseRepository.getWarehouses();
                runOnUiThread(() -> {
                    bindMotorbikeSpinner();
                    bindWarehouseSpinner();
                    applyPreselectedVehicle();
                    loadColorsForMotorbike();
                    setLoading(false);
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        });
    }

    private void bindMotorbikeSpinner() {
        List<String> labels = new ArrayList<>();
        for (MotorbikeDto motorbike : motorbikes) {
            labels.add(motorbike.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMotorbike.setAdapter(adapter);
    }

    private void applyPreselectedVehicle() {
        if (preselectedVehicle == null || preselectionApplied) {
            return;
        }

        preselectionApplied = true;

        if (preselectedVehicle.getMotorbikeId() != null) {
            for (int i = 0; i < motorbikes.size(); i++) {
                if (motorbikes.get(i).getId() == preselectedVehicle.getMotorbikeId()) {
                    spinnerMotorbike.setSelection(i);
                    break;
                }
            }
        } else if (!motorbikes.isEmpty()) {
            spinnerMotorbike.setSelection(0);
        }

        if (preselectedVehicle.getColorId() != null) {
            pendingColorId = preselectedVehicle.getColorId();
        }

        if (preselectedVehicle.getQuantity() > 0) {
            inputQuantity.setText(String.valueOf(preselectedVehicle.getQuantity()));
        }

        if (warehouses.size() > 0 && spinnerWarehouse.getSelectedItemPosition() == -1) {
            spinnerWarehouse.setSelection(0);
        }
    }

    private void loadColorsForMotorbike() {
        int position = spinnerMotorbike.getSelectedItemPosition();
        if (position < 0 || position >= motorbikes.size()) {
            bindColorSpinner(new ArrayList<>());
            setLoading(false);
            return;
        }

        setLoading(true);
        executor.execute(() -> {
            try {
                MotorbikeDto motorbike = motorbikes.get(position);
                MotorbikeDto detail = stockRepository.getMotorbikeDetail(motorbike.getId());
                motorbikeColors = detail != null ? detail.getColors() : new ArrayList<>();
                runOnUiThread(() -> {
                    bindColorSpinner(motorbikeColors);
                    applyPendingColorSelection();
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

    private void bindColorSpinner(List<MotorbikeColorDto> colors) {
        List<String> labels = new ArrayList<>();
        for (MotorbikeColorDto color : colors) {
            labels.add(color.getColorType());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerColor.setAdapter(adapter);
    }

    private void bindWarehouseSpinner() {
        List<String> labels = new ArrayList<>();
        for (WarehouseService.WarehouseDto warehouse : warehouses) {
            labels.add(warehouse.name);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWarehouse.setAdapter(adapter);
        if (!warehouses.isEmpty()) {
            spinnerWarehouse.setSelection(0);
        }
    }

    private void applyPendingColorSelection() {
        if (pendingColorId == null || motorbikeColors == null) {
            return;
        }
        for (int i = 0; i < motorbikeColors.size(); i++) {
            if (motorbikeColors.get(i).getColorId() == pendingColorId) {
                spinnerColor.setSelection(i);
                break;
            }
        }
        pendingColorId = null;
    }

    private void createOrder() {
        String quantityText = inputQuantity.getText().toString().trim();
        if (TextUtils.isEmpty(quantityText)) {
            Toast.makeText(this, "Vui lòng nhập số lượng", Toast.LENGTH_LONG).show();
            return;
        }

        if (spinnerMotorbike.getSelectedItemPosition() < 0 || spinnerMotorbike.getSelectedItemPosition() >= motorbikes.size()) {
            Toast.makeText(this, "Vui lòng chọn xe", Toast.LENGTH_LONG).show();
            return;
        }

        if (spinnerColor.getSelectedItemPosition() < 0 || spinnerColor.getSelectedItemPosition() >= motorbikeColors.size()) {
            Toast.makeText(this, "Vui lòng chọn màu", Toast.LENGTH_LONG).show();
            return;
        }

        if (spinnerWarehouse.getSelectedItemPosition() < 0 || spinnerWarehouse.getSelectedItemPosition() >= warehouses.size()) {
            Toast.makeText(this, "Vui lòng chọn kho", Toast.LENGTH_LONG).show();
            return;
        }

        int quantity = Integer.parseInt(quantityText);
        if (quantity <= 0) {
            Toast.makeText(this, "Số lượng phải lớn hơn 0", Toast.LENGTH_LONG).show();
            return;
        }

        setLoading(true);
        executor.execute(() -> {
            try {
                MotorbikeDto motorbike = motorbikes.get(spinnerMotorbike.getSelectedItemPosition());
                MotorbikeColorDto color = motorbikeColors.get(spinnerColor.getSelectedItemPosition());
                WarehouseService.WarehouseDto warehouse = warehouses.get(spinnerWarehouse.getSelectedItemPosition());

                Long discountId = parseOptionalLong(inputDiscountId.getText().toString().trim());
                Long promotionId = parseOptionalLong(inputPromotionId.getText().toString().trim());

                CreateManagerOrderRequest.OrderItem orderItem = new CreateManagerOrderRequest.OrderItem(
                        quantity,
                        warehouse.id,
                        motorbike.getId(),
                        color.getColorId(),
                        discountId,
                        promotionId
                );

                CreateManagerOrderRequest request = new CreateManagerOrderRequest(
                        (String) spinnerOrderType.getSelectedItem(),
                        userSession.getAgencyId(),
                        java.util.Collections.singletonList(orderItem)
                );

                ManagerOrderSummaryDto created = managerRepository.createOrder(request);
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, "Đã tạo đơn hàng #" + (created != null ? created.getId() : ""), Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK);
                    finish();
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private Long parseOptionalLong(String value) {
        if (TextUtils.isEmpty(value)) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnCreate.setEnabled(!loading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}


