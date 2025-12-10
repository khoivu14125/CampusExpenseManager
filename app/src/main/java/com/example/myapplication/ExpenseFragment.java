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

        // Khởi tạo các view
        addExpenseFab = view.findViewById(R.id.addExpenseFab);
        recyclerView = view.findViewById(R.id.expenseRecyclerView);
        startDateEditText = view.findViewById(R.id.startDateEditText);
        endDateEditText = view.findViewById(R.id.endDateEditText);
        searchCategoryAutoCompleteTextView = view.findViewById(R.id.searchCategoryAutoCompleteTextView);
        searchCategoryContainer = view.findViewById(R.id.searchCategoryContainer);
        transactionTypeTabLayout = view.findViewById(R.id.transactionTypeTabLayout);

        // Thiết lập RecyclerView và Adapters
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        expenseAdapter = new ExpenseAdapter(expenseList, this);
        
        // Khởi tạo IncomeAdapter với listener xử lý khi click vào item
        incomeAdapter = new IncomeAdapter(incomeList, this::onIncomeItemClick);
        
        // Mặc định hiển thị danh sách chi phí
        recyclerView.setAdapter(expenseAdapter);

        setupSearchFields();
        setupTabs();
        loadData();

        // Xử lý sự kiện khi nhấn nút thêm mới (Floating Action Button)
        addExpenseFab.setOnClickListener(v -> showAddChoiceDialog());
    }

    // Thiết lập Tabs để chuyển đổi giữa xem Chi phí và Thu nhập
    private void setupTabs() {
        transactionTypeTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadData(); // Tải lại dữ liệu khi chuyển tab
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    // Tải dữ liệu dựa trên tab đang chọn và các bộ lọc tìm kiếm
    private void loadData() {
        int selectedTabPosition = transactionTypeTabLayout.getSelectedTabPosition();
        if (selectedTabPosition == 0) {
            // Tab Chi phí
            recyclerView.setAdapter(expenseAdapter);
            searchCategoryContainer.setVisibility(View.VISIBLE); // Hiển thị bộ lọc danh mục
            performExpenseSearch();
        } else {
            // Tab Thu nhập
            recyclerView.setAdapter(incomeAdapter);
            searchCategoryContainer.setVisibility(View.GONE); // Ẩn bộ lọc danh mục vì thu nhập không có danh mục chi tiết như chi phí
            performIncomeSearch();
        }
    }

    // Hiển thị dialog để chọn thêm Chi phí hoặc Thu nhập
    private void showAddChoiceDialog() {
        final String[] options = {"Thêm Chi Phí", "Thêm Thu Nhập"};
        new AlertDialog.Builder(getContext())
                .setTitle("Chọn loại giao dịch")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showAddOrUpdateExpenseDialog(null); // null nghĩa là thêm mới
                    } else {
                        showAddOrUpdateIncomeDialog(null); // null nghĩa là thêm mới
                    }
                })
                .show();
    }

    // Hiển thị dialog thêm hoặc cập nhật thu nhập
    private void showAddOrUpdateIncomeDialog(@Nullable final Income incomeToUpdate) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_income, null);
        builder.setView(dialogView);

        final TextInputEditText amountEditText = dialogView.findViewById(R.id.incomeAmountEditText);
        final TextInputEditText noteEditText = dialogView.findViewById(R.id.incomeNoteEditText);
        final Button saveButton = dialogView.findViewById(R.id.saveIncomeButton);
        
        // Nếu là cập nhật, điền thông tin cũ vào form
        if (incomeToUpdate != null) {
            amountEditText.setText(String.valueOf((long)incomeToUpdate.getAmount())); 
            noteEditText.setText(incomeToUpdate.getDescription());
            saveButton.setText("Cập nhật");
        }

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
                String dateStr = (incomeToUpdate != null) ? incomeToUpdate.getDate() : dbDateFormat.format(new Date());

                if (incomeToUpdate == null) {
                    // Thêm thu nhập mới
                    long result = db.addIncome(amount, note, dateStr);
                    if (result != -1) {
                        Toast.makeText(getContext(), "Đã thêm thu nhập thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Lỗi khi thêm thu nhập", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Cập nhật thu nhập hiện có
                    int result = db.updateIncome(incomeToUpdate.getId(), amount, note, dateStr);
                    if (result > 0) {
                        Toast.makeText(getContext(), "Đã cập nhật thu nhập", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Lỗi khi cập nhật thu nhập", Toast.LENGTH_SHORT).show();
                    }
                }
                dialog.dismiss();
                loadData(); // Làm mới danh sách
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
    
    // Xử lý khi click vào một mục thu nhập: Hiển thị lựa chọn Cập nhật hoặc Xóa
    private void onIncomeItemClick(Income income) {
        new AlertDialog.Builder(getContext())
                .setTitle("Chọn Hành Động")
                .setItems(new String[]{"Cập nhật", "Xóa"}, (dialog, which) -> {
                    if (which == 0) {
                        showAddOrUpdateIncomeDialog(income);
                    } else {
                        new AlertDialog.Builder(getContext())
                                .setTitle("Xóa Thu Nhập")
                                .setMessage("Bạn có chắc chắn muốn xóa khoản thu nhập này không?")
                                .setPositiveButton("Có", (d, w) -> {
                                    db.deleteIncome(income.getId());
                                    Toast.makeText(getContext(), "Đã xóa thu nhập", Toast.LENGTH_SHORT).show();
                                    loadData();
                                })
                                .setNegativeButton("Không", null)
                                .show();
                    }
                })
                .show();
    }

    // Thiết lập các trường tìm kiếm và bộ lọc
    private void setupSearchFields() {
        startDateEditText.setOnClickListener(v -> showDatePickerDialog(startDateEditText));
        endDateEditText.setOnClickListener(v -> showDatePickerDialog(endDateEditText));

        String[] searchCategories = {"Tất cả", "Thuê nhà", "Ăn uống", "Đi lại", "Giáo dục", "Giải trí", "Sức khỏe", "Quần áo", "Khác"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, searchCategories);
        searchCategoryAutoCompleteTextView.setAdapter(categoryAdapter);
        searchCategoryAutoCompleteTextView.setText("Tất cả", false);

        // Tự động tìm kiếm khi thay đổi ngày hoặc danh mục
        TextWatcher searchWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { loadData(); }
        };
        startDateEditText.addTextChangedListener(searchWatcher);
        endDateEditText.addTextChangedListener(searchWatcher);
        searchCategoryAutoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> loadData());
    }

    // Hiển thị dialog chọn ngày
    private void showDatePickerDialog(final TextInputEditText dateEditText) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            String selectedDate = dbDateFormat.format(calendar.getTime());
            dateEditText.setText(selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    // Thực hiện tìm kiếm chi phí
    private void performExpenseSearch() {
        String startDate = startDateEditText.getText().toString();
        String endDate = endDateEditText.getText().toString();
        String category = searchCategoryAutoCompleteTextView.getText().toString();

        expenseList = db.searchExpenses(startDate, endDate, category);
        expenseAdapter.updateData(expenseList);
    }
    
    // Thực hiện tìm kiếm thu nhập
    private void performIncomeSearch() {
        String startDate = startDateEditText.getText().toString();
        String endDate = endDateEditText.getText().toString();
        
        incomeList = db.searchIncomes(startDate, endDate);
        incomeAdapter.updateData(incomeList);
    }

    // Hiển thị dialog thêm hoặc cập nhật chi phí
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

            // Nếu đang không lọc theo ngày, reset bộ lọc danh mục về Tất cả để thấy item vừa thêm
            if (TextUtils.isEmpty(startDateEditText.getText()) && TextUtils.isEmpty(endDateEditText.getText())) {
                 searchCategoryAutoCompleteTextView.setText("Tất cả", false);
            }
            
            loadData();

            // Kiểm tra xem có vượt quá ngân sách không và cảnh báo
            String monthYear = monthYearFormat.format(new Date());
            checkBudgetAndShowWarning(category, monthYear);
        });
        dialog.show();
    }

    // Kiểm tra ngân sách và hiển thị cảnh báo nếu vượt quá
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

    // Xử lý khi click vào một mục chi phí
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
