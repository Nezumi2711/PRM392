package com.waterbase.foodify.ViewHolder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.squareup.picasso.Picasso;
import com.waterbase.foodify.Cart;
import com.waterbase.foodify.Database.Database;
import com.waterbase.foodify.Model.Order;
import com.waterbase.foodify.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartViewHolder>{

    private List<Order> listData = new ArrayList<>();
    private Cart cart;

    public CartAdapter(List<Order> listData, Cart cart) {
        this.listData = listData;
        this.cart = cart;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(cart);
        View itemView = inflater.inflate(R.layout.cart_layout, parent, false);
        return new CartViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, final int position) {

        Picasso.with(cart.getBaseContext())
                        .load(listData.get(position).getImage())
                        .resize(70,70)
                        .centerCrop()
                        .into(holder.cart_image);

        holder.btn_quantity.setNumber(listData.get(position).getQuantity());
        holder.btn_quantity.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
            @Override
            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                Order order = listData.get(holder.getAdapterPosition());
                order.setQuantity(String.valueOf(newValue));
                new Database(cart).updateCart(order);

                //Calculate total price
                float total = 0;
                List<Order> orders = new Database(cart).getCarts();
                for (Order item : orders)
                    total += (Float.parseFloat(item.getPrice())) * (Float.parseFloat(item.getQuantity())) * (100 - Long.parseLong(item.getDiscount()))/100;
                Locale locale = new Locale("vi", "VN");
                NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
                float price = newValue*(Float.parseFloat(order.getPrice())) * (100 - Long.parseLong(order.getDiscount()))/100;
                holder.txt_price.setText(fmt.format(price));

                cart.txtTotalPrice.setText(fmt.format(total));
            }
        });

        Locale locale = new Locale("vi", "VN");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
        float price = (Float.parseFloat(listData.get(position).getPrice()))*(Float.parseFloat(listData.get(position).getQuantity())) * (100 - Long.parseLong(listData.get(position).getDiscount()))/100;
        holder.txt_price.setText(fmt.format(price));
        if(Integer.parseInt(listData.get(position).getDiscount()) > 0)
        {
            holder.txtDiscount.setVisibility(View.VISIBLE);
            holder.txtDiscount.setText("-" + listData.get(position).getDiscount() + "%");
        }

        holder.txt_card_item.setText(listData.get(position).getProductName());
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public Order getItem(int position)
    {
        return listData.get(position);
    }

    public void removeItem(int position)
    {
        listData.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Order item, int position)
    {
        listData.add(position, item);
        notifyItemInserted(position);
    }
}
