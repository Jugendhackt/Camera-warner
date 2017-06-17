package org.jugendhackt.camera_warner;

/**
 * Created by Martin Goetze on 10.06.2017.
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.support.v14.preference.MultiSelectListPreference;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.widget.Toast;

/**
 * This Fragment class implements the setting's custom logic (e.g. check data for validity and set preference summaries)
 */
public class SettingsFragment extends PreferenceFragmentCompat
        implements Preference.OnPreferenceChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //register to receive callbacks if any SharedPreference changes so that the summary can be updated
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {

        // Add preferences, defined in the XML file in res->xml->preferences.xml
        addPreferencesFromResource(R.xml.preferences);

        //set the summaries for the different preferences for the first time
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen prefScreen = getPreferenceScreen();
        int count = prefScreen.getPreferenceCount();
        for (int i = 0; i < count; i++) {
            Preference p = prefScreen.getPreference(i);
            // You don't need to set up preference summaries for checkbox preferences because
            // they are already set up in xml using summaryOff and summary On
            if (!(p instanceof CheckBoxPreference) && !(p instanceof MultiSelectListPreference)) {
                String value = sharedPreferences.getString(p.getKey(), "");
                setPreferenceSummary(p, value);
            }
        }

        //attach listener to check for changes in the radius preference
        //because it should only contain only doubles and the code has to enforce this
        Preference preference = findPreference(getString(R.string.pref_radius_key));
        preference.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //get the preference that was changed
        Preference preference = findPreference(key);

        // Updates the summary for the preference if is not null
        if (null != preference) {
            // You don't need to set up preference summaries for checkbox preferences because
            // they are already set up in xml using summaryOff and summary On
            if (!(preference instanceof CheckBoxPreference) && !(preference instanceof MultiSelectListPreference)) {
                String value = sharedPreferences.getString(preference.getKey(), "");
                setPreferenceSummary(preference, value);
            }
        }
    }

    /**
     * Updates the summary for the preference
     *
     * @param preference The preference to be updated
     * @param value      The value that the preference was updated to
     */
    private void setPreferenceSummary(Preference preference, String value) {
        if (preference instanceof ListPreference) {
            // For list preferences, figure out the label of the selected value
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(value);
            if (prefIndex >= 0) {
                // Set the summary to that label
                listPreference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else if (preference instanceof EditTextPreference) {
            if (preference.getKey().equals(getString(R.string.pref_radius_key))) {
                preference.setSummary(value + "m");
            } else {
                // For EditTextPreferences, set the summary to the value's simple string representation.
                preference.setSummary(value);
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // In this context, we're using the onPreferenceChange listener for checking whether the
        // radius setting was set to a valid value.

        //the error message that will be shown if an invalid value is entered
        Toast error = Toast.makeText(getContext(), "Please select a number greater than 0", Toast.LENGTH_SHORT);

        // Double check that the preference is the radius preference
        String sizeKey = getString(R.string.pref_radius_key);
        if (preference.getKey().equals(sizeKey)) {
            String radius = (String) newValue;
            try {
                double size = Double.parseDouble(radius);
                // If the number is outside of the acceptable range, show an error.
                if (size <= 0) {
                    error.show();
                    return false;
                }
            } catch (NumberFormatException nfe) {
                // If whatever the user entered can't be parsed to a number, show an error
                error.show();
                return false;
            }
        }

        //numbers seems to be valid; accept it
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //properly unregister the callback
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
