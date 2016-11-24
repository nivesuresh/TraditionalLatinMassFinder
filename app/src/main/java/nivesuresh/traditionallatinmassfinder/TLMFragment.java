package nivesuresh.traditionallatinmassfinder;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nivesuresh on 7/17/16.
 */
public class TLMFragment extends Fragment implements TLMTask.AsyncListener {

    private ListView listview;
    private TextView emptyTextView;
    private TextView locationTextView;

    private TLMAdapter adapter;
    public static List<TLMData> tlmDataList = new ArrayList<>();

    private static String location;

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

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        location = sharedPref.getString(LOCATION, "61704");

        listview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        executeTLMTask();
        //populateListView(tlmDataList);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);

        //Implemented searchview with inspiration from: http://javapapers.com/android/android-searchview-action-bar-tutorial/
//        SearchManager searchManager =
//                (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
//        SearchView searchView =
//                (SearchView) menu.findItem(R.id.action_search).getActionView();
//        searchView.setSearchableInfo(
//                searchManager.getSearchableInfo(getActivity().getComponentName()));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_about) {
            Intent intent = new Intent(getActivity(), TLMAboutActivity.class);
            startActivity(intent);
        } else if(id == R.id.action_news) {
            Intent intent = new Intent(getActivity(), TLMNewsActivity.class);
            startActivity(intent);
        } else if(id == R.id.action_search){
            searchListener();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume(){
        super.onResume();
//        executeTLMTask();
        populateListView(tlmDataList);
    }

    /**
     * A helper function for the Alert dialog to pop up when
     * "Search" button on ActionBar is clicked
     */
    private void searchListener(){
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
    private void executeTLMTask(){

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
        if(tlmDataList.isEmpty()){
            //TODO
        }

        if(tlmDataList != null && !tlmDataList.isEmpty()) {
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
    public void onStop(){
        super.onStop();

        SharedPreferences pref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putString(LOCATION, location);
        editor.commit();
    }


}
