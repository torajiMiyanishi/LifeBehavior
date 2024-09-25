package jp.soars.modules.gis_otp.logger;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 人の移動をCSVファイルに記録するロガー．
 * 各移動について，人ID，移動開始日，移動開始時間，移動開始分，移動終了日，移動終了時間，移動終了分，
 * 出発スポット，到着スポット，経路ID，移動モード，徒歩時間（秒），徒歩距離（メートル）が記録される．
 * 経路IDはTRouteLoggerで出力される経路番号に対応する．
 * シミュレーションのメインループの外側でopenとcloseメソッドを呼ぶことにより，ロギングの開始と終了ができる．
 * 各エージェントのロールにおいて，適宜writeを使って移動情報を出力する．
 */
public class TPersonTripLogger {

    /** 出力ストリーム */
    private static PrintWriter fOutput = null;

    /**
     * ログファイルをオープンする．
     * [filename].csvと[filename]_routes.objがディレクトリlogDirの下に出力される．
     * 前者がcsvファイル，後者が経路IDに対応する経路が格納されたバイナリファイルである．
     * @param logDir ログを出録するディレクトリ
     * @param filename ファイル名．
     * @throws IOException 
     */
    public static void open(String logDir, String filename) throws IOException {
        TRouteLogger.open(logDir + File.separator + filename + "_routes.obj");
        fOutput = new PrintWriter(logDir + File.separator + filename + ".csv");
        fOutput.println("person_id,start_day,start_hour,start_minute,end_day,end_hour,end_minute,origin,destination,route_id,modes,waking_time,waking_distance");
    }

    /**
     * ログファイルをクローズする．
     * @throws IOException 
     */
    public static void close() throws IOException {
        fOutput.close();
        TRouteLogger.close();
        fOutput = null;
    }

    /**
     * 移動データを出力する．
     * @param person_id 人ID
     * @param start_day 移動開始日
     * @param start_hour 移動開始時間
     * @param start_minute 移動開始分
     * @param end_day 移動終了日
     * @param end_hour 移動終了時間
     * @param end_minute 移動終了分
     * @param origin 出発スポット
     * @param destination 到着スポット
     * @param route_id 経路ID
     * @param modes 移動モード
     * @param timeForWalking 徒歩時間（秒）
     * @param distanceForWalking 徒歩距離（メートル）
     */
    public static synchronized void write(int person_id, int start_day, int start_hour, int start_minute, 
                                          int end_day, int end_hour,int end_minute, 
                                          String origin, String destination, int route_id,
                                          String modes, int timeForWalking, double distanceForWalking) {
        if (fOutput == null) {
            return;
        }
        fOutput.println(person_id + "," + start_day + "," + start_hour + "," + start_minute + ","
                         + end_day + "," + end_hour + "," + end_minute + "," + origin + "," + destination + ","
                         + route_id + "," + modes + "," + timeForWalking + "," + distanceForWalking);
    }
    
}
