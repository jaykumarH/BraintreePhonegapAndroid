package com.aptitudes;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Admin on 9/13/2016.
 */
public class Apputility {

    public static final String SERVER_BASE = "http://192.168.0.16:80/braintree/braintreetest.php"; // Replace with your own server
    public static String checkNwConn = "check network connection";
    public static boolean isNetConnected(Context context) {
        if (context == null) {
            return true;
        }
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nwInfo = connectivityManager.getActiveNetworkInfo();
        if (nwInfo != null && nwInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    public static String actionGetToken = "getToken";
    public static String actionGetNonce = "getNonce";
}

