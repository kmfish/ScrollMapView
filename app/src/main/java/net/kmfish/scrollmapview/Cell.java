package net.kmfish.scrollmapview;

import android.support.annotation.ColorInt;

/**
 * Created by lijun3 on 2017/12/18.
 */

public class Cell {

    public static final Cell EMPTY = new Cell();

    @ColorInt
    private int mColor;

    private int strokeColor;

    private int width, height;

    public int getColor() {
        if (0 == mColor) {
            return 0xFF000000;
        }

        return mColor;
    }

    public int getStrokeColor() {
        if (0 == strokeColor) {
            return 0xFF000000;
        }
        return strokeColor;
    }

    public void setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
    }

    public void setColor(int color) {
        mColor = color;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
