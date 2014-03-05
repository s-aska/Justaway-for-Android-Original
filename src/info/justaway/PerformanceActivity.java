package info.justaway;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;

public class PerformanceActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(R.color.background);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new SettingsFragment()).commit();

        ActionBar actionBar = getActionBar();
        if (actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
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
            if (userIconSizePreference != null) {
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

            ListPreference pageCountPreference = (ListPreference) findPreference("page_count");
            if (pageCountPreference != null) {
                pageCountPreference.setSummary(pageCountPreference.getEntry());
                pageCountPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
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
}
