package com.doumiao.joke.schedule;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.doumiao.joke.lang.HttpClientHelper;
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
		int sum = 0, error = 0;
		HttpResponse response = null;
		HttpGet get = null;
		for (Map<String, Object> article : articles) {
			int id = (Integer) article.get("id");
			int articleId = (Integer) article.get("article_id");
			String pic = (String) article.get("pic");
			try {
				File picFile = new File(path + "/" + pic);

				upyun.setContentMD5(UpYun.md5(picFile));
				boolean result = upyun.writeFile("article/0" + pic, picFile,
						true);

				// 压缩图
				Map<String, String> params = new HashMap<String, String>();
				params.put(PARAMS.KEY_X_GMKERL_TYPE.getValue(),
						PARAMS.VALUE_FIX_BOTH.getValue());
				params.put(PARAMS.KEY_X_GMKERL_VALUE.getValue(), "90x90");
				params.put(PARAMS.KEY_X_GMKERL_QUALITY.getValue(), "95");
				params.put(PARAMS.KEY_X_GMKERL_UNSHARP.getValue(), "true");
				boolean r = upyun.writeFile("/article/90" + pic, picFile, true,
						params);
				sum++;
				HttpClient client = HttpClientHelper.getClient();
				String picDomain = Config.get("pic_domain");
				get = new HttpGet(picDomain + "/article/0" + pic);
				response = client.execute(get);
				if (result & r
						& response.getStatusLine().getStatusCode() == 200) {
					if (log.isDebugEnabled()) {
						log.debug(get.getURI());
					}
					jdbcTemplate
							.update("update joke_article set `status` = ? where id = ?",
									2, articleId);
					jdbcTemplate.update(
							"delete from joke_upload_pic where id = ?", id);
				} else {
					error++;
					log.error("上传失败:" + picFile.getAbsolutePath());
				}
			} catch (FileNotFoundException fnfe) {
				error++;
				log.error(fnfe.getMessage());
			} catch (SocketTimeoutException ste) {
				error++;
				log.error(ste.getMessage());
			} catch (IOException e) {
				error++;
				String msg = e.getMessage();
				log.error(e.getMessage());
				if (msg.contains("not an image")) {
					jdbcTemplate
							.update("update joke_article set `status` = ? where id = ?",
									0, articleId);
					jdbcTemplate.update(
							"delete from joke_upload_pic where id = ?", id);
				}
			} catch (Exception e) {
				error++;
				log.error(e, e);
			} finally {
				get.releaseConnection();
			}
			// java.io.IOException:
			// {"msg":"not an image","code":40300018,"id":"e2f1031873b5a01b46488416197815d6"}
		}
		if (log.isInfoEnabled()) {
			log.info("upload pic:" + sum + " error:" + error);
		}
	}
}
