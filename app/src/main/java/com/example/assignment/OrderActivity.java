package com.example.assignment;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class OrderActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        String role = getIntent().getStringExtra("role");
        TextView tv = findViewById(R.id.tvOrder);
        String message = getString(R.string.order_greeting, role);
        tv.setText(message);
    }
}

