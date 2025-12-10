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
    private PieChart categoryPieChart;
    private DatabaseHelper db;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    
    // Điều hướng tháng
    private ImageButton prevMonthButton, nextMonthButton;
    private TextView currentMonthTextView;
    private Calendar selectedMonthCalendar;
    private SimpleDateFormat monthYearFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
    private SimpleDateFormat displayMonthFormat = new SimpleDateFormat("MM/yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        db = DatabaseHelper.getInstance(getContext());
        selectedMonthCalendar = Calendar.getInstance(); // Mặc định là tháng hiện tại
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ các view
        totalBudgetDisplayTextView = view.findViewById(R.id.totalBudgetDisplayTextView);
        totalSpendingTextView = view.findViewById(R.id.totalSpendingTextView);
        remainingBudgetTextView = view.findViewById(R.id.remainingBudgetTextView);
        
        categoryPieChart = view.findViewById(R.id.categoryPieChart);
        
        // Thiết lập điều hướng tháng
        prevMonthButton = view.findViewById(R.id.prevMonthButton);
        nextMonthButton = view.findViewById(R.id.nextMonthButton);
        currentMonthTextView = view.findViewById(R.id.currentMonthTextView);
        
        updateMonthDisplay();
        
        // Xử lý sự kiện chuyển tháng trước
        prevMonthButton.setOnClickListener(v -> {
            selectedMonthCalendar.add(Calendar.MONTH, -1);
            updateMonthDisplay();
            loadDashboardData();
        });
        
        // Xử lý sự kiện chuyển tháng sau
        nextMonthButton.setOnClickListener(v -> {
            selectedMonthCalendar.add(Calendar.MONTH, 1);
            updateMonthDisplay();
            loadDashboardData();
        });
    }
    
    // Cập nhật text hiển thị tháng
    private void updateMonthDisplay() {
        currentMonthTextView.setText("Tháng " + displayMonthFormat.format(selectedMonthCalendar.getTime()));
    }

    @Override
    public void onResume() {
        super.onResume();
        db.processRecurringExpenses(); // Xử lý các chi phí định kỳ trước khi load dữ liệu
        loadDashboardData(); // Load dữ liệu dashboard
    }

    // Tải và hiển thị dữ liệu cho dashboard
    private void loadDashboardData() {
        String currentMonthYear = monthYearFormat.format(selectedMonthCalendar.getTime());

        // Lấy tổng thu nhập trong tháng
        double totalIncome = db.getTotalIncomeForMonth(currentMonthYear);

        // Lấy tổng chi tiêu trong tháng
        double totalSpending = db.getTotalSpendingForMonth(currentMonthYear);
        
        // Tính toán số dư (Thu nhập - Chi tiêu)
        double balance = totalIncome - totalSpending;

        // Cập nhật giao diện
        totalBudgetDisplayTextView.setText(currencyFormat.format(totalIncome));
        totalSpendingTextView.setText(currencyFormat.format(totalSpending));
        remainingBudgetTextView.setText(currencyFormat.format(balance));
        
        // Đổi màu sắc dựa trên trạng thái số dư (Dương: Xanh, Âm: Đỏ)
        if (balance < 0) {
             remainingBudgetTextView.setTextColor(Color.RED);
        } else {
             // Màu xanh đậm cho số dư dương
             remainingBudgetTextView.setTextColor(Color.parseColor("#006400")); 
        }

        // Hiển thị biểu đồ tròn phân tích chi tiêu theo danh mục
        setupCategoryPieChart(currentMonthYear);
    }

    // Cấu hình và hiển thị biểu đồ tròn (PieChart)
    private void setupCategoryPieChart(String monthYear) {
        categoryPieChart.setVisibility(View.VISIBLE);
        List<DatabaseHelper.CategorySpending> spendingList = db.getSpendingByCategoryForMonth(monthYear);

        // Nếu không có dữ liệu, hiển thị thông báo
        if (spendingList.isEmpty()) {
            categoryPieChart.clear();
            categoryPieChart.setNoDataText("Không có dữ liệu chi tiêu cho tháng này");
            categoryPieChart.invalidate();
            return;
        }

        // Tạo dữ liệu cho biểu đồ
        List<PieEntry> entries = new ArrayList<>();
        for (DatabaseHelper.CategorySpending spending : spendingList) {
            entries.add(new PieEntry((float) spending.totalAmount, spending.category));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Chi tiêu theo danh mục");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS); // Sử dụng bảng màu mặc định
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        categoryPieChart.setData(pieData);
        categoryPieChart.getDescription().setEnabled(false);
        categoryPieChart.setEntryLabelColor(Color.BLACK);
        categoryPieChart.invalidate(); // Làm mới biểu đồ
    }
}
