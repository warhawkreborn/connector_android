package it.thalhammer.warhawkreborn.fragment;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import it.thalhammer.warhawkreborn.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}