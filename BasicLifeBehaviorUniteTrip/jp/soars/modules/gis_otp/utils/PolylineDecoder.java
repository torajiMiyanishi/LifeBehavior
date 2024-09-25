package jp.soars.modules.gis_otp.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Google MapのPolyline形式をデコードして緯度・経度を表示するプログラム
 */
public class PolylineDecoder {

    public static void main(String[] args) {
        //Polyline形式の文字列
        String encoded = "u}psCseytVHJ^^VVNTf@t@Zf@JNJTNZ^z@^z@FNHRn@tAJRNXNZHPFHDHFR@FD^Db@@FBb@Db@VnDe@F";
        List<double[]> decodedPath = decode(encoded);
        for (double[] latLng : decodedPath) {
            System.out.println("Latitude: " + latLng[0] + ", Longitude: " + latLng[1]);
        }
    }

    /**
     * Polyline形式の文字列を緯度・経度の配列のリストに変換する。
     * @param encoded Polyline形式の文字列
     * @return 緯度・経度の配列のリスト
     */
    public static List<double[]> decode(String encoded) {
        List<double[]> poly = new ArrayList<double[]>();
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

            double[] p = new double[2];
            p[0] = lat / 1E5;
            p[1] = lng / 1E5;
            poly.add(p);
        }
        return poly;
    }
}
