package com.example.assignment.ui.stock.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.assignment.R;
import com.example.assignment.ui.stock.model.StockListItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StockListAdapter extends RecyclerView.Adapter<StockListAdapter.StockViewHolder> {

    public interface OnStockClickListener {
        void onStockClick(StockListItem item);
    }

    private final List<StockListItem> items = new ArrayList<>();
    private final OnStockClickListener listener;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));

    public StockListAdapter(OnStockClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<StockListItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stock_card, parent, false);
        return new StockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class StockViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageView;
        private final TextView titleView;
        private final TextView subtitleView;
        private final TextView priceView;
        private final TextView quantityView;

        StockViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.stockImage);
            titleView = itemView.findViewById(R.id.motorbikeName);
            subtitleView = itemView.findViewById(R.id.motorbikeModel);
            priceView = itemView.findViewById(R.id.stockPrice);
            quantityView = itemView.findViewById(R.id.stockQuantity);
        }

        void bind(StockListItem item) {
            titleView.setText(item.getMotorbikeName());
            subtitleView.setText(item.getModel() + " - " + item.getVersion());
            priceView.setText(currencyFormat.format(item.getPrice()));
            quantityView.setText(String.valueOf(item.getQuantity()));

            Glide.with(itemView.getContext())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.bg_placeholder)
                    .into(imageView);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStockClick(item);
                }
            });
        }
    }
}


