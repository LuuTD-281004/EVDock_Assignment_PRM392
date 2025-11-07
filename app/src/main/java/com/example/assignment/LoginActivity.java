package com.example.assignment;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import android.content.Intent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.assignment.api.models.LoginResponse;
import com.example.assignment.api.services.AuthApi;
import com.example.assignment.utils.SharedPreferencesHelper;

import java.util.List;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    ProgressBar progressBar;
    LinearLayout quickLoginContainer;
    AuthApi authApi;
    SharedPreferencesHelper prefsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authApi = new AuthApi();
        prefsHelper = new SharedPreferencesHelper(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        quickLoginContainer = findViewById(R.id.quickLoginContainer);

        // Setup quick login buttons
        setupQuickLoginButtons();

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            performLogin(email, password);
        });
    }

    private void setupQuickLoginButtons() {
        List<SharedPreferencesHelper.SavedCredential> credentials = prefsHelper.getSavedCredentials();
        
        // Clear existing buttons (except the title)
        quickLoginContainer.removeAllViews();
        
        // Add title
        TextView title = new TextView(this);
        title.setText("Tài khoản đã lưu (Nhấn để đăng nhập)");
        title.setTextColor(getResources().getColor(android.R.color.white, null));
        title.setTextSize(14);
        title.setPadding(0, 16, 0, 8);
        quickLoginContainer.addView(title);

        // Add buttons for each saved credential
        for (SharedPreferencesHelper.SavedCredential cred : credentials) {
            Button btn = new Button(this);
            btn.setText(cred.label);
            btn.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) btn.getLayoutParams();
            params.setMargins(0, 8, 0, 0);
            btn.setLayoutParams(params);
            
            btn.setOnClickListener(v -> {
                etEmail.setText(cred.email);
                etPassword.setText(cred.password);
                performLogin(cred.email, cred.password);
            });
            
            quickLoginContainer.addView(btn);
        }
    }

    private void performLogin(String email, String password) {
        setLoading(true);
        
        authApi.login(email, password, new AuthApi.LoginCallback() {
            @Override
            public void onSuccess(LoginResponse response) {
                setLoading(false);
                
                LoginResponse.Data data = null;
                
                // Handle both response structures: nested (data) or direct
                if (response.getData() != null) {
                    data = response.getData();
                } else {
                    // If no nested data, try to extract from response directly
                    // This handles cases where API returns data at root level
                    Toast.makeText(LoginActivity.this, "Phản hồi từ server không hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (data != null) {
                    // Save tokens and user info
                    if (data.getAccessToken() != null) {
                        prefsHelper.saveToken(data.getAccessToken());
                    }
                    if (data.getRefreshToken() != null) {
                        prefsHelper.saveRefreshToken(data.getRefreshToken());
                    }
                    if (data.getUserId() != null) {
                        prefsHelper.saveUserId(data.getUserId());
                    }
                    if (data.getAgencyId() != null) {
                        prefsHelper.saveAgencyId(data.getAgencyId());
                    }
                    
                    // Map API role to app role
                    String appRole = mapApiRoleToAppRole(data.getRole());
                    if (appRole != null) {
                        prefsHelper.saveUserRole(appRole);
                    }
                    
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                    
                    // Navigate based on role
                    navigateToRoleScreen(appRole);
                } else {
                    Toast.makeText(LoginActivity.this, "Phản hồi từ server không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String mapApiRoleToAppRole(List<String> apiRoles) {
        if (apiRoles == null || apiRoles.isEmpty()) {
            return "DealerStaff"; // default
        }
        
        String apiRole = apiRoles.get(0);
        if (apiRole == null) {
            return "DealerStaff";
        }
        
        // Map API roles to app roles
        switch (apiRole) {
            case "Admin":
                return "Admin";
            case "Evm Staff":
            case "Staff":
                return "EVMStaff";
            case "Dealer Manager":
            case "DealerManager":
                return "DealerManager";
            case "Dealer Staff":
            case "DealerStaff":
                return "DealerStaff";
            default:
                return "DealerStaff";
        }
    }

    private void navigateToRoleScreen(String role) {
        Intent intent;
        if (role != null) {
            if (role.equals("DealerStaff")) {
                intent = new Intent(this, CatalogActivity.class);
            } else if (role.equals("EVMStaff")) {
                intent = new Intent(this, OrderActivity.class);
            } else if (role.equals("DealerManager")) {
                intent = new Intent(this, OrderManagerActivity.class);
            } else if (role.equals("Admin")) {
                // Admin can go to OrderActivity for now
                intent = new Intent(this, OrderActivity.class);
            } else {
                intent = new Intent(this, CatalogActivity.class);
            }
        } else {
            intent = new Intent(this, CatalogActivity.class);
        }
        
        intent.putExtra("role", role);
        intent.putExtra("user_email", etEmail.getText().toString());
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        etEmail.setEnabled(!loading);
        etPassword.setEnabled(!loading);
    }
}
