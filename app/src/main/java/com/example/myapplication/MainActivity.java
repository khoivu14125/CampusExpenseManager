package com.example.myapplication;

import android.content.Intent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Get user name from SharedPreferences safely
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userEmail = prefs.getString("USER_EMAIL", "");
        this.userName = "User"; // Default name
        if (userEmail != null && !userEmail.isEmpty()) {
            String[] parts = userEmail.split("@");
            if (parts.length > 0 && !parts[0].isEmpty()) {
                String namePart = parts[0];
                this.userName = namePart.substring(0, 1).toUpperCase() + namePart.substring(1);
            }
        }

        // Handle bottom navigation item selection
        bottomNavigationView.setOnItemSelectedListener(item -> {
            updateFragment(item.getItemId());
            return true;
        });

        // Set the default selected item on initial creation, or update title on recreation
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
        } else {
            // On recreation, the fragment is restored, so just update the title
            updateTitle(bottomNavigationView.getSelectedItemId());
        }
    }

    private void updateFragment(int itemId) {
        if (itemId == R.id.nav_dashboard) {
            replaceFragment(new HomeFragment());
        } else if (itemId == R.id.nav_expense) {
            replaceFragment(new ExpenseFragment());
        } else if (itemId == R.id.nav_budgets) {
            replaceFragment(new BudgetsFragment());
        } else if (itemId == R.id.nav_profile) {
            replaceFragment(new ProfileFragment());
        }
        updateTitle(itemId);
    }

    private void updateTitle(int itemId) {
        if (itemId == R.id.nav_dashboard) {
            setGreeting(this.userName);
        } else if (itemId == R.id.nav_expense) {
            getSupportActionBar().setTitle("Expense");
        } else if (itemId == R.id.nav_budgets) {
            getSupportActionBar().setTitle("Budgets");
        } else if (itemId == R.id.nav_profile) {
            getSupportActionBar().setTitle("Profile");
        }
    }

    private void setGreeting(String userName) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        String greeting;
        if (hour >= 6 && hour < 12) {
            greeting = "Good Morning, " + userName;
        } else if (hour >= 12 && hour < 18) {
            greeting = "Good Afternoon, " + userName;
        } else {
            greeting = "Good Evening, " + userName;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(greeting);
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}
