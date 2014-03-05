package info.justaway;

import android.app.Activity;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(R.color.background);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            PreferenceManager preferenceManager = getPreferenceManager();
            if (preferenceManager == null) {
                return;
            }
            preferenceManager.setSharedPreferencesName("settings");

            addPreferencesFromResource(R.xml.pref_general);

            ListPreference fontSizePreference = (ListPreference) findPreference("font_size");
            if (fontSizePreference == null) {
                return;
            }
            fontSizePreference.setSummary(fontSizePreference.getValue() + " pt");
            fontSizePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    preference.setSummary(newValue + " pt");
                    return true;
                }
            });

            ListPreference longTapPreference = (ListPreference) findPreference("long_tap");
            if (longTapPreference == null) {
                return;
            }
            longTapPreference.setSummary(longTapPreference.getEntry());
            longTapPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    ListPreference listPreference = (ListPreference) preference;
                    int listId = listPreference.findIndexOfValue((String) newValue);
                    CharSequence[] entries;
                    entries = listPreference.getEntries();
                    if (entries != null) {
                        preference.setSummary(entries[listId]);
                    }
                    return true;
                }
            });
        }
    }
}
