package com.waterbase.foodifyServer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.waterbase.foodifyServer.Common.Common;
import com.waterbase.foodifyServer.Interface.ItemClickListener;
import com.waterbase.foodifyServer.Model.MyResponse;
import com.waterbase.foodifyServer.Model.Notification;
import com.waterbase.foodifyServer.Model.Request;
import com.waterbase.foodifyServer.Model.Sender;
import com.waterbase.foodifyServer.Model.Token;
import com.waterbase.foodifyServer.Remote.APIService;
import com.waterbase.foodifyServer.ViewHolder.OrderViewHolder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderStatus extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;

    FirebaseDatabase db;
    DatabaseReference requests;

    MaterialSpinner spinner;

    APIService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);
    
        //Firebase
        db = FirebaseDatabase.getInstance();
        requests = db.getReference("Requests");

        //Init Service
        mService = Common.getFCMClient();

        //Init
        recyclerView = (RecyclerView) findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        
        loadOrders();
    }

    private void loadOrders() {
        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(
                Request.class,
                R.layout.order_layout,
                OrderViewHolder.class,
                requests
        ) {
            @Override
            protected void populateViewHolder(OrderViewHolder viewHolder, Request model, int i) {
                viewHolder.txtOrderId.setText(adapter.getRef(i).getKey());
                viewHolder.txtOrderStatus.setText(Common.coverCodeToStatus(model.getStatus()));
                viewHolder.txtOrderAddress.setText(model.getAddress());
                viewHolder.txtOrderPhone.setText(model.getPhone());

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        if(!isLongClick){
                            Intent trackingOrder = new Intent(OrderStatus.this, TrackingOrder.class);
                            Common.currentRequest = model;
                            startActivity(trackingOrder);
                        }
//                        else {
//                            Intent orderDetail = new Intent(OrderStatus.this, OrderDetail.class);
//                            Common.currentRequest = model;
//                            orderDetail.putExtra("OrderId", adapter.getRef(position).getKey());
//                            startActivity(orderDetail);
//                        }
                    }
                });
            }
        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if(item.getTitle().equals(Common.UPDATE))
            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()));
        else if(item.getTitle().equals(Common.DELETE))
            deleteOrder(adapter.getRef(item.getOrder()).getKey());

        return super.onContextItemSelected(item);
    }

    private void deleteOrder(String key) {
        requests.child(key).removeValue();
    }

    private void showUpdateDialog(String key, Request item) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(OrderStatus.this);
        alertDialog.setTitle("Cập nhật tình trạng đơn hàng");
        alertDialog.setMessage("Vui lòng chọn tình trạng đơn:");

        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.update_order_layout, null);

        spinner = (MaterialSpinner) view.findViewById(R.id.statusSpinner);
        spinner.setItems("Placed", "On my way", "Shipped");

        alertDialog.setView(view);

        final String localKey = key;
        alertDialog.setPositiveButton("Cập nhật", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();;
                item.setStatus(String.valueOf(spinner.getSelectedIndex()));

                requests.child(localKey).setValue(item);
                
                sendOrderStatusToUser(localKey, item);
            }
        });
        alertDialog.setNegativeButton("Huỷ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();
    }

    private void sendOrderStatusToUser(String localKey, Request item) {
        DatabaseReference tokens = db.getReference("Tokens");
        tokens.orderByKey().equalTo(item.getPhone())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot postSnapShot: dataSnapshot.getChildren()) {
                            Token token = postSnapShot.getValue(Token.class);

                            //Make raw payload
                            Notification notification = new Notification("Foodify", "Đơn của bạn " + localKey + " đã được cập nhật!");
                            Sender content = new Sender(token.getToken(), notification);

                            mService.sendNotification(content)
                                    .enqueue(new Callback<MyResponse>() {
                                        @Override
                                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                            if(response.body().success == 1) {
                                                Toast.makeText(OrderStatus.this, "Đơn hàng của bạn đã được cập nhật!", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(OrderStatus.this, "Đơn hàng đã được cập nhật nhưng chưa gửi thông báo!", Toast.LENGTH_SHORT).show();
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
}