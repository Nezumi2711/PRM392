package com.waterbase.foodify;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.waterbase.foodify.Model.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class ChangePassword extends AppCompatActivity {

    User user;
    EditText edtPassword, edtPasswordVerify;
    String phone;
    Button btnChangePass;
    final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set font all activity
        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/font.otf")
                                .setFontAttrId(io.github.inflationx.calligraphy3.R.attr.fontPath)
                                .build()))
                .build());

        setContentView(R.layout.activity_change_password);

        edtPassword = findViewById(R.id.edtPassword);
        edtPasswordVerify = findViewById(R.id.edtPasswordVerify);
        btnChangePass = findViewById(R.id.btnChangePass);

        user = (User) getIntent().getSerializableExtra("user");
        phone = getIntent().getStringExtra("phone");

        //Init Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        btnChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Verify password
                Pattern patternPassword = Pattern.compile(PASSWORD_PATTERN);
                Matcher matcherPassword = patternPassword.matcher(edtPasswordVerify.getText().toString());

                if (matcherPassword.matches() && edtPasswordVerify.getText().toString().length() >= 8) {

                    //Check same password
                    if (edtPassword.getText().toString().equals(edtPasswordVerify.getText().toString())) {
                        table_user.child(phone).child("password").setValue(edtPasswordVerify.getText().toString());
                        Toast.makeText(ChangePassword.this, "Đổi mật khẩu thành công. Vui lòng đăng nhập lại để tiếp tục!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ChangePassword.this, SignIn.class));
                        finish();
                    } else {
                        edtPasswordVerify.setError("Mật khẩu không giống nhau. Vui lòng kiểm tra lại!");
                    }
                } else {
                    Toast.makeText(ChangePassword.this, "Mật khẩu của bạn cần tối thiểu có 8 ký tự, 1 ký tự viết hoa, 1 số và 1 ký tự đặc biệt!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}