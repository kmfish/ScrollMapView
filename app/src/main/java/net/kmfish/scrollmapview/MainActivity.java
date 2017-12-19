package net.kmfish.scrollmapview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;

public class MainActivity extends AppCompatActivity {

    private TableView mTableView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTableView = findViewById(R.id.table_view);
        initData();
    }

    private void initData() {

        mTableView.setTable(loadData());
    }

    private Table loadData() {
        int rowSize = 5;
        int columnSize = 5;

        int cellWidth = 400;
        int cellHeight = 400;

        Table table = new Table(rowSize, columnSize);
        for (int i = 0; i < rowSize; i++) {

            SparseArray<Cell> row = new SparseArray<>();
            for (int j = 0; j < columnSize; j++) {
                Cell cell = new Cell();
                cell.setWidth(cellWidth);
                cell.setHeight(cellHeight);

                float percent = 0.2f * (1 + 1.5f * j);
                int color = ColorUtils.caculateColor(0xFFFF0000, 0xFF880000, percent);
                cell.setColor(color);

                row.append(j, cell);
            }

            table.addRow(i, row);
        }

        return table;

    }
}
