package com.waterbase.foodify;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.waterbase.foodify.Common.Common;
import com.waterbase.foodify.Database.Database;
import com.waterbase.foodify.Helper.RecyclerItemTouchHelper;
import com.waterbase.foodify.Interface.RecyclerItemTouchHelperListener;
import com.waterbase.foodify.Model.Order;
import com.waterbase.foodify.Model.Request;
import com.waterbase.foodify.Remote.APIService;
import com.waterbase.foodify.Remote.IGoogleService;
import com.waterbase.foodify.ViewHolder.CartAdapter;
import com.waterbase.foodify.ViewHolder.CartViewHolder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import info.hoang8f.widget.FButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Cart extends AppCompatActivity implements RecyclerItemTouchHelperListener {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    public TextView txtTotalPrice, txtEmptyCart;
    FButton btnPlace;

    List<Order> cart = new ArrayList<>();
    CartAdapter adapter;

    APIService mService;

    RelativeLayout rootLayout;

    //Location
    private static final int REQUEST_CHECK_SETTINGS = 100;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_IN_MILLISECONDS = 1000;
    private static final String TAG = Cart.class.getSimpleName();

    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallBack;
    private Location mCurrentLocation;
    private boolean mRequestingLocationUpdates = false;


    //Declare Google Map API Retrofit
    IGoogleService mGoogleMapService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cart);

        //Init
        mService = Common.getFCMService();
        mGoogleMapService = Common.getGoogleMapAPI();

        //Firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        //Init
        recyclerView = (RecyclerView) findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        rootLayout = findViewById(R.id.rootLayout);
        txtEmptyCart = findViewById(R.id.textEmptyCart);

        visibilityTextEmpty();

        //Swipe to delete
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        txtTotalPrice = (TextView) findViewById(R.id.total);
        btnPlace = (FButton) findViewById(R.id.btnPlaceOrder);

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cart.size() > 0) {
                    showAlertDialog();
                } else
                    Toast.makeText(Cart.this, "Giỏ hàng của bạn đang trống!!!", Toast.LENGTH_SHORT).show();
            }
        });

        loadListFood();

        //Location

        if (Common.currentLocation == null) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mSettingsClient = LocationServices.getSettingsClient(this);

            mLocationCallBack = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    mCurrentLocation = locationResult.getLastLocation();
                    Common.currentLocation = mCurrentLocation;
                }
            };

            mLocationRequest = LocationRequest.create()
                    .setInterval(UPDATE_INTERVAL_IN_MILLISECONDS)
                    .setFastestInterval(FASTEST_UPDATE_IN_MILLISECONDS)
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(mLocationRequest);
            mLocationSettingsRequest = builder.build();

            Dexter.withContext(this)
                    .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                            mRequestingLocationUpdates = true;
                            startLocationUpdates();
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse response) {
                            if (response.isPermanentlyDenied()) {
                                openSettings();
                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    }).check();
        }

        // calling the action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(0xFFFF5353));
        actionBar.setTitle(Html.fromHtml("<font color='#ffffff'>Giỏ hàng</font>"));

        // showing the back button in action bar
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("Còn 1 bước nữa!");
        alertDialog.setMessage("Xin vui lòng điền thông tin dưới đây: ");

        LayoutInflater inflater = this.getLayoutInflater();
        View order_address_comment = inflater.inflate(R.layout.order_address_comment, null);

        MaterialEditText edtAddress = (MaterialEditText) order_address_comment.findViewById(R.id.edtAddress);
        MaterialEditText edtComment = (MaterialEditText) order_address_comment.findViewById(R.id.edtComment);


        //Radio
        RadioButton rdiShipToAddress = order_address_comment.findViewById(R.id.rdiShipToAddress);
        RadioButton rdiAddress = order_address_comment.findViewById(R.id.rdiAddress);
        RadioButton rdiHomeAddress = order_address_comment.findViewById(R.id.rdiHomeAddress);

        rdiHomeAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!TextUtils.isEmpty(Common.currentUser.getHomeAddress()) || Common.currentUser.getHomeAddress() != null) {
                        edtAddress.setText(Common.currentUser.getHomeAddress());
                        edtAddress.setEnabled(false);
                    } else {
                        edtAddress.setText("");
                        edtAddress.setEnabled(false);
                        Toast.makeText(Cart.this, "Vui lòng điền địa chỉ mặc định của bạn!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        rdiAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    edtAddress.setText("");
                    edtAddress.setEnabled(true);
                }

            }
        });

        //Add event
        rdiShipToAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (Common.currentAddress == null) {
                        mGoogleMapService.getAddressName(String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&key=AIzaSyCqmoLlawoQzmwjZI2_Yo_V33An_nNZADs",
                                        Common.currentLocation.getLatitude(),
                                        Common.currentLocation.getLongitude()))
                                .enqueue(new Callback<String>() {
                                    @Override
                                    public void onResponse(Call<String> call, Response<String> response) {
                                        try {
                                            JSONObject jsonObject = new JSONObject(response.body());

                                            JSONArray resultsArray = jsonObject.getJSONArray("results");

                                            JSONObject firstObject = resultsArray.getJSONObject(0);

                                            Common.currentAddress = firstObject.getString("formatted_address");
                                            edtAddress.setText(Common.currentAddress);
                                            edtAddress.setEnabled(false);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<String> call, Throwable t) {
                                        Toast.makeText(Cart.this, "Error to get location: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                        Log.i("LOCATION", "" + t.getMessage());
                                    }
                                });
                    } else {
                        edtAddress.setText(Common.currentAddress);
                        edtAddress.setEnabled(false);
                    }

                }
            }
        });

        alertDialog.setView(order_address_comment);
        alertDialog.setIcon(R.drawable.ic_baseline_shopping_cart_24);

        alertDialog.setPositiveButton("Đặt", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (TextUtils.isEmpty(edtAddress.getText().toString())) {
                    Toast.makeText(Cart.this, "Vui lòng nhập địa chỉ giao hàng!", Toast.LENGTH_SHORT).show();
                    return;
                }

                //Create new Request
                Request request = new Request(
                        Common.currentUser.getPhone(),
                        Common.currentUser.getName(),
                        edtAddress.getText().toString(),
                        txtTotalPrice.getText().toString(),
                        "0",
                        edtComment.getText().toString(),
                        "0",
                        cart
                );

                //Pass object to another activity!
                Intent intent = new Intent(Cart.this, OrderDetail.class);
                intent.putExtra("requests", request);
                startActivity(intent);

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


    private void loadListFood() {
        cart = new Database(this).getCarts(Common.currentUser.getPhone());
        adapter = new CartAdapter(cart, this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        //Calculate total price
        float total = 0;
        for (Order order : cart)
            total += (Float.parseFloat(order.getPrice())) * (Float.parseFloat(order.getQuantity())) * (100 - Long.parseLong(order.getDiscount())) / 100;
        ;
        Locale locale = new Locale("vi", "VN");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

        txtTotalPrice.setText(fmt.format(total));
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

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().equals(Common.DELETE))
            deleteCart(item.getOrder());
        return true;
    }

    private void deleteCart(int position) {
        //Remove item at List<Order> by position
        cart.remove(position);
        //Delete all old data from SQLite
        new Database(this).cleanCart(Common.currentUser.getPhone());
        //Update new data from List<Order> to SQLite
        for (Order item : cart)
            new Database(this).addToCart(item);
        //Refresh
        loadListFood();
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof CartViewHolder) {
            String name = ((CartAdapter) recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition()).getProductName();

            Order deleteItem = ((CartAdapter) recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());
            int deleteIndex = viewHolder.getAdapterPosition();

            adapter.removeItem(deleteIndex);
            new Database(getBaseContext()).removeFromCart(deleteItem.getProductId(), Common.currentUser.getPhone());

            updateTotalPrice();

            //Make Snackbar
            Snackbar snackbar = Snackbar.make(rootLayout, name + " đã xoá khỏi giỏ hàng của bạn!", Snackbar.LENGTH_LONG);
            snackbar.setAction("Hoàn tác", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.restoreItem(deleteItem, deleteIndex);
                    new Database(getBaseContext()).addToCart(deleteItem);
                    updateTotalPrice();
                    visibilityTextEmpty();
                }
            });

            visibilityTextEmpty();
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }

    public void updateTotalPrice() {
        //Calculate total price
        float total = 0;
        List<Order> orders = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
        for (Order item : orders)
            total += (Float.parseFloat(item.getPrice())) * (Float.parseFloat(item.getQuantity())) * (100 - Long.parseLong(item.getDiscount())) / 100;
        Locale locale = new Locale("vi", "VN");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
        txtTotalPrice.setText(fmt.format(total));
    }

    //Location

    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void startLocationUpdates() {
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest).addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @SuppressLint("MissingPermission")
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallBack, Looper.myLooper());

            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade location settings");

                        try {
                            ResolvableApiException rae = (ResolvableApiException) e;
                            rae.startResolutionForResult(Cart.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException sie) {
                            Log.i(TAG, "PendingIntent unable to execute request");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        String errorMessage = "Location settings are inadequate, and cannot be fixed here. Fix in Settings";
                        Log.e(TAG, errorMessage);

                        Toast.makeText(Cart.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallBack).addOnCompleteListener(this, task -> Log.d(TAG, "Location updates stopped!"));
    }

    private boolean checkPermission() {
        int permissionState = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRequestingLocationUpdates && checkPermission()) {
            startLocationUpdates();
        }
        visibilityTextEmpty();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mRequestingLocationUpdates) {
            stopLocationUpdates();
        }
    }

    private void visibilityTextEmpty() {
        if (cart.size() > 0) {
            txtEmptyCart.setVisibility(View.GONE);
        } else {
            txtEmptyCart.setVisibility(View.VISIBLE);
        }
    }

}