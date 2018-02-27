package com.hotteam67.firebaseviewer;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.annimon.stream.Stream;
import com.cpjd.main.Settings;
import com.cpjd.main.TBA;
import com.cpjd.models.Event;
import com.cpjd.models.Match;
import com.evrencoskun.tableview.TableView;
import com.hotteam67.firebaseviewer.firebase.CalculatedTableProcessor;
import com.hotteam67.firebaseviewer.firebase.FirebaseHelper;
import com.hotteam67.firebaseviewer.firebase.DataTableProcessor;
import com.hotteam67.firebaseviewer.tableview.MainTableAdapter;
import com.hotteam67.firebaseviewer.tableview.MainTableViewListener;
import com.hotteam67.firebaseviewer.tableview.RowHeaderViewHolder;
import com.hotteam67.firebaseviewer.tableview.Sort;
import com.hotteam67.firebaseviewer.tableview.tablemodel.CellModel;
import com.hotteam67.firebaseviewer.tableview.tablemodel.ColumnHeaderModel;
import com.hotteam67.firebaseviewer.tableview.tablemodel.RowHeaderModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TableView mTableView;
    private MainTableAdapter mTableAdapter;

    private ProgressDialog mProgressDialog;


    private ImageButton settingsButton;
    private ImageButton refreshButton;

    private int REQUEST_ENABLE_PERMISSION = 3;

    private EditText teamSearchView;
    private EditText matchSearchView;

    DataTableProcessor rawData;
    CalculatedTableProcessor calculatedData;
    DataTableProcessor unfilteredCalculatedData;

    List<String> redTeams = new ArrayList<>();
    List<String> blueTeams = new ArrayList<>();

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

        matchSearchView = findViewById(R.id.matchNumberSearch);
        matchSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try
                {
                    if (matchSearchView.getText().toString().trim().isEmpty())
                    {

                        calculatedData.GetProcessor().SetTeamNumberFilter("");
                        mTableAdapter.setAllItems(calculatedData.GetProcessor(), rawData);
                    }
                    int matchNumber = Integer.valueOf(matchSearchView.getText().toString());
                    if (matchNumber <= redTeams.size() && matchNumber <= blueTeams.size())
                    {
                        List<String> red = new ArrayList<>(
                                Arrays.asList(redTeams.get(matchNumber - 1).split(",")));
                        List<String> blue = new ArrayList<>(
                                Arrays.asList(blueTeams.get(matchNumber - 1).split(",")));

                        List<String> filters = new ArrayList<>();
                        filters.addAll(red);
                        filters.addAll(blue);

                        List<ColumnHeaderModel> columns = new ArrayList<>();
                        List<RowHeaderModel> rows = new ArrayList<>();
                        List<List<CellModel>> cells = new ArrayList<>();

                        columns.addAll(calculatedData.GetProcessor().GetColumns());
                        rows.addAll(calculatedData.GetProcessor().GetRowHeaders());

                        for (List<CellModel> cell : calculatedData.GetProcessor().GetCells())
                        {
                            List<CellModel> newRow = new ArrayList<>();
                            newRow.addAll(cell);
                            cells.add(newRow);
                        }


                        DataTableProcessor processor = new DataTableProcessor(columns, cells, rows);
                        processor.SetMultiTeamFilter(Stream.of(filters).toArray(String[]::new));

                        //refreshCalculations(true);
                        List<ColumnHeaderModel> columnHeaderModels = processor.GetColumns();
                        columnHeaderModels.add(0, new ColumnHeaderModel("ALLIANCE"));

                        List<List<CellModel>> outputCells = processor.GetCells();
                        for (int i = 0; i < outputCells.size(); ++i)
                        {
                            String teamNumber = processor.GetRowHeaders().get(i).getData();

                            if (red.contains(teamNumber))
                            {
                                outputCells.get(i).add(0, new CellModel(i + "_00", "RED"));
                            }
                            else {
                                outputCells.get(i).add(0, new CellModel(i + "_00", "BLUE"));
                                blue.remove(teamNumber);
                            }
                            red.remove(teamNumber);
                            blue.remove(teamNumber);
                        }

                        List<RowHeaderModel> rowHeaders = processor.GetRowHeaders();

                        int firstRowSize = 0;
                        if (outputCells.size() > 0)
                        {
                            firstRowSize = outputCells.get(0).size() - 1; // -1 for alliance
                        }
                        for (String team : red)
                        {
                            List<CellModel> row = new ArrayList<>();
                            row.add(new CellModel("0", "RED"));

                            for (int i = 0; i < firstRowSize; ++i)
                            {
                                row.add(new CellModel("0", "N/A"));
                            }


                            outputCells.add(row);
                            rowHeaders.add(new RowHeaderModel(team));
                        }
                        for (String team : blue)
                        {
                            List<CellModel> row = new ArrayList<>();
                            row.add(new CellModel("0", "BLUE"));

                            for (int i = 0; i < firstRowSize; ++i)
                            {
                                row.add(new CellModel("0", "N/A"));
                            }

                            outputCells.add(row);
                            rowHeaders.add(new RowHeaderModel(team));
                        }

                        DataTableProcessor newProcessor = new DataTableProcessor(columnHeaderModels, outputCells, rowHeaders);

                        mTableAdapter.setAllItems(Sort.BubbleSortDescendingByRowHeader(newProcessor), rawData);
                    }
                    else
                    {
                        rawData.SetTeamNumberFilter("");
                        refreshCalculations();
                    }
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
            loadEventMatches();
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

        downloadEventMatches();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String databaseUrl = (String) prefs.getAll().get("pref_databaseUrl");
        String eventName = (String) prefs.getAll().get("pref_eventName");
        String apiKey = (String) prefs.getAll().get("pref_apiKey");

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
        refreshCalculations(false);
    }
    private void refreshCalculations(boolean dontUpdate)
    {
        List<String> calculatedColumns = new ArrayList<>();
        List<Integer> calculatedColumnsIndices = new ArrayList<>();

        calculatedColumns.add("T. Scale");
        calculatedColumnsIndices.add(0);
        calculatedColumns.add("T. Switch");
        calculatedColumnsIndices.add(11);
        calculatedColumns.add("T. Vault");
        calculatedColumnsIndices.add(8);
        calculatedColumns.add("A. Scale");
        calculatedColumnsIndices.add(12);
        calculatedColumns.add("A. Switch");
        calculatedColumnsIndices.add(3);
        calculatedColumns.add("A. Vault");
        calculatedColumnsIndices.add(6);
        calculatedColumns.add("Climbed");
        calculatedColumnsIndices.add(5);
        calculatedColumns.add("Assisted");
        calculatedColumnsIndices.add(10);


        calculatedData = new CalculatedTableProcessor(
                rawData,calculatedColumns, calculatedColumnsIndices);

        if (!dontUpdate)
            mTableAdapter.setAllItems(Sort.BubbleSortDescendingByRowHeader(calculatedData.GetProcessor()), rawData);
    }

    private void downloadEventMatches()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String matchCode = (String) prefs.getAll().get("pref_matchCode");

        TBA.setID("HOT67", "BluetoothScouter", "V1");
        TBA tba = new TBA();
        Settings.GET_EVENT_MATCHES = true;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
        StrictMode.setThreadPolicy(policy);

        try
        {
            StringBuilder s = new StringBuilder();
            Event e = tba.getEvent(matchCode, //2017);
                    Integer.valueOf(new SimpleDateFormat("yyyy", Locale.US).format(new Date())));

            for (Match m : e.matches)
            {
                if (m.comp_level.equals("qm"))
                {
                    for (String t : m.redTeams)
                    {
                        s.append(t.replace("frc", "")).append(",");
                    }
                    for (int t = 0; t < m.blueTeams.length; ++t)
                    {
                        s.append(m.blueTeams[t].replace("frc", ""));
                        if (t + 1 != m.blueTeams.length)
                            s.append(",");
                    }
                    s.append("\n");
                }
            }
            FileHandler.Write(FileHandler.SERVER_MATCHES, s.toString());

            loadEventMatches();
        }
        catch (Exception e)
        {
            Log.e("[Matches Fetcher]", "Failed to get event: " + e.getMessage(), e);
        }
    }

    private void loadEventMatches()
    {
        redTeams = new ArrayList<>();
        blueTeams = new ArrayList<>();

        String content = FileHandler.LoadContents(FileHandler.SERVER_MATCHES);
        List<String> contents = Arrays.asList(content.split("\n"));

        for (String match : contents)
        {
            List<String> teams = Arrays.asList(match.split(","));
            // red teams first
            try
            {
                StringBuilder red = new StringBuilder();
                for (int i = 0; i < 3; ++i)
                {
                    red.append(teams.get(i));
                    if (i + 1 != 3)
                        red.append(",");
                }
                redTeams.add(red.toString());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            try
            {
                StringBuilder blue = new StringBuilder();
                for (int i = 3; i < 6; ++i)
                {
                    blue.append(teams.get(i));
                    if (i + 1 != 6)
                        blue.append(",");
                }
                blueTeams.add(blue.toString());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
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
