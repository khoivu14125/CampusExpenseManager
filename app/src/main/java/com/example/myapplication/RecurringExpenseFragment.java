package com.example.myapplication;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RecurringExpenseFragment extends Fragment {

    private RecyclerView recurringExpenseRecyclerView;
    private FloatingActionButton addRecurringExpenseFab;
    private RecurringExpenseAdapter adapter;
    private List<RecurringExpense> recurringExpenseList = new ArrayList<>();
    private DatabaseHelper db;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        db = DatabaseHelper.getInstance(getContext());
        return inflater.inflate(R.layout.fragment_recurring_expense, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootView = view;

        recurringExpenseRecyclerView = view.findViewById(R.id.recurringExpenseRecyclerView);
        addRecurringExpenseFab = view.findViewById(R.id.addRecurringExpenseFab);

        recurringExpenseRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecurringExpenseAdapter(recurringExpenseList);
        recurringExpenseRecyclerView.setAdapter(adapter);

        addRecurringExpenseFab.setOnClickListener(v -> showAddRecurringExpenseDialog());

        loadRecurringExpenses();
    }

    private void loadRecurringExpenses() {
        recurringExpenseList.clear();
        recurringExpenseList.addAll(db.getAllRecurringExpenses());
        adapter.notifyDataSetChanged();
    }

    private void showAddRecurringExpenseDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_recurring_expense, null);
        builder.setView(dialogView);

        final TextInputEditText amountEditText = dialogView.findViewById(R.id.amountEditText);
        final AutoCompleteTextView categoryAutoCompleteTextView = dialogView.findViewById(R.id.categoryAutoCompleteTextView);
        final TextInputEditText descriptionEditText = dialogView.findViewById(R.id.descriptionEditText);
        final TextInputEditText startDateEditText = dialogView.findViewById(R.id.startDateEditText);
        final TextInputEditText endDateEditText = dialogView.findViewById(R.id.endDateEditText);
        final Button saveButton = dialogView.findViewById(R.id.saveButton);

        String[] categories = {"Thuê nhà", "Ăn uống", "Đi lại", "Giáo dục", "Giải trí", "Sức khỏe", "Quần áo", "Tiện ích", "Wifi", "Khác"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, categories);
        categoryAutoCompleteTextView.setAdapter(categoryAdapter);

        startDateEditText.setOnClickListener(v -> showDatePickerDialog(startDateEditText));
        endDateEditText.setOnClickListener(v -> showDatePickerDialog(endDateEditText));

        androidx.appcompat.app.AlertDialog dialog = builder.create();

        saveButton.setOnClickListener(v -> {
            String amountStr = amountEditText.getText().toString();
            String category = categoryAutoCompleteTextView.getText().toString();
            String description = descriptionEditText.getText().toString();
            String startDate = startDateEditText.getText().toString();
            String endDate = endDateEditText.getText().toString();

            if (TextUtils.isEmpty(amountStr) || TextUtils.isEmpty(category) || TextUtils.isEmpty(startDate) || TextUtils.isEmpty(endDate)) {
                Snackbar.make(rootView, "Vui lòng điền đầy đủ thông tin", Snackbar.LENGTH_LONG).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);
            RecurringExpense expense = new RecurringExpense(amount, description, category, startDate, endDate, null);
            db.addRecurringExpense(expense);
            
            // Check and process immediately if needed
            db.processRecurringExpenses();
            
            dialog.dismiss();
            loadRecurringExpenses();
            Snackbar.make(rootView, "Đã thêm chi phí định kỳ", Snackbar.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    private void showDatePickerDialog(final TextInputEditText dateEditText) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            String selectedDate = dateFormat.format(calendar.getTime());
            dateEditText.setText(selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }
}
