public class Day{
    public enum DayType {
        SUNDAY,
        MONDAY,
        TUESDAY,
        WEDNESDAY,
        THURSDAY,
        FRIDAY,
        SATURDAY,
    }

    // 指定された int 値に基づいて曜日を返すメソッド
    public static DayType getDay(int dayNumber) {
        // dayNumber を 7 で割った余りを使って対応する曜日を返す
        return DayType.values()[dayNumber % 7];
    }

    // hh, mm の時刻をもとに 15 分刻みでのタイムティックを返すメソッド
    public static int getTimeTick(int hh, int mm) {
        // 時間のチェック（範囲外の時刻を処理するために例外を投げる）
        if (hh < 0 || hh >= 24 || mm < 0 || mm >= 60) {
            throw new IllegalArgumentException("Invalid time provided: " + hh + ":" + mm);
        }

        // 分を15分単位に切り上げる
        int minuteTick = mm / 15;
        return hh * 4 + minuteTick;
    }
}



