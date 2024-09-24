
import jp.soars.core.TAgent;
import jp.soars.core.TRole;

import java.util.*;

/**
 * 行為役割
 * @author miyanishi
 */


public final class RoleOfBehavior extends TRole {

    /** 行為関係 */
    private Behavior.BehaviorType fCurrentBehavior; // 現在のステップの行為
    private static final int MAX_HISTORY_SIZE = 8;
    private Queue<Behavior.BehaviorType> fBehaviorHistories = new LinkedList<>();


    /** 属性ラベル */
    private Behavior.AttributeType fAttributeType;

    /** 行為を更新するルール */
    public static final String RULE_NAME_OF_DECIDE_BEHAVIOR = "DecideBehavior";
    /** 移動ルール */
//    private static final String RULE_NAME_OF_AGENT_MOVING = "AgentMoving";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     */
    public RoleOfBehavior(TAgent owner, int age, String gender_id){
        // 親クラスのコンストラクタを呼び出す．
        // 以下の2つの引数は省略可能で，その場合デフォルト値で設定される．
        // 第3引数:この役割が持つルール数 (デフォルト値 10)
        // 第4引数:この役割が持つ子役割数 (デフォルト値 5)
        super(RoleName.Behavior, owner, 0, 0);
        // 属性の生成とセット
        String genderStr    = (gender_id.equals("0")) ? "MALE" : "FEMALE";
        fAttributeType = Behavior.generateAttributeType(age, genderStr);
        // 現在の行為の生成とセット
        fCurrentBehavior = Behavior.getBehaviorProbByTime(fAttributeType,Day.getDay(0)).getBehaviorByRate(getRandom());

        new RuleOfDecideBehavior(RULE_NAME_OF_DECIDE_BEHAVIOR,this).setStage(Stage.DecideBehavior);

//        RuleOfAgentMoving ruleOfAgentMoving = new RuleOfAgentMoving(RULE_NAME_OF_AGENT_MOVING,this);
//        new RuleOfDecideBehavior(RULE_NAME_OF_DECIDE_BEHAVIOR,this,ruleOfAgentMoving).setStage(Stage.DecideBehavior);
    }

    // 行為の履歴に記録するメソッド
    private void addBehaviorHistories(Behavior.BehaviorType behavior){
        if (fBehaviorHistories.size() >= MAX_HISTORY_SIZE) {
            fBehaviorHistories.poll();  // 最も古い要素を削除
        }
        fBehaviorHistories.add(behavior);  // 新しい行為を追加
    }

    /* Getter */
    public Behavior.BehaviorType getCurrentBehavior(){
        return fCurrentBehavior;
    }
    public Behavior.AttributeType getAttributeType(){return fAttributeType;}

    /* Setter */
    public void setCurrentBehavior(Behavior.BehaviorType currentBehavior){
        fCurrentBehavior = currentBehavior; // 現在の行為をセット
        addBehaviorHistories(currentBehavior);// 行為の履歴に追加
    }






}
