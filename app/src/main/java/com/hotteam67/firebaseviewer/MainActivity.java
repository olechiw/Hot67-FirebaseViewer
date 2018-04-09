package com.hotteam67.firebaseviewer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.evrencoskun.tableview.TableView;
import com.hotteam67.firebaseviewer.data.CalculatedTableProcessor;
import com.hotteam67.firebaseviewer.data.ColumnSchema;
import com.hotteam67.firebaseviewer.data.DataTable;
import com.hotteam67.firebaseviewer.tableview.MainTableAdapter;
import com.hotteam67.firebaseviewer.tableview.MainTableViewListener;
import com.hotteam67.firebaseviewer.data.Sort;
import com.hotteam67.firebaseviewer.tableview.tablemodel.CellModel;
import com.hotteam67.firebaseviewer.tableview.tablemodel.ColumnHeaderModel;
import com.hotteam67.firebaseviewer.tableview.tablemodel.RowHeaderModel;
import com.hotteam67.firebaseviewer.web.FirebaseHandler;
import com.hotteam67.firebaseviewer.web.TBAHandler;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String AVG = "AVG";
    public static final String MAX = "MAX";
    public static final String EMPTY = "";
    public static final String RED = "RED";
    public static final String N_A = "N/A";
    public static final String BLUE = "BLUE";
    public static final String ALLIANCE = "A";

    private MainTableAdapter tableAdapter;

    private ImageButton refreshButton;

    private int REQUEST_ENABLE_PERMISSION = 3;

    private EditText teamSearchView;
    private EditText matchSearchView;

    // State for which calculation is currently in the UI
    int calculationState = CalculatedTableProcessor.Calculation.AVERAGE;
    DataTable rawData;

    // Both tables loaded into memory, meaning faster switching but slower loading
    CalculatedTableProcessor calculatedDataAverages;
    CalculatedTableProcessor calculatedDataMaximums;

    // TBA-Pulled data, Rankings, Nicknames, and Schedule in sequential order
    private JSONObject teamNumbersRanks;
    private JSONObject teamNumbersNames;

    List<String> redTeams = new ArrayList<>();
    List<String> blueTeams = new ArrayList<>();

    public MainActivity() {}

    public static final int RawDataRequestCode = 1;

    /*
    Result for raw data activity, load the match number if one was selected
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == RawDataRequestCode)
        {
            if (data == null) return;
            String result = data.getStringExtra("Match Number");
            matchSearchView.setText(result);
        }
    }

    /*
    Construct UI
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
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


        ImageButton settingsButton = finalView.findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(view -> OnSettingsButton());

        finalView.findViewById(R.id.calculationButton).setOnClickListener(this::OnCalculationButton);

        refreshButton = finalView.findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(view -> RefreshTable());

        ImageButton clearButton = findViewById(R.id.clearButton);
        clearButton.setOnClickListener(v -> {
            matchSearchView.setText(EMPTY);
            teamSearchView.setText(EMPTY);
        });

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
                    SetTeamNumberFilter(editable.toString());
                    Update();
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
                OnMatchSearch(s);
            }
        });

        TableView tableView = findViewById(R.id.mainTableView);

        // Create TableView Adapter
        tableAdapter = new MainTableAdapter(this);
        tableView.setAdapter(tableAdapter);

        // Create listener
        tableView.setTableViewListener(new MainTableViewListener(tableView));

        if (ContextCompat.checkSelfPermission(
                        this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED)
        {
            LoadSerializedTables();
            LoadTBADataLocal();
        }
        else
        {
            Log.d("HotTeam67", "Requesting Permissions");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_ENABLE_PERMISSION);
        }
    }

    /*
    When the match search text changes
     */
    private void OnMatchSearch(Editable s)
    {
        try
        {
            if (s.toString().trim().isEmpty())
            {
                SetTeamNumberFilter(EMPTY);
                Update();
                return;
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

                List<RowHeaderModel> rows = new ArrayList<>();
                List<List<CellModel>> cells = new ArrayList<>();
                List<ColumnHeaderModel> columns = new ArrayList<>(GetActiveTable().GetColumns());

                for (String team : filters)
                {
                    SetTeamNumberFilter(team);
                    rows.addAll(GetActiveTable().GetRowHeaders());

                    for (List<CellModel> cell : GetActiveTable().GetCells())
                    {
                        List<CellModel> newRow = new ArrayList<>(cell);
                        cells.add(newRow);
                    }
                }

                DataTable processor = new DataTable(columns, cells, rows);
                processor.SetTeamNumberFilter(EMPTY);

                List<ColumnHeaderModel> columnHeaderModels = processor.GetColumns();
                columnHeaderModels.add(0, new ColumnHeaderModel(ALLIANCE));

                List<List<CellModel>> outputCells = processor.GetCells();
                for (int i = 0; i < outputCells.size(); ++i)
                {
                    String teamNumber = processor.GetRowHeaders().get(i).getData();

                    if (red.contains(teamNumber))
                    {
                        outputCells.get(i).add(0, new CellModel(i + "_00", RED));
                    }
                    else {
                        outputCells.get(i).add(0, new CellModel(i + "_00", BLUE));
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
                    row.add(new CellModel("0", RED));

                    for (int i = 0; i < firstRowSize; ++i)
                    {
                        row.add(new CellModel("0", N_A));
                    }


                    outputCells.add(row);
                    rowHeaders.add(new RowHeaderModel(team));
                }
                for (String team : blue)
                {
                    List<CellModel> row = new ArrayList<>();
                    row.add(new CellModel("0", BLUE));

                    for (int i = 0; i < firstRowSize; ++i)
                    {
                        row.add(new CellModel("0", N_A));
                    }

                    outputCells.add(row);
                    rowHeaders.add(new RowHeaderModel(team));
                }

                DataTable newProcessor = new DataTable(columnHeaderModels, outputCells, rowHeaders);

                tableAdapter.setAllItems(Sort.BubbleSortByColumn(newProcessor, 0, false), rawData); // Sort by alliance
            }
            else
            {
                calculatedDataAverages.GetProcessor().SetTeamNumberFilter(EMPTY);
                tableAdapter.setAllItems(calculatedDataAverages.GetProcessor(), rawData);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /*
    Calculation button event handler
     */
    private void OnCalculationButton(View v)
    {
        switch (calculationState)
        {
            case CalculatedTableProcessor.Calculation.AVERAGE:
                calculationState = CalculatedTableProcessor.Calculation.MAXIMUM;
                ((Button)v).setText(AVG);
                Update();
                if (!matchSearchView.getText().toString().isEmpty())
                    matchSearchView.setText(matchSearchView.getText());
                break;
            case CalculatedTableProcessor.Calculation.MAXIMUM:
                calculationState = CalculatedTableProcessor.Calculation.AVERAGE;
                ((Button)v).setText(MAX);
                Update();
                if (!matchSearchView.getText().toString().isEmpty())
                    matchSearchView.setText(matchSearchView.getText());
                break;
        }
    }

    /*
    Settings button event handler
     */
    private void OnSettingsButton()
    {
        Intent settingsIntent = new Intent(this, PreferencesActivity.class);
        startActivity(settingsIntent);
    }

    /*
    Once disk permission is obtained
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (requestCode == REQUEST_ENABLE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                LoadSerializedTables();
                LoadTBADataLocal();
            }
        }
    }

    /*
    Serialize the three datatables and write them to disk
     */
    private void SerializeTables()
    {
        @SuppressLint("StaticFieldLeak") AsyncTask serializeTask = new AsyncTask()
        {
            @Override
            protected Object doInBackground(Object[] objects)
            {
                FileHandler.Serialize(calculatedDataMaximums, FileHandler.MAXIMUMS_CACHE);
                FileHandler.Serialize(calculatedDataAverages, FileHandler.AVERAGES_CACHE);
                FileHandler.Serialize(rawData, FileHandler.RAW_CACHE);
                return null;
            }
        };
        serializeTask.execute();

    }

    /*
    Load tables from disk into memory (raw, both calculated tables)
     */
    private void LoadSerializedTables()
    {
        StartProgressAnimation();
        @SuppressLint("StaticFieldLeak") AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                calculatedDataAverages = (CalculatedTableProcessor)
                        FileHandler.DeSerialize(FileHandler.AVERAGES_CACHE);
                calculatedDataMaximums = (CalculatedTableProcessor)
                        FileHandler.DeSerialize(FileHandler.MAXIMUMS_CACHE);
                rawData = (DataTable)
                        FileHandler.DeSerialize(FileHandler.RAW_CACHE);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                Update();
                EndProgressAnimation();
            }
        };
        task.execute();
    }

    /*
    Re-download all scouting data + TBA data, then refresh
     */
    private void RefreshTable()
    {
        StartProgressAnimation();

        teamSearchView.setText(EMPTY);

        LoadTBAData();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String databaseUrl = (String) prefs.getAll().get("pref_databaseUrl");
        String eventName = (String) prefs.getAll().get("pref_eventName");
        String apiKey = (String) prefs.getAll().get("pref_apiKey");

        final FirebaseHandler model = new FirebaseHandler(
                databaseUrl, eventName, apiKey);

        // Null child to get all raw data
        model.Download(() -> {

            rawData = new DataTable(model.getResult(), ColumnSchema.CalculatedColumnsRawNames(), ColumnSchema.SumColumns());

            RunCalculations();

            return null;
        });
    }

    /*
    Re-run all calculations with the current raw data
     */
    private void RunCalculations()
    {
        // Runs
        @SuppressLint("StaticFieldLeak") AsyncTask averagesTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                CalculatedTableProcessor averages = new CalculatedTableProcessor(
                        rawData,
                        ColumnSchema.CalculatedColumns(),
                        ColumnSchema.CalculatedColumnsRawNames(),
                        teamNumbersRanks,
                        CalculatedTableProcessor.Calculation.AVERAGE);
                SetCalculatedDataAverages(averages);
                UpdateIfLoaded();

                return null;
            }
        };
        @SuppressLint("StaticFieldLeak") AsyncTask maximumsTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                CalculatedTableProcessor maximums = new CalculatedTableProcessor(
                        rawData,
                        ColumnSchema.CalculatedColumns(),
                        ColumnSchema.CalculatedColumnsRawNames(),
                        teamNumbersRanks,
                        CalculatedTableProcessor.Calculation.MAXIMUM);
                SetCalculatedDataMaximums(maximums);
                UpdateIfLoaded();

                return null;
            }
        };
        averagesTask.execute();
        maximumsTask.execute();
    }

    private synchronized void SetCalculatedDataAverages(CalculatedTableProcessor table)
    {
        calculatedDataAverages = table;
    }

    private synchronized void SetCalculatedDataMaximums(CalculatedTableProcessor table)
    {
        calculatedDataMaximums = table;
    }

    private synchronized void UpdateIfLoaded()
    {
        if (calculatedDataMaximums != null && calculatedDataAverages != null)
        {
            runOnUiThread(() ->
            {
                Update();
                EndProgressAnimation();
                SerializeTables();
            });
        }
    }

    /*
    Load TBA data from the API v3
     */
    private void LoadTBAData()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String matchCode = (String) prefs.getAll().get("pref_matchCode");
        String matchKey = String.valueOf(Calendar.getInstance().get(Calendar.YEAR)) + matchCode;

        try
        {
            StringBuilder s = new StringBuilder();

            // Call api and load into csv
            TBAHandler.Matches(matchKey, matches -> {
                try {
                    for (List<List<String>> m : matches) {
                        List<String> redTeams = m.get(0);
                        List<String> blueTeams = m.get(1);
                        for (String t : redTeams) {
                            s.append(t.replace("frc", EMPTY)).append(",");
                        }
                        for (int t = 0; t < blueTeams.size(); ++t) {
                            s.append(blueTeams.get(t).replace("frc", EMPTY));
                            if (t + 1 != blueTeams.size())
                                s.append(",");
                        }
                        s.append("\n");
                    }
                    FileHandler.Write(FileHandler.VIEWER_MATCHES, s.toString());
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            });

            // Load into json
            try {
                TBAHandler.TeamNames(matchKey, teamNames -> {
                    FileHandler.Write(FileHandler.TEAM_NAMES, teamNames.toString());
                    teamNumbersNames = teamNames;
                });
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            try {
                TBAHandler.Rankings(matchKey, rankings ->
                {
                    FileHandler.Write(FileHandler.TEAM_RANKS, rankings.toString());
                    teamNumbersRanks = rankings;
                }
                );
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            LoadTBADataLocal();
        }
        catch (Exception e)
        {
            Log.e("[Matches Fetcher]", "Failed to get event: " + e.getMessage(), e);
        }
    }

    /*
    Load TBA data from files
     */
    private void LoadTBADataLocal()
    {
        redTeams = new ArrayList<>();
        blueTeams = new ArrayList<>();

        String content = FileHandler.LoadContents(FileHandler.VIEWER_MATCHES);
        if (content == null || content.trim().isEmpty())
            return;
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

        try {
            teamNumbersNames = new JSONObject(FileHandler.LoadContents(FileHandler.TEAM_NAMES));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            teamNumbersNames = new JSONObject();
        }
        try
        {
            teamNumbersRanks = new JSONObject(FileHandler.LoadContents(FileHandler.TEAM_RANKS));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            teamNumbersRanks = new JSONObject();
        }
    }

    /*
    Update the UI with the currently active table
     */
    private void Update()
    {
        if (calculatedDataMaximums == null || calculatedDataAverages == null)
            return;

        if (calculationState == CalculatedTableProcessor.Calculation.MAXIMUM)
            tableAdapter.setAllItems(calculatedDataMaximums.GetProcessor(), rawData);
        else
            tableAdapter.setAllItems(calculatedDataAverages.GetProcessor(), rawData);
    }

    /*
    Get the active datatable
     */
    private DataTable GetActiveTable()
    {
        if (calculationState == CalculatedTableProcessor.Calculation.MAXIMUM)
            return calculatedDataMaximums.GetProcessor();
        else
            return calculatedDataAverages.GetProcessor();
    }

    /*
    Set the team number filter on the active table
     */
    private void SetTeamNumberFilter(String s)
    {
        calculatedDataMaximums.GetProcessor().SetTeamNumberFilter(s);
        calculatedDataAverages.GetProcessor().SetTeamNumberFilter(s);
    }

    /*
    Get a jsonobject of team numbers and team names
     */
    public JSONObject GetTeamNumbersNames() { return teamNumbersNames; }

    /*
    Spin the refresh button around, and disable it
     */
    private void StartProgressAnimation()
    {
        RotateAnimation anim = (RotateAnimation)
                AnimationUtils.loadAnimation(this, R.anim.rotate);
        refreshButton.setAnimation(anim);
        refreshButton.setEnabled(false);
    }

    /*
    Stop refresh button animation and enable it
     */
    private void EndProgressAnimation()
    {
        refreshButton.clearAnimation();
        refreshButton.setEnabled(true);
    }
}
