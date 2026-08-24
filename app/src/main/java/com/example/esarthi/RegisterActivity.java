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

public class RegisterActivity extends AppCompatActivity {

    EditText etFullName, etUsername, etEmail, etMobile, etPassword, etConfirmPassword;
    Button btnRegister;
    TextView txtLogin;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etMobile = findViewById(R.id.etMobile);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);
        txtLogin = findViewById(R.id.txtLogin);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating Account...");
        progressDialog.setCancelable(false);

        txtLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        btnRegister.setOnClickListener(v -> {

            String fullname = etFullName.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String mobile = etMobile.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (fullname.isEmpty()) {
                etFullName.setError("Enter Full Name");
                return;
            }

            if (username.isEmpty()) {
                etUsername.setError("Enter Username");
                return;
            }

            if (email.isEmpty()) {
                etEmail.setError("Enter Email");
                return;
            }

            if (mobile.isEmpty()) {
                etMobile.setError("Enter Mobile Number");
                return;
            }

            if (password.isEmpty()) {
                etPassword.setError("Enter Password");
                return;
            }

            if (confirmPassword.isEmpty()) {
                etConfirmPassword.setError("Confirm Password");
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(RegisterActivity.this, "Passwords do not match",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            progressDialog.show();

            SharedPreferences sp = getSharedPreferences("ESarthi", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();

            editor.putString("fullname", fullname);
            editor.putString("username", username);
            editor.putString("email", email);
            editor.putString("mobile", mobile);
            editor.putString("password", password);

            editor.apply();

            progressDialog.dismiss();

            Toast.makeText(RegisterActivity.this, "Registration Successfully Done",
                    Toast.LENGTH_SHORT).show();

            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();

        });
    }
}