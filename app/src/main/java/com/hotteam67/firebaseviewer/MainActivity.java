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
import android.util.StateSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.evrencoskun.tableview.TableView;
import com.hotteam67.firebaseviewer.firebase.CalculatedTableProcessor;
import com.hotteam67.firebaseviewer.firebase.FirebaseHelper;
import com.hotteam67.firebaseviewer.firebase.DataTableProcessor;
import com.hotteam67.firebaseviewer.tableview.MainTableAdapter;
import com.hotteam67.firebaseviewer.tableview.MainTableViewListener;
import com.hotteam67.firebaseviewer.tableview.Sort;
import com.hotteam67.firebaseviewer.tableview.tablemodel.CellModel;
import com.hotteam67.firebaseviewer.tableview.tablemodel.ColumnHeaderModel;
import com.hotteam67.firebaseviewer.tableview.tablemodel.RowHeaderModel;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

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
    CalculatedTableProcessor calculatedDataAverages;
    CalculatedTableProcessor calculatedDataMaximums;

    int calculationState = CalculatedTableProcessor.Calculation.AVERAGE;

    JSONObject teamNumbersRanks;

    List<String> redTeams = new ArrayList<>();
    List<String> blueTeams = new ArrayList<>();

    public MainActivity() {
        // Required empty public constructor
    }

    public static final int RawDataRequestCode = 1;

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

        preferredOrder.add("Teleop Scale");
        preferredOrder.add("Teleop Switch");
        preferredOrder.add("Teleop Vault");
        preferredOrder.add("Auton Scale");
        preferredOrder.add("Auton Switch");
        preferredOrder.add("Auton Vault");
        preferredOrder.add("Climbed");
        preferredOrder.add("Assisted");
        preferredOrder.add("Was Assisted");


        settingsButton = finalView.findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(view -> onSettingsButton());

        finalView.findViewById(R.id.calculationButton).setOnClickListener(v -> {
            switch (calculationState)
            {
                case CalculatedTableProcessor.Calculation.AVERAGE:
                    calculationState = CalculatedTableProcessor.Calculation.MAXIMUM;
                    ((Button)v).setText("AVG");
                    Update();
                    if (!matchSearchView.getText().toString().isEmpty())
                        matchSearchView.setText(matchSearchView.getText());
                    break;
                case CalculatedTableProcessor.Calculation.MAXIMUM:
                    calculationState = CalculatedTableProcessor.Calculation.AVERAGE;
                    ((Button)v).setText("MAX");
                    Update();
                    if (!matchSearchView.getText().toString().isEmpty())
                        matchSearchView.setText(matchSearchView.getText());
                    break;
            }
        });

        refreshButton = finalView.findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(view -> refresh());

        ImageButton clearButton = findViewById(R.id.clearButton);
        clearButton.setOnClickListener(v -> {
            matchSearchView.setText("");
            teamSearchView.setText("");
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
                    SetFilter(editable.toString());
                    Update();
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
                    if (s.toString().trim().isEmpty())
                    {
                        SetFilter("");
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

                        List<ColumnHeaderModel> columns = new ArrayList<>();
                        List<RowHeaderModel> rows = new ArrayList<>();
                        List<List<CellModel>> cells = new ArrayList<>();
                        columns.addAll(Processor().GetColumns());

                        for (String team : filters)
                        {
                            SetFilter(team);
                            rows.addAll(Processor().GetRowHeaders());

                            for (List<CellModel> cell : Processor().GetCells())
                            {
                                List<CellModel> newRow = new ArrayList<>();
                                newRow.addAll(cell);
                                cells.add(newRow);
                            }
                        }

                        DataTableProcessor processor = new DataTableProcessor(columns, cells, rows);
                        processor.SetTeamNumberFilter("");

                        List<ColumnHeaderModel> columnHeaderModels = processor.GetColumns();
                        columnHeaderModels.add(0, new ColumnHeaderModel("A"));

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

                        mTableAdapter.setAllItems(Sort.BubbleSortByColumn(newProcessor, 0, false), rawData); // Sort by alliance
                    }
                    else
                    {
                        calculatedDataAverages.GetProcessor().SetTeamNumberFilter("");
                        mTableAdapter.setAllItems(calculatedDataAverages.GetProcessor(), rawData);
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
        rawData = new DataTableProcessor(helper.getResult(), preferredOrder);
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


            rawData = new DataTableProcessor(model.getResult(), preferredOrder);

            refreshCalculations();

            hideProgressDialog();
            return null;
        });
    }
    List<String> preferredOrder = new ArrayList<>();


    private void refreshCalculations()
    {
        List<String> calculatedColumns = new ArrayList<>();
        List<String> calculatedColumnsIndices = new ArrayList<>();

        calculatedColumns.add("T. Scale");
        calculatedColumnsIndices.add("Teleop Scale");
        calculatedColumns.add("T. Switch");
        calculatedColumnsIndices.add("Teleop Switch");
        calculatedColumns.add("T. Vault");
        calculatedColumnsIndices.add("Teleop Vault");
        calculatedColumns.add("A. Crossed");
        calculatedColumnsIndices.add("Crossed Line");
        calculatedColumns.add("A. Scale");
        calculatedColumnsIndices.add("Auton Scale");
        calculatedColumns.add("A. Switch");
        calculatedColumnsIndices.add("Auton Switch");
        calculatedColumns.add("A. Vault");
        calculatedColumnsIndices.add("Auton Vault");
        calculatedColumns.add("Climbed");
        calculatedColumnsIndices.add("Climbed");
        calculatedColumns.add("Assisted");
        calculatedColumnsIndices.add("Assisted");

        CalculatedTableProcessor.SumColumn column = new CalculatedTableProcessor.SumColumn();
        column.columnName = "Cubes";
        column.columnsNames = new ArrayList<>();
        column.columnsNames.add("A. Scale");
        column.columnsNames.add("T. Scale");
        column.columnsNames.add("A. Vault");
        column.columnsNames.add("T. Vault");
        column.columnsNames.add("A. Switch");
        column.columnsNames.add("T. Switch");

        ArrayList<CalculatedTableProcessor.SumColumn> sumColumns = new ArrayList<>();
        sumColumns.add(column);


        calculatedDataAverages = new CalculatedTableProcessor(
                rawData,calculatedColumns, calculatedColumnsIndices, sumColumns, teamNumbersRanks,
                CalculatedTableProcessor.Calculation.AVERAGE);

        calculatedDataMaximums = new CalculatedTableProcessor(
                rawData, calculatedColumns, calculatedColumnsIndices, sumColumns, teamNumbersRanks,
                CalculatedTableProcessor.Calculation.MAXIMUM);
        Update();
    }

    private JSONObject teamNumbersNames;

    private void downloadEventMatches()
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
                            s.append(t.replace("frc", "")).append(",");
                        }
                        for (int t = 0; t < blueTeams.size(); ++t) {
                            s.append(blueTeams.get(t).replace("frc", ""));
                            if (t + 1 != blueTeams.size())
                                s.append(",");
                        }
                        s.append("\n");

                        FileHandler.Write(FileHandler.VIEWER_MATCHES, s.toString());
                    }
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

            loadEventMatches();
        }
        catch (Exception e)
        {
            Log.e("[Matches Fetcher]", "Failed to get event: " + e.getMessage(), e);
        }
    }

    private void Update()
    {
        if (calculationState == CalculatedTableProcessor.Calculation.MAXIMUM)
            mTableAdapter.setAllItems(calculatedDataMaximums.GetProcessor(), rawData);
        else
            mTableAdapter.setAllItems(calculatedDataAverages.GetProcessor(), rawData);
    }

    private DataTableProcessor Processor()
    {
        if (calculationState == CalculatedTableProcessor.Calculation.MAXIMUM)
            return calculatedDataMaximums.GetProcessor();
        else
            return calculatedDataAverages.GetProcessor();
    }

    private void SetFilter(String s)
    {
        calculatedDataMaximums.GetProcessor().SetTeamNumberFilter(s);
        calculatedDataAverages.GetProcessor().SetTeamNumberFilter(s);
    }

    private void loadEventMatches()
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

    public JSONObject GetTeamNumbersNames() { return teamNumbersNames; }


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
