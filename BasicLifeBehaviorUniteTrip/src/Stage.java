/**
 * ステージ定義
 * @author miyanishi
 */
public enum Stage {
    /** 状態変更ステージ */
    Deactivate,
    /** 行為決定ステージ */
    DecideBehavior,
    /** 人流データでの検証用にログを取得するステージ */
    LocationFetching,
}
