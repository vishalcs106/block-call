package android.blockcall.in.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CallReceiver extends BroadcastReceiver {

    public CallReceiver() {
    }

    Context mContext;
    boolean requested = false;
    SharedPreferences sharedpreferences;
    @Override
    public void onReceive(Context context, Intent intent)
    {
        sharedpreferences = context.getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        requested = sharedpreferences.getBoolean("requested", false);
        MyPhoneStateListener phoneListener=new MyPhoneStateListener();
        TelephonyManager telephony = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        telephony.listen(phoneListener,PhoneStateListener.LISTEN_CALL_STATE);
        mContext = context;
    }


    public class MyPhoneStateListener extends PhoneStateListener {
        public void onCallStateChanged(int state, final String incomingNumber){
            switch(state){
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.d("DEBUG", "IDLE");
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.d("DEBUG", "OFFHOOK");
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    Executor eS = Executors.newSingleThreadExecutor();
                    eS.execute(new Runnable() {
                        @Override
                        public void run() {
                            disconnectCallAndroid();
                            sendMessageRequest(incomingNumber);
                        }
                    });
                    Log.d("DEBUG", "RINGING"+ incomingNumber);
                    break;
            }
        }
    }

    public int disconnectCallAndroid()
    {
        Runtime runtime = Runtime.getRuntime();
        int nResp = 0;
        try
        {
            Log.d("Keys.LOGTAG", "service call phone 6 \n");
            runtime.exec("service call phone 5 \n");

        }catch(Exception exc)
        {
            Log.e("Keys.LOGTAG", exc.getMessage());
            exc.printStackTrace();
        }
        return nResp;
    }
    public String getUDID(Context context) {

        String	udid = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return udid;
    }

    private void sendMessageRequest(String phNum) {
        SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putBoolean("requested", requested);
            editor.commit();
            String URL = "https://doctorscircle.in/utility/send_download_app_sms";
            String url = URL + "?device_id=" + getUDID(mContext) + "&user_id=0&ph_num=" + phNum.substring(3);
            JsonObjectRequest videosRequest = new JsonObjectRequest(Request.Method.GET, url, (String) null,
                    new com.android.volley.Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (response.getBoolean("status")) {

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d("", "Error: " + error.getMessage());
                }
            });
            videosRequest.setRetryPolicy(new DefaultRetryPolicy(
                    (int) TimeUnit.SECONDS.toMillis(20),
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            AppController.getInstance().addToRequestQueue(videosRequest);
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    requested = false;
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putBoolean("requested", requested);
                    editor.commit();
                }
            }, 1000);


    }
}
