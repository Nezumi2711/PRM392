package com.waterbase.foodify.ViewHolder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.squareup.picasso.Picasso;
import com.waterbase.foodify.Cart;
import com.waterbase.foodify.Model.Order;
import com.waterbase.foodify.OrderDetail;
import com.waterbase.foodify.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

class MyViewHolder extends RecyclerView.ViewHolder {

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

public class OrderDetailAdapter extends RecyclerView.Adapter<MyViewHolder>{

    List<Order> myOrders;
    private OrderDetail orderDetail;

    public OrderDetailAdapter(List<Order> myOrders, OrderDetail orderDetail) {
        this.myOrders = myOrders;
        this.orderDetail = orderDetail;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_detail_layout, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        double totalPrice;
        Order order = myOrders.get(position);

        Picasso.with(orderDetail.getBaseContext())
                .load(myOrders.get(position).getImage())
                .resize(70,70)
                .centerCrop()
                .into(holder.cart_image);

        totalPrice = Double.parseDouble(order.getQuantity()) * Double.parseDouble(order.getPrice());
        Locale locale = new Locale("vi", "VN");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);


        holder.name.setText(String.format("Món ăn: %s", order.getProductName()));
        holder.quantity.setText(String.format("Số lượng: %s", order.getQuantity()));
        holder.price.setText("Giá: " + fmt.format(totalPrice));
        holder.discount.setText(String.format("Giảm giá: %s", order.getDiscount()));

    }

    @Override
    public int getItemCount() {
        return myOrders.size();
    }
}
