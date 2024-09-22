import java.util.Map;

import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;

import jp.soars.core.TAgent;
import jp.soars.core.TAgentManager;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;
import jp.soars.modules.gis_otp.otp.TOtpResult.EOtpStatus;
import jp.soars.modules.gis_otp.role.EStage;
import jp.soars.modules.gis_otp.role.IRoleOfPlanning;
import jp.soars.modules.gis_otp.role.TRoleOfGisAgent;
import jp.soars.modules.gis_otp.role.TRuleOfPlanning;
import jp.soars.modules.gis_otp.role.TTripInformation;

/**
 * Person役割．
 * Person役割は，年齢に関係なく，自宅で午前6時に，日中の活動場所までの移動について計画する．
 * 自宅での計画においては，日中の活動場所まで経路検索を行い，それに基づいた活動場所までの移動と，
 * 活動場所に到着後の計画をスケジュールする．ただし，なるべく徒歩とバスでの移動を選択するが，
 * 20分以上歩かなければならない場合，または，午前10時までに活動場所に到着しない場合，または，
 * 午後8時までに自宅に到着しない場合は，行きも帰りも車での移動を選択するものとする．
 * 出発時刻になったら自宅を出発し，途中スポットを経由して，到着時刻に活動場所に移動する．
 * 活動場所での計画においては，17時以降に帰宅するように帰りの経路検索を行い，それに基づいた自宅までの移動と
 * 次の日の計画をスケジュールする．
 */
public class RoleOfPerson extends TRole implements IRoleOfPlanning {

    /** 計画をするルール */
    public static final String RULE_NAME_OF_PLANNING = "Plan";

    /** TRoleOfGisAgentの役割名 */
    public static final Enum<?> GIS_AGENT = jp.soars.modules.gis_otp.role.ERoleName.GisAgent;

    /** 自宅 */
    private TSpot fHome;

    /** 日中の活動場所 */
    private TSpot fWorkPlace;

    /** 自宅を出発する時刻 */
    private TTime fTimeOfLeavingHome;

    /** 活動場所を出発する時刻 */
    private TTime fTimeOfLeavingWorkPlace;


    /**
     * コンストラクタ
     * @param owner オーナーエージェント
     * @param home 家
     * @param workPlace 日中の活動場所
     * @param timeOfLeavingHome 家を出発する時刻
     * @param timeOfLeavingWorkPlace 活動場所を出発する時刻
     */
    public RoleOfPerson(TAgent owner, TSpot home, TSpot workPlace,
                         TTime timeOfLeavingHome, TTime timeOfLeavingWorkPlace) {
        super(RoleName.Person, owner);
        fHome = home;
        fWorkPlace = workPlace;
        fTimeOfLeavingHome = timeOfLeavingHome;
        fTimeOfLeavingWorkPlace = timeOfLeavingWorkPlace;
        TRuleOfPlanning planning = new TRuleOfPlanning(RULE_NAME_OF_PLANNING, this);
        planning.setTimeAndStage(0, fTimeOfLeavingHome.getHour() - 1, 0, 0, EStage.AgentPlanning); //家を出発する1時間前に計画
    }

    /**
     * 次の行動を計画する。
     * @param currentTime 現在時刻
     * @param currentStage 現在ステージ
     * @param spotManager スポット管理
     * @param agentManager エージェント管理
     * @param globalSharedVariables グローバル共有変数集合
     */
    @Override
    public void doPlanning(TTime currentTime, Enum<?> currentStage, TSpotManager spotManager,
                           TAgentManager agentManager, Map<String, Object> globalSharedVariables) {
        TAgent owner = (TAgent)getOwner(); //オーナーエージェント
        TRoleOfGisAgent rga = (TRoleOfGisAgent)owner.getRole(GIS_AGENT);
        TRuleOfPlanning planning = (TRuleOfPlanning)getRule(RULE_NAME_OF_PLANNING); //計画ルール
        TraverseModeSet traverseModeSet = determineModeSet(rga); //移動手段を決定する．
        if (owner.isAt(fHome)) { //家にいたら
            int hour = fTimeOfLeavingHome.getHour(); //家を出発する時
            int minute = fTimeOfLeavingHome.getMinute(); //家を出発する分
            boolean arriveBy = false; //出発時刻で検索
            TTripInformation ti = rga.findRoute(hour, minute, arriveBy, traverseModeSet, fHome, fWorkPlace); //経路検索
            if (ti == null) { //経路が見つからない場合，以降動かない（＝計画ルールをスケジュールせずにリターンする）
                System.err.println("Failed to find a route at home.");
                return;
            }
            rga.scheduleToMove(currentTime.getDay(), ti); //移動をスケジュール
            //移動終了時刻の１分後に計画ルールをスケジュールする．
            int planningHour = ti.getEndHour();
            int planningMinute = ti.getEndMinute();
            if (planningMinute == 59) {
                ++planningHour;
                planningMinute = 0;
            } else {
                ++planningMinute;
            }
            planning.setTimeAndStage(currentTime.getDay(), planningHour, planningMinute, 0, EStage.AgentPlanning);
        } else if (owner.isAt(fWorkPlace)) { //日中の活動場所にいたら
            int hour = fTimeOfLeavingWorkPlace.getHour(); //活動場所を出発する時
            int minute = fTimeOfLeavingWorkPlace.getMinute(); //活動場所を出発する分
            boolean arriveBy = false; //出発時刻で検索
            TTripInformation ti = rga.findRoute(hour, minute, arriveBy, traverseModeSet, fWorkPlace, fHome); //経路検索
            if (ti == null) { //経路が見つからない場合，以降動かない（＝計画ルールをスケジュールせずにリターンする）
                System.err.println("Failed to find a route at home.");
                return;
            }
            rga.scheduleToMove(currentTime.getDay(), ti); //移動をスケジュール
            //明日の出発時刻の１時間前に計画ルールをスケジュールする．
            planning.setTimeAndStage(currentTime.getDay() + 1, fTimeOfLeavingHome.getHour() - 1, 0, 0, EStage.AgentPlanning);
        }
    }

    /**
     * 以下のルールに従って，移動手段を決定する．
     * - なるべく徒歩とバスを選択する．
     * - 行きに，バスを使った場合，徒歩時間が20分以上，または，到着時刻が10時以降になる場合は，車を選択する．
     * - 帰りに，バスを使った場合，徒歩時間が20分以上，または，到着時刻が20時以降になる場合は，車を選択する．
     * - 行きと帰りは同じ移動手段となるように，行きの移動手段を選択する．すなわち，行きに車を使った場合，帰りも車を使う．
     *   また，帰りにバスを使うと徒歩時間が20分以上，または，到着時刻が20時以降になる場合は，最初から車を選択する．
     * @return 移動手段
     */
    private TraverseModeSet determineModeSet(TRoleOfGisAgent rga) {
        int hour = fTimeOfLeavingHome.getHour(); //自宅を出発する時刻（時）
        int minute = fTimeOfLeavingHome.getMinute(); //自宅を出発する時刻（分）
        boolean arriveBy = false; //出発時刻で検索
        TraverseModeSet traverseModeSet = new TraverseModeSet(TraverseMode.TRANSIT,TraverseMode.WALK); //徒歩，バス
        TTripInformation ti = rga.findRoute(hour, minute, arriveBy, traverseModeSet, fHome, fWorkPlace); //経路検索
        if (ti.getSearchStatus() != EOtpStatus.SUCCESS //経路が見つからない
                || ti.getTimeForWalking() > 20L * 60L //徒歩時間が20分以上
                || ti.getEndHour() >= 10) { //到着時刻が10時以降
            return new TraverseModeSet(TraverseMode.CAR); //行きも帰りも車
        } else { //行きは車を使わなくても行ける場合
            hour = fTimeOfLeavingWorkPlace.getHour(); //日中の活動場所を出発する時刻（時）
            minute = fTimeOfLeavingWorkPlace.getMinute(); //日中の活動場所を出発する時刻（分）
            arriveBy = false; //出発時刻で検索
            traverseModeSet = new TraverseModeSet(TraverseMode.TRANSIT,TraverseMode.WALK); //徒歩，バス
            ti = rga.findRoute(hour, minute, arriveBy, traverseModeSet, fWorkPlace, fHome); //経路検索
            if (ti.getSearchStatus() != EOtpStatus.SUCCESS //経路が見つからない
                    || ti.getTimeForWalking() > 20L * 60L //徒歩時間が20分以上
                    || ti.getEndHour() >= 20 || ti.getEndDay() > 0) { //帰宅時刻が20時以降．
                return new TraverseModeSet(TraverseMode.CAR); //行きも帰りも車
            } else { //帰りも歩きとバスで帰れる場合
                return new TraverseModeSet(TraverseMode.TRANSIT,TraverseMode.WALK); //行きも帰りも歩きとバス
            }
        }
    }

}

