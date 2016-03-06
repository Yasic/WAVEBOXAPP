package com.yasic.waveboxapp.Utils;

/**
 * Created by ESIR on 2016/3/6.
 */
public class MathUtils {
    public static double getDistance(double deLat, double deLng){
        return Math.sqrt(Math.pow(Math.abs(deLat), Math.abs(deLng)));
    }
}
