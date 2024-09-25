package jp.soars.modules.gis_otp.role;

import java.util.concurrent.ConcurrentHashMap;
import org.opentripplanner.routing.core.TraverseModeSet;
import jp.soars.core.TSpot;
import jp.soars.modules.gis_otp.otp.TOtpResult.EOtpStatus;

/**
 * 移動情報をキャッシュするクラス．
 * 検索クエリをキー，移動情報を値としてキャッシュする．
 */
public class TTripInformationCache {

    /** 検索クエリから生成された文字列をキー，移動情報を値とするハッシュマップ */
    private static ConcurrentHashMap<String,TTripInformation> fDatabase = new ConcurrentHashMap<>();

    /**
     * 移動情報が存在するか？
     * @param key キー
     * @return true:存在する，false:存在しない
     */
    public static boolean exist(String key) {
        return fDatabase.containsKey(key);
    }

    /**
     * 検索クエリからキーを作成する．
     * @param rga GIS情報を用いて移動するエージェント役割
     * @param hour 時
     * @param minute 分
     * @param arriveBy true:到着時刻で検索する，false:出発時刻で検索する
     * @param traverseModeSet 移動手段集合
     * @param origin 出発地スポット
     * @param destination 目的地スポット
     * @return キー
     */
    public static String makeKey(TRoleOfGisAgent rga, int hour, int minute, boolean arriveBy, 
                                 TraverseModeSet traverseModeSet, TSpot origin, TSpot destination) {
        StringBuffer sb = new StringBuffer();
        int year = rga.getYearOfTimeTable();
        int month = rga.getMonthOfTimeTable();
        int day = rga.getDateOfTimeTable();
        sb.append(year).append("+").append(month).append("+").append(day).append("+");
        sb.append(hour).append("+").append(minute).append("+");
        sb.append(arriveBy).append("+").append(traverseModeSet.toString()).append("+");
        sb.append(origin.getName()).append("+").append(destination.getName());
        return sb.toString();
    }

    /**
     * 移動情報を返す．
     * @param key キー
     * @return 移動情報．存在しない場合はnullが返る．
     */
    public static TTripInformation get(String key) {
        return fDatabase.get(key);
    }

    /**
     * キーで移動情報を登録する．
     * @param key キー
     * @param startDay 移動開始日
     * @param startHour 移動開始時
     * @param startMinute 移動開始分
     * @param endDay 移動終了日
     * @param endHour 移動終了時
     * @param endMinute 移動終了分
     * @param origin 出発地スポット
     * @param destination 目的地スポット
     * @param routeID 経路ID
     * @param modes 移動手段
     * @param timeForWalking 徒歩時間
     * @param distanceForWalking 徒歩距離
     * @return 移動情報
     */
    public static TTripInformation register(String key, EOtpStatus status,
                                            int startDay, int startHour, int startMinute, 
                                            int endDay, int endHour, int endMinute, 
                                            TSpot origin, TSpot destination, int routeID,
                                            String modes, int timeForWalking, double distanceForWalking) {
        TTripInformation ti = new TTripInformation(status,
                                                   startDay, startHour, startMinute, 
                                                   endDay, endHour, endMinute, 
                                                   origin, destination, routeID,
                                                   modes, timeForWalking, distanceForWalking);
        fDatabase.put(key, ti);
        return ti;
    }

    /**
     * キーで移動情報を登録する．
     * @param key キー
     * @param status 検索結果
     * @return 移動情報
     */
    public static TTripInformation register(String key, EOtpStatus status) {
        TTripInformation ti = new TTripInformation(status);
        fDatabase.put(key, ti);
        return ti;
    }

}
