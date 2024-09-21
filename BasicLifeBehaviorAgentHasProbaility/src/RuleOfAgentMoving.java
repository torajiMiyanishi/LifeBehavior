import jp.soars.core.*;

import java.util.Map;


/**
 * 会社から自宅に移動するルール
 * @author miyanishi
 */


public final class RuleOfAgentMoving extends TAgentRule {

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールをもつ役割
     */
    public RuleOfAgentMoving(String name, TRole owner) {
        // 親クラスのコンストラクタを呼び出す．
        super(name, owner);
    }

    /**
     * ルールを実行する．
     * @param currentTime 現在時刻
     * @param currentStage 現在ステージ
     * @param spotManager スポット管理
     * @param agentManager エージェント管理
     * @param globalSharedVariables グローバル共有変数集合
     */
    @Override
    public final void doIt(TTime currentTime, Enum<?> currentStage, TSpotManager spotManager,
                           TAgentManager agentManager, Map<String, Object> globalSharedVariables) {
        // DecideBehaviorにて外出に分類される行為に更新されたら、家の外に移動。その逆も含む。
        boolean debugFlag = true;
        RoleOfBehavior behaviorRole = (RoleOfBehavior) getOwnerRole();
        RoleOfResident residentRole = (RoleOfResident) getAgent().getRole(RoleName.Resident);
        if (Behavior.BEHAVIOR_LOCATION_LABEL.get(behaviorRole.getCurrentBehavior()) == Behavior.LocationType.OUTDOOR){
            moveTo(residentRole.getVisitedLocation()); // 外出へ
        } else {
            moveTo(residentRole.getHome()); // 自宅へ
        }

    }
}