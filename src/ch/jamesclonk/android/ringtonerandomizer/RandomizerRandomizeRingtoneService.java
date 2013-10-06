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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
//import android.util.Log;

public class RandomizerRandomizeRingtoneService extends Service {
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        //Log.i("RingtoneRandomizer", "RandomizerRandomizeRingtoneService.onCreate()");
    }
    
    @Override
    public void onDestroy() {
        //Log.i("RingtoneRandomizer", "RandomizerRandomizeRingtoneService.onDestroy()");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.d("RingtoneRandomizer", "RandomizerRandomizeRingtoneService: onStartCommand = startId " + startId + ": " + intent);
        
        final Context context = this.getApplicationContext();
        RandomizerHelper.pickARandomRingtone(context);
        
        final boolean showToast = intent.getBooleanExtra("ch.jamesclonk.android.ringtonerandomizer.showToast", false);
        if (showToast) {
            //Log.d("RingtoneRandomizer", "RandomizerRandomizeRingtoneService: Toast!");
            final String text = context.getString(R.string.toastNewRingtone) + ": " + RandomizerHelper.getCurrentRingtone(context)[2];
            RandomizerHelper.showToast(context, text);
        }
        
        return Service.START_NOT_STICKY;
    }
    
}
