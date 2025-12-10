package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText editTextEmail, editTextPin;
    private Button buttonVerify;
    private DatabaseHelper db;
    private View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        db = DatabaseHelper.getInstance(this);
        rootView = findViewById(android.R.id.content);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPin = findViewById(R.id.editTextPin);
        buttonVerify = findViewById(R.id.buttonVerify);

        // Xử lý sự kiện khi nhấn nút Xác nhận
        buttonVerify.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String pin = editTextPin.getText().toString().trim();

            // Kiểm tra nhập liệu
            if (email.isEmpty() || pin.isEmpty()) {
                Snackbar.make(rootView, "Vui lòng nhập email và mã PIN", Snackbar.LENGTH_LONG).show();
                return;
            }

            // Kiểm tra xem email có tồn tại trong hệ thống không
            if (db.checkUser(email)) {
                // Kiểm tra xem mã PIN có khớp với email không
                if (db.verifyPin(email, pin)) {
                    // Nếu đúng, chuyển sang màn hình Đặt lại mật khẩu
                    Intent intent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
                    intent.putExtra("email", email); // Truyền email sang màn hình tiếp theo
                    startActivity(intent);
                    finish();
                } else {
                    Snackbar.make(rootView, "Mã PIN không chính xác", Snackbar.LENGTH_LONG).show();
                }
            } else {
                Snackbar.make(rootView, "Email không tồn tại", Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
