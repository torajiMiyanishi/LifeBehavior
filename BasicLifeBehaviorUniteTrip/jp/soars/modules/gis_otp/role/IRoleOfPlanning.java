package jp.soars.modules.gis_otp.role;

import java.util.Map;

import jp.soars.core.TAgentManager;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;

/**
 * 次の行動を計画する役割のインターフェイス
 */
public interface IRoleOfPlanning {

    /**
     * 次の行動を計画する。
     * @param currentTime 現在時刻
     * @param currentStage 現在ステージ
     * @param spotManager スポット管理
     * @param agentManager エージェント管理
     * @param globalSharedVariables グローバル共有変数集合
     */
    public void doPlanning(TTime currentTime, Enum<?> currentStage, TSpotManager spotManager,
                           TAgentManager agentManager, Map<String, Object> globalSharedVariables);
}
