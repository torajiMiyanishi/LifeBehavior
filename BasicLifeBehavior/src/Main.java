import jp.soars.core.TAgent;
import jp.soars.core.TAgentManager;
import jp.soars.core.TRuleExecutor;
import jp.soars.core.TSOARSBuilder;
import jp.soars.core.TSpot;
import jp.soars.core.TSpotManager;
import jp.soars.core.enums.ERuleDebugMode;
import jp.soars.utils.random.ICRandom;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

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

        String simulationStart = "0/00:00:00";
        String simulationEnd = "28/00:00:00";
        String tick = "0:15:00";
        List<Enum<?>> stages = List.of(Stage.DecideBehavior,Stage.AgentMoving);
        Set<Enum<?>> agentTypes = new HashSet<>();
        Collections.addAll(agentTypes, AgentType.values());
        Set<Enum<?>> spotTypes = new HashSet<>();
        Collections.addAll(spotTypes, SpotType.values());
        TSOARSBuilder builder = new TSOARSBuilder(simulationStart, simulationEnd, tick, stages, agentTypes, spotTypes);

        // *************************************************************************************************************
        // TSOARSBuilderの任意設定項目
        // *************************************************************************************************************

        // 行為更新ステージを毎時刻ルールが実行される定期実行ステージとして登録
        builder.setPeriodicallyExecutedStage(Stage.DecideBehavior, simulationStart, tick);

        // マスター乱数発生器のシード値設定
        long seed = 0L;
        builder.setRandomSeed(seed);

        // ルールログとランタイムログの出力設定
        String pathOfLogDir = "C:\\Users\\tora2\\IdeaProjects\\LifeBehavior\\logs";
        builder.setRuleLoggingEnabled(pathOfLogDir + File.separator + "rule_log.csv");
        builder.setRuntimeLoggingEnabled(pathOfLogDir + File.separator + "runtime_log.csv");

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
        String csvAgentInfo = "Z:\\lab\\lifebehavior\\agent_1000.csv";
        List<String> rows = new ArrayList<>();
        try (Stream<String> lines = Files.lines(Paths.get(csvAgentInfo))) {
            lines.skip(1).forEach(line -> {
                rows.add(line);
                    }
            );
        }

        List<TSpot> homes = spotManager.createSpots(SpotType.Home, rows.size());
        TSpot visitedLocation = spotManager.createSpot(SpotType.VisitedLocation);
        List<TAgent> humans = agentManager.createAgents(AgentType.Human, rows.size());
        for (int i=0; i<rows.size(); i++) {
            String row = rows.get(i);
            String[] values = row.split(",");
            int age = Integer.parseInt(values[0].trim());
            Behavior.Gender gender = Behavior.Gender.valueOf(values[1].trim());
            Behavior.BehaviorType currentBehavior = Behavior.ACTIVITY_TO_BEHAVIOR_TYPE.get(values[2].trim());


            TAgent human = humans.get(i); // i番目の人間エージェント
            TSpot home = homes.get(i); // i番目の人間エージェントの自宅
            human.initializeCurrentSpot(home); // 初期スポットを自宅に設定
            new RoleOfHuman(human, 0, Behavior.Gender.MALE); // 人間役割を作成
            human.activateRole(RoleName.Human); // 人間役割をアクティブ化
            new RoleOfResident(human, home, visitedLocation); // 住民役割を作成
            human.activateRole(RoleName.Resident); // 住民役割をアクティブ化
            new RoleOfBehavior(human, currentBehavior, Behavior.getTransitionProbabilityData(gender,age)); // 行為者役割を作成
            human.activateRole(RoleName.Behavior); // 行為者役割をアクティブ化
        }

        // *************************************************************************************************************
        // 独自に作成するログ用のPrintWriter
        //   - スポットログ:各時刻での各エージェントの現在位置ログ
        // *************************************************************************************************************

        // スポットログ用PrintWriter
        PrintWriter spotLogPW = new PrintWriter(new BufferedWriter(new FileWriter(pathOfLogDir + File.separator + "spot_log.csv")));
        // スポットログのカラム名出力
        spotLogPW.print("CurrentTime");
        for (TAgent human : humans) {
            spotLogPW.print(',');
            spotLogPW.print(human.getName());
        }
        spotLogPW.println();

        // 行為ログ用PrintWriter
        PrintWriter behaviorLogPW = new PrintWriter(new BufferedWriter(new FileWriter(pathOfLogDir + File.separator + "behavior_log.csv")));
        // 行為ログのカラム名出力
        behaviorLogPW.print("CurrentTime,CurrentDay");
        for (TAgent human : humans) {
            behaviorLogPW.print(',');
            behaviorLogPW.print(human.getName());
        }
        behaviorLogPW.println();
        // *************************************************************************************************************
        // シミュレーションのメインループ
        // *************************************************************************************************************

        // 1ステップ分のルールを実行 (ruleExecutor.executeStage()で1ステージ毎に実行することもできる)
        // 実行された場合:true，実行されなかった(終了時刻)場合は:falseが帰ってくるため，while文で回すことができる．
        while (ruleExecutor.executeStep()) {
            // 標準出力に現在時刻を表示する
            System.out.println(ruleExecutor.getCurrentTime());

            // 行為ログ出力
            behaviorLogPW.print(ruleExecutor.getCurrentTime());
            behaviorLogPW.print(',');
            behaviorLogPW.print(Day.getDay(ruleExecutor.getCurrentTime().getDay()));
            for (TAgent human : humans) {
                behaviorLogPW.print(',');
                RoleOfBehavior behaviorRole = (RoleOfBehavior) human.getRole(RoleName.Behavior);
                behaviorLogPW.print(behaviorRole.getCurrentBehavior());
            }
            behaviorLogPW.println();

            // スポットログ出力
            spotLogPW.print(ruleExecutor.getCurrentTime());
            spotLogPW.print(',');
            spotLogPW.print(Day.getDay(ruleExecutor.getCurrentTime().getDay()));
            for (TAgent human : humans) {
                spotLogPW.print(',');
                spotLogPW.print(human.getCurrentSpotName());
            }
            spotLogPW.println();
        }

        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown();
        spotLogPW.close();
        behaviorLogPW.close();
    }
}