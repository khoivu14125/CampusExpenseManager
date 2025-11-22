package com.example.myapplication;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RecurringExpenseFragment extends Fragment {

    private RecyclerView recurringExpenseRecyclerView;
    private RecurringExpenseAdapter adapter;
    private List<RecurringExpense> recurringExpenseList = new ArrayList<>();
    private DatabaseHelper db;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        db = DatabaseHelper.getInstance(getContext());
        rootView = inflater.inflate(R.layout.fragment_recurring_expense, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup RecyclerView
        recurringExpenseRecyclerView = view.findViewById(R.id.recurringExpenseRecyclerView);
        recurringExpenseRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecurringExpenseAdapter(recurringExpenseList);
        recurringExpenseRecyclerView.setAdapter(adapter);

        FloatingActionButton fab = view.findViewById(R.id.fab_add_recurring_expense);
        fab.setOnClickListener(v -> showAddRecurringExpenseDialog());

        loadRecurringExpenses();
    }

    private void showAddRecurringExpenseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_recurring_expense, null);
        builder.setView(dialogView);

        final TextInputEditText amountEditText = dialogView.findViewById(R.id.amountEditText);
        final AutoCompleteTextView categoryAutoCompleteTextView = dialogView.findViewById(R.id.categoryAutoCompleteTextView);
        final TextInputEditText descriptionEditText = dialogView.findViewById(R.id.descriptionEditText);
        final TextInputEditText startDateEditText = dialogView.findViewById(R.id.startDateEditText);
        final TextInputEditText endDateEditText = dialogView.findViewById(R.id.endDateEditText);
        final Button saveButton = dialogView.findViewById(R.id.saveButton);
        final Button cancelButton = dialogView.findViewById(R.id.cancelButton);

        String[] categories = {"Thuê nhà", "Ăn uống", "Đi lại", "Giáo dục", "Giải trí", "Sức khỏe", "Quần áo", "Tiện ích", "Wifi", "Khác"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, categories);
        categoryAutoCompleteTextView.setAdapter(categoryAdapter);

        startDateEditText.setOnClickListener(v -> showDatePickerDialog(startDateEditText));
        endDateEditText.setOnClickListener(v -> showDatePickerDialog(endDateEditText));

        AlertDialog dialog = builder.create();

        saveButton.setOnClickListener(v -> {
            addRecurringExpense(amountEditText, categoryAutoCompleteTextView, descriptionEditText, startDateEditText, endDateEditText);
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRecurringExpenses();
    }

    private void loadRecurringExpenses() {
        recurringExpenseList.clear();
        recurringExpenseList.addAll(db.getAllRecurringExpenses());
        adapter.notifyDataSetChanged();
    }

    private void addRecurringExpense(EditText amountEditText, AutoCompleteTextView categoryAutoCompleteTextView, EditText descriptionEditText, EditText startDateEditText, EditText endDateEditText) {
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
        RecurringExpense expense = new RecurringExpense();
        expense.setAmount(amount);
        expense.setDescription(description);
        expense.setCategory(category);
        expense.setStartDate(startDate);
        expense.setEndDate(endDate);

        db.addRecurringExpense(expense);
        updateBudgetsForRecurringExpense(category, amount, startDate, endDate);

        db.processRecurringExpenses();

        loadRecurringExpenses();
        Snackbar.make(rootView, "Đã thêm chi phí định kỳ và cập nhật ngân sách", Snackbar.LENGTH_SHORT).show();
    }

    private void updateBudgetsForRecurringExpense(String category, double amount, String startDate, String endDate) {
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        try {
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(dateFormat.parse(startDate));

            Calendar endCal = Calendar.getInstance();
            endCal.setTime(dateFormat.parse(endDate));

            startCal.set(Calendar.DAY_OF_MONTH, 1);

            while (startCal.compareTo(endCal) <= 0) {
                String currentMonthYear = monthFormat.format(startCal.getTime());
                updateBudgetForMonth(currentMonthYear, category, amount);
                startCal.add(Calendar.MONTH, 1);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Snackbar.make(rootView, "Lỗi định dạng ngày tháng.", Snackbar.LENGTH_LONG).show();
        }
    }

    private void updateBudgetForMonth(String monthYear, String category, double amountToAdd) {
        List<CategoryBudget> budgets = db.getCategoryBudgetsForMonth(monthYear);
        Map<String, CategoryBudget> budgetMap = budgets.stream().collect(Collectors.toMap(CategoryBudget::getCategory, Function.identity()));

        CategoryBudget targetBudget = budgetMap.get(category);
        List<CategoryBudget> budgetsToSave = new ArrayList<>();

        if (targetBudget != null) {
            targetBudget.setBudgetedAmount(targetBudget.getBudgetedAmount() + amountToAdd);
            budgetsToSave.add(targetBudget);
        } else {
            targetBudget = new CategoryBudget(monthYear, category, amountToAdd);
            budgetsToSave.add(targetBudget);
        }

        if (!budgetsToSave.isEmpty()) {
            db.saveCategoryBudgets(budgetsToSave);
        }
    }


    private void showDatePickerDialog(final EditText dateEditText) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            String selectedDate = dateFormat.format(calendar.getTime());
            dateEditText.setText(selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }
}
