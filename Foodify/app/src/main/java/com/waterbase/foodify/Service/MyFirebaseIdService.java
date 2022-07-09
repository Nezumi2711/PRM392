package com.waterbase.foodify.Service;

import androidx.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.waterbase.foodify.Common.Common;
import com.waterbase.foodify.Model.Token;

public class MyFirebaseIdService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        updateTokenToFirebase(token);
    }

    private void updateTokenToFirebase(String tokenRefreshed) {
        if (Common.currentUser != null) {
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            DatabaseReference tokens = db.getReference("Tokens");
            Token token = new Token(tokenRefreshed, false);
            tokens.child(Common.currentUser.getPhone()).setValue(token);
        }
    }
}
