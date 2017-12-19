package net.kmfish.scrollmapview;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;

/**
 * Created by lijun3 on 2017/12/18.
 */

public class Table {

    private static final String TAG = "Table";

    private final int rowSize;
    private final int columnSize;

    private final SparseArray<SparseArray<Cell>> mCellArray;


    public Table(int row, int column) {
        this.rowSize = row;
        this.columnSize = column;

        mCellArray = new SparseArray<>(row);
    }

    public void addRow(int rowIndex, SparseArray<Cell> columnBoxs) {
        if (null == columnBoxs || columnBoxs.size() > columnSize
                || rowIndex < 0 || rowIndex >= rowSize) {

            Log.e(TAG, "addRow invalid");
            return;
        }

        mCellArray.append(rowIndex, columnBoxs);
    }

    public void removeCell(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= rowSize) {
            return;
        }

        mCellArray.remove(rowIndex);
    }

    public void clear() {
        mCellArray.clear();
    }

    @Nullable
    public SparseArray<Cell> getColumnCells(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= rowSize) {
            return null;
        }

        return mCellArray.get(rowIndex);
    }

    @NonNull
    public Cell getCell(int rowIndex, int columnIndex) {
        if (columnIndex < 0 || columnIndex >= columnSize
                || rowIndex < 0 || rowIndex >= rowSize) {
            return Cell.EMPTY;
        }

        SparseArray<Cell> columnCells = getColumnCells(rowIndex);
        if (null == columnCells) {
            return Cell.EMPTY;
        }

        return columnCells.get(columnIndex);
    }

    public int getRowSize() {
        return rowSize;
    }

    public int getColumnSize() {
        return columnSize;
    }
}
