package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    private TextView userEmailTextView;
    private Button changePasswordButton, logoutButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userEmailTextView = view.findViewById(R.id.userEmailTextView);
        changePasswordButton = view.findViewById(R.id.changePasswordButton);
        logoutButton = view.findViewById(R.id.logoutButton);

        // Get email from SharedPreferences
        SharedPreferences prefs = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String email = prefs.getString("USER_EMAIL", "N/A");
        userEmailTextView.setText(email);

        changePasswordButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ResetPasswordActivity.class);
            intent.putExtra("email", email);
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
}
