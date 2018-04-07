package com.hotteam67.firebaseviewer;

import com.hotteam67.firebaseviewer.firebase.CalculatedTableProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jakob on 4/7/2018.
 */

public class ColumnSchema {
    public static List<String> PreferredOrder()
    {
        List<String> preferredOrder = new ArrayList<>();

        preferredOrder.add("Teleop Scale");
        preferredOrder.add("Teleop Switch");
        preferredOrder.add("Teleop Vault");
        preferredOrder.add("Crossed Line");
        preferredOrder.add("Auton Scale");
        preferredOrder.add("Auton Switch");
        preferredOrder.add("Auton Vault");
        preferredOrder.add("Climbed");
        preferredOrder.add("Assisted");
        preferredOrder.add("Was Assisted");

        return preferredOrder;
    }

    public static List<String> CalculatedColumns()
    {
        List<String> calculatedColumns = new ArrayList<>();

        calculatedColumns.add("T. Scale");
        calculatedColumns.add("T. Switch");
        calculatedColumns.add("T. Vault");
        calculatedColumns.add("A. Crossed");
        calculatedColumns.add("A. Scale");
        calculatedColumns.add("A. Switch");
        calculatedColumns.add("A. Vault");
        calculatedColumns.add("Climbed");
        calculatedColumns.add("Assisted");

        return  calculatedColumns;
    }

    public static List<String> CalculatedColumnsRawNames()
    {
        List<String> calculatedColumnsIndices = new ArrayList<>();

        calculatedColumnsIndices.add("Teleop Scale");
        calculatedColumnsIndices.add("Teleop Switch");
        calculatedColumnsIndices.add("Teleop Vault");
        calculatedColumnsIndices.add("Crossed Line");
        calculatedColumnsIndices.add("Auton Scale");
        calculatedColumnsIndices.add("Auton Switch");
        calculatedColumnsIndices.add("Auton Vault");
        calculatedColumnsIndices.add("Climbed");
        calculatedColumnsIndices.add("Assisted");

        return calculatedColumnsIndices;
    }

    public static List<CalculatedTableProcessor.SumColumn> SumColumns()
    {
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

        return sumColumns;
    }
}
