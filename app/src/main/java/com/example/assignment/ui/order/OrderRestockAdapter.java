package com.example.assignment.ui.order;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assignment.R;
import com.example.assignment.data.remote.dto.order.OrderRestockSummaryDto;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderRestockAdapter extends RecyclerView.Adapter<OrderRestockAdapter.OrderViewHolder> {

    public interface OnOrderInteractionListener {
        void onOrderSelected(OrderRestockSummaryDto order);

        void onAdvanceStatus(OrderRestockSummaryDto order);

        void onCancel(OrderRestockSummaryDto order);
    }

    private final List<OrderRestockSummaryDto> items = new ArrayList<>();
    private final OnOrderInteractionListener listener;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));
    private final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public OrderRestockAdapter(OnOrderInteractionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<OrderRestockSummaryDto> orders) {
        items.clear();
        if (orders != null) {
            items.addAll(orders);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_restock, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
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

    class OrderViewHolder extends RecyclerView.ViewHolder {

        private final TextView orderId;
        private final TextView orderDate;
        private final TextView orderStatus;
        private final TextView agencyName;
        private final TextView orderQuantity;
        private final TextView orderTotal;
        private final Button btnNext;
        private final Button btnCancel;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.orderId);
            orderDate = itemView.findViewById(R.id.orderDate);
            orderStatus = itemView.findViewById(R.id.orderStatus);
            agencyName = itemView.findViewById(R.id.agencyName);
            orderQuantity = itemView.findViewById(R.id.orderQuantity);
            orderTotal = itemView.findViewById(R.id.orderTotal);
            btnNext = itemView.findViewById(R.id.btnNextStatus);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }

        void bind(OrderRestockSummaryDto order) {
            orderId.setText("Order #" + order.getId());
            orderDate.setText(formatDate(order.getOrderAt()));
            orderStatus.setText(order.getStatus());
            if (order.getAgency() != null) {
                agencyName.setText(order.getAgency().getName());
            } else if (order.getAgencyId() != null) {
                agencyName.setText("Agency #" + order.getAgencyId());
            } else {
                agencyName.setText("Unknown agency");
            }
            orderQuantity.setText("Quantity: " + order.getItemQuantity());
            orderTotal.setText(currencyFormat.format(order.getSubtotal()));

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderSelected(order);
                }
            });

            String status = order.getStatus() != null ? order.getStatus().toUpperCase(Locale.getDefault()) : "";
            String nextStatus = getNextStatus(status);

            if (nextStatus != null) {
                btnNext.setVisibility(View.VISIBLE);
                btnNext.setText("Next: " + nextStatus);
                btnNext.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onAdvanceStatus(order);
                    }
                });
            } else {
                btnNext.setVisibility(View.GONE);
            }

            if (!status.equals("CANCELED") && !status.equals("DELIVERED")) {
                btnCancel.setVisibility(View.VISIBLE);
                btnCancel.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onCancel(order);
                    }
                });
            } else {
                btnCancel.setVisibility(View.GONE);
            }
        }

        private String getNextStatus(String current) {
            switch (current) {
                case "PENDING":
                    return "APPROVED";
                case "APPROVED":
                    return "DELIVERED";
                default:
                    return null;
            }
        }
    }
}


