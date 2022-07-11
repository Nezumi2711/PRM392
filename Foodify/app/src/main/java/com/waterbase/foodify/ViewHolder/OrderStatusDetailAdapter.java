package com.waterbase.foodify.ViewHolder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.waterbase.foodify.Model.Order;
import com.waterbase.foodify.OrderStatusDetail;
import com.waterbase.foodify.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderStatusDetailAdapter extends RecyclerView.Adapter<MyViewHolder>{

    List<Order> myOrders;
    private OrderStatusDetail orderStatusDetail;

    public OrderStatusDetailAdapter(List<Order> myOrders, OrderStatusDetail orderStatusDetail) {
        this.myOrders = myOrders;
        this.orderStatusDetail = orderStatusDetail;
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

        Picasso.with(orderStatusDetail.getBaseContext())
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
