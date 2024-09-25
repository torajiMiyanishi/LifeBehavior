package jp.soars.modules.gis_otp.logger;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.locationtech.jts.geom.LineString;
import org.opentripplanner.common.geometry.SphericalDistanceLibrary;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.spt.GraphPath;

import jp.soars.modules.gis_otp.otp.TOtpResult;

/**
 * 経路のロガークラス．
 */
public class TRouteLogger {

    /** UTC/協定世界時とJST/日本標準時の時差（秒） */
    static final long TIME_DIFFERENCE_UTC_JST_IN_SECONDS = 9L * 3600L;

    /** 経路ID */
    private static int fRouteID = 0;

    /** 出力ストリーム */
    private static DataOutputStream fOutput = null;

    /**
     * ロガーファイルをオープンする．
     * @param path ファイルパス
     * @throws IOException 
     */
    public static void open(String path) throws IOException {
        fOutput = new DataOutputStream(new FileOutputStream(path));
    }

    /**
     * 旅程番号で指定された経路を出力する．
     * @param result 検索結果
     * @param itineraryIndex 旅程番号
     * @return 経路ID
     * @throws IOException
     */
    public static synchronized int write(TOtpResult result, int itineraryIndex) throws IOException {
        if (fOutput == null) {
            return -1;
        }
        int id = fRouteID;
        writeRoute(result, itineraryIndex);
        ++fRouteID;
        return id;
    }

    /**
     * ログファイルをクローズする．
     * @throws IOException
     */
    public static void close() throws IOException {
        fOutput.close();
        fOutput = null;
    }

    /**
     * 座標とタイムスタンプを出力する。
     * @param longitude 経度
     * @param latitude 緯度
     * @param altitude 標高
     * @param timestamp タイムスタンプ
     * @return 自分自身
     * @throws IOException 
     */
    private static void writePoint(double longitude, double latitude, double altitude, long timestamp) throws IOException {
        fOutput.writeDouble(longitude);
        fOutput.writeDouble(latitude);
        fOutput.writeDouble(altitude);
        fOutput.writeLong(timestamp);
    }

    /**
     * LineStringを出力する。
     * @param line LineString
     * @param distance LineStringの長さ
     * @param startTime 始点の時刻
     * @param endTime 終点の時刻
     * @throws IOException 
     */
    private static void writeLineString(LineString line, double distance, long startTime, long endTime) throws IOException {
        double x = line.getPointN(0).getCoordinate().getX();
        double y = line.getPointN(0).getCoordinate().getY();
        writePoint(x, y, 0.0, startTime);
        double d = 0.0;
        for (int i = 1; i < line.getNumPoints(); ++i) {
            d += SphericalDistanceLibrary.distance(line.getPointN(i).getCoordinate(), 
                                                   line.getPointN(i - 1).getCoordinate());
            long time = (long)((double)(endTime - startTime) * (d / distance)) + startTime;
            x = line.getPointN(i).getCoordinate().getX();
            y = line.getPointN(i).getCoordinate().getY();
            writePoint(x, y, 0.0, time);
        }
    }

    /**
     * 経路に含まれる地点数を返す．
     * @param states 経路を構成する状態
     * @return 地点数
     */
    private static int countPoints(List<State> states) {
        int count = 0;
        for (int i = 0; i < states.size(); ++i) {
            State s = states.get(i);
            if (s.getBackEdge() != null) {
                LineString line = s.getBackEdge().getGeometry();
                if (line != null) {
                    count += line.getNumPoints();
                }
            }
        }
        return count;
    }

    /**
     * 経路をバイナリ形式で出力する．
     * @param result 検索結果
     * @param itineraryIndex 旅程番号
     * @throws IOException
     */
    private static void writeRoute(TOtpResult result, int itineraryIndex) throws IOException {
        GraphPath path = result.getGraphPath(itineraryIndex);
        List<State> states = path.states;
        fOutput.writeInt(countPoints(states)); //Pointの数
        for (int i = 0; i < states.size(); ++i) {
            State s = states.get(i);
            if (s.getBackEdge() != null) {
                LineString line = s.getBackEdge().getGeometry();
                if (line != null) {
                    double distance = s.getBackEdge().getDistance();
                    long endTime = s.getTimeSeconds() + TIME_DIFFERENCE_UTC_JST_IN_SECONDS;
                    long startTime = s.getBackState().getTimeSeconds() + TIME_DIFFERENCE_UTC_JST_IN_SECONDS;
                    writeLineString(line, distance, startTime, endTime);
                }
            }
        }
    }
    
}
