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
import com.example.assignment.data.remote.dto.order.OrderRestockDetailDto;
import com.example.assignment.data.remote.dto.order.OrderRestockItemDetailDto;
import com.example.assignment.data.remote.dto.order.OrderRestockItemDto;
import com.example.assignment.data.repository.OrderRestockRepository;
import com.example.assignment.data.session.SessionManager;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrderRestockDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_ID = "extra_order_id";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private OrderRestockRepository repository;
    private long orderId;

    private TextView orderTitle;
    private TextView orderStatus;
    private TextView orderDate;
    private TextView agencyInfo;
    private LinearLayout itemsContainer;
    private TextView totalAmount;
    private TextView paymentInfo;
    private ProgressBar progressBar;
    private Button btnNextStatus;
    private Button btnCheckCredit;
    private Button btnCancelOrder;
    private Button btnDeleteOrder;

    private OrderRestockDetailDto currentOrder;
    private final Map<Long, OrderRestockItemDetailDto> itemDetailCache = new HashMap<>();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_restock_detail);

        orderId = getIntent().getLongExtra(EXTRA_ORDER_ID, -1);
        if (orderId == -1) {
            Toast.makeText(this, "Thiếu thông tin đơn hàng", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        SessionManager sessionManager = new SessionManager(getApplicationContext());
        ApiServiceFactory factory = new ApiServiceFactory(sessionManager);
        repository = new OrderRestockRepository(factory);

        bindViews();
        setupListeners();
        loadOrder();
    }

    private void bindViews() {
        orderTitle = findViewById(R.id.orderTitle);
        orderStatus = findViewById(R.id.orderStatus);
        orderDate = findViewById(R.id.orderDate);
        agencyInfo = findViewById(R.id.agencyInfo);
        itemsContainer = findViewById(R.id.itemsContainer);
        totalAmount = findViewById(R.id.totalAmount);
        paymentInfo = findViewById(R.id.paymentInfo);
        progressBar = findViewById(R.id.progressBar);
        btnNextStatus = findViewById(R.id.btnNextStatus);
        btnCheckCredit = findViewById(R.id.btnCheckCredit);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);
        btnDeleteOrder = findViewById(R.id.btnDeleteOrder);
    }

    private void setupListeners() {
        btnNextStatus.setOnClickListener(v -> confirmAction("Chuyển trạng thái", "Bạn có chắc muốn chuyển trạng thái đơn hàng?", () -> {
            String nextStatus = getNextStatus(currentOrder != null ? currentOrder.getStatus() : null);
            if (nextStatus == null) {
                Toast.makeText(this, "Không thể chuyển trạng thái", Toast.LENGTH_LONG).show();
                return;
            }
            performUpdateStatus(nextStatus);
        }));

        btnCheckCredit.setOnClickListener(v -> confirmAction("Check credit", "Bạn có chắc muốn check credit đơn hàng này?", this::performCheckCredit));

        btnCancelOrder.setOnClickListener(v -> confirmAction("Hủy đơn hàng", "Bạn có chắc muốn hủy đơn hàng này?", () -> performUpdateStatus("CANCELED")));

        btnDeleteOrder.setOnClickListener(v -> confirmAction("Xóa đơn hàng", "Bạn có chắc muốn xóa đơn hàng này?", this::performDelete));
    }

    private void loadOrder() {
        setLoading(true);
        executor.execute(() -> {
            try {
                OrderRestockDetailDto detail = repository.getOrderDetail(orderId);
                runOnUiThread(() -> {
                    currentOrder = detail;
                    bindOrder(detail);
                    setLoading(false);
                });
                loadItemDetails(detail.getOrderItems());
            } catch (IOException e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        });
    }

    private void loadItemDetails(List<OrderRestockItemDto> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            return;
        }

        executor.execute(() -> {
            for (OrderRestockItemDto item : orderItems) {
                long itemId = item.getId();
                if (itemDetailCache.containsKey(itemId)) {
                    continue;
                }
                try {
                    OrderRestockItemDetailDto detail = repository.getOrderItemDetail(itemId);
                    itemDetailCache.put(itemId, detail);
                } catch (IOException ignored) {
                }
            }
            runOnUiThread(() -> {
                if (currentOrder != null) {
                    bindItems(currentOrder.getOrderItems());
                }
            });
        });
    }

    private void bindOrder(OrderRestockDetailDto detail) {
        orderTitle.setText(String.format(Locale.getDefault(), "Order #%d", detail.getId()));
        orderStatus.setText(String.format(Locale.getDefault(), "Status: %s", detail.getStatus()));
        orderDate.setText(String.format(Locale.getDefault(), "Ngày đặt: %s", detail.getOrderAt()));
        if (detail.getAgency() != null) {
            agencyInfo.setText(String.format(Locale.getDefault(), "Agency: %s", detail.getAgency().getName()));
        } else if (detail.getAgencyId() != null) {
            agencyInfo.setText(String.format(Locale.getDefault(), "Agency ID: %d", detail.getAgencyId()));
        } else {
            agencyInfo.setText("Agency: -");
        }

        totalAmount.setText(String.format(Locale.getDefault(), "Tổng tiền: %s", currencyFormat.format(detail.getSubtotal())));

        if (detail.getAgencyBill() != null) {
            paymentInfo.setText(String.format(Locale.getDefault(), "Hóa đơn #%d - %s", detail.getAgencyBill().getId(), detail.getAgencyBill().getStatus()));
        } else if (detail.getPaymentStatus() != null) {
            paymentInfo.setText(String.format(Locale.getDefault(), "Thanh toán: %s", detail.getPaymentStatus()));
        } else {
            paymentInfo.setText("Thanh toán: -");
        }

        bindItems(detail.getOrderItems());
        updateActionVisibility(detail.getStatus());
    }

    private void bindItems(List<OrderRestockItemDto> orderItems) {
        itemsContainer.removeAllViews();
        if (orderItems == null || orderItems.isEmpty()) {
            TextView textView = new TextView(this);
            textView.setText("Không có mặt hàng");
            itemsContainer.addView(textView);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (OrderRestockItemDto item : orderItems) {
            View view = inflater.inflate(R.layout.view_order_item, itemsContainer, false);
            TextView motorbike = view.findViewById(R.id.itemMotorbike);
            TextView warehouse = view.findViewById(R.id.itemWarehouse);
            TextView color = view.findViewById(R.id.itemColor);
            TextView quantity = view.findViewById(R.id.itemQuantity);
            TextView total = view.findViewById(R.id.itemTotal);

            OrderRestockItemDetailDto detail = itemDetailCache.get(item.getId());

            if (detail != null && detail.getElectricMotorbike() != null) {
                motorbike.setText(String.format(Locale.getDefault(), "Xe: %s", detail.getElectricMotorbike().getName()));
            } else {
                motorbike.setText(String.format(Locale.getDefault(), "Xe ID: %d", item.getElectricMotorbikeId() != null ? item.getElectricMotorbikeId() : 0));
            }

            if (detail != null && detail.getWarehouse() != null) {
                warehouse.setText(String.format(Locale.getDefault(), "Kho: %s", detail.getWarehouse().getName()));
            } else {
                warehouse.setText(String.format(Locale.getDefault(), "Kho ID: %d", item.getWarehouseId() != null ? item.getWarehouseId() : 0));
            }

            if (detail != null && detail.getColor() != null) {
                color.setText(String.format(Locale.getDefault(), "Màu: %s", detail.getColor().getColorType()));
            } else {
                color.setText("Màu: -");
            }

            quantity.setText(String.format(Locale.getDefault(), "Số lượng: %d", item.getQuantity()));
            total.setText(String.format(Locale.getDefault(), "Tổng: %s", currencyFormat.format(item.getTotalPrice())));

            itemsContainer.addView(view);
        }
    }

    private void updateActionVisibility(@Nullable String status) {
        String current = status != null ? status.toUpperCase(Locale.getDefault()) : "";
        String nextStatus = getNextStatus(current);

        if (nextStatus != null) {
            btnNextStatus.setText(getActionLabel(nextStatus));
            btnNextStatus.setVisibility(View.VISIBLE);
        } else {
            btnNextStatus.setVisibility(View.GONE);
        }

        boolean showCheckCredit = "PENDING".equals(current)
                && currentOrder != null
                && !currentOrder.isCreditChecked();
        btnCheckCredit.setVisibility(showCheckCredit ? View.VISIBLE : View.GONE);
        if (showCheckCredit) {
            btnCheckCredit.setText("Check credit");
        }

        btnCancelOrder.setVisibility(!"CANCELED".equals(current) && !"DELIVERED".equals(current) ? View.VISIBLE : View.GONE);
    }

    private String getStatusLabel(String status) {
        if (status == null) {
            return "";
        }
        switch (status.toUpperCase(Locale.getDefault())) {
            case "PENDING":
                return "Pending";
            case "APPROVED":
                return "Approved";
            case "DELIVERED":
                return "Delivered";
            default:
                return status;
        }
    }

    private String getActionLabel(String status) {
        if (status == null) {
            return "";
        }
        switch (status.toUpperCase(Locale.getDefault())) {
            case "APPROVED":
                return "Approve order";
            case "DELIVERED":
                return "Deliver to dealer";
            default:
                return getStatusLabel(status);
        }
    }

    private String getNextStatus(@Nullable String status) {
        if (status == null) {
            return null;
        }
        switch (status.toUpperCase(Locale.getDefault())) {
            case "PENDING":
                return "APPROVED";
            case "APPROVED":
                return "DELIVERED";
            default:
                return null;
        }
    }

    private void performUpdateStatus(String nextStatus) {
        setLoading(true);
        executor.execute(() -> {
            try {
                OrderRestockDetailDto detail = repository.updateStatus(orderId, nextStatus);
                itemDetailCache.clear();
                runOnUiThread(() -> {
                    currentOrder = detail;
                    bindOrder(detail);
                    setLoading(false);
                    setResult(RESULT_OK);
                    Toast.makeText(this, "Đã cập nhật trạng thái", Toast.LENGTH_LONG).show();
                });
                loadItemDetails(detail.getOrderItems());
            } catch (IOException e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void performCheckCredit() {
        setLoading(true);
        executor.execute(() -> {
            try {
                OrderRestockDetailDto detail = repository.checkCredit(orderId);
                runOnUiThread(() -> {
                    currentOrder = detail;
                    bindOrder(detail);
                    setLoading(false);
                    setResult(RESULT_OK);
                    Toast.makeText(this, "Đã check credit", Toast.LENGTH_LONG).show();
                });
                loadItemDetails(detail.getOrderItems());
            } catch (IOException e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void performDelete() {
        setLoading(true);
        executor.execute(() -> {
            try {
                repository.deleteOrder(orderId);
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, "Đã xóa đơn hàng", Toast.LENGTH_LONG).show();
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

    private void confirmAction(String title, String message, Runnable action) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Đồng ý", (dialog, which) -> action.run())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnNextStatus.setEnabled(!loading);
        btnCheckCredit.setEnabled(!loading);
        btnCancelOrder.setEnabled(!loading);
        btnDeleteOrder.setEnabled(!loading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}


