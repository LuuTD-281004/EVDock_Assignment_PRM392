package com.example.assignment.ui.order;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assignment.R;
import com.example.assignment.data.remote.dto.order.manager.ManagerOrderSummaryDto;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ManagerOrderAdapter extends RecyclerView.Adapter<ManagerOrderAdapter.ManagerOrderViewHolder> {

    public interface OnManagerOrderListener {
        void onOrderSelected(ManagerOrderSummaryDto order);

        void onAccept(ManagerOrderSummaryDto order);

        void onCancel(ManagerOrderSummaryDto order);
    }

    private final List<ManagerOrderSummaryDto> orders = new ArrayList<>();
    private final OnManagerOrderListener listener;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));
    private final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public ManagerOrderAdapter(OnManagerOrderListener listener) {
        this.listener = listener;
    }

    public void submitList(List<ManagerOrderSummaryDto> newOrders) {
        orders.clear();
        if (newOrders != null) {
            orders.addAll(newOrders);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ManagerOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_restock, parent, false);
        return new ManagerOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ManagerOrderViewHolder holder, int position) {
        holder.bind(orders.get(position));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    private String formatDate(String value) {
        if (value == null) {
            return "-";
        }
        try {
            Date date = inputFormat.parse(value);
            if (date != null) {
                return displayFormat.format(date);
            }
        } catch (ParseException ignored) {
        }
        return value;
    }

    class ManagerOrderViewHolder extends RecyclerView.ViewHolder {

        private final TextView orderId;
        private final TextView orderDate;
        private final TextView orderStatus;
        private final TextView agencyName;
        private final TextView orderQuantity;
        private final TextView orderTotal;
        private final Button btnNextStatus;
        private final Button btnCancel;

        ManagerOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.orderId);
            orderDate = itemView.findViewById(R.id.orderDate);
            orderStatus = itemView.findViewById(R.id.orderStatus);
            agencyName = itemView.findViewById(R.id.agencyName);
            orderQuantity = itemView.findViewById(R.id.orderQuantity);
            orderTotal = itemView.findViewById(R.id.orderTotal);
            btnNextStatus = itemView.findViewById(R.id.btnNextStatus);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }

        void bind(ManagerOrderSummaryDto order) {
            orderId.setText("Order #" + order.getId());
            orderDate.setText(formatDate(order.getOrderAt()));
            orderStatus.setText(order.getStatus());
            if (order.getAgency() != null) {
                agencyName.setText(order.getAgency().getName());
            } else if (order.getAgencyId() != null) {
                agencyName.setText("Agency #" + order.getAgencyId());
            } else {
                agencyName.setText("Agency: -");
            }
            orderQuantity.setText("Quantity: " + order.getQuantity());
            orderTotal.setText(currencyFormat.format(order.getSubtotal()));

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderSelected(order);
                }
            });

            String status = order.getStatus() != null ? order.getStatus().toUpperCase(Locale.getDefault()) : "";
            boolean canAccept = status.equals("DRAFT") || status.equals("PENDING");
            boolean canCancel = !status.equals("CANCELED") && !status.equals("DELIVERED");

            btnNextStatus.setVisibility(canAccept ? View.VISIBLE : View.GONE);
            btnCancel.setVisibility(canCancel ? View.VISIBLE : View.GONE);

            btnNextStatus.setText("Accept");
            btnNextStatus.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAccept(order);
                }
            });

            btnCancel.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCancel(order);
                }
            });
        }
    }
}


