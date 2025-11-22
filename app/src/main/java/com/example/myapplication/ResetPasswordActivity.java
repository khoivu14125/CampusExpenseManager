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

        email = getIntent().getStringExtra("email");

        buttonResetPassword.setOnClickListener(v -> {
            String newPassword = editTextNewPassword.getText().toString().trim();
            String confirmPassword = editTextConfirmPassword.getText().toString().trim();

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Snackbar.make(rootView, "Vui lòng nhập đầy đủ thông tin", Snackbar.LENGTH_LONG).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Snackbar.make(rootView, "Mật khẩu không khớp", Snackbar.LENGTH_LONG).show();
                return;
            }

            boolean isUpdated = db.updatePassword(email, newPassword);
            if (isUpdated) {
                Snackbar.make(rootView, "Đặt lại mật khẩu thành công", Snackbar.LENGTH_SHORT)
                        .addCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                finish();
                            }
                        }).show();
            } else {
                Snackbar.make(rootView, "Đặt lại mật khẩu thất bại", Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
