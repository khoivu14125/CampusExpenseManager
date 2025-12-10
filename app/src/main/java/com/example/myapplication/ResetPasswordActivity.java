package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

public class ResetPasswordActivity extends AppCompatActivity {

    private TextInputEditText editTextNewPassword, editTextConfirmPassword;
    private Button buttonResetPassword;
    private DatabaseHelper db;
    private String email;
    private View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        db = DatabaseHelper.getInstance(this);
        rootView = findViewById(android.R.id.content);
        
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonResetPassword = findViewById(R.id.buttonResetPassword);

        // Nhận email từ Intent được truyền tới
        email = getIntent().getStringExtra("email");

        // Xử lý sự kiện khi nhấn nút Đặt lại mật khẩu
        buttonResetPassword.setOnClickListener(v -> {
            String newPassword = editTextNewPassword.getText().toString().trim();
            String confirmPassword = editTextConfirmPassword.getText().toString().trim();

            // Kiểm tra nhập liệu
            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Snackbar.make(rootView, "Vui lòng nhập đầy đủ thông tin", Snackbar.LENGTH_LONG).show();
                return;
            }

            // Kiểm tra mật khẩu nhập lại có khớp không
            if (!newPassword.equals(confirmPassword)) {
                Snackbar.make(rootView, "Mật khẩu không khớp", Snackbar.LENGTH_LONG).show();
                return;
            }

            // Cập nhật mật khẩu mới vào DB
            boolean isUpdated = db.updatePassword(email, newPassword);
            if (isUpdated) {
                Snackbar.make(rootView, "Đặt lại mật khẩu thành công", Snackbar.LENGTH_SHORT)
                        .addCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                finish(); // Đóng màn hình sau khi thành công
                            }
                        }).show();
            } else {
                Snackbar.make(rootView, "Đặt lại mật khẩu thất bại", Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
