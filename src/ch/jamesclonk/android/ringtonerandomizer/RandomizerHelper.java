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

import java.util.ArrayList;
import java.util.Random;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;
//import android.util.Log;

public class RandomizerHelper {
    
    public static final String[] getCurrentRingtone(final Context context) {
        try {
            final Uri currUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE);
            final String[] projection = { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.TITLE };
            final Cursor cursor = context.getContentResolver().query(currUri, projection, null, null, null);
            cursor.moveToFirst();
            
            return new String[] { cursor.getString(0), cursor.getString(1), cursor.getString(2) };
            
        } catch (Exception ex) {
            return new String[] { "", "", "" };
        }
    }
    
    public static void setAsRingtone(final Context context, final Uri uri) {
        try {
            RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, uri);
            
            // update the notification icon (service)
            RandomizerHelper.updateRandomizerService(context);
            
        } catch (Throwable t) {
            // Log.e("RingtoneRandomizer", t.getMessage(), t);
        }
    }
    
    public static void clearRingtone(final Context context, final Uri uri) {
        final SharedPreferences preferences = RandomizerPreferences.getPreferences(context);
        final SharedPreferences.Editor editor = preferences.edit();
        
        // set "IS_RINGTONE" to false for this Uri
        final ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.IS_RINGTONE, false);
        context.getContentResolver().update(uri, values, null, null);
        
        // remove ringtone setting also from preferences
        editor.remove(uri.toString());
        editor.commit();
    }
    
    public static void pickARandomRingtone(final Context context) {
        try {
            final ArrayList<String> ringtoneIds = RandomizerHelper.getRandomizableRingtoneIds(context);
            
            final Random randomGenerator = new Random();
            final int index = randomGenerator.nextInt(ringtoneIds.size());
            
            final Uri uri = Uri.parse(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString() + "/" + ringtoneIds.get(index));
            RandomizerHelper.setAsRingtone(context, uri);
            
        } catch (Throwable t) {
            // Log.e("RingtoneRandomizer", t.getMessage(), t);
        }
    }
    
    public static void updateRandomizerService(final Context context) {
        final SharedPreferences preferences = RandomizerPreferences.getPreferences(context);
        if (preferences.getBoolean("notificationOn", true)) {
            context.startService(new Intent(context.getApplicationContext(), RandomizerNotificationService.class));
        } else {
            context.stopService(new Intent(context.getApplicationContext(), RandomizerNotificationService.class));
        }
    }
    
    public static void showToast(final Context context, final String text) {
        final SharedPreferences preferences = RandomizerPreferences.getPreferences(context);
        if (preferences.getBoolean("messagesOn", true)) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        }
    }
    
    public static void clearRingtoneList(final Context context) {
        final SharedPreferences preferences = RandomizerPreferences.getPreferences(context);
        final SharedPreferences.Editor editor = preferences.edit();
        
        final Cursor cursor = RandomizerHelper.getAllRingtones(context);
        
        while (cursor.moveToNext()) {
            final Uri uri = Uri.parse(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString() + "/" + cursor.getString(0));
            
            // set "IS_RINGTONE" to false for this Uri
            final ContentValues values = new ContentValues();
            values.put(MediaStore.Audio.Media.IS_RINGTONE, false);
            context.getContentResolver().update(uri, values, null, null);
            
            // remove ringtone setting also from preferences
            editor.remove(uri.toString());
        }
        cursor.close();
        editor.commit();
        // Log.i("RingtoneRandomizer",
        // "RandomizerHelper: ringtone list cleared..");
    }
    
    public static final boolean isRandomizable(final Context context, final Uri uri) {
        final SharedPreferences preferences = RandomizerPreferences.getPreferences(context);
        return preferences.getBoolean(uri.toString(), true);
    }
    
    public static final ArrayList<String> getRandomizableRingtoneIds(final Context context) {
        final ArrayList<String> ringtoneIds = new ArrayList<String>();
        
        try {
            final String selection = MediaStore.Audio.Media.IS_RINGTONE + " != 0";
            final String[] projection = { MediaStore.Audio.Media._ID };
            final Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null);
            
            while (cursor.moveToNext()) {
                if (RandomizerHelper.isRandomizable(context, Uri.parse(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString() + "/" + cursor.getString(0)))) {
                    ringtoneIds.add(cursor.getString(0));
                }
            }
            
        } catch (Exception ex) {
            // nothing
        }
        
        return ringtoneIds;
    }
    
    public static final Cursor getAllRingtones(final Context context) {
        try {
            final String selection = MediaStore.Audio.Media.IS_RINGTONE + " != 0";
            final String[] projection = { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM };
            final String order = MediaStore.Audio.Media.TITLE + " ASC";
            final Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, order);
            
            return cursor;
            
        } catch (Exception ex) {
            // nothing
        }
        
        return null;
    }
}
