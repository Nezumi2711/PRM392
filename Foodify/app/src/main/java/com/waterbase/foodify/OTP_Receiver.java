package com.waterbase.foodify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OTP_Receiver extends BroadcastReceiver {

    private static EditText otp1, otp2, otp3, otp4, otp5, otp6;

    public void setEditText(EditText otp1, EditText otp2, EditText otp3, EditText otp4, EditText otp5, EditText otp6){
        OTP_Receiver.otp1 = otp1;
        OTP_Receiver.otp2 = otp2;
        OTP_Receiver.otp3 = otp3;
        OTP_Receiver.otp4 = otp4;
        OTP_Receiver.otp5 = otp5;
        OTP_Receiver.otp6 = otp6;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);

        for(SmsMessage sms: messages){
            String message = sms.getMessageBody();

            Pattern otpPattern = Pattern.compile("(|^)\\d{6}");
            Matcher matcher = otpPattern.matcher(message);
            if (matcher.find()){
                Double otp = Double.parseDouble(matcher.group(0));

                otp6.setText(String.valueOf(otp % 10));
                otp = otp / 10;
                otp5.setText(String.valueOf(otp % 10));
                otp = otp / 10;
                otp4.setText(String.valueOf(otp % 10));
                otp = otp / 10;
                otp3.setText(String.valueOf(otp % 10));
                otp = otp / 10;
                otp2.setText(String.valueOf(otp % 10));
                otp = otp / 10;
                otp1.setText(String.valueOf(otp % 10));

            }
        }
    }

}
