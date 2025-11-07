package com.example.assignment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.assignment.data.remote.ApiServiceFactory;
import com.example.assignment.data.remote.dto.stock.AgencyStockDetailDto;
import com.example.assignment.data.remote.dto.stock.promotion.StockPromotionWrapperDto;
import com.example.assignment.data.repository.StockRepository;
import com.example.assignment.data.session.SessionManager;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StockDetailActivity extends AppCompatActivity {

    public static final String EXTRA_STOCK_ID = "stock_id";

    private TextView motorbikeName;
    private TextView motorbikeInfo;
    private ImageView motorbikeImage;
    private TextView colorText;
    private TextView quantityText;
    private TextView priceText;
    private TextView createdText;
    private TextView updatedText;
    private TextView promotionHeader;
    private LinearLayout promotionContainer;
    private ProgressBar progressBar;
    private Button btnEdit;
    private Button btnDelete;

    private StockRepository stockRepository;
    private long stockId;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));

    private final ActivityResultLauncher<Intent> editLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadStockDetail();
                    setResult(RESULT_OK);
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);

        stockId = getIntent().getLongExtra(EXTRA_STOCK_ID, -1);
        if (stockId == -1) {
            Toast.makeText(this, "Thiếu thông tin tồn kho", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        SessionManager sessionManager = new SessionManager(getApplicationContext());
        ApiServiceFactory factory = new ApiServiceFactory(sessionManager);
        stockRepository = new StockRepository(factory);

        bindViews();
        setupListeners();
        loadStockDetail();
    }

    private void bindViews() {
        motorbikeName = findViewById(R.id.motorbikeName);
        motorbikeInfo = findViewById(R.id.motorbikeInfo);
        motorbikeImage = findViewById(R.id.motorbikeImage);
        colorText = findViewById(R.id.colorText);
        quantityText = findViewById(R.id.quantityText);
        priceText = findViewById(R.id.priceText);
        createdText = findViewById(R.id.createdText);
        updatedText = findViewById(R.id.updatedText);
        promotionHeader = findViewById(R.id.promotionHeader);
        promotionContainer = findViewById(R.id.promotionContainer);
        progressBar = findViewById(R.id.progressBar);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
    }

    private void setupListeners() {
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditStockActivity.class);
            intent.putExtra(EditStockActivity.EXTRA_STOCK_ID, stockId);
            editLauncher.launch(intent);
        });

        btnDelete.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Xác nhận")
                .setMessage("Bạn có chắc muốn xóa tồn kho này?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteStock())
                .setNegativeButton("Hủy", null)
                .show());
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
                    bindDetail(detail);
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

    private void bindDetail(AgencyStockDetailDto detail) {
        String name = detail.getMotorbike() != null ? detail.getMotorbike().getName() : "Xe";
        motorbikeName.setText(name);

        String model = detail.getMotorbike() != null ? detail.getMotorbike().getModel() : null;
        String version = detail.getMotorbike() != null ? detail.getMotorbike().getVersion() : null;
        motorbikeInfo.setText(String.format(Locale.getDefault(), "%s - %s",
                model != null ? model : "Không rõ",
                version != null ? version : "Không rõ"));

        String imageUrl = null;
        if (detail.getMotorbike() != null && detail.getMotorbike().getImages() != null
                && !detail.getMotorbike().getImages().isEmpty()) {
            imageUrl = detail.getMotorbike().getImages().get(0).getImageUrl();
        }

        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.bg_placeholder)
                .into(motorbikeImage);

        colorText.setText(String.format(Locale.getDefault(), "Màu: %s",
                detail.getColor() != null ? detail.getColor().getColorType() : "Không xác định"));
        quantityText.setText(String.format(Locale.getDefault(), "Số lượng: %d", detail.getQuantity()));
        priceText.setText(currencyFormat.format(detail.getPrice()));
        createdText.setText(String.format(Locale.getDefault(), "Tạo lúc: %s", formatDate(detail.getCreateAt())));
        updatedText.setText(String.format(Locale.getDefault(), "Cập nhật: %s", formatDate(detail.getUpdateAt())));

        List<StockPromotionWrapperDto> promotions = detail.getPromotions();
        promotionContainer.removeAllViews();
        if (promotions != null && !promotions.isEmpty()) {
            promotionHeader.setVisibility(View.VISIBLE);
            for (StockPromotionWrapperDto wrapper : promotions) {
                View promoView = getLayoutInflater().inflate(R.layout.view_promotion_item, promotionContainer, false);
                TextView title = promoView.findViewById(R.id.promoTitle);
                TextView value = promoView.findViewById(R.id.promoValue);
                TextView status = promoView.findViewById(R.id.promoStatus);

                if (wrapper.getStockPromotion() != null) {
                    title.setText(wrapper.getStockPromotion().getName());
                    String valueText = wrapper.getStockPromotion().getValueType().equalsIgnoreCase("PERCENT")
                            ? wrapper.getStockPromotion().getValue() + "%"
                            : currencyFormat.format(wrapper.getStockPromotion().getValue());
                    value.setText(valueText);
                    status.setText(wrapper.getStockPromotion().getStatus());
                }

                promotionContainer.addView(promoView);
            }
        } else {
            promotionHeader.setVisibility(View.GONE);
        }
    }

    private String formatDate(String input) {
        if (input == null) {
            return "-";
        }
        try {
            OffsetDateTime dateTime = OffsetDateTime.parse(input);
            return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } catch (DateTimeParseException e) {
            return input;
        }
    }

    private void deleteStock() {
        setLoading(true);
        executor.execute(() -> {
            try {
                stockRepository.deleteStock(stockId);
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, "Đã xóa tồn kho", Toast.LENGTH_LONG).show();
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
        btnEdit.setEnabled(!loading);
        btnDelete.setEnabled(!loading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}


