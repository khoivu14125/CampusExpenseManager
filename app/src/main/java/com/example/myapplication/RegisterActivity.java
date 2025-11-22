package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText editTextFullName, editTextEmail, editTextPassword, editTextPin;
    private Button buttonRegister;
    private TextView textViewLogin;
    private DatabaseHelper db;
    private View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = DatabaseHelper.getInstance(this);
        rootView = findViewById(android.R.id.content);
        
        editTextFullName = findViewById(R.id.editTextFullName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextPin = findViewById(R.id.editTextPin);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLogin = findViewById(R.id.textViewLogin);

        buttonRegister.setOnClickListener(v -> {
            String fullName = editTextFullName.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            String pin = editTextPin.getText().toString().trim();

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || pin.isEmpty()) {
                Snackbar.make(rootView, "Vui lòng nhập đầy đủ thông tin", Snackbar.LENGTH_LONG).show();
                return;
            }
            
            if (pin.length() != 6) {
                Snackbar.make(rootView, "Mã PIN phải gồm 6 chữ số", Snackbar.LENGTH_LONG).show();
                return;
            }

            long result = db.addUser(email, password, fullName, pin);
            if (result != -1) {
                Snackbar.make(rootView, "Đăng ký thành công", Snackbar.LENGTH_SHORT)
                        .addCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                finish();
                            }
                        }).show();
            } else {
                Snackbar.make(rootView, "Đăng ký thất bại (Email có thể đã tồn tại)", Snackbar.LENGTH_LONG).show();
            }
        });

        textViewLogin.setOnClickListener(v -> {
            finish();
        });
    }
}
