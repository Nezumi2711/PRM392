package com.waterbase.foodify.ViewHolder;

import android.content.Context;
import android.graphics.Color;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.squareup.picasso.Picasso;
import com.waterbase.foodify.Cart;
import com.waterbase.foodify.Common.Common;
import com.waterbase.foodify.Database.Database;
import com.waterbase.foodify.Interface.ItemClickListener;
import com.waterbase.foodify.Model.Order;
import com.waterbase.foodify.R;

import org.w3c.dom.Text;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class CartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {

    public TextView txt_card_item, txt_price;
    public ElegantNumberButton btn_quantity;
    public ImageView cart_image;

    private ItemClickListener itemClickListener;

    public void setTxt_card_item(TextView txt_card_item) {
        this.txt_card_item = txt_card_item;
    }

    public CartViewHolder(View itemView) {
        super(itemView);
        txt_card_item = (TextView) itemView.findViewById(R.id.cart_item_name);
        txt_price = (TextView) itemView.findViewById(R.id.cart_item_Price);
        btn_quantity = (ElegantNumberButton) itemView.findViewById(R.id.btn_quantity);
        cart_image = (ImageView) itemView.findViewById(R.id.cart_image);

        itemView.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        contextMenu.setHeaderTitle("Tuỳ chọn:");
        contextMenu.add(0, 0, getAdapterPosition(), Common.DELETE);
    }
}

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
                Order order = listData.get(position);
                order.setQuantity(String.valueOf(newValue));
                new Database(cart).updateCart(order);

                //Calculate total price
                float total = 0;
                List<Order> orders = new Database(cart).getCarts();
                for (Order item : orders)
                    total += (Float.parseFloat(order.getPrice())) * (Float.parseFloat(item.getQuantity()));
                Locale locale = new Locale("vi", "VN");
                NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
                float price = newValue*(Float.parseFloat(order.getPrice()));
                holder.txt_price.setText(fmt.format(price));

                cart.txtTotalPrice.setText(fmt.format(total));
            }
        });

        Locale locale = new Locale("vi", "VN");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
        float price = (Float.parseFloat(listData.get(position).getPrice()))*(Float.parseFloat(listData.get(position).getQuantity()));
        holder.txt_price.setText(fmt.format(price));

        holder.txt_card_item.setText(listData.get(position).getProductName());
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }
}
