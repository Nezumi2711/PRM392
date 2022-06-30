package com.waterbase.foodifyServer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import com.waterbase.foodifyServer.Model.Banner;
import com.waterbase.foodifyServer.Model.Food;
import com.waterbase.foodifyServer.ViewHolder.BannerViewHolder;

import java.util.ArrayList;
import java.util.List;

import info.hoang8f.widget.FButton;

public class BannerActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    RelativeLayout rootLayout;

    FloatingActionButton fab;

    //Firebase
    FirebaseDatabase db;
    DatabaseReference banners;
    FirebaseStorage storage;
    StorageReference storageReference;

    FirebaseRecyclerAdapter<Banner, BannerViewHolder> adapter;

    //Add new Banner
    MaterialEditText edtName, edtFoodId;
    FButton btnUpload, btnSelect;

    Banner newBanner;
    Uri filePath;
    
    List<Food> foodList =  new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner);

        //Init firebase
        db = FirebaseDatabase.getInstance();
        banners = db.getReference("Banner");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        recyclerView = findViewById(R.id.recycler_banner);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        rootLayout = findViewById(R.id.rootLayout);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener((view) -> { showAddBanner(); });

        loadListBanner();
    }

    private void loadListBanner() {
        FirebaseRecyclerOptions<Banner> allBanner = new FirebaseRecyclerOptions.Builder<Banner>()
                .setQuery(banners, Banner.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Banner, BannerViewHolder>(allBanner) {
            @Override
            protected void onBindViewHolder(@NonNull BannerViewHolder holder, int position, @NonNull Banner model) {
                holder.banner_name.setText(model.getName());
                Picasso.with(getBaseContext())
                        .load(model.getImage())
                        .into(holder.banner_image);
            }

            @NonNull
            @Override
            public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.banner_layout, parent, false);
                return new BannerViewHolder(itemView);
            }
        };
        adapter.startListening();

        //Set adapter
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }
    
    private void loadListFood() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference foods = database.getReference("Foods");

        foods.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Food food = snapshot.getValue(Food.class);
                    foodList.add(food);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(BannerActivity.this, "Can't get list Banner from database!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private void showAddBanner() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(BannerActivity.this);
        alertDialog.setTitle("Thêm ảnh bìa");
        alertDialog.setMessage("Chọn món ăn bạn muốn thêm:");

        loadListFood();

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_banner, null);
    }
}