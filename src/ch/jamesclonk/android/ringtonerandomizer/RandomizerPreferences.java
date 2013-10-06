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

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class RandomizerPreferences extends PreferenceActivity {
	
	  @Override
	  protected void onCreate(Bundle bundle) {
	    super.onCreate(bundle);
	    this.addPreferencesFromResource(R.xml.preferences);
	  }
	  
	  public static final SharedPreferences getPreferences(final ContextWrapper ctx) {
		  return RandomizerPreferences.getPreferences(ctx.getBaseContext());
	  }
	  
	  public static final SharedPreferences getPreferences(final Context ctx) {
		  return ctx.getSharedPreferences(ctx.getPackageName() + "_preferences", MODE_PRIVATE);
	  }	 
}
