package com.example.mymap.Utility;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PolylineDecoder {
    public static double rounding = 1E5;
    public static String decodePolyToString(String encoded, String initial) {
        StringBuilder resultString = new StringBuilder("[");

        if (initial != null) {
            resultString.append("[").append(initial).append("]").append(",");
        }

        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            resultString.append("[").append(lat / rounding).append(",").append(lng / rounding).append("],");
        }

        resultString.deleteCharAt(resultString.length() - 1);
        resultString.append("]");

        return resultString.toString();
    }

    public static ArrayList<String> decodePolyToList(String encoded, String initial) {
        ArrayList<String> resultList = new ArrayList<>();

        if (initial != null) {
            resultList.add(initial);
        }

        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            resultList.add((lat / rounding) + "," + (lng / rounding));
        }

        return resultList;
    }

    public static String decodePolyListToString(ArrayList<String> polyList) {
        StringBuilder sb = new StringBuilder("[");
        for (String coord : polyList) {
            sb.append("[");
            sb.append(coord);
            sb.append("],");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("]");
        return sb.toString();
    }
}
