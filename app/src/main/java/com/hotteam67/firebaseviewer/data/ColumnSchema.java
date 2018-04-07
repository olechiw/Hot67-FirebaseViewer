package com.hotteam67.firebaseviewer.data;

import com.hotteam67.firebaseviewer.data.DataTableProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jakob on 4/7/2018.
 */

public class ColumnSchema {
    public static List<String> PreferredOrder()
    {
        List<String> preferredOrder = new ArrayList<>();

        preferredOrder.add("Total Cubes");
        preferredOrder.add("Teleop Scale");
        preferredOrder.add("Teleop Switch");
        preferredOrder.add("Opponent Switch");
        preferredOrder.add("Teleop Vault");
        preferredOrder.add("Crossed Line");
        preferredOrder.add("Auton Scale");
        preferredOrder.add("Auton Switch");
        preferredOrder.add("Cubes Dropped");
        preferredOrder.add("Climbed");
        preferredOrder.add("Assisted");
        preferredOrder.add("Auton Vault");

        return preferredOrder;
    }

    public static List<String> CalculatedColumns()
    {
        List<String> calculatedColumns = new ArrayList<>();

        calculatedColumns.add("Cubes");
        calculatedColumns.add("T. Scale");
        calculatedColumns.add("T. Switch");
        calculatedColumns.add("O-Switch");
        calculatedColumns.add("T. Vault");
        calculatedColumns.add("A. Crossed");
        calculatedColumns.add("A. Scale");
        calculatedColumns.add("A. Switch");
        calculatedColumns.add("Drops");
        calculatedColumns.add("Climbed");
        calculatedColumns.add("Assisted");
        calculatedColumns.add("A. Vault");

        return  calculatedColumns;
    }

    public static List<String> CalculatedColumnsRawNames()
    {
        List<String> calculatedColumnsIndices = new ArrayList<>();

        calculatedColumnsIndices.add("Total Cubes");
        calculatedColumnsIndices.add("Teleop Scale");
        calculatedColumnsIndices.add("Teleop Switch");
        calculatedColumnsIndices.add("Opponent Switch");
        calculatedColumnsIndices.add("Teleop Vault");
        calculatedColumnsIndices.add("Crossed Line");
        calculatedColumnsIndices.add("Auton Scale");
        calculatedColumnsIndices.add("Auton Switch");
        calculatedColumnsIndices.add("Cubes Dropped");
        calculatedColumnsIndices.add("Climbed");
        calculatedColumnsIndices.add("Assisted");
        calculatedColumnsIndices.add("Auton Vault");

        return calculatedColumnsIndices;
    }

    public static List<DataTableProcessor.SumColumn> SumColumns()
    {
        DataTableProcessor.SumColumn column = new DataTableProcessor.SumColumn();
        column.columnName = "Total Cubes";
        column.columnsNames = new ArrayList<>();
        column.columnsNames.add("Auton Scale");
        column.columnsNames.add("Teleop Scale");
        column.columnsNames.add("Auton Vault");
        column.columnsNames.add("Teleop Vault");
        column.columnsNames.add("Auton Switch");
        column.columnsNames.add("Teleop Switch");

        ArrayList<DataTableProcessor.SumColumn> sumColumns = new ArrayList<>();
        sumColumns.add(column);

        return sumColumns;
    }
}
