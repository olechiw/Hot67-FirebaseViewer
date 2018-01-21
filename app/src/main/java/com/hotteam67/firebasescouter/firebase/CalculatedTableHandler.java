package com.hotteam67.firebasescouter.firebase;

import android.util.Log;

import java.util.List;

/**
 * Created by Jakob on 1/19/2018.
 */

public class CalculatedTableHandler {

    private RawTableHandler rawTableHandler;
    private List<String> columnsNames;

    public CalculatedTableHandler(RawTableHandler rawData)
    {
        rawTableHandler = rawData;
        columnsNames = rawData.GetColumnNames();
    }
}