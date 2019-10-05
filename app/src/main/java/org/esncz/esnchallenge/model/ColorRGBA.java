package org.esncz.esnchallenge.model;

import android.graphics.Color;

import java.io.Serializable;

/**
 * @author chochy
 * Date: 2019-10-05
 */
public class ColorRGBA implements Serializable {
    private double alpha;
    private double red;
    private double green;
    private double blue;

    public ColorRGBA() {
    }

    public ColorRGBA(float alpha, float red, float green, float blue) {
        this.alpha = alpha;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double getRed() {
        return red;
    }

    public void setRed(double red) {
        this.red = red;
    }

    public double getGreen() {
        return green;
    }

    public void setGreen(double green) {
        this.green = green;
    }

    public double getBlue() {
        return blue;
    }

    public void setBlue(double blue) {
        this.blue = blue;
    }

    public int asColorInt() {
        return Color.argb(floatToInt(alpha), floatToInt(red), floatToInt(green), floatToInt(blue));
    }

    public int asSolidColorInt() {
        return Color.rgb(floatToInt(red), floatToInt(green), floatToInt(blue));
    }

    private int floatToInt(double val) {
        val = Math.max(0.0, Math.min(1.0, val));
        return (int)Math.floor(val == 1.0 ? 255 : val * 256.0);
    }

    @Override
    public String toString() {
        return "ColorRGBA{rgba(" + red + ", " + green + ", " + blue + ", " + alpha + ")}";
    }
}
