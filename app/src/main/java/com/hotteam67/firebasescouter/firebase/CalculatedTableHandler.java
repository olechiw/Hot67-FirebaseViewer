package com.hotteam67.firebasescouter.firebase;

import com.hotteam67.firebasescouter.tableview.tablemodel.CellModel;
import com.hotteam67.firebasescouter.tableview.tablemodel.ColumnHeaderModel;
import com.hotteam67.firebasescouter.tableview.tablemodel.RowHeaderModel;

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
    private HashMap<String, Integer> calculatedColumnsDataTableColumns;

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

    public CalculatedTableHandler(DataTableProcessor rawData, HashMap<String, Integer> calculatedColumns)
    {
        rawDataTable = rawData;
        columnsNames = rawData.GetColumnNames();

        List<String> teamNumbers = new ArrayList<>();

        List<ColumnHeaderModel> calcColumnHeaders = new ArrayList<>();
        List<List<CellModel>> calcCells = new ArrayList<>();
        List<RowHeaderModel> calcRowHeaders = new ArrayList<>();

        calculatedColumnsDataTableColumns = calculatedColumns;
        for (HashMap.Entry<String, Integer> entry : calculatedColumns.entrySet())
        {
            String calculatedName = getCalculatedColumnName(entry.getKey(), entry.getValue());
            calcColumnHeaders.add(new ColumnHeaderModel(calculatedName));
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