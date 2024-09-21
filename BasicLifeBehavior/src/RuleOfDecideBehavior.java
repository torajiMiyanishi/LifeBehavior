import java.util.Map;

import jp.soars.core.TAgentManager;
import jp.soars.core.TAgentRule;
import jp.soars.core.TRole;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;
import jp.soars.core.TRule;


/**
 * 会社から自宅に移動するルール
 * @author miyanishi
 */


public final class RuleOfDecideBehavior extends TAgentRule {

    /** 移動するルール */
    private final TRule fRuleOfAgentMoving;


    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールをもつ役割
     */
    public RuleOfDecideBehavior(String name, TRole owner, RuleOfAgentMoving ruleOfAgentMoving) {
        // 親クラスのコンストラクタを呼び出す．
        super(name, owner);
        fRuleOfAgentMoving = ruleOfAgentMoving;
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
        // その時刻、現在の行為種別に依存した遷移確率をもとに次のステップの行為種別を決定する。
        if ( ! (currentTime.getDay() == 0 && currentTime.getHour() == 0 && currentTime.getMinute() == 0)){
            boolean debugFlag = true;
            RoleOfBehavior behaviorRole = (RoleOfBehavior) getOwnerRole();
            Behavior.BehaviorType nextBehavior = behaviorRole.getNextBehavior(currentTime.getHour(),currentTime.getMinute(),Day.getDay(currentTime.getDay()));

            /** 現在と次の行為で、外出/在宅が異なる場合、移動ルールを次のステージに予約 */
            Behavior.BehaviorType currentBehavior = behaviorRole.getCurrentBehavior();
            if (! Behavior.BEHAVIOR_LOCATION_LABEL.get(currentBehavior).equals(Behavior.BEHAVIOR_LOCATION_LABEL.get(nextBehavior))){
                fRuleOfAgentMoving.setTimeAndStage(
                        currentTime.getDay(),currentTime.getHour(),currentTime.getMinute(),currentTime.getSecond(),Stage.AgentMoving);
//                System.out.println("Rule Reservation @RuleOfDecideBehavior");
            }

            // 行為を更新
            behaviorRole.setCurrentBehavior(nextBehavior);
        }


    }
}