package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class RecurringExpenseAdapter extends RecyclerView.Adapter<RecurringExpenseAdapter.RecurringExpenseViewHolder> {

    private List<RecurringExpense> recurringExpenseList;
    private OnDeleteClickListener onDeleteClickListener;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public interface OnDeleteClickListener {
        void onDeleteClick(RecurringExpense expense);
    }

    public RecurringExpenseAdapter(List<RecurringExpense> recurringExpenseList, OnDeleteClickListener onDeleteClickListener) {
        this.recurringExpenseList = recurringExpenseList;
        this.onDeleteClickListener = onDeleteClickListener;
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
        
        // Set start and end dates individually
        holder.startDateTextView.setText(expense.getStartDate());
        holder.endDateTextView.setText(expense.getEndDate());
        
        if (expense.getDescription() != null && !expense.getDescription().isEmpty()) {
            holder.descriptionTextView.setText(expense.getDescription());
            holder.descriptionTextView.setVisibility(View.VISIBLE);
        } else {
            holder.descriptionTextView.setVisibility(View.GONE);
        }

        holder.deleteButton.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(expense);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recurringExpenseList.size();
    }

    public static class RecurringExpenseViewHolder extends RecyclerView.ViewHolder {
        public TextView categoryTextView, amountTextView, startDateTextView, endDateTextView, descriptionTextView;
        public ImageButton deleteButton;

        public RecurringExpenseViewHolder(View view) {
            super(view);
            categoryTextView = view.findViewById(R.id.categoryTextView);
            amountTextView = view.findViewById(R.id.amountTextView);
            startDateTextView = view.findViewById(R.id.startDateTextView);
            endDateTextView = view.findViewById(R.id.endDateTextView);
            descriptionTextView = view.findViewById(R.id.descriptionTextView);
            deleteButton = view.findViewById(R.id.deleteButton);
        }
    }
}
