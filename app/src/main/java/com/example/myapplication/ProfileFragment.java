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

        // Ánh xạ view
        emailTextView = view.findViewById(R.id.emailTextView);
        fullNameEditText = view.findViewById(R.id.fullNameEditText);
        pinEditText = view.findViewById(R.id.pinEditText);
        updateProfileButton = view.findViewById(R.id.updateProfileButton);
        changePasswordButton = view.findViewById(R.id.changePasswordButton);
        logoutButton = view.findViewById(R.id.logoutButton);

        // Lấy email người dùng hiện tại từ SharedPreferences
        SharedPreferences prefs = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        currentUserEmail = prefs.getString("USER_EMAIL", "N/A");
        
        // Tải thông tin người dùng từ DB
        loadUserProfile();

        // Sự kiện cập nhật thông tin cá nhân
        updateProfileButton.setOnClickListener(v -> updateUserProfile());

        // Sự kiện chuyển sang màn hình đổi mật khẩu
        changePasswordButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ResetPasswordActivity.class);
            intent.putExtra("email", currentUserEmail); // Truyền email qua intent
            startActivity(intent);
        });

        // Sự kiện đăng xuất
        logoutButton.setOnClickListener(v -> {
            // Xóa dữ liệu phiên đăng nhập trong SharedPreferences
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            // Quay về màn hình Đăng nhập và xóa hết các màn hình trước đó
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    // Tải và hiển thị thông tin người dùng (Email, Tên, PIN)
    private void loadUserProfile() {
        DatabaseHelper.User user = db.getUser(currentUserEmail);
        if (user != null) {
            emailTextView.setText(user.getEmail());
            fullNameEditText.setText(user.getFullName());
            pinEditText.setText(user.getPin());
        }
    }

    // Cập nhật thông tin người dùng trong DB
    private void updateUserProfile() {
        String fullName = fullNameEditText.getText().toString().trim();
        String pin = pinEditText.getText().toString().trim();

        // Kiểm tra nhập liệu
        if (fullName.isEmpty() || pin.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pin.length() != 6) {
            Toast.makeText(getContext(), "Mã PIN phải gồm 6 chữ số", Toast.LENGTH_SHORT).show();
            return;
        }

        // Thực hiện cập nhật
        boolean success = db.updateUserProfile(currentUserEmail, fullName, pin);
        if (success) {
            Toast.makeText(getContext(), "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
        }
    }
}
