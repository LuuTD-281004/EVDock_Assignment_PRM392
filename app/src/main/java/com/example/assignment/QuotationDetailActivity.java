package com.example.assignment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.assignment.data.remote.ApiServiceFactory;
import com.example.assignment.data.remote.dto.quotation.QuotationCustomerDto;
import com.example.assignment.data.remote.dto.quotation.QuotationDetailDto;
import com.example.assignment.data.remote.dto.quotation.QuotationMotorbikeDto;
import com.example.assignment.data.repository.QuotationRepository;
import com.example.assignment.data.session.SessionManager;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QuotationDetailActivity extends AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private QuotationRepository repository;

    private TextView quoteCode;
    private TextView quoteStatus;
    private TextView quoteType;
    private TextView priceInfo;
    private TextView dateInfo;
    private TextView customerInfo;
    private TextView vehicleInfo;
    private ProgressBar progressBar;
    private Button btnEdit;
    private Button btnDelete;

    private long quotationId;
    private boolean firstLoad = true;

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));
    private final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quotation_detail);

        quotationId = getIntent().getLongExtra(QuotationListActivity.EXTRA_QUOTATION_ID, -1);
        if (quotationId == -1) {
            Toast.makeText(this, "Không tìm thấy báo giá", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        SessionManager sessionManager = new SessionManager(getApplicationContext());
        ApiServiceFactory factory = new ApiServiceFactory(sessionManager);
        repository = new QuotationRepository(factory);

        bindViews();
        setupListeners();

        loadDetail();
    }

    private void bindViews() {
        quoteCode = findViewById(R.id.quoteCode);
        quoteStatus = findViewById(R.id.quoteStatus);
        quoteType = findViewById(R.id.quoteType);
        priceInfo = findViewById(R.id.priceInfo);
        dateInfo = findViewById(R.id.dateInfo);
        customerInfo = findViewById(R.id.customerInfo);
        vehicleInfo = findViewById(R.id.vehicleInfo);
        progressBar = findViewById(R.id.progressBar);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
    }

    private void setupListeners() {
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, QuotationFormActivity.class);
            intent.putExtra(QuotationFormActivity.EXTRA_MODE, QuotationFormActivity.MODE_EDIT);
            intent.putExtra(QuotationListActivity.EXTRA_QUOTATION_ID, quotationId);
            startActivity(intent);
        });

        btnDelete.setOnClickListener(v -> new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this quotation?")
                .setPositiveButton("Delete", (dialog, which) -> deleteQuotation())
                .setNegativeButton("Cancel", null)
                .show());
    }

    private void loadDetail() {
        setLoading(true);
        executor.execute(() -> {
            try {
                QuotationDetailDto detail = repository.getQuotationDetail(quotationId);
                runOnUiThread(() -> {
                    setLoading(false);
                    bindDetail(detail);
                    firstLoad = false;
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void bindDetail(QuotationDetailDto detail) {
        if (detail == null) {
            Toast.makeText(this, "Không có dữ liệu", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        quoteCode.setText(detail.getQuoteCode() != null ? detail.getQuoteCode() : "Quotation");
        quoteStatus.setText(String.format(Locale.getDefault(), "Status: %s", detail.getStatus()));
        quoteType.setText(String.format(Locale.getDefault(), "Type: %s", detail.getType()));
        priceInfo.setText(String.format(Locale.forLanguageTag("vi-VN"), "Giá cuối: %s (gốc %s, khuyến mãi %s)",
                currencyFormat.format(detail.getFinalPrice()),
                currencyFormat.format(detail.getBasePrice()),
                currencyFormat.format(detail.getPromotionPrice())));
        dateInfo.setText(String.format(Locale.getDefault(), "Created: %s  |  Valid until: %s",
                formatDate(detail.getCreateDate()),
                formatDate(detail.getValidUntil())));

        QuotationCustomerDto customer = detail.getCustomer();
        if (customer != null) {
            customerInfo.setText(String.format(Locale.getDefault(), "%s\nPhone: %s\nEmail: %s",
                    valueOrDash(customer.getName()),
                    valueOrDash(customer.getPhone()),
                    valueOrDash(customer.getEmail())));
        } else {
            customerInfo.setText("-");
        }

        QuotationMotorbikeDto motorbike = detail.getMotorbike();
        if (motorbike != null) {
            StringBuilder builder = new StringBuilder();
            builder.append(valueOrDash(motorbike.getName()));
            if (motorbike.getModel() != null) {
                builder.append("\nModel: ").append(motorbike.getModel());
            }
            if (motorbike.getVersion() != null) {
                builder.append("\nVersion: ").append(motorbike.getVersion());
            }
            if (detail.getColor() != null) {
                builder.append("\nColor: ").append(detail.getColor().getColorType());
            }
            vehicleInfo.setText(builder.toString());
        } else {
            vehicleInfo.setText("-");
        }
    }

    private void deleteQuotation() {
        setLoading(true);
        executor.execute(() -> {
            try {
                repository.deleteQuotation(quotationId);
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, "Đã xóa báo giá", Toast.LENGTH_LONG).show();
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

    private String valueOrDash(String value) {
        return value != null && !value.isEmpty() ? value : "-";
    }

    private String formatDate(String value) {
        if (value == null) {
            return "-";
        }
        try {
            java.util.Date date = inputFormat.parse(value);
            if (date != null) {
                return displayFormat.format(date);
            }
        } catch (ParseException ignored) {
        }
        return value;
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnEdit.setEnabled(!loading);
        btnDelete.setEnabled(!loading);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!firstLoad) {
            loadDetail();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}


