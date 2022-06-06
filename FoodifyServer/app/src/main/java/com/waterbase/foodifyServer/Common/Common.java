package com.waterbase.foodifyServer.Common;

import com.waterbase.foodifyServer.Model.User;

public class Common {
    public static User currentUser;

    public static final String UPDATE = "Cập nhật";
    public static final String DELETE = "Xoá";
    public static final int PICK_IMAGE_REQUEST = 71;

    public static String coverCodeToStatus(String code){
        if(code.equals("0"))
            return "Placed";
        else if(code.equals("1"))
            return "On my way";
        else
            return "Shipped";
    }
}
