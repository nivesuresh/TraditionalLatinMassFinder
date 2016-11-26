package nivesuresh.traditionallatinmassfinder;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by nivesuresh on 7/17/16.
 */
public class TLMFragment extends Fragment implements TLMTask.AsyncListener, LocationListener {

    private ListView listview;
    private TextView emptyTextView;
    private TextView locationTextView;

    private TLMAdapter adapter;
    public static List<TLMData> tlmDataList = new ArrayList<>();

    private static String location;
    private LocationManager locationManager;

    public static final String LOCATION = "location";
    public static final String TLM_DATA_LIST = "tlmDataList";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        listview = (ListView) rootView.findViewById(R.id.listview);
        emptyTextView = (TextView) rootView.findViewById(R.id.empty_tv);
        locationTextView = (TextView) rootView.findViewById(R.id.location_tv);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        } catch(SecurityException e) {
            Log.d("Error", e.getStackTrace().toString());
        }


        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        location = sharedPref.getString(LOCATION, "61704");

        listview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        executeTLMTask();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        final MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                location = query;
                tlmDataList.clear();
                executeTLMTask();
                item.collapseActionView();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.i("well", " this worked");
                return false;
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);

        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_about) {
            Intent intent = new Intent(getActivity(), TLMAboutActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_news) {
            Intent intent = new Intent(getActivity(), TLMNewsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        populateListView(tlmDataList);
    }

    /**
     * A helper function for the Alert dialog to pop up when
     * "Search" button on ActionBar is clicked
     */
    private void searchListener() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Enter Location (Zipcode or Place)");

        final EditText input = new EditText(getActivity());
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                location = input.getText().toString();
                tlmDataList.clear();
                executeTLMTask();
                //populateListView();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    /**
     * A helper function to populate the listview with data
     * from the Asynctask (based on the zip code)
     */
    private void executeTLMTask() {

        if (tlmDataList.isEmpty()) {
            try {
                new TLMTask(getActivity().getApplicationContext(), this).execute(location);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void populateListView(List<TLMData> tlmData) {
        tlmDataList = tlmData;
        if (tlmDataList.isEmpty()) {
            //TODO: throw up a progress bar
        }

        if (tlmDataList != null && !tlmDataList.isEmpty()) {
            adapter = new TLMAdapter(getActivity(), tlmDataList);

            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TLMData data = (TLMData) listview.getItemAtPosition(position);
                    Intent intent = new Intent(getActivity(), TLMDetailActivity.class);
                    intent.putExtra(TLM_DATA_LIST, data);
                    intent.putExtra(LOCATION, location);
                    startActivity(intent);
                }
            });

            listview.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onComplete(List<TLMData> tlmData) {
        populateListView(tlmData);
    }

    @Override
    public void onLocationChanged(Location location) {

        List<Address> addresses = new ArrayList<>();
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch(IOException e) {
            Log.d("Exception", e.getStackTrace().toString());
        }

        this.location = addresses.get(0).getFeatureName();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onStop() {
        super.onStop();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = pref.edit();

        editor.putString(LOCATION, location);
        editor.commit();
    }


}
