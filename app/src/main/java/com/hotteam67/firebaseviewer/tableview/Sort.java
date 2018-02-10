package com.hotteam67.firebaseviewer.tableview;

import com.hotteam67.firebaseviewer.firebase.DataTableProcessor;
import com.hotteam67.firebaseviewer.tableview.tablemodel.CellModel;
import com.hotteam67.firebaseviewer.tableview.tablemodel.ColumnHeaderModel;
import com.hotteam67.firebaseviewer.tableview.tablemodel.RowHeaderModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by Jakob on 2/8/2018.
 */

public final class Sort {
    public static DataTableProcessor BubbleSortDescendingByRowHeader(DataTableProcessor input)
    {
        List<ColumnHeaderModel> columns = input.GetColumns();
        List<List<CellModel>> cells = input.GetCells();
        List<RowHeaderModel> rows = input.GetRowHeaders();

        boolean changed = true;
        while (changed)
        {
            changed = false;
            try {
                for (int i = 0; i < cells.size(); ++i)
                {
                    List<CellModel> row = cells.get(i);

                    int value = Integer.valueOf(rows.get(i).getData());

                    if (i + 1 >= cells.size())
                        continue;

                    int nextValue = Integer.valueOf(
                            rows.get(i + 1).getData());

                    if (value > nextValue)
                    {
                        cells.set(i, cells.get(i + 1));
                        RowHeaderModel prevRow = rows.get(i);
                        rows.set(i, rows.get(i + 1));
                        cells.set(i + 1, row);
                        rows.set(i + 1, prevRow);
                        changed = true;
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return new DataTableProcessor(columns, cells, rows);
    }

    public static DataTableProcessor BubbleSortDescendingByColumn(DataTableProcessor input, int column)
    {
        List<ColumnHeaderModel> columns = input.GetColumns();
        List<List<CellModel>> cells = input.GetCells();
        List<RowHeaderModel> rows = input.GetRowHeaders();

        boolean changed = true;
        while (changed)
        {
            changed = false;
            try {
                for (int i = 0; i < cells.size(); ++i)
                {
                    List<CellModel> row = cells.get(i);

                    double value = Double.valueOf(row.get(column).getData().toString());

                    if (i + 1 >= cells.size())
                        continue;

                    double nextValue = Double.valueOf(
                            cells.get(i + 1).get(column).getData().toString());

                    if (value < nextValue)
                    {
                        cells.set(i, cells.get(i + 1));
                        RowHeaderModel prevRow = rows.get(i);
                        rows.set(i, rows.get(i + 1));
                        cells.set(i + 1, row);
                        rows.set(i + 1, prevRow);
                        changed = true;
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return new DataTableProcessor(columns, cells, rows);
    }
}
