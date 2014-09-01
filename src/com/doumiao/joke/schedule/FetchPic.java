package com.doumiao.joke.schedule;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.imageio.ImageIO;

import org.apache.catalina.tribes.util.Arrays;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
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
		String[] types = {"gif","png","jpg","jpeg","bmp"};
		List<Map<String, Object>> articles = jdbcTemplate
				.queryForList("select id, pic_ori from joke_article where type = 'PIC' and `status` = 0 order by id desc limit 0,100");

		String path = jdbcTemplate
				.queryForObject(
						"select value from joke_config where `key` = 'pic_upload_path'",
						String.class);
		HttpClient client = HttpClientHelper.getClient();
		HttpGet get = null;
		HttpEntity entity = null;
		FileOutputStream fout = null;
		int sum = 0;
		for (Map<String, Object> article : articles) {
			String url = null;
			try {
				int id = (Integer) article.get("id");
				url = (String) article.get("pic_ori");
				String picType = url.substring(url.lastIndexOf(".") + 1);
				if(ArrayUtils.indexOf(types, picType)==-1){
					picType="jpg";
				}
				Calendar c = Calendar.getInstance();
				int year = c.get(Calendar.YEAR);
				int month = c.get(Calendar.MONTH) + 1;
				long millis = c.getTimeInMillis();
				String fileName = "/" + year + "/" + month + "/" + millis + "."
						+ picType;
				File file = new File(path + "/" + fileName);
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
				fout = new FileOutputStream(file);
				if ("gif".equals(picType.toLowerCase())) {
					get = new HttpGet(url);
					entity = client.execute(get).getEntity();
					IOUtils.copy(entity.getContent(), fout);
				} else {
					Image srcImg = ImageIO.read(new URL(url));
					int w = srcImg.getWidth(null);
					int h = srcImg.getHeight(null);
					BufferedImage buffImg = new BufferedImage(
							srcImg.getWidth(null), srcImg.getHeight(null),
							BufferedImage.TYPE_INT_RGB);
					Graphics2D g = buffImg.createGraphics();
					g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
							RenderingHints.VALUE_INTERPOLATION_BILINEAR);
					g.drawImage(
							srcImg.getScaledInstance(w, h, Image.SCALE_SMOOTH),
							0, 0, null);
					Image img = ImageIO.read(this.getClass()
							.getResourceAsStream("logo.png"));
					int _w = img.getWidth(null);
					int _h = img.getHeight(null);
					g.setComposite(AlphaComposite.getInstance(
							AlphaComposite.SRC_ATOP, 1f));
					int __h = _h > (int) (0.2 * h) ? (int) (0.2 * h) : _h;
					int __w = (int) ((double) __h / _h * _w);
					g.drawImage(
							img.getScaledInstance(__w, __h, Image.SCALE_SMOOTH),
							w - __w - 10, h - __h - 10, null);
					g.setComposite(AlphaComposite
							.getInstance(AlphaComposite.SRC_OVER));
					g.dispose();
					ImageIO.write(buffImg, picType, fout);
				}
				sum++;
				jdbcTemplate
						.update("update joke_article set pic = ?, status = ? where id = ?",
								fileName, 1, id);
				jdbcTemplate
						.update("insert into joke_upload_pic(article_id, pic) values(?,?)",
								id, fileName);
			} catch (SocketTimeoutException ste) {
				log.error("timeout:" + url);
			} catch (Exception e) {
				log.error(url, e);
			} finally {
				IOUtils.closeQuietly(fout);
			}
		}
		if (log.isInfoEnabled()) {
			log.info("fetch pic:" + sum);
		}
	}

	public static void main(String[] args) {
		String url = "http://i1.juyouqu.com/uploads/content//2013/01/1357282467422.gif";
		System.out.println(url.substring(url.lastIndexOf(".") + 1));
	}
}
