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

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> expenseList;
    private OnItemClickListener listener;
    private final SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public interface OnItemClickListener {
        void onItemClick(Expense expense);
    }

    public ExpenseAdapter(List<Expense> expenseList, OnItemClickListener listener) {
        this.expenseList = expenseList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);
        holder.categoryTextView.setText(expense.getCategory());

        // Format date for display
        try {
            Date date = dbDateFormat.parse(expense.getDate());
            holder.dateTextView.setText(displayDateFormat.format(date));
        } catch (ParseException e) {
            holder.dateTextView.setText(expense.getDate()); // Fallback to raw date
        }

        // Format currency
        holder.amountTextView.setText(currencyFormat.format(expense.getAmount()));

        // Handle description for ALL categories
        if (expense.getDescription() != null && !expense.getDescription().isEmpty()) {
            holder.descriptionTextView.setText("Ghi chú: " + expense.getDescription());
            holder.descriptionTextView.setVisibility(View.VISIBLE);
        } else {
            holder.descriptionTextView.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(expense));
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public void updateData(List<Expense> newExpenseList) {
        this.expenseList = newExpenseList;
        notifyDataSetChanged();
    }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        public TextView categoryTextView, dateTextView, amountTextView, descriptionTextView;

        public ExpenseViewHolder(View view) {
            super(view);
            categoryTextView = view.findViewById(R.id.categoryTextView);
            dateTextView = view.findViewById(R.id.dateTextView);
            amountTextView = view.findViewById(R.id.amountTextView);
            descriptionTextView = view.findViewById(R.id.descriptionTextView);
        }
    }
}
