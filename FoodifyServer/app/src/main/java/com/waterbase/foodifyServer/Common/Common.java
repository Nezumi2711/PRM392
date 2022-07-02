package com.waterbase.foodifyServer.Common;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.waterbase.foodifyServer.Model.Request;
import com.waterbase.foodifyServer.Model.User;
import com.waterbase.foodifyServer.Remote.APIService;
import com.waterbase.foodifyServer.Remote.FCMRetrofitClient;
import com.waterbase.foodifyServer.Remote.IGeoCoordinates;
import com.waterbase.foodifyServer.Remote.RetrofitClient;

public class Common {
    public static User currentUser;
    public static Request currentRequest;

    public static String topicName = "News";

    public static final String UPDATE = "Cập nhật";
    public static final String DELETE = "Xoá";
    public static final int PICK_IMAGE_REQUEST = 71;

    public static final String baseUrl = "https://maps.googleapis.com";

    private static final String fcmUrl = "https://fcm.googleapis.com/";

    public static APIService getFCMClient(){
        return FCMRetrofitClient.getClient(fcmUrl).create(APIService.class);
    }

    public static String coverCodeToStatus(String code){
        if(code.equals("0"))
            return "Đã nhận đơn";
        else if(code.equals("1"))
            return "Đang trên đường";
        else
            return "Giao thành công";
    }

    public static IGeoCoordinates getGeoCodeService() {
        return RetrofitClient.getClient(baseUrl).create(IGeoCoordinates.class);
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, int newWidth, int newHeight)  {
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

        float scaleX = newWidth / (float)bitmap.getWidth();
        float scaleY = newWidth / (float)bitmap.getHeight();
        float pivotX = 0, pivotY = 0;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleX, scaleY, pivotX, pivotY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;
    }
}
