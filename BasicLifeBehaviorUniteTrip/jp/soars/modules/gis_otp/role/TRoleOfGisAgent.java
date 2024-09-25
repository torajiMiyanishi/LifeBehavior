package jp.soars.modules.gis_otp.role;

import java.io.IOException;
import java.util.Calendar;

import org.opentripplanner.routing.core.TraverseModeSet;
import jp.soars.core.TAgent;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;
import jp.soars.modules.gis_otp.logger.TPersonTripLogger;
import jp.soars.modules.gis_otp.logger.TRouteLogger;
import jp.soars.modules.gis_otp.otp.TOtpResult;
import jp.soars.modules.gis_otp.otp.TOtpRouter;
import jp.soars.modules.gis_otp.otp.TThreadLocalOfOtpRouter;
import jp.soars.modules.gis_otp.otp.TOtpResult.EOtpStatus;
import jp.soars.modules.synthetic_population.TRoleOfSyntheticPopulationData;

/**
 * GIS情報を用いて移動するエージェント役割
 */
public class TRoleOfGisAgent extends TRole {

    /** 出発地を出発するルール */
    public static final String RULE_NAME_OF_LEAVING_ORIGIN = "LeaveOrigin";

    /** 目的地に到着するルール */
    public static final String RULE_NAME_OF_ARRIVING_AT_DESTINATION = "ArriveAtDestination";

    /** 目的地へ直接移動するルール */
    public static final String RULE_NAME_OF_MOVING_DIRECTLY_TO_DESTINATION = "MoveDirectlyToDestination";

    /** 経路探索を行う年のデフォルト値 */
    public static final int DEFAULT_YEAR_OF_TIME_TABLE = 2024;

    /** 経路探索を行う月のデフォルト値 */
    public static final int DEFAULT_MONTH_OF_TIME_TABLE = 4;

    /** 経路探索を行う日のデフォルト値 */
    public static final int DEFAULT_DATE_OF_TIME_TABLE = 24;

    /** 経路探索を行う年 */
    private int fYearOfTimeTable = DEFAULT_YEAR_OF_TIME_TABLE;

    /** 経路探索を行う月 */
    private int fMonthOfTimeTable = DEFAULT_MONTH_OF_TIME_TABLE;

    /** 経路探索を行う日 */
    private int fDateOfTimeTable = DEFAULT_DATE_OF_TIME_TABLE;

    /**
     * コンストラクタ
     * @param owner オーナーエージェント
     */
    public TRoleOfGisAgent(TAgent owner) {
        super(ERoleName.GisAgent, owner);
        new TRuleOfLeavingOrigin(RULE_NAME_OF_LEAVING_ORIGIN, this);
        new TRuleOfArrivingAtDestination(RULE_NAME_OF_ARRIVING_AT_DESTINATION, this);
        new TRuleOfMovingDirectlyToDestination(RULE_NAME_OF_MOVING_DIRECTLY_TO_DESTINATION, this);
    }

    /**
     * 時刻表を検索する際に使う年月日を設定する。
     * @param year 年
     * @param month 月
     * @param date 日
     */
    public void setDateOfTimeTable(int year, int month, int date) {
        fYearOfTimeTable = year;
        fMonthOfTimeTable = month;
        fDateOfTimeTable = date;
    }

    /**
     * 時刻表を検索する際に使う年を返す．
     * @return 年
     */
    public int getYearOfTimeTable() {
        return fYearOfTimeTable;
    }

    /**
     * 時刻表を検索する際に使う月を返す．
     * @return 月
     */
    public int getMonthOfTimeTable() {
        return fMonthOfTimeTable;
    }

    /**
     * 時刻表を検索する際に使う日を返す．
     * @return 日
     */
    public int getDateOfTimeTable() {
        return fDateOfTimeTable;
    }

    /**
     * 移動をスケジュールする。
     * @param traverseModeSet 移動手段の集合(MODE_BICYCLE, MODE_WALK, MODE_CAR, 
     *                                      MODE_BUS, MODE_TRAM, MODE_SUBWAY, 
     *                                      MODE_RAIL, MODE_FERRY, MODE_CABLE_CAR, 
     *                                      MODE_GONDOLA, MODE_FUNICULAR, 
     *                                      MODE_AIRPLANE, MODE_TRANSIT, MODE_ALL)
     * @param arriveBy true:到着時刻で検索する、false:出発時刻で検索する
     * @param hour 時
     * @param minute 分
     * @param origin 出発スポット
     * @param destination 到着スポット
     * @return 経路検索結果
     * @throws IOException 
     */
    public TOtpResult findRoutes(int maxNoOfItineraries, TraverseModeSet traverseModeSet, boolean arriveBy,
                                 int hour, int minute, TSpot origin, TSpot destination) {
        TOtpRouter router = TThreadLocalOfOtpRouter.get();
        double lat1 = ((TRoleOfGisSpot)origin.getRole(ERoleName.GisSpot)).getLatitude();
        double lon1 = ((TRoleOfGisSpot)origin.getRole(ERoleName.GisSpot)).getLongitude();
        double lat2 = ((TRoleOfGisSpot)destination.getRole(ERoleName.GisSpot)).getLatitude();
        double lon2 = ((TRoleOfGisSpot)destination.getRole(ERoleName.GisSpot)).getLongitude();
        return router.doIt(maxNoOfItineraries, traverseModeSet, arriveBy, 
                           fYearOfTimeTable, fMonthOfTimeTable, fDateOfTimeTable, 
                           hour, minute, lat1, lon1, lat2, lon2);
    }

    /**
     * 移動をスケジュールする
     * @param startDay 開始日
     * @param startHour 開始時
     * @param startMinute 開始分
     * @param endDay 終了日
     * @param endHour 終了時
     * @param endMinute 終了分
     * @param origin 出発スポット
     * @param destination 到着スポット
     */
    private void scheduleToMove(int startDay, int startHour, int startMinute, 
                                int endDay, int endHour, int endMinute,
                               TSpot origin, TSpot destination) {
        if (startHour == endHour && startMinute == endMinute) {
            //出発時刻と到着時刻が同じならば直接目的地へ移動
            TRuleOfMovingDirectlyToDestination moving = (TRuleOfMovingDirectlyToDestination)getRule(RULE_NAME_OF_MOVING_DIRECTLY_TO_DESTINATION);
            moving.setOriginAndDestination(origin, destination);
            moving.setTimeAndStage(startDay, startHour, startMinute, 0, EStage.AgentMoving);
        } else {
            //出発時刻と到着時刻が異なれば途中スポットを経由して移動
            TRuleOfLeavingOrigin leaving = (TRuleOfLeavingOrigin)getRule(RULE_NAME_OF_LEAVING_ORIGIN);
            leaving.setOriginAndDestination(origin, destination);
            leaving.setTimeAndStage(startDay, startHour, startMinute, 0, EStage.AgentMoving);
            TRuleOfArrivingAtDestination arriving = (TRuleOfArrivingAtDestination)getRule(RULE_NAME_OF_ARRIVING_AT_DESTINATION);
            arriving.setTimeAndStage(endDay, endHour, endMinute, 0, EStage.AgentMoving);    
        }                
    }

    /**
     * 移動手段を返す．
     * @param result 検索結果
     * @param itineraryIndex 旅程番号
     * @return 移動手段
     */
    private String getModes(TOtpResult result, int itineraryIndex) {
        int noOfLegs = result.getNoOfLegs(itineraryIndex);
        String modes = "";
        for (int l = 0; l < noOfLegs; ++l) {
            String modeOfLeg = result.getTraverseModeOfLeg(itineraryIndex, l);
            if (l == 0) {
                modes = modeOfLeg;
            } else {
                modes = modes + "|" + modeOfLeg;
            }
        }
        return modes;
    }

    private String getTripIDs(TOtpResult result, int itineraryIndex) {
        int noOfLegs = result.getNoOfLegs(itineraryIndex);
        String ids = "";
        for (int l = 0; l < noOfLegs; ++l) {
            String id = result.getTripIdOfLeg(itineraryIndex, l);
            if (l == 0) {
                ids = id;
            } else {
                ids = ids + "|" + id;
            }
        }
        return ids;
    }

    private String getStopIds(TOtpResult result, int itineraryIndex) {
        int noOfLegs = result.getNoOfLegs(itineraryIndex);
        String ids = "";
        for (int l = 0; l < noOfLegs; ++l) {
            String id = result.getStopIdsOfLeg(itineraryIndex, l);
            if (l == 0) {
                ids = id;
            } else {
                ids = ids + "|" + id;
            }
        }
        return ids;
    }

    /**
     * 徒歩時間を返す．
     * @param result 検索結果
     * @param itineraryIndex 旅程番号
     * @return 徒歩時間
     */
    private int getTimeForWalking(TOtpResult result, int itineraryIndex) {
        int noOfLegs = result.getNoOfLegs(itineraryIndex);
        int timeForWalking = 0;
        for (int l = 0; l < noOfLegs; ++l) {
            String modeOfLeg = result.getTraverseModeOfLeg(itineraryIndex, l);
            if (modeOfLeg.equals("WALK")) {
                timeForWalking += result.getTravelTimeOfLeg(itineraryIndex, l);
            }
        }
        return timeForWalking;
    }

    /**
     * 徒歩距離を返す．
     * @param result 検索結果
     * @param itineraryIndex 旅程番号
     * @return 徒歩距離
     */
    private double getDistanceForWalking(TOtpResult result, int itineraryIndex) {
        int noOfLegs = result.getNoOfLegs(itineraryIndex);
        double distanceForWalking = 0.0;
        for (int l = 0; l < noOfLegs; ++l) {
            String modeOfLeg = result.getTraverseModeOfLeg(itineraryIndex, l);
            if (modeOfLeg.equals("WALK")) {
                distanceForWalking += result.getTravelDistanceOfLeg(itineraryIndex, l);
            }
        }
        return distanceForWalking;
    }

    /**
     * 移動経路を検索する．
     * @param hour 時
     * @param minute 分
     * @param arriveBy true:到着時刻で検索，false:出発時刻で検索
     * @param modeSet 移動手段
     * @param origin 出発地スポット
     * @param destination 目的地スポット
     * @return 移動情報
     */
    public TTripInformation findRoute(int hour, int minute, boolean arriveBy, 
                                      TraverseModeSet modeSet, TSpot origin, TSpot destination) {
        TAgent owner = (TAgent)getOwner();
        TRoleOfGisAgent roleOfGis = (TRoleOfGisAgent)owner.getRole(jp.soars.modules.gis_otp.role.ERoleName.GisAgent);
        TRoleOfSyntheticPopulationData roleOfSpd = (TRoleOfSyntheticPopulationData)owner.getRole(jp.soars.modules.synthetic_population.ERoleName.SyntheticPopulationData);
        String key = TTripInformationCache.makeKey(roleOfGis, hour, minute, arriveBy, modeSet, origin, destination);
        TTripInformation ti = TTripInformationCache.get(key);
        if (ti == null) {
            int maxNoOfItineraries = 1;
            TOtpResult result = roleOfGis.findRoutes(maxNoOfItineraries, modeSet, arriveBy, 
                                                     hour, minute, origin, destination);
            if (result.getStatus() != EOtpStatus.SUCCESS) {
                ti = TTripInformationCache.register(key, result.getStatus()); //経路が見つからなかったことをキャッシュ
                System.err.println(result.getStatus() + ": PersonID(" 
                                   + roleOfSpd.getSyntheticPopulationData().getPersonId() + "), " + modeSet);
                return ti;
            }
            try {
                int itineraryIndex = 0;
                int routeId = TRouteLogger.write(result, itineraryIndex);
                Calendar startTime = result.getStartTime(itineraryIndex);
                Calendar endTime = result.getEndTime(itineraryIndex);
                int startDay = startTime.get(Calendar.DAY_OF_MONTH) - roleOfGis.getDateOfTimeTable();
                int startHour = startTime.get(Calendar.HOUR_OF_DAY);
                int startMinute = startTime.get(Calendar.MINUTE);
                int endDay = endTime.get(Calendar.DAY_OF_MONTH) - roleOfGis.getDateOfTimeTable();
                int endHour = endTime.get(Calendar.HOUR_OF_DAY);
                int endMinute = endTime.get(Calendar.MINUTE);
                String modes = getModes(result, itineraryIndex);
                // System.out.println(modes + ", " + getTripIDs(result, itineraryIndex) + ", " + getStopIds(result, itineraryIndex));
                int timeForWalking = getTimeForWalking(result, itineraryIndex);
                double distanceForWalking = getDistanceForWalking(result, itineraryIndex);
                ti = TTripInformationCache.register(key, result.getStatus(),
                                                    startDay, startHour, startMinute,
                                                    endDay, endHour, endMinute,
                                                    origin, destination, routeId,
                                                    modes, timeForWalking, distanceForWalking);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ti;
    }

    /**
     * 移動をスケジュールして，移動をロギングする．
     * @param day 日
     * @param ri 移動情報
     */
    public void scheduleToMove(int day, TTripInformation ri) {
        TAgent owner = (TAgent)getOwner();
        TRoleOfGisAgent roleOfGis = (TRoleOfGisAgent)owner.getRole(jp.soars.modules.gis_otp.role.ERoleName.GisAgent);
        TRoleOfSyntheticPopulationData roleOfSpd = (TRoleOfSyntheticPopulationData)owner.getRole(jp.soars.modules.synthetic_population.ERoleName.SyntheticPopulationData);
        int personId = roleOfSpd.getSyntheticPopulationData().getPersonId();
        roleOfGis.scheduleToMove(day, ri.getStartHour(), ri.getStartMinute(), 
                                 day, ri.getEndHour(), ri.getEndMinute(), 
                                 ri.getOrigin(), ri.getDestination());
        TPersonTripLogger.write(personId, day, ri.getStartHour(), ri.getStartMinute(), 
                                day, ri.getEndHour(), ri.getEndMinute(), 
                                ri.getOrigin().getName(), ri.getDestination().getName(), ri.getRouteID(),
                                ri.getModes(), ri.getTimeForWalking(), ri.getDistanceForWalking());
    }

    /**
     * 移動をスケジュールして，移動をロギングする． 日付を超過した移動に対応
     * @param ri 移動情報
     */
    public void scheduleToMove(TTripInformation ri) {
        TAgent owner = (TAgent)getOwner();
        TRoleOfGisAgent roleOfGis = (TRoleOfGisAgent)owner.getRole(jp.soars.modules.gis_otp.role.ERoleName.GisAgent);
        TRoleOfSyntheticPopulationData roleOfSpd = (TRoleOfSyntheticPopulationData)owner.getRole(jp.soars.modules.synthetic_population.ERoleName.SyntheticPopulationData);
        int personId = roleOfSpd.getSyntheticPopulationData().getPersonId();
//        System.out.println("[ri.getStartDay()]"+ ri.getStartDay() + "[ri.getEndDay()]" + ri.getEndDay());
        roleOfGis.scheduleToMove(ri.getStartDay(), ri.getStartHour(), ri.getStartMinute(),
                ri.getEndDay(), ri.getEndHour(), ri.getEndMinute(),
                ri.getOrigin(), ri.getDestination());
        TPersonTripLogger.write(personId, ri.getStartDay(), ri.getStartHour(), ri.getStartMinute(),
                ri.getEndDay(), ri.getEndHour(), ri.getEndMinute(),
                ri.getOrigin().getName(), ri.getDestination().getName(), ri.getRouteID(),
                ri.getModes(), ri.getTimeForWalking(), ri.getDistanceForWalking());
    }

}
