package jp.soars.modules.gis_otp.role;

import jp.soars.core.TRole;
import jp.soars.core.TSpot;

/**
 * GISスポット役割
 */
public class TRoleOfGisSpot extends TRole {

    /** 緯度 */
    private double fLatitude;

    /** 経度 */
    private double fLongitude;

    /** 途中スポット */
    private TSpot fSpotOnTheWay;

    /**
     * コンストラクタ
     * @param owner この役割を持つスポット
     * @param latitude 緯度
     * @param longitude 経度
     */
    public TRoleOfGisSpot(TSpot owner, double latitude, double longitude) {
        super(ERoleName.GisSpot, owner);
        fLatitude = latitude;
        fLongitude = longitude;
    }

    /**
     * 緯度を返す。
     * @return 緯度
     */
    public double getLatitude() {
        return fLatitude;
    }

    /**
     * 経度を返す。
     * @return 経度
     */
    public double getLongitude() {
        return fLongitude;
    }

    /**
     * 途中スポットを返す．
     * @return 途中スポット
     */
    public TSpot getSpotOnTheWay() {
        return fSpotOnTheWay;
    }

    /**
     * 途中スポットを設定する．
     * @param spot 途中スポット
     */
    public void setSpotOnTheWay(TSpot spot) {
        fSpotOnTheWay = spot;
    }
    
}
