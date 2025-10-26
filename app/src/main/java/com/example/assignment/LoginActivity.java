package com.example.assignment;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import android.content.Intent;
import android.database.Cursor;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin, btnEVM, btnDealer, btnManager;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnEVM = findViewById(R.id.btnEVM);
        btnDealer = findViewById(R.id.btnDealer);
        btnManager = findViewById(R.id.btnManager);

        // ✅ Gắn listener đúng biến
        btnEVM.setOnClickListener(v -> etEmail.setText("evmstaff@gmail.com"));
        btnDealer.setOnClickListener(v -> etEmail.setText("dealerstaff@gmail.com"));
        btnManager.setOnClickListener(v -> etEmail.setText("john.doe@email.com"));

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            Cursor cursor = dbHelper.loginUser(email, password);
            if (cursor.moveToFirst()) {
                String role = cursor.getString(cursor.getColumnIndexOrThrow("role"));
                Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                if (role.equals("DealerStaff")) {
                    startActivity(new Intent(this, CatalogActivity.class).putExtra("role", role));
                } else if (role.equals("EVMStaff")) {
                    startActivity(new Intent(this, OrderActivity.class).putExtra("role", role));
                } else if (role.equals("DealerManager")) {
                    startActivity(new Intent(this, OrderManagerActivity.class).putExtra("role", role));
                }
            } else {
                Toast.makeText(this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
            }
            cursor.close();
        });
    }
}
