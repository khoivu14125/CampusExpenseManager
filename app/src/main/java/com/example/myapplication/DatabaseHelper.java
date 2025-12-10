package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import org.mindrot.jbcrypt.BCrypt;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "CampusExpenseManager.db";
    private static final int DATABASE_VERSION = 12;

    private static DatabaseHelper instance;

    // Định nghĩa tên các bảng
    private static final String TABLE_USERS = "users"; // Bảng người dùng
    private static final String TABLE_EXPENSES = "expenses"; // Bảng chi phí
    private static final String TABLE_INCOME = "income"; // Bảng thu nhập chi tiết
    private static final String TABLE_CATEGORY_BUDGETS = "category_budgets"; // Bảng ngân sách theo danh mục
    private static final String TABLE_RECURRING_EXPENSES = "recurring_expenses"; // Bảng chi phí định kỳ
    private static final String TABLE_MONTHLY_INCOME = "monthly_income"; // Bảng thu nhập hàng tháng (cũ/tổng quan)

    // Các cột chung
    private static final String COLUMN_ID = "id";

    // Các cột bảng Users
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_FULL_NAME = "full_name";
    private static final String COLUMN_PIN = "pin";

    // Các cột bảng Expenses/Income
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_DATE = "date"; // Định dạng: "yyyy-MM-dd"

    // Các cột bảng Category Budgets
    private static final String COLUMN_MONTH_YEAR = "month_year"; // Định dạng: "yyyy-MM"
    private static final String COLUMN_CATEGORY_NAME = "category_name";
    private static final String COLUMN_BUDGETED_AMOUNT = "budgeted_amount";

    // Các cột bảng Recurring Expenses
    private static final String COLUMN_START_DATE = "start_date";
    private static final String COLUMN_END_DATE = "end_date";
    private static final String COLUMN_LAST_GENERATED_DATE = "last_generated_date";

    // Các cột bảng Monthly Income
    private static final String COLUMN_INCOME_AMOUNT = "income_amount";

    // Singleton pattern để đảm bảo chỉ có một instance của DatabaseHelper
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
        // Tạo bảng Users
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_EMAIL + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_FULL_NAME + " TEXT,"
                + COLUMN_PIN + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // Tạo bảng Expenses
        String CREATE_EXPENSES_TABLE = "CREATE TABLE " + TABLE_EXPENSES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_AMOUNT + " REAL NOT NULL,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_CATEGORY + " TEXT,"
                + COLUMN_DATE + " TEXT" + ")";
        db.execSQL(CREATE_EXPENSES_TABLE);
        
        // Tạo bảng Income
        String CREATE_INCOME_TABLE = "CREATE TABLE " + TABLE_INCOME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_AMOUNT + " REAL NOT NULL,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_DATE + " TEXT" + ")";
        db.execSQL(CREATE_INCOME_TABLE);

        // Tạo bảng Category Budgets
        String CREATE_CATEGORY_BUDGETS_TABLE = "CREATE TABLE " + TABLE_CATEGORY_BUDGETS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_MONTH_YEAR + " TEXT NOT NULL,"
                + COLUMN_CATEGORY_NAME + " TEXT NOT NULL,"
                + COLUMN_BUDGETED_AMOUNT + " REAL NOT NULL,"
                + "UNIQUE(" + COLUMN_MONTH_YEAR + ", " + COLUMN_CATEGORY_NAME + ") ON CONFLICT REPLACE" + ")";
        db.execSQL(CREATE_CATEGORY_BUDGETS_TABLE);

        // Tạo bảng Recurring Expenses
        String CREATE_RECURRING_EXPENSES_TABLE = "CREATE TABLE " + TABLE_RECURRING_EXPENSES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_AMOUNT + " REAL NOT NULL,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_CATEGORY + " TEXT UNIQUE,"
                + COLUMN_START_DATE + " TEXT NOT NULL,"
                + COLUMN_END_DATE + " TEXT NOT NULL,"
                + COLUMN_LAST_GENERATED_DATE + " TEXT" + ")";
        db.execSQL(CREATE_RECURRING_EXPENSES_TABLE);

        // Tạo bảng Monthly Income
        String CREATE_MONTHLY_INCOME_TABLE = "CREATE TABLE " + TABLE_MONTHLY_INCOME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_MONTH_YEAR + " TEXT UNIQUE,"
                + COLUMN_INCOME_AMOUNT + " REAL NOT NULL" + ")";
        db.execSQL(CREATE_MONTHLY_INCOME_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Xử lý nâng cấp database qua các version
        if (oldVersion < 8) {
            String CREATE_RECURRING_EXPENSES_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_RECURRING_EXPENSES + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_AMOUNT + " REAL NOT NULL,"
                    + COLUMN_DESCRIPTION + " TEXT,"
                    + COLUMN_CATEGORY + " TEXT,"
                    + COLUMN_START_DATE + " TEXT NOT NULL,"
                    + COLUMN_END_DATE + " TEXT NOT NULL,"
                    + COLUMN_LAST_GENERATED_DATE + " TEXT" + ")";
            db.execSQL(CREATE_RECURRING_EXPENSES_TABLE);
        }
        if (oldVersion < 9) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_FULL_NAME + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_PIN + " TEXT");
            } catch (Exception e) {
                // Các cột có thể đã tồn tại
            }
        }
        if (oldVersion < 10) {
            String CREATE_MONTHLY_INCOME_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_MONTHLY_INCOME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_MONTH_YEAR + " TEXT UNIQUE,"
                    + COLUMN_INCOME_AMOUNT + " REAL NOT NULL" + ")";
            db.execSQL(CREATE_MONTHLY_INCOME_TABLE);
        }
        if (oldVersion < 11) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECURRING_EXPENSES);
            String CREATE_RECURRING_EXPENSES_TABLE = "CREATE TABLE " + TABLE_RECURRING_EXPENSES + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_AMOUNT + " REAL NOT NULL,"
                    + COLUMN_DESCRIPTION + " TEXT,"
                    + COLUMN_CATEGORY + " TEXT UNIQUE,"
                    + COLUMN_START_DATE + " TEXT NOT NULL,"
                    + COLUMN_END_DATE + " TEXT NOT NULL,"
                    + COLUMN_LAST_GENERATED_DATE + " TEXT" + ")";
            db.execSQL(CREATE_RECURRING_EXPENSES_TABLE);
        }
         if (oldVersion < 12) {
            String CREATE_INCOME_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_INCOME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_AMOUNT + " REAL NOT NULL,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_DATE + " TEXT" + ")";
            db.execSQL(CREATE_INCOME_TABLE);
        }
    }
    
    // --- Các phương thức quản lý Thu nhập (Income) ---
    public long addIncome(double amount, String description, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_DATE, date);
        return db.insert(TABLE_INCOME, null, values);
    }

    public int updateIncome(int id, double amount, String description, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_DATE, date);
        return db.update(TABLE_INCOME, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void deleteIncome(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_INCOME, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }
    
    // Tính tổng thu nhập cho một tháng cụ thể
    public double getTotalIncomeForMonth(String monthYear) {
        SQLiteDatabase db = this.getReadableDatabase();
        double total = 0;
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_INCOME + " WHERE " + COLUMN_DATE + " LIKE ?", new String[]{monthYear + "%"});
        try {
            if (cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return total;
    }

    // Tìm kiếm thu nhập theo khoảng thời gian
    public List<Income> searchIncomes(String startDate, String endDate) {
        List<Income> incomeList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder selection = new StringBuilder();
        List<String> selectionArgs = new ArrayList<>();

        if (!TextUtils.isEmpty(startDate)) {
            selection.append(COLUMN_DATE + " >= ?");
            selectionArgs.add(startDate);
        }
        if (!TextUtils.isEmpty(endDate)) {
            if (selection.length() > 0) {
                selection.append(" AND ");
            }
            selection.append(COLUMN_DATE + " <= ?");
            selectionArgs.add(endDate);
        }

        Cursor cursor = db.query(TABLE_INCOME, null, selection.length() > 0 ? selection.toString() : null, selectionArgs.toArray(new String[0]), null, null, COLUMN_DATE + " DESC, " + COLUMN_ID + " DESC");

        try {
            if (cursor.moveToFirst()) {
                do {
                    Income income = new Income();
                    income.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                    income.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)));
                    income.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                    income.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)));
                    incomeList.add(income);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return incomeList;
    }

    // --- Các phương thức quản lý Người dùng (User) ---
    public long addUser(String email, String password, String fullName, String pin) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, email);
        // Mật khẩu được mã hóa bằng BCrypt trước khi lưu
        values.put(COLUMN_PASSWORD, BCrypt.hashpw(password, BCrypt.gensalt()));
        values.put(COLUMN_FULL_NAME, fullName);
        values.put(COLUMN_PIN, pin);
        return db.insert(TABLE_USERS, null, values);
    }

    public long addUser(String email, String password) {
        return addUser(email, password, "", "");
    }

    // Kiểm tra đăng nhập
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

    public User getUser(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, COLUMN_EMAIL + " = ?", new String[]{email}, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                User user = new User();
                user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
                user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULL_NAME)));
                user.setPin(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PIN)));
                return user;
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    public boolean updateUserProfile(String currentEmail, String fullName, String pin) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FULL_NAME, fullName);
        values.put(COLUMN_PIN, pin);
        return db.update(TABLE_USERS, values, COLUMN_EMAIL + " = ?", new String[]{currentEmail}) > 0;
    }

    public boolean verifyPin(String email, String pin) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_PIN}, COLUMN_EMAIL + " = ?", new String[]{email}, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                String storedPin = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PIN));
                return pin.equals(storedPin);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return false;
    }

    public boolean updatePassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD, BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        return db.update(TABLE_USERS, values, COLUMN_EMAIL + " = ?", new String[]{email}) > 0;
    }

    // --- Các phương thức quản lý Chi phí (Expense) ---
    public long addExpense(double amount, String description, String category, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_DATE, date);
        return db.insert(TABLE_EXPENSES, null, values);
    }

    // Tìm kiếm chi phí theo nhiều tiêu chí
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
        if (!TextUtils.isEmpty(category) && !"Tất cả".equalsIgnoreCase(category)) {
            if (selection.length() > 0) selection.append(" AND ");
            selection.append(COLUMN_CATEGORY + " = ?");
            selectionArgs.add(category);
        }
        // Sắp xếp theo ngày giảm dần (mới nhất lên đầu)
        Cursor cursor = db.query(TABLE_EXPENSES, null, selection.length() > 0 ? selection.toString() : null, selectionArgs.toArray(new String[0]), null, null, COLUMN_DATE + " DESC, " + COLUMN_ID + " DESC");
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

    // --- Các phương thức cho Dashboard & Báo cáo ---
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

    // Lấy chi tiêu theo từng danh mục trong tháng để vẽ biểu đồ
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

    // --- Các phương thức quản lý Ngân sách (Budget) ---
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

    public void deleteBudgetsForMonth(String monthYear) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CATEGORY_BUDGETS, COLUMN_MONTH_YEAR + " = ?", new String[]{monthYear});
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

    // --- Các phương thức quản lý Chi phí Định kỳ (Recurring Expenses) ---
    public long addRecurringExpense(RecurringExpense recurringExpense) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_AMOUNT, recurringExpense.getAmount());
        values.put(COLUMN_DESCRIPTION, recurringExpense.getDescription());
        values.put(COLUMN_CATEGORY, recurringExpense.getCategory());
        values.put(COLUMN_START_DATE, recurringExpense.getStartDate());
        values.put(COLUMN_END_DATE, recurringExpense.getEndDate());
        values.put(COLUMN_LAST_GENERATED_DATE, recurringExpense.getLastGeneratedDate());
        return db.replace(TABLE_RECURRING_EXPENSES, null, values);
    }

    public List<RecurringExpense> getAllRecurringExpenses() {
        List<RecurringExpense> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_RECURRING_EXPENSES, null, null, null, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    RecurringExpense expense = new RecurringExpense();
                    expense.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                    expense.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)));
                    expense.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                    expense.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
                    expense.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_DATE)));
                    expense.setEndDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_END_DATE)));
                    expense.setLastGeneratedDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_GENERATED_DATE)));
                    list.add(expense);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    public RecurringExpense getRecurringExpenseByCategory(String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_RECURRING_EXPENSES, null, COLUMN_CATEGORY + " = ?", new String[]{category}, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                RecurringExpense expense = new RecurringExpense();
                expense.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                expense.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)));
                expense.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                expense.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
                expense.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_DATE)));
                expense.setEndDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_END_DATE)));
                expense.setLastGeneratedDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_GENERATED_DATE)));
                return expense;
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    public void updateRecurringExpenseLastGeneratedDate(int id, String lastGeneratedDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LAST_GENERATED_DATE, lastGeneratedDate);
        db.update(TABLE_RECURRING_EXPENSES, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void deleteRecurringExpense(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_RECURRING_EXPENSES, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // Tự động xử lý các chi phí định kỳ khi mở ứng dụng
    public void processRecurringExpenses() {
        List<RecurringExpense> recurringExpenses = getAllRecurringExpenses();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (RecurringExpense recurring : recurringExpenses) {
            try {
                Calendar nextGenCal = Calendar.getInstance();
                String lastGenDateStr = recurring.getLastGeneratedDate();

                if (lastGenDateStr == null || lastGenDateStr.isEmpty()) {
                    // Nếu chưa bao giờ tạo, bắt đầu từ ngày bắt đầu của chi phí
                    nextGenCal.setTime(dateFormat.parse(recurring.getStartDate()));
                } else {
                    // Nếu đã tạo, bắt đầu từ tháng tiếp theo
                    nextGenCal.setTime(dateFormat.parse(lastGenDateStr));
                    nextGenCal.add(Calendar.MONTH, 1);
                }

                Calendar endCal = Calendar.getInstance();
                endCal.setTime(dateFormat.parse(recurring.getEndDate()));

                // Lặp qua từng tháng cho đến khi vượt quá ngày kết thúc
                while (!nextGenCal.after(endCal)) {
                    // Kiểm tra xem tháng này có hợp lệ không
                    Calendar startCal = Calendar.getInstance();
                    startCal.setTime(dateFormat.parse(recurring.getStartDate()));
                    if (nextGenCal.get(Calendar.YEAR) > startCal.get(Calendar.YEAR) ||
                            (nextGenCal.get(Calendar.YEAR) == startCal.get(Calendar.YEAR) && nextGenCal.get(Calendar.MONTH) >= startCal.get(Calendar.MONTH))) {

                        String generationDate = dateFormat.format(nextGenCal.getTime());
                        // Thêm chi phí vào bảng Expenses
                        addExpense(recurring.getAmount(), recurring.getDescription() + " (Định kỳ)", recurring.getCategory(), generationDate);
                        // Cập nhật ngày tạo cuối cùng
                        updateRecurringExpenseLastGeneratedDate(recurring.getId(), generationDate);
                    }
                    // Chuyển sang tháng tiếp theo
                    nextGenCal.add(Calendar.MONTH, 1);
                }
            } catch (ParseException e) {
                Log.e("DatabaseHelper", "Lỗi khi xử lý chi phí định kỳ ID: " + recurring.getId(), e);
            }
        }
    }

    // --- Income Methods (Legacy) ---
    public void setMonthlyIncome(String monthYear, double incomeAmount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MONTH_YEAR, monthYear);
        values.put(COLUMN_INCOME_AMOUNT, incomeAmount);
        db.replace(TABLE_MONTHLY_INCOME, null, values);
    }

    public double getMonthlyIncome(String monthYear) {
        SQLiteDatabase db = this.getReadableDatabase();
        double income = 0.0;
        Cursor cursor = db.query(TABLE_MONTHLY_INCOME, new String[]{COLUMN_INCOME_AMOUNT}, COLUMN_MONTH_YEAR + " = ?", new String[]{monthYear}, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                income = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_INCOME_AMOUNT));
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return income;
    }

    // --- Data Holder Classes ---
    public static class User {
        private int id;
        private String email;
        private String fullName;
        private String pin;

        // Getters and Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getPin() { return pin; }
        public void setPin(String pin) { this.pin = pin; }
    }

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
