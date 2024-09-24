import jp.soars.core.TAgent;
import jp.soars.core.TSpot;
import jp.soars.core.TTime;
import jp.soars.modules.synthetic_population.ERoleName;
import jp.soars.modules.synthetic_population.TRoleOfSyntheticPopulationData;

import java.util.*;

/**
 * マルコフ連鎖で決定した行為に応じて，移動の有無や，移動先を意思決定するクラス．
 * */

public class BehaviorLocator {
    /**
     * 場所性に依存する行為において，目的候補地を返す．
     * @param behaviorType
     * @return
     */
    public static List<TSpot> getCandidateSpots(TAgent ownerAgent, Behavior.BehaviorType behaviorType, TTime curerntTime){

        List<TSpot> candidateSpots = new ArrayList<>();
        TRoleOfSyntheticPopulationData spdRole = (TRoleOfSyntheticPopulationData) ownerAgent.getRole(ERoleName.SyntheticPopulationData);
        Behavior.IndustryType industryType = Behavior.INDUSTRY_ID_TO_INDUSTRY_TYPE.get(spdRole.getSyntheticPopulationData().getIndustryTypeId());
//        if (industryType == null){
//            System.out.println("spdRole.getSyntheticPopulationData().getIndustryTypeId() >>> " + spdRole.getSyntheticPopulationData().getIndustryTypeId() + " @BehaviorLocator");
//        }
        RoleOfResident residentRole = (RoleOfResident) ownerAgent.getRole(RoleName.Resident);
        TSpot currentSpot = ownerAgent.getCurrentSpot();
        TSpot homeSpot = residentRole.getHome();
        boolean isAtHome = (currentSpot == homeSpot);
        RoleOfPoi currentPoi = null;
        if(!isAtHome){
            currentPoi = (RoleOfPoi) currentSpot.getRole(RoleName.Poi);
//            System.out.println("[currentPoi] "+ currentPoi + " , [currentSpot] "+ currentSpot.getName() + " @BehaviorLocator");
        }

//        System.out.println(behaviorType+":isAt:" + ownerAgent.getCurrentSpot().getName() + " isAtHome:" + isAtHome + " @BehaviorLocator");
        switch (Behavior.BEHAVIOR_LOCATION_DECISION_TYPE.get(behaviorType)) {

            case HOME_ONLY: // 自宅のみで可能な行為 >> 自宅に移動
                if (!isAtHome) { // 現在自宅にいなければ
                    candidateSpots.add(homeSpot); // 自宅を候補に追加
                }
                break;

            case WORK:
                if (!isAtHome){
                    if (industryType != Behavior.IndustryType.NOT_ASSIGNED) { // 職種割り当てが存在する．
                        if (!currentPoi.getIndustryTypes().contains(industryType)) { // 現在地が職場でない場合．
                            if (MapApp.INDUSTRY_TYPE_TO_POI_SPOTS.get(industryType) == null) {
//                                System.out.println(industryType + " @BehaviorLocator");
                                candidateSpots.addAll(MapApp.INDUSTRY_TYPE_TO_POI_SPOTS.get(industryType)); // 当該産業分類の候補地を全追加．
                            }
                        }
                    }
                }
                break;

            case BOTH:
                if (!isAtHome){
                    if (!currentPoi.getBehaviorTypes().contains(behaviorType)){
                        // 現在位置では，行為が実行できない状態．すなわち移動が必要．
                        candidateSpots.add(homeSpot);//自宅を候補に追加
                        candidateSpots.addAll(MapApp.BEHAVIOR_TYPE_TO_POI_SPOTS.get(behaviorType));//行為種別に紐づく候補地をすべて追加
                    }
                }
                break;

            case NOT_HOME:
                if (isAtHome){
                    // 現在位置では，行為が実行できない状態．すなわち移動が必要．
                    candidateSpots.addAll(MapApp.BEHAVIOR_TYPE_TO_POI_SPOTS.get(behaviorType));//行為種別に紐づく候補地をすべて追加
                } else {
                    if (!currentPoi.getBehaviorTypes().contains(behaviorType)) {
                        // 現在位置では，行為が実行できない状態．すなわち移動が必要．
                        candidateSpots.addAll(MapApp.BEHAVIOR_TYPE_TO_POI_SPOTS.get(behaviorType));//行為種別に紐づく候補地をすべて追加
                    }
                }
                break;

            case TRAVELING:
                if(curerntTime.isLessThan(12,0,0)){ // 午前中
                    if (isAtHome){ // 自宅にいる場合
                        if (behaviorType == Behavior.BehaviorType.COMMUTING) {
                            candidateSpots.addAll(MapApp.getSpotsByIndustryType(industryType)); // 通勤先群
                        } else if (behaviorType == Behavior.BehaviorType.SCHOOL_COMMUTING) {
                            candidateSpots.addAll(MapApp.getSpotsByBehaviorType(behaviorType)); // 通学先群
                        }
                    } else { // 自宅にいない場合
//                        System.out.println(ownerAgent.getCurrentSpot().getName() +" @BehaviorLocator");
                        if (behaviorType == Behavior.BehaviorType.COMMUTING // 通勤中で
                                && !currentPoi.getIndustryTypes().contains(industryType)){ // 会社にいない場合
                            candidateSpots.addAll(MapApp.getSpotsByIndustryType(industryType)); // 通勤先群
                        } else if (behaviorType == Behavior.BehaviorType.SCHOOL_COMMUTING // 通学中で
                                && !currentPoi.getBehaviorTypes().contains(behaviorType)) { // 学校のにいない場合
                            candidateSpots.addAll(MapApp.getSpotsByBehaviorType(behaviorType)); // 通学先群
                        }
                    }
                } else { // 午後
                    if (!isAtHome){ // 自宅にいない場
                        candidateSpots.add(homeSpot); // 自宅に移動
                    }
                }
        }
        return candidateSpots;
    }
}
