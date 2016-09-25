package com.plugins;

import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.aptitudes.Apputility;
import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.Card;
import com.braintreepayments.api.exceptions.BraintreeError;
import com.braintreepayments.api.exceptions.ErrorWithResponse;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.AndroidPayCardNonce;
import com.braintreepayments.api.models.CardBuilder;
import com.braintreepayments.api.models.CardNonce;
import com.braintreepayments.api.models.PayPalAccountNonce;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.braintreepayments.api.models.PostalAddress;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Admin on 9/18/2016.
 */
public class BraintreePlugin extends CordovaPlugin implements PaymentMethodNonceCreatedListener, BraintreeErrorListener {

    private static final int REQUEST_CODE = Menu.FIRST;
    private AsyncHttpClient client = new AsyncHttpClient();
    private String clientToken;
    private BraintreeFragment mBraintreeFragment;
    private CallbackContext callbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        this.callbackContext = callbackContext;
        try {
            if (action.equalsIgnoreCase(Apputility.actionGetToken)) {
                return getToken();
            } else if (action.equalsIgnoreCase(Apputility.actionGetNonce)) {
                JSONObject arg_object = args.getJSONObject(0);
                if (mBraintreeFragment != null) {
                    CardBuilder cardBuilder = new CardBuilder()
                            .cardNumber(arg_object.getString("cardNo"))
                            .expirationDate("09/2018");

                    Card.tokenize(mBraintreeFragment, cardBuilder);
                    PluginResult.Status status = PluginResult.Status.NO_RESULT;
                    PluginResult pluginResult = new PluginResult(status);
                    pluginResult.setKeepCallback(true);
                    callbackContext.sendPluginResult(pluginResult);
                }
                return true;
            } else {
                callbackContext.error("invalid action");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
            return false;
        }
    }

    //region Custom Methods
    private synchronized boolean getToken() {
        if (Apputility.isNetConnected(cordova.getActivity())) {
            client.get(Apputility.SERVER_BASE + "?action=token", new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    PluginResult result = new PluginResult(PluginResult.Status.ERROR);
                    result.setKeepCallback(false);
                    callbackContext.sendPluginResult(result);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    clientToken = responseString.trim();
                    Log.d("token", clientToken);
                    try {
                        mBraintreeFragment = BraintreeFragment.newInstance(cordova.getActivity(), clientToken);
                        PluginResult result = new PluginResult(PluginResult.Status.OK, clientToken);
                        result.setKeepCallback(false);
                        callbackContext.sendPluginResult(result);
                        // mBraintreeFragment is ready to use!
                    } catch (InvalidArgumentException e) {
                        Toast.makeText(cordova.getActivity(), "Oops..something wrong with token here", Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            Toast.makeText(cordova.getActivity(), Apputility.checkNwConn, Toast.LENGTH_LONG).show();
        }

        PluginResult.Status status = PluginResult.Status.NO_RESULT;
        PluginResult pluginResult = new PluginResult(status);
        pluginResult.setKeepCallback(true);
        callbackContext.sendPluginResult(pluginResult);
        return true;

    }

    private void sendNonceToServer(RequestParams requestParams) {
        if (Apputility.isNetConnected(cordova.getActivity())) {
            client.post(Apputility.SERVER_BASE + "?action=payment", requestParams, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Toast.makeText(cordova.getActivity(), responseString, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    Toast.makeText(cordova.getActivity(), responseString, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(cordova.getActivity(), Apputility.checkNwConn, Toast.LENGTH_LONG).show();
        }

    }
    //endregion

    @Override
    public void onError(Exception error) {
        if (error instanceof ErrorWithResponse) {
            ErrorWithResponse errorWithResponse = (ErrorWithResponse) error;
            BraintreeError cardErrors = ((ErrorWithResponse) error).errorFor("creditCard");
            if (cardErrors != null) {
                // There is an issue with the credit card.
                BraintreeError expirationMonthError = cardErrors.errorFor("expirationMonth");
                if (expirationMonthError != null) {
                    // There is an issue with the expiration month.
                    Toast.makeText(cordova.getActivity(), expirationMonthError.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethodNonce) {
        String nonce = paymentMethodNonce.getNonce();

        if (paymentMethodNonce instanceof PayPalAccountNonce) {
            PostalAddress shippingAddress = ((PayPalAccountNonce) paymentMethodNonce).getShippingAddress();
            // ...
        } else {
            if (paymentMethodNonce instanceof AndroidPayCardNonce) {
                String lastTwo = ((AndroidPayCardNonce) paymentMethodNonce).getLastTwo();
                // ...
            } else {
                if (paymentMethodNonce instanceof CardNonce) {
                    String cardType = ((CardNonce) paymentMethodNonce).getCardType();
                    RequestParams requestParams = new RequestParams();
                    requestParams.put("payment_method_nonce", nonce);
                    requestParams.put("amount", "10.00");
                    requestParams.put("cardtype", cardType);
                    sendNonceToServer(requestParams);
                }
            }
        }
    }
}
