package jp.soars.modules.gis_otp.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 *  zip形式のファイルをファイルシステムに展開するクラス
 */
public class TCAdvancedUnzip {

	/** データの読み込み終了を表す定数 */
	public static final int EOF = -1;
	
	/**
	 * コンストラクタ
	 */
	public TCAdvancedUnzip() {
	}
	
	/**
	 * ZIPファイルを解凍する．
	 * @param workingDir ワーキングディレクトリ
	 * @param filename ファイル名
	 * @throws IOException
	 */
	public void doIt(String workingDir, String filename) throws IOException {
		ZipFile zipFile = new ZipFile(workingDir + File.separator + filename);
		Enumeration<?> entries = zipFile.entries();
		while( entries.hasMoreElements() ) {
			ZipEntry target = (ZipEntry)entries.nextElement();
			getEntry(workingDir, zipFile, target);
		}
		zipFile.close();
	}

	/**
	 * 与えられた ZipEntry の内容を取り出して再生する．
	 * @param zipFile ZIPファイル
	 * @param target ZIPエントリ
	 * @throws ZipException
	 * @throws IOException
	 */
	private void getEntry(String workingDir, ZipFile zipFile, ZipEntry target) throws ZipException, IOException {
		File file = new File(workingDir + File.separator + target.getName());
		if (target.isDirectory()) {
			file.mkdirs();
		} else {
			BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(target));
			String parentName;
			if ((parentName = file.getParent()) != null) {
				File dir = new File(parentName);
				dir.mkdirs();
			}
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
			int c;
			while ((c = bis.read()) != EOF) {
				bos.write((byte) c);
			}
			bos.close();
			bis.close();
		}
	}
	
	/**
	 * メインメソッド
	 * @param args
	 */
	public static void main( String args[] ) {
		if(args.length != 2) {
			System.err.println("usage: java TUnzip workingDir zipfile");
			System.exit(1);
		}
		String workingDir = args[0];
		String zipfile = args[1];
		try {
			TCAdvancedUnzip unzip = new TCAdvancedUnzip();
			unzip.doIt(workingDir, zipfile);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(5);
		}
	}


}
