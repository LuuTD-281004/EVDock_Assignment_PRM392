package com.example.assignment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.assignment.data.remote.ApiServiceFactory;
import com.example.assignment.data.repository.AuthRepository;
import com.example.assignment.data.session.SessionManager;
import com.example.assignment.data.session.UserRole;
import com.example.assignment.data.session.UserSession;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnEVM;
    private Button btnDealer;
    private Button btnManager;
    private ProgressBar progressBar;

    private SessionManager sessionManager;
    private AuthRepository authRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(getApplicationContext());
        ApiServiceFactory factory = new ApiServiceFactory(sessionManager);
        authRepository = new AuthRepository(factory, sessionManager);

        bindViews();
        bindListeners();

        UserSession existingSession = sessionManager.getUserSession();
        if (existingSession != null && sessionManager.getAccessToken() != null) {
            navigateToHome(existingSession);
            finish();
        }
    }

    private void bindViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnEVM = findViewById(R.id.btnEVM);
        btnDealer = findViewById(R.id.btnDealer);
        btnManager = findViewById(R.id.btnManager);
        progressBar = findViewById(R.id.progressBar);
    }

    private void bindListeners() {
        btnEVM.setOnClickListener(v -> {
            etEmail.setText("evmstaff@gmail.com");
            etPassword.setText("123456");
        });
        btnDealer.setOnClickListener(v -> {
            etEmail.setText("dealerstaff@gmail.com");
            etPassword.setText("123456");
        });
        btnManager.setOnClickListener(v -> {
            etEmail.setText("john.doe@email.com");
            etPassword.setText("123456");
        });

        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        final String email = etEmail.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        executor.execute(() -> {
            AuthRepository.Result result = authRepository.login(email, password);
            runOnUiThread(() -> {
                setLoading(false);
                if (result.isSuccess() && result.getSession() != null) {
                    Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                    navigateToHome(result.getSession());
                    finish();
                } else {
                    String error = result.getError() != null ? result.getError() : "Đăng nhập thất bại";
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        btnEVM.setEnabled(!loading);
        btnDealer.setEnabled(!loading);
        btnManager.setEnabled(!loading);
        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    private void navigateToHome(UserSession session) {
        UserRole role = session.getRole();
        Intent intent;
        switch (role) {
            case EVM_STAFF:
            case EVM_ADMIN:
                intent = new Intent(this, OrderRestockManagementActivity.class);
                break;
            case DEALER_MANAGER:
                intent = new Intent(this, OrderRestockManagerActivity.class);
                break;
            case DEALER_STAFF:
            case UNKNOWN:
            default:
                intent = new Intent(this, CatalogActivity.class);
                break;
        }
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
