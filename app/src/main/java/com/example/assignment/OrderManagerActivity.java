package com.example.assignment;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class OrderManagerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_manager);

        String role = getIntent().getStringExtra("role");
        TextView tv = findViewById(R.id.tvOrderManager);
        String message = getString(R.string.order_greeting, role);
        tv.setText(message);
    }
}

