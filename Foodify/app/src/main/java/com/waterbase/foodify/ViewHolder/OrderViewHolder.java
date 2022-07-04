package com.waterbase.foodify.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.waterbase.foodify.Interface.ItemClickListener;
import com.waterbase.foodify.R;

public class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txtOrderId, txtOrderStatus, txtOrderPhone, txtOrderAddress, txtOrderDate;

    private ItemClickListener itemClickListener;

    public ImageView btn_delete;

    public OrderViewHolder(@NonNull View itemView) {
        super(itemView);

        txtOrderAddress = itemView.findViewById(R.id.order_address);
        txtOrderId = itemView.findViewById(R.id.order_id);
        txtOrderStatus = itemView.findViewById(R.id.order_status);
        txtOrderPhone = itemView.findViewById(R.id.order_phone);
        txtOrderDate = itemView.findViewById(R.id.order_date);
        btn_delete = itemView.findViewById(R.id.btn_delete);

        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
       // itemClickListener.onClick(v, getAdapterPosition(), false);
    }
}
