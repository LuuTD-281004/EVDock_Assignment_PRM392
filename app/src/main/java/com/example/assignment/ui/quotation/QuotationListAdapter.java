package com.example.assignment.ui.quotation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assignment.R;
import com.example.assignment.data.remote.dto.quotation.QuotationSummaryDto;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class QuotationListAdapter extends RecyclerView.Adapter<QuotationListAdapter.QuotationViewHolder> {

    public interface OnQuotationActionListener {
        void onView(QuotationSummaryDto quotation);

        void onEdit(QuotationSummaryDto quotation);

        void onDelete(QuotationSummaryDto quotation);
    }

    private final List<QuotationSummaryDto> items = new ArrayList<>();
    private final OnQuotationActionListener listener;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));
    private final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public QuotationListAdapter(OnQuotationActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<QuotationSummaryDto> quotations) {
        items.clear();
        if (quotations != null) {
            items.addAll(quotations);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public QuotationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quotation_card, parent, false);
        return new QuotationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuotationViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class QuotationViewHolder extends RecyclerView.ViewHolder {

        private final TextView quoteCode;
        private final TextView status;
        private final TextView type;
        private final TextView price;
        private final TextView createdAt;
        private final Button btnView;
        private final Button btnEdit;
        private final Button btnDelete;

        QuotationViewHolder(@NonNull View itemView) {
            super(itemView);
            quoteCode = itemView.findViewById(R.id.quoteCode);
            status = itemView.findViewById(R.id.quoteStatus);
            type = itemView.findViewById(R.id.quoteType);
            price = itemView.findViewById(R.id.quotePrice);
            createdAt = itemView.findViewById(R.id.quoteDate);
            btnView = itemView.findViewById(R.id.btnView);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(QuotationSummaryDto quotation) {
            quoteCode.setText(quotation.getQuoteCode() != null ? quotation.getQuoteCode() : "Quotation");
            status.setText(quotation.getStatus());
            type.setText(quotation.getType());
            price.setText(currencyFormat.format(quotation.getFinalPrice()));
            createdAt.setText(formatDate(quotation.getCreateDate()));

            btnView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onView(quotation);
                }
            });

            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEdit(quotation);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(quotation);
                }
            });
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
    }
}


