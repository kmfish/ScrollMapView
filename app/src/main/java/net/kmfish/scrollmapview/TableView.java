package net.kmfish.scrollmapview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.EdgeEffect;
import android.widget.OverScroller;


/**
 * Created by lijun3 on 2017/12/18.
 */

public class TableView extends View {

    private static final String TAG = "TableView";

    @NonNull
    private Table mTable;

    @NonNull
    private OverScroller mScroller;

    @Nullable
    private Cell mCurrentCell;

    private final int BG_COLOR = 0xFFFFFFFF;
    private final int CELL_COLOR = 0xFF000000;

    private Paint mCellPaint;

    private Rect mTempRect;

    private int lastPointX, lastPointY;
    private int lastMoveX, lastMoveY;

    private int mActivePointerId = INVALID_POINTER;
    private static final int INVALID_POINTER = -1;

    private boolean isNewEvent;
    private boolean mIsBeingDragged = false;

    private EdgeEffect mEdgeEffectTop, mEdgeEffectBottom, mEdgeEffectLeft, mEdgeEffectRight;


    private int mTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;

    private int mOverscrollDistance;
    private int mOverflingDistance;

    private float mVerticalScrollFactor;

    private VelocityTracker mVelocityTracker;

    private int mContentHeight;


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
        mScroller = new OverScroller(getContext());

        mCellPaint = new Paint();
        mCellPaint.setColor(CELL_COLOR);
        mCellPaint.setStyle(Paint.Style.FILL);

        mTempRect = new Rect();

        mEdgeEffectLeft = buildEdgeEffect();
        mEdgeEffectTop = buildEdgeEffect();
        mEdgeEffectRight = buildEdgeEffect();
        mEdgeEffectBottom = buildEdgeEffect();

        initScrollView();
    }

    private EdgeEffect buildEdgeEffect() {
        return new EdgeEffect(getContext());
    }

    private void initScrollView() {
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mOverscrollDistance = 50;//configuration.getScaledOverscrollDistance();
        mOverflingDistance = 50;//configuration.getScaledOverflingDistance();
//        mVerticalScrollFactor = configuration.getScaledVerticalScrollFactor();

        setOverScrollMode(OVER_SCROLL_ALWAYS);

        setWillNotDraw(false); //必须！！！！
    }

    private void initVelocityTrackerIfNotExist() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    public void setTable(@NonNull Table table) {
        mTable = table;

        mCurrentCell = mTable.getCell(0, 0);

        mContentHeight = mTable.getRowSize() * mTable.getCell(0, 0).getHeight();

        invalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            Log.d(TAG, "computeScroll computeScrollOffset");

            int oldX = getScrollX();
            int oldY = getScrollY();
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();

            int range = getScrollRange();
            if (oldX != x || oldY != y) {
                overScrollBy(x - oldX, y - oldY, oldX, oldY, range, range, mOverflingDistance, mOverflingDistance, false);
            }
            final int overScrollMode = getOverScrollMode();
            final boolean canOverScroll = overScrollMode == OVER_SCROLL_ALWAYS ||
                    (overScrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && range > 0);
            if (canOverScroll) {
                if (y < 0 && oldY >= 0) {
                    mEdgeEffectTop.onAbsorb((int) mScroller.getCurrVelocity());
                } else if (y > range && oldY < range) {
                    mEdgeEffectBottom.onAbsorb((int) mScroller.getCurrVelocity());
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        initVelocityTrackerIfNotExist();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsBeingDragged = mScroller.isFinished();

                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }

                isNewEvent = true;
                lastPointX = (int) event.getX();
                lastPointY = (int) event.getY();
                mActivePointerId = event.getPointerId(0);

                break;
            case MotionEvent.ACTION_MOVE:
                final int activePointerIndex = event.findPointerIndex(mActivePointerId);
                if (activePointerIndex == -1) {
                    Log.e(TAG, "Invalid pointerId=" + mActivePointerId + " in onTouchEvent");
                    break;
                }

                final int y = (int) event.getY(activePointerIndex);
                final int x = (int) event.getX(activePointerIndex);
                int deltaY = lastPointY - y;
                int deltaX = lastPointX - x;

                Log.d(TAG, "onTouch move deltaY:" + deltaY);

                if (!mIsBeingDragged && Math.abs(deltaY) > mTouchSlop) {
                    mIsBeingDragged = true;
                    if (deltaY > 0) {
                        deltaY -= mTouchSlop;
                    } else {
                        deltaY += mTouchSlop;
                    }
                }
                if (!mIsBeingDragged && Math.abs(deltaX) > mTouchSlop) {
                    mIsBeingDragged = true;
                    if (deltaX > 0) {
                        deltaX -= mTouchSlop;
                    } else {
                        deltaX += mTouchSlop;
                    }
                }

                if (mIsBeingDragged) {
                    lastPointY = y;

                    overScrollBy(0, deltaY, 0, getScrollY(),
                            0, getScrollRange(), 0, mOverscrollDistance,
                            true);

                    final int pulledToY = (int) (getScrollY() + deltaY);
//                lastPointY = y;
                    if (pulledToY < 0) {
                        mEdgeEffectTop.onPull(deltaY / getHeight());

//                    mEdgeEffectTop.onPull(deltaY/getHeight(),event.getX(mActivePointerId)/getWidth());
                        if (!mEdgeEffectBottom.isFinished()) {
                            mEdgeEffectBottom.onRelease();
                        }
                    } else if (pulledToY > getScrollRange()) {
                        mEdgeEffectTop.onPull(deltaY / getHeight());

//                    mEdgeEffectBottom.onPull(deltaY/getHeight(),1.0f-event.getX(mActivePointerId)/getWidth());
                        if (!mEdgeEffectTop.isFinished()) {
                            mEdgeEffectTop.onRelease();
                        }
                    }
                    if (mEdgeEffectTop != null
                            && mEdgeEffectBottom != null && (!mEdgeEffectTop.isFinished()
                            || !mEdgeEffectBottom.isFinished())) {
                        postInvalidate();
                    }
                }

                break;
            case MotionEvent.ACTION_UP:

                if (mIsBeingDragged) {
                    mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int initialVelocity = (int) mVelocityTracker.getYVelocity(mActivePointerId);
                    Log.d(TAG, "velocity " + initialVelocity + " " + mMinimumVelocity);
                    if (Math.abs(initialVelocity) > mMinimumVelocity) {
                        // fling
                        doFling(-initialVelocity);
                    }
                    endDrag();
                }

                break;

            case MotionEvent.ACTION_CANCEL:
                endDrag();
                break;
            default:
                break;
        }

        if (mVelocityTracker != null) {
            mVelocityTracker.addMovement(event);
        }

        return true;
    }

    private void doFling(int speed) {
        Log.d(TAG, "doFling:" + speed);
        if (mScroller == null) {
            return;
        }
        mScroller.fling(0, getScrollY(), 0, speed, 0, 0, -500, 10000);
        invalidate();
    }

    private void endDrag() {
        mIsBeingDragged = false;
        recycleVelocityTracker();
        mActivePointerId = INVALID_POINTER;
        lastMoveY = 0;
        lastMoveX = 0;
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        Log.d(TAG, "onOverScrolled mScroller.isFinished():" + mScroller.isFinished());
        Log.d(TAG, String.format("onOverScrolled scrollX:%d scrollY:%d clampedX:%b clampedY:%b", scrollX, scrollY, clampedX, clampedY));

        if (!mScroller.isFinished()) {
            int oldX = getScrollX();
            int oldY = getScrollY();
            scrollTo(scrollX, scrollY);
            onScrollChanged(scrollX, scrollY, oldX, oldY);
            if (clampedY) {
                Log.e(TAG, "springBack");
                mScroller.springBack(getScrollX(), getScrollY(), 0, getScrollRange(), 0, getScrollRange());
            }
        } else {
            // TouchEvent中的overScroll调用
            super.scrollTo(scrollX, scrollY);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mEdgeEffectTop != null) {
            final int scrollY = getScrollY();
            if (!mEdgeEffectTop.isFinished()) {
                final int count = canvas.save();
                final int width = getWidth() - getPaddingLeft() - getPaddingRight();
                canvas.translate(getPaddingLeft(), Math.min(0, scrollY));
                mEdgeEffectTop.setSize(width, getHeight());
                if (mEdgeEffectTop.draw(canvas)) {
                    postInvalidate();
                }
                canvas.restoreToCount(count);
            }
        }

        if (mEdgeEffectBottom != null) {
            final int scrollY = getScrollY();
            if (!mEdgeEffectBottom.isFinished()) {
                final int count = canvas.save();
                final int width = getWidth() - getPaddingLeft() - getPaddingRight();
                canvas.translate(-width + getPaddingLeft(), Math.max(getScrollRange(), scrollY) + getHeight());
                canvas.rotate(180, width, 0);
                mEdgeEffectBottom.setSize(width, getHeight());
                if (mEdgeEffectBottom.draw(canvas)) {
                    postInvalidate();
                }
                canvas.restoreToCount(count);
            }

        }

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

    private int getScrollRange() {
        int scrollRange = 0;
        scrollRange = Math.max(0, mContentHeight - getHeight());
        Log.d(TAG, "scrollRange is " + scrollRange);
        return scrollRange;
    }

}
