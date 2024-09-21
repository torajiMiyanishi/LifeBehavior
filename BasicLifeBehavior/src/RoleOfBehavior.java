
import jp.soars.core.TAgent;
import jp.soars.core.TRole;

import java.util.*;

/**
 * 行為役割
 * @author miyanishi
 */


public final class RoleOfBehavior extends TRole {

    /** 行為間の遷移確率 */
    private Behavior.TransitionProbabilityData fTransitionProbabilityData;

    /** 現在の行為 */
    private Behavior.BehaviorType fCurrentBehavior;

    /** 行為を更新するルール */
    public static final String RULE_NAME_OF_DECIDE_BEHAVIOR = "DecideBehavior";
    /** 移動ルール */
    private static final String RULE_NAME_OF_AGENT_MOVING = "AgentMoving";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     */
    public RoleOfBehavior(TAgent owner, Behavior.BehaviorType currentBehavior, Behavior.TransitionProbabilityData transitionProbabilityData) throws Exception {
        // 親クラスのコンストラクタを呼び出す．
        // 以下の2つの引数は省略可能で，その場合デフォルト値で設定される．
        // 第3引数:この役割が持つルール数 (デフォルト値 10)
        // 第4引数:この役割が持つ子役割数 (デフォルト値 5)
        super(RoleName.Behavior, owner, 0, 0);
        fCurrentBehavior = currentBehavior;
        fTransitionProbabilityData = transitionProbabilityData;

        RuleOfAgentMoving ruleOfAgentMoving = new RuleOfAgentMoving(RULE_NAME_OF_AGENT_MOVING,this);
        new RuleOfDecideBehavior(RULE_NAME_OF_DECIDE_BEHAVIOR,this,ruleOfAgentMoving).setStage(Stage.DecideBehavior);

    }

    public Behavior.BehaviorType getNextBehavior(int current_h, int current_m, Day.DayType currentDay){
        double[] probabilities = fTransitionProbabilityData.getTransitionProbabilities(
                Behavior.BEHAVIOR_TYPE_ORDERING.indexOf(fCurrentBehavior),Day.getTimeTick(current_h,current_m),currentDay);
//        System.out.println("[probabilities] " + probabilities);
        // ルーレット選択を行う
        // 確率の合計を計算
        double sum = 0.0;
        for (double probability : probabilities) {
            sum += probability;
        }
        // 0からsumの範囲で乱数を生成
        Random rand = new Random();
        double randomValue = rand.nextDouble() * sum;
//        System.out.println("[randomValue]" + randomValue);
        // 累積確率を計算して選択する
        Behavior.BehaviorType selectedBehavior = null;
        double cumulativeProbability = 0.0;
        for (int i = 0; i < probabilities.length; i++) {
            cumulativeProbability += probabilities[i];
//            System.out.println("[cumulativeProbability] " + cumulativeProbability);
            if (randomValue <= cumulativeProbability) {
                selectedBehavior = Behavior.BEHAVIOR_TYPE_ORDERING.get(i);
                break;
            }
        }
//        System.out.println("[selectedBehavior] " + selectedBehavior);
        return selectedBehavior;
    }

    public Behavior.BehaviorType getCurrentBehavior(){
        return fCurrentBehavior;
    }

    public void setCurrentBehavior(Behavior.BehaviorType currentBehavior){
        fCurrentBehavior = currentBehavior;
    }



}
