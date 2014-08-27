package com.doumiao.joke.schedule;
import java.util.List;

import com.doumiao.joke.schedule.UpYun.FolderItem;

public class FileBucketDemo {

	private static final String BUCKET_NAME = "yixiaoqianjin";
	private static final String USER_NAME = "zhangjianyong";
	private static final String USER_PWD = "Danawa1234";

	private static UpYun upyun = null;

	public static void main(String[] args) throws Exception {

		upyun = new UpYun(BUCKET_NAME, USER_NAME, USER_PWD);
		upyun.setApiDomain(UpYun.ED_AUTO);
		upyun.setTimeout(60);
		upyun.setDebug(true);
		long usage = upyun.getBucketUsage();
		System.out.println("空间总使用量：" + usage/1024/1024 + "M");
		listDir("", true);
	}

	public static void listDir(String path, boolean delete) {
		List<FolderItem> items = upyun.readDir(path);
		for (FolderItem i : items) {
			String p = path + "/" + i.name;
			if (i.type.equals("Folder")) {
				System.out.println(i.type + ":" + p);
				listDir(p, delete);
				if (delete) {
					upyun.rmDir(p);
				}
			} else {
				System.out.println(i.type + ":" + p);
				if (delete) {
					upyun.deleteFile(p);
				}
			}
		}
	}
}
