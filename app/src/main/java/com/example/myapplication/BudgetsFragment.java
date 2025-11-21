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
    private FloatingActionButton addRecurringExpenseFab;
    private Button saveButton;
    private TextView totalBudgetTextView;
    private CategoryBudgetAdapter adapter;
    private DatabaseHelper db;
    private List<CategoryBudget> currentBudgets = new ArrayList<>();
    private String selectedMonthYear;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());


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
        addRecurringExpenseFab = view.findViewById(R.id.addRecurringExpenseFab);
        saveButton = view.findViewById(R.id.saveButton);
        totalBudgetTextView = view.findViewById(R.id.totalBudgetTextView);

        budgetsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CategoryBudgetAdapter(currentBudgets, this);
        budgetsRecyclerView.setAdapter(adapter);

        monthYearEditText.setOnClickListener(v -> showMonthYearPickerDialog());
        addRecurringExpenseFab.setOnClickListener(v -> showAddRecurringExpenseDialog());
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
        
        // 1. Thêm các danh mục chuẩn vào list hiển thị
        for (String categoryName : standardCategories) {
            CategoryBudget budget = existingBudgetsMap.get(categoryName);
            if (budget == null) {
                // Nếu chưa có thì tạo mới với giá trị 0
                budget = new CategoryBudget(monthYear, categoryName, 0.0);
            }
            currentBudgets.add(budget);
        }

        // 2. Lưu ý: Tôi đã loại bỏ bước thêm các danh mục "lạ" (tiếng Anh) vào currentBudgets.
        // Khi người dùng nhấn "Lưu", danh sách currentBudgets (chỉ chứa tiếng Việt) sẽ được ghi đè vào DB.
        // DatabaseHelper.saveCategoryBudgets sử dụng logic REPLACE, nhưng nó không xóa các bản ghi thừa.
        // Tuy nhiên, để thực sự xóa "bóng ma", ta cần một phương thức xóa triệt để trong DB hoặc ghi đè thông minh.
        
        // Nhưng với cách implement hiện tại của saveBudgets bên dưới, nó chỉ update/insert những cái CÓ trong list.
        // Những cái KHÔNG có trong list (tiếng Anh) vẫn nằm im trong DB gây sai lệch tổng.
        
        // GIẢI PHÁP: Để xóa các mục tiếng Anh, ta cần thêm chúng vào list nhưng set giá trị về 0 (cách tạm thời)
        // HOẶC tốt nhất là xóa chúng khỏi DB trước khi lưu cái mới.
        
        // Ở đây, tôi sẽ chọn cách: Hiển thị CHỈ tiếng Việt.
        // Khi bấm LƯU, tôi sẽ gọi một hàm (mà tôi sẽ thêm vào DatabaseHelper sau nếu cần, hoặc dùng trick ở đây)
        // Nhưng để đơn giản và an toàn, tôi sẽ làm như sau trong hàm saveBudgets:
        // Trước khi lưu danh sách mới, tôi sẽ xóa toàn bộ budget của tháng đó đi, rồi insert lại list mới.
        // Như vậy sẽ sạch bóng ma.

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

        // Xóa toàn bộ ngân sách cũ của tháng này để loại bỏ các danh mục tiếng Anh "ma"
        db.deleteBudgetsForMonth(selectedMonthYear);
        
        // Lưu danh sách mới (chỉ toàn tiếng Việt)
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

    private void showAddRecurringExpenseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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

        AlertDialog dialog = builder.create();

        saveButton.setOnClickListener(v -> {
            String amountStr = amountEditText.getText().toString();
            String category = categoryAutoCompleteTextView.getText().toString();
            String description = descriptionEditText.getText().toString();
            String startDate = startDateEditText.getText().toString();
            String endDate = endDateEditText.getText().toString();

            if (TextUtils.isEmpty(amountStr) || TextUtils.isEmpty(category) || TextUtils.isEmpty(startDate) || TextUtils.isEmpty(endDate)) {
                Toast.makeText(getContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);
            RecurringExpense expense = new RecurringExpense(amount, description, category, startDate, endDate, null);
            db.addRecurringExpense(expense);

            // Process recurring expenses immediately to ensure they appear in transactions
            db.processRecurringExpenses();
            
            // --- Logic Update Budget ---
            // Check if the recurring expense falls within the currently selected month
            // Assuming startDate is "yyyy-MM-dd" and selectedMonthYear is "yyyy-MM"
            if (startDate.startsWith(selectedMonthYear)) {
                boolean budgetUpdated = false;
                for (CategoryBudget budget : currentBudgets) {
                    if (budget.getCategory().equals(category)) {
                        double newBudgetAmount = budget.getBudgetedAmount() + amount;
                        budget.setBudgetedAmount(newBudgetAmount);
                        budgetUpdated = true;
                        break;
                    }
                }
                
                // If category not found in current list (should rarely happen due to predefined list), add it
                if (!budgetUpdated) {
                    CategoryBudget newBudget = new CategoryBudget(selectedMonthYear, category, amount);
                    currentBudgets.add(newBudget);
                }
                
                // Save the updated budgets to DB immediately
                db.saveCategoryBudgets(currentBudgets);
                
                // Refresh UI
                adapter.setData(currentBudgets);
                updateTotalBudget();
            }

            dialog.dismiss();
            Toast.makeText(getContext(), "Đã thêm chi phí định kỳ và cập nhật ngân sách", Toast.LENGTH_SHORT).show();
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
