package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BudgetsFragment extends Fragment implements CategoryBudgetAdapter.OnBudgetChangeListener {

    private TextInputEditText monthYearEditText;
    private RecyclerView budgetsRecyclerView;
    private FloatingActionButton setBudgetFab;
    private TextView totalBudgetTextView;
    private CategoryBudgetAdapter adapter;
    private DatabaseHelper db;
    private List<CategoryBudget> currentBudgets = new ArrayList<>();
    private String selectedMonthYear;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        db = DatabaseHelper.getInstance(getContext());
        return inflater.inflate(R.layout.fragment_budgets, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        monthYearEditText = view.findViewById(R.id.monthYearEditText);
        budgetsRecyclerView = view.findViewById(R.id.budgetsRecyclerView);
        setBudgetFab = view.findViewById(R.id.setBudgetFab);
        totalBudgetTextView = view.findViewById(R.id.totalBudgetTextView);

        budgetsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CategoryBudgetAdapter(currentBudgets, this);
        budgetsRecyclerView.setAdapter(adapter);

        monthYearEditText.setOnClickListener(v -> showMonthYearPickerDialog());
        setBudgetFab.setOnClickListener(v -> saveBudgets());

        setCurrentMonthYear();
    }

    private void setCurrentMonthYear() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        selectedMonthYear = dateFormat.format(calendar.getTime());
        monthYearEditText.setText(selectedMonthYear);
        loadBudgetsForMonth(selectedMonthYear);
    }

    private void showMonthYearPickerDialog() {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Select Month and Year");
        final MaterialDatePicker<Long> picker = builder.build();

        picker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selection);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
            selectedMonthYear = dateFormat.format(calendar.getTime());
            monthYearEditText.setText(selectedMonthYear);
            loadBudgetsForMonth(selectedMonthYear);
        });

        picker.show(getChildFragmentManager(), picker.toString());
    }

    private void loadBudgetsForMonth(String monthYear) {
        Map<String, CategoryBudget> existingBudgetsMap = db.getCategoryBudgetsForMonth(monthYear)
                .stream()
                .collect(Collectors.toMap(CategoryBudget::getCategory, Function.identity()));

        List<String> allCategories = Arrays.asList("Rent", "Food", "Transport", "Education", "Entertainment", "Health", "Clothing", "Utilities", "Other");

        currentBudgets.clear();
        for (String categoryName : allCategories) {
            CategoryBudget budget = existingBudgetsMap.get(categoryName);
            if (budget == null) {
                budget = new CategoryBudget(monthYear, categoryName, 0.0);
            }
            currentBudgets.add(budget);
        }
        adapter.setData(currentBudgets);
        updateTotalBudget();
    }

    private void saveBudgets() {
        List<CategoryBudget> budgetsToSave = adapter.getBudgetData();

        if (selectedMonthYear == null || selectedMonthYear.isEmpty()) {
            Toast.makeText(getContext(), "Please select a month and year.", Toast.LENGTH_SHORT).show();
            return;
        }

        for (CategoryBudget budget : budgetsToSave) {
            budget.setMonthYear(selectedMonthYear);
        }

        db.saveCategoryBudgets(budgetsToSave);
        updateTotalBudget();
        Toast.makeText(getContext(), "Budgets for " + selectedMonthYear + " saved.", Toast.LENGTH_SHORT).show();
    }

    private void updateTotalBudget() {
        double total = 0;
        for (CategoryBudget budget : currentBudgets) {
            total += budget.getBudgetedAmount();
        }
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        totalBudgetTextView.setText(String.format("Tổng: %s", currencyFormat.format(total)));
    }

    @Override
    public void onBudgetChanged() {
        updateTotalBudget();
    }
}
