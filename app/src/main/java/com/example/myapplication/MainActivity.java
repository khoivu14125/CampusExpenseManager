package com.example.myapplication;

import android.content.Intent;
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

        // Get user name and store it
        Intent intent = getIntent();
        String userEmail = intent.getStringExtra("USER_EMAIL");
        this.userName = "User"; // Default name
        if (userEmail != null && !userEmail.isEmpty()) {
            this.userName = userEmail.split("@")[0];
            this.userName = this.userName.substring(0, 1).toUpperCase() + this.userName.substring(1);
        }

        // Handle bottom navigation item selection
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                replaceFragment(new HomeFragment());
                setGreeting(userName); // Set greeting for Home
                return true;
            } else if (itemId == R.id.nav_expense) {
                replaceFragment(new ExpenseFragment());
                getSupportActionBar().setTitle("Expense"); // Set static title
                return true;
            } else if (itemId == R.id.nav_budgets) {
                replaceFragment(new BudgetsFragment());
                getSupportActionBar().setTitle("Budgets"); // Set static title
                return true;
            } else if (itemId == R.id.nav_profile) {
                replaceFragment(new ProfileFragment());
                getSupportActionBar().setTitle("Profile"); // Set static title
                return true;
            }
            return false;
        });

        // Set the default selected item and title
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
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
