package com.example.assignment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.assignment.data.remote.ApiServiceFactory;
import com.example.assignment.data.remote.dto.order.OrderRestockColorDto;
import com.example.assignment.data.remote.dto.order.OrderRestockMotorbikeDto;
import com.example.assignment.data.remote.dto.order.OrderRestockWarehouseDto;
import com.example.assignment.data.remote.dto.order.manager.ManagerOrderItemDetailDto;
import com.example.assignment.data.remote.dto.order.manager.ManagerOrderItemDto;
import com.example.assignment.data.remote.dto.order.manager.ManagerOrderSummaryDto;
import com.example.assignment.data.remote.dto.stock.AgencyStockItemDto;
import com.example.assignment.data.repository.OrderRestockManagerRepository;
import com.example.assignment.data.repository.StockRepository;
import com.example.assignment.data.session.SessionManager;
import com.example.assignment.data.session.UserRole;
import com.example.assignment.data.session.UserSession;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrderRestockManagerDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER = "extra_order";
    public static final String EXTRA_FIRST_ITEM_ID = "extra_first_item_id";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private OrderRestockManagerRepository repository;
    private StockRepository stockRepository;
    private SessionManager sessionManager;
    private UserSession userSession;
    private ManagerOrderSummaryDto order;
    private long firstItemId;

    private TextView orderTitle;
    private TextView orderStatus;
    private TextView orderDate;
    private TextView agencyInfo;
    private LinearLayout itemsContainer;
    private TextView totalAmount;
    private ProgressBar progressBar;
    private Button btnAccept;
    private Button btnCancel;
    private Button btnAddStock;

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));
    private final List<ManagerOrderItemDetailDto> currentItemDetails = new ArrayList<>();
    private boolean stockUpdated = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_order_restock_detail);

        sessionManager = new SessionManager(getApplicationContext());
        userSession = sessionManager.getUserSession();
        if (userSession == null || userSession.getRole() != UserRole.DEALER_MANAGER) {
            Toast.makeText(this, "Không đủ quyền", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        ApiServiceFactory factory = new ApiServiceFactory(sessionManager);
        repository = new OrderRestockManagerRepository(factory);
        stockRepository = new StockRepository(factory);

        order = (ManagerOrderSummaryDto) getIntent().getSerializableExtra(EXTRA_ORDER);
        firstItemId = getIntent().getLongExtra(EXTRA_FIRST_ITEM_ID, 0);

        if (order == null) {
            Toast.makeText(this, "Không tìm thấy thông tin đơn hàng", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        bindViews();
        bindOrder();
        loadItemDetails();
    }

    private void bindViews() {
        orderTitle = findViewById(R.id.orderTitle);
        orderStatus = findViewById(R.id.orderStatus);
        orderDate = findViewById(R.id.orderDate);
        agencyInfo = findViewById(R.id.agencyInfo);
        itemsContainer = findViewById(R.id.itemsContainer);
        totalAmount = findViewById(R.id.totalAmount);
        progressBar = findViewById(R.id.progressBar);
        btnAccept = findViewById(R.id.btnAccept);
        btnCancel = findViewById(R.id.btnCancel);
        btnAddStock = findViewById(R.id.btnAddStock);

        btnAccept.setOnClickListener(v -> confirmAndRun("Xác nhận đơn hàng", "Bạn có chắc muốn xác nhận đơn hàng này?", this::performAccept));
        btnCancel.setOnClickListener(v -> confirmAndRun("Hủy đơn hàng", "Bạn có chắc muốn hủy đơn hàng này?", this::performCancel));
        btnAddStock.setOnClickListener(v -> confirmAndRun("Cập nhật tồn kho", "Đưa xe từ đơn hàng này vào kho đại lý?", this::performAddStock));
    }

    private void bindOrder() {
        orderTitle.setText(String.format(Locale.getDefault(), "Order #%d", order.getId()));
        orderStatus.setText(String.format(Locale.getDefault(), "Trạng thái: %s", order.getStatus()));
        orderDate.setText(String.format(Locale.getDefault(), "Ngày tạo: %s", order.getOrderAt()));
        if (order.getAgency() != null) {
            agencyInfo.setText(String.format(Locale.getDefault(), "Đại lý: %s", order.getAgency().getName()));
        } else if (order.getAgencyId() != null) {
            agencyInfo.setText(String.format(Locale.getDefault(), "Đại lý ID: %d", order.getAgencyId()));
        } else {
            agencyInfo.setText("Đại lý: -");
        }
        totalAmount.setText(String.format(Locale.getDefault(), "Tổng tiền: %s", currencyFormat.format(order.getSubtotal())));
        updateButtons(order.getStatus());
        renderItems(order.getOrderItems(), null);
    }

    private void updateButtons(String status) {
        String normalized = status != null ? status.toUpperCase(Locale.getDefault()) : "";
        boolean canAccept = normalized.equals("DRAFT") || normalized.equals("PENDING");
        boolean canCancel = !normalized.equals("CANCELED") && !normalized.equals("DELIVERED");

        btnAccept.setEnabled(canAccept);
        btnAccept.setVisibility(canAccept ? View.VISIBLE : View.GONE);
        if (canAccept) {
            btnAccept.setText("Accept");
        }
        btnCancel.setEnabled(canCancel);
        btnCancel.setVisibility(canCancel ? View.VISIBLE : View.GONE);

        boolean canAddStock = normalized.equals("DELIVERED")
                && !stockUpdated
                && userSession.getAgencyId() != null
                && !currentItemDetails.isEmpty();
        btnAddStock.setEnabled(canAddStock);
        btnAddStock.setVisibility(canAddStock ? View.VISIBLE : View.GONE);
    }

    private void loadItemDetails() {
        List<ManagerOrderItemDto> items = order.getOrderItems();
        if (items == null || items.isEmpty()) {
            updateButtons(order.getStatus());
            return;
        }

        setLoading(true);
        executor.execute(() -> {
            try {
                List<ManagerOrderItemDetailDto> details = new ArrayList<>();
                for (ManagerOrderItemDto item : items) {
                    ManagerOrderItemDetailDto detail = repository.getOrderItemDetail(item.getId());
                    details.add(detail);
                }
                runOnUiThread(() -> {
                    currentItemDetails.clear();
                    currentItemDetails.addAll(details);
                    renderItems(items, details);
                    setLoading(false);
                    updateButtons(order.getStatus());
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                    updateButtons(order.getStatus());
                });
            }
        });
    }

    private void renderItems(List<ManagerOrderItemDto> items, @Nullable List<ManagerOrderItemDetailDto> details) {
        itemsContainer.removeAllViews();
        if (items == null || items.isEmpty()) {
            TextView label = new TextView(this);
            label.setText("Không có mặt hàng");
            itemsContainer.addView(label);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < items.size(); i++) {
            ManagerOrderItemDto item = items.get(i);
            ManagerOrderItemDetailDto detail = details != null && i < details.size() ? details.get(i) : null;

            View view = inflater.inflate(R.layout.view_order_item, itemsContainer, false);
            TextView motorbike = view.findViewById(R.id.itemMotorbike);
            TextView warehouse = view.findViewById(R.id.itemWarehouse);
            TextView color = view.findViewById(R.id.itemColor);
            TextView quantity = view.findViewById(R.id.itemQuantity);
            TextView total = view.findViewById(R.id.itemTotal);

            if (detail != null) {
                OrderRestockMotorbikeDto mb = detail.getMotorbike();
                motorbike.setText(String.format(Locale.getDefault(), "Xe: %s", mb != null ? mb.getName() : "-"));
                OrderRestockWarehouseDto wh = detail.getWarehouse();
                warehouse.setText(String.format(Locale.getDefault(), "Kho: %s", wh != null ? wh.getName() : "-"));
                OrderRestockColorDto colorDto = detail.getColor();
                color.setText(String.format(Locale.getDefault(), "Màu: %s", colorDto != null ? colorDto.getColorType() : "-"));
                quantity.setText(String.format(Locale.getDefault(), "Số lượng: %d", detail.getQuantity()));
                total.setText(String.format(Locale.getDefault(), "Đơn giá: %s", currencyFormat.format(detail.getPrice())));
            } else {
                motorbike.setText(String.format(Locale.getDefault(), "Xe ID: %d", item.getMotorbikeId() != null ? item.getMotorbikeId() : 0));
                warehouse.setText(String.format(Locale.getDefault(), "Kho ID: %d", item.getWarehouseId() != null ? item.getWarehouseId() : 0));
                color.setText(String.format(Locale.getDefault(), "Màu ID: %d", item.getColorId() != null ? item.getColorId() : 0));
                quantity.setText(String.format(Locale.getDefault(), "Số lượng: %d", item.getQuantity()));
                total.setText("Đơn giá: -");
            }

            itemsContainer.addView(view);
        }
    }

    private void performAccept() {
        setLoading(true);
        executor.execute(() -> {
            try {
                ManagerOrderSummaryDto updated = repository.acceptOrder(order.getId());
                runOnUiThread(() -> {
                    order = updated;
                    stockUpdated = false;
                    bindOrder();
                    setResult(RESULT_OK);
                    setLoading(false);
                    Toast.makeText(this, "Đã xác nhận đơn hàng", Toast.LENGTH_LONG).show();
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void performCancel() {
        setLoading(true);
        executor.execute(() -> {
            try {
                repository.deleteOrder(order.getId());
                runOnUiThread(() -> {
                    setResult(RESULT_OK);
                    setLoading(false);
                    Toast.makeText(this, "Đã hủy đơn hàng", Toast.LENGTH_LONG).show();
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

    private void performAddStock() {
        Long agencyId = userSession.getAgencyId();
        if (agencyId == null) {
            Toast.makeText(this, "Không tìm thấy thông tin đại lý", Toast.LENGTH_LONG).show();
            return;
        }
        if (currentItemDetails.isEmpty()) {
            Toast.makeText(this, "Không có dữ liệu mặt hàng", Toast.LENGTH_LONG).show();
            return;
        }

        setLoading(true);
        executor.execute(() -> {
            try {
                for (ManagerOrderItemDetailDto detail : currentItemDetails) {
                    if (detail == null) {
                        continue;
                    }
                    OrderRestockMotorbikeDto mb = detail.getMotorbike();
                    OrderRestockColorDto color = detail.getColor();
                    if (mb == null || color == null) {
                        continue;
                    }
                    long motorbikeId = mb.getId();
                    long colorId = color.getId();
                    int quantity = detail.getQuantity();
                    if (quantity <= 0) {
                        continue;
                    }
                    double price = detail.getPrice();
                    if (price <= 0) {
                        int totalQuantity = order.getQuantity();
                        price = order.getSubtotal() > 0 && totalQuantity > 0
                                ? order.getSubtotal() / totalQuantity
                                : 1d;
                    }

                    List<AgencyStockItemDto> existing = stockRepository.getStocks(agencyId, motorbikeId, colorId);
                    if (!existing.isEmpty()) {
                        AgencyStockItemDto stock = existing.get(0);
                        int newQuantity = stock.getQuantity() + quantity;
                        stockRepository.updateStock(stock.getId(), newQuantity, price);
                    } else {
                        stockRepository.createStock(agencyId, motorbikeId, colorId, quantity, price);
                    }
                }

                runOnUiThread(() -> {
                    setLoading(false);
                    stockUpdated = true;
                    updateButtons(order.getStatus());
                    Toast.makeText(this, "Đã cập nhật tồn kho", Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK);
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
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Đồng ý", (dialog, which) -> action.run())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnAccept.setEnabled(!loading && btnAccept.getVisibility() == View.VISIBLE);
        btnCancel.setEnabled(!loading && btnCancel.getVisibility() == View.VISIBLE);
        btnAddStock.setEnabled(!loading && btnAddStock.getVisibility() == View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}


