package com.hotteam67.firebaseviewer;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TableView mTableView;
    private MainTableAdapter mTableAdapter;

    private ProgressDialog mProgressDialog;


    private ImageButton settingsButton;
    private ImageButton refreshButton;

    private int REQUEST_ENABLE_PERMISSION = 3;

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

        if (ContextCompat.checkSelfPermission(
                        this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED)
        {
            refreshLocal();
        }
        else
        {
            Log.d("FirebaseScouter", "Requesting Permissions");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_ENABLE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (requestCode == REQUEST_ENABLE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                refreshLocal();
            }
        }
    }

    private void refreshLocal()
    {
        Log.d("FirebaseScouter", "Permissions exist");

        teamSearchView.setText("");

        SharedPreferences url = PreferenceManager.getDefaultSharedPreferences(this);
        String databaseUrl = (String) url.getAll().get("pref_databaseUrl");
        String eventName = (String) url.getAll().get("pref_eventName");
        String apiKey = (String) url.getAll().get("pref_apiKey");

        FirebaseHelper helper = new FirebaseHelper(databaseUrl, eventName, apiKey);
        helper.LoadLocal();
        rawData = new DataTableProcessor(helper.getResult());
        refreshCalculations();
    }

    private void refresh()
    {
        teamSearchView.setText("");

        SharedPreferences url = PreferenceManager.getDefaultSharedPreferences(this);
        String databaseUrl = (String) url.getAll().get("pref_databaseUrl");
        String eventName = (String) url.getAll().get("pref_eventName");
        String apiKey = (String) url.getAll().get("pref_apiKey");

        final FirebaseHelper model = new FirebaseHelper(
                databaseUrl, eventName, apiKey);

        showProgressDialog();
        // Null child to get all raw data
        model.Download(() -> {

            rawData = new DataTableProcessor(model.getResult());

            refreshCalculations();

            hideProgressDialog();
            return null;
        });
    }

    private void refreshCalculations()
    {
        HashMap<String, Integer> calculatedColumns = new HashMap<>();
        List<Integer> calculatedColumnsIndices = new ArrayList<>();

        calculatedColumns.put("Auton Switch", CalculatedTableProcessor.Calculation.AVERAGE);
        calculatedColumnsIndices.add(3);
        calculatedColumns.put("Auton Scale", CalculatedTableProcessor.Calculation.AVERAGE);
        calculatedColumnsIndices.add(2);
        calculatedColumns.put("Teleop Scale", CalculatedTableProcessor.Calculation.AVERAGE);
        calculatedColumnsIndices.add(11);
        calculatedColumns.put("Teleop Switch", CalculatedTableProcessor.Calculation.AVERAGE);
        calculatedColumnsIndices.add(12);


        calculatedData = new CalculatedTableProcessor(
                rawData,calculatedColumns, calculatedColumnsIndices);

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
