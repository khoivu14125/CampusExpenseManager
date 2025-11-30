package com.example.myapplication;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpenseFragment extends Fragment implements ExpenseAdapter.OnItemClickListener {

    private FloatingActionButton addExpenseFab;
    private RecyclerView recyclerView;
    private ExpenseAdapter expenseAdapter;
    private IncomeAdapter incomeAdapter;
    private List<Expense> expenseList = new ArrayList<>();
    private List<Income> incomeList = new ArrayList<>();
    private DatabaseHelper db;

    private TextInputEditText startDateEditText, endDateEditText;
    private AutoCompleteTextView searchCategoryAutoCompleteTextView;
    private TextInputLayout searchCategoryContainer;
    private TabLayout transactionTypeTabLayout;

    private final SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat monthYearFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        db = DatabaseHelper.getInstance(getContext());
        return inflater.inflate(R.layout.fragment_expense, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addExpenseFab = view.findViewById(R.id.addExpenseFab);
        recyclerView = view.findViewById(R.id.expenseRecyclerView);
        startDateEditText = view.findViewById(R.id.startDateEditText);
        endDateEditText = view.findViewById(R.id.endDateEditText);
        searchCategoryAutoCompleteTextView = view.findViewById(R.id.searchCategoryAutoCompleteTextView);
        searchCategoryContainer = view.findViewById(R.id.searchCategoryContainer);
        transactionTypeTabLayout = view.findViewById(R.id.transactionTypeTabLayout);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        expenseAdapter = new ExpenseAdapter(expenseList, this);
        incomeAdapter = new IncomeAdapter(incomeList);
        
        // Default to showing expenses
        recyclerView.setAdapter(expenseAdapter);

        setupSearchFields();
        setupTabs();
        loadData();

        addExpenseFab.setOnClickListener(v -> showAddChoiceDialog());
    }

    private void setupTabs() {
        transactionTypeTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadData();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    private void loadData() {
        int selectedTabPosition = transactionTypeTabLayout.getSelectedTabPosition();
        if (selectedTabPosition == 0) {
            recyclerView.setAdapter(expenseAdapter);
            searchCategoryContainer.setVisibility(View.VISIBLE);
            performExpenseSearch();
        } else {
            recyclerView.setAdapter(incomeAdapter);
            searchCategoryContainer.setVisibility(View.GONE);
            performIncomeSearch();
        }
    }

    private void showAddChoiceDialog() {
        final String[] options = {"Thêm Chi Phí", "Thêm Thu Nhập"};
        new AlertDialog.Builder(getContext())
                .setTitle("Chọn loại giao dịch")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showAddOrUpdateExpenseDialog(null);
                    } else {
                        showAddIncomeDialog();
                    }
                })
                .show();
    }

    private void showAddIncomeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_income, null);
        builder.setView(dialogView);

        final TextInputEditText amountEditText = dialogView.findViewById(R.id.incomeAmountEditText);
        final TextInputEditText noteEditText = dialogView.findViewById(R.id.incomeNoteEditText);
        final Button saveButton = dialogView.findViewById(R.id.saveIncomeButton);

        AlertDialog dialog = builder.create();

        saveButton.setOnClickListener(v -> {
            String amountStr = amountEditText.getText().toString();
            String note = noteEditText.getText().toString();

            if (TextUtils.isEmpty(amountStr)) {
                Toast.makeText(getContext(), "Số tiền không được để trống", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                String dateStr = dbDateFormat.format(new Date());

                long result = db.addIncome(amount, note, dateStr);
                if (result != -1) {
                    Toast.makeText(getContext(), "Đã thêm thu nhập thành công", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadData(); // Refresh the view
                } else {
                    Toast.makeText(getContext(), "Lỗi khi thêm thu nhập", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void setupSearchFields() {
        startDateEditText.setOnClickListener(v -> showDatePickerDialog(startDateEditText));
        endDateEditText.setOnClickListener(v -> showDatePickerDialog(endDateEditText));

        String[] searchCategories = {"Tất cả", "Thuê nhà", "Ăn uống", "Đi lại", "Giáo dục", "Giải trí", "Sức khỏe", "Quần áo", "Khác"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, searchCategories);
        searchCategoryAutoCompleteTextView.setAdapter(categoryAdapter);
        searchCategoryAutoCompleteTextView.setText("Tất cả", false);

        TextWatcher searchWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { loadData(); }
        };
        startDateEditText.addTextChangedListener(searchWatcher);
        endDateEditText.addTextChangedListener(searchWatcher);
        searchCategoryAutoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> loadData());
    }

    private void showDatePickerDialog(final TextInputEditText dateEditText) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            String selectedDate = dbDateFormat.format(calendar.getTime());
            dateEditText.setText(selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void performExpenseSearch() {
        String startDate = startDateEditText.getText().toString();
        String endDate = endDateEditText.getText().toString();
        String category = searchCategoryAutoCompleteTextView.getText().toString();

        expenseList = db.searchExpenses(startDate, endDate, category);
        expenseAdapter.updateData(expenseList);
    }
    
    private void performIncomeSearch() {
        String startDate = startDateEditText.getText().toString();
        String endDate = endDateEditText.getText().toString();
        
        incomeList = db.searchIncomes(startDate, endDate);
        incomeAdapter.updateData(incomeList);
    }

    private void showAddOrUpdateExpenseDialog(@Nullable final Expense expenseToUpdate) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_expense, null);
        builder.setView(dialogView);

        final TextInputEditText amountEditText = dialogView.findViewById(R.id.amountEditText);
        final AutoCompleteTextView categoryAutoCompleteTextView = dialogView.findViewById(R.id.categoryAutoCompleteTextView);
        final TextInputLayout noteTextInputLayout = dialogView.findViewById(R.id.noteTextInputLayout);
        final TextInputEditText noteEditText = dialogView.findViewById(R.id.noteEditText);
        final Button saveButton = dialogView.findViewById(R.id.saveButton);

        String[] categories = {"Thuê nhà", "Ăn uống", "Đi lại", "Giáo dục", "Giải trí", "Sức khỏe", "Quần áo", "Khác"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, categories);
        categoryAutoCompleteTextView.setAdapter(adapter);

        noteTextInputLayout.setVisibility(View.VISIBLE);

        if (expenseToUpdate != null) {
            amountEditText.setText(String.valueOf(expenseToUpdate.getAmount()));
            categoryAutoCompleteTextView.setText(expenseToUpdate.getCategory(), false);
            noteEditText.setText(expenseToUpdate.getDescription());
        }

        AlertDialog dialog = builder.create();

        saveButton.setOnClickListener(v -> {
            String amountStr = amountEditText.getText().toString();
            String category = categoryAutoCompleteTextView.getText().toString();
            String note = noteEditText.getText().toString();

            if (TextUtils.isEmpty(amountStr) || TextUtils.isEmpty(category)) {
                Log.d("ExpenseFragment", "Số tiền và danh mục là bắt buộc");
                return;
            }

            double amount = Double.parseDouble(amountStr);
            String description = note;
            String dateStr = (expenseToUpdate != null) ? expenseToUpdate.getDate() : dbDateFormat.format(new Date());

            if (expenseToUpdate == null) {
                db.addExpense(amount, description, category, dateStr);
            } else {
                db.updateExpense(expenseToUpdate.getId(), amount, description, category, dateStr);
            }
            dialog.dismiss();

            if (TextUtils.isEmpty(startDateEditText.getText()) && TextUtils.isEmpty(endDateEditText.getText())) {
                 searchCategoryAutoCompleteTextView.setText("Tất cả", false);
            }
            
            loadData();

            String monthYear = monthYearFormat.format(new Date());
            checkBudgetAndShowWarning(category, monthYear);
        });
        dialog.show();
    }

    private void checkBudgetAndShowWarning(String category, String monthYear) {
        double categoryBudget = db.getBudgetForCategory(category, monthYear);
        if (categoryBudget > 0) { 
            double categorySpending = db.getCategorySpendingForMonth(category, monthYear);
            if (categorySpending > categoryBudget) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Vượt Quá Ngân Sách")
                        .setMessage("Bạn đã vượt quá ngân sách cho danh mục '" + category + "' trong tháng này.")
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }
        }
    }

    @Override
    public void onItemClick(Expense expense) {
         new AlertDialog.Builder(getContext())
                .setTitle("Chọn Hành Động")
                .setItems(new String[]{"Cập nhật", "Xóa"}, (dialog, which) -> {
                    if (which == 0) {
                        showAddOrUpdateExpenseDialog(expense);
                    } else {
                        new AlertDialog.Builder(getContext())
                                .setTitle("Xóa Chi Phí")
                                .setMessage("Bạn có chắc chắn muốn xóa khoản chi này không?")
                                .setPositiveButton("Có", (d, w) -> {
                                    db.deleteExpense(expense.getId());
                                    loadData();
                                })
                                .setNegativeButton("Không", null)
                                .show();
                    }
                })
                .show();
    }
}
