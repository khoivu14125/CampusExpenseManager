package com.example.myapplication;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
    private RecyclerView expenseRecyclerView;
    private ExpenseAdapter expenseAdapter;
    private List<Expense> expenseList = new ArrayList<>();
    private DatabaseHelper db;

    private TextInputEditText startDateEditText, endDateEditText;
    private AutoCompleteTextView searchCategoryAutoCompleteTextView;

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
        expenseRecyclerView = view.findViewById(R.id.expenseRecyclerView);
        startDateEditText = view.findViewById(R.id.startDateEditText);
        endDateEditText = view.findViewById(R.id.endDateEditText);
        searchCategoryAutoCompleteTextView = view.findViewById(R.id.searchCategoryAutoCompleteTextView);

        expenseRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        expenseAdapter = new ExpenseAdapter(expenseList, this);
        expenseRecyclerView.setAdapter(expenseAdapter);

        setupSearchFields();
        loadExpenses();

        addExpenseFab.setOnClickListener(v -> showAddOrUpdateExpenseDialog(null));
    }

    private void setupSearchFields() {
        startDateEditText.setOnClickListener(v -> showDatePickerDialog(startDateEditText));
        endDateEditText.setOnClickListener(v -> showDatePickerDialog(endDateEditText));

        String[] searchCategories = {"All", "Rent", "Food", "Transport", "Education", "Entertainment", "Health", "Clothing", "Other"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, searchCategories);
        searchCategoryAutoCompleteTextView.setAdapter(categoryAdapter);
        searchCategoryAutoCompleteTextView.setText("All", false);

        TextWatcher searchWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { performSearch(); }
        };
        startDateEditText.addTextChangedListener(searchWatcher);
        endDateEditText.addTextChangedListener(searchWatcher);
        searchCategoryAutoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> performSearch());
    }

    private void showDatePickerDialog(final TextInputEditText dateEditText) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            String selectedDate = dbDateFormat.format(calendar.getTime());
            dateEditText.setText(selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void performSearch() {
        String startDate = startDateEditText.getText().toString();
        String endDate = endDateEditText.getText().toString();
        String category = searchCategoryAutoCompleteTextView.getText().toString();

        expenseList = db.searchExpenses(startDate, endDate, category);
        expenseAdapter.updateData(expenseList);
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

        String[] categories = {"Rent", "Food", "Transport", "Education", "Entertainment", "Health", "Clothing", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, categories);
        categoryAutoCompleteTextView.setAdapter(adapter);

        if (expenseToUpdate != null) {
            amountEditText.setText(String.valueOf(expenseToUpdate.getAmount()));
            categoryAutoCompleteTextView.setText(expenseToUpdate.getCategory(), false);
            if ("Other".equals(expenseToUpdate.getCategory())) {
                noteTextInputLayout.setVisibility(View.VISIBLE);
                noteEditText.setText(expenseToUpdate.getDescription());
            }
        }

        categoryAutoCompleteTextView.setOnItemClickListener((parent, v, position, id) -> {
            if ("Other".equals(parent.getItemAtPosition(position).toString())) {
                noteTextInputLayout.setVisibility(View.VISIBLE);
            } else {
                noteTextInputLayout.setVisibility(View.GONE);
            }
        });

        AlertDialog dialog = builder.create();

        saveButton.setOnClickListener(v -> {
            String amountStr = amountEditText.getText().toString();
            String category = categoryAutoCompleteTextView.getText().toString();
            String note = noteEditText.getText().toString();

            if (TextUtils.isEmpty(amountStr) || TextUtils.isEmpty(category)) {
                Toast.makeText(getContext(), "Amount and category are required", Toast.LENGTH_SHORT).show();
                return;
            }
            if ("Other".equals(category) && TextUtils.isEmpty(note)) {
                Toast.makeText(getContext(), "Note is required for \'Other\' category", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);
            String description = "Other".equals(category) ? note : "";
            String dateStr = (expenseToUpdate != null) ? expenseToUpdate.getDate() : dbDateFormat.format(new Date());

            if (expenseToUpdate == null) {
                db.addExpense(amount, description, category, dateStr);
            } else {
                db.updateExpense(expenseToUpdate.getId(), amount, description, category, dateStr);
            }
            dialog.dismiss();
            loadExpenses();

            // Check budget after saving
            String monthYear = monthYearFormat.format(new Date());
            checkBudgetAndShowWarning(category, monthYear);
        });
        dialog.show();
    }

    private void checkBudgetAndShowWarning(String category, String monthYear) {
        double categoryBudget = db.getBudgetForCategory(category, monthYear);
        if (categoryBudget > 0) { // Only check if a budget is set
            double categorySpending = db.getCategorySpendingForMonth(category, monthYear);
            if (categorySpending > categoryBudget) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Budget Exceeded")
                        .setMessage("You have exceeded the budget for the '" + category + "' category this month.")
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }
        }
    }

    private void loadExpenses() {
        performSearch();
    }

    @Override
    public void onItemClick(Expense expense) {
         new AlertDialog.Builder(getContext())
                .setTitle("Choose Action")
                .setItems(new String[]{"Update", "Delete"}, (dialog, which) -> {
                    if (which == 0) {
                        showAddOrUpdateExpenseDialog(expense);
                    } else {
                        new AlertDialog.Builder(getContext())
                                .setTitle("Delete Expense")
                                .setMessage("Are you sure you want to delete this expense?")
                                .setPositiveButton(android.R.string.yes, (d, w) -> {
                                    db.deleteExpense(expense.getId());
                                    loadExpenses();
                                })
                                .setNegativeButton(android.R.string.no, null)
                                .show();
                    }
                })
                .show();
    }
}
