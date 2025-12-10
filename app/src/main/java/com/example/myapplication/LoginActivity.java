package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegister, textViewForgotPassword;
    private DatabaseHelper db;
    private View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo DatabaseHelper để kiểm tra thông tin đăng nhập
        db = DatabaseHelper.getInstance(this);
        rootView = findViewById(android.R.id.content);
        
        // Ánh xạ các view
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.textViewRegister);
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword);

        // Xử lý sự kiện khi nhấn nút Đăng nhập
        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            // Kiểm tra đầu vào
            if (email.isEmpty() || password.isEmpty()) {
                Snackbar.make(rootView, "Vui lòng nhập đầy đủ thông tin", Snackbar.LENGTH_LONG).show();
                return;
            }

            // Kiểm tra thông tin đăng nhập trong cơ sở dữ liệu
            boolean isAuthenticated = db.checkUser(email, password);
            if (isAuthenticated) {
                // Lưu email người dùng vào SharedPreferences để duy trì phiên đăng nhập hoặc lấy thông tin
                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("USER_EMAIL", email);
                editor.apply();

                // Thông báo thành công và chuyển sang màn hình chính
                Snackbar.make(rootView, "Đăng nhập thành công", Snackbar.LENGTH_SHORT)
                        .addCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish(); // Đóng màn hình đăng nhập
                            }
                        }).show();
            } else {
                Snackbar.make(rootView, "Email hoặc mật khẩu không chính xác", Snackbar.LENGTH_LONG).show();
            }
        });

        // Chuyển sang màn hình Đăng ký
        textViewRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Chuyển sang màn hình Quên mật khẩu
        textViewForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }
}
