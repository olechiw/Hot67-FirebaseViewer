package com.hotteam67.firebaseviewer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.evrencoskun.tableview.TableView;
import com.hotteam67.firebaseviewer.firebase.CalculatedTableProcessor;
import com.hotteam67.firebaseviewer.firebase.FirebaseHelper;
import com.hotteam67.firebaseviewer.firebase.DataTableProcessor;
import com.hotteam67.firebaseviewer.tableview.MainTableAdapter;
import com.hotteam67.firebaseviewer.tableview.MainTableViewListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private TableView mTableView;
    private MainTableAdapter mTableAdapter;

    private ProgressDialog mProgressDialog;


    private ImageButton settingsButton;
    private ImageButton refreshButton;

    private EditText teamSearchView;

    DataTableProcessor rawData;
    CalculatedTableProcessor calculatedData;

    public MainActivity() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ActionBar bar = getSupportActionBar();
        View finalView = getLayoutInflater().inflate(
                R.layout.actionbar_main,
                null);
        finalView.setLayoutParams(new ActionBar.LayoutParams(
                ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT));
        bar.setCustomView(finalView);
        bar.setDisplayShowCustomEnabled(true);

        settingsButton = finalView.findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(view -> onSettingsButton());

        refreshButton = finalView.findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(view -> refresh());

        teamSearchView = finalView.findViewById(R.id.teamNumberSearch);
        teamSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    rawData.SetTeamNumberFilter(teamSearchView.getText().toString());
                    refreshCalculations();
                }
                catch (NullPointerException e)
                {
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        mTableView = findViewById(R.id.mainTableView);

        // Create TableView Adapter
        mTableAdapter = new MainTableAdapter(this);
        mTableView.setAdapter(mTableAdapter);

        // Create listener
        mTableView.setTableViewListener(new MainTableViewListener(mTableView));

        refresh();
    }

    private void refresh()
    {
        teamSearchView.setText("");

        SharedPreferences url = PreferenceManager.getDefaultSharedPreferences(this);
        String databaseUrl = (String) url.getAll().get("pref_databaseUrl");
        String eventName = (String) url.getAll().get("pref_eventName");

        final FirebaseHelper model = new FirebaseHelper(
                databaseUrl, eventName);

        showProgressDialog();
        // Null child to get all raw data
        model.Download(() -> {

            rawData = new DataTableProcessor(model.getResult());

            refreshCalculations();

            hideProgressDialog();
            return null;
        }, getAssets());
    }

    private void refreshCalculations()
    {
        HashMap<String, Integer> calculatedColumns = new HashMap<>();

        calculatedColumns.put("Auto High Goals", CalculatedTableProcessor.Calculation.AVERAGE);

        calculatedData = new CalculatedTableProcessor(
                rawData,calculatedColumns, new ArrayList<>(
                Arrays.asList(new Integer[] { 13 })));

        mTableAdapter.setAllItems(calculatedData.GetProcessor(), rawData);
    }


    private void onSettingsButton()
    {
        Intent settingsIntent = new Intent(this, PreferencesActivity.class);
        startActivity(settingsIntent);
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Getting data, please wait...");
            mProgressDialog.setCancelable(false);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {

        if ((mProgressDialog != null) && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
        mProgressDialog = null;
    }
}