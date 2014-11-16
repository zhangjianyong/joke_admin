package com.doumiao.joke.schedule;

import java.io.File;
import java.io.FileOutputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.imageio.ImageIO;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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
		String[] types = { "gif", "png", "jpg", "jpeg", "bmp" };
		List<Map<String, Object>> articles = jdbcTemplate
				.queryForList("select id, pic_ori,pic,fetch_site from joke_article where type = 'PIC' and `status` = 0 order by id desc limit 0,100");

		String path = jdbcTemplate
				.queryForObject(
						"select value from joke_config where `key` = 'pic_upload_path'",
						String.class);
		HttpClient client = HttpClientHelper.getClient();
		HttpGet get = null;
		HttpEntity entity = null;
		FileOutputStream fout = null;
		int sum = 0,error = 0;;
		for (Map<String, Object> article : articles) {
			String url = null;
			String fileName = null;
			try {
				int id = (Integer) article.get("id");
				Object picOri = article.get("pic_ori");
				Object pic = article.get("pic");
				if (picOri != null) {
					url = (String) picOri;
					String picType = url.substring(url.lastIndexOf(".") + 1);
					if (ArrayUtils.indexOf(types, picType) == -1) {
						picType = "jpg";
					}
					Calendar c = Calendar.getInstance();
					int year = c.get(Calendar.YEAR);
					int month = c.get(Calendar.MONTH) + 1;
					long millis = c.getTimeInMillis();
					fileName = "/" + year + "/" + month + "/" + millis + "."
							+ picType;
					File file = new File(path + "/" + fileName);
					if (!file.getParentFile().exists()) {
						file.getParentFile().mkdirs();
					}
					fout = new FileOutputStream(file);
					if (StringUtils.isNotBlank(url)) {
						if ("gif".equals(picType.toLowerCase())) {
							get = new HttpGet(url);
							entity = client.execute(get).getEntity();
							IOUtils.copy(entity.getContent(), fout);
						} else {
							Thumbnails
									.of(new URL(url))
									.watermark(
											Positions.BOTTOM_RIGHT,
											ImageIO.read(this.getClass()
													.getResourceAsStream(
															"logo.png")), 1f)
									.outputQuality(1).scale(1).toFile(file);
						}
					}
				} else if (pic != null && article.get("fetch_site")==null) {
					fileName = (String) pic;
				}
				sum++;
				jdbcTemplate
						.update("update joke_article set pic = ?, status = ? where id = ?",
								fileName, 1, id);
				jdbcTemplate
						.update("insert into joke_upload_pic(article_id, pic) values(?,?)",
								id, fileName);
			} catch (SocketTimeoutException ste) {
				error++;
				log.error("timeout:" + url);
			} catch (Exception e) {
				error++;
				log.error(url, e);
			} finally {
				IOUtils.closeQuietly(fout);
			}
		}
		if (log.isInfoEnabled()) {
			log.info("fetch pic:" + sum+" error:"+error);
		}
	}

	public static void main(String[] args) {
		String url = "http://i1.juyouqu.com/uploads/content//2013/01/1357282467422.gif";
		System.out.println(url.substring(url.lastIndexOf(".") + 1));
	}
}
