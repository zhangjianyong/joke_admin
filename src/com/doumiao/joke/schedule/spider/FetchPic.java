package com.doumiao.joke.schedule.spider;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.doumiao.joke.lang.HttpClientHelper;

@Component
public class FetchPic {
	private static final Log log = LogFactory.getLog(FetchPic.class);

	@Resource
	private JdbcTemplate jdbcTemplate;

	@Scheduled(fixedDelay = 60000)
	public void fetch() {
		List<Map<String, Object>> articles = jdbcTemplate
				.queryForList("select id, pic_ori from joke_article where type = 'PIC' and is_show = 0 limit 0,100");

		String path = jdbcTemplate
				.queryForObject(
						"select value from joke_config where `key` = 'pic_upload_path'",
						String.class);
		HttpClient client = HttpClientHelper.getClient();
		HttpGet get = null;
		HttpEntity entity = null;
		FileOutputStream fout = null;
		for (Map<String, Object> article : articles) {
			try {
				int id = (Integer) article.get("id");
				String url = (String) article.get("pic_ori");
				get = new HttpGet(url);
				HttpResponse response = client.execute(get);
				entity = response.getEntity();
				Calendar c = Calendar.getInstance();
				int year = c.get(Calendar.YEAR);
				int month = c.get(Calendar.MONTH);
				long millis = c.getTimeInMillis();
				String fileName = year + "/" + month + "/" + millis + ".jpg";
				File file = new File(path + fileName);
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
				fout = new FileOutputStream(file);
				IOUtils.copy(entity.getContent(), fout);
				jdbcTemplate.update(
						"update joke_article set pic = ? where id = ?",
						fileName, id);
				jdbcTemplate
						.update("insert into joke_upload_pic(article_id, pic) values(?,?)",
								id, fileName);
			} catch (Exception e) {
				log.error(e, e);
			} finally {
				EntityUtils.consumeQuietly(entity);
				IOUtils.closeQuietly(fout);
			}
		}
	}
}
