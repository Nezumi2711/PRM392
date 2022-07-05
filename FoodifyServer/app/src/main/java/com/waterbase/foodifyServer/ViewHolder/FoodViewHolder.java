package com.waterbase.foodifyServer.ViewHolder;

import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.waterbase.foodifyServer.Common.Common;
import com.waterbase.foodifyServer.R;

public class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
    public TextView txtFoodName;
    public ImageView imageView;

    public FoodViewHolder(View itemView) {
        super(itemView);

        txtFoodName = (TextView) itemView.findViewById(R.id.food_name);
        imageView = (ImageView) itemView.findViewById(R.id.food_image);

        itemView.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("Tuỳ chọn:");

        menu.add(0,0, getAdapterPosition(), Common.UPDATE);
        menu.add(0,1, getAdapterPosition(), Common.DELETE);
    }
}