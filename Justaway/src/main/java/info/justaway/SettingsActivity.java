package info.justaway;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import info.justaway.util.ThemeUtil;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtil.setTheme(this);
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new SettingsFragment()).commit();

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
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

    public void applyTheme() {
        setResult(RESULT_OK);
        finish();
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

            ListPreference themePreference = (ListPreference) findPreference("themeName");
            if (themePreference == null) {
                return;
            }
            themePreference.setSummary(themePreference.getEntry());
            themePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    ListPreference listPreference = (ListPreference) preference;
                    int listId = listPreference.findIndexOfValue((String) newValue);
                    CharSequence[] entries;
                    entries = listPreference.getEntries();
                    if (entries != null) {
                        preference.setSummary(entries[listId]);
                    }
                    FragmentManager fragmentManager = getFragmentManager();
                    if (fragmentManager != null) {
                        new ThemeSwitchDialogFragment().show(fragmentManager, "dialog");
                    }
                    return true;
                }
            });
        }
    }

    public static final class ThemeSwitchDialogFragment extends DialogFragment {

        private SettingsActivity mActivity;

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);

            mActivity = (SettingsActivity) activity;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setMessage(R.string.confirm_theme_apply_finish);
            builder.setPositiveButton(getString(R.string.button_yes),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mActivity.applyTheme();
                            dismiss();
                        }
                    }
            );
            builder.setNegativeButton(getString(R.string.button_no),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                        }
                    }
            );
            return builder.create();
        }
    }
}
