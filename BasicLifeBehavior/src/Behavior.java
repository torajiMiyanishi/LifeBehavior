import org.json.JSONArray;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Behavior {

    public static final String DIR_DATA = "Z:\\lab\\lifebehavior\\"; // 遷移確率のディレクトリ

    private static final Map<AttributeType, TransitionProbabilityData> AlltransitionDataMap = new HashMap<>(); // キャッシュ用マップ
    public static final List<String> ACTIVITY_ORDERING = List.of(
            "睡眠", "食事", "身のまわりの用事", "療養・静養", "仕事", "仕事のつきあい", "授業・学内の活動",
            "学校外の学習", "炊事・掃除・洗濯", "買い物", "子どもの世話", "家庭雑事", "通勤", "通学", "社会参加",
            "会話・交際", "スポーツ", "行楽・散策", "趣味・娯楽・教養", "マスメディア接触", "休息");
    public static final List<BehaviorType> BEHAVIOR_TYPE_ORDERING = new ArrayList<>();
    public static final Map<BehaviorType, String> BEHAVIOR_TYPE_TO_ACTIVITY = new HashMap<>();
    public static final Map<String, BehaviorType> ACTIVITY_TO_BEHAVIOR_TYPE = new HashMap<>();
    // BehaviorType毎に「外出」か「在宅」をラベルとしてマッピングする辞書
    public static final Map<BehaviorType, LocationType> BEHAVIOR_LOCATION_LABEL = new HashMap<>();

    // 「外出」「在宅」を表す enum の定義
    public enum LocationType {
        OUTDOOR, INDOOR      // 外出，在宅
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
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.SLEEP, LocationType.INDOOR);
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.EATING, LocationType.INDOOR);
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.PERSONAL_CARE, LocationType.INDOOR);
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.MEDICAL_CARE, LocationType.OUTDOOR);
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.WORK, LocationType.OUTDOOR);
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.WORK_SOCIALIZING, LocationType.OUTDOOR);
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.SCHOOL_ACTIVITIES, LocationType.OUTDOOR);
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.STUDY_OUTSIDE, LocationType.OUTDOOR);
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.HOUSEWORK, LocationType.INDOOR);
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.SHOPPING, LocationType.OUTDOOR);
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.CHILDCARE, LocationType.INDOOR);
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.HOUSEHOLD_TASKS, LocationType.INDOOR);
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.COMMUTING, LocationType.OUTDOOR);
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.SCHOOL_COMMUTING, LocationType.OUTDOOR);
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.SOCIAL_PARTICIPATION, LocationType.OUTDOOR);
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.SOCIALIZING, LocationType.OUTDOOR);
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.SPORTS, LocationType.OUTDOOR);
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.RECREATION, LocationType.OUTDOOR);
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.HOBBIES, LocationType.INDOOR);
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.MEDIA_CONSUMPTION, LocationType.INDOOR);
        BEHAVIOR_LOCATION_LABEL.put(BehaviorType.REST, LocationType.INDOOR);
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

    // イニシャライザ：すべての属性ごとにTransitionProbabilityDataを読み込んでキャッシュする
    public static void initialize() throws Exception {
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
