package com.example.assignment.ui.catalog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.assignment.R;
import com.example.assignment.data.remote.dto.order.manager.ManagerOrderSummaryDto;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VehicleCardAdapter extends RecyclerView.Adapter<VehicleCardAdapter.VehicleViewHolder> {

    public interface OnVehicleClickListener {
        void onVehicleClick(VehicleItem item);
    }

    private final List<VehicleItem> items = new ArrayList<>();
    private final OnVehicleClickListener listener;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));

    public VehicleCardAdapter(OnVehicleClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<VehicleItem> vehicles) {
        items.clear();
        if (vehicles != null) {
            items.addAll(vehicles);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vehicle_card, parent, false);
        return new VehicleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class VehicleViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageView;
        private final TextView titleView;
        private final TextView subtitleView;
        private final TextView priceView;
        private final TextView stockView;
        private final TextView orderView;

        VehicleViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.vehicleImage);
            titleView = itemView.findViewById(R.id.vehicleTitle);
            subtitleView = itemView.findViewById(R.id.vehicleSubtitle);
            priceView = itemView.findViewById(R.id.vehiclePrice);
            stockView = itemView.findViewById(R.id.vehicleStock);
            orderView = itemView.findViewById(R.id.vehicleOrder);
        }

        void bind(VehicleItem item) {
            titleView.setText(item.getName());
            String model = item.getModel() != null ? item.getModel() : "";
            String version = item.getVersion() != null ? item.getVersion() : "";
            subtitleView.setText(String.format(Locale.getDefault(), "%s %s", model, version));
            priceView.setText(currencyFormat.format(item.getPrice()));
            stockView.setText(String.format(Locale.getDefault(), "Stock: %d", item.getQuantity()));

            ManagerOrderSummaryDto pending = item.getPendingOrder();
            if (pending != null) {
                orderView.setVisibility(View.VISIBLE);
                orderView.setText(String.format(Locale.getDefault(), "Pending order: #%d", pending.getId()));
            } else {
                orderView.setVisibility(View.GONE);
            }

            Glide.with(itemView.getContext())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.bg_placeholder)
                    .into(imageView);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVehicleClick(item);
                }
            });
        }
    }
}


