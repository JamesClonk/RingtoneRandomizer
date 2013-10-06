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

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class RandomizerWidget extends AppWidgetProvider {
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		final ComponentName thisWidget = new ComponentName(context.getApplicationContext(), RandomizerWidget.class);
		final RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.widget);

        final Intent randomizeIntent = new Intent(context, RandomizerRandomizeRingtoneService.class);
        randomizeIntent.putExtra("ch.jamesclonk.android.ringtonerandomizer.showToast", true);
        
        final PendingIntent contentIntent = PendingIntent.getService(context, 0, randomizeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //updateViews.setOnClickPendingIntent(R.id.widget, contentIntent);
        updateViews.setOnClickPendingIntent(R.id.widget_icon, contentIntent);
		
		appWidgetManager.updateAppWidget(thisWidget, updateViews);
	}
}