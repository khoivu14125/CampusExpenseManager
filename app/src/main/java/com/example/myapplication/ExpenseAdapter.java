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
 * Adapter hiển thị danh sách các khoản chi phí.
 */
public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> expenseList;
    private OnItemClickListener listener;
    private final SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    // Interface xử lý sự kiện click vào item (để sửa hoặc xóa)
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

        // Định dạng ngày hiển thị (từ yyyy-MM-dd sang dd-MM-yyyy)
        try {
            Date date = dbDateFormat.parse(expense.getDate());
            holder.dateTextView.setText(displayDateFormat.format(date));
        } catch (ParseException e) {
            holder.dateTextView.setText(expense.getDate()); // Fallback nếu lỗi
        }

        // Định dạng số tiền theo chuẩn tiền tệ Việt Nam
        holder.amountTextView.setText(currencyFormat.format(expense.getAmount()));

        // Hiển thị ghi chú nếu có
        if (expense.getDescription() != null && !expense.getDescription().isEmpty()) {
            holder.descriptionTextView.setText("Ghi chú: " + expense.getDescription());
            holder.descriptionTextView.setVisibility(View.VISIBLE);
        } else {
            holder.descriptionTextView.setVisibility(View.GONE);
        }

        // Gán sự kiện click
        holder.itemView.setOnClickListener(v -> listener.onItemClick(expense));
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    // Cập nhật dữ liệu mới cho adapter
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
