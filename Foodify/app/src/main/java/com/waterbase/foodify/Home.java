package com.waterbase.foodify;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.andremion.counterfab.CounterFab;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import com.waterbase.foodify.Common.Common;
import com.waterbase.foodify.Database.Database;
import com.waterbase.foodify.Interface.ItemClickListener;
import com.waterbase.foodify.Model.Banner;
import com.waterbase.foodify.Model.Category;
import com.waterbase.foodify.Model.Token;
import com.waterbase.foodify.ViewHolder.MenuViewHolder;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import io.paperdb.Paper;


public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseDatabase database;
    DatabaseReference category;

    RecyclerView recyler_menu;
    FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;

    SwipeRefreshLayout swipeRefreshLayout;

    CounterFab fab;

    //Slider
    HashMap<String, String> image_list;
    SliderLayout mSlider;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set font all activity
        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/font.ttf")
                                .setFontAttrId(io.github.inflationx.calligraphy3.R.attr.fontPath)
                                .build()))
                .build());

        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Menu");
        setSupportActionBar(toolbar);

        //Floating Action Button
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Home.this, Cart.class));
            }
        });

        fab.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));

        //View
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Common.isConnectedToInternet(getBaseContext()))
                    loadMenu();
                else {
                    Toast.makeText(getBaseContext(), "Vui lòng kiểm tra kết nối mạng!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        //Default, load for first time
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (Common.isConnectedToInternet(getBaseContext()))
                    loadMenu();
                else {
                    Toast.makeText(getBaseContext(), "Vui lòng kiểm tra kết nối mạng!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.app_name, R.string.app_name);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //Init database
        database = FirebaseDatabase.getInstance();
        category = database.getReference("Category");

        FirebaseRecyclerOptions<Category> options = new FirebaseRecyclerOptions.Builder<Category>()
                .setQuery(category, Category.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MenuViewHolder viewHolder, int i, @NonNull Category model) {
                viewHolder.txtMenuName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.imageView);
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        //Get Category Id and send to new Activity
                        Intent foodList = new Intent(getBaseContext(), FoodList.class);
                        foodList.putExtra("CategoryId", adapter.getRef(position).getKey());
                        foodList.putExtra("CategoryName", model.getName());
                        startActivity(foodList);
                    }
                });
            }

            @NonNull
            @Override
            public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.menu_item, parent, false);
                return new MenuViewHolder(itemView);
            }
        };

        adapter.startListening();

        //Init paper
        Paper.init(this);

        //Load menu
        recyler_menu = (RecyclerView) findViewById(R.id.recyler_menu);
        recyler_menu.setLayoutManager(new GridLayoutManager(this, 2));
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recyler_menu.getContext(),
                R.anim.layout_fall_down);
        recyler_menu.setLayoutAnimation(controller);

        //Set name for user
        View headerView = navigationView.getHeaderView(0);
        TextView navUserName = (TextView) headerView.findViewById(R.id.txtFullName);
        navUserName.setText(Common.currentUser.getName());

        updateToken(FirebaseInstanceId.getInstance().getToken());

        //Setup Slider
        setupSlider();

    }

    private void setupSlider() {
        mSlider = findViewById(R.id.slider);
        image_list = new HashMap<>();

        DatabaseReference banners = database.getReference("Banner");

        banners.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot postSnapShot: dataSnapshot.getChildren())
                {
                    Banner banner = postSnapShot.getValue(Banner.class);

                    image_list.put(banner.getName() + "@@@" + banner.getId(), banner.getImage());
                }
                for(String key: image_list.keySet())
                {
                    String[] keySplit = key.split("@@@");
                    String nameOfFood = keySplit[0];
                    String idOfFood = keySplit[1];

                    //Create Slider
                    TextSliderView textSliderView = new TextSliderView(getBaseContext());
                    textSliderView
                            .description(nameOfFood)
                            .image(image_list.get(key))
                            .setScaleType(BaseSliderView.ScaleType.Fit)
                            .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                                @Override
                                public void onSliderClick(BaseSliderView slider) {
                                    Intent intent = new Intent(Home.this, FoodDetail.class);
                                    //Send Food id to another activity
                                    intent.putExtras(textSliderView.getBundle());
                                    startActivity(intent);
                                }
                            });

                    //Add extra bundle
                    textSliderView.bundle(new Bundle());
                    textSliderView.getBundle().putString("FoodId", idOfFood);

                    mSlider.addSlider(textSliderView);

                    //Remove event after finish
                    banners.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mSlider.setPresetTransformer(SliderLayout.Transformer.Background2Foreground);
        mSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mSlider.setCustomAnimation(new DescriptionAnimation());
        mSlider.setDuration(4000);

    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
        mSlider.stopAutoCycle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.startListening();
        fab.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));
    }

    private void updateToken(String token) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");
        Token data = new Token(token, false);
        tokens.child(Common.currentUser.getPhone()).setValue(data);
    }

    private void loadMenu() {

        recyler_menu.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);

        //Animation
        recyler_menu.getAdapter().notifyDataSetChanged();
        recyler_menu.scheduleLayoutAnimation();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_cart) {
            Intent cartIntent = new Intent(Home.this, Cart.class);
            startActivity(cartIntent);
        } else if (id == R.id.nav_order) {
            Intent orderIntent = new Intent(Home.this, OrderStatus.class);
            startActivity(orderIntent);
        } else if (id == R.id.nav_change_pwd) {
            showChangePasswordDialog();
        } else if (id == R.id.nav_home_address) {
            showHomeAddressDialog();
        } else if (id == R.id.nav_setting) {
            showSettingDialog();
        } else if (id == R.id.nav_favorites) {
            startActivity(new Intent(Home.this, FavoritesActivity.class));
        }
        else if (id == R.id.nav_log_out) {

            //Delete Remember user & password
            Paper.book().destroy();

            //Logout
            Intent signIn = new Intent(Home.this, SignIn.class);
            signIn.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(signIn);
            finish();
        }

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    private void showSettingDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Cài đặt");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_setting = inflater.inflate(R.layout.setting_layout, null);

        CheckBox ckb_subscribe_new = layout_setting.findViewById(R.id.ckb_sub_new);
        //Add code remember state of Checkbox
        Paper.init(this);
        String isSubscribe = Paper.book().read("sub_new");
        if(isSubscribe == null || TextUtils.isEmpty(isSubscribe) || isSubscribe.equals("false"))
            ckb_subscribe_new.setChecked(false);
        else
            ckb_subscribe_new.setChecked(true);

        alertDialog.setView(layout_setting);

        alertDialog.setPositiveButton("Xong", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                if(ckb_subscribe_new.isChecked())
                {
                    FirebaseMessaging.getInstance().subscribeToTopic(Common.topicName);
                    //Write value
                    Paper.book().write("sub_new", "true");
                }
                else
                {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.topicName);
                    //Write value
                    Paper.book().write("sub_new", "false");
                }
            }
        });

        alertDialog.show();
    }

    private void showHomeAddressDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Đặt địa chỉ mặc định");
        alertDialog.setMessage("Vui lòng điền đầy đủ thông tin");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_home_address = inflater.inflate(R.layout.home_address_layout, null);

        final MaterialEditText edtHomeAddress = layout_home_address.findViewById(R.id.edtHomeAddress);

        alertDialog.setView(layout_home_address);

        alertDialog.setPositiveButton("Cập nhật", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                //Set new Home Address
                Common.currentUser.setHomeAddress(edtHomeAddress.getText().toString());

                FirebaseDatabase.getInstance().getReference("User")
                        .child(Common.currentUser.getPhone())
                        .setValue(Common.currentUser)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(Home.this, "Cập nhật địa chỉ thành công!", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        alertDialog.show();
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Đổi mật khẩu");
        alertDialog.setMessage("Vui lòng điền đầy đủ thông tin");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_pwd = inflater.inflate(R.layout.change_password_layout, null);

        MaterialEditText edtPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtPassword);
        MaterialEditText edtNewPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtNewPassword);
        MaterialEditText edtRepeatPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtRepeatPassword);

        alertDialog.setView(layout_pwd);

        //Button
        alertDialog.setPositiveButton("Đổi mật khẩu", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                android.app.AlertDialog waitingDialog = new SpotsDialog(Home.this);
                waitingDialog.show();

                //Check old password
                if(edtPassword.getText().toString().equals(Common.currentUser.getPassword())) {

                    //Check new password and repeat password
                    if(edtNewPassword.getText().toString().equals(edtRepeatPassword.getText().toString())){
                        Map<String, Object> passwordUpdate = new HashMap<>();
                        passwordUpdate.put("password", edtNewPassword.getText().toString());

                        //Make update
                        DatabaseReference user = FirebaseDatabase.getInstance().getReference("User");
                        user.child(Common.currentUser.getPhone())
                                .updateChildren(passwordUpdate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        waitingDialog.dismiss();
                                        Toast.makeText(Home.this, "Mật khẩu đã được cập nhật!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(Home.this, "Đã xảy ra lỗi hệ thống: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        waitingDialog.dismiss();
                        Toast.makeText(Home.this, "Mật khẩu chưa khớp! Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    waitingDialog.dismiss();
                    Toast.makeText(Home.this, "Sai mật khẩu! Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        AlertDialog dialog = alertDialog.create();
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.menu_search)
            startActivity(new Intent(Home.this, SearchActivity.class));

        return super.onOptionsItemSelected(item);
    }
}