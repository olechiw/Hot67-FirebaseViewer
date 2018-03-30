package com.hotteam67.firebaseviewer.firebase;

import android.util.Log;

import com.annimon.stream.Stream;
import com.hotteam67.firebaseviewer.tableview.tablemodel.CellModel;
import com.hotteam67.firebaseviewer.tableview.tablemodel.ColumnHeaderModel;
import com.hotteam67.firebaseviewer.tableview.tablemodel.RowHeaderModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Jakob on 1/19/2018.
 */

public class CalculatedTableProcessor {

    private DataTableProcessor rawDataTable;
    private List<String> columnsNames;

    private DataTableProcessor calculatedDataTable;
    private HashMap<String, Integer> calculatedColumnHeaders;
    private List<Integer> calculatedColumnIndices;

    public final static class Calculation
    {
        public static final int AVERAGE = 0;
        public static final int MAXIMUM = 1;
        public static final int MINIMUM = 2;
    }

    public CalculatedTableProcessor(DataTableProcessor rawData, List<String> calculatedColumns,
                                    List<String> columnIndices)
    {
        rawDataTable = rawData;
        columnsNames = rawData.GetColumnNames();
        calculatedColumnIndices = new ArrayList<>();
        for (int i = 0; i < calculatedColumns.size(); ++i)
        {
            if (columnsNames.contains(columnIndices.get(i)))
                calculatedColumnIndices.add(columnsNames.indexOf(columnIndices.get(i)));
        }

        SetupCalculatedColumns(calculatedColumns);
    }

    public void SetupCalculatedColumns(List<String> calculatedColumns)
    {
        List<ColumnHeaderModel> calcColumnHeaders = new ArrayList<>();
        List<List<CellModel>> calcCells = new ArrayList<>();
        List<RowHeaderModel> calcRowHeaders = new ArrayList<>();

        List<RowHeaderModel> rawRowHeaders = rawDataTable.GetRowHeaders();

        /*
        Load calculated column names
         */
        for (String s : calculatedColumns)
            calcColumnHeaders.add(new ColumnHeaderModel("Avg. " + s));

        List<ColumnHeaderModel> columns = rawDataTable.GetColumns();
        for (int i = 0; i < columns.size(); ++i)
        {
            Log.e("FirebaseViewer", columns.get(i).getData() + ": " + i);
        }

        /*
        Load every unique team number
         */
        List<String> teamNumbers = new ArrayList<>();

        Log.d("FirebaseScouter", "Finding unique teams from rowheader of size: " + rawRowHeaders.size());
        for (RowHeaderModel row : rawRowHeaders)
        {
            String team = row.getData();
            if (!teamNumbers.contains(team))
                teamNumbers.add(team);
        }
/*
        teamNumbers.addAll(Stream.of(rawRowHeaders)
                .map(x -> x.getData())
                .distinct().toList());
*/

        /*
        Create a calculated row for each teamnumber
         */
        int current_row = 0;
        for (String teamNumber : teamNumbers)
        {
            Log.d("FirebaseScouter", "Doing calculations for teamnumber: " + teamNumber);

            // Get all matches for team number
            List<List<CellModel>> matches = new ArrayList<>();
            for (List<CellModel> row : rawDataTable.GetCells())
            {
                if (rawRowHeaders.get(rawDataTable.GetCells().indexOf(row))
                        .getData().equals(teamNumber))
                    matches.add(row);
            }
                            /*
                Stream.of(rawDataTable.GetCells())
                        .filter(x ->
                                rawRowHeaders.get(rawDataTable.GetCells().indexOf(x))
                                        .getData().equals(teamNumber)).toList();
                                        */

            List<CellModel> row = new ArrayList<>();
            for (int column : calculatedColumnIndices)
            {
                Log.d("FirebaseScouter", "Calculating for column: " +
                        column
                        + " with name: " + calculatedColumns.get(calculatedColumnIndices.indexOf(column)));

                List<String> values = new ArrayList<>();
                // Get raw data collection
                for (List<CellModel> s : matches)
                {
                    values.add(s.get(column).getContent().toString());
                }
                /*
                values.addAll(Stream.of(matches).map(x ->
                        x.get(column)
                                .getContent().toString()
                ).toList());
                */

                // Calculate
                Double value = doCalculatedColumn(columnsNames.get(column), values, Calculation.AVERAGE);

                // Round
                value = Math.floor(value * 1000) / 1000;

                // Add cell to row
                row.add(new CellModel(current_row + "_" + column, value.toString()));
            }
            // Add row to calculated list
            calcCells.add(row);
            calcRowHeaders.add(new RowHeaderModel(teamNumber));

            current_row++;
        }

        calculatedDataTable = new DataTableProcessor(calcColumnHeaders, calcCells, calcRowHeaders);
    }

    public DataTableProcessor GetProcessor()
    {
        return calculatedDataTable;
    }

    public HashMap<String, Integer> GetColumns()
    {
        return calculatedColumnHeaders;
    }

    public List<Integer> GetColumnIndices()
    {
        return calculatedColumnIndices;
    }

    public static double doCalculatedColumn(String columnName, List<String> columnValues,
                                                 int calculation)
    {
        switch (calculation)
        {
            case Calculation.AVERAGE:
            {
                try
                {
                    /*
                    return Stream.of(columnValues)
                            // Convert to a number
                            .mapToDouble(CalculatedTableProcessor::ConvertToDouble)
                            .average().getAsDouble();
                            */
                    double d = 0;
                    for (String s : columnValues) {
                        //Log.e("FirebaseScouter", "Averaging : " + s);
                        d += ConvertToDouble(s);
                    }

                    d /= columnValues.size();
                    return d;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.e("FirebaseScouter",
                            "Failed to do average calculation on column: " + columnName);
                    return -1;
                }
            }
            case Calculation.MAXIMUM:
                try
                {
                    return Stream.of(columnValues)
                            // Convert to number
                            .mapToDouble(CalculatedTableProcessor::ConvertToDouble)
                            .max().getAsDouble();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.e("FirebaseScouter",
                            "Failed to do max calculation on column: " + columnName);
                    return -1;
                }
            case Calculation.MINIMUM:
                try
                {
                    return Stream.of(columnValues)
                            // Convert to number
                            .mapToDouble(CalculatedTableProcessor::ConvertToDouble)
                            .min().getAsDouble();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.e("FirebaseScouter",
                            "Failed to do max calculation on column: " + columnName);
                    return -1;
                }
            default:
                return -1;
        }
    }

    /*
    Safely converts either a boolean or number string to a double, for averaging, minimizing and
    maximizing
     */
    public static double ConvertToDouble(String s)
    {
        switch (s) {
            case "true":
                return 1;
            case "false":
                return 0;
            default:
                return Double.valueOf(s);
        }
    }

    public static String getCalculatedColumnName(String column, int calculation)
    {
        switch (calculation)
        {
            case Calculation.AVERAGE:
                return column + " Average";
            case Calculation.MINIMUM:
                return column + " Minimum";
            case Calculation.MAXIMUM:
                return column + " Maximum";
            default:
                return column + "Unknown";
        }
    }
}