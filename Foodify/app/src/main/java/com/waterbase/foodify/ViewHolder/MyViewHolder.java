package com.waterbase.foodify.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.waterbase.foodify.R;

public class MyViewHolder extends RecyclerView.ViewHolder {

    public TextView name, quantity, price, discount;
    public ImageView cart_image;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        name = (TextView) itemView.findViewById(R.id.product_name);
        quantity = (TextView) itemView.findViewById(R.id.product_quantity);
        price = (TextView)  itemView.findViewById(R.id.product_price);
        discount = (TextView) itemView.findViewById(R.id.product_discount);
        cart_image = (ImageView) itemView.findViewById(R.id.cart_image);
    }
}
