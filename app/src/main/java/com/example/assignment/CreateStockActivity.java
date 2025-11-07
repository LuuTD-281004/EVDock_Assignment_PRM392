package com.example.assignment;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.assignment.data.remote.ApiServiceFactory;
import com.example.assignment.data.remote.dto.stock.AgencyStockDetailDto;
import com.example.assignment.data.remote.dto.stock.motorbike.MotorbikeColorDto;
import com.example.assignment.data.remote.dto.stock.motorbike.MotorbikeDto;
import com.example.assignment.data.repository.StockRepository;
import com.example.assignment.data.session.SessionManager;
import com.example.assignment.data.session.UserSession;
import com.example.assignment.util.SimpleItemSelectedListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateStockActivity extends AppCompatActivity {

    private Spinner spinnerMotorbike;
    private Spinner spinnerColor;
    private EditText inputQuantity;
    private EditText inputPrice;
    private ProgressBar progressBar;
    private Button btnSave;

    private SessionManager sessionManager;
    private StockRepository stockRepository;
    private UserSession userSession;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<MotorbikeDto> motorbikes = new ArrayList<>();
    private List<MotorbikeColorDto> currentColors = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_stock);

        sessionManager = new SessionManager(getApplicationContext());
        ApiServiceFactory factory = new ApiServiceFactory(sessionManager);
        stockRepository = new StockRepository(factory);
        userSession = sessionManager.getUserSession();

        if (userSession == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        spinnerMotorbike = findViewById(R.id.spinnerMotorbike);
        spinnerColor = findViewById(R.id.spinnerColor);
        inputQuantity = findViewById(R.id.inputQuantity);
        inputPrice = findViewById(R.id.inputPrice);
        progressBar = findViewById(R.id.progressBar);
        btnSave = findViewById(R.id.btnSave);

        spinnerMotorbike.setOnItemSelectedListener(new SimpleItemSelectedListener(position -> {
            if (position >= 0 && position < motorbikes.size()) {
                loadColorsForMotorbike(motorbikes.get(position).getId());
            }
        }));

        btnSave.setOnClickListener(v -> saveStock());

        loadMotorbikes();
    }

    private void loadMotorbikes() {
        setLoading(true);
        executor.execute(() -> {
            try {
                List<MotorbikeDto> data = stockRepository.getMotorbikes(1000);
                runOnUiThread(() -> {
                    motorbikes.clear();
                    motorbikes.addAll(data);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_spinner_item,
                            buildMotorbikeTitles(data));
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerMotorbike.setAdapter(adapter);
                    if (!data.isEmpty()) {
                        loadColorsForMotorbike(data.get(0).getId());
                    } else {
                        setLoading(false);
                        Toast.makeText(this, "Không có xe khả dụng", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private List<String> buildMotorbikeTitles(List<MotorbikeDto> data) {
        List<String> titles = new ArrayList<>();
        for (MotorbikeDto motorbike : data) {
            titles.add(motorbike.getName() != null ? motorbike.getName() : "Xe không tên");
        }
        return titles;
    }

    private void loadColorsForMotorbike(long motorbikeId) {
        setLoading(true);
        executor.execute(() -> {
            try {
                MotorbikeDto detail = stockRepository.getMotorbikeDetail(motorbikeId);
                List<MotorbikeColorDto> colors = detail != null ? detail.getColors() : new ArrayList<>();
                runOnUiThread(() -> {
                    setLoading(false);
                    currentColors = colors != null ? colors : new ArrayList<>();
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_spinner_item,
                            buildColorTitles(currentColors));
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerColor.setAdapter(adapter);
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, "Không thể tải màu sắc", Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private List<String> buildColorTitles(List<MotorbikeColorDto> colors) {
        List<String> titles = new ArrayList<>();
        if (colors == null || colors.isEmpty()) {
            titles.add("Không có màu khả dụng");
        } else {
            for (MotorbikeColorDto color : colors) {
                titles.add(color.getColorType());
            }
        }
        return titles;
    }

    private void saveStock() {
        if (motorbikes.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn xe", Toast.LENGTH_LONG).show();
            return;
        }

        int motorbikePosition = spinnerMotorbike.getSelectedItemPosition();
        if (motorbikePosition < 0 || motorbikePosition >= motorbikes.size()) {
            Toast.makeText(this, "Vui lòng chọn xe", Toast.LENGTH_LONG).show();
            return;
        }

        if (currentColors == null || currentColors.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn màu", Toast.LENGTH_LONG).show();
            return;
        }

        int colorPosition = spinnerColor.getSelectedItemPosition();
        if (colorPosition < 0 || colorPosition >= currentColors.size()) {
            Toast.makeText(this, "Vui lòng chọn màu", Toast.LENGTH_LONG).show();
            return;
        }

        String quantityStr = inputQuantity.getText().toString().trim();
        String priceStr = inputPrice.getText().toString().trim();

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

        if (quantity <= 0 || price <= 0) {
            Toast.makeText(this, "Số lượng và giá phải lớn hơn 0", Toast.LENGTH_LONG).show();
            return;
        }

        setLoading(true);
        long motorbikeId = motorbikes.get(motorbikePosition).getId();
        long colorId = currentColors.get(colorPosition).getId();

        executor.execute(() -> {
            try {
                AgencyStockDetailDto detail = stockRepository.createStock(
                        userSession.getAgencyId(),
                        motorbikeId,
                        colorId,
                        quantity,
                        price
                );
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, "Tạo tồn kho thành công", Toast.LENGTH_LONG).show();
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


