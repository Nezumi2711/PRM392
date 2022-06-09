package com.waterbase.foodify;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.waterbase.foodify.Common.Common;
import com.waterbase.foodify.Model.User;

public class SignUp extends AppCompatActivity {

    EditText edtPhone, edtName, edtPassword;
    Button btnSignUp;
    TextView txtAppName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        edtName = (EditText) findViewById(R.id.txtFullName);
        edtPassword = (EditText) findViewById(R.id.edtPassword);
        edtPhone = (EditText) findViewById(R.id.edtPhone);
        btnSignUp = (Button) findViewById(R.id.btnSignUp);

        txtAppName = (TextView) findViewById(R.id.txtAppName);

        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/NABILA.TTF");
        txtAppName.setTypeface(face);

        //Init Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(edtName.getText().toString()) && !TextUtils.isEmpty(edtPassword.getText().toString())
                        && !TextUtils.isEmpty(edtPhone.getText().toString()))    {
                    if (Common.isConnectedToInternet(getBaseContext())) {
                        final ProgressDialog mDialog = new ProgressDialog(SignUp.this);
                        mDialog.setMessage("Vui lòng chờ...");
                        mDialog.show();

                        table_user.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //Check if already user phone
                                if (dataSnapshot.child(edtPhone.getText().toString()).exists()) {
                                    mDialog.dismiss();
                                    Toast.makeText(SignUp.this, "Số điện thoại đã được đăng ký!", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    mDialog.dismiss();
                                    User user = new User(edtName.getText().toString(), edtPassword.getText().toString());
                                    Intent intent = new Intent(SignUp.this, VerifyPhone.class);
                                    intent.putExtra("user", user);
                                    intent.putExtra("phone", edtPhone.getText().toString());
                                    startActivity(intent);
//                                    User user = new User(edtName.getText().toString(), edtPassword.getText().toString());
//                                    table_user.child(edtPhone.getText().toString()).setValue(user);
//                                    Toast.makeText(SignUp.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
//                                    finish();
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