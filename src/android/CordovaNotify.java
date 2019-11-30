package cordova.plugin.mqtt.notify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * This class echoes a string called from JavaScript.
 */
public class CordovaNotify extends CordovaPlugin {
    private static final String TAG = CordovaNotify.class.getSimpleName();

    BroadcastReceiver receiver;

    private CallbackContext batteryCallbackContext = null;

    public CordovaNotify() {}

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        JSONObject message = args.getJSONObject(0);
        Log.e(TAG, action);
        switch (action) {
            case "init":
                init(message,  callbackContext);
                return true;
            case "setting":
                return true;
            case "registry":
                registryEvent(callbackContext);
                return true;
            case "deregistry":
                removeEvent(callbackContext);
                return true;
            default:
                return false;
        }
    }

    private void removeEvent(CallbackContext callbackContext){
        removeBatteryListener();
        this.sendUpdate(new JSONObject(), false); // release status callback in JS side
        this.batteryCallbackContext = null;
        callbackContext.success();
    }

    private void registryEvent(CallbackContext callbackContext) {
        if (this.batteryCallbackContext != null) {
            removeBatteryListener();
        }
        this.batteryCallbackContext = callbackContext;

        // We need to listen to power events to update battery status
        IntentFilter intentFilter = new IntentFilter("android.corodva.messageArrived");
        if (this.receiver == null) {
            this.receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    updateMessageInfo(intent);
                }
            };
            webView.getContext().registerReceiver(this.receiver, intentFilter);
        }

        // Don't return any result now, since status results will be sent when events come in from broadcast receiver
        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);
        callbackContext.sendPluginResult(pluginResult);
    }

    /**
     * 初始化mqttTopic
     * @param message
     */
    private void init(JSONObject message, CallbackContext callback) {
        try {
            String group = message.getString("group");
            String uniqueId = message.getString("unique");
            List<String> tags = new ArrayList<String>();
            tags.add("cordova.domain." + group);
            tags.add("cordova.user." + uniqueId);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    PushManager.setTags(cordova.getContext(), tags);
                }
            }).start();
            callback.success("init success");
        } catch (Exception e) {
            Log.e(TAG, "init error:" + e.getMessage());
            callback.error("init error");
        }
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        String apiKey = preferences.getString("api_key", "");
        PushManager.startWork(webView.getContext(), PushConstants.LOGIN_TYPE_API_KEY,apiKey);
    }


    /**
     * Stop battery receiver.
     */
    public void onDestroy() {
        removeBatteryListener();
    }

    /**
     * Stop battery receiver.
     */
    public void onReset() {
        removeBatteryListener();
    }

    /**
     * Stop the battery receiver and set it to null.
     */
    private void removeBatteryListener() {
        if (this.receiver != null) {
            try {
                webView.getContext().unregisterReceiver(this.receiver);
                this.receiver = null;
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering receiver: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Creates a JSONObject with the current battery information
     *
     * @param intent the current battery information
     * @return a JSONObject containing the battery status information
     */
    private JSONObject getMessageInfo(Intent intent) {
        JSONObject obj = new JSONObject();
        try {
            JSONObject sub = new JSONObject();
            sub.put("title", intent.getStringExtra("title"));
            sub.put("message", intent.getStringExtra("content"));
            sub.put("extra", intent.getStringExtra("extra"));
            obj.put("type", intent.getStringExtra("type"));
            obj.put("msg", sub);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return obj;
    }

    /**
     * Updates the JavaScript side whenever the battery changes
     *
     * @param batteryIntent the current battery information
     * @return
     */
    private void updateMessageInfo(Intent batteryIntent) {
        sendUpdate(this.getMessageInfo(batteryIntent), true);
    }

    /**
     * Create a new plugin result and send it back to JavaScript
     */
    private void sendUpdate(JSONObject info, boolean keepCallback) {
        if (this.batteryCallbackContext != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, info);
            result.setKeepCallback(keepCallback);
            this.batteryCallbackContext.sendPluginResult(result);
        }
    }
}
