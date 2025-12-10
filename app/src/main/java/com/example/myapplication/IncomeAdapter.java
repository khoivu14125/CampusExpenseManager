package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter hiển thị danh sách các khoản thu nhập.
 */
public class IncomeAdapter extends RecyclerView.Adapter<IncomeAdapter.IncomeViewHolder> {

    private List<Income> incomeList;
    private final SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private OnItemClickListener listener;

    // Interface xử lý sự kiện click vào item
    public interface OnItemClickListener {
        void onItemClick(Income income);
    }

    public IncomeAdapter(List<Income> incomeList, OnItemClickListener listener) {
        this.incomeList = incomeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public IncomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_income, parent, false);
        return new IncomeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull IncomeViewHolder holder, int position) {
        Income income = incomeList.get(position);

        // Định dạng ngày hiển thị
        try {
            Date date = dbDateFormat.parse(income.getDate());
            holder.dateTextView.setText(displayDateFormat.format(date));
        } catch (ParseException e) {
            holder.dateTextView.setText(income.getDate()); // Fallback nếu lỗi
        }

        // Định dạng số tiền
        holder.amountTextView.setText(currencyFormat.format(income.getAmount()));

        // Hiển thị mô tả
        if (income.getDescription() != null && !income.getDescription().isEmpty()) {
            holder.descriptionTextView.setText(income.getDescription());
            holder.descriptionTextView.setVisibility(View.VISIBLE);
        } else {
            holder.descriptionTextView.setText("Khoản thu nhập"); // Text mặc định
            holder.descriptionTextView.setVisibility(View.VISIBLE);
        }

        // Gán sự kiện click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(income);
            }
        });
    }

    @Override
    public int getItemCount() {
        return incomeList.size();
    }

    public void updateData(List<Income> newIncomeList) {
        this.incomeList = newIncomeList;
        notifyDataSetChanged();
    }

    public static class IncomeViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTextView, amountTextView, descriptionTextView;

        public IncomeViewHolder(View view) {
            super(view);
            dateTextView = view.findViewById(R.id.dateTextView);
            amountTextView = view.findViewById(R.id.amountTextView);
            descriptionTextView = view.findViewById(R.id.descriptionTextView);
        }
    }
}
