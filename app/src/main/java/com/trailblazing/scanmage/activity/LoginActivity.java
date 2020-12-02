package com.trailblazing.scanmage.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.trailblazing.scanmage.R;

public class LoginActivity extends AppCompatActivity {

    ProgressBar loginProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText phoneEditText = findViewById(R.id.phone);
        loginProgressBar = findViewById(R.id.login_progress_bar);

        findViewById(R.id.send_otp).setOnClickListener(v -> {
            loginProgressBar.setVisibility(View.VISIBLE);
            String number = "+91" + phoneEditText.getText().toString().trim();

            if (number.length() < 10) {
                phoneEditText.setError("Please provide valid number!");
                phoneEditText.requestFocus();
            }

            Intent intent = new Intent(LoginActivity.this, OtpVerificationActivity.class);
            intent.putExtra("phonenumber", number);
            loginProgressBar.setVisibility(View.GONE);
            LoginActivity.this.startActivity(intent);
        });
    }

    protected void onStart() {
        super.onStart();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}