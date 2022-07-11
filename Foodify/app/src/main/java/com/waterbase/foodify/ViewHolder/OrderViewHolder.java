package com.waterbase.foodify.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.waterbase.foodify.Interface.ItemClickListener;
import com.waterbase.foodify.R;

public class OrderViewHolder extends RecyclerView.ViewHolder {

    public TextView txtOrderId, txtOrderStatus, txtOrderAddress, txtOrderDate, txtOrderPayment;

    public ImageView btn_delete, btn_detail;

    public OrderViewHolder(@NonNull View itemView) {
        super(itemView);

        txtOrderAddress = itemView.findViewById(R.id.order_address);
        txtOrderId = itemView.findViewById(R.id.order_id);
        txtOrderStatus = itemView.findViewById(R.id.order_status);
        txtOrderDate = itemView.findViewById(R.id.order_date);
        txtOrderPayment = itemView.findViewById(R.id.order_payment);
        btn_delete = itemView.findViewById(R.id.btn_delete);
        btn_detail = itemView.findViewById(R.id.btn_detail);
    }
}
