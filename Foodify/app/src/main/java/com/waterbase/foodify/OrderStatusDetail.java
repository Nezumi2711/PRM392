package com.waterbase.foodify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.waterbase.foodify.Model.Request;
import com.waterbase.foodify.ViewHolder.OrderDetailAdapter;
import com.waterbase.foodify.ViewHolder.OrderStatusDetailAdapter;

public class OrderStatusDetail extends AppCompatActivity {

    TextView order_address, order_total, order_comment;
    Request request;
    RecyclerView lstFoods;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status_detail);

        String comment = "";

        setTitle("Thông tin chi tiết đơn hàng");

        order_address = findViewById(R.id.order_address);
        order_total = findViewById(R.id.order_total);
        order_comment = findViewById(R.id.order_comment);

        lstFoods = findViewById(R.id.lstFoods);
        lstFoods.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        lstFoods.setLayoutManager(layoutManager);

        //Firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

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

        OrderStatusDetailAdapter adapter = new OrderStatusDetailAdapter(request.getFoods(), this);
        adapter.notifyDataSetChanged();
        lstFoods.setAdapter(adapter);

        // calling the action bar
        ActionBar actionBar = getSupportActionBar();

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