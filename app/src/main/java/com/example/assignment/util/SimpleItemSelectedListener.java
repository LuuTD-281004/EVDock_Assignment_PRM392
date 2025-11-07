package com.example.assignment.util;

import android.view.View;
import android.widget.AdapterView;

public class SimpleItemSelectedListener implements AdapterView.OnItemSelectedListener {

    public interface OnItemSelectedCallback {
        void onItemSelected(int position);
    }

    private final OnItemSelectedCallback callback;

    public SimpleItemSelectedListener(OnItemSelectedCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (callback != null) {
            callback.onItemSelected(position);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // no-op
    }
}


