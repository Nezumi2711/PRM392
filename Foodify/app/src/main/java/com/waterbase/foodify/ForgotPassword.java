package com.waterbase.foodify;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.waterbase.foodify.Common.Common;
import com.waterbase.foodify.Model.User;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForgotPassword extends AppCompatActivity {

    EditText edtPhone;
    ProgressBar progressBar;
    Button btnSendOTP;

    final String PHONE_PATTERN = "^0[98753]{1}\\d{8}$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        btnSendOTP = findViewById(R.id.btnSendOTP);
        progressBar = findViewById(R.id.progressBar);
        edtPhone = findViewById(R.id.edtPhone);

        //Init Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        btnSendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Validate Phone Number
                Pattern patternPhone = Pattern.compile(PHONE_PATTERN);
                Matcher matcherPhone = patternPhone.matcher(edtPhone.getText().toString());
                if (matcherPhone.find()) {
                    if (!TextUtils.isEmpty(edtPhone.getText().toString())) {
                        //Check Internet
                        if (Common.isConnectedToInternet(getBaseContext())) {
                            progressBar.setVisibility(View.VISIBLE);
                            btnSendOTP.setVisibility(View.GONE);

                            //Get data from database
                            table_user.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    //Check phone in database
                                    if (dataSnapshot.child(edtPhone.getText().toString()).exists()) {
                                        progressBar.setVisibility(View.GONE);
                                        btnSendOTP.setVisibility(View.VISIBLE);

                                        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                                "+84" + edtPhone.getText().toString(),
                                                60,
                                                TimeUnit.SECONDS,
                                                ForgotPassword.this,
                                                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                                    @Override
                                                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                                                        progressBar.setVisibility(View.GONE);
                                                        btnSendOTP.setVisibility(View.VISIBLE);
                                                    }

                                                    @Override
                                                    public void onVerificationFailed(FirebaseException e) {
                                                        progressBar.setVisibility(View.GONE);
                                                        btnSendOTP.setVisibility(View.VISIBLE);
                                                        Toast.makeText(ForgotPassword.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }

                                                    @Override
                                                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                                        progressBar.setVisibility(View.GONE);
                                                        btnSendOTP.setVisibility(View.VISIBLE);
                                                        Toast.makeText(ForgotPassword.this, "Mã OTP đã được gửi!", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(ForgotPassword.this, VerifyPhone.class);
                                                        intent.putExtra("phone", edtPhone.getText().toString());
                                                        intent.putExtra("verificationId", verificationId);
                                                        intent.putExtra("isForgotPassword", "true");
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                }
                                        );
                                    } else {
                                        progressBar.setVisibility(View.GONE);
                                        btnSendOTP.setVisibility(View.VISIBLE);
                                        Toast.makeText(ForgotPassword.this, "Số điện thoại này chưa được đăng ký. Vui lòng kiểm tra lại!", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        } else {
                            Toast.makeText(ForgotPassword.this, "Vui lòng kiểm tra kết nối mạng!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        Toast.makeText(ForgotPassword.this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    edtPhone.setError("Số điện thoại không đúng định dạng. Vui lòng kiểm tra lại");
                }
            }
        });
    }
}