package com.doumiao.joke.schedule;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.doumiao.joke.schedule.UpYun.PARAMS;

@Component
public class UploadPic {
	Log log = LogFactory.getLog(UploadPic.class);

	@Resource
	private JdbcTemplate jdbcTemplate;

	@Scheduled(fixedDelay = 60000)
	public void upload() {

		UpYun upyun = new UpYun("yixiaoqianjin", "zhangjianyong", "Danawa1234");
		upyun.setApiDomain(UpYun.ED_AUTO);
		upyun.setTimeout(60);
		upyun.setDebug(true);
		List<Map<String, Object>> articles = jdbcTemplate
				.queryForList("select id, article_id, pic from joke_upload_pic order by id desc limit 0,100");

		String path = jdbcTemplate
				.queryForObject(
						"select value from joke_config where `key` = 'pic_upload_path'",
						String.class);
		int sum=0,error=0;
		
		for (Map<String, Object> article : articles) {
			try {
				int id = (Integer) article.get("id");
				int articleId = (Integer) article.get("article_id");
				String pic = (String) article.get("pic");

				File picFile = new File(path + "/" + pic);

				upyun.setContentMD5(UpYun.md5(picFile));
				boolean result = upyun.writeFile("article/0"+pic, picFile, true);

				// 压缩图
				Map<String, String> params = new HashMap<String, String>();
				params.put(PARAMS.KEY_X_GMKERL_TYPE.getValue(),
						PARAMS.VALUE_FIX_BOTH.getValue());
				params.put(PARAMS.KEY_X_GMKERL_VALUE.getValue(), "90x90");
				params.put(PARAMS.KEY_X_GMKERL_QUALITY.getValue(), "95");
				params.put(PARAMS.KEY_X_GMKERL_UNSHARP.getValue(), "true");
				boolean r = upyun.writeFile("/article/90"+pic, picFile, true, params);
				sum++;
				if (result & r) {
					jdbcTemplate.update(
							"update joke_article set `status` = ? where id = ?",
							2, articleId);
					jdbcTemplate.update(
							"delete from joke_upload_pic where id = ?", id);
				} else {
					error++;
					log.error("上传失败:" + picFile.getAbsolutePath());
				}
			} catch (FileNotFoundException fnfe) {
				log.error(fnfe.getMessage());
			} catch (Exception e) {
				log.error(e, e);
			}
		}
		if(log.isInfoEnabled()){
			log.info("upload pic:"+sum+" error:"+error);
		}
	}
}
