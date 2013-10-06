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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.view.Menu;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;

public class RandomizerControlPanel extends Activity {
    
    static final private int RANDOMIZE_ID = Menu.FIRST;
    static final private int CLEAR_ID = Menu.FIRST + 1;
    static final private int EXIT_ID = Menu.FIRST + 2;
    
    static final private int PREVIEW_ID = Menu.FIRST;
    static final private int SET_ID = Menu.FIRST + 1;
    static final private int REMOVE_ID = Menu.FIRST + 2;
    
    private Context context = null;
    private RandomizerControlPanel rcp = null;
    private SharedPreferences preferences = null;
    private CheckBox randomizerCheckbox = null;
    private CheckBox notifcationCheckbox = null;
    private CheckBox messagesCheckbox = null;
    private String sArtist = null;
    private String sAlbum = null;
    private ListView ringtoneList = null;
    private RingtoneAdapter adapter = null;
    private MediaPlayer mediaPlayer = null;
    private ProgressDialog progressDialog = null;
    private String progressTitle = null;
    private String progressText = null;
    private String sMenuTitle = null;
    private String sPreview = null;
    private String sSet = null;
    private String sRemove = null;
    
    @Override
    protected void onResume() {
        super.onResume();
        // create & fill the ringtone listview
        this.createRingtoneList();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.stop();
        mediaPlayer.reset();
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.main);
        
        context = this.getApplicationContext();
        rcp = this;
        preferences = RandomizerPreferences.getPreferences(this);
        mediaPlayer = new MediaPlayer();
        
        sArtist = this.getString(R.string.artist);
        sAlbum = this.getString(R.string.album);
        
        sMenuTitle = this.getString(R.string.menuTitle);
        sPreview = this.getString(R.string.preview);
        sSet = this.getString(R.string.set);
        sRemove = this.getString(R.string.remove);
        
        progressTitle = this.getString(R.string.progressTitle);
        progressText = this.getString(R.string.progressText);
        
        this.findViewById(R.id.selectAll).setOnClickListener(mSelectAllListener);
        this.findViewById(R.id.deselectAll).setOnClickListener(mDeselectAllListener);
        
        randomizerCheckbox = (CheckBox) this.findViewById(R.id.randomizerCheckbox);
        notifcationCheckbox = (CheckBox) this.findViewById(R.id.notifcationCheckbox);
        messagesCheckbox = (CheckBox) this.findViewById(R.id.messagesCheckbox);
        randomizerCheckbox.setChecked(this.isRandomizerOn());
        notifcationCheckbox.setChecked(this.isNotificationOn());
        messagesCheckbox.setChecked(this.isMessagesOn());
        randomizerCheckbox.setOnClickListener(mRandomizerCheckboxListener);
        notifcationCheckbox.setOnClickListener(mNotificationCheckboxListener);
        messagesCheckbox.setOnClickListener(mMessagesCheckboxListener);
        
        ringtoneList = (ListView) findViewById(R.id.ringtoneList);
        
        // starts/stops the notification service
        RandomizerHelper.updateRandomizerService(context);
    }
    
    private void createRingtoneList() {
        progressDialog = ProgressDialog.show(this, progressTitle, progressText, true, false);
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                
                final Cursor cursor = RandomizerHelper.getAllRingtones(context);
                adapter = new RingtoneAdapter(rcp, R.layout.ringtone_entry, cursor);
                
                handler.sendEmptyMessage(0);
            }
        });
        thread.start();
    }
    
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
            ringtoneList.setAdapter(adapter);
            // register our lovely contextmenu for this listview.
            rcp.registerForContextMenu(ringtoneList);
        }
    };
    
    private void selectAllRingtones() {
        final SharedPreferences.Editor editor = preferences.edit();
        final Cursor cursor = RandomizerHelper.getAllRingtones(context);
        while (cursor.moveToNext()) {
            final Uri uri = Uri.parse(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString() + "/" + cursor.getString(0));
            // remove ringtone setting from preferences
            editor.remove(uri.toString());
        }
        editor.commit();
        
        for (ViewHolder viewholder : adapter.holderList) {
            viewholder.cb.setChecked(true);
        }
    }
    
    private void deselectAllRingtones() {
        final SharedPreferences.Editor editor = preferences.edit();
        final Cursor cursor = RandomizerHelper.getAllRingtones(context);
        while (cursor.moveToNext()) {
            final Uri uri = Uri.parse(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString() + "/" + cursor.getString(0));
            // add ringtone setting to preferences
            editor.putBoolean(uri.toString(), false);
        }
        editor.commit();
        
        for (ViewHolder viewholder : adapter.holderList) {
            viewholder.cb.setChecked(false);
        }
    }
    
    // this class is used to hold and speed up access to the listview and its
    // components
    final class ViewHolder {
        LinearLayout ll;
        CheckBox cb;
        TextView t1;
        TextView t2;
        TextView t3;
    }
    
    private class RingtoneAdapter extends CursorAdapter {
        
        private Cursor cursor = null;
        private LayoutInflater layoutInflater = null;
        public ArrayList<ViewHolder> holderList = new ArrayList<ViewHolder>();
        
        public RingtoneAdapter(Context context, int textViewResourceId, Cursor cursor) {
            super(context, cursor);
            this.cursor = cursor;
            layoutInflater = LayoutInflater.from(context);
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // the view will be stored in this ViewHolder
            ViewHolder viewHolder = null;
            
            if (convertView == null) {
                // (LayoutInflater)
                // getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.ringtone_entry, null);
                
                viewHolder = new ViewHolder();
                viewHolder.ll = (LinearLayout) convertView.findViewById(R.id.ringtoneLayout);
                viewHolder.cb = (CheckBox) convertView.findViewById(R.id.ringtoneCheckbox);
                viewHolder.t1 = (TextView) convertView.findViewById(R.id.ringtoneTitle);
                viewHolder.t2 = (TextView) convertView.findViewById(R.id.ringtoneArtist);
                viewHolder.t3 = (TextView) convertView.findViewById(R.id.ringtoneAlbum);
                
                // add the ViewHolder to the ArrayList, for later use
                holderList.add(viewHolder);
                
                // store ViewHolder
                convertView.setTag(viewHolder);
            } else {
                // get back Viewholder
                viewHolder = (ViewHolder) convertView.getTag();
            }
            
            cursor.moveToPosition(position);
            final Uri uri = Uri.parse(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString() + "/" + cursor.getString(0));
            
            // I have no f***ing idea why I have to do this here, but otherwise
            // the contextmenu won't show up!
            // I'm probably doing something wrong, really really wrong! ;-)
            convertView.setOnCreateContextMenuListener(null);
            
            viewHolder.cb.setChecked(RandomizerHelper.isRandomizable(context, uri));
            viewHolder.cb.setOnClickListener(mRingtoneCheckBoxListener);
            viewHolder.cb.setTag(uri);
            
            viewHolder.t1.setText(cursor.getString(2));
            
            final String artist = cursor.getString(3);
            final String album = cursor.getString(4);
            // only show artist || album if not null/unknown
            if (artist != null && !artist.equals("<unknown>")) {
                viewHolder.t2.setVisibility(View.VISIBLE);
                viewHolder.t2.setText(sArtist + ": " + artist);
            }
            if (album != null && !album.equals("<unknown>")) {
                viewHolder.t3.setVisibility(View.VISIBLE);
                viewHolder.t3.setText(sAlbum + ": " + album);
            }
            
            return convertView;
        }
        
        public OnClickListener mRingtoneCheckBoxListener = new OnClickListener() {
            public void onClick(View v) {
                final CheckBox cb = (CheckBox) v;
                final Uri uri = (Uri) cb.getTag();
                
                // since the default value is "true", we only have to set
                // "false" and can remove the entry otherwise
                final SharedPreferences.Editor editor = preferences.edit();
                if (!cb.isChecked()) {
                    editor.putBoolean(uri.toString(), false);
                } else {
                    editor.remove(uri.toString());
                }
                editor.commit();
            }
        };
        
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
        }
        
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return null;
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.reset();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        menu.add(Menu.NONE, RANDOMIZE_ID, Menu.NONE, R.string.randomize).setShortcut('0', 'r').setIcon(R.drawable.menu_randomize);
        menu.add(Menu.NONE, CLEAR_ID, Menu.NONE, R.string.clear).setShortcut('1', 'c').setIcon(R.drawable.menu_clear);
        menu.add(Menu.NONE, EXIT_ID, Menu.NONE, R.string.exit).setShortcut('2', 'q').setIcon(R.drawable.menu_close);
        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case RANDOMIZE_ID:
                this.pickARandomRingtone();
                return true;
            case CLEAR_ID:
                this.clearRingtoneList();
                return true;
            case EXIT_ID:
                finish();
                return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle(sMenuTitle);
        menu.add(Menu.NONE, PREVIEW_ID, PREVIEW_ID, sPreview);
        menu.add(Menu.NONE, SET_ID, SET_ID, sSet);
        menu.add(Menu.NONE, REMOVE_ID, REMOVE_ID, sRemove);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
        final Cursor cursor = (Cursor) adapter.getItem(menuInfo.position);
        final Uri uri = Uri.parse(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString() + "/" + cursor.getString(0));
        
        try {
            switch (item.getItemId()) {
                case PREVIEW_ID:
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                    }
                    mediaPlayer.setDataSource(context, uri);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    return true;
                case SET_ID:
                    RandomizerHelper.setAsRingtone(context, uri);
                    return true;
                case REMOVE_ID:
                    RandomizerHelper.clearRingtone(context, uri);
                    // menuInfo.targetView.setVisibility(View.GONE);
                    // just setting it to GONE seems not enough, doesn't properly "redraw".
                    // have to recreate the ringtone listview.
                    this.createRingtoneList();
                    return true;
            }
        } catch (Exception ex) {
            // nothing
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    OnClickListener mSelectAllListener = new OnClickListener() {
        public void onClick(View v) {
            rcp.selectAllRingtones();
        }
    };
    
    OnClickListener mDeselectAllListener = new OnClickListener() {
        public void onClick(View v) {
            rcp.deselectAllRingtones();
        }
    };
    
    OnClickListener mRandomizerCheckboxListener = new OnClickListener() {
        public void onClick(View v) {
            rcp.setRandomizer(randomizerCheckbox.isChecked());
        }
    };
    
    OnClickListener mNotificationCheckboxListener = new OnClickListener() {
        public void onClick(View v) {
            rcp.setNotification(notifcationCheckbox.isChecked());
            
            // starts/stops the notification service
            RandomizerHelper.updateRandomizerService(context);
        }
    };
    
    OnClickListener mMessagesCheckboxListener = new OnClickListener() {
        public void onClick(View v) {
            rcp.setMessages(messagesCheckbox.isChecked());
        }
    };
    
    private void pickARandomRingtone() {
        // start the randomizer service with a toast
        final Intent randomizeIntent = new Intent(context, RandomizerRandomizeRingtoneService.class);
        randomizeIntent.putExtra("ch.jamesclonk.android.ringtonerandomizer.showToast", true);
        this.startService(randomizeIntent);
    }
    
    private void clearRingtoneList() {
        // ask for confirmation, otherwise the user might clear the ringtone
        // list by accident!
        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.clearTitle).setMessage(R.string.clearQuestion)
                .setPositiveButton(R.string.clearYes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // clear all custom ringtones from the list
                        RandomizerHelper.clearRingtoneList(context);
                        // recreate the listview
                        rcp.createRingtoneList();
                    }
                }).setNegativeButton(R.string.clearNo, null).show();
    }
    
    private void setRandomizer(final boolean status) {
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("randomizerOn", status);
        editor.commit();
    }
    
    private final boolean isRandomizerOn() {
        return preferences.getBoolean("randomizerOn", true);
    }
    
    private void setNotification(final boolean status) {
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("notificationOn", status);
        editor.commit();
    }
    
    private final boolean isNotificationOn() {
        return preferences.getBoolean("notificationOn", true);
    }
    
    private void setMessages(final boolean status) {
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("messagesOn", status);
        editor.commit();
    }
    
    private final boolean isMessagesOn() {
        return preferences.getBoolean("messagesOn", true);
    }
}
