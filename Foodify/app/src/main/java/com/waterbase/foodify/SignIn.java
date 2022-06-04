package com.waterbase.foodify;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.waterbase.foodify.Common.Common;
import com.waterbase.foodify.Model.User;

public class SignIn extends AppCompatActivity {

    EditText edtPhone, edtPassword;
    Button btnSignIn;
    TextView txtAppName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        edtPassword = (EditText) findViewById(R.id.edtPassword);
        edtPhone = (EditText) findViewById(R.id.edtPhone);
        btnSignIn = (Button) findViewById(R.id.btnSignIn);

        txtAppName = (TextView) findViewById(R.id.txtAppName);

        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/NABILA.TTF");
        txtAppName.setTypeface(face);

        //Init Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ProgressDialog mDialog = new ProgressDialog(SignIn.this);
                mDialog.setMessage("Please waiting...");
                mDialog.show();

                table_user.addListenerForSingleValueEvent(new ValueEventListener() {


                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Check phone and password is null or not
                        if(!edtPhone.getText().toString().isEmpty() && !edtPassword.getText().toString().isEmpty()){
                            //Check if user not in database
                            if(dataSnapshot.child(edtPhone.getText().toString()).exists()){
                                //Get User information
                                mDialog.dismiss();
                                User user = dataSnapshot.child(edtPhone.getText().toString()).getValue(User.class);
                                user.setPhone(edtPhone.getText().toString());
                                if(user.getPassword().equals(edtPassword.getText().toString())){
                                    Intent homeIntent = new Intent(SignIn.this, Home.class);
                                    Common.currentUser  = user;
                                    startActivity(homeIntent);
                                    finish();
                                } else {
                                    Toast.makeText(SignIn.this, "Số điện thoại hoặc mật khẩu không đúng. Xin vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                mDialog.dismiss();
                                Toast.makeText(SignIn.this, "Số điện thoại chưa được đăng ký. Vui lòng đăng ký để sử dụng!", Toast.LENGTH_SHORT).show();
                            }
                        } else{
                            mDialog.dismiss();
                            Toast.makeText(SignIn.this, "Số điện thoại và mật khẩu không được để trống. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    public void register(View view) {
        startActivity(new Intent(SignIn.this, com.waterbase.foodify.SignUp.class));
    }
}