import jp.soars.core.TAgent;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;


/**
 * 住民役割
 * @author miyanishi
 */


public final class RoleOfResident extends TRole {

    /** 自宅 */
    private final TSpot fHome;

    /** 会社 */
    private final TSpot fVisitedLocation;

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     * @param home 自宅
     * @param visitedLocation 会社
     */
    public RoleOfResident(TAgent owner, TSpot home, TSpot visitedLocation) {
        // 親クラスのコンストラクタを呼び出す．
        // 以下の2つの引数は省略可能で，その場合デフォルト値で設定される．
        // 第3引数:この役割が持つルール数 (デフォルト値 10)
        // 第4引数:この役割が持つ子役割数 (デフォルト値 5)
        super(RoleName.Resident, owner, 0, 0);

        fHome = home;
        fVisitedLocation = visitedLocation;
    }

    public TSpot getHome(){
        return fHome;
    }
    public TSpot getVisitedLocation(){
        return fVisitedLocation;
    }
}
