package com.hotteam67.firebaseviewer.tableview;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.evrencoskun.tableview.ITableView;
import com.evrencoskun.tableview.listener.ITableViewListener;
import com.hotteam67.firebaseviewer.RawDataActivity;
import com.hotteam67.firebaseviewer.firebase.DataTableProcessor;
import com.hotteam67.firebaseviewer.tableview.tablemodel.CellModel;
import com.hotteam67.firebaseviewer.tableview.tablemodel.ColumnHeaderModel;
import com.hotteam67.firebaseviewer.tableview.tablemodel.RowHeaderModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by evrencoskun on 2.12.2017.
 */

public class MainTableViewListener implements ITableViewListener {

    private ITableView mTableView;

    public final String MatchNumber = "Match Number";

    public MainTableViewListener(ITableView pTableView) {
        this.mTableView = pTableView;
    }

    @Override
    public void onCellClicked(@NonNull RecyclerView.ViewHolder p_jCellView, int p_nXPosition, int
            p_nYPosition) {

    }

    @Override
    public void onColumnHeaderClicked(@NonNull RecyclerView.ViewHolder p_jColumnHeaderView, int
            p_nXPosition) {

    }

    @Override
    public void onColumnHeaderLongPressed(@NonNull RecyclerView.ViewHolder p_jColumnHeaderView,
                                          int p_nXPosition) {
    }

    @Override
    public void onRowHeaderClicked(@NonNull RecyclerView.ViewHolder p_jRowHeaderView, int
            p_nYPosition) {
        MainTableAdapter adapter = (MainTableAdapter) mTableView.getAdapter();
        DataTableProcessor rawData = adapter.GetRawData();
        if (rawData == null)
            return;

        String teamNumber = adapter.GetCalculatedData().GetRowHeaders().get(p_nYPosition).getData();
        rawData.SetTeamNumberFilter(teamNumber);

        /*
        Copy to final data
         */
        List<List<CellModel>> cells = new ArrayList<>();
        for (List<CellModel> row : rawData.GetCells()) {
            ArrayList<CellModel> newRow = new ArrayList<>();
            newRow.addAll(row);
            cells.add(newRow);
        }
        List<RowHeaderModel> rows = new ArrayList<>();
        rows.addAll(rawData.GetRowHeaders());
        List<ColumnHeaderModel> columns = new ArrayList<>();
        columns.addAll(rawData.GetColumns());


        if (!columns.get(0).getData().equals(MatchNumber)) {
            int columnIndex = -1;
            for (ColumnHeaderModel column : columns) {
                if (column.getData().equals(MatchNumber)) {
                    columnIndex = columns.indexOf(column);
                }
            }
            if (columnIndex != -1) {
                columns.remove(columnIndex);
                for (List<CellModel> row : cells) {
                    CellModel value = row.get(columnIndex);
                    rows.set(cells.indexOf(row), new RowHeaderModel(value.getData().toString()));
                    row.remove(columnIndex);
                }
            }


            DataTableProcessor finalData = new DataTableProcessor(
                    columns,
                    cells,
                    rows
            );
            Log.d("FirebaseScouter", "Set team number filter: " + teamNumber);


            Intent rawDataIntent = new Intent(adapter.GetContext(), RawDataActivity.class);
            rawDataIntent.putExtra(RawDataActivity.RAW_DATA_ATTRIBUTE, finalData);
            adapter.GetContext().startActivity(rawDataIntent);
        }
    }

    @Override
    public void onRowHeaderLongPressed(@NonNull RecyclerView.ViewHolder p_jRowHeaderView, int
            p_nYPosition) {

    }
}
