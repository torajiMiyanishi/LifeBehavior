import jp.soars.core.*;
import jp.soars.modules.gis_otp.otp.TOtpResult.EOtpStatus;
import jp.soars.modules.gis_otp.role.*;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;

import javax.management.relation.Role;
import java.util.*;

/**
 * Tripper役割．
 * Person役割を参考に，自宅とpoiを同列に扱う移動を実行する役割
 * 出発時刻制約のみ考慮する
 *
 * poiのジャンルを入力値とし，目的地の決定から実際の移動まで行う
 */
public class RoleOfTripper extends TRole implements IRoleOfPlanning {

    /** 計画をするルール */
    public static final String RULE_NAME_OF_PLANNING = "Plan";

    /** 移動行動が終了したら，フラグを操作する */
    public static final String RULE_NAME_OF_DEACTIVATE = "Deactivate";
    /** フラグ操作ルール */
    public TRule fSwitchIsTraveling;

    /** TRoleOfGisAgentの役割名 */
    public static final Enum<?> GIS_AGENT = jp.soars.modules.gis_otp.role.ERoleName.GisAgent;

    /** 旅行中か否か */
    private boolean fIsTraveling;

    /** 起点 */
    private TSpot fOriginSpot;
    /** 優先順位付けされた終点poiの候補地 */
    private List<TSpot> fPrioritizedPois;
    /** 現在時刻 */
    private TTime fCurrentTime;

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
    public RoleOfTripper(TAgent owner, TSpot home, TSpot workPlace,
                         TTime timeOfLeavingHome, TTime timeOfLeavingWorkPlace) {
        super(RoleName.Person, owner);
        fHome = home;
        fWorkPlace = workPlace;
        fTimeOfLeavingHome = timeOfLeavingHome;
        fTimeOfLeavingWorkPlace = timeOfLeavingWorkPlace;
        TRuleOfPlanning planning = new TRuleOfPlanning(RULE_NAME_OF_PLANNING, this);
        planning.setTimeAndStage(0, fTimeOfLeavingHome.getHour() - 1, 0, 0, EStage.AgentPlanning); //家を出発する1時間前に計画
    }
    public RoleOfTripper(TAgent owner, TTime time){
        super(RoleName.Tripper, owner);
        fCurrentTime = time;
        fIsTraveling = false;
    }

    /** Getter,Setter */
    public boolean isTraveling() { return fIsTraveling; }
    public void setIsTraveling(boolean isTraveling){ fIsTraveling = isTraveling;}


    /**
     * 動的な行動計画の予約
     * */
    public void reservePlanning(TSpot originSpot, List<TSpot> candidatePois){
        fOriginSpot = originSpot;
        fPrioritizedPois = prioritizeCandidates(candidatePois); // poiを評価して優先順位付け
        if (getRule(RULE_NAME_OF_PLANNING) == null && getOwner().activateRole(RoleName.Tripper)){
            TRuleOfPlanning planning = new TRuleOfPlanning(RULE_NAME_OF_PLANNING, this);
            planning.setTimeAndStage(fCurrentTime.getDay(), fCurrentTime.getHour(), fCurrentTime.getMinute(), 0, EStage.AgentPlanning); //叩かれたその時に計画
        } else {
            System.err.println("Try to reserve the duplicated rule");
            System.exit(1);
        }
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
//    public void doPlanning(TTime currentTime, Enum<?> currentStage, TSpotManager spotManager,
//                           TAgentManager agentManager, Map<String, Object> globalSharedVariables) {
//        TAgent owner = (TAgent)getOwner(); //オーナーエージェント
//        TRoleOfGisAgent rga = (TRoleOfGisAgent)owner.getRole(GIS_AGENT);
//        TRuleOfPlanning planning = (TRuleOfPlanning)getRule(RULE_NAME_OF_PLANNING); //計画ルール
////        TraverseModeSet traverseModeSet = determineModeSet(rga); //移動手段を決定する．
//        TraverseModeSet traverseModeSet = determineModeSet(); //CAR固定でテスト
//        if (owner.isAt(fHome)) { //家にいたら
//            int hour = fTimeOfLeavingHome.getHour(); //家を出発する時
//            int minute = fTimeOfLeavingHome.getMinute(); //家を出発する分
//            boolean arriveBy = false; //出発時刻で検索
//            TTripInformation ti = rga.findRoute(hour, minute, arriveBy, traverseModeSet, fHome, fWorkPlace); //経路検索
//            if (ti == null) { //経路が見つからない場合，以降動かない（＝計画ルールをスケジュールせずにリターンする）
//                System.err.println("Failed to find a route at home.");
//                return;
//            }
//            rga.scheduleToMove(currentTime.getDay(), ti); //移動をスケジュール
//            //移動終了時刻の１分後に計画ルールをスケジュールする．
//            int planningHour = ti.getEndHour();
//            int planningMinute = ti.getEndMinute();
//            if (planningMinute == 59) {
//                ++planningHour;
//                planningMinute = 0;
//            } else {
//                ++planningMinute;
//            }
//            planning.setTimeAndStage(currentTime.getDay(), planningHour, planningMinute, 0, EStage.AgentPlanning);
//        } else if (owner.isAt(fWorkPlace)) { //日中の活動場所にいたら
//            int hour = fTimeOfLeavingWorkPlace.getHour(); //活動場所を出発する時
//            int minute = fTimeOfLeavingWorkPlace.getMinute(); //活動場所を出発する分
//            boolean arriveBy = false; //出発時刻で検索
//            TTripInformation ti = rga.findRoute(hour, minute, arriveBy, traverseModeSet, fWorkPlace, fHome); //経路検索
//            if (ti == null) { //経路が見つからない場合，以降動かない（＝計画ルールをスケジュールせずにリターンする）
//                System.err.println("Failed to find a route at home.");
//                return;
//            }
//            rga.scheduleToMove(currentTime.getDay(), ti); //移動をスケジュール
//            //明日の出発時刻の１時間前に計画ルールをスケジュールする．
//            planning.setTimeAndStage(currentTime.getDay() + 1, fTimeOfLeavingHome.getHour() - 1, 0, 0, EStage.AgentPlanning);
//        }
//    }
    public void doPlanning(TTime currentTime, Enum<?> currentStage, TSpotManager spotManager,
                           TAgentManager agentManager, Map<String, Object> globalSharedVariables){
        TAgent owner = (TAgent)getOwner(); //オーナーエージェント
        TRoleOfGisAgent rga = (TRoleOfGisAgent)owner.getRole(GIS_AGENT);
        //        TraverseModeSet traverseModeSet = determineModeSet(rga); //移動手段を決定する．
        TraverseModeSet traverseModeSet = determineModeSet(); //CAR固定でテスト

        /**
         * 優先順位順に，pathが張れるかチェックし，張れたらそこへ移動．
         * 張れなければ，時点の候補地をチェック．以降繰り返し
         * */
        int hour = currentTime.getHour(); //家を出発する時
        int minute = currentTime.getMinute(); //家を出発する分
        boolean arriveBy = false; //出発時刻で検索
        TTripInformation ti = null;
        for (TSpot poiSpot: fPrioritizedPois){
            ti = rga.findRoute(hour, minute, arriveBy, traverseModeSet, ((TAgent) getOwner()).getCurrentSpot(), poiSpot); //経路検索
            if (ti != null) { //経路が見つかったら
                break;
            }
        }
        if (ti != null){
            rga.scheduleToMove(currentTime.getDay(), ti); // 移動をスケジュール
            RuleOfDeactivate deactivate = new RuleOfDeactivate(RULE_NAME_OF_DEACTIVATE, this);//移動終了時に旅行中フラグをfalseにする
            deactivate.setTimeAndStage(ti.getEndDay(),ti.getEndHour(),ti.getEndMinute(),0,Stage.Deactivate);//旅行計画が正常にスケジュールされた場合は，trip終了時刻に不活性化
        } else {
            System.err.println("Failed to find any route.");
//            deactivateSelf.setTimeAndStage(currentTime.getDay(),currentTime.getHour(),currentTime.getMinute()+1,0,Stage.DeactivateTripper);//旅行計画が失敗した場合，即刻不活性化．
            System.exit(1);
            /**
             * 今後，どこにも移動できなくなってしまう状況に陥ることが発生しうる場合，対処法をここに記述
             */
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
    /**
     * 目的地の優先順位をつけるメソッド。
     * 現状ではランダムに並び替えるが、将来的に評価値やハフモデルなどを実装予定。
     *
     * @param candidateSpots 候補となるスポットのリスト
     * @return ランダムに並び替えられたスポットリスト
     */
    public List<TSpot> prioritizeCandidates(List<TSpot> candidateSpots) {
        // 候補リストをコピーする
        List<TSpot> prioritizedSpots = new ArrayList<>(candidateSpots);

        // ランダムに並び替える
        Collections.shuffle(prioritizedSpots);

        return prioritizedSpots;
    }


    /**
     * テスト用
     * 強制的に自動車で移動させる
     * */
    private TraverseModeSet determineModeSet(){
        return new TraverseModeSet(TraverseMode.CAR);
    }

}

