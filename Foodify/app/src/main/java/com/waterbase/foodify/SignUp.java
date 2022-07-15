package com.waterbase.foodify;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
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

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class SignUp extends AppCompatActivity {

    EditText edtPhone, edtName, edtPassword, edtPasswordVerify;
    Button btnSignUp;
    TextView txtAppName;

    final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!_])(?=\\S+$).{4,}$";
    final String PHONE_PATTERN = "^0[98753]{1}\\d{8}$";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_up);

        edtName = (EditText) findViewById(R.id.txtFullName);
        edtPassword = (EditText) findViewById(R.id.edtPassword);
        edtPasswordVerify = (EditText) findViewById(R.id.edtPasswordVerify);
        edtPhone = (EditText) findViewById(R.id.edtPhone);
        btnSignUp = (Button) findViewById(R.id.btnSignUp);

        final ProgressBar progressBar = findViewById(R.id.progressBar);

        txtAppName = (TextView) findViewById(R.id.txtAppName);


        //Init Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(edtName.getText().toString()) && !TextUtils.isEmpty(edtPassword.getText().toString())
                        && !TextUtils.isEmpty(edtPhone.getText().toString())) {

                    //Verify password
                    Pattern patternPassword = Pattern.compile(PASSWORD_PATTERN);
                    Matcher matcherPassword = patternPassword.matcher(edtPasswordVerify.getText().toString());

                    if (matcherPassword.matches() && edtPasswordVerify.getText().toString().length() >= 8) {

                        //Check same password
                        if (edtPassword.getText().toString().equals(edtPasswordVerify.getText().toString())) {

                            //Validate Phone Number
                            Pattern patternPhone = Pattern.compile(PHONE_PATTERN);
                            Matcher matcherPhone = patternPhone.matcher(edtPhone.getText().toString());
                            if (matcherPhone.find()) {

                                //Check Internet connection
                                if (Common.isConnectedToInternet(getBaseContext())) {
                                    progressBar.setVisibility(View.VISIBLE);
                                    btnSignUp.setVisibility(View.GONE);
                                    table_user.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            //Check if already user phone
                                            if (dataSnapshot.child(edtPhone.getText().toString()).exists()) {
                                                progressBar.setVisibility(View.GONE);
                                                btnSignUp.setVisibility(View.VISIBLE);
                                                Toast.makeText(SignUp.this, "Số điện thoại đã được đăng ký!", Toast.LENGTH_SHORT).show();
                                                finish();
                                            } else {
                                                PhoneAuthOptions options =
                                                        PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                                                                .setPhoneNumber("+84" + edtPhone.getText().toString())       // Phone number to verify
                                                                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                                                .setActivity(SignUp.this)                 // Activity (for callback binding)
                                                                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                                                    @Override
                                                                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                                                                        progressBar.setVisibility(View.GONE);
                                                                        btnSignUp.setVisibility(View.VISIBLE);
                                                                    }

                                                                    @Override
                                                                    public void onVerificationFailed(FirebaseException e) {
                                                                        progressBar.setVisibility(View.GONE);
                                                                        btnSignUp.setVisibility(View.VISIBLE);
                                                                        Toast.makeText(SignUp.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                    }

                                                                    @Override
                                                                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                                                        progressBar.setVisibility(View.GONE);
                                                                        btnSignUp.setVisibility(View.VISIBLE);
                                                                        User user = new User(edtName.getText().toString(), Common.encryptPassword(edtPassword.getText().toString()));
                                                                        Intent intent = new Intent(SignUp.this, VerifyPhone.class);
                                                                        intent.putExtra("user", user);
                                                                        intent.putExtra("phone", edtPhone.getText().toString());
                                                                        intent.putExtra("verificationId", verificationId);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                })          // OnVerificationStateChangedCallbacks
                                                                .build();
                                                PhoneAuthProvider.verifyPhoneNumber(options);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                        }
                                    });
                                } else {
                                    Toast.makeText(SignUp.this, "Vui lòng kiểm tra kết nối mạng!", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            } else {
                                edtPhone.setError("Số điện thoại không đúng định dạng. Vui lòng kiểm tra lại!");
                            }
                        } else {
                            edtPasswordVerify.setError("Mật khẩu không giống nhau! Vui lòng kiểm tra lại");
                        }
                    } else {
                        Toast.makeText(SignUp.this, "Mật khẩu của bạn cần tối thiểu có 8 ký tự, 1 ký tự viết hoa, 1 số và 1 ký tự đặc biệt!", Toast.LENGTH_LONG).show();
                    }

                } else {
                    Toast.makeText(SignUp.this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

    }

    public void login(View view) {
        startActivity(new Intent(SignUp.this, SignIn.class));
    }
}