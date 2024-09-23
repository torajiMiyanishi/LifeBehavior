import jp.soars.core.TAgent;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;


import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;


/**
 * Poi役割
 * @author miyanishi
 */


public final class RoleOfPoi extends TRole{

    /** poiの名前 */
    private String fName;
    /** ジャンルコード（建物分類） */
    private String fGenreCode;
    /** ジャンル名 */
    private String fGenreName;
    /** 面積 */
    private double fBuildingArea;
    /** 産業大分類ラベル */
    private List<Behavior.IndustryType> fIndustryTypes = new ArrayList<>();
    /** 行為ラベル */
    private List<Behavior.BehaviorType> fBehaviorTypes = new ArrayList<>();



    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     * @param record csvファイルのレコード
     * */
    public RoleOfPoi(TSpot owner, String[] record){
        // 親クラスのコンストラクタを呼び出す．
        // 以下の2つの引数は省略可能で，その場合デフォルト値で設定される．
        // 第3引数:この役割が持つルール数 (デフォルト値 10)
        // 第4引数:この役割が持つ子役割数 (デフォルト値 5)
        super(RoleName.Resident, owner, 0, 0);

        fName = record[0];
        fGenreCode = String.valueOf(record[1]);
        fGenreName = record[5];
        fBuildingArea = Double.parseDouble(record[2]);
        for (String industryName :record[6].split(",")){
            fIndustryTypes.add(Behavior.IndustryType.valueOf(industryName));
        }
        for (String behaviorName :record[7].split(",")){
            fBehaviorTypes.add(Behavior.ACTIVITY_TO_BEHAVIOR_TYPE.get(behaviorName.replaceAll(" ","")));
        }
    }
    @Override
    public String toString() {
        return "RoleOfPoi{" +
                "fName='" + fName + '\'' +
                ", fGenreCode='" + fGenreCode + '\'' +
                ", fGenreName='" + fGenreName + '\'' +
                ", fBuildingArea=" + fBuildingArea +
                ", fIndustryTypes=" + fIndustryTypes +
                ", fBehaviorTypes=" + fBehaviorTypes +
                '}';
    }

}
