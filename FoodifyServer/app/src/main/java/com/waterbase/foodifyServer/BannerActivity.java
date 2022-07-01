package com.waterbase.foodifyServer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.squareup.picasso.Picasso;
import com.waterbase.foodifyServer.Common.Common;
import com.waterbase.foodifyServer.Model.Banner;
import com.waterbase.foodifyServer.Model.Food;
import com.waterbase.foodifyServer.Model.Request;
import com.waterbase.foodifyServer.ViewHolder.BannerViewHolder;

import java.util.ArrayList;
import java.util.List;

import info.hoang8f.widget.FButton;

public class BannerActivity extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference foods = database.getReference("Foods");

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
    MaterialSpinner foodListSpinner;

    Banner newBanner;
    Uri filePath;
    
    List<Food> foodList =  new ArrayList<>();
    List foodName = new ArrayList();

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
        loadListFood();
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

        foods.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Food food = snapshot.getValue(Food.class);
                    foodList.add(food);
                }

                foodName = new ArrayList();

                for(Food food: foodList)
                    foodName.add(food.getName());
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

        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.add_new_banner, null);

        foodListSpinner = view.findViewById(R.id.foodList);

        foodListSpinner.setItems(foodName);
        alertDialog.setView(view);
        alertDialog.setIcon(R.drawable.ic_baseline_laptop_24);

        alertDialog.setPositiveButton("Thêm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Get Object Food has selected
                Food foodSelectedName = foodList.get(foodListSpinner.getSelectedIndex());

                //Get key Food Id from Firebase
                foods.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if(dataSnapshot.exists())
                        {
                            String pushKey = dataSnapshot.getKey();
                            foods.child(pushKey).addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                    if(dataSnapshot.exists())
                                    {
                                        if(dataSnapshot.getKey().equals("name"))
                                        {
                                            if(dataSnapshot.getValue().toString().equals(foodSelectedName.getName())){
                                                //Create new Object banner
                                                newBanner = new Banner();
                                                newBanner.setId(pushKey);
                                                newBanner.setName(foodSelectedName.getName());
                                                newBanner.setImage(foodSelectedName.getImage());

                                                if(newBanner != null)
                                                    //Push Banner to Firebase
                                                    banners.push().setValue(newBanner);
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onChildRemoved(DataSnapshot dataSnapshot) {

                                }

                                @Override
                                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });

        alertDialog.setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();

    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if(item.getTitle().equals(Common.UPDATE))
        {
            showUpdateBannerDialog(adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()));
        }
        else if(item.getTitle().equals(Common.DELETE))
        {
            deleteBanner(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    private void deleteBanner(String key) {
        banners.child(key).removeValue();
    }

    private void showUpdateBannerDialog(String key, Banner item) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(BannerActivity.this);
        alertDialog.setTitle("Cập nhật ảnh bìa");
        alertDialog.setMessage("Vui lòng chọn món ăn bạn muốn cập nhật:");


        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.add_new_banner, null);

        foodListSpinner = view.findViewById(R.id.foodList);
        foodListSpinner.setItems(foodName);

        banners.orderByKey().equalTo(key)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot postSnapShot: dataSnapshot.getChildren()) {
                            Banner data = postSnapShot.getValue(Banner.class);
                            foodListSpinner.setSelectedIndex(foodName.indexOf(data.getName()));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(BannerActivity.this, "Đã có lỗi từ hệ thống, vui lòng thử lại sau!", Toast.LENGTH_SHORT).show();
                    }
                });

        alertDialog.setView(view);

        final String localKey = key;

        alertDialog.setPositiveButton("Cập nhật", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Get Object Food has selected
                Food foodSelectedName = foodList.get(foodListSpinner.getSelectedIndex());

                //Get key Food Id from Firebase
                foods.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if(dataSnapshot.exists())
                        {
                            String pushKey = dataSnapshot.getKey();
                            foods.child(pushKey).addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                    if(dataSnapshot.exists())
                                    {
                                        if(dataSnapshot.getKey().equals("name"))
                                        {
                                            if(dataSnapshot.getValue().toString().equals(foodSelectedName.getName())){
                                                item.setId(pushKey);
                                                item.setName(foodSelectedName.getName());
                                                item.setImage(foodSelectedName.getImage());

                                                banners.child(localKey).setValue(item);
                                                adapter.notifyDataSetChanged();
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onChildRemoved(DataSnapshot dataSnapshot) {

                                }

                                @Override
                                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });

        alertDialog.setNegativeButton("Huỷ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();
    }
}