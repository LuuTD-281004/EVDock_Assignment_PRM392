package com.example.assignment;

import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.assignment.data.remote.ApiServiceFactory;
import com.example.assignment.data.remote.dto.quotation.CreateQuotationRequest;
import com.example.assignment.data.remote.dto.quotation.QuotationDetailDto;
import com.example.assignment.data.remote.dto.quotation.UpdateQuotationRequest;
import com.example.assignment.data.remote.dto.stock.motorbike.MotorbikeColorDto;
import com.example.assignment.data.remote.dto.stock.motorbike.MotorbikeDto;
import com.example.assignment.data.repository.QuotationRepository;
import com.example.assignment.data.repository.StockRepository;
import com.example.assignment.data.session.SessionManager;
import com.example.assignment.data.session.UserSession;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.TimeZone;

import java.time.Instant;

public class QuotationFormActivity extends AppCompatActivity {

    public static final String EXTRA_MODE = "extra_mode";
    public static final int MODE_CREATE = 0;
    public static final int MODE_EDIT = 1;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private SessionManager sessionManager;
    private QuotationRepository quotationRepository;
    private StockRepository stockRepository;
    private UserSession userSession;

    private int mode = MODE_CREATE;
    private long quotationId = -1;

    private TextView formTitle;
    private Spinner spinnerType;
    private EditText inputBasePrice;
    private EditText inputPromotionPrice;
    private EditText inputFinalPrice;
    private TextView inputValidUntil;
    private EditText inputCustomerId;
    private Spinner spinnerMotorbike;
    private Spinner spinnerColor;
    private ProgressBar progressBar;
    private Button btnSave;

    private final List<MotorbikeDto> motorbikes = new ArrayList<>();
    private final List<MotorbikeColorDto> colors = new ArrayList<>();
    private final NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
    private final java.text.SimpleDateFormat isoFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
    private final java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
    private final java.text.SimpleDateFormat displayFormat = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private Calendar selectedDate = Calendar.getInstance();
    private Long pendingMotorbikeId;
    private Long pendingColorId;
    private String pendingType;
    private Double pendingBasePrice;
    private Double pendingPromotionPrice;
    private Double pendingFinalPrice;
    private Long pendingCustomerId;
    private String pendingValidUntil;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quotation_form);

        mode = getIntent().getIntExtra(EXTRA_MODE, MODE_CREATE);
        quotationId = getIntent().getLongExtra(QuotationListActivity.EXTRA_QUOTATION_ID, -1);

        sessionManager = new SessionManager(getApplicationContext());
        ApiServiceFactory factory = new ApiServiceFactory(sessionManager);
        quotationRepository = new QuotationRepository(factory);
        stockRepository = new StockRepository(factory);
        userSession = sessionManager.getUserSession();

        bindViews();
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        setupTypeSpinner();
        setupListeners();
        loadMotorbikes();

        if (mode == MODE_EDIT && quotationId != -1) {
            loadQuotationDetail();
        } else {
            updateTitle();
        }
    }

    private void bindViews() {
        formTitle = findViewById(R.id.formTitle);
        spinnerType = findViewById(R.id.spinnerType);
        inputBasePrice = findViewById(R.id.inputBasePrice);
        inputPromotionPrice = findViewById(R.id.inputPromotionPrice);
        inputFinalPrice = findViewById(R.id.inputFinalPrice);
        inputValidUntil = findViewById(R.id.inputValidUntil);
        inputCustomerId = findViewById(R.id.inputCustomerId);
        spinnerMotorbike = findViewById(R.id.spinnerMotorbike);
        spinnerColor = findViewById(R.id.spinnerColor);
        progressBar = findViewById(R.id.progressBar);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupTypeSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"AT_STORE", "ORDER", "PRE_ORDER"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveQuotation());

        inputValidUntil.setOnClickListener(v -> showDatePicker());

        spinnerMotorbike.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                loadColors();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                autoCalculateFinalPrice();
            }
        };
        inputBasePrice.addTextChangedListener(watcher);
        inputPromotionPrice.addTextChangedListener(watcher);
    }

    private void updateTitle() {
        if (mode == MODE_EDIT) {
            formTitle.setText("Edit Quotation");
        } else {
            formTitle.setText("Create Quotation");
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDate.set(Calendar.YEAR, year);
            selectedDate.set(Calendar.MONTH, month);
            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            inputValidUntil.setText(String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadMotorbikes() {
        setLoading(true);
        executor.execute(() -> {
            try {
                List<MotorbikeDto> data = stockRepository.getMotorbikes(1000);
                runOnUiThread(() -> {
                    motorbikes.clear();
                    motorbikes.addAll(data);
                    bindMotorbikeSpinner();
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
        applyPendingSelections();
    }

    private void loadColors() {
        setLoading(true);
        executor.execute(() -> {
            try {
                int index = spinnerMotorbike.getSelectedItemPosition();
                if (index < 0 || index >= motorbikes.size()) {
                    runOnUiThread(() -> {
                        colors.clear();
                        bindColorSpinner();
                        setLoading(false);
                    });
                    return;
                }

                MotorbikeDto motorbike = motorbikes.get(index);
                MotorbikeDto detail = stockRepository.getMotorbikeDetail(motorbike.getId());
                List<MotorbikeColorDto> colorList = detail != null ? detail.getColors() : new ArrayList<>();
                runOnUiThread(() -> {
                    colors.clear();
                    if (colorList != null) {
                        colors.addAll(colorList);
                    }
                    bindColorSpinner();
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

    private void bindColorSpinner() {
        List<String> labels = new ArrayList<>();
        for (MotorbikeColorDto color : colors) {
            labels.add(color.getColorType());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerColor.setAdapter(adapter);
        applyPendingColor();
    }

    private void loadQuotationDetail() {
        setLoading(true);
        executor.execute(() -> {
            try {
                QuotationDetailDto detail = quotationRepository.getQuotationDetail(quotationId);
                runOnUiThread(() -> {
                    setLoading(false);
                    updateTitle();
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

    private void bindDetail(QuotationDetailDto detail) {
        if (detail == null) {
            return;
        }

        pendingType = detail.getType();
        pendingBasePrice = detail.getBasePrice();
        pendingPromotionPrice = detail.getPromotionPrice();
        pendingFinalPrice = detail.getFinalPrice();
        pendingCustomerId = detail.getCustomerId();
        pendingValidUntil = detail.getValidUntil();
        pendingMotorbikeId = detail.getMotorbikeId();
        pendingColorId = detail.getColorId();

        applyPendingFormValues();
        applyPendingSelections();
        applyPendingColor();
    }

    private void selectSpinnerValue(Spinner spinner, String value) {
        if (value == null) {
            return;
        }
        for (int i = 0; i < spinner.getCount(); i++) {
            Object item = spinner.getItemAtPosition(i);
            if (value.equalsIgnoreCase(String.valueOf(item))) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void autoCalculateFinalPrice() {
        try {
            double base = parseNumber(inputBasePrice.getText().toString());
            double promotion = parseNumber(inputPromotionPrice.getText().toString());
            double finalValue = base - promotion;
            if (finalValue < 0) {
                finalValue = 0;
            }
            inputFinalPrice.setText(String.valueOf((long) finalValue));
        } catch (NumberFormatException ignored) {
        }
    }

    private double parseNumber(String value) throws NumberFormatException {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            Number number = numberFormat.parse(value.replace(",", ""));
            return number != null ? number.doubleValue() : 0;
        } catch (Exception e) {
            return Double.parseDouble(value);
        }
    }

    private void saveQuotation() {
        if (userSession == null || userSession.getAgencyId() == null || userSession.getUserId() == null) {
            Toast.makeText(this, "Phiên đăng nhập không hợp lệ", Toast.LENGTH_LONG).show();
            return;
        }

        String customerIdText = inputCustomerId.getText().toString().trim();
        if (customerIdText.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập Customer ID", Toast.LENGTH_LONG).show();
            return;
        }

        int motorbikePosition = spinnerMotorbike.getSelectedItemPosition();
        int colorPosition = spinnerColor.getSelectedItemPosition();
        if (motorbikePosition < 0 || motorbikePosition >= motorbikes.size()) {
            Toast.makeText(this, "Vui lòng chọn xe", Toast.LENGTH_LONG).show();
            return;
        }
        if (colorPosition < 0 || colorPosition >= colors.size()) {
            Toast.makeText(this, "Vui lòng chọn màu", Toast.LENGTH_LONG).show();
            return;
        }

        double basePrice = parseNumber(inputBasePrice.getText().toString());
        double promotionPrice = parseNumber(inputPromotionPrice.getText().toString());
        double finalPrice = parseNumber(inputFinalPrice.getText().toString());

        if (basePrice <= 0 || finalPrice <= 0) {
            Toast.makeText(this, "Giá không hợp lệ", Toast.LENGTH_LONG).show();
            return;
        }

        String type = (String) spinnerType.getSelectedItem();
        long customerId = Long.parseLong(customerIdText);
        long motorbikeId = motorbikes.get(motorbikePosition).getId();
        long colorId = colors.get(colorPosition).getColorId();
        long dealerStaffId = Long.parseLong(userSession.getUserId());
        long agencyId = userSession.getAgencyId();

        String validUntil = inputValidUntil.getText().toString();
        if (validUntil.isEmpty()) {
            selectedDate = Calendar.getInstance();
        }
        selectedDate.set(Calendar.HOUR_OF_DAY, 23);
        selectedDate.set(Calendar.MINUTE, 59);
        selectedDate.set(Calendar.SECOND, 59);

        String validUntilIso = isoFormat.format(selectedDate.getTime());

        setLoading(true);
        executor.execute(() -> {
            try {
                if (mode == MODE_EDIT && quotationId != -1) {
                    UpdateQuotationRequest request = new UpdateQuotationRequest(
                            type,
                            basePrice,
                            promotionPrice,
                            finalPrice,
                            validUntilIso,
                            customerId,
                            motorbikeId,
                            colorId
                    );
                    quotationRepository.updateQuotation(quotationId, request);
                } else {
                    CreateQuotationRequest request = new CreateQuotationRequest(
                            type,
                            basePrice,
                            promotionPrice,
                            finalPrice,
                            validUntilIso,
                            customerId,
                            motorbikeId,
                            colorId,
                            dealerStaffId,
                            agencyId
                    );
                    quotationRepository.createQuotation(request);
                }

                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, "Đã lưu báo giá", Toast.LENGTH_LONG).show();
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

    private void applyPendingFormValues() {
        if (pendingType != null) {
            selectSpinnerValue(spinnerType, pendingType);
        }
        if (pendingBasePrice != null) {
            inputBasePrice.setText(String.valueOf(pendingBasePrice.longValue()));
        }
        if (pendingPromotionPrice != null) {
            inputPromotionPrice.setText(String.valueOf(pendingPromotionPrice.longValue()));
        }
        if (pendingFinalPrice != null) {
            inputFinalPrice.setText(String.valueOf(pendingFinalPrice.longValue()));
        }
        if (pendingCustomerId != null) {
            inputCustomerId.setText(String.valueOf(pendingCustomerId));
        }
        if (pendingValidUntil != null) {
            setDateFromString(pendingValidUntil);
        }
    }

    private void applyPendingSelections() {
        if (pendingMotorbikeId == null) {
            return;
        }
        for (int i = 0; i < motorbikes.size(); i++) {
            if (motorbikes.get(i).getId() == pendingMotorbikeId) {
                spinnerMotorbike.setSelection(i);
                break;
            }
        }
        pendingMotorbikeId = null;
    }

    private void applyPendingColor() {
        if (pendingColorId == null) {
            return;
        }
        for (int i = 0; i < colors.size(); i++) {
            if (colors.get(i).getColorId() == pendingColorId) {
                spinnerColor.setSelection(i);
                break;
            }
        }
        pendingColorId = null;
    }

    private void setDateFromString(String value) {
        if (value == null) {
            return;
        }
        try {
            Date date;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                date = Date.from(Instant.parse(value));
            } else {
                date = inputFormat.parse(value);
            }
            if (date != null) {
                selectedDate.setTime(date);
                inputValidUntil.setText(displayFormat.format(date));
            }
        } catch (Exception e) {
            try {
                Date fallback = inputFormat.parse(value);
                if (fallback != null) {
                    selectedDate.setTime(fallback);
                    inputValidUntil.setText(displayFormat.format(fallback));
                }
            } catch (ParseException ignored) {
                inputValidUntil.setText(value);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}


