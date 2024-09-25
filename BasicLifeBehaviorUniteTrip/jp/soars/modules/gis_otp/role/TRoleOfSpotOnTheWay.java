package jp.soars.modules.gis_otp.role;

import jp.soars.core.TRole;
import jp.soars.core.TSpot;

/**
 * 途中スポット役割
 */
public class TRoleOfSpotOnTheWay extends TRole {

    /** 目的地スポット */
    private TSpot fDestinationSpot;

    /**
     * コンストラクタ
     * @param owner オーナースポット
     * @param destination 目的地スポット
     */
    public TRoleOfSpotOnTheWay(TSpot owner, TSpot destination) {
        super(ERoleName.SpotOnTheWay, owner);
        fDestinationSpot = destination;
    }

    /**
     * 目的地スポットを返す．
     * @return 目的地スポット
     */
    public TSpot getDestinationSpot() {
        return fDestinationSpot;
    }
    
}
