package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class ResetPasswordActivity extends AppCompatActivity {

    private TextInputEditText editTextNewPassword, editTextConfirmPassword;
    private Button buttonResetPassword;
    private DatabaseHelper db;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        db = DatabaseHelper.getInstance(this);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonResetPassword = findViewById(R.id.buttonResetPassword);

        email = getIntent().getStringExtra("email");

        buttonResetPassword.setOnClickListener(v -> {
            String newPassword = editTextNewPassword.getText().toString().trim();
            String confirmPassword = editTextConfirmPassword.getText().toString().trim();

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(getApplicationContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isUpdated = db.updatePassword(email, newPassword);
            if (isUpdated) {
                Toast.makeText(getApplicationContext(), "Password reset successful", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Password reset failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
