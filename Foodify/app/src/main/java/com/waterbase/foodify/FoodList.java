package com.waterbase.foodify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.StrikethroughSpan;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;
import com.waterbase.foodify.Common.Common;
import com.waterbase.foodify.Database.Database;
import com.waterbase.foodify.Interface.ItemClickListener;
import com.waterbase.foodify.Model.Favorites;
import com.waterbase.foodify.Model.Food;
import com.waterbase.foodify.Model.Order;
import com.waterbase.foodify.ViewHolder.FoodViewHolder;

import java.util.ArrayList;
import java.util.List;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class FoodList extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference foodList;

    String categoryId = "", categoryName = "";

    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;

    //Search functionality
    FirebaseRecyclerAdapter<Food, FoodViewHolder> searchAdapter;
    List<String> suggestList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;

    //Favorites
    Database localDB;

    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_food_list);

        //Firebase
        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("Foods");

        //Local DB
        localDB = new Database(this);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Get Intent here
                if (getIntent() != null)
                    categoryId = getIntent().getStringExtra("CategoryId");
                categoryName = getIntent().getStringExtra("CategoryName");
                if (!categoryId.isEmpty() && categoryId != null) {
                    if (Common.isConnectedToInternet(getBaseContext()))
                        loadListFood(categoryId);
                    else {
                        Toast.makeText(FoodList.this, "Vui lòng kiểm tra kết nối mạng!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        });

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                //Get Intent here
                if (getIntent() != null)
                    categoryId = getIntent().getStringExtra("CategoryId");
                categoryName = getIntent().getStringExtra("CategoryName");
                if (!categoryId.isEmpty() && categoryId != null) {
                    if (Common.isConnectedToInternet(getBaseContext()))
                        loadListFood(categoryId);
                    else {
                        Toast.makeText(FoodList.this, "Vui lòng kiểm tra kết nối mạng!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                setTitle(categoryName);

                //Search
                materialSearchBar = (MaterialSearchBar) findViewById(R.id.searchBar);
                materialSearchBar.setHint("Tìm kiếm món ăn");
                loadSuggest();
                materialSearchBar.setCardViewElevation(10);
                materialSearchBar.addTextChangeListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        //When user type their text, we will change suggest list

                        List<String> suggest = new ArrayList<String>();
                        for (String search : suggestList) {
                            if (search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                                suggest.add(search);
                        }
                        materialSearchBar.setLastSuggestions(suggest);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
                    @Override
                    public void onSearchStateChanged(boolean enabled) {
                        //When search bar is close
                        //Restore original adapter
                        if (!enabled)
                            recyclerView.setAdapter(adapter);
                    }

                    @Override
                    public void onSearchConfirmed(CharSequence text) {
                        //When search finish
                        //Show result of search adapter
                        startSearch(text);
                    }

                    @Override
                    public void onButtonClicked(int buttonCode) {

                    }
                });
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // calling the action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(0xFFFF5353));
        actionBar.setTitle(Html.fromHtml("<font color='#ffffff'>" + categoryName +"</font>"));

        // showing the back button in action bar
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadListFood(categoryId);
    }

    private void startSearch(CharSequence text) {
        //Create query by name
        Query searchByName = foodList.orderByChild("name").equalTo(text.toString());
        //Create options with query
        FirebaseRecyclerOptions<Food> foodOptions = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(searchByName, Food.class)
                .build();
        searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(foodOptions) {
            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder viewHolder, int i, @NonNull Food model) {
                viewHolder.food_name.setText(model.getName());
                if (Integer.parseInt(model.getDiscount()) > 0) {
                    String foodPrice = model.getPrice() + "đ";
                    long newFoodPrice = Long.parseLong(model.getPrice()) - Long.parseLong(model.getPrice()) * Long.parseLong(model.getDiscount()) / 100;
                    SpannableStringBuilder spnBuilder = new SpannableStringBuilder(foodPrice);
                    StrikethroughSpan strikethroughSpan = new StrikethroughSpan();
                    spnBuilder.setSpan(strikethroughSpan, 0, foodPrice.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    viewHolder.food_price.setText(spnBuilder);
                    viewHolder.newPrice.setText(newFoodPrice + "đ");
                    viewHolder.discount.setText("- " + model.getDiscount() + "%");
                } else {
                    viewHolder.food_price.setText(String.format("%s đ", model.getPrice()));
                    viewHolder.discount.setVisibility(View.GONE);
                    viewHolder.newPrice.setVisibility(View.GONE);
                }

                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.food_image);

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Start Activity
                        Intent foodDetail = new Intent(FoodList.this, FoodDetail.class);
                        foodDetail.putExtra("FoodId", searchAdapter.getRef(position).getKey()); // Send Food Id to new activity
                        startActivity(foodDetail);
                    }
                });
            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.food_item, parent, false);
                return new FoodViewHolder(itemView);
            }
        };

        searchAdapter.startListening();
        recyclerView.setAdapter(searchAdapter); //Set adapter for Recycler View is Search result
    }

    private void loadSuggest() {
        foodList.orderByChild("menuId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Food item = postSnapshot.getValue(Food.class);
                            suggestList.add(item.getName());
                        }

                        materialSearchBar.setLastSuggestions(suggestList);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void loadListFood(String categoryId) {
        //Create query by category Id
        Query searchByName = foodList.orderByChild("menuId").equalTo(categoryId);
        //Create Options with query
        FirebaseRecyclerOptions<Food> foodOptions = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(searchByName, Food.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(foodOptions) {
            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder viewHolder, int position, @NonNull Food model) {
                viewHolder.food_name.setText(model.getName());

                if (Integer.parseInt(model.getDiscount()) > 0) {
                    String foodPrice = model.getPrice() + "đ";
                    long newFoodPrice = Long.parseLong(model.getPrice()) - Long.parseLong(model.getPrice()) * Long.parseLong(model.getDiscount()) / 100;
                    SpannableStringBuilder spnBuilder = new SpannableStringBuilder(foodPrice);
                    StrikethroughSpan strikethroughSpan = new StrikethroughSpan();
                    spnBuilder.setSpan(strikethroughSpan, 0, foodPrice.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    viewHolder.food_price.setText(spnBuilder);
                    viewHolder.newPrice.setText(newFoodPrice + "đ");
                    viewHolder.discount.setText("- " + model.getDiscount() + "%");
                } else {
                    viewHolder.food_price.setText(String.format("%s đ", model.getPrice()));
                    viewHolder.discount.setVisibility(View.GONE);
                    viewHolder.newPrice.setVisibility(View.GONE);
                }

                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.food_image);

                //Quick Cart

                viewHolder.quick_cart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean ifExists = new Database(getBaseContext()).checkFoodExists(adapter.getRef(viewHolder.getAdapterPosition()).getKey(), Common.currentUser.getPhone());
                        if (!ifExists) {
                            new Database(getBaseContext()).addToCart(new Order(
                                    Common.currentUser.getPhone(),
                                    adapter.getRef(viewHolder.getAdapterPosition()).getKey(),
                                    model.getName(),
                                    "1",
                                    model.getPrice(),
                                    model.getDiscount(),
                                    model.getImage()
                            ));
                        } else {
                            new Database(getBaseContext()).increaseCart(Common.currentUser.getPhone(), adapter.getRef(viewHolder.getAdapterPosition()).getKey());
                        }

                        Toast.makeText(FoodList.this, "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
                    }
                });


                //Add Favorites
                if (localDB.isFavorite(adapter.getRef(position).getKey(), Common.currentUser.getPhone()))
                    viewHolder.fav_image.setImageResource(R.drawable.ic_baseline_favorite_24);

                //Click to change state of Favorites
                viewHolder.fav_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Favorites favorites = new Favorites();

                        favorites.setFoodId(adapter.getRef(viewHolder.getAdapterPosition()).getKey());
                        favorites.setFoodName(model.getName());
                        favorites.setFoodDescription(model.getDescription());
                        favorites.setFoodDiscount(model.getDiscount());
                        favorites.setFoodImage(model.getImage());
                        favorites.setFoodMenuId(model.getMenuId());
                        favorites.setUserPhone(Common.currentUser.getPhone());
                        favorites.setFoodPrice(model.getPrice());

                        if (!localDB.isFavorite(adapter.getRef(viewHolder.getAdapterPosition()).getKey(), Common.currentUser.getPhone())) {
                            localDB.addToFavorites(favorites);
                            viewHolder.fav_image.setImageResource(R.drawable.ic_baseline_favorite_24);
                            Toast.makeText(FoodList.this, model.getName() + " đã thêm vào danh sách yêu thích", Toast.LENGTH_SHORT).show();
                        } else {
                            localDB.removeFromFavorites(adapter.getRef(viewHolder.getAdapterPosition()).getKey(), Common.currentUser.getPhone());
                            viewHolder.fav_image.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                            Toast.makeText(FoodList.this, model.getName() + " đã xoá khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Start Activity
                        Intent foodDetail = new Intent(FoodList.this, FoodDetail.class);
                        foodDetail.putExtra("FoodId", adapter.getRef(position).getKey()); // Send Food Id to new activity
                        startActivity(foodDetail);
                    }
                });
            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.food_item, parent, false);
                return new FoodViewHolder(itemView);
            }
        };

        //Set Adapter
        recyclerView.setAdapter(adapter);
        adapter.startListening();
        swipeRefreshLayout.setRefreshing(false);
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (adapter == null) {
            adapter.stopListening();
            searchAdapter.stopListening();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}