package com.doumiao.joke.lang;

import com.doumiao.joke.schedule.UpYun;

public class UpYunHelper {
	private static final String BUCKET_NAME = "yixiaoqianjin";
	private static final String USER_NAME = "zhangjianyong";
	private static final String USER_PWD = "Danawa1234";

	private static UpYun upyun = null;

	static {
		upyun = new UpYun(BUCKET_NAME, USER_NAME, USER_PWD);
		upyun.setApiDomain(UpYun.ED_AUTO);
		upyun.setTimeout(60);
		upyun.setDebug(true);
	}

	public static UpYun getClient() {
		return upyun;
	}
}
