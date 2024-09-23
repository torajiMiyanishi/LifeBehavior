import jp.soars.modules.gis_otp.logger.TKeplerFileMaker;

import java.io.File;
import java.io.IOException;

/**
 * 移動データファイルと経路データファイルを用いて，全エージェントの0日目の移動経路を
 * Kepler.gl用ファイルに出力するサンプルプログラム．
 */
public class TSampleOfMakingKeplerJson {
    /**
     * メインメソッド
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String workDir = "C:\\Users\\tora2\\IdeaProjects\\LifeBehavior\\logs"; //入力ファイルが格納されているディレクトリ．出力先にもなる．
        String personTripFile = workDir + File.separator + "person_trips.csv"; //各個人の移動データのファイル．入力ファイル．
        String routeFile = workDir + File.separator + "person_trips_routes.obj"; //経路データのファイル．入力ファイル．
        String outputFile = workDir + File.separator + "trips.json"; //kepler.gl用のファイル．出力ファイル．
        TKeplerFileMaker kfm = new TKeplerFileMaker(personTripFile, routeFile, outputFile); //Kepler.gl用ファイルの生成器
        while (kfm.readNext()) { //次の個人移動データを読み込む．
            if (kfm.getStartDay() == 0) { //移動開始日が0日目ならば
                kfm.writeRoute(); //Kepler.gl用ファイルに経路を出力する．
            }
        }
        kfm.close(); //全ファイルをクローズする．
    }

}
