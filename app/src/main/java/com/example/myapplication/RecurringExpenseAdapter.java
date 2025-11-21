package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class RecurringExpenseAdapter extends RecyclerView.Adapter<RecurringExpenseAdapter.RecurringExpenseViewHolder> {

    private List<RecurringExpense> recurringExpenseList;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public RecurringExpenseAdapter(List<RecurringExpense> recurringExpenseList) {
        this.recurringExpenseList = recurringExpenseList;
    }

    @NonNull
    @Override
    public RecurringExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recurring_expense, parent, false);
        return new RecurringExpenseViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecurringExpenseViewHolder holder, int position) {
        RecurringExpense expense = recurringExpenseList.get(position);
        holder.categoryTextView.setText(expense.getCategory());
        holder.amountTextView.setText(currencyFormat.format(expense.getAmount()));
        holder.dateRangeTextView.setText(String.format("From: %s To: %s", expense.getStartDate(), expense.getEndDate()));
        
        if (expense.getDescription() != null && !expense.getDescription().isEmpty()) {
            holder.descriptionTextView.setText(expense.getDescription());
            holder.descriptionTextView.setVisibility(View.VISIBLE);
        } else {
            holder.descriptionTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return recurringExpenseList.size();
    }

    public static class RecurringExpenseViewHolder extends RecyclerView.ViewHolder {
        public TextView categoryTextView, amountTextView, dateRangeTextView, descriptionTextView;

        public RecurringExpenseViewHolder(View view) {
            super(view);
            categoryTextView = view.findViewById(R.id.categoryTextView);
            amountTextView = view.findViewById(R.id.amountTextView);
            dateRangeTextView = view.findViewById(R.id.dateRangeTextView);
            descriptionTextView = view.findViewById(R.id.descriptionTextView);
        }
    }
}
