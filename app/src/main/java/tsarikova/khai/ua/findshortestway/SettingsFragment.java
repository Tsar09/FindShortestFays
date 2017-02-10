package tsarikova.khai.ua.findshortestway;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by ira on 04.02.2017.
 */

public class SettingsFragment extends PreferenceFragment{

    public SettingsFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        final ListPreference lp = setListPreferenceDate((ListPreference) findPreference("list"), getActivity());

        lp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                setListPreferenceDate(lp, getActivity());
                return true;
            }
        });

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private ListPreference setListPreferenceDate(ListPreference lp, Activity mActivity){
        if(lp == null){
            lp = new ListPreference(mActivity);
        }
        lp.setEntries(Transport.names());
        lp.setDefaultValue("WALKING");
        lp.setEntryValues(Transport.names());
        lp.setTitle("Transport");

        return  lp;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            startActivity(new Intent(getActivity(), SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
