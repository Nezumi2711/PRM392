package com.waterbase.foodify;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.waterbase.foodify.Common.Common;
import com.waterbase.foodify.Model.Request;
import com.waterbase.foodify.ViewHolder.OrderViewHolder;

public class OrderStatus extends AppCompatActivity {

    public RecyclerView recyclerView;
    public RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;

    FirebaseDatabase database;
    DatabaseReference requests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_order_status);

        //Firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");
        
        recyclerView = (RecyclerView) findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        if(getIntent() == null)
            loadOrders(Common.currentUser.getPhone());
        else
            loadOrders(getIntent().getStringExtra("userPhone"));
        
        loadOrders(Common.currentUser.getPhone());

        // calling the action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(0xFFFF5353));
        actionBar.setTitle(Html.fromHtml("<font color='#ffffff'>Tình trạng đơn hàng</font>"));

        // showing the back button in action bar
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void loadOrders(String phone) {

        Query getOrderByUser = requests.orderByChild("phone")
                .equalTo(phone);

        FirebaseRecyclerOptions<Request> orderOptions = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(getOrderByUser, Request.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(orderOptions) {
            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder orderViewHolder, int i, @NonNull Request model) {
                orderViewHolder.txtOrderId.setText("Id đơn hàng: #" + adapter.getRef(i).getKey());
                orderViewHolder.txtOrderStatus.setText("Tình trạng đơn: " + Common.convertCodeToStatus(model.getStatus()));
                orderViewHolder.txtOrderAddress.setText("Địa chỉ: " + model.getAddress());
                orderViewHolder.txtOrderDate.setText("Ngày đặt: " + Common.getDate(Long.parseLong(adapter.getRef(i).getKey())));
                orderViewHolder.txtOrderPayment.setText("Tình trạng thanh toán: " + Common.convertCodePaymentToStatus(model.getPaymentStatus()));
                orderViewHolder.txtOrderCount.setText("Số lượng món ăn: " + model.getFoods().size());
                orderViewHolder.txtOrderPrice.setText("Tổng cộng: " + model.getTotal());

                orderViewHolder.btn_detail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Pass object to another activity!
                        Intent intent = new Intent(OrderStatus.this, OrderStatusDetail.class);
                        intent.putExtra("requests", model);
                        startActivity(intent);
                    }
                });


                orderViewHolder.btn_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(adapter.getItem(orderViewHolder.getAdapterPosition()).getStatus().equals("0"))
                            deleteOrder(adapter.getRef(orderViewHolder.getAdapterPosition()).getKey());
                        else
                            Toast.makeText(OrderStatus.this, "Bạn không thể xoá đơn này!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.order_layout, parent, false);
                return new OrderViewHolder(itemView);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    private void deleteOrder(String key) {
        AlertDialog alertDialog = new AlertDialog.Builder(OrderStatus.this).setTitle("Xác nhận").setMessage("Bạn có muốn xoá đơn #" + key).setPositiveButton("Đồng ý",null)
                        .setNegativeButton("Thoát", null)
                        .create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button agree = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                agree.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requests.child(key)
                                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(OrderStatus.this, new StringBuilder("Đơn hàng ").append(key).append(" đã xoá!").toString(), Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener((e) -> {
                                    Toast.makeText(OrderStatus.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                        alertDialog.dismiss();
                    }
                });
            }
        });

        alertDialog.show();

    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
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

    @Override
    protected void onResume() {
        super.onResume();
        adapter.startListening();
    }
}