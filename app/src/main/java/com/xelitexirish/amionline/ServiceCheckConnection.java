package com.xelitexirish.amionline;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.ConditionVariable;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.annotation.Nullable;

public class ServiceCheckConnection extends Service{

    private static int MOOD_NOTIFICATIONS = 5356346;
    private static boolean isNotificationRunning = false;
    private ConditionVariable conditionVariable;
    private NotificationManager notificationManager;
    int min_in_mills = 60000;

    private boolean previousConnectionState;
    public static String TAG_PREVIOUS_STATE = "previous_connection_state";

    @Override
    public void onCreate() {
        super.onCreate();

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Thread notifyingThread = new Thread(null, mTask, "UpdatingConnectionState");
        conditionVariable = new ConditionVariable(false);
        notifyingThread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.previousConnectionState = intent.getExtras().getBoolean(TAG_PREVIOUS_STATE);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        notificationManager.cancel(MOOD_NOTIFICATIONS);
        isNotificationRunning = false;
        conditionVariable.open();
    }

    private Runnable mTask = new Runnable() {
        @Override
        public void run() {

            // Check connection here
            if (MainActivity.hasSimpleNetworkConnection(ServiceCheckConnection.this)){
                if (previousConnectionState == false){
                    showNotification(true);
                }
            }else {
                if (previousConnectionState == true){
                    showNotification(false);
                }
            }

            if (conditionVariable.block(min_in_mills)) return;

            // Done
            ServiceCheckConnection.this.stopSelf();
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void showNotification(boolean state){
        String content;

        if(state) {
            content = "You are now connected to the internet!";
        }else {
            content = "You have just lost connection to the internet";
        }

        Notification.Builder notification = new Notification.Builder(this)
                .setWhen(System.currentTimeMillis())
                .setContentText(content)
                .setSmallIcon(R.mipmap.ic_launcher);

        notification.setPriority(Notification.PRIORITY_HIGH);
        notificationManager.notify(MOOD_NOTIFICATIONS, notification.build());
        isNotificationRunning = true;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new Binder() {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    };

    public static boolean isNotificationRunning(){
        return isNotificationRunning;
    }
}
