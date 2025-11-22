package com.example.myapplication;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView totalSpendingTextView, remainingBudgetTextView, totalBudgetDisplayTextView;
    private TextView incomeDisplayTextView;
    private Button editIncomeButton;
    private PieChart categoryPieChart;
    private DatabaseHelper db;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    
    // Month navigation
    private ImageButton prevMonthButton, nextMonthButton;
    private TextView currentMonthTextView;
    private Calendar selectedMonthCalendar;
    private SimpleDateFormat monthYearFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
    private SimpleDateFormat displayMonthFormat = new SimpleDateFormat("MM/yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        db = DatabaseHelper.getInstance(getContext());
        selectedMonthCalendar = Calendar.getInstance(); // Default to current month
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        totalBudgetDisplayTextView = view.findViewById(R.id.totalBudgetDisplayTextView);
        totalSpendingTextView = view.findViewById(R.id.totalSpendingTextView);
        remainingBudgetTextView = view.findViewById(R.id.remainingBudgetTextView);
        
        incomeDisplayTextView = view.findViewById(R.id.incomeDisplayTextView);
        editIncomeButton = view.findViewById(R.id.editIncomeButton);
        
        categoryPieChart = view.findViewById(R.id.categoryPieChart);
        
        // Setup month navigation
        prevMonthButton = view.findViewById(R.id.prevMonthButton);
        nextMonthButton = view.findViewById(R.id.nextMonthButton);
        currentMonthTextView = view.findViewById(R.id.currentMonthTextView);
        
        updateMonthDisplay();
        
        prevMonthButton.setOnClickListener(v -> {
            selectedMonthCalendar.add(Calendar.MONTH, -1);
            updateMonthDisplay();
            loadDashboardData();
        });
        
        nextMonthButton.setOnClickListener(v -> {
            selectedMonthCalendar.add(Calendar.MONTH, 1);
            updateMonthDisplay();
            loadDashboardData();
        });
        
        editIncomeButton.setOnClickListener(v -> showEditIncomeDialog());
    }
    
    private void updateMonthDisplay() {
        currentMonthTextView.setText("Tháng " + displayMonthFormat.format(selectedMonthCalendar.getTime()));
    }
    
    private void showEditIncomeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Nhập thu nhập tháng");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        
        // Pre-fill with current value if exists
        String currentMonthYear = monthYearFormat.format(selectedMonthCalendar.getTime());
        double currentIncome = db.getMonthlyIncome(currentMonthYear);
        if (currentIncome > 0) {
             input.setText(String.valueOf((int)currentIncome)); // Display as int for cleaner look if possible
        }
        
        builder.setView(input);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String incomeStr = input.getText().toString();
            if (!incomeStr.isEmpty()) {
                try {
                    double income = Double.parseDouble(incomeStr);
                    db.setMonthlyIncome(currentMonthYear, income);
                    loadDashboardData(); // Refresh UI
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        db.processRecurringExpenses(); // Process recurring expenses first
        loadDashboardData(); // Then load the data
    }

    private void loadDashboardData() {
        String currentMonthYear = monthYearFormat.format(selectedMonthCalendar.getTime());

        // Load income
        double income = db.getMonthlyIncome(currentMonthYear);
        incomeDisplayTextView.setText(currencyFormat.format(income));

        // Load and display monthly overview
        double totalSpending = db.getTotalSpendingForMonth(currentMonthYear);
        
        // Calculate remaining budget (Income - Spending)
        double remainingBudget = income - totalSpending;

        // Update labels
        totalBudgetDisplayTextView.setText("Tổng thu nhập: " + currencyFormat.format(income));
        totalSpendingTextView.setText("Đã chi tiêu: " + currencyFormat.format(totalSpending));
        remainingBudgetTextView.setText("Số dư: " + currencyFormat.format(remainingBudget));

        // Load and display category analysis
        setupCategoryPieChart(currentMonthYear);
    }

    private void setupCategoryPieChart(String monthYear) {
        categoryPieChart.setVisibility(View.VISIBLE);
        List<DatabaseHelper.CategorySpending> spendingList = db.getSpendingByCategoryForMonth(monthYear);

        if (spendingList.isEmpty()) {
            categoryPieChart.clear();
            categoryPieChart.setNoDataText("Không có dữ liệu chi tiêu cho tháng này");
            categoryPieChart.invalidate();
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        for (DatabaseHelper.CategorySpending spending : spendingList) {
            entries.add(new PieEntry((float) spending.totalAmount, spending.category));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Chi tiêu theo danh mục");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        categoryPieChart.setData(pieData);
        categoryPieChart.getDescription().setEnabled(false);
        categoryPieChart.setEntryLabelColor(Color.BLACK);
        categoryPieChart.invalidate(); // Refresh chart
    }
}
