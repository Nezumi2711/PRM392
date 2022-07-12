package com.waterbase.foodify;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.waterbase.foodify.Common.Common;
import com.waterbase.foodify.Database.Database;
import com.waterbase.foodify.Model.DataMessage;
import com.waterbase.foodify.Model.MyResponse;
import com.waterbase.foodify.Model.Request;
import com.waterbase.foodify.Model.Token;
import com.waterbase.foodify.Remote.APIService;
import com.waterbase.foodify.ViewHolder.OrderDetailAdapter;
import com.waterbase.foodify.Zalopay.Api.CreateOrder;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class OrderDetail extends AppCompatActivity {

    TextView order_address, order_total, order_comment;
    Request request;
    RecyclerView lstFoods;
    RecyclerView.LayoutManager layoutManager;
    MaterialButton btnPay, btnZaloPay;

    FirebaseDatabase database;
    DatabaseReference requests;

    APIService mService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_order_detail);

        String comment = "";

        setTitle("Thông tin chi tiết đơn hàng");

        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // ZaloPay SDK Init
        ZaloPaySDK.init(2554, Environment.SANDBOX);

        order_address = (TextView) findViewById(R.id.order_address);
        order_total = (TextView) findViewById(R.id.order_total);
        order_comment = (TextView) findViewById(R.id.order_comment);

        btnPay = findViewById(R.id.btnPay);
        btnZaloPay = findViewById(R.id.btnZaloPay);

        lstFoods = findViewById(R.id.lstFoods);
        lstFoods.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        lstFoods.setLayoutManager(layoutManager);

        //Firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        mService = Common.getFCMService();

        if (getIntent() != null)
            request = (Request) getIntent().getSerializableExtra("requests");

        //Set value
        order_total.setText("Tổng cộng: " + request.getTotal());
        order_address.setText("Địa chỉ giao: " + request.getAddress());
        if (TextUtils.isEmpty(request.getComment()))
            comment = "Không có";
        else
            comment = request.getComment();
        order_comment.setText("Ghi chú: " + comment);

        OrderDetailAdapter adapter = new OrderDetailAdapter(request.getFoods(), this);
        adapter.notifyDataSetChanged();
        lstFoods.setAdapter(adapter);

        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Summit to Firebase
                //We will using System.CurrentMilli to key
                String order_number = String.valueOf(System.currentTimeMillis());
                requests.child(order_number).setValue(request);

                sendNotificationOrder(order_number);

                //Delete Cart
                new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());
                Toast.makeText(OrderDetail.this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(OrderDetail.this, Home.class));
                finish();
            }
        });

        btnZaloPay.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                String order_number = String.valueOf(System.currentTimeMillis());
                String totalPrice = request.getTotal().substring(0, request.getTotal().length() - 2).replace(".", ""); //Remove character " đ"

                CreateOrder orderApi = new CreateOrder();

                try {
                    JSONObject data = orderApi.createOrder(totalPrice, order_number);
                    String code = data.getString("return_code");

                    if (code.equals("1")) {
                        String token = data.getString("zp_trans_token");
                        ZaloPaySDK.getInstance().payOrder(OrderDetail.this, token, "demozpdk://app", new PayOrderListener() {
                            @Override
                            public void onPaymentSucceeded(final String transactionId, final String transToken, final String appTransID) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        request.setPaymentStatus("1");
                                        //Summit to Firebase
                                        //We will using System.CurrentMilli to key
                                        requests.child(order_number).setValue(request);

                                        sendNotificationOrder(order_number);

                                        //Delete Cart
                                        new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());
                                        Toast.makeText(OrderDetail.this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(OrderDetail.this, Home.class));
                                        finish();
                                    }

                                });
                            }

                            @Override
                            public void onPaymentCanceled(String zpTransToken, String appTransID) {
                                Toast.makeText(OrderDetail.this, "Bạn đã huỷ thanh toán!", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onPaymentError(ZaloPayError zaloPayError, String zpTransToken, String appTransID) {
                                Toast.makeText(OrderDetail.this, "Đã xảy ra lỗi hệ thống! Xin vui lòng thử lại sau!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                } catch (Exception e) {
                    Log.e("ERROR", "" + e.getMessage());
                }
            }
        });

        // calling the action bar
        ActionBar actionBar = getSupportActionBar();

        // showing the back button in action bar
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void sendNotificationOrder(String order_number) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query data = tokens.orderByChild("serverToken").equalTo(true); //Get all node with isServerToken is true;
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {

                    Token serverToken = postSnapShot.getValue(Token.class);

                    //Create raw payload to send
                    Map<String, String> dataSend = new HashMap<>();
                    dataSend.put("title", "Foodify");
                    dataSend.put("message", "Bạn có 1 đơn hàng mới #" + order_number);
                    DataMessage dataMessage = new DataMessage(serverToken.getToken(), dataSend);

                    mService.sendNotification(dataMessage)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                    //Only run when get result
                                    if (response.code() == 200) {
                                        if (response.body().success == 1) {
                                            new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());
                                            Toast.makeText(OrderDetail.this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            Toast.makeText(OrderDetail.this, "Hệ thống bị lỗi. Vui lòng thử lại sau!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    Log.e("ERROR", t.getMessage());
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);
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