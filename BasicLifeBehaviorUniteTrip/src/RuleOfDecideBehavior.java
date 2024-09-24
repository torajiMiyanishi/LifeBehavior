import java.util.*;

import jp.soars.core.*;


/**
 * 会社から自宅に移動するルール
 * @author miyanishi
 */


public final class RuleOfDecideBehavior extends TAgentRule {

    /** 移動するルール */
//    private final TRule fRuleOfAgentMoving;


    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールをもつ役割
     */
    public RuleOfDecideBehavior(String name, TRole owner) {
        // 親クラスのコンストラクタを呼び出す．
        super(name, owner);
//        fRuleOfAgentMoving = ruleOfAgentMoving;
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
        boolean isSimulationStartStep = (currentTime.getDay() == 0 && currentTime.getHour() == 0 && currentTime.getMinute() == 0); // シミュレーション開始ステップは行為者率で行為を与える
        RoleOfTripper tripperRole = (RoleOfTripper) getAgent().getRole(RoleName.Tripper);

//        System.out.println("doIt is ignition " + getAgent().getCurrentSpot().getName() + ":" + tripperRole.isActive() + " @RuleOfDecideBehavior");
        if (!isSimulationStartStep && !tripperRole.isActive()) {// 移動中は行為の更新は行わない
            /** マルコフ連鎖を用いて次の行為を決定 */
            RoleOfBehavior behaviorRole = (RoleOfBehavior) getOwnerRole();
            Behavior.BehaviorType nextBehavior = Behavior.getNextBehavior(behaviorRole.getAttributeType(),behaviorRole.getCurrentBehavior()
                    , currentTime.getHour(),currentTime.getMinute(),Day.getDay(currentTime.getDay()),getRandom());

            /** 現在と次の行為で、外出/在宅が異なる場合、移動ルールを次のステージに予約 */
            Behavior.BehaviorType currentBehavior = behaviorRole.getCurrentBehavior();
            if (currentBehavior == nextBehavior) { // 前後で行為種別が同じ
                behaviorRole.setCurrentBehavior(nextBehavior);// 行為を更新
            } else { // 前後で行為種別が異なる
                if (Behavior.BEHAVIOR_LOCATION_LABEL.get(nextBehavior) == Behavior.LocationDependency.LOCATION_INDEPENDENT){ // 次の行為種別が活動場所を限定されない
                    // 行為の実行場所が限定されない．>> 移動を必要としない
                    behaviorRole.setCurrentBehavior(nextBehavior);// 行為を更新
                } else { // 次の行為種別が活動場所を限定する可能性を有する．
                    List<TSpot> candidateSpots = BehaviorLocator.getCandidateSpots(getAgent(),nextBehavior,currentTime);
                    if (!candidateSpots.isEmpty()) { // 候補が存在する場合
//                        System.out.println(getAgent().getCurrentSpot().getName() + ":" + tripperRole.isActive() + " @RuleOfDecideBehavior");
                        tripperRole.reservePlanning(getAgent().getCurrentSpot(),candidateSpots); // 移動を計画する．
                    }
                }
            }
        }
    }
}