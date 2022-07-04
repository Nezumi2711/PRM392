package com.waterbase.foodify.ViewHolder;

import android.content.Context;
import android.content.Intent;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.waterbase.foodify.Common.Common;
import com.waterbase.foodify.Database.Database;
import com.waterbase.foodify.FoodDetail;
import com.waterbase.foodify.FoodList;
import com.waterbase.foodify.Interface.ItemClickListener;
import com.waterbase.foodify.Model.Favorites;
import com.waterbase.foodify.Model.Order;
import com.waterbase.foodify.R;

import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesViewHolder> {

    private Context context;
    private List<Favorites> favoritesList;

    public FavoritesAdapter(Context context, List<Favorites> favoritesList) {
        this.context = context;
        this.favoritesList = favoritesList;
    }

    @NonNull
    @Override
    public FavoritesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.favorite_item, parent, false);
        return new FavoritesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoritesViewHolder viewHolder, int position) {
        Favorites model = favoritesList.get(position);

        viewHolder.food_name.setText(model.getFoodName());

        if (Integer.parseInt(model.getFoodDiscount()) > 0) {
            String foodPrice = model.getFoodPrice() + "đ";
            long newFoodPrice = Long.parseLong(model.getFoodPrice()) - Long.parseLong(model.getFoodPrice()) * Long.parseLong(model.getFoodDiscount()) / 100;
            SpannableStringBuilder spnBuilder = new SpannableStringBuilder(foodPrice);
            StrikethroughSpan strikethroughSpan = new StrikethroughSpan();
            spnBuilder.setSpan(strikethroughSpan, 0, foodPrice.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            viewHolder.food_price.setText(spnBuilder);
            viewHolder.newPrice.setText(newFoodPrice + "đ");
            viewHolder.discount.setText("- " + model.getFoodDiscount() + "%");
        } else {
            viewHolder.food_price.setText(String.format("%s đ", model.getFoodPrice()));
            viewHolder.discount.setVisibility(View.GONE);
            viewHolder.newPrice.setVisibility(View.GONE);
        }

        Picasso.with(context).load(model.getFoodImage()).into(viewHolder.food_image);

        //Quick Cart

        viewHolder.quick_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean ifExists = new Database(context).checkFoodExists(model.getFoodId(), Common.currentUser.getPhone());
                if (!ifExists) {
                    new Database(context).addToCart(new Order(
                            Common.currentUser.getPhone(),
                            model.getFoodId(),
                            model.getFoodName(),
                            "1",
                            model.getFoodPrice(),
                            model.getFoodDiscount(),
                            model.getFoodImage()
                    ));
                } else {
                    new Database(context).increaseCart(Common.currentUser.getPhone(), model.getFoodId());
                }

                Toast.makeText(context, "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
            }
        });

        viewHolder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                //Start Activity
                Intent foodDetail = new Intent(context, FoodDetail.class);
                foodDetail.putExtra("FoodId", model.getFoodId()); // Send Food Id to new activity
                context.startActivity(foodDetail);
            }
        });
    }

    @Override
    public int getItemCount() {
        return favoritesList.size();
    }

    public void removeItem(int position)
    {
        favoritesList.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Favorites item, int position)
    {
        favoritesList.add(position, item);
        notifyItemInserted(position);
    }

    public Favorites getItem(int position)
    {
        return favoritesList.get(position);
    }
}
