package com.example.esarthi;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText etEmail, etNewPassword, etConfirmPassword;
    Button btnUpdatePassword;
    TextView txtBackLogin;
    ProgressDialog progressDialog;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);

        etEmail = findViewById(R.id.etEmail);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnUpdatePassword = findViewById(R.id.btnUpdatePassword);
        txtBackLogin = findViewById(R.id.txtBackLogin);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating Password...");
        progressDialog.setCancelable(false);

        preferences = getSharedPreferences("ESarthi", MODE_PRIVATE);
        editor = preferences.edit();

        btnUpdatePassword.setOnClickListener(v -> {

            String email = etEmail.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (email.isEmpty()) {
                etEmail.setError("Enter Email");
                return;
            }

            if (newPassword.isEmpty()) {
                etNewPassword.setError("Enter New Password");
                return;
            }

            if (confirmPassword.isEmpty()) {
                etConfirmPassword.setError("Confirm Password");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(ForgotPasswordActivity.this, "Passwords do not match",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            String savedEmail = preferences.getString("email", "");

            if (!email.equals(savedEmail)) {
                Toast.makeText(ForgotPasswordActivity.this, "Email not found",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            progressDialog.show();

            editor.putString("password", newPassword);
            editor.apply();

            progressDialog.dismiss();

            Toast.makeText(ForgotPasswordActivity.this,
                    "Password Updated Successfully",
                    Toast.LENGTH_SHORT).show();

            startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
            finish();

        });

        txtBackLogin.setOnClickListener(v -> {
            startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
            finish();
        });
    }
}