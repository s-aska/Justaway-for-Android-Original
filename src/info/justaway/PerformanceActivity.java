package info.justaway;

import android.app.Activity;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class PerformanceActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

            addPreferencesFromResource(R.xml.pref_performance);

            ListPreference userIconSizePreference = (ListPreference) findPreference("user_icon_size");
            if (userIconSizePreference == null) {
                return;
            }
            userIconSizePreference.setSummary(userIconSizePreference.getEntry());
            userIconSizePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
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
