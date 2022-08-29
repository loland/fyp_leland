package com.example.mymap.Utility;

import android.util.Log;

import java.util.HashMap;

public class GeoCalculator {
//    static HashMap<Integer, String> degreesToClockMap;
//    static {
//        degreesToClockMap = new HashMap<Integer, String>();
//        degreesToClockMap.put(0, "12");
//        degreesToClockMap.put(1, "1");
//        degreesToClockMap.put(2, "2");
//        degreesToClockMap.put(3, "3");
//        degreesToClockMap.put(4, "4");
//        degreesToClockMap.put(5, "5");
//        degreesToClockMap.put(6, "6");
//        degreesToClockMap.put(7, "7");
//        degreesToClockMap.put(8, "8");
//        degreesToClockMap.put(9, "9");
//        degreesToClockMap.put(300, "10");
//        degreesToClockMap.put(330, "11");
//        degreesToClockMap.put(360, "12");
//    }
    public static int compare(double lat1, double lon1, double lat2, double lon2) {  // generally used geo measurement function
        float radius = (float) 6378.137; // Radius of earth in KM
        double distanceLat = lat2 * Math.PI / 180 - lat1 * Math.PI / 180;
        double distanceLong = lon2 * Math.PI / 180 - lon1 * Math.PI / 180;
        double a = Math.sin(distanceLat/2) * Math.sin(distanceLat/2) +
                Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                        Math.sin(distanceLong/2) * Math.sin(distanceLong/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = radius * c;
        return (int) (d * 1000); // meters
    }

    public static int compareString(String start, String end) {  // generally used geo measurement function
        String[] startList = start.split(",", 2);
        String[] endList = end.split(",", 2);

        double lat1 = Double.parseDouble(startList[0]);
        double lon1 = Double.parseDouble(startList[1]);
        double lat2 = Double.parseDouble(endList[0]);
        double lon2 = Double.parseDouble(endList[1]);

        Log.d("GeolocationCalculator/measureString", String.format("start: (%s,%s), end: (%s,%s)", lat1, lon1, lat2, lon2));

        float radius = (float) 6378.137; // Radius of earth in KM
        double distanceLat = lat2 * Math.PI / 180 - lat1 * Math.PI / 180;
        double distanceLong = lon2 * Math.PI / 180 - lon1 * Math.PI / 180;
        double a = Math.sin(distanceLat/2) * Math.sin(distanceLat/2) +
                Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                        Math.sin(distanceLong/2) * Math.sin(distanceLong/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = radius * c;

        Log.d("GeolocationCalculator/measureString", "distance: " + (int) (d * 1000));
        return (int) (d * 1000); // meters
    }

    public static int degreeBearing(String start, String end) {
        String[] startList = start.split(",", 2);
        String[] endList = end.split(",", 2);

        double lat1 = Double.parseDouble(startList[0]);
        double lon1 = Double.parseDouble(startList[1]);
        double lat2 = Double.parseDouble(endList[0]);
        double lon2 = Double.parseDouble(endList[1]);

        double dLon = ToRad(lon2-lon1);
        double dPhi = Math.log(Math.tan(ToRad(lat2)/2+Math.PI/4)/Math.tan(ToRad(lat1)/2+Math.PI/4));
        if (Math.abs(dLon) > Math.PI)
            dLon = dLon > 0 ? -(2*Math.PI-dLon) : (2*Math.PI+dLon);
        return (int) ToBearing(Math.atan2(dLon, dPhi));
    }

    public static double ToRad(double degrees)
    {
        return degrees * (Math.PI / 180);
    }

    public static double ToDegrees(double radians)
    {
        return radians * 180 / Math.PI;
    }

    public static double ToBearing(double radians)
    {
        // convert radians to degrees (as bearing: 0...360)
        return (ToDegrees(radians) +360) % 360;
    }

    public static String degreesToClock(int degrees) {
        degrees = Math.round((float) degrees / 30);
        if (degrees == 0) {
            return "12";
        }
        return String.valueOf(degrees);
    }
}
