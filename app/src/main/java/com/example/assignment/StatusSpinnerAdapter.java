package com.example.assignment;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class StatusSpinnerAdapter extends ArrayAdapter<String> {

    public StatusSpinnerAdapter(@NonNull Context context, @NonNull List<String> objects) {
        super(context, android.R.layout.simple_spinner_item, objects);
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView text = view.findViewById(android.R.id.text1);
        text.setTextColor(getContext().getResources().getColor(android.R.color.black));
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        TextView text = view.findViewById(android.R.id.text1);
        text.setPadding(24, 16, 24, 16);
        return view;
    }
}


