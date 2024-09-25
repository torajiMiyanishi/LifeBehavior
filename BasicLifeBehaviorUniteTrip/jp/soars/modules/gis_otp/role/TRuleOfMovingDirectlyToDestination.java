package jp.soars.modules.gis_otp.role;

import java.util.Map;

import jp.soars.core.TAgentManager;
import jp.soars.core.TAgentRule;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;

/**
 * 途中スポットを経由せずに、出発地から目的地へ直接移動する。
 * 出発地と目的地の距離が近いため，出発時刻と到着時刻が一致してしまったときに利用される．
 */
public class TRuleOfMovingDirectlyToDestination extends TAgentRule {

    /** 出発地 */
    private TSpot fOrigin;

    /** 目的地 */
    private TSpot fDestination;

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner オーナー役割
     */
    public TRuleOfMovingDirectlyToDestination(String name, TRole owner) {
        super(name, owner);
    }

    /**
     * 出発地と目的地を設定する。
     * @param origin 出発地
     * @param destination 目的地
     */
    public void setOriginAndDestination(TSpot origin, TSpot destination) {
        fOrigin = origin;
        fDestination = destination;
    }

    /**
     * 出発地と目的地をリセットする。
     */
    public void resetOriginAndDestination() {
        fOrigin = null;
        fDestination = null;
    }

    @Override
    public void doIt(TTime currentTime, Enum<?> currentStage, TSpotManager spotManager,
                     TAgentManager agentManager, Map<String, Object> globalSharedVariables) {
        boolean debugFlag = true;
        if (isAt(fOrigin)) {
            moveTo(fDestination);
            appendToDebugInfo("success", debugFlag);
        } else {
            appendToDebugInfo("fail", debugFlag);
        }        
    }
}
