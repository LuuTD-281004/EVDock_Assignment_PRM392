package com.example.assignment;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;

public class CatalogActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Nhận vai trò (role)
        String role = getIntent().getStringExtra("role");
        TextView tv = findViewById(R.id.tvCatalog);
        String message = getString(R.string.catalog_greeting, role);
        tv.setText(message);

        // Nút đăng xuất
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            // Tạo Intent quay về LoginActivity
            Intent intent = new Intent(CatalogActivity.this, LoginActivity.class);
            // Xóa toàn bộ activity trước đó (để không back lại được)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
