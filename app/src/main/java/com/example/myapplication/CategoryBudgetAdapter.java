package com.example.myapplication;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.text.DecimalFormat;
import java.util.List;

public class CategoryBudgetAdapter extends RecyclerView.Adapter<CategoryBudgetAdapter.CategoryBudgetViewHolder> {

    private List<CategoryBudget> budgetList;
    private OnBudgetChangeListener listener;
    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public interface OnBudgetChangeListener {
        void onBudgetChanged();
    }

    public CategoryBudgetAdapter(List<CategoryBudget> budgetList, OnBudgetChangeListener listener) {
        this.budgetList = budgetList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryBudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_budget, parent, false);
        return new CategoryBudgetViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryBudgetViewHolder holder, int position) {
        // Remove any existing watcher to prevent unwanted updates during view recycling.
        if (holder.textWatcher != null) {
            holder.categoryBudgetEditText.removeTextChangedListener(holder.textWatcher);
        }

        CategoryBudget budget = budgetList.get(position);
        holder.categoryNameTextView.setText(budget.getCategory());

        // Set the text *after* removing the listener.
        if (budget.getBudgetedAmount() > 0) {
            holder.categoryBudgetEditText.setText(decimalFormat.format(budget.getBudgetedAmount()));
        } else {
            holder.categoryBudgetEditText.setText("");
        }

        // Create a new watcher for the current item.
        holder.textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Ensure we are updating the correct budget item.
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    try {
                        String input = s.toString();
                        double amount = input.isEmpty() ? 0.0 : Double.parseDouble(input);
                        budgetList.get(currentPosition).setBudgetedAmount(amount);

                        if (listener != null) {
                            listener.onBudgetChanged();
                        }
                    } catch (NumberFormatException e) {
                        budgetList.get(currentPosition).setBudgetedAmount(0.0);
                    }
                }
            }
        };
        holder.categoryBudgetEditText.addTextChangedListener(holder.textWatcher);
    }

    @Override
    public int getItemCount() {
        return budgetList.size();
    }

    public void setData(List<CategoryBudget> newBudgetList) {
        this.budgetList = newBudgetList;
        notifyDataSetChanged();
    }

    public List<CategoryBudget> getBudgetData() {
        return this.budgetList;
    }

    public static class CategoryBudgetViewHolder extends RecyclerView.ViewHolder {
        public TextView categoryNameTextView;
        public TextInputEditText categoryBudgetEditText;
        public TextWatcher textWatcher;

        public CategoryBudgetViewHolder(View view) {
            super(view);
            categoryNameTextView = view.findViewById(R.id.categoryNameTextView);
            categoryBudgetEditText = view.findViewById(R.id.categoryBudgetEditText);
        }
    }
}
