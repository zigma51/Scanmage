package com.trailblazing.scanmage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.trailblazing.scanmage.model.User;

import java.util.concurrent.TimeUnit;

public class OtpVerificationActivity extends AppCompatActivity {

    private String verificationId;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private OtpEditText otpEditText;
    private EditText nameEditText;
    private EditText emailEditText;
    String username;
    String email;

    private DatabaseReference mDatabase;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);
        progressBar = findViewById(R.id.progress_circular_2);

        otpEditText = findViewById(R.id.et_otp);
        nameEditText = findViewById(R.id.name);
        emailEditText = findViewById(R.id.email);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();


        TextView checkVerificationCode = findViewById(R.id.check_verification_note);

        mAuth = FirebaseAuth.getInstance();

        String phonenumber = getIntent().getStringExtra("phonenumber");
        sendVerificationCode(phonenumber);

        checkVerificationCode.setText("Please type the verification code sent\nto " + phonenumber);


        findViewById(R.id.verify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String otp = otpEditText.getText().toString();
                username = nameEditText.getText().toString();
                email = emailEditText.getText().toString();


                if (otp.length() == 0 || otp.length() < 6) {
                    otpEditText.setError("Enter OTP!");
                    otpEditText.requestFocus();
                    return;
                }
                if (username.isEmpty() || username.length() < 4) {
                    nameEditText.setError("Please Enter your Name");
                    nameEditText.requestFocus();
                    return;
                }
                if (email.isEmpty() || email.length() < 4) {
                    emailEditText.setError("Please Enter your Name");
                    emailEditText.requestFocus();
                    return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    emailEditText.setError("Please enter a Valid Email Address!");
                    emailEditText.requestFocus();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                OtpVerificationActivity.this.verifyCode(otp);
            }
        });

    }


    private void verifyCode(String code) {
        progressBar.setVisibility(View.VISIBLE);
        if (verificationId != null) {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
            signInWithCredential(credential);
        }
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(OtpVerificationActivity.this, "Verified Successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(OtpVerificationActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    OtpVerificationActivity.this.startActivity(intent);
                    if (user != null) {
                        mDatabase.child("users").child(user.getUid()).child("username").setValue(username);
                        System.out.println(user.getUid());
                        mDatabase.child("users").child(user.getUid()).child("email").setValue(email);
                    }
                } else {
                    Toast.makeText(OtpVerificationActivity.this,
                            task.getException().getMessage(),
                            Toast.LENGTH_LONG).show();
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void sendVerificationCode(String number) {
        progressBar.setVisibility(View.VISIBLE);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallBack
        );
    }


    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBack =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onCodeSent(
                        @NonNull String s,
                        @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                    super.onCodeSent(s, forceResendingToken);
                    verificationId = s;
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(OtpVerificationActivity.this, "OTP has been sent", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                    String code = phoneAuthCredential.getSmsCode();
                    if (code != null) {
                        otpEditText.setText(code);
                        verifyCode(code);
                    }
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Toast.makeText(OtpVerificationActivity.this,
                            e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                    progressBar.setVisibility(View.GONE);
                    finish();
                }
            };
}