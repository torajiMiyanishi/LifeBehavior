package jp.soars.modules.gis_otp.role;

import java.util.Map;
import jp.soars.core.TAgentManager;
import jp.soars.core.TAgentRule;
import jp.soars.core.TRole;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;

/**
 * 次の行動をスケジュールする役割IPlanningRoleのdoItメソッドを呼び出すルール。
 */
public class TRuleOfPlanning extends TAgentRule {

    /** 次の行動をスケジュールする役割 */
    private IRoleOfPlanning fPlanningRole;

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner オーナー役割
     */
    public TRuleOfPlanning(String name, TRole owner) {
        super(name, owner);
        fPlanningRole = (IRoleOfPlanning)owner;
    }

    @Override
    public void doIt(TTime currentTime, Enum<?> currentStage, TSpotManager spotManager,
                     TAgentManager agentManager, Map<String, Object> globalSharedVariables) {
        fPlanningRole.doPlanning(currentTime, currentStage, spotManager, agentManager, globalSharedVariables);
    }
    
}
