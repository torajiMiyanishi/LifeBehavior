package jp.soars.modules.gis_otp.role;

import java.util.Map;

import jp.soars.core.TAgentManager;
import jp.soars.core.TAgentRule;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;

/**
 * 目的地スポットへ到着するルール．
 * 途中スポットから目的地へ移動する．
 */
public class TRuleOfArrivingAtDestination extends TAgentRule {

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner オーナー役割
     */
    public TRuleOfArrivingAtDestination(String name, TRole owner) {
        super(name, owner);
    }

    @Override
    public void doIt(TTime currentTime, Enum<?> currentStage, TSpotManager spotManager,
                     TAgentManager agentManager, Map<String, Object> globalSharedVariables) {
        boolean debugFlag = true;
        TSpot currentSpot = getCurrentSpot();
        TRoleOfSpotOnTheWay role = (TRoleOfSpotOnTheWay)currentSpot.getRole(ERoleName.SpotOnTheWay); //途中スポット役割
        if (role == null) {
            System.err.println(currentSpot.getName());
        }
        moveTo(role.getDestinationSpot()); //現在の途中スポットに対応する目的地スポットへ移動する．
        appendToDebugInfo("success", debugFlag);
    }

    
}
