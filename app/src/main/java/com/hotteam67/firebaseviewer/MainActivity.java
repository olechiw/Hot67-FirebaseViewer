package com.hotteam67.firebaseviewer;

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
import com.hotteam67.firebaseviewer.firebase.CalculatedTableHandler;
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
        refreshButton.setOnClickListener(view -> onRefreshButton());

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

            }
        });

        mTableView = findViewById(R.id.mainTableView);

        // Create TableView Adapter
        mTableAdapter = new MainTableAdapter(this);
        mTableView.setAdapter(mTableAdapter);

        // Create listener
        mTableView.setTableViewListener(new MainTableViewListener(mTableView));


        final FirebaseHelper model = new FirebaseHelper(
                    "https://hot-67-scouting.firebaseio.com/",
                    "testevent1");

        showProgressDialog();
        // Null child to get all raw data
        model.Download(() -> {

            DataTableProcessor rawDataProcessor = new DataTableProcessor(model.getResult());

            HashMap<String, Integer> calculatedColumns = new HashMap<>();
            calculatedColumns.put("Auto High Goals", CalculatedTableHandler.Calculation.AVERAGE);
            CalculatedTableHandler calculatedTableHandler = new CalculatedTableHandler(
                    rawDataProcessor,calculatedColumns, new ArrayList<>(
                            Arrays.asList(new Integer[] { 3 })));
            mTableAdapter.setAllItems(calculatedTableHandler.GetProcessor(), rawDataProcessor);
            hideProgressDialog();
            return null;

        }, getAssets());
    }

    private void onRefreshButton()
    {

    }

    private void onSettingsButton()
    {

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
