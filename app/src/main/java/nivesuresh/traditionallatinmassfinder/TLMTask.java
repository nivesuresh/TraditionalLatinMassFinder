package nivesuresh.traditionallatinmassfinder;

import android.net.Uri;
import android.os.AsyncTask;
import android.text.format.Time;
import android.util.Log;

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
import java.util.List;

/**
 * Created by nivesuresh on 6/24/16.
 */
public class TLMTask extends AsyncTask<String, Void, List<TLMData>> {

    @Override
    protected List<TLMData> doInBackground(String... params) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

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

            Log.d("NIVE_SURESH", tlmJsonStr);

            if (!tlmJsonStr.contains("Error"))
                results = getTLMDataFromJson(tlmJsonStr);
            else results = null;

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
}