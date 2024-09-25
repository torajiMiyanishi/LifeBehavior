package jp.soars.modules.gis_otp.logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.lang.Integer;

/**
 * TPersonTripLoggerが出力した人の移動ログと，TRouteLoggerが出力した経路ログを使って，
 * Kepler.gl用のGeoJson形式のファイルを作成する．
 */
public class TKeplerFileMaker {

    /** 人の移動情報ファイル */
    private BufferedReader fPersonTripFile;

    /** 経路リスト */
    private List<TRoute> fRoutes;

    /** Kepler.gl用のGeoJsonファイル */
    private PrintWriter fKeplerFile;

    /** 現在読み込んでいる移動情報のトークンの配列 */
    private String[] fTokens;

    /** Kepler.gl用のGeoJsonにはじめて経路を書き込むか？ */
    private boolean fFirstOutput;

    /**
     * コンストラクタ
     * @param personTripFile 人の移動情報ファイル
     * @param routeFile 経路ファイル
     * @param keplerFile Kepler.gl用のファイル（出力先）
     * @throws IOException
     */
    public TKeplerFileMaker(String personTripFile, String routeFile, String keplerFile) throws IOException {
        openPersonTripFile(personTripFile);
        fRoutes = TRoute.readTrips(routeFile);
        openKeplerFile(keplerFile);
        fFirstOutput = true;
    }

    /**
     * 全てのファイルを閉じる．
     * @throws IOException
     */
    public void close() throws IOException {
        closePersonTripFile();
        closeKeplerFile();
    }

    /**
     * Kepler.gl用のGeoJsonファイルをオープンする．
     * @param path ファイルパス
     * @throws FileNotFoundException
     */
    public void openKeplerFile(String path) throws FileNotFoundException {
        fKeplerFile = new PrintWriter(path);
        fKeplerFile.println("{\"type\": \"FeatureCollection\",\"features\": [");
    }

    /**
     * Kepler.gl用のGeoJsonファイルをクローズする．
     */
    public void closeKeplerFile() {
        fKeplerFile.println("]}");
        fKeplerFile.close();
    }


    /**
     * 人の移動ファイルをオープンする
     * @param path ファイルパス
     * @throws IOException
     */
    public void openPersonTripFile(String path) throws IOException {
        fPersonTripFile = new BufferedReader(new FileReader(path));
        fPersonTripFile.readLine(); // Skip header
    }

    /**
     * 人の移動ファイルをクローズする
     * @throws IOException
     */
    public void closePersonTripFile() throws IOException {
        fPersonTripFile.close();
    }

    /**
     * 次の移動情報を読み込む
     * @return true:次が存在する，falase:次が存在しない
     * @throws IOException
     */
    public boolean readNext() throws IOException {
        String line = fPersonTripFile.readLine();
        if (line == null) {
            return false;
        }
        fTokens = line.split(",");
        return true;
    }

    /**
     * Person IDを返す．
     * @return Person ID
     */
    public int getPersonID() {
        return Integer.parseInt(fTokens[0]);
    }

    /**
     * 移動開始日を返す．
     * @return 移動開始日
     */
    public int getStartDay() {
        return Integer.parseInt(fTokens[1]);
    }
    
    /**
     * 移動開始時を返す．
     * @return 移動開始時
     */
    public int getStartHour() {
        return Integer.parseInt(fTokens[2]);
    }

    /**
     * 移動開始分を返す．
     * @return 移動開始分
     */
    public int getStartMinute() {
        return Integer.parseInt(fTokens[3]);
    }

    /**
     * 移動終了日を返す．
     * @return 移動終了日
     */
    public int getEndDay() {
        return Integer.parseInt(fTokens[4]);
    }
    
    /**
     * 移動終了時を返す．
     * @return 移動終了時
     */
    public int getEndHour() {
        return Integer.parseInt(fTokens[5]);
    }

    /**
     * 移動終了分を返す．
     * @return 移動終了分
     */
    public int getEndMinute() {
        return Integer.parseInt(fTokens[6]);
    }

    /**
     * 出発地スポット名を返す．
     * @return 出発地スポット名
     */
    public String getOrigin() {
        return fTokens[7];
    }

    /**
     * 目的地スポット名を返す．
     * @return 目的地スポット名
     */
    public String getDestination() {
        return fTokens[8];
    }

    /**
     * 経路IDを返す．
     * @return 経路ID
     */
    public int getRouteID() {
        return Integer.parseInt(fTokens[9]);
    }

    /**
     * 移動手段を返す．
     * @return 移動手段
     */
    public String getModes() {
        return fTokens[10];
    }

    /**
     * 徒歩時間（秒）を返す．
     * @return 徒歩時間（秒）
     */
    public int getWakingTime() {
        return Integer.parseInt(fTokens[11]);
    }

    /**
     * 徒歩距離（メートル）を返す．
     * @return （メートル）
     */
    public double getWalkingDistance() {
        return Double.parseDouble(fTokens[12]);
    }

    /**
     * 現在読み込んでいる移動情報に対応する経路をKepler.gl用ファイルに出力する．
     */
    public void writeRoute() {
        int routeID = getRouteID();
        int startDay = getStartDay();
        if (fFirstOutput) {
            fFirstOutput = false;
        } else {
            fKeplerFile.print(",");
        }
        fRoutes.get(routeID).writeTo(startDay, fKeplerFile);
    }

}
