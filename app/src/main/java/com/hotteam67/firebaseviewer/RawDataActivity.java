package com.hotteam67.firebaseviewer;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.evrencoskun.tableview.TableView;
import com.hotteam67.firebaseviewer.firebase.DataTableProcessor;
import com.hotteam67.firebaseviewer.tableview.MainTableAdapter;
import com.hotteam67.firebaseviewer.tableview.Sort;

public class RawDataActivity extends AppCompatActivity {

    TextView teamNumberView;

    public static final String RAW_DATA_ATTRIBUTE = "raw_data_attribute";
    public static final String TEAM_NUMBER_ATTRIBUTE = "team_number_attribute";

    private ImageButton backButton;
    private DataTableProcessor dataTableProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raw_data);

        ActionBar bar = getSupportActionBar();
        View finalView = getLayoutInflater().inflate(R.layout.actionbar_raw, null);
        finalView.setLayoutParams(new ActionBar.LayoutParams(
                ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT
        ));
        bar.setCustomView(finalView);
        bar.setDisplayShowCustomEnabled(true);

        teamNumberView = findViewById(R.id.teamNumberTextView);

        Bundle b = getIntent().getExtras();
        if (b != null)
        {
            dataTableProcessor = (DataTableProcessor) b.getSerializable(RAW_DATA_ATTRIBUTE);
            String teamNumber = b.getString(TEAM_NUMBER_ATTRIBUTE);
            teamNumberView.setText("Raw Data: " + teamNumber);
        }


        try {
            if (dataTableProcessor != null) {
                if (!(dataTableProcessor.GetCells().size() > 0)) {
                    Log.e("FirebaseScouter", "No input raw data found");
                    return;
                }

                int matchNumberIndex = dataTableProcessor.GetColumnNames().indexOf("Match Number");
                dataTableProcessor = Sort.BubbleSortDescendingByRowHeader(dataTableProcessor);

                TableView table = findViewById(R.id.mainTableView);
                MainTableAdapter adapter = new MainTableAdapter(this);
                table.setAdapter(adapter);
                adapter.setAllItems(dataTableProcessor, null);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }
}