package jp.soars.modules.gis_otp.logger;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import jp.soars.utils.csv.TCCsvData;

/**
 * 経路クラス．
 * このクラスを使うと，TRouteLoggerで出力された経路を読み込んで，Kepler.glで読み込めるjson形式で出力することができる．
 */
public class TRoute {

    /** 緯度の配列 */
    private double[] fLatitudes;

    /** 経度の配列 */
    private double[] fLongitudes;

    /** 高度の配列 */
    private double[] fAltitudes;

    /** タイムスタンプの配列 */
    private long[] fTimestamps;

    /** 一日の秒数 */
    public static long DAY_IN_SECOND = 24L * 60L * 60L;

    /**
     * コンストラクタ
     */
    public TRoute() {
    }

    /**
     * バイナリストリームから経路を読み込む．
     * @param dis バイナリストリーム
     * @return true:読込成功，false:ファイルの終わりに達した
     * @throws IOException
     */
    public boolean readFrom(DataInputStream dis) throws IOException {
        try {
            int noOfPoints = dis.readInt();
            fLatitudes = new double [noOfPoints];
            fLongitudes = new double [noOfPoints];
            fAltitudes = new double [noOfPoints];
            fTimestamps = new long [noOfPoints];
            for (int i = 0; i < noOfPoints; ++i) {
                fLatitudes[i] = dis.readDouble();
                fLongitudes[i] = dis.readDouble();
                fAltitudes[i] = dis.readDouble();
                fTimestamps[i] = dis.readLong();
            }
        } catch (EOFException eofex) {
            return false;
        }
        return true;
    }

    /**
     * Kepler.glで読み込めるJSON形式で経路を出力する．その際，引数の日でタイムスタンプを調整する．
     * @param day 日
     * @param pw 出力ストリーム
     */
    public void writeTo(int day, PrintWriter pw) {
        pw.print("{\"type\": \"Feature\",\"properties\": {\"vendor\":  \"A\"},\"geometry\": {\"type\": \"LineString\",\"coordinates\": [");
        boolean first = true;
        for (int i = 0; i < fLatitudes.length; ++i) {
            if (first) {
                first = false;
            } else {
                pw.print(",");
            }
            long timestamp = fTimestamps[i] + (long)day * DAY_IN_SECOND;
            pw.print("[" + fLatitudes[i] + "," + fLongitudes[i] + "," + fAltitudes[i] + "," + timestamp + "]");
        }
        pw.println("]}}");
    }

    /**
     * TRouteLoggerで出力されたファイルから複数の経路を読み込んで返す．
     * @param path ファイル名
     * @return 経路のリスト
     * @throws IOException
     */
    public static List<TRoute> readTrips(String path) throws IOException {
        DataInputStream dis = new DataInputStream(new FileInputStream(path));
        ArrayList<TRoute> trips = new ArrayList<>();
        while (true) {
            TRoute trip = new TRoute();
            if (!trip.readFrom(dis)) {
                break;
            }
            trips.add(trip);    
        }    
        dis.close();
        return trips;
    }

    public static void main(String[] args) throws IOException {
        List<TRoute> trips = readTrips("log/otp_role/routes.obj");
        TCCsvData personTrips = new TCCsvData("log/otp_role/person_trips.csv");
        PrintWriter pw = new PrintWriter("log/trips.json");
        pw.println("{\"type\": \"FeatureCollection\",\"features\": [");
        boolean first = true;
        for (int i = 0; i < personTrips.getNoOfRows(); ++i) {
            int startDay = personTrips.getElementAsInteger(i, "start_day");
            if (startDay == 1 || startDay == 2) {
                int routeID = personTrips.getElementAsInteger(i, "route_id");
                    if (first) {
                        first = false;
                    } else {
                        pw.print(",");
                    }
                    trips.get(routeID).writeTo(startDay, pw);
            }
        }
        pw.println("]}");
        pw.close();
    }

}
