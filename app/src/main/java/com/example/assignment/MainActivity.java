package com.example.assignment;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView tvWelcome;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // tạo file layout đơn giản

        tvWelcome = findViewById(R.id.tvWelcome);
        String email = getIntent().getStringExtra("user_email");
        tvWelcome.setText("Xin chào, " + (email != null ? email : "Người dùng"));
    }
}
