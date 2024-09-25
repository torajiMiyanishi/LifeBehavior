package jp.soars.modules.gis_otp.role;

import java.util.List;

import jp.soars.core.TSpot;
import jp.soars.core.TSpotManager;

/**
 * 途中スポット作成器クラス
 */
public class TSpotOnTheWayMaker {

    /**
     * 目的地スポットに対応する途中スポットを生成する
     * @param spotManager スポット管理
     * @param destinationSpots 目的地スポットのリスト
     * @param spotType 途中スポットのタイプ
     * @return 途中スポットのリスト
     */
    public static List<TSpot> create(TSpotManager spotManager, 
                                     List<TSpot> destinationSpots, Enum<?> spotType) {
        int noOfSpots = destinationSpots.size();
        List<TSpot> spotsOnTheWay = spotManager.createSpots(spotType, noOfSpots);
        for (int i = 0; i < noOfSpots; ++i) {
            TSpot spot = destinationSpots.get(i);
            TSpot spotOnTheWay = spotsOnTheWay.get(i);
            ((TRoleOfGisSpot)spot.getRole(jp.soars.modules.gis_otp.role.ERoleName.GisSpot)).setSpotOnTheWay(spotOnTheWay);
            new TRoleOfSpotOnTheWay(spotOnTheWay, spot);
        }
        return spotsOnTheWay;
    }


}
