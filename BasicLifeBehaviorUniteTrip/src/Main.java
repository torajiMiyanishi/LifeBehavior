import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import jp.soars.core.TAgent;
import jp.soars.core.TAgentManager;
import jp.soars.core.TRuleExecutor;
import jp.soars.core.TSOARSBuilder;
import jp.soars.core.TSpot;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;
import jp.soars.core.enums.ERuleDebugMode;

import jp.soars.modules.gis_otp.logger.TPersonTripLogger;
import jp.soars.modules.gis_otp.otp.TThreadLocalOfOtpRouter;
import jp.soars.modules.gis_otp.role.EStage;
import jp.soars.modules.gis_otp.role.TRoleOfGisAgent;
import jp.soars.modules.gis_otp.role.TRoleOfGisSpot;
import jp.soars.modules.gis_otp.role.TSpotOnTheWayMaker;

import jp.soars.modules.gis_otp.sample.TRoleOfPerson;
import jp.soars.modules.synthetic_population.TRoleOfSyntheticPopulationData;
import jp.soars.modules.synthetic_population.TSyntheticPopulationData;
import jp.soars.modules.synthetic_population.TSyntheticPopulationData.THousehold;

import jp.soars.utils.csv.TCCsvData;
import jp.soars.utils.random.ICRandom;

import org.opentripplanner.common.geometry.SphericalDistanceLibrary;

/**
 * メインクラス
 * @author miyanishi
 */
public class Main {

    public static void main(String[] args) throws Exception {
        // *************************************************************************************************************
        // TSOARSBuilderの必須設定項目
        //   - simulationStart:シミュレーション開始時刻
        //   - simulationEnd:シミュレーション終了時刻
        //   - tick:1ステップの時間間隔
        //   - stages:使用するステージリスト(実行順)
        //   - agentTypes:使用するエージェントタイプ集合
        //   - spotTypes:使用するスポットタイプ集合
        // *************************************************************************************************************
        long startTime = System.currentTimeMillis(); //実験開始時刻
        String simulationStart  = "0/00:00:00";
        String simulationEnd    = "1/00:00:00";
        String tick                = "0:01:00";
        String behaviorTick        = "0:05:00"; // 行為決定のティック
        long seed = 10400L; // マスターシード値
        List<Enum<?>> stages = List.of(Stage.DecideBehavior, EStage.AgentPlanning, EStage.AgentMoving, Stage.Deactivate);
        Set<Enum<?>> layers = new HashSet<>();
        Collections.addAll(layers, Layer.values());
        Layer defaultLayer = Layer.Geospatial;
//        TSOARSBuilder builder = new TSOARSBuilder(simulationStart, simulationEnd, tick, stages, agentTypes, spotTypes);
        TSOARSBuilder builder = createBuilder(simulationStart, simulationEnd, tick, stages, seed, layers, defaultLayer); //SOARSビルダ
        // *************************************************************************************************************
        // TSOARSBuilderの任意設定項目
        // *************************************************************************************************************

        // 行為更新ステージを毎時刻ルールが実行される定期実行ステージとして登録
        builder.setPeriodicallyExecutedStage(Stage.DecideBehavior, simulationStart, behaviorTick);


//        // マスター乱数発生器のシード値設定
//        long seed = 0L;
//        builder.setRandomSeed(seed);

        // gisデータ類のパス
//        String dirToInput = "C:\\Users\\tora2\\IdeaProjects\\LifeBehavior\\";
        String dirToInput = "Z:\\lab\\";
        String pathOfPopulationDataFile = dirToInput + "input/2015_003_8_47207_ok_10.csv"; //合成人口データファイル
        String pathToPoi = "Z:\\lab\\zenrin_poi\\modified\\47207.csv"; //日中の活動場所の建物座標データファイル
        String pathToPbf = dirToInput + "input/Ishigakishi.osm.pbf"; //OpenStreetMap用のPBFファイル
        String dirToGtfs = dirToInput + "input"; //石垣市のGTFSファイルが格納されているディレクトリ

        // ルールログとランタイムログの出力設定
        String pathOfLogDir = "C:\\Users\\tora2\\IdeaProjects\\LifeBehavior\\logs";
//        String pathOfLogDir = "Z:\\lab\\output\\logs";
        builder.setRuleLoggingEnabled(pathOfLogDir + File.separator + "rule_log.csv");
        builder.setRuntimeLoggingEnabled(pathOfLogDir + File.separator + "runtime_log.csv");
        String personTripLog = "person_trips"; //移動ログ
        String spotLog = "spot_log.csv"; //スポットログ

        // ルールログのデバッグ情報出力設定
        builder.setRuleDebugMode(ERuleDebugMode.LOCAL);

        // *************************************************************************************************************
        // TSOARSBuilderでシミュレーションに必要なインスタンスの作成と取得
        // *************************************************************************************************************

        builder.build();
        TRuleExecutor ruleExecutor = builder.getRuleExecutor();
        TAgentManager agentManager = builder.getAgentManager();
        TSpotManager spotManager = builder.getSpotManager();
        ICRandom random = builder.getRandom();
        Map<String, Object> globalSharedVariableSet = builder.getGlobalSharedVariableSet();


        // *************************************************************************************************************
        // 行為間遷移確率を導入
        // *************************************************************************************************************

        Behavior.initialize();

        // *************************************************************************************************************
        // エージェントとスポットを生成
        // *************************************************************************************************************

        //合成人口データの読み込み
        List<TSyntheticPopulationData> spData = TSyntheticPopulationData.raedDataFromCsvFile(pathOfPopulationDataFile);
        Map<Integer,THousehold> householdDB = TSyntheticPopulationData.getHouseholdDatabase(); //世帯データベース
        // 自宅スポットの作成
        Iterator<THousehold> householdItr = householdDB.values().iterator();
        int noOfHomes = householdDB.size(); // 世帯数
        List<TSpot> homes = spotManager.createSpots(SpotType.Home, noOfHomes, Layer.Geospatial); // 自宅スポットを生成．(Home1, Home2, ...)
        for (int i = 0; i < noOfHomes; ++i) {
            TSpot h = homes.get(i);
            THousehold household = householdItr.next();
            household.setSpot(h); //世帯にスポットを対応付ける
            double lat = household.getLatitude(); //緯度
            double lon = household.getLongitude(); //経度
            new TRoleOfGisSpot(h, lat, lon); //GISスポットロールを生成して自宅スポットに登録
        }
        // 日中の活動場所スポットの作成
        List<String[]> records = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(pathToPoi))) {
            records = reader.readAll();
            records = records.subList(1,records.size()); // skip header
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
        int noOfPois = records.size();
        List<TSpot> pois = spotManager.createSpots(SpotType.Poi, noOfPois, Layer.Geospatial); //活動場所スポットの生成
        for (int i = 0; i < noOfPois; ++i) {
            TSpot poi = pois.get(i);
            String[] record = records.get(i);
            double lat = Double.parseDouble(record[4]); //緯度
            double lon = Double.parseDouble(record[3]); //経度
            new TRoleOfGisSpot(poi, lat, lon); //GISスポットロールを生成して活動場所スポットに登録
            new RoleOfPoi(poi, record); //poiの属性を保持するロール
            poi.activateRole(RoleName.Poi); // アクティベート
        }
        // 途中スポットの作成
        TSpotOnTheWayMaker.create(spotManager, homes, SpotType.SpotOnTheWay); //自宅スポットの途中スポット
        TSpotOnTheWayMaker.create(spotManager, pois, SpotType.SpotOnTheWay); //活動場所スポットの途中スポット
        // testのためのスポット
        TSpot testSpot = spotManager.createSpots(SpotType.Test,1,Layer.Test).get(0);

        //エージェントの生成
        int noOfPersons = spData.size(); // 合成人口データで定義された人数
        List<TAgent> persons = agentManager.createAgents(AgentType.Person, noOfPersons); // Personエージェントを生成
        for (int i = 0; i < persons.size(); ++i) {
            TAgent person = persons.get(i); // i番目のエージェントを取り出す．
            new TRoleOfGisAgent(person); //GISエージェント役割を生成してエージェントに割り当てる
            TSyntheticPopulationData spd = spData.get(i); //i番目のエージェントの人工合成データ
            new TRoleOfSyntheticPopulationData(person, spd); //合成人口役割を生成してエージェントに割り当てる
            int householdId = spd.getHouseholdId(); //世帯ID
            TSpot home = householdDB.get(householdId).getSpot(); //自宅スポット
            new RoleOfResident(person,home); // 住民役割
            person.activateRole(RoleName.Resident);
            person.initializeCurrentSpot(home); //初期位置を自宅スポットに設定する．
            person.initializeCurrentSpot(testSpot); // テスト用にtestレイヤのスポットを設定．
//            TSpot workPlace = chooseWorkPlace(home, pois, 300.0, random); //活動場所を選ぶ
            new RoleOfTripper(person,ruleExecutor.getCurrentTime()); // 動的な移動計画
            person.activateRole(jp.soars.modules.gis_otp.role.ERoleName.GisAgent); //GISエージェント役割をアクティブ化


            /** gis-otp-moduleとマルコフ連鎖生活行動モデルの結合 */
            new RoleOfBehavior(person, spd.getAge(), spd.getGender()); // 行為者役割を作成
            person.activateRole(RoleName.Behavior); // 行為者役割をアクティブ化
        }






//        String csvAgentInfo = "Z:\\lab\\lifebehavior\\agent_1000.csv";
//        List<String> rows = new ArrayList<>();
//        try (Stream<String> lines = Files.lines(Paths.get(csvAgentInfo))) {
//            lines.skip(1).forEach(line -> {
//                rows.add(line);
//                    }
//            );
//        }
//
//        List<TSpot> homes = spotManager.createSpots(SpotType.Home, rows.size());
//        TSpot visitedLocation = spotManager.createSpot(SpotType.VisitedLocation);
//        List<TAgent> humans = agentManager.createAgents(AgentType.Human, rows.size());
//        for (int i=0; i<rows.size(); i++) {
//            String row = rows.get(i);
//            String[] values = row.split(",");
//            int age = Integer.parseInt(values[0].trim());
//            Behavior.Gender gender = Behavior.Gender.valueOf(values[1].trim());
//            Behavior.BehaviorType currentBehavior = Behavior.ACTIVITY_TO_BEHAVIOR_TYPE.get(values[2].trim());
//
//
//            TAgent human = humans.get(i); // i番目の人間エージェント
//            TSpot home = homes.get(i); // i番目の人間エージェントの自宅
//            human.initializeCurrentSpot(home); // 初期スポットを自宅に設定
//            new RoleOfHuman(human, age, gender); // 人間役割を作成
//            human.activateRole(RoleName.Human); // 人間役割をアクティブ化
//            new RoleOfResident(human, home, visitedLocation); // 住民役割を作成
//            human.activateRole(RoleName.Resident); // 住民役割をアクティブ化
//            new RoleOfBehavior(human, currentBehavior); // 行為者役割を作成
//            human.activateRole(RoleName.Behavior); // 行為者役割をアクティブ化
//        }

        // *************************************************************************************************************
        // 独自に作成するログ用のPrintWriter
        //   - スポットログ:各時刻での各エージェントの現在位置ログ
        // *************************************************************************************************************

//        // スポットログ用PrintWriter
//        PrintWriter spotLogPW = new PrintWriter(new BufferedWriter(new FileWriter(pathOfLogDir + File.separator + "spot_log.csv")));
//        // スポットログのカラム名出力
//        spotLogPW.print("CurrentTime");
//        for (TAgent person : persons) {
//            spotLogPW.print(',');
//            spotLogPW.print(person.getName());
//        }
//        spotLogPW.println();

        // 行為ログ用PrintWriter
        PrintWriter behaviorLogPW = new PrintWriter(new BufferedWriter(new FileWriter(pathOfLogDir + File.separator + "behavior_log.csv")));
        // 行為ログのカラム名出力
        behaviorLogPW.print("CurrentTime,CurrentDay");
        for (TAgent person : persons) {
            behaviorLogPW.print(',');
            behaviorLogPW.print(person.getName());
        }
        behaviorLogPW.println();

        PrintWriter spotLogPW = openSpotLog(pathOfLogDir + File.separator + spotLog, persons); //スポットログをオープン
        TPersonTripLogger.open(pathOfLogDir, personTripLog); //移動ログをオープン
        TThreadLocalOfOtpRouter.initialize(pathToPbf, dirToGtfs); //OTPルータの初期化
        // *************************************************************************************************************
        // シミュレーションのメインループ
        // *************************************************************************************************************

        // 1ステップ分のルールを実行 (ruleExecutor.executeStage()で1ステージ毎に実行することもできる)
        // 実行された場合:true，実行されなかった(終了時刻)場合は:falseが帰ってくるため，while文で回すことができる．
        while (ruleExecutor.executeStep()) {
            // 標準出力に現在時刻を表示する
            List<String> strings = new ArrayList<>();
//            for (TAgent person:persons){
//                RoleOfBehavior behaviorRole = (RoleOfBehavior) person.getRole(RoleName.Behavior);
//
//                strings.add(person.getCurrentSpot().getName()+":"+ behaviorRole.getCurrentBehavior()+":"+person.getRole(RoleName.Tripper).isActive());
//            }
//            System.out.println(ruleExecutor.getCurrentTime() + String.join("   ",strings)); // +" "+ persons.get(0).toString());
            System.out.println(ruleExecutor.getCurrentTime());
            writeSpotLog(spotLogPW, ruleExecutor.getCurrentTime(), persons); //スポットログの出力

            // 行為ログ出力
            behaviorLogPW.print(ruleExecutor.getCurrentTime());
            behaviorLogPW.print(',');
            behaviorLogPW.print(Day.getDay(ruleExecutor.getCurrentTime().getDay()));
            for (TAgent person : persons) {
                behaviorLogPW.print(',');
                RoleOfBehavior behaviorRole = (RoleOfBehavior) person.getRole(RoleName.Behavior);
                behaviorLogPW.print(behaviorRole.getCurrentBehavior());
            }
            behaviorLogPW.println();

//            // スポットログ出力
//            spotLogPW.print(ruleExecutor.getCurrentTime());
//            spotLogPW.print(',');
//            spotLogPW.print(Day.getDay(ruleExecutor.getCurrentTime().getDay()));
//            for (TAgent person : persons) {
//                spotLogPW.print(',');
//                spotLogPW.print(person.getCurrentSpotName());
//            }
//            spotLogPW.println();
        }

        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown();

        behaviorLogPW.close();

        TPersonTripLogger.close(); //移動ログをクローズ
        spotLogPW.close(); // スポットログをクローズ
        System.out.println("Elapsed time: " + (System.currentTimeMillis() - startTime) / 1000 + "[sec]"); //実行時間
    }


    /**
     * SOARSビルダを生成する．
     * @param startTime シミュレーション開始時刻
     * @param endTime シミュレーション終了時刻
     * @param tick １ステップの時間間隔
     * @param stages ステージリスト
     * @param seed マスター乱数シード
     * @return SOARSビルダ
     * @throws IOException
     */
    private static TSOARSBuilder createBuilder(String startTime, String endTime,
                                               String tick, List<Enum<?>> stages, long seed, Set<Enum<?>> layers, Enum<Layer> defaultLayer) throws IOException {
        Set<Enum<?>> agentTypes = new HashSet<>(); // 全エージェントタイプ
        Set<Enum<?>> spotTypes = new HashSet<>(); // 全スポットタイプ
        Collections.addAll(agentTypes, AgentType.values()); // EAgentType に登録されているエージェントタイプをすべて追加
        Collections.addAll(spotTypes, SpotType.values()); // ESpotType に登録されているスポットタイプをすべて追加
        TSOARSBuilder builder = new TSOARSBuilder(startTime, endTime, tick, stages, agentTypes, spotTypes,layers,defaultLayer); // ビルダー作成
        builder.setRandomSeed(seed); // シード値設定
        // builder.setRuleLoggingEnabled(pathOfLogDir + File.separator + "rule_log.csv") // ルールログ出力設定
        //        .setRuntimeLoggingEnabled(pathOfLogDir + File.separator + "runtime_log.csv"); // ランタイムログ出力設定
        builder.setRuleDebugMode(ERuleDebugMode.LOCAL); // ローカル設定に従う
        builder.build(); // インスタンスのビルド
        return builder;
    }

    /**
     * 家からdistance以上の距離にある活動場所をランダムに選択して返す．
     * @param home 家
     * @param workPlaces 活動場所のリスト
     * @param distance 距離
     * @param random 乱数発生器
     * @return 活動場所
     */
    private static TSpot chooseWorkPlace(TSpot home, List<TSpot> workPlaces, double distance, ICRandom random) {
        TRoleOfGisSpot rh = (TRoleOfGisSpot)home.getRole(jp.soars.modules.gis_otp.role.ERoleName.GisSpot);
        double homeLat = rh.getLatitude(); //自宅の緯度
        double homeLon = rh.getLongitude(); //自宅の経度
        TSpot workPlace = workPlaces.get(random.nextInt(workPlaces.size())); //活動場所をランダムに選択
        TRoleOfGisSpot rwp = (TRoleOfGisSpot)workPlace.getRole(jp.soars.modules.gis_otp.role.ERoleName.GisSpot);
        double lat = rwp.getLatitude(); //活動場所の緯度
        double lon = rwp.getLongitude(); //活動場所の経度
        while (SphericalDistanceLibrary.distance(homeLat, homeLon, lat, lon) < distance) { //自宅と活動場所の距離が近かったら
            workPlace = workPlaces.get(random.nextInt(workPlaces.size())); //活動場所を選びなおす
            rwp = (TRoleOfGisSpot)workPlace.getRole(jp.soars.modules.gis_otp.role.ERoleName.GisSpot);
            lat = rwp.getLatitude(); //活動場所の緯度
            lon = rwp.getLongitude(); //活動場所の経度
        }
        return workPlace;
    }

    /**
     * スポットログをオープンする
     * @param path スポットログのパス
     * @param persons エージェントのリスト
     * @return 出力ストリーム
     * @throws FileNotFoundException
     */
    private static PrintWriter openSpotLog(String path, List<TAgent> persons) throws FileNotFoundException {
        PrintWriter spotLogPW = new PrintWriter(path); //スポットログ
        // スポットログのヘッダのカラム名出力
        spotLogPW.print("CurrentTime");
        for (TAgent agent : persons) {
            spotLogPW.print("," + agent.getName());
        }
        spotLogPW.println();
        return spotLogPW;
    }

    /**
     * スポットログを出力する．
     * @param spotLogPW 出力ストリーム
     * @param currentTime 現在時刻
     * @param persons エージェントのリスト
     */
    private static void writeSpotLog(PrintWriter spotLogPW, TTime currentTime, List<TAgent> persons) {
        spotLogPW.print(currentTime); // スポットログ出力
        for (TAgent a : persons) {
            spotLogPW.print("," + a.getCurrentSpotName());
        }
        spotLogPW.println();
    }
}