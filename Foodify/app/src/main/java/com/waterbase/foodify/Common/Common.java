package com.waterbase.foodify.Common;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.format.DateFormat;

import com.waterbase.foodify.Model.User;
import com.waterbase.foodify.Remote.APIService;
import com.waterbase.foodify.Remote.IGoogleService;
import com.waterbase.foodify.Remote.RetrofitClient;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Locale;

public class Common {
    public static User currentUser;

    public final static String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!_])(?=\\S+$).{4,}$";

    public static String versionAppNewest = "";
    public static String changelog = "";

    public static String PHONE_TEXT = "userPhone";

    public static Location currentLocation;
    public static String currentAddress;

    public static String topicName = "News";

    private static final String BASE_URL = "https://fcm.googleapis.com/";
    private static final String GOOGLE_API_URL = "https://maps.googleapis.com/";

    public static final String INTENT_FOOD_ID = "FoodId";

    public static APIService getFCMService(){
        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }

    public static IGoogleService getGoogleMapAPI(){
        return RetrofitClient.getGoogleApiClient(GOOGLE_API_URL).create(IGoogleService.class);
    }

    public static final String DELETE = "Xoá";
    public static final String USER_KEY = "User";
    public static final String PWD_KEY = "Password";

    public static String convertCodeToStatus(String status) {
        if(status.equals("0"))
            return "Đã nhận đơn";
        else if(status.equals("1"))
            return "Đang trên đường";
        else
            return "Giao thành công";
    }

    public static String convertCodePaymentToStatus(String status) {
        if(status.equals("0"))
            return "CHƯA THANH TOÁN";
        else
            return "ĐÃ THANH TOÁN";
    }

    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(connectivityManager != null) {
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if(info != null){
                for (int i = 0; i < info.length; i++) {
                    if(info[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }

        return false;
    }

    public static String getDate(long time){
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        StringBuilder date = new StringBuilder(
                DateFormat.format("dd-MM-yyyy HH:mm", calendar).toString());
        return date.toString();
    }

    public static String encryptPassword(String input)
    {
        try {
            // getInstance() method is called with algorithm SHA-1
            MessageDigest md = MessageDigest.getInstance("SHA-1");

            // digest() method is called
            // to calculate message digest of the input string
            // returned as array of byte
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);

            // Add preceding 0s to make it 32 bit
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }

            // return the HashText
            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
