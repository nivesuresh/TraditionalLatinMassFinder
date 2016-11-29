package nivesuresh.traditionallatinmassfinder;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.support.design.widget.Snackbar;

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
    //    private ProgressBar progressBar;
    private ProgressDialog progressDialog;

    private TLMAdapter adapter;
    public static List<TLMData> tlmDataList = new ArrayList<>();

    private static String location;

    private LocationManager locationManager;
    private String provider;

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(getActivity(), TLMActivity.LOCATION_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), TLMActivity.LOCATION_PERMISSION)) {
                    showExplanation("Location Permission Required!", "To view a list of churches near you, please turn on location permissions in settings. Otherwise, enter a zip code.", new String[]{TLMActivity.LOCATION_PERMISSION}, TLMActivity.REQUEST_PERMISSION);
                } else {
                    requestPermissions(new String[]{TLMActivity.LOCATION_PERMISSION}, TLMActivity.REQUEST_PERMISSION);
                }
            } else {
                getCurrentLocation();
                executeTLMTask();
            }
        }

        listview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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

        if (provider != null) {
            try {
                locationManager.requestLocationUpdates(provider, 400, 1, this);
            } catch (SecurityException e) {
                Log.d("Security Exception", e.getStackTrace().toString());
            }
        }

        populateListView(tlmDataList);
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
            emptyTextView.setVisibility(View.VISIBLE);
        } else {
            emptyTextView.setVisibility(View.GONE);
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

    private void convertLatLongToLocation(Location location) {
        List<Address> addresses = new ArrayList<>();
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            Log.d("Exception", e.getStackTrace().toString());
        }

        this.location = addresses.get(0).getPostalCode();
    }

    @Override
    public void onLocationChanged(Location location) {
        convertLatLongToLocation(location);
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
    @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == TLMActivity.REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
                executeTLMTask();
            } else {
                Snackbar snackbar = Snackbar.make(getView(), "Turn on Location Permission", Snackbar.LENGTH_LONG);
                snackbar.setAction("Settings", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (getActivity() == null) {
                            return;
                        }
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                        intent.setData(uri);
                        TLMFragment.this.startActivity(intent);
                    }
                });
                snackbar.show();
            }
        }
    }

    private void getCurrentLocation() {
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        //Adapted from: http://bangalorebanerjee.blogspot.com/2014/02/android-location-and-navigate.html
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, true);

        try {
            Location devicelocation = locationManager.getLastKnownLocation(provider);
            convertLatLongToLocation(devicelocation);
        } catch (SecurityException e) {
            Log.d("Security Exception", e.getStackTrace().toString());
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void showExplanation(String title,
                                 String message,
                                 final String[] permission,
                                 final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermissions(permission, permissionRequestCode);
                    }
                });
        builder.create().show();
    }

    @Override
    public void onStop() {
        super.onStop();

//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        SharedPreferences.Editor editor = pref.edit();
//
//        editor.putString(LOCATION, location);
//        editor.commit();
    }
}
