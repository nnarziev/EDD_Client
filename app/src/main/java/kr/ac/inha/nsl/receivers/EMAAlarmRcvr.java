package kr.ac.inha.nsl.receivers;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;

import kr.ac.inha.nsl.EMAActivity;
import kr.ac.inha.nsl.R;
import kr.ac.inha.nsl.services.CustomSensorsService;
import kr.ac.inha.nsl.services.SendGPSStats;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static kr.ac.inha.nsl.EMAActivity.EMA_NOTIF_HOURS;
import static kr.ac.inha.nsl.services.CustomSensorsService.EMA_NOTIFICATION_ID;
import static kr.ac.inha.nsl.services.CustomSensorsService.EMA_NOTIF_EXPIRE;
import static kr.ac.inha.nsl.services.CustomSensorsService.SERVICE_START_X_MIN_BEFORE_EMA;

public class EMAAlarmRcvr extends BroadcastReceiver {
    private static final String TAG = EMAAlarmRcvr.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences loginPrefs = context.getSharedPreferences("UserLogin", MODE_PRIVATE);
        SharedPreferences.Editor editor = loginPrefs.edit();
        editor.putBoolean("ema_btn_make_visible", true);
        editor.apply();

        int ema_order = intent.getIntExtra("ema_order", -1);
        sendNotification(context, ema_order);
        Intent gpsIntent = new Intent(context, SendGPSStats.class);
        gpsIntent.putExtra("ema_order", ema_order);
        context.startService(gpsIntent);

        setAlarams(context, ema_order);
    }

    private void sendNotification(Context context, int ema_order) {
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(context, EMAActivity.class);
        Log.e(TAG, "sendNotification: " + ema_order);
        notificationIntent.putExtra("ema_order", ema_order);
        //PendingIntent pendingIntent = PendingIntent.getActivities(CustomSensorsService.this, 0, new Intent[]{notificationIntent}, 0);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String channelId = context.getString(R.string.notif_channel_id);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context.getApplicationContext(), channelId);
        builder.setContentTitle(context.getString(R.string.app_name))
                .setTimeoutAfter(1000 * EMA_NOTIF_EXPIRE)
                .setContentText(context.getString(R.string.daily_notif_text))
                .setTicker("New Message Alert!")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher_no_bg)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, context.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        final Notification notification = builder.build();
        notificationManager.notify(EMA_NOTIFICATION_ID, notification);
    }

    public void setAlarams(Context context, int ema_order) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        Intent intent1 = new Intent(context, EMAAlarmRcvr.class);
        intent1.putExtra("ema_order", 1);
        Intent intent2 = new Intent(context, EMAAlarmRcvr.class);
        intent2.putExtra("ema_order", 2);
        Intent intent3 = new Intent(context, EMAAlarmRcvr.class);
        intent3.putExtra("ema_order", 3);
        Intent intent4 = new Intent(context, EMAAlarmRcvr.class);
        intent4.putExtra("ema_order", 4);

        PendingIntent pendingIntent1 = PendingIntent.getBroadcast(context, 1, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, 2, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingIntent3 = PendingIntent.getBroadcast(context, 3, intent3, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingIntent4 = PendingIntent.getBroadcast(context, 4, intent4, PendingIntent.FLAG_UPDATE_CURRENT);
        if (alarmManager == null)
            return;

        Calendar firingCal1 = Calendar.getInstance();
        firingCal1.set(Calendar.HOUR_OF_DAY, EMA_NOTIF_HOURS[0]); // at 10am
        firingCal1.set(Calendar.MINUTE, 0); // Particular minute
        firingCal1.set(Calendar.SECOND, 0); // particular second
        firingCal1.set(Calendar.MILLISECOND, 0); // particular second

        Calendar firingCal2 = Calendar.getInstance();
        firingCal2.set(Calendar.HOUR_OF_DAY, EMA_NOTIF_HOURS[1]); // at 2pm
        firingCal2.set(Calendar.MINUTE, 0); // Particular minute
        firingCal2.set(Calendar.SECOND, 0); // particular second
        firingCal2.set(Calendar.MILLISECOND, 0); // particular second

        Calendar firingCal3 = Calendar.getInstance();
        firingCal3.set(Calendar.HOUR_OF_DAY, EMA_NOTIF_HOURS[2]); // at 6pm
        firingCal3.set(Calendar.MINUTE, 0); // Particular minute
        firingCal3.set(Calendar.SECOND, 0); // particular second
        firingCal3.set(Calendar.MILLISECOND, 0); // particular second

        Calendar firingCal4 = Calendar.getInstance();
        firingCal4.set(Calendar.HOUR_OF_DAY, EMA_NOTIF_HOURS[3]); // at 10pm
        firingCal4.set(Calendar.MINUTE, 0); // Particular minute
        firingCal4.set(Calendar.SECOND, 0); // particular second
        firingCal4.set(Calendar.MILLISECOND, 0); // particular second

        if (ema_order == 1)
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, firingCal2.getTimeInMillis(), 30000, pendingIntent2); //set from today
        else if (ema_order == 2)
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, firingCal3.getTimeInMillis(), 30000, pendingIntent3); //set from today
        else if (ema_order == 3)
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, firingCal4.getTimeInMillis(), 30000, pendingIntent4); //set from today
        else if (ema_order == 4) {
            firingCal1.add(Calendar.DAY_OF_MONTH, 1);
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, firingCal1.getTimeInMillis(), 30000, pendingIntent1); //set from today
        }
    }
}

