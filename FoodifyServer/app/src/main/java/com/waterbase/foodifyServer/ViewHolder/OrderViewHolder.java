package com.waterbase.foodifyServer.ViewHolder;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.waterbase.foodifyServer.R;

public class OrderViewHolder extends RecyclerView.ViewHolder{

    public TextView txtOrderId, txtOrderStatus, txtOrderPhone, txtOrderAddress, txtOrderDate, txtOrderPayment;

    public Button btnEdit, btnRemove, btnDetail, btnDirection;

    public OrderViewHolder(@NonNull View itemView) {
        super(itemView);

        txtOrderAddress = itemView.findViewById(R.id.order_address);
        txtOrderId = itemView.findViewById(R.id.order_id);
        txtOrderStatus = itemView.findViewById(R.id.order_status);
        txtOrderPhone = itemView.findViewById(R.id.order_phone);
        txtOrderDate = itemView.findViewById(R.id.order_date);
        txtOrderPayment = itemView.findViewById(R.id.order_payment);

        btnEdit = itemView.findViewById(R.id.btnEdit);
        btnDetail = itemView.findViewById(R.id.btnDetail);
        btnDirection = itemView.findViewById(R.id.btnDirection);
        btnRemove = itemView.findViewById(R.id.btnRemove);


    }
}
