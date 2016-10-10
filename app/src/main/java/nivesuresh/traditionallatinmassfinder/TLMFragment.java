package nivesuresh.traditionallatinmassfinder;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by nivesuresh on 7/17/16.
 */
public class TLMFragment extends Fragment {

    private ListView listview;
    private TextView emptyTextView;
    private TextView locationTextView;

    private TLMAdapter adapter;
    private List<TLMData> tlmDataList = new ArrayList<>();

    private static String zipCode;

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

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        populateListView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_search){
            searchListener();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume(){
        super.onResume();
        populateListView();
    }

    /**
     * A helper function for the Alert dialog to pop up when
     * "Search" button on ActionBar is clicked
     */
    private void searchListener(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Enter Zipcode");

        final EditText input = new EditText(getActivity());
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(5);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setFilters(filters);

        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                zipCode = input.getText().toString();
                tlmDataList.clear();
                populateListView();
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
    private void populateListView(){

        if (tlmDataList.isEmpty()) {
            try {
                List<TLMData> results = new TLMTask().execute(zipCode).get();
                if(results != null) tlmDataList.addAll(results);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        if(tlmDataList.isEmpty()){
            emptyTextView.setVisibility(View.VISIBLE);
            locationTextView.setVisibility(View.GONE);
        } else{
            emptyTextView.setVisibility(View.GONE);
            locationTextView.setVisibility(View.VISIBLE);
        }

        if(tlmDataList != null && !tlmDataList.isEmpty()) {

            adapter = new TLMAdapter(getActivity(), tlmDataList);

            listview.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }
}
