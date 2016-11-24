package nivesuresh.traditionallatinmassfinder;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

/**
 * Created by nivesuresh on 11/23/16.
 */
public class TLMDetailFragment extends Fragment {

    TLMData data;
    String location;
//    String place;
//    String location;
//    String units;

    TextView churchNameDetailTextView;
    TextView addressDetailTextView;
    TextView hoursDetailTextView;
    TextView phoneDetailTextView;
    TextView websiteDetailTextView;
    TextView emailDetailTextView;
    TextView affiliationDetailTextView;
    TextView distanceDetailTextView;


    View rootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        Bundle bundle = getActivity().getIntent().getExtras();
        if(bundle == null) {
            return rootView;
        } else{
            data = (TLMData) bundle.getSerializable(TLMFragment.TLM_DATA_LIST);
            location = (String) bundle.getSerializable(TLMFragment.LOCATION);
            Log.d("NIVE_SURESH", "Data: " + (data == null));
            Log.d("NIVE_SURESH", "Location: " + (location == null));

            if(data != null && location != null)
                update(data, location);
            return rootView;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void update(TLMData data, String location){
        Log.d("Data name", data.getChurchName());
        churchNameDetailTextView = (TextView) rootView.findViewById(R.id.churchNameDetailTextView);
        churchNameDetailTextView.setText(data.getChurchName());

        addressDetailTextView = (TextView) rootView.findViewById(R.id.addressDetailTextView);
        String address = data.getStreetAddress() + "\n" + data.getTown() + ", " + data.getStateProvince() + " " + data.getZipPostalCode()
                + "\n" + data.getCountry();
        //addressDetailTextView.setText();

        //http://stackoverflow.com/questions/2624649/autolink-for-map-not-working
        SpannableString spanStr = new SpannableString(address);
        spanStr.setSpan(new UnderlineSpan(), 0, spanStr.length(), 0);
        addressDetailTextView.setText(spanStr);


        addressDetailTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent geoIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q="
                        + addressDetailTextView.getText().toString()));
                startActivity(geoIntent);
            }
        });

        hoursDetailTextView = (TextView) rootView.findViewById(R.id.hoursDetailTextView);
        List<String> hoursList = Arrays.asList(data.getTimes().split(","));

        StringBuffer hours = new StringBuffer();
        for(String hour : hoursList) {
            hours.append(hour);
            hours.append("\n");
        }

        hoursDetailTextView.setText(hours.toString());

        phoneDetailTextView = (TextView) rootView.findViewById(R.id.phoneDetailTextView);
        phoneDetailTextView.setText(data.getPhone());

        websiteDetailTextView = (TextView) rootView.findViewById(R.id.websiteDetailTextView);
        websiteDetailTextView.setText(data.getWebsite());

        emailDetailTextView = (TextView) rootView.findViewById(R.id.emailDetailTextView);
        emailDetailTextView.setText(data.getEmail());

        affiliationDetailTextView = (TextView) rootView.findViewById(R.id.affiliationDetailTextView);
        affiliationDetailTextView.setText(data.getAffiliation());

        distanceDetailTextView = (TextView) rootView.findViewById(R.id.distanceDetailTextView);
        String distance = new DecimalFormat("##.##").format(data.getDistance());
        distanceDetailTextView.setText(distance + " miles");


    }
}
