package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

public class ProfileFragment extends Fragment {

    private TextInputEditText fullNameEditText, pinEditText;
    private TextView emailTextView;
    private Button updateProfileButton, changePasswordButton, logoutButton;
    private DatabaseHelper db;
    private String currentUserEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = DatabaseHelper.getInstance(getContext());

        emailTextView = view.findViewById(R.id.emailTextView);
        fullNameEditText = view.findViewById(R.id.fullNameEditText);
        pinEditText = view.findViewById(R.id.pinEditText);
        updateProfileButton = view.findViewById(R.id.updateProfileButton);
        changePasswordButton = view.findViewById(R.id.changePasswordButton);
        logoutButton = view.findViewById(R.id.logoutButton);

        // Get email from SharedPreferences
        SharedPreferences prefs = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        currentUserEmail = prefs.getString("USER_EMAIL", "N/A");
        
        loadUserProfile();

        updateProfileButton.setOnClickListener(v -> updateUserProfile());

        changePasswordButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ResetPasswordActivity.class);
            intent.putExtra("email", currentUserEmail);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {
            // Clear SharedPreferences
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            // Navigate to Login screen
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void loadUserProfile() {
        DatabaseHelper.User user = db.getUser(currentUserEmail);
        if (user != null) {
            emailTextView.setText(user.getEmail());
            fullNameEditText.setText(user.getFullName());
            pinEditText.setText(user.getPin());
        }
    }

    private void updateUserProfile() {
        String fullName = fullNameEditText.getText().toString().trim();
        String pin = pinEditText.getText().toString().trim();

        if (fullName.isEmpty() || pin.isEmpty()) {
            Log.d("ProfileFragment", "Vui lòng nhập đầy đủ thông tin");
            return;
        }

        if (pin.length() != 6) {
            Log.d("ProfileFragment", "Mã PIN phải gồm 6 chữ số");
            return;
        }

        boolean success = db.updateUserProfile(currentUserEmail, fullName, pin);
        if (success) {
            Log.d("ProfileFragment", "Cập nhật thông tin thành công");
        } else {
            Log.d("ProfileFragment", "Cập nhật thất bại");
        }
    }
}
