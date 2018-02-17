package com.hotteam67.firebaseviewer.firebase;

import android.util.Log;

import com.annimon.stream.Stream;
import com.hotteam67.firebaseviewer.tableview.tablemodel.CellModel;
import com.hotteam67.firebaseviewer.tableview.tablemodel.ColumnHeaderModel;
import com.hotteam67.firebaseviewer.tableview.tablemodel.RowHeaderModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Jakob on 1/18/2018.
 */

public class DataTableProcessor implements Serializable {
    private List<ColumnHeaderModel> mColumnHeaderList;
    private List<List<CellModel>> cellList;
    private List<RowHeaderModel> rowHeaderList;

    private final String TeamNumber = "Team Number";

    public DataTableProcessor(HashMap<String, Object> rawData)
    {
        /*
        Load the Raw Data into model
         */
        mColumnHeaderList = new ArrayList<>();
        cellList = new ArrayList<>();
        rowHeaderList = new ArrayList<>();

        if (rawData == null)
            return;

        int row_id = 0;
        // Load rows and headers into cellmodels
        for (HashMap.Entry<String, Object> row : rawData.entrySet())
        {
            // Load the row
            try {
                HashMap<String, String> rowMap = (HashMap<String, String>) row.getValue();
                cellList.add(new ArrayList<CellModel>());

                // TeamNumber - before everything else
                String number = rowMap.get(TeamNumber);
                //cellList.get(row_id).add(new CellModel(row_id + "_0", number));
                rowHeaderList.add(new RowHeaderModel(number));
                rowMap.remove(TeamNumber);


                int column_id = 0;
                for (HashMap.Entry<String, String> cell : rowMap.entrySet()) {


                    String cell_id = row_id + "_" + column_id;

                    CellModel model = new CellModel(cell_id, cell.getValue());
                    cellList.get(row_id).add(model);

                    column_id++;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            if (row_id == 0)
            {
                // Load column headers on first row
                try
                {
                    if (rawData.entrySet().size() > 1)
                    {
                        HashMap<String, String> row1 = (HashMap<String, String>) row.getValue();
                        // TeamNumber
                        row1.remove(TeamNumber);
                        //mColumnHeaderList.add(new ColumnHeaderModel(TeamNumber));
                        for (HashMap.Entry<String, String> column : row1.entrySet())
                            mColumnHeaderList.add(new ColumnHeaderModel(column.getKey()));
                    }
                    else
                    {
                        Log.e("FirebaseScouter", "Failed to get fire result for columns");
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            ++row_id;
        }
    }

    public DataTableProcessor(List<ColumnHeaderModel> columnNames, List<List<CellModel>> cellValues, List<RowHeaderModel> rowNames)
    {
        mColumnHeaderList = columnNames;
        cellList = cellValues;
        rowHeaderList = rowNames;
    }


    public void SetTeamNumberFilter(String term)
    {
        multiFilter = new ArrayList<>(
                Collections.singletonList(term));
    }

    private List<String> multiFilter = new ArrayList<>();
    public void SetMultiTeamFilter(String... terms)
    {
        multiFilter = new ArrayList<>(
                Arrays.asList(terms)
        );
    }

    public List<ColumnHeaderModel> GetColumns()
    {
        return mColumnHeaderList;
    }

    public List<String> GetColumnNames()
    {
        List<String> nameList = new ArrayList<>();
        for (ColumnHeaderModel model : mColumnHeaderList)
            nameList.add(model.getData());

        return nameList;
    }

    public List<List<CellModel>> GetCells()
    {
        if (multiFilter == null || multiFilter.size() == 0)
            return cellList;
        else
        {
            try {
                List<RowHeaderModel> filteredRows = GetRowHeaders();
                List<List<CellModel>> cells = new ArrayList<>();
                if (filteredRows.size() > 0) {
                    for (RowHeaderModel row : filteredRows)
                    {
                        cells.add(cellList.get(rowHeaderList.indexOf(row)));
                    }
                }
                Log.d("FirebaseViewer", "Returning cells: " + cells.size());
                return cells;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return cellList;
            }
        }
    }

    public List<RowHeaderModel> GetRowHeaders()
    {
        if (multiFilter == null || multiFilter.size() == 0)
            return rowHeaderList;
        if (multiFilter.get(0) == null || multiFilter.get(0).trim().isEmpty())
            return rowHeaderList;
        List<RowHeaderModel> filteredRows = new ArrayList<>();
        List<RowHeaderModel> unFilteredRows = new ArrayList<>();
        unFilteredRows.addAll(rowHeaderList);

        for (String teamNumberFilter : multiFilter)
        {
            for (int i = 0; i < unFilteredRows.size(); ++i)
            {
                RowHeaderModel row = unFilteredRows.get(i);
                if (row.getData().equals(teamNumberFilter))
                {
                    filteredRows.add(row);
                    unFilteredRows.remove(i);
                }
            }
        }
        Log.d("FirebaseViewer", "Returning rows: " + filteredRows.size());
        return filteredRows;
    }
}
