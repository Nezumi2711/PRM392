package com.waterbase.foodify;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.waterbase.foodify.Model.User;

import java.util.concurrent.TimeUnit;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class VerifyPhone extends AppCompatActivity {

    private EditText inputCode1, inputCode2, inputCode3, inputCode4, inputCode5, inputCode6;
    User user = null;
    String verificationId, phone, isForgotPassword = null;
    TextView resendCode, countdown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_verify_phone);

        user = (User) getIntent().getSerializableExtra("user");
        phone = getIntent().getStringExtra("phone");
        isForgotPassword = getIntent().getStringExtra("isForgotPassword");

        TextView textView = findViewById(R.id.txtPhone);
        textView.setText("Mã xác thực đã gửi đến số " + phone + ". Vui lòng nhập mã OTP khi nhận được tin nhắn!");

        inputCode1 = findViewById(R.id.inputCode1);
        inputCode2 = findViewById(R.id.inputCode2);
        inputCode3 = findViewById(R.id.inputCode3);
        inputCode4 = findViewById(R.id.inputCode4);
        inputCode5 = findViewById(R.id.inputCode5);
        inputCode6 = findViewById(R.id.inputCode6);

        resendCode = findViewById(R.id.resendCode);
        countdown = findViewById(R.id.countdown);

        resendCode.setVisibility(View.GONE);
        countdown.setVisibility(View.VISIBLE);

        CountDownTimer count = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                countdown.setText(millisUntilFinished / 1000 + " giây");
            }

            @Override
            public void onFinish() {
                resendCode.setVisibility(View.VISIBLE);
                countdown.setVisibility(View.GONE);
            }
        };
        count.start();

        resendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendCode.setVisibility(View.GONE);
                countdown.setVisibility(View.VISIBLE);

                PhoneAuthProvider.verifyPhoneNumber(
                        PhoneAuthOptions
                                .newBuilder(FirebaseAuth.getInstance())
                                .setActivity(VerifyPhone.this)
                                .setPhoneNumber("+84" + phone)
                                .setTimeout(60L, TimeUnit.SECONDS)
                                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                    @Override
                                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                                    }

                                    @Override
                                    public void onVerificationFailed(FirebaseException e) {
                                        Toast.makeText(VerifyPhone.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onCodeSent(String newVerificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                        verificationId = newVerificationId;
                                        Toast.makeText(VerifyPhone.this, "Mã OTP đã được gửi!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .build());

                count.start();
            }
        });

        setupOTPInputs();

        //Init Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        final ProgressBar progressBar = findViewById(R.id.progressBar);
        final Button buttonVerify = findViewById(R.id.btnVerify);

        verificationId = getIntent().getStringExtra("verificationId");

        buttonVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(inputCode1.getText().toString().trim().isEmpty()
                    || inputCode2.getText().toString().trim().isEmpty()
                    || inputCode3.getText().toString().trim().isEmpty()
                    || inputCode4.getText().toString().trim().isEmpty()
                    || inputCode5.getText().toString().trim().isEmpty()
                    || inputCode6.getText().toString().trim().isEmpty())    {
                    Toast.makeText(VerifyPhone.this, "Mã code không đúng định dạng!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String code =
                        inputCode1.getText().toString() +
                                inputCode2.getText().toString() +
                                inputCode3.getText().toString() +
                                inputCode4.getText().toString() +
                                inputCode5.getText().toString() +
                                inputCode6.getText().toString();
                if(verificationId != null) {
                    progressBar.setVisibility(View.VISIBLE);
                    buttonVerify.setVisibility(View.GONE);
                    PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(
                            verificationId,
                            code
                    );
                    FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressBar.setVisibility(View.GONE);
                                    buttonVerify.setVisibility(View.VISIBLE);
                                    if(task.isSuccessful()) {

                                        table_user.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(isForgotPassword == null){

                                                    //Create new User
                                                    table_user.child(getIntent().getStringExtra("phone")).setValue(user);
                                                    Intent intent = new Intent(getApplicationContext(), SignIn.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                                    Toast.makeText(VerifyPhone.this, "Tạo tài khoản thành công! Vui lòng đăng nhập lại để tiếp tục", Toast.LENGTH_SHORT).show();
                                                    startActivity(intent);
                                                    finish();
                                                } else {

                                                    //Change pass user
                                                    Intent intent = new Intent(VerifyPhone.this, ChangePassword.class);
                                                    intent.putExtra("user", user);
                                                    intent.putExtra("phone", phone);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    } else {
                                        Toast.makeText(VerifyPhone.this, "Mã code không đúng. Vui lòng kiểm tra lại!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

    }

    private void setupOTPInputs() {
        inputCode1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().trim().isEmpty()){
                    inputCode2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputCode2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().trim().isEmpty()){
                    inputCode3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputCode3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().trim().isEmpty()){
                    inputCode4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputCode4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().trim().isEmpty()){
                    inputCode5.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputCode5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().trim().isEmpty()){
                    inputCode6.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}