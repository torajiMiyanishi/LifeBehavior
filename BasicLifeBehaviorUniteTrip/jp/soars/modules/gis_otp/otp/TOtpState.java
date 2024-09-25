package jp.soars.modules.gis_otp.otp;

/**
 * 移動中の状態（移動モードと位置）を表すクラス．
 * 移動モードには以下が定義されている：
 * - WALK
 * - BICYCLE
 * - CAR
 * - TRAM
 * - SUBWAY
 * - RAIL
 * - BUS
 * - FERRY
 * - CABLE_CAR
 * - GONDOLA
 * - FUNICULAR
 * - TRANSIT
 * - LEG_SWITCH
 * - AIRPLANE
 */
public class TOtpState {

    /** 移動モード */
    private String fMode;

    /** 緯度 */
    private double fLatitude;

    /** 経度 */
    private double fLongitude;

    /**
     * コンストラクタ
     * @param mode 移動モード
     * @param latitude 緯度
     * @param longitude 経度
     */
    public TOtpState(String mode, double latitude, double longitude) {
        fMode = mode;
        fLatitude = latitude;
        fLongitude = longitude;
    }
    
    @Override
    public String toString() {
        return fMode + "," + fLatitude + "," + fLongitude;
    }

    /**
     * 移動モードを返す．
     * @return 移動モード
     */
    public String getMode() {
        return fMode;
    }

    /**
     * 緯度を返す．
     * @return 緯度
     */
    public double getLatitude() {
        return fLatitude;
    }

    /**
     * 経度を返す．
     * @return 経度
     */
    public double getLongitude() {
        return fLongitude;
    }
}
