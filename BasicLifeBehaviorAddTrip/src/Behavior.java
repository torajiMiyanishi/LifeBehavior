import org.joda.time.Days;
import org.json.JSONObject;
import org.json.JSONArray;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Behavior {

    public static final String DIR_DATA = "Z:\\lab\\lifebehavior\\"; // 遷移確率のディレクトリ
    public static final String PATH_TO_INITIAL_BEHAVIOR_PROB = "Y:\\生活行動モデル\\data\\initialBehaviorProbabilities.json"; // 初期行為決定のための0:00における時間帯別行為者率

    private static final Map<AttributeType, TransitionProbabilityData> AlltransitionDataMap = new HashMap<>(); // 全ての属性のすべての時刻の遷移確率を持つ変数
    private static final List<BehaviorProbByTime> InitialBehaviorProbs = new ArrayList<>(); // 全ての属性の0:00における行為者率を持つ変数

    public static final List<String> ACTIVITY_ORDERING = List.of(
            "睡眠", "食事", "身のまわりの用事", "療養・静養", "仕事", "仕事のつきあい", "授業・学内の活動",
            "学校外の学習", "炊事・掃除・洗濯", "買い物", "子どもの世話", "家庭雑事", "通勤", "通学", "社会参加",
            "会話・交際", "スポーツ", "行楽・散策", "趣味・娯楽・教養", "マスメディア接触", "休息");
    public static final List<BehaviorType> BEHAVIOR_TYPE_ORDERING = new ArrayList<>();
    public static final Map<BehaviorType, String> BEHAVIOR_TYPE_TO_ACTIVITY = new HashMap<>();
    public static final Map<String, BehaviorType> ACTIVITY_TO_BEHAVIOR_TYPE = new HashMap<>();
    // BehaviorType毎に「外出」か「在宅」をラベルとしてマッピングする辞書
    public static final Map<BehaviorType, LocationDependency> BEHAVIOR_LOCATION_LABEL = new HashMap<>();

    // 行為が場所に依存しているか？ enum の定義
    public enum LocationDependency {
        LOCATION_DEPENDENT,  // 特定の場所に依存する
        LOCATION_INDEPENDENT, // 特定の場所に依存しない
        BOTH, // 両方の行為が含まれている（例：食事）
    }
    // 属性情報の定義
    public enum Gender {
        MALE, FEMALE // 男性，女性
    }
    public enum AttributeType {
        MALE_10, MALE_20, MALE_30, MALE_40, MALE_50, MALE_60, MALE_70, FEMALE_10, FEMALE_20, FEMALE_30, FEMALE_40, FEMALE_50, FEMALE_60, FEMALE_70
    }



    public enum BehaviorType {
        PERSONAL_CARE,       // 身のまわりの用事
        SLEEP,               // 睡眠
        WORK,                // 仕事
        COMMUTING,           // 通勤
        SOCIAL_PARTICIPATION,// 社会参加
        SPORTS,              // スポーツ
        STUDY_OUTSIDE,       // 学校外の学習
        EATING,              // 食事
        HOBBIES,             // 趣味・娯楽・教養
        WORK_SOCIALIZING,    // 仕事のつきあい
        HOUSEWORK,           // 炊事・掃除・洗濯
        MEDICAL_CARE,        // 療養・静養
        CHILDCARE,           // 子どもの世話
        RECREATION,          // 行楽・散策
        SCHOOL_ACTIVITIES,   // 授業・学内の活動
        SCHOOL_COMMUTING,    // 通学
        REST,                // 休息
        HOUSEHOLD_TASKS,     // 家庭雑事
        SHOPPING,            // 買い物
        MEDIA_CONSUMPTION,   // マスメディア接触
        SOCIALIZING          // 会話・交際
    }

    // 静的ブロックでマップを初期化する
    static {
        BEHAVIOR_TYPE_TO_ACTIVITY.put(BehaviorType.SLEEP, "睡眠");
        BEHAVIOR_TYPE_TO_ACTIVITY.put(BehaviorType.EATING, "食事");
        BEHAVIOR_TYPE_TO_ACTIVITY.put(BehaviorType.PERSONAL_CARE, "身のまわりの用事");
        BEHAVIOR_TYPE_TO_ACTIVITY.put(BehaviorType.MEDICAL_CARE, "療養・静養");
        BEHAVIOR_TYPE_TO_ACTIVITY.put(BehaviorType.WORK, "仕事");
        BEHAVIOR_TYPE_TO_ACTIVITY.put(BehaviorType.WORK_SOCIALIZING, "仕事のつきあい");
        BEHAVIOR_TYPE_TO_ACTIVITY.put(BehaviorType.SCHOOL_ACTIVITIES, "授業・学内の活動");
        BEHAVIOR_TYPE_TO_ACTIVITY.put(BehaviorType.STUDY_OUTSIDE, "学校外の学習");
        BEHAVIOR_TYPE_TO_ACTIVITY.put(BehaviorType.HOUSEWORK, "炊事・掃除・洗濯");
        BEHAVIOR_TYPE_TO_ACTIVITY.put(BehaviorType.SHOPPING, "買い物");
        BEHAVIOR_TYPE_TO_ACTIVITY.put(BehaviorType.CHILDCARE, "子どもの世話");
        BEHAVIOR_TYPE_TO_ACTIVITY.put(BehaviorType.HOUSEHOLD_TASKS, "家庭雑事");
        BEHAVIOR_TYPE_TO_ACTIVITY.put(BehaviorType.COMMUTING, "通勤");
        BEHAVIOR_TYPE_TO_ACTIVITY.put(BehaviorType.SCHOOL_COMMUTING, "通学");
        BEHAVIOR_TYPE_TO_ACTIVITY.put(BehaviorType.SOCIAL_PARTICIPATION, "社会参加");
        BEHAVIOR_TYPE_TO_ACTIVITY.put(BehaviorType.SOCIALIZING, "会話・交際");
        BEHAVIOR_TYPE_TO_ACTIVITY.put(BehaviorType.SPORTS, "スポーツ");
        BEHAVIOR_TYPE_TO_ACTIVITY.put(BehaviorType.RECREATION, "行楽・散策");
        BEHAVIOR_TYPE_TO_ACTIVITY.put(BehaviorType.HOBBIES, "趣味・娯楽・教養");
        BEHAVIOR_TYPE_TO_ACTIVITY.put(BehaviorType.MEDIA_CONSUMPTION, "マスメディア接触");
        BEHAVIOR_TYPE_TO_ACTIVITY.put(BehaviorType.REST, "休息");

        // 逆のマッピングも作成
        for (Map.Entry<BehaviorType, String> entry : BEHAVIOR_TYPE_TO_ACTIVITY.entrySet()) {
            ACTIVITY_TO_BEHAVIOR_TYPE.put(entry.getValue(), entry.getKey());
        }

        // behavior typeの順序も保持
        for (String activity: ACTIVITY_ORDERING){
            BEHAVIOR_TYPE_ORDERING.add(ACTIVITY_TO_BEHAVIOR_TYPE.get(activity));
        }

        // BEHAVIOR_TYPE_ORDERING に基づいて「外出」か「在宅」を enum の LocationType に定義する
        // BEHAVIOR_TYPE_ORDERING に基づいて「特定の場所に依存するか」を enum の LocationDependency に定義する
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.SLEEP, LocationDependency.LOCATION_DEPENDENT);        // 睡眠は自宅に依存
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.EATING, LocationDependency.BOTH);                     // 食事は場所によらない
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.PERSONAL_CARE, LocationDependency.LOCATION_DEPENDENT); // 身のまわりの用事は自宅に依存
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.MEDICAL_CARE, LocationDependency.LOCATION_DEPENDENT);  // 療養・静養は特定場所に依存
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.WORK, LocationDependency.LOCATION_DEPENDENT);          // 仕事は職場に依存
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.WORK_SOCIALIZING, LocationDependency.LOCATION_DEPENDENT); // 仕事のつきあいは職場に依存
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.SCHOOL_ACTIVITIES, LocationDependency.LOCATION_DEPENDENT); // 学内の活動は学校に依存
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.STUDY_OUTSIDE, LocationDependency.LOCATION_DEPENDENT);    // 学校外の学習は場所に依存
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.HOUSEWORK, LocationDependency.LOCATION_DEPENDENT);       // 家事は自宅に依存
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.SHOPPING, LocationDependency.LOCATION_DEPENDENT);        // 買い物は場所に依存（店舗）
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.CHILDCARE, LocationDependency.LOCATION_DEPENDENT);       // 子どもの世話は場所に依存
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.HOUSEHOLD_TASKS, LocationDependency.LOCATION_DEPENDENT); // 家庭雑事は自宅に依存
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.COMMUTING, LocationDependency.LOCATION_DEPENDENT);       // 通勤は職場に依存
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.SCHOOL_COMMUTING, LocationDependency.LOCATION_DEPENDENT); // 通学は学校に依存
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.SOCIAL_PARTICIPATION, LocationDependency.LOCATION_DEPENDENT); // 社会参加は場所に依存
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.SOCIALIZING, LocationDependency.BOTH);                  // 会話・交際は場所によらない
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.SPORTS, LocationDependency.LOCATION_DEPENDENT);         // スポーツは場所に依存
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.RECREATION, LocationDependency.LOCATION_DEPENDENT);     // 行楽・散策は場所に依存
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.HOBBIES, LocationDependency.BOTH);                      // 趣味・娯楽は場所によらない
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.MEDIA_CONSUMPTION, LocationDependency.LOCATION_INDEPENDENT); // マスメディア接触は場所によらない
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.REST, LocationDependency.LOCATION_INDEPENDENT);         // 休息は場所によらない

    }

    public static AttributeType generateAttributeType(int age, String gender){
        String ageStr;
        // 年代の判定
        if (age >= 0 && age < 20) {
            ageStr = "10";
        } else if (age >= 20 && age < 30) {
            ageStr = "20";
        } else if (age >= 30 && age < 40) {
            ageStr = "30";
        } else if (age >= 40 && age < 50) {
            ageStr = "40";
        } else if (age >= 50 && age < 60) {
            ageStr = "50";
        } else if (age >= 60 && age < 70) {
            ageStr = "60";
        } else {
            ageStr = "70";
        }
        return AttributeType.valueOf(gender+"_"+ageStr);
    }



    // ファイルパスをジェンダーと年齢に基づいて生成
    public static String getFilePath(AttributeType attributeType, Day.DayType dayType) {
        String[] parts      = attributeType.toString().split("_");
        Gender gender       = Gender.valueOf(parts[0]);
        String genderStr    = (gender == Gender.MALE) ? "男" : "女";
        String dayOfWeek;
        String ageStr;

        // 年代の判定
        if (parts[1].equals("10")) {
            ageStr = "１０代";
        } else if (parts[1].equals("20")) {
            ageStr = "２０代";
        } else if (parts[1].equals("30")) {
            ageStr = "３０代";
        } else if (parts[1].equals("40")) {
            ageStr = "４０代";
        } else if (parts[1].equals("50")) {
            ageStr = "５０代";
        } else if (parts[1].equals("60")) {
            ageStr = "６０代";
        } else {
            ageStr = "７０歳以上";
        }

        // 曜日の判定
        if (dayType.equals(Day.DayType.SATURDAY)) {
            dayOfWeek = "土曜日";
        } else if (dayType.equals(Day.DayType.SUNDAY)) {
            dayOfWeek = "日曜日";
        } else {
            dayOfWeek = "平日";
        }

        // パスの構築
        return DIR_DATA + "遷移確率_" + genderStr + ageStr + "_" + dayOfWeek + ".json";
    }

    // TransitionProbabilityDataを保持するクラス
    public static class TransitionProbabilityData {
        private double[][][] transitionMatrixWeekday;
        private double[][][] transitionMatrixSaturday;
        private double[][][] transitionMatrixSunday;
        private int numberOfActivities;
        private int numberOfTimeTicks;

        public TransitionProbabilityData(double[][][] transitionMatrixWeekday, double[][][] transitionMatrixSaturday, double[][][] transitionMatrixSunday) {
            this.transitionMatrixWeekday = transitionMatrixWeekday;
            this.transitionMatrixSaturday = transitionMatrixSaturday;
            this.transitionMatrixSunday = transitionMatrixSunday;
            this.numberOfActivities = transitionMatrixWeekday[0].length;  // 行為数 (すべての行列が同じ次元を持つことが前提)
            this.numberOfTimeTicks = transitionMatrixWeekday.length;      // タイムティック数
        }

        public double[] getTransitionProbabilities(int currentActivity, int timeTick, Day.DayType day) {
            double[][][] selectedMatrix = getTransitionMatrix(day);

            if (currentActivity >= 0 && currentActivity < numberOfActivities && timeTick >= 0 && timeTick < numberOfTimeTicks) {
                return selectedMatrix[timeTick][currentActivity];
            } else {
                throw new IllegalArgumentException("Invalid activity or time tick");
            }
        }

        public double[][][] getTransitionMatrix(Day.DayType day) {
            switch (day) {
                case SATURDAY:
                    return transitionMatrixSaturday;
                case SUNDAY:
                    return transitionMatrixSunday;
                default:
                    return transitionMatrixWeekday;
            }
        }
    }


    // JSONファイルを読み込んで、TransitionProbabilityDataインスタンスを生成する
    public static double[][][] loadFromJson(String filePath) throws Exception {
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        JSONArray jsonArray = new JSONArray(content);

        int size1 = jsonArray.length();
        int size2 = jsonArray.getJSONArray(0).length();
        int size3 = jsonArray.getJSONArray(0).getJSONArray(0).length();
        double[][][] transitionMatrix = new double[size1][size2][size3];

        for (int i = 0; i < size1; i++) {
            JSONArray array2D = jsonArray.getJSONArray(i);
            for (int j = 0; j < size2; j++) {
                JSONArray array1D = array2D.getJSONArray(j);
                for (int k = 0; k < size3; k++) {
                    transitionMatrix[i][j][k] = array1D.getDouble(k);
                }
            }
        }

        return transitionMatrix;
    }

    // 時間帯別の行為者率を保持するデータ型の定義
    public static class BehaviorProbByTime {
        private AttributeType attributeType;
        private Day.DayType dayType;
        private int hour;
        private int minute;
        private HashMap<BehaviorType, Double> probs;

        public BehaviorProbByTime(AttributeType attributeType, Day.DayType dayType, int hour, int minute, HashMap<BehaviorType, Double> probs) {
            this.attributeType = attributeType;
            this.dayType = dayType;
            this.hour = hour;
            this.minute = minute;
            this.probs = probs;
        }
        // Getter
        public AttributeType getAttribute(){
            return attributeType;
        }
        public Day.DayType getDayType(){
            return dayType;
        }
        public HashMap<BehaviorType, Double> getProbs(){
            return probs;
        }
        public BehaviorType getBehaviorByRate() {
            // まず、全ての確率を加算して総和を計算
            double sum = 0.0;
            for (double probability : probs.values()) {
                sum += probability;
            }

            // 0 から sum の範囲で乱数を生成
            Random rand = new Random();
            double randomValue = rand.nextDouble() * sum;

            // 累積確率を計算して選択する
            double cumulativeProbability = 0.0;
            BehaviorType selectedBehavior = null;

            // 確率と対応する行為をループで確認
            for (Map.Entry<BehaviorType, Double> entry : probs.entrySet()) {
                cumulativeProbability += entry.getValue(); // 累積確率に加算
                if (randomValue <= cumulativeProbability) {
                    selectedBehavior = entry.getKey(); // 条件を満たした行為を選択
                    break;
                }
            }

            // 選択された行為を返す
            return selectedBehavior;
        }

    }
    // 属性と曜日からBehaviorProbByTimeを取得する
    public static BehaviorProbByTime getBehaviorProbByTime(AttributeType attributeType, Day.DayType dayType){
        for (BehaviorProbByTime probData: InitialBehaviorProbs){
            if (probData.getAttribute() == attributeType && probData.getDayType() == dayType){
                return probData;
            }
        }
        System.out.println("データが存在しないエラー @Behavior.java");
        System.out.println("AttributeType "+ attributeType + ", Day.DayType " + dayType);
        return null;
    }

    // イニシャライザ
    public static void initialize() throws Exception {
        /** すべての属性ごとにTransitionProbabilityDataを読み込んでキャッシュする */
        // サンプルの属性ごとに全ファイルをロードする（年齢、性別、曜日を組み合わせたすべてのファイル）
        for (AttributeType attributeType : AttributeType.values()) {
            // 各曜日ごとにファイルパスを生成し、対応する行列をロードする
            String filePathWeekday = getFilePath(attributeType, Day.DayType.MONDAY);
            double[][][] matrixWeekday = loadFromJson(filePathWeekday);

            String filePathSaturday = getFilePath(attributeType, Day.DayType.SATURDAY);
            double[][][] matrixSaturday = loadFromJson(filePathSaturday);

            String filePathSunday = getFilePath(attributeType, Day.DayType.SUNDAY);
            double[][][] matrixSunday = loadFromJson(filePathSunday);

            // 3つの行列（平日、土曜日、日曜日）を持つTransitionProbabilityDataをキャッシュに格納
            AlltransitionDataMap.put(attributeType, new TransitionProbabilityData(matrixWeekday, matrixSaturday, matrixSunday));
        }
        /** 全ての属性，全ての曜日分の0:00の行為者率をjsonから読み込んでキャッシュする */
        try {
            String content = new String(Files.readAllBytes(Paths.get(PATH_TO_INITIAL_BEHAVIOR_PROB))); // ファイルを読み込んで文字列に変換
            JSONObject jsonObject = new JSONObject(content); // JSON文字列をパース
            // JSONオブジェクトを走査して Map に格納
            for (String groupAndDay : jsonObject.keySet()) {
                JSONObject innerObject = jsonObject.getJSONObject(groupAndDay);
                HashMap<BehaviorType, Double> activityProb = new HashMap<>();
                for (String activity : innerObject.keySet()) {
                    activityProb.put(ACTIVITY_TO_BEHAVIOR_TYPE.get(activity), innerObject.getDouble(activity));
                }
                AttributeType attributeType = AttributeType.valueOf(groupAndDay.split("-")[0]);
                Day.DayType dayType = Day.DayType.valueOf(groupAndDay.split("-")[1]);
                InitialBehaviorProbs.add(new BehaviorProbByTime(attributeType, dayType,0,0,activityProb));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Behavior is Initialized");
    }


    // 指定された属性，時刻，曜日の遷移する行動を確率的に算出するメソッド
    public static Behavior.BehaviorType getNextBehavior(AttributeType attributeType, BehaviorType currentBehavior, int current_h, int current_m, Day.DayType currentDay){
        double[] probabilities = AlltransitionDataMap.get(attributeType).getTransitionProbabilities(
                Behavior.BEHAVIOR_TYPE_ORDERING.indexOf(currentBehavior),Day.getTimeTick(current_h,current_m),currentDay);
        // ルーレット選択を行う
        // 確率の合計を計算
        double sum = 0.0;
        for (double probability : probabilities) {
            sum += probability;
        }
        // 0からsumの範囲で乱数を生成
        Random rand = new Random();
        double randomValue = rand.nextDouble() * sum;
        // 累積確率を計算して選択する
        Behavior.BehaviorType selectedBehavior = null;
        double cumulativeProbability = 0.0;
        for (int i = 0; i < probabilities.length; i++) {
            cumulativeProbability += probabilities[i];
            if (randomValue <= cumulativeProbability) {
                selectedBehavior = Behavior.BEHAVIOR_TYPE_ORDERING.get(i);
                break;
            }
        }
        return selectedBehavior;
    }

    public static void main(String[] args) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(PATH_TO_INITIAL_BEHAVIOR_PROB))); // ファイルを読み込んで文字列に変換
            JSONObject jsonObject = new JSONObject(content); // JSON文字列をパース
            // JSONオブジェクトを走査して Map に格納
            for (String groupAndDay : jsonObject.keySet()) {
                JSONObject innerObject = jsonObject.getJSONObject(groupAndDay);
                HashMap<BehaviorType, Double> activityProb = new HashMap<>();
                for (String activity : innerObject.keySet()) {
                    activityProb.put(ACTIVITY_TO_BEHAVIOR_TYPE.get(activity), innerObject.getDouble(activity));
                }
                AttributeType attributeType = AttributeType.valueOf(groupAndDay.split("-")[0]);
                Day.DayType dayType = Day.DayType.valueOf(groupAndDay.split("-")[1]);
                InitialBehaviorProbs.add(new BehaviorProbByTime(attributeType, dayType,0,0,activityProb));
            }
            // 読み込んだデータを表示
            System.out.println(getBehaviorProbByTime(AttributeType.MALE_20, Day.DayType.MONDAY).getBehaviorByRate());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main1(String[] args) {
        try {
            // 初期化：全ファイルを一度だけ読み込む
            Behavior.initialize();

            // 属性に基づいてTransitionProbabilityDataを取得

            // 現在の行為nと時刻tから、遷移確率を取得する
            BehaviorType currentActivity = BehaviorType.SLEEP;
            AttributeType attributeType = AttributeType.FEMALE_20;
            BehaviorType nextBehavior = getNextBehavior(attributeType, currentActivity, 2, 0, Day.DayType.MONDAY);

            System.out.println("Next behavior is "+ nextBehavior);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
