package com.waterbase.foodify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.waterbase.foodify.Common.Common;
import com.waterbase.foodify.Model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InformationUser extends AppCompatActivity {

    TextView edtFullName, edtPhone, edtHomeAddress, edtOldPassword, edtPassword, edtNewPassword;
    Button btnSave;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information_user);

        final ProgressBar progressBar = findViewById(R.id.progressBar);

        edtFullName = findViewById(R.id.edtFullName);
        edtPhone = findViewById(R.id.edtPhone);
        edtHomeAddress = findViewById(R.id.edtHomeAddress);
        edtOldPassword = findViewById(R.id.edtOldPassword);
        edtPassword = findViewById(R.id.edtPassword);
        edtNewPassword = findViewById(R.id.edtPasswordVerify);

        btnSave = findViewById(R.id.btnSave);

        //Load data
        edtFullName.setText(Common.currentUser.getName());
        edtPhone.setText(Common.currentUser.getPhone());
        edtHomeAddress.setText(Common.currentUser.getHomeAddress());

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean verifyName = false, verifyHomeAddress = false, verifyPassword = false;

                progressBar.setVisibility(View.VISIBLE);
                btnSave.setVisibility(View.GONE);

                User user = new User();

                //Validate Name
                if (!TextUtils.isEmpty(edtFullName.getText().toString())) {
                    if (edtFullName.getText().toString().length() >= 8) {
                        user.setName(edtFullName.getText().toString());
                        verifyName = true;
                    } else {
                        edtFullName.setError("Vui lòng điền trường này với ít nhất là 8 ký tự!");
                    }
                } else {
                    edtFullName.setError("Vui lòng không bỏ trống trường này!");
                }

                user.setPhone(edtPhone.getText().toString());

                //Validate Home Address
                if (!TextUtils.isEmpty(edtHomeAddress.getText().toString())) {
                    if (edtHomeAddress.getText().toString().length() >= 12) {
                        user.setHomeAddress(edtHomeAddress.getText().toString());
                        verifyHomeAddress = true;
                    } else {
                        edtHomeAddress.setError("Vui lòng điền trường này với ít nhất là 12 ký tự!");
                    }
                } else {
                    verifyHomeAddress = true;
                }


                //Validate Password
                if (!TextUtils.isEmpty(edtOldPassword.getText().toString()) && !TextUtils.isEmpty(edtPassword.getText().toString()) && !TextUtils.isEmpty(edtNewPassword.getText().toString())) {
                    //Verify password
                    Pattern patternPassword = Pattern.compile(Common.PASSWORD_PATTERN);
                    Matcher matcherPassword = patternPassword.matcher(edtNewPassword.getText().toString());

                    if (matcherPassword.matches() && edtNewPassword.getText().toString().length() >= 8) {
                        //Check old password
                        if (Common.encryptPassword(edtOldPassword.getText().toString()).equals(Common.currentUser.getPassword())) {

                            //Check new password and repeat password
                            if (edtPassword.getText().toString().equals(edtNewPassword.getText().toString())) {
                                user.setPassword(Common.encryptPassword(edtNewPassword.getText().toString()));
                                verifyPassword = true;
                            } else {
                                edtNewPassword.setError("Mật khẩu chưa khớp! Vui lòng thử lại!");
                            }
                        } else {
                            edtOldPassword.setError("Sai mật khẩu, vui lòng thử lại!");
                        }
                    } else {
                        Toast.makeText(InformationUser.this, "Mật khẩu của bạn cần tối thiểu có 8 ký tự, 1 ký tự viết hoa, 1 số và 1 ký tự đặc biệt!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    user.setPassword(Common.currentUser.getPassword());
                    verifyPassword = true;
                }

                if (verifyName && verifyPassword && verifyHomeAddress) {
                    Common.currentUser = user;
                    FirebaseDatabase.getInstance().getReference("User")
                            .child(Common.currentUser.getPhone())
                            .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    progressBar.setVisibility(View.GONE);
                                    btnSave.setVisibility(View.VISIBLE);
                                    Toast.makeText(InformationUser.this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setVisibility(View.VISIBLE);
                }
            }
        });


        // calling the action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(0xFFFF5353));
        actionBar.setTitle(Html.fromHtml("<font color='#ffffff'>Giỏ hàng</font>"));

        // showing the back button in action bar
        actionBar.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}