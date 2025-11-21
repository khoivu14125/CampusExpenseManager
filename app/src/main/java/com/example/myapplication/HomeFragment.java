package com.example.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import java.util.Collections;

public class HomeFragment extends Fragment {

    private TextView totalSpendingTextView, remainingBudgetTextView, totalBudgetDisplayTextView;
    private PieChart categoryPieChart;
    private LineChart spendingTrendChart;
    private DatabaseHelper db;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        db = DatabaseHelper.getInstance(getContext());
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        totalBudgetDisplayTextView = view.findViewById(R.id.totalBudgetDisplayTextView);
        totalSpendingTextView = view.findViewById(R.id.totalSpendingTextView);
        remainingBudgetTextView = view.findViewById(R.id.remainingBudgetTextView);
        categoryPieChart = view.findViewById(R.id.categoryPieChart);
        spendingTrendChart = view.findViewById(R.id.spendingTrendChart);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardData(); // Tải lại dữ liệu mỗi khi fragment được hiển thị
    }

    private void loadDashboardData() {
        String currentMonthYear = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Calendar.getInstance().getTime());

        // Load and display monthly overview
        double totalSpending = db.getTotalSpendingForMonth(currentMonthYear);
        double totalBudget = db.getTotalBudgetForMonth(currentMonthYear);
        double remainingBudget = totalBudget - totalSpending;

        totalBudgetDisplayTextView.setText("Tổng ngân sách: " + currencyFormat.format(totalBudget));
        totalSpendingTextView.setText("Đã chi tiêu: " + currencyFormat.format(totalSpending));
        remainingBudgetTextView.setText("Ngân sách còn lại: " + currencyFormat.format(remainingBudget));

        // Load and display category analysis
        setupCategoryPieChart();

        // Load and display spending trend
        setupSpendingTrendChart();
    }

    private void setupCategoryPieChart() {
        categoryPieChart.setVisibility(View.VISIBLE);
        String currentMonthYear = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Calendar.getInstance().getTime());
        List<DatabaseHelper.CategorySpending> spendingList = db.getSpendingByCategoryForMonth(currentMonthYear);

        if (spendingList.isEmpty()) {
            categoryPieChart.clear();
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

    private void setupSpendingTrendChart() {
        spendingTrendChart.setVisibility(View.VISIBLE);
        List<DatabaseHelper.MonthlySpending> trendList = db.getMonthlySpendingTrend(6); // Last 6 months

        if (trendList.isEmpty() || trendList.size() < 2) {
            spendingTrendChart.clear();
            spendingTrendChart.invalidate();
            return;
        }

        List<Entry> entries = new ArrayList<>();
        final List<String> xAxisLabels = new ArrayList<>();
        for (int i = 0; i < trendList.size(); i++) {
            entries.add(new Entry(i, (float) trendList.get(i).totalAmount));
            xAxisLabels.add(trendList.get(i).monthYear);
        }

        LineDataSet dataSet = new LineDataSet(entries, "Xu hướng chi tiêu");
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.chart_line_color));
        dataSet.setCircleColor(ContextCompat.getColor(getContext(), R.color.chart_circle_color));

        LineData lineData = new LineData(dataSet);
        spendingTrendChart.setData(lineData);

        XAxis xAxis = spendingTrendChart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value >= 0 && value < xAxisLabels.size()) {
                    return xAxisLabels.get((int) value);
                }
                return "";
            }
        });
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        spendingTrendChart.getDescription().setEnabled(false);
        spendingTrendChart.invalidate(); // Refresh chart
    }
}
