package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;
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
    private Button saveButton;
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
        saveButton = view.findViewById(R.id.saveButton);
        totalBudgetTextView = view.findViewById(R.id.totalBudgetTextView);

        budgetsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CategoryBudgetAdapter(currentBudgets, this);
        budgetsRecyclerView.setAdapter(adapter);

        monthYearEditText.setOnClickListener(v -> showMonthYearPickerDialog());
        saveButton.setOnClickListener(v -> saveBudgets());

        setCurrentMonthYear();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (selectedMonthYear != null) {
            loadBudgetsForMonth(selectedMonthYear);
        }
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
        builder.setTitleText("Chọn Tháng và Năm");
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

        List<String> standardCategories = Arrays.asList("Thuê nhà", "Ăn uống", "Đi lại", "Giáo dục", "Giải trí", "Sức khỏe", "Quần áo", "Tiện ích", "Wifi", "Khác");

        currentBudgets.clear();
        
        for (String categoryName : standardCategories) {
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
            Toast.makeText(getContext(), "Vui lòng chọn tháng và năm.", Toast.LENGTH_SHORT).show();
            return;
        }

        for (CategoryBudget budget : budgetsToSave) {
            budget.setMonthYear(selectedMonthYear);
        }

        db.deleteBudgetsForMonth(selectedMonthYear);
        db.saveCategoryBudgets(budgetsToSave);
        
        updateTotalBudget();
        Toast.makeText(getContext(), "Đã lưu ngân sách cho " + selectedMonthYear, Toast.LENGTH_SHORT).show();
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
