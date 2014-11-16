package com.doumiao.joke.test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.doumiao.joke.lang.UpYunHelper;
import com.doumiao.joke.schedule.UpYun;
import com.doumiao.joke.schedule.UpYun.FolderItem;

public class UpYunTest {
	
	public void uploadStatic() {
		String[] dirs = new String[] {
				//"D:/data/workspace/pri/java/joke/WebContent/static/js",
				"D:/data/workspace/pri/java/joke/WebContent/static/css"
				//"D:/data/workspace/pri/java/joke/WebContent/static/flash",
				//"D:/data/workspace/pri/java/joke/WebContent/static/images",
				//"D:/data/workspace/pri/java/joke/WebContent/static/avatar"
				};
		for (String d : dirs) {
			File df = new File(d);
			dir(df);
		}
	}

	public void dir(File file) {
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				dir(f);
			}
		} else {
			try {
				String path = file.getPath().replace("D:\\data\\workspace\\pri\\java\\joke\\WebContent\\", "");
				System.out.println(path);
				UpYun yun = UpYunHelper.getClient();
				yun.setContentMD5(UpYun.md5(file));
				boolean result = yun.writeFile(path, file, true);
				if (!result) {
					System.err.print("error");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Test
	public void total() {
		UpYun client = UpYunHelper.getClient();
		long usage = client.getBucketUsage();
		System.out.println("空间总使用量：" + usage / 1024 / 1024 + "M");
		listDir(client,"", false);
	}

	public void listDir(UpYun client, String path, boolean delete) {
		List<FolderItem> items = client.readDir(path);
		for (FolderItem i : items) {
			String p = path + "/" + i.name;
			if (i.type.equals("Folder")) {
				System.out.println(i.type + ":" + p);
				listDir(client,p, delete);
				if (delete) {
					client.rmDir(p);
				}
			} else {
				System.out.println(i.type + ":" + p);
				if (delete) {
					client.deleteFile(p);
				}
			}
		}
	}
}
