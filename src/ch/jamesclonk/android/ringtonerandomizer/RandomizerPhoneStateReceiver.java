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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

public class RandomizerPhoneStateReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(final Context context, Intent intent) {
        // check if randomizer is on before doing anything
        final SharedPreferences preferences = RandomizerPreferences.getPreferences(context);
        if (preferences.getBoolean("randomizerOn", true)) {
            // get phone state and do something if it is "IDLE" (which it is AFTER a call)
            final String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            if (state.equals("IDLE")) {
                // start the randomizer service, but without a toast
                final Intent randomizeIntent = new Intent(context, RandomizerRandomizeRingtoneService.class);
                randomizeIntent.putExtra("ch.jamesclonk.android.ringtonerandomizer.showToast", false);
                context.startService(randomizeIntent);
            }
        }
    }
}
