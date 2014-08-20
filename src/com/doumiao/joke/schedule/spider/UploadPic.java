package com.doumiao.joke.schedule.spider;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class UploadPic {
	Log log = LogFactory.getLog(UploadPic.class);

	@Resource
	private JdbcTemplate jdbcTemplate;

	@Scheduled(fixedDelay = 10000)
	public void upload() {

		UpYun upyun = new UpYun("yixiaoqianjin", "zhangjianyong", "Danawa1234");
		upyun.setApiDomain(UpYun.ED_AUTO);
		upyun.setTimeout(60);
		upyun.setDebug(true);
		List<Map<String, Object>> articles = jdbcTemplate
				.queryForList("select id, article_id, pic from joke_upload_pic limit 0,100");

		String path = jdbcTemplate
				.queryForObject(
						"select value from joke_config where `key` = 'pic_upload_path'",
						String.class);
		for (Map<String, Object> article : articles) {
			try {
				int id = (Integer) article.get("id");
				int articleId = (Integer) article.get("article_id");
				String pic = (String) article.get("pic");

				File picFile = new File(path + pic);
				if (!picFile.isFile()) {
					log.error("本地待上传的测试文件不存在!");
				}

				upyun.setContentMD5(UpYun.md5(picFile));
				boolean result = upyun.writeFile(pic, picFile, true);
				if (result) {
					jdbcTemplate.update(
							"update joke_article set is_show = 1 where id = ?",
							articleId);
					jdbcTemplate.update("delete from joke_upload_pic where id = ?",
							id);
				}else{
					log.error("上传失败:"+picFile.getAbsolutePath());
				}
			} catch (Exception e) {
				log.error(e, e);
			}
		}
	}
}
