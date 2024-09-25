import jp.soars.core.*;
import org.locationtech.jts.geom.Triangle;

import java.util.Map;


/**
 * 移動行動が終了する時刻に，自身でTripperRoleをdeactivateするルール
 * @author miyanishi
 */


public final class RuleOfDeactivate extends TAgentRule {

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールをもつ役割
     */
    public RuleOfDeactivate(String name, TRole owner) {
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
//        System.out.println("deactivate が発火しました　@RuleOfDeactivate");
        RoleOfTripper tripperRole = (RoleOfTripper) getAgent().getRole(RoleName.Tripper);
        tripperRole.endTripExecution();
    }
}