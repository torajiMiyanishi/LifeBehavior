package jp.soars.modules.gis_otp.role;

import java.util.Map;

import jp.soars.core.TAgentManager;
import jp.soars.core.TAgentRule;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;

/**
 * 出発地スポットを出発するルール．
 * 出発地スポットから途中スポットへ移動する．
 */
public class TRuleOfLeavingOrigin extends TAgentRule {

    /** 出発地スポット */
    private TSpot fOrigin;

    /** 目的地スポット */
    private TSpot fDestination;

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner オーナー役割
     */
    public TRuleOfLeavingOrigin(String name, TRole owner) { super(name, owner); }

    /**
     * 出発地スポットと目的地スポットを設定する．
     * @param origin 出発地スポット
     * @param destination 目的地スポット
     */
    public void setOriginAndDestination(TSpot origin, TSpot destination) {
        fOrigin = origin;
        fDestination = destination;
    }

    /**
     * 出発地スポットと目的地スポットをリセットする．
     */
    public void resetOriginAndDestination() {
        fOrigin = null;
        fDestination = null;
    }

    @Override
    public void doIt(TTime currentTime, Enum<?> currentStage, TSpotManager spotManager,
                     TAgentManager agentManager, Map<String, Object> globalSharedVariables) {
        boolean debugFlag = true;
        if (isAt(fOrigin)) { //出発地スポットにいるならば
            //目的地スポットに対応する途中スポットへ移動する．
            moveTo(((TRoleOfGisSpot)fDestination.getRole(ERoleName.GisSpot)).getSpotOnTheWay());
            appendToDebugInfo("success", debugFlag);
        } else {
            appendToDebugInfo("fail", debugFlag);
        }        
    }
}
