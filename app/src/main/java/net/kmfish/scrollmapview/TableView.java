package net.kmfish.scrollmapview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

/**
 * Created by lijun3 on 2017/12/18.
 */

public class TableView extends View {

    @NonNull
    private Table mTable;

    @NonNull
    private Scroller mScroller;

    @Nullable
    private Cell mCurrentCell;

    private final int BG_COLOR = 0xFFFFFFFF;
    private final int CELL_COLOR = 0xFF000000;

    private Paint mCellPaint;

    private Rect mTempRect;

    private int lastPointX, lastPointY;
    private int lastMoveX, lastMoveY;

    private boolean isNewEvent;


    public TableView(Context context) {
        this(context, null);
    }

    public TableView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TableView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mScroller = new Scroller(getContext());

        mCellPaint = new Paint();
        mCellPaint.setColor(CELL_COLOR);
        mCellPaint.setStyle(Paint.Style.FILL);

        mTempRect = new Rect();
    }

    public void setTable(@NonNull Table table) {
        mTable = table;

        mCurrentCell = mTable.getCell(0, 0);

        invalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        } else {
            requestLayout();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mScroller.forceFinished(true);
                isNewEvent = true;
                lastPointX = (int) event.getX();
                lastPointY = (int) event.getY();

                break;
            case MotionEvent.ACTION_MOVE:
//                if (isNewEvent) {
//                    if (Math.abs(lastPointX - event.getX()) > 100) {
//                        isNewEvent = false;
//                    } else if (Math.abs(lastPointY - event.getY()) > 50) {
//                            isNewEvent = false;
//                        } else {
//                            return false;
//                        }
//                    }

                int totalMoveX = (int) (lastPointX - event.getX()) + lastMoveX;
                int totalMoveY = (int) (lastPointY - event.getY()) + lastMoveY;
//                smoothScrollTo(totalMoveX, totalMoveY);
                scrollTo(totalMoveX, totalMoveY);

                break;
            case MotionEvent.ACTION_UP:
                lastMoveX = getScrollX();
                lastMoveY = getScrollY();
                break;
            default:
                break;
        }

        return true;
    }

        @Override
        protected void onDraw (Canvas canvas){
            super.onDraw(canvas);

            canvas.drawColor(BG_COLOR);

            drawBox(canvas);
        }

    private void drawBox(Canvas canvas) {
        int startX = 0, startY = 0;

        int cellW = 0;
        int cellH = 0;

        for (int i = 0; i < mTable.getRowSize(); i++) {
            for (int j = 0; j < mTable.getColumnSize(); j++) {
                Cell cell = mTable.getCell(i, j);
                drawBox(canvas, cell, startX, startY, String.format("(%d, %d)", i, j));
                if (cellW == 0) {
                    cellW = cell.getWidth();
                }
                if (cellH == 0) {
                    cellH = cell.getHeight();
                }

                startX += cellW;
            }

            startX = 0;
            startY += cellH;
        }
    }

    private void drawBox(Canvas canvas, Cell cell, int startX, int startY, String text) {
        canvas.save();
        canvas.translate(startX, startY);

        mTempRect.set(0, 0, cell.getWidth(), cell.getHeight());
        mCellPaint.setColor(cell.getColor());
        mCellPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(mTempRect, mCellPaint);

        mCellPaint.setStyle(Paint.Style.STROKE);
        mCellPaint.setColor(cell.getStrokeColor());
        canvas.drawRect(mTempRect, mCellPaint);

        mCellPaint.setColor(Color.BLACK);
        mCellPaint.setTextSize(MeasureUtil.dp2px(getContext(), 16));

        int textWidth = (int) mCellPaint.measureText(text);

        canvas.drawText(text, mTempRect.centerX() - textWidth / 2, mTempRect.centerY(), mCellPaint);

        canvas.restore();
    }

    private void smoothScrollTo(int fx, int fy) {
        int dx = fx - mScroller.getFinalX();
        int dy = fy - mScroller.getFinalY();
        smoothScrollBy(dx, dy);
    }

    private void smoothScrollBy(int dx, int dy) {
        mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), dx, dy, 200);
        invalidate();
    }

}
