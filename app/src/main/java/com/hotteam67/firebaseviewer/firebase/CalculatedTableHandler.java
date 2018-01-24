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

public class CalculatedTableHandler {

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

    public CalculatedTableHandler(DataTableProcessor rawData)
    {
        rawDataTable = rawData;
        columnsNames = rawData.GetColumnNames();
    }

    public CalculatedTableHandler(DataTableProcessor rawData, HashMap<String, Integer> calculatedColumns,
                                  List<Integer> columnIndices)
    {
        rawDataTable = rawData;
        columnsNames = rawData.GetColumnNames();
        calculatedColumnIndices = columnIndices;

        SetupCalculatedColumns(calculatedColumns);
    }

    public void SetupCalculatedColumns(HashMap<String, Integer> calculatedColumns)
    {
        List<ColumnHeaderModel> calcColumnHeaders = new ArrayList<>();
        List<List<CellModel>> calcCells = new ArrayList<>();
        List<RowHeaderModel> calcRowHeaders = new ArrayList<>();

        List<RowHeaderModel> rawRowHeaders = rawDataTable.GetRowHeaders();

        /*
        Load calculated column names
         */
        calculatedColumnHeaders = calculatedColumns;
        for (HashMap.Entry<String, Integer> entry : calculatedColumns.entrySet())
        {
            String calculatedName = getCalculatedColumnName(entry.getKey(), entry.getValue());
            calcColumnHeaders.add(new ColumnHeaderModel(calculatedName));
        }

        /*
        Load every unique team number
         */
        List<String> teamNumbers = new ArrayList<>();

        // Basically linq, query all of the distinct team numbers and store them
        Log.d("FirebaseScouter", "Finding unique teams from rowheader of size: " + rawRowHeaders.size());
        // THIS IS ZERO FOR SOME REASON
        teamNumbers.addAll(Stream.of(rawRowHeaders)
                .map(x -> x.getData())
                .distinct().toList());

        /*
        Create a calculated row for each teamnumber
         */
        int current_row = 0;
        for (String teamNumber : teamNumbers)
        {
            Log.d("FirebaseScouter", "Doing calculations for teamnumber: " + teamNumber);
            // Get all matches for team number
            List<List<CellModel>> matches = Stream.of(rawDataTable.GetCells())
                    .filter(x ->
                            rawRowHeaders.get(rawDataTable.GetCells().indexOf(x))
                    .getData().equals(teamNumber)).toList();

            List<CellModel> row = new ArrayList<>();
            int currentCalculatedColumn = 0;
            int current_column = 0;
            for (HashMap.Entry<String, Integer> entry : calculatedColumns.entrySet())
            {
                int rawColumnIndex = calculatedColumnIndices.get(currentCalculatedColumn);
                currentCalculatedColumn++;

                List<String> values = new ArrayList<>();
                values.addAll(Stream.of(matches).map(x ->
                        /*
                        Get the calculated column index, and use it to create a list of all of the
                        values for this team. The indices correspond with the entry for calculation
                        type
                         */
                        x.get(rawColumnIndex)
                                .getContent().toString()
                ).toList());

                // Calculate
                Double value = doCalculatedColumn(columnsNames.get(rawColumnIndex), values, entry.getValue());
                value = Math.floor(value * 1000) / 1000;
                // Add cell to row
                row.add(new CellModel(current_row + "_" + current_column, value.toString()));

                current_column++;
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
                    return Stream.of(columnValues)
                            // Convert to a number
                            .mapToDouble(CalculatedTableHandler::ConvertToDouble)
                            .average().getAsDouble();
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
                            .mapToDouble(CalculatedTableHandler::ConvertToDouble)
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
                            .mapToDouble(CalculatedTableHandler::ConvertToDouble)
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
                return Double.valueOf("s");
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