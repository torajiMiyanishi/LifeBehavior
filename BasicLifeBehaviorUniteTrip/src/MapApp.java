import java.util.*;

import jp.soars.core.TSpot;

/**
 * poi情報の検索条件に基づいたSpotを抽出するクラス
 * のちのマップ機能
 */
public class MapApp {
    /** マスタ類 */
    public static final Map<String, List<TSpot>> GENRE_CODE_TO_POI_SPOTS = new HashMap<>(); // ジャンルコードとpoi spotの辞書
    public static final Map<Behavior.BehaviorType, List<TSpot>> BEHAVIOR_TYPE_TO_POI_SPOTS = new HashMap<>(); // 行為ラベルとpoi spotの辞書
    public static final Map<Behavior.IndustryType, List<TSpot>> INDUSTRY_TYPE_TO_POI_SPOTS = new HashMap<>(); // 産業大分類ラベルとpoi spotの辞書

    /** poiSpotが生成されるごとに，マスタを更新する */
    public static void update(TSpot poiSpot){
        RoleOfPoi poiRole = (RoleOfPoi) poiSpot.getRole(RoleName.Poi);

        // ジャンルコードに基づいて更新
        String genreCode = poiRole.getGenreCode();
        GENRE_CODE_TO_POI_SPOTS.putIfAbsent(genreCode, new ArrayList<>());
        GENRE_CODE_TO_POI_SPOTS.get(genreCode).add(poiSpot);

        // 行為ラベルに基づいて更新
        for (Behavior.BehaviorType behaviorType: poiRole.getBehaviorTypes()){
            BEHAVIOR_TYPE_TO_POI_SPOTS.putIfAbsent(behaviorType, new ArrayList<>());
            BEHAVIOR_TYPE_TO_POI_SPOTS.get(behaviorType).add(poiSpot);
        }

        // 産業大分類ラベルに基づいて更新
        for (Behavior.IndustryType industryType: poiRole.getIndustryTypes()){
            INDUSTRY_TYPE_TO_POI_SPOTS.putIfAbsent(industryType, new ArrayList<>());
            INDUSTRY_TYPE_TO_POI_SPOTS.get(industryType).add(poiSpot);
        }
    }

    /** Getterメソッドの実装例 */
    public static List<TSpot> getSpotsByGenreCode(String genreCode) {
        return GENRE_CODE_TO_POI_SPOTS.getOrDefault(genreCode, Collections.emptyList());
    }

    public static List<TSpot> getSpotsByBehaviorType(Behavior.BehaviorType behaviorType) {
        return BEHAVIOR_TYPE_TO_POI_SPOTS.getOrDefault(behaviorType, Collections.emptyList());
    }

    public static List<TSpot> getSpotsByIndustryType(Behavior.IndustryType industryType) {
        return INDUSTRY_TYPE_TO_POI_SPOTS.getOrDefault(industryType, Collections.emptyList());
    }
}
