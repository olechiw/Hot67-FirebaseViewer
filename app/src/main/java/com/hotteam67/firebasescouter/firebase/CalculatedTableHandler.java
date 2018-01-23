package com.hotteam67.firebasescouter.firebase;

import android.util.Log;

import com.hotteam67.firebasescouter.tableview.tablemodel.CellModel;
import com.hotteam67.firebasescouter.tableview.tablemodel.ColumnHeaderModel;
import com.hotteam67.firebasescouter.tableview.tablemodel.RowHeaderModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Jakob on 1/19/2018.
 */

public class CalculatedTableHandler {

    private DataTableProcessor rawDataTable;
    private List<String> columnsNames;

    private DataTableProcessor calculatedDataTable;
    private HashMap<String, Integer> calculatedColumnsDataTableColumns;
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

        /*
        Load calculated column names
         */
        calculatedColumnsDataTableColumns = calculatedColumns;
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
        teamNumbers.addAll(rawDataTable.GetCells().stream()
                .map(x -> x.get(0).getContent().toString())
                .distinct().collect(Collectors.toList()));

        /*
        Create a calculated row for each teamnumber
         */
        int current_row = 0;
        for (String teamNumber : teamNumbers)
        {
            // Get all matches for team number
            List<List<CellModel>> matches = rawDataTable.GetCells().stream()
                    .filter(x -> x.get(0).getContent().toString() == teamNumber)
                    .collect(Collectors.toList());

            List<CellModel> row = new ArrayList<>();
            int currentCalculatedColumn = 0;
            int current_column = 0;
            for (HashMap.Entry<String, Integer> entry : calculatedColumns.entrySet())
            {
                int rawColumnIndex = calculatedColumnIndices.get(currentCalculatedColumn);
                currentCalculatedColumn++;

                List<String> values = new ArrayList<>();
                values.addAll(matches.stream().map(x ->
                        /*
                        Get the calculated column index, and use it to create a list of all of the
                        values for this team. The indices correspond with the entry for calculation
                        type
                         */
                        x.get(rawColumnIndex)
                                .getContent().toString()
                ).collect(Collectors.toList()));

                // Calculate
                Double value = doCalculatedColumn(columnsNames.get(rawColumnIndex), values, entry.getValue());
                // Add cell to row
                row.add(new CellModel(current_row + "_" + current_column, value.toString()));

                current_column++;
            }
            // Add row to calculated list
            calcCells.add(row);
            calcRowHeaders.add(new RowHeaderModel(teamNumber));

            current_row++;
        }
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
                    return columnValues.stream()
                            // Convert to number
                            .mapToDouble(CalculatedTableHandler::SafeConvert)
                            .average().getAsDouble();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.e("FirebaseScouter",
                            "Failed to do average calculation on column: " + columnName);
                }
                break;
            }
            case Calculation.MAXIMUM:
                try
                {
                    return columnValues.stream()
                            // Convert to number
                            .mapToDouble(CalculatedTableHandler::SafeConvert)
                            .max().getAsDouble();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.e("FirebaseScouter",
                            "Failed to do max calculation on column: " + columnName);
                }
                break;
            case Calculation.MINIMUM:
                try
                {
                    return columnValues.stream()
                            // Convert to number
                            .mapToDouble(CalculatedTableHandler::SafeConvert)
                            .min().getAsDouble();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.e("FirebaseScouter",
                            "Failed to do max calculation on column: " + columnName);
                }
                break;
        }
        return 0;
    }

    public static double SafeConvert(String value)
    {
        try
        {
            return Double.valueOf(value);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return 0;
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