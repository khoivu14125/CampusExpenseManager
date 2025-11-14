package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "CampusExpenseManager.db";
    private static final int DATABASE_VERSION = 4;

    private static DatabaseHelper instance;

    // Tables
    private static final String TABLE_USERS = "users";
    private static final String TABLE_EXPENSES = "expenses";
    private static final String TABLE_CATEGORY_BUDGETS = "category_budgets";

    // Common columns
    private static final String COLUMN_ID = "id";

    // Users columns
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";

    // Expenses columns
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_DATE = "date"; // Format: "yyyy-MM-dd"

    // Category Budgets columns
    private static final String COLUMN_MONTH_YEAR = "month_year"; // Format: "yyyy-MM"
    private static final String COLUMN_CATEGORY_NAME = "category_name";
    private static final String COLUMN_BUDGETED_AMOUNT = "budgeted_amount";

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_EMAIL + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);

        String CREATE_EXPENSES_TABLE = "CREATE TABLE " + TABLE_EXPENSES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_AMOUNT + " REAL NOT NULL,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_CATEGORY + " TEXT,"
                + COLUMN_DATE + " TEXT" + ")";
        db.execSQL(CREATE_EXPENSES_TABLE);

        String CREATE_CATEGORY_BUDGETS_TABLE = "CREATE TABLE " + TABLE_CATEGORY_BUDGETS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_MONTH_YEAR + " TEXT NOT NULL,"
                + COLUMN_CATEGORY_NAME + " TEXT NOT NULL,"
                + COLUMN_BUDGETED_AMOUNT + " REAL NOT NULL,"
                + "UNIQUE(" + COLUMN_MONTH_YEAR + ", " + COLUMN_CATEGORY_NAME + ") ON CONFLICT REPLACE" + ")";
        db.execSQL(CREATE_CATEGORY_BUDGETS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY_BUDGETS);
        onCreate(db);
    }

    // --- User methods ---
    public long addUser(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, BCrypt.hashpw(password, BCrypt.gensalt()));
        return db.insert(TABLE_USERS, null, values);
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_PASSWORD}, COLUMN_EMAIL + " = ?", new String[]{email}, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                String hashedPassword = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
                return BCrypt.checkpw(password, hashedPassword);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return false;
    }

    public boolean checkUser(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID}, COLUMN_EMAIL + " = ?", new String[]{email}, null, null, null);
        try {
            return (cursor.getCount() > 0);
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public boolean updatePassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD, BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        return db.update(TABLE_USERS, values, COLUMN_EMAIL + " = ?", new String[]{email}) > 0;
    }

    // --- Expense methods ---
    public long addExpense(double amount, String description, String category, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_DATE, date);
        return db.insert(TABLE_EXPENSES, null, values);
    }

    public List<Expense> searchExpenses(String startDate, String endDate, String category) {
        List<Expense> expenseList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder selection = new StringBuilder();
        List<String> selectionArgs = new ArrayList<>();
        if (!TextUtils.isEmpty(startDate)) {
            selection.append(COLUMN_DATE + " >= ?");
            selectionArgs.add(startDate);
        }
        if (!TextUtils.isEmpty(endDate)) {
            if (selection.length() > 0) selection.append(" AND ");
            selection.append(COLUMN_DATE + " <= ?");
            selectionArgs.add(endDate);
        }
        if (!TextUtils.isEmpty(category) && !"All".equalsIgnoreCase(category)) {
            if (selection.length() > 0) selection.append(" AND ");
            selection.append(COLUMN_CATEGORY + " = ?");
            selectionArgs.add(category);
        }
        Cursor cursor = db.query(TABLE_EXPENSES, null, selection.length() > 0 ? selection.toString() : null, selectionArgs.toArray(new String[0]), null, null, COLUMN_DATE + " DESC");
        try {
            if (cursor.moveToFirst()) {
                do {
                    Expense expense = new Expense();
                    expense.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                    expense.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)));
                    expense.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                    expense.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
                    expense.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)));
                    expenseList.add(expense);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return expenseList;
    }

    public int updateExpense(int id, double amount, String description, String category, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_DATE, date);
        return db.update(TABLE_EXPENSES, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void deleteExpense(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EXPENSES, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // --- Dashboard & Alert Methods ---
    public double getTotalSpendingForMonth(String monthYear) {
        SQLiteDatabase db = this.getReadableDatabase();
        double total = 0;
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_EXPENSES + " WHERE " + COLUMN_DATE + " LIKE ?", new String[]{monthYear + "%"});
        try {
            if (cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return total;
    }

    public double getTotalBudgetForMonth(String monthYear) {
        SQLiteDatabase db = this.getReadableDatabase();
        double total = 0;
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_BUDGETED_AMOUNT + ") FROM " + TABLE_CATEGORY_BUDGETS + " WHERE " + COLUMN_MONTH_YEAR + " = ?", new String[]{monthYear});
        try {
            if (cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return total;
    }

    public List<CategorySpending> getSpendingByCategoryForMonth(String monthYear) {
        List<CategorySpending> spendingList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_CATEGORY + ", SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_EXPENSES + " WHERE " + COLUMN_DATE + " LIKE ? AND " + COLUMN_CATEGORY + " IS NOT NULL" + " GROUP BY " + COLUMN_CATEGORY, new String[]{monthYear + "%"});
        try {
            if (cursor.moveToFirst()) {
                do {
                    String category = cursor.getString(0);
                    double amount = cursor.getDouble(1);
                    spendingList.add(new CategorySpending(category, amount));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return spendingList;
    }

    public List<MonthlySpending> getMonthlySpendingTrend(int numberOfMonths) {
        List<MonthlySpending> trendList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUBSTR(" + COLUMN_DATE + ", 1, 7) as month, SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_EXPENSES + " GROUP BY month ORDER BY month DESC LIMIT ?", new String[]{String.valueOf(numberOfMonths)});
        try {
            if (cursor.moveToFirst()) {
                do {
                    String monthYear = cursor.getString(0);
                    double amount = cursor.getDouble(1);
                    trendList.add(new MonthlySpending(monthYear, amount));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        java.util.Collections.reverse(trendList);
        return trendList;
    }

    public double getCategorySpendingForMonth(String category, String monthYear) {
        SQLiteDatabase db = this.getReadableDatabase();
        double total = 0;
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_EXPENSES + " WHERE " + COLUMN_CATEGORY + " = ? AND " + COLUMN_DATE + " LIKE ?", new String[]{category, monthYear + "%"});
        try {
            if (cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return total;
    }

    public double getBudgetForCategory(String category, String monthYear) {
        SQLiteDatabase db = this.getReadableDatabase();
        double budgetAmount = 0.0;
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_BUDGETED_AMOUNT + " FROM " + TABLE_CATEGORY_BUDGETS + " WHERE " + COLUMN_CATEGORY_NAME + " = ? AND " + COLUMN_MONTH_YEAR + " = ?", new String[]{category, monthYear});
        try {
            if (cursor.moveToFirst()) {
                budgetAmount = cursor.getDouble(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return budgetAmount;
    }

    // --- Category Budget methods ---
    public void saveCategoryBudgets(List<CategoryBudget> budgets) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            for (CategoryBudget budget : budgets) {
                values.clear();
                values.put(COLUMN_MONTH_YEAR, budget.getMonthYear());
                values.put(COLUMN_CATEGORY_NAME, budget.getCategory());
                values.put(COLUMN_BUDGETED_AMOUNT, budget.getBudgetedAmount());
                db.replace(TABLE_CATEGORY_BUDGETS, null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public List<CategoryBudget> getCategoryBudgetsForMonth(String monthYear) {
        List<CategoryBudget> budgetList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CATEGORY_BUDGETS, null, COLUMN_MONTH_YEAR + " = ?", new String[]{monthYear}, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    CategoryBudget budget = new CategoryBudget();
                    budget.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                    budget.setMonthYear(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MONTH_YEAR)));
                    budget.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_NAME)));
                    budget.setBudgetedAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_BUDGETED_AMOUNT)));
                    budgetList.add(budget);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return budgetList;
    }

    // --- Data Holder Classes ---
    public static class CategorySpending {
        public final String category;
        public final double totalAmount;

        public CategorySpending(String category, double totalAmount) {
            this.category = category;
            this.totalAmount = totalAmount;
        }
    }

    public static class MonthlySpending {
        public final String monthYear;
        public final double totalAmount;

        public MonthlySpending(String monthYear, double totalAmount) {
            this.monthYear = monthYear;
            this.totalAmount = totalAmount;
        }
    }
}
