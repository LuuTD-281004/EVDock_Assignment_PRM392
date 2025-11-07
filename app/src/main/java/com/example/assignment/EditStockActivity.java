package com.example.assignment;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.assignment.data.remote.ApiServiceFactory;
import com.example.assignment.data.remote.dto.stock.AgencyStockDetailDto;
import com.example.assignment.data.repository.StockRepository;
import com.example.assignment.data.session.SessionManager;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditStockActivity extends AppCompatActivity {

    public static final String EXTRA_STOCK_ID = "stock_id";

    private TextView stockInfo;
    private EditText inputQuantity;
    private EditText inputPrice;
    private ProgressBar progressBar;
    private Button btnSave;

    private StockRepository stockRepository;
    private long stockId;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_stock);

        stockId = getIntent().getLongExtra(EXTRA_STOCK_ID, -1);
        if (stockId == -1) {
            Toast.makeText(this, "Thiếu thông tin tồn kho", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        SessionManager sessionManager = new SessionManager(getApplicationContext());
        ApiServiceFactory factory = new ApiServiceFactory(sessionManager);
        stockRepository = new StockRepository(factory);

        stockInfo = findViewById(R.id.stockInfo);
        inputQuantity = findViewById(R.id.inputQuantity);
        inputPrice = findViewById(R.id.inputPrice);
        progressBar = findViewById(R.id.progressBar);
        btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(v -> updateStock());

        loadStockDetail();
    }

    private void loadStockDetail() {
        setLoading(true);
        executor.execute(() -> {
            try {
                AgencyStockDetailDto detail = stockRepository.getStockDetail(stockId);
                runOnUiThread(() -> {
                    setLoading(false);
                    if (detail == null) {
                        Toast.makeText(this, "Không tìm thấy tồn kho", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                    String info = String.format(Locale.getDefault(), "%s - %s", 
                            detail.getMotorbike() != null ? detail.getMotorbike().getName() : "Xe",
                            detail.getColor() != null ? detail.getColor().getColorType() : "Màu không xác định");
                    stockInfo.setText(info);
                    inputQuantity.setText(String.valueOf(detail.getQuantity()));
                    inputPrice.setText(NumberFormat.getNumberInstance(Locale.getDefault()).format(detail.getPrice()));
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

    private void updateStock() {
        String quantityStr = inputQuantity.getText().toString().trim();
        String priceStr = inputPrice.getText().toString().trim().replace(",", "");

        if (quantityStr.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_LONG).show();
            return;
        }

        int quantity;
        double price;
        try {
            quantity = Integer.parseInt(quantityStr);
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá trị không hợp lệ", Toast.LENGTH_LONG).show();
            return;
        }

        if (quantity < 0 || price <= 0) {
            Toast.makeText(this, "Số lượng hoặc giá không hợp lệ", Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận")
                .setMessage("Bạn có chắc muốn cập nhật tồn kho?")
                .setPositiveButton("Đồng ý", (dialog, which) -> performUpdate(quantity, price))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void performUpdate(int quantity, double price) {
        setLoading(true);
        executor.execute(() -> {
            try {
                stockRepository.updateStock(stockId, quantity, price);
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_LONG).show();
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

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!loading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}


