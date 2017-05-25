package io.jrogers.android.wampexample;


import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.WampError;
import ws.wamp.jawampa.transport.SimpleWampWebsocketListener;

public class USSD extends AccessibilityService {

    String phone_num ;

    public USSD ()
    {
        start();
    }

    String Server_Result;
    String USSD_Result;

    private SimpleWampWebsocketListener mServer;
    private WampClient mClient1;
    private WampClient mClient2;

    private Subscription mEventPublication;
    private Subscription mEventSubscription;

    String LOG_TAG ="USSD_Network";

    private static final int EVENT_INTERVAL = 2000;
    private int mLastEventValue = 0;

    public static String TAG = USSD.class.getSimpleName();

    ArrayList<String> full_text =new ArrayList<String>();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG, "onAccessibilityEvent");

        AccessibilityNodeInfo source = event.getSource();
 /*   if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && !event.getClassName().equals("com.android.phone.UssdAlertActivity")) { // android.app.AlertDialog is the standard but not for all phones  */
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && !String.valueOf(event.getClassName()).contains("UssdAlertActivity")) {
            return;
        }
        if(event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && (source == null || !source.getClassName().equals("android.widget.TextView"))) {
            return;
        }
        if(event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && TextUtils.isEmpty(source.getText())) {
            return;
        }

        List<CharSequence> eventText;

        if(event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            eventText = event.getText();
        } else {
            eventText = Collections.singletonList(source.getText());
        }

        String text = processUSSDText(eventText);

        if( TextUtils.isEmpty(text) ) return;

        // Close dialog
        performGlobalAction(GLOBAL_ACTION_BACK); // This works on 4.1+ only

        Log.d(TAG, text);
        USSD_Result= text;

        full_text.add(text);

         start();
        // Handle USSD response here

    }

    private String processUSSDText(List<CharSequence> eventText) {
        for (CharSequence s : eventText) {
            String text = String.valueOf(s);
            // Return text if text is the expected ussd response
            if( true ) {
                return text;
            }
        }
        return null;
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "onServiceConnected");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.flags = AccessibilityServiceInfo.DEFAULT;
        info.packageNames = new String[]{"com.android.phone"};
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        setServiceInfo(info);
    }


    private void start() {
       // String routerUri = "ws://192.168.113.207:9090";
         String routerUri = "ws://172.160.35.146:9090";

        // get system phone number
       // TelephonyManager tMgr = (TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
       // String mPhoneNumber = tMgr.getLine1Number();

     /*   Intent intent =new Intent();
        phone_num=intent.getStringExtra("phone");
        Log.d(TAG, phone_num);*/

        if (mServer == null && TextUtils.isEmpty(routerUri)) {
            return;
        }

        WampClientBuilder builder = new WampClientBuilder();

        // Build two clients
        try {
            builder.withUri(TextUtils.isEmpty(routerUri) ? "ws://localhost:8080/ws1" : routerUri)
                    .withRealm("realm1")
                    .withInfiniteReconnects()
                    .withReconnectInterval(3, TimeUnit.SECONDS);
            mClient1 = builder.build();
            mClient2 = builder.build();
        } catch (WampError e) {
            return;
        }




        mClient2.makeSubscription("test.event", String.class)
                .subscribe(new Action1<String>() {
                               @Override
                               public void call(final String result) {
                                   Server_Result=result;
                                   if (result!=null)
                                   //    String cToSend = "tel:" + Uri.encode("#") + result + Uri.encode("#");
                                   startActivity(new Intent("android.intent.action.CALL",
                                           Uri.parse(result)));
                               }
                           });

        mClient1.open();
        mClient2.open();

        // Publish an event regularly
        mEventPublication = Schedulers.computation().createWorker().schedulePeriodically(new Action0() {
            @Override
            public void call() {
                if (full_text.size()==0)

                mClient1.publish("com.example.hello", "444444" + mLastEventValue);

                else
                    mClient1.publish("com.example.hello", USSD_Result );
                mLastEventValue++;
            }
        }, EVENT_INTERVAL, EVENT_INTERVAL, TimeUnit.MILLISECONDS);
    }
}