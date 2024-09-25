package jp.soars.modules.gis_otp.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * ディレクトリをZIP形式でアーカイブするクラス
 * @author isao
 *
 */
public class TCAdvancedZip {

	/** ファイルの読み込みの終わりを表す定数 */
	public static final int EOF = -1;

	public static final String SEPARATOR = "/";

	/**
	 * コンストラクタ
	 *
	 */
	public TCAdvancedZip() {
	}

	/**
	 * アーカイブを実行する．
	 * @param workingDir ワーキングディレクトリ
	 * @param targetDir 対象のディレクトリ
	 * @param ZIPのファイル名
	 * @throws IOException
	 */
	public void doIt(String workingDir, String targetDir, String zipFilename) throws IOException {
		File file = new File(workingDir + SEPARATOR + targetDir);
		File zipFile = new File(zipFilename);
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
		if (file.isFile()) {
			appendFileToZipOutputStream(zos, workingDir, targetDir);
		} else {
			ArrayList<String> pathNames = collectPathNames(workingDir, targetDir);
			Iterator<String> itr = pathNames.iterator();
			while (itr.hasNext()) {
				String p = itr.next();
				if (p.startsWith("./")) {
					appendFileToZipOutputStream(zos, workingDir, p.substring(2));
				} else {
					appendFileToZipOutputStream(zos, workingDir, p);
				}
			}
		}
		zos.close();
	}

	/**
	 * 指定されたディレクトリ以下のパス名を再帰的に収集する．
	 * @param workingDir ワーキングディレクトリ
	 * @param parentDirectoryName ディレクトリ名
	 * @return 再帰的に収集されたパス名の配列
	 */
	private ArrayList<String> collectPathNames(String workingDir, String parentDirectoryName) {
		File parentDirectory = new File(workingDir + File.separator + parentDirectoryName);
		if (!parentDirectory.exists()) {
			System.err.println("Error: " + parentDirectory.getName() + " does not exist!!");
			System.exit(5);
		}
		String[] list = parentDirectory.list();
		File[] files = new File[list.length];
		ArrayList<String> pathNames = new ArrayList<String>();
		for (int i = 0; i < list.length; i++) {
			String pathName = parentDirectoryName + SEPARATOR + list[i];
			files[i] = new File(workingDir + SEPARATOR + pathName);
			if (files[i].isFile()) {
				pathNames.add(pathName);
			} else if (files[i].list().length == 0) {
				pathNames.add(pathName);
			} else {
				String name = parentDirectoryName + SEPARATOR + list[i];
				ArrayList<String> subPathNames = collectPathNames(workingDir, name);
				for (String subPathName: subPathNames) {
					pathNames.add(subPathName);
				}
			}
		}
		return pathNames;
	}

	/**
	 * ZIPストリームへファイルを追加する．
	 * @param zos ZIPストリーム
	 * @param file ファイル
	 * @throws IOException
	 */
	private void appendFileToZipOutputStream(ZipOutputStream zos, String baseDir, String pathName) throws IOException {
		File f = new File(baseDir + SEPARATOR + pathName);
		if (f.isFile()) {
			ZipEntry target = new ZipEntry(pathName);
			zos.putNextEntry(target);
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(baseDir + SEPARATOR + pathName));
			byte buf[] = new byte[1024];
			int count;
			while ((count = bis.read(buf, 0, 1024)) != EOF) {
				zos.write(buf, 0, count);
			}
			bis.close();
		} else {
			ZipEntry target = new ZipEntry(pathName + SEPARATOR);
      target.setMethod(ZipEntry.STORED);
      target.setSize(0);
      target.setCrc(0);
			zos.putNextEntry(target);
		}
		zos.closeEntry();
	}

	/**
	 * メインメソッド
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println("usage: java TAdvancedZip workingDir targetDir zipFilename");
			System.exit(1);
		}
		String workingDir = args[0];
		String targetDir = args[1];
		String zipFilename = args[2];
		TCAdvancedZip zip = new TCAdvancedZip();
		try {
			zip.doIt(workingDir, targetDir, zipFilename);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(5);
		}
	}

}
