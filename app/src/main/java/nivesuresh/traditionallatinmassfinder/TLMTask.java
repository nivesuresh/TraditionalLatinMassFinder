package nivesuresh.traditionallatinmassfinder;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by nivesuresh on 6/24/16.
 */
public class TLMTask extends AsyncTask<String, Void, List<TLMData>> {

    public String zipCode;
    public Context context;

    AsyncListener listener;

    public TLMTask(Context context, AsyncListener listener) {
        if(listener == null) throw new NullPointerException("Listener can't be null");
        this.setListener(listener);
        this.context = context;
    }

    public interface AsyncListener {
        public void onComplete(List<TLMData> tlmData);
    }

    public void setListener(AsyncListener listener) {
        this.listener = listener;
    }

    @Override
    protected List<TLMData> doInBackground(String... params) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        zipCode = params[0];

        String tlmJsonStr;
        List<TLMData> results = new ArrayList<>();

        try {
            final String TLM_URL = "http://www.leetaurapps.com/LatinMassInfo.json";

            URL url = new URL(TLM_URL);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            tlmJsonStr = buffer.toString();

            if (!tlmJsonStr.contains("Error")) {
                results = getTLMDataFromJson(tlmJsonStr);
            } else results = null;

        } catch (Exception e) {
            Log.e("TLMTask", "Error ", e);
            return null;

        } finally{
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("TLMTask", "Error closing stream", e);
                }
            }
        }

        return results;
    }

    @Override
    protected void onPostExecute(List<TLMData> tlmData) {
        List<TLMData> sortedTlmData = getClosest30(tlmData);
        listener.onComplete(sortedTlmData);
    }

    public List<TLMData> getTLMDataFromJson(String tlmJsonStr)
            throws JSONException {

        final String LIST = "list";
        final String AFFILIATION = "Affiliation";
        final String CHURCH_NAME = "ChurchName";
        final String COUNTRY = "Country";
        final String EMAIL = "Email";
        final String LATITUDE = "Latitude";
        final String LONGITUDE = "Longitude";
        final String PHONE = "Phone";
        final String STATE_PROVINCE = "StateProvince";
        final String STREET_ADDRESS = "StreetAddress";
        final String TIMES = "Times";
        final String TOWN = "Town";
        final String WEBSITE = "Website";
        final String ZIP_POSTAL_CODE = "ZipPostalCode";

        JSONArray tlmJsonArray = new JSONArray(tlmJsonStr);
        List<TLMData> resultData = new ArrayList<>();

        for(int i = 0; i < tlmJsonArray.length(); i++) {
            JSONObject tlmJsonObject = tlmJsonArray.getJSONObject(i);
            String affiliation = tlmJsonObject.getString(AFFILIATION);
            String churchName = tlmJsonObject.getString(CHURCH_NAME);
            String country = tlmJsonObject.getString(COUNTRY);
            String email = tlmJsonObject.getString(EMAIL);
            String latitude = tlmJsonObject.getString(LATITUDE);
            String longitude = tlmJsonObject.getString(LONGITUDE);
            String phone = tlmJsonObject.getString(PHONE);
            String stateProvince = tlmJsonObject.getString(STATE_PROVINCE);
            String streetAddress = tlmJsonObject.getString(STREET_ADDRESS);
            String times = tlmJsonObject.getString(TIMES);
            String town = tlmJsonObject.getString(TOWN);
            String website = tlmJsonObject.getString(WEBSITE);
            String zipPostalCode = tlmJsonObject.getString(ZIP_POSTAL_CODE);

            TLMData tlmData = new TLMData(affiliation, churchName, country, email, latitude, longitude, phone,
                    stateProvince, streetAddress, times, town, website, zipPostalCode);

            resultData.add(i, tlmData);
        }

        return resultData;
    }

    public List<TLMData> getClosest30(List<TLMData> totalTlmData) {
        List<TLMData> distanceInTlmData = calculateDistance((ArrayList<TLMData>)totalTlmData);
        List<TLMData> newTotalTlmData = sortLocations((ArrayList<TLMData>)distanceInTlmData);
        List<TLMData> closest30 = new ArrayList<>();

        for(int i = 0; i < 30; i++) {
            closest30.add(newTotalTlmData.get(i));
        }
        return closest30;
    }

    private List<TLMData> calculateDistance(ArrayList<TLMData> tlmDataList) {
        final ArrayList<Double> myLatLng = getLatLngFromZip(context);

        Location currLocation = new Location("Curr Zip");
        currLocation.setLatitude(myLatLng.get(0));
        currLocation.setLongitude(myLatLng.get(1));

        for(TLMData data : tlmDataList) {
            Location tempLocation = new Location("Location");
            tempLocation.setLatitude(Double.parseDouble(data.getLatitude()));
            tempLocation.setLongitude(Double.parseDouble(data.getLongitude()));

            Float distance = currLocation.distanceTo(tempLocation);

            double inches = (39.370078 * distance);
            double miles = (double) (inches / 63360);

            data.setDistance(miles);
        }

        return tlmDataList;
    }

    private ArrayList<Double> getLatLngFromZip(Context context) {
        final Geocoder geocoder = new Geocoder(context);
        double latitude = 0, longitude = 0;
        try {
            List<Address> addresses = geocoder.getFromLocationName(zipCode, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                // Use the address as needed
                latitude = address.getLatitude();
                longitude = address.getLongitude();
            }
        } catch (IOException e) {
            Toast.makeText(context, "Unable to geocode zipcode", Toast.LENGTH_LONG).show();
        } catch (IllegalArgumentException e) {
        }

        ArrayList<Double> latLong = new ArrayList<Double>();
        latLong.add(latitude);
        latLong.add(longitude);

        return latLong;
    }

    public List<TLMData> sortLocations(ArrayList<TLMData> totalTlmData) {
        Comparator comp = new Comparator<TLMData>() {
            @Override
            public int compare(TLMData o, TLMData o2) {
                return (int)(o.getDistance() - o2.getDistance());
            }
        };

        Collections.sort(totalTlmData, comp);
        return totalTlmData;
    }

}