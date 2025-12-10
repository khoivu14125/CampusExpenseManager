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

/**
 * Adapter cho RecyclerView hiển thị danh sách ngân sách theo danh mục.
 * Cho phép người dùng nhập/sửa số tiền ngân sách trực tiếp.
 */
public class CategoryBudgetAdapter extends RecyclerView.Adapter<CategoryBudgetAdapter.CategoryBudgetViewHolder> {

    private List<CategoryBudget> budgetList;
    private OnBudgetChangeListener listener;
    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    // Interface để thông báo khi có thay đổi về ngân sách (để cập nhật tổng ngân sách)
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
        // Gỡ bỏ TextWatcher cũ để tránh việc kích hoạt listener không mong muốn khi tái sử dụng view
        if (holder.textWatcher != null) {
            holder.categoryBudgetEditText.removeTextChangedListener(holder.textWatcher);
        }

        CategoryBudget budget = budgetList.get(position);
        holder.categoryNameTextView.setText(budget.getCategory());

        // Hiển thị số tiền ngân sách (nếu > 0)
        if (budget.getBudgetedAmount() > 0) {
            holder.categoryBudgetEditText.setText(decimalFormat.format(budget.getBudgetedAmount()));
        } else {
            holder.categoryBudgetEditText.setText("");
        }

        // Tạo TextWatcher mới cho item hiện tại
        holder.textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Đảm bảo cập nhật đúng item trong danh sách
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    try {
                        String input = s.toString();
                        double amount = input.isEmpty() ? 0.0 : Double.parseDouble(input);
                        budgetList.get(currentPosition).setBudgetedAmount(amount);

                        // Thông báo cho Fragment biết ngân sách đã thay đổi
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
