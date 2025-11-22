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

        db = DatabaseHelper.getInstance(this);
        rootView = findViewById(android.R.id.content);
        
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.textViewRegister);
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword);

        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Snackbar.make(rootView, "Vui lòng nhập đầy đủ thông tin", Snackbar.LENGTH_LONG).show();
                return;
            }

            boolean isAuthenticated = db.checkUser(email, password);
            if (isAuthenticated) {
                // Save email to SharedPreferences
                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("USER_EMAIL", email);
                editor.apply();

                Snackbar.make(rootView, "Đăng nhập thành công", Snackbar.LENGTH_SHORT)
                        .addCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }).show();
                // For faster transition if snackbar delay is not desired, uncomment below and remove callback
                // Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                // startActivity(intent);
                // finish();
            } else {
                Snackbar.make(rootView, "Email hoặc mật khẩu không chính xác", Snackbar.LENGTH_LONG).show();
            }
        });

        textViewRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        textViewForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }
}
