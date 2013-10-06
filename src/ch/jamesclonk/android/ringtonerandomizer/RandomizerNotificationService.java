/*
    Copyright © 2010 Fabio Berchtold

    This file is part of "Ringtone Randomizer".

    "Ringtone Randomizer". is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    "Ringtone Randomizer". is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with "Ringtone Randomizer"..  If not, see <http://www.gnu.org/licenses/>.
*/

package ch.jamesclonk.android.ringtonerandomizer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
//import android.util.Log;

public class RandomizerNotificationService extends Service {
    
    private NotificationManager notificationManager = null;
    private SharedPreferences preferences = null;
    private String notificationTitle = null;
    private String notificationCurrently = null;
    private String notificationText = null;
    private Notification notification = null;
    private Intent notificationIntent = null;
    private PendingIntent contentIntent = null;
    private Context context = null;
    
    public class RandomizerBinder extends Binder {
        RandomizerNotificationService getService() {
            return RandomizerNotificationService.this;
        }
    }
    
    private final IBinder rBinder = new RandomizerBinder();
    
    @Override
    public IBinder onBind(Intent intent) {
        return rBinder;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        //Log.i("RingtoneRandomizer", "RandomizerNotificationService.onCreate()");

        context = this.getApplicationContext();
        preferences = RandomizerPreferences.getPreferences(this);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        
        this.initNotification();
    }
    
    @Override
    public void onDestroy() {
        //Log.i("RingtoneRandomizer", "RandomizerNotificationService.onDestroy()");
        notificationManager.cancelAll(); 
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.d("RingtoneRandomizer", "RandomizerNotificationService: onStartCommand = startId " + startId + ": " + intent);
        this.updateNotification();
        
        return Service.START_STICKY;
    }
    
    private void initNotification() {
        //Log.d("RingtoneRandomizer", "RandomizerNotificationService.initNotification()");
        
        final int icon = R.drawable.notification;
        
        notificationTitle = this.getText(R.string.appName).toString();
        notificationCurrently = this.getText(R.string.notificationCurrently).toString();
        notificationText = notificationCurrently + ": " + RandomizerHelper.getCurrentRingtone(context)[2];
        
        notification = new Notification(icon, null /* notificationTitle */, System.currentTimeMillis());
        notification.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        
        notificationIntent = new Intent(context, RandomizerRandomizeRingtoneService.class);
        notificationIntent.putExtra("ch.jamesclonk.android.ringtonerandomizer.showToast", true);
        
        contentIntent = PendingIntent.getService(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    
    private void updateNotification() {
        //Log.d("RingtoneRandomizer", "RandomizerNotificationService.updateNotification()");
        
        // only show icon if turned on in preferences
        if (preferences.getBoolean("notificationOn", true)) {
            notificationText = notificationCurrently + ": " + RandomizerHelper.getCurrentRingtone(context)[2];
            
            notification.when = System.currentTimeMillis();
            notification.setLatestEventInfo(context, notificationTitle, notificationText, contentIntent);
            
            // finally, show the notification
            notificationManager.notify(1, notification);
            
        } else {
            // clear notification
            notificationManager.cancelAll();
        }
    }
    
}
