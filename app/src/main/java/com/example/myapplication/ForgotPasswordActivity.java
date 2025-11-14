package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText editTextEmail;
    private Button buttonVerify;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        db = DatabaseHelper.getInstance(this);
        editTextEmail = findViewById(R.id.editTextEmail);
        buttonVerify = findViewById(R.id.buttonVerify);

        buttonVerify.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (db.checkUser(email)) {
                Intent intent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Email not found", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
