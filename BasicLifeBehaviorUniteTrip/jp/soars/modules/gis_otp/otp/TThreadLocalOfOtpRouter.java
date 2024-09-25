package jp.soars.modules.gis_otp.otp;

import java.io.IOException;

/**
 * TOtpRouterオブジェクトを各スレッドに割り付けるためのThreadLocalオブジェクトをもつクラス。
 */
public class TThreadLocalOfOtpRouter {

    /** OSMデータファイル */
    private static String fPathToPbf;
    
    /** GTFSデータファイルが格納されているディレクトリ */
    private static String fDirectoryOfGtfs;

    /** スレッドごとに用意されるOTPルータ */
    private static ThreadLocal<TOtpRouter> fRouters;

    /**
     * コンストラクタ。
     * privateにすることにより、newできなくしている。
     */
    private TThreadLocalOfOtpRouter() {}

    /**
     * 初期化する。
     * @param pathToPbf PBFファイルへのパス
     * @param directoryOfGtfs GTFSデータがあるディレクトリ
     */
    public static void initialize(String pathToPbf, String directoryOfGtfs) {
        fPathToPbf = pathToPbf;
        fDirectoryOfGtfs = directoryOfGtfs;
        fRouters = new ThreadLocal<TOtpRouter>() {
            @Override
            protected TOtpRouter initialValue() {
                try {
                    TOtpRouter r = new TOtpRouter(fPathToPbf, fDirectoryOfGtfs);
                    return r;
                } catch (IOException e) {
                    System.err.println("Initializing TOtpRouter failed because " 
                                       +  pathToPbf + " and/or " + fDirectoryOfGtfs 
                                       + " may be invalid.");
                    e.printStackTrace();
                    return null;
                }
            };
        };
    }

    /**
     * カレントスレッドに対応するTOtpRouterオブジェクトを返す。
     * @return TOtpRouterオブジェクト
     */
    public static TOtpRouter get() {
        return fRouters.get();
    }    
}
