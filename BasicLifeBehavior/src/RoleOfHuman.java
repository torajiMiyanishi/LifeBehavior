import jp.soars.core.TAgent;
import jp.soars.core.TRole;


/**
 * 住民役割
 * @author miyanishi
 */


public final class RoleOfHuman extends TRole {

    /** 年齢 */
    public static int fAge;

    /** 性別 */
    public static Behavior.Gender fGender;

    /***/
    public RoleOfHuman(TAgent owner, int age, Behavior.Gender gender) {
        // 親クラスのコンストラクタを呼び出す．
        // 以下の2つの引数は省略可能で，その場合デフォルト値で設定される．
        // 第3引数:この役割が持つルール数 (デフォルト値 10)
        // 第4引数:この役割が持つ子役割数 (デフォルト値 5)
        super(RoleName.Human, owner, 0, 0);

        fAge = age;
        fGender = gender;
    }
    public int getAge(){return fAge;}
    public Behavior.Gender getGender(){return fGender;}
}
