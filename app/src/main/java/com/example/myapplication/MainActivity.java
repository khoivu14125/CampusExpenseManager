package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private MaterialToolbar topAppBar;
    private BottomNavigationView bottomNavigationView;
    private String userName;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo database helper
        db = DatabaseHelper.getInstance(this);
        // Xử lý các chi phí định kỳ khi mở ứng dụng (tự động thêm nếu đến ngày)
        db.processRecurringExpenses();

        topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Lấy tên người dùng từ SharedPreferences để hiển thị lời chào
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userEmail = prefs.getString("USER_EMAIL", "");
        this.userName = "Người dùng"; // Tên mặc định
        if (userEmail != null && !userEmail.isEmpty()) {
            String[] parts = userEmail.split("@");
            if (parts.length > 0 && !parts[0].isEmpty()) {
                String namePart = parts[0];
                this.userName = namePart.substring(0, 1).toUpperCase() + namePart.substring(1);
            }
        }

        // Xử lý sự kiện khi người dùng chọn các mục trên thanh điều hướng dưới cùng
        bottomNavigationView.setOnItemSelectedListener(item -> {
            updateFragment(item.getItemId());
            return true;
        });

        // Đặt fragment mặc định là Dashboard khi mở app lần đầu
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
        } else {
            // Nếu activity được tạo lại (ví dụ xoay màn hình), cập nhật lại tiêu đề
            updateTitle(bottomNavigationView.getSelectedItemId());
        }
    }

    // Chuyển đổi Fragment dựa trên item được chọn trên menu
    private void updateFragment(int itemId) {
        if (itemId == R.id.nav_dashboard) {
            replaceFragment(new HomeFragment()); // Màn hình chính
        } else if (itemId == R.id.nav_expense) {
            replaceFragment(new ExpenseFragment()); // Màn hình chi phí & thu nhập
        } else if (itemId == R.id.nav_recurring) {
            replaceFragment(new RecurringExpenseFragment()); // Màn hình chi phí định kỳ
        } else if (itemId == R.id.nav_budgets) {
            replaceFragment(new BudgetsFragment()); // Màn hình ngân sách
        } else if (itemId == R.id.nav_profile) {
            replaceFragment(new ProfileFragment()); // Màn hình hồ sơ người dùng
        }
        updateTitle(itemId);
    }

    // Cập nhật tiêu đề trên Toolbar dựa trên màn hình hiện tại
    private void updateTitle(int itemId) {
        if (itemId == R.id.nav_dashboard) {
            setGreeting(this.userName);
        } else if (itemId == R.id.nav_expense) {
            getSupportActionBar().setTitle("Chi Tiêu");
        } else if (itemId == R.id.nav_recurring) {
            getSupportActionBar().setTitle("Định Kỳ");
        } else if (itemId == R.id.nav_budgets) {
            getSupportActionBar().setTitle("Ngân Sách");
        } else if (itemId == R.id.nav_profile) {
            getSupportActionBar().setTitle("Hồ Sơ");
        }
    }

    // Hiển thị lời chào theo thời gian trong ngày (Sáng, Chiều, Tối)
    private void setGreeting(String userName) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        String greeting;
        if (hour >= 6 && hour < 12) {
            greeting = "Chào buổi sáng, " + userName;
        } else if (hour >= 12 && hour < 18) {
            greeting = "Chào buổi chiều, " + userName;
        } else {
            greeting = "Chào buổi tối, " + userName;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(greeting);
        }
    }

    // Hàm hỗ trợ thay thế Fragment trong container
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}
