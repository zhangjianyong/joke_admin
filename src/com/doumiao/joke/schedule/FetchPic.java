package com.doumiao.joke.schedule;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.net.SocketTimeoutException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.imageio.ImageIO;

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
				.queryForList("select id, pic_ori from joke_article where type = 'PIC' and `status` = 0 limit 0,100");

		String path = jdbcTemplate
				.queryForObject(
						"select value from joke_config where `key` = 'pic_upload_path'",
						String.class);
		HttpClient client = HttpClientHelper.getClient();
		HttpGet get = null;
		HttpEntity entity = null;
		FileOutputStream fout = null;
		int sum=0;
		for (Map<String, Object> article : articles) {
			String url=null;
			try {
				int id = (Integer) article.get("id");
				url = (String) article.get("pic_ori");
				get = new HttpGet(url);
				HttpResponse response = client.execute(get);
				entity = response.getEntity();
				Calendar c = Calendar.getInstance();
				int year = c.get(Calendar.YEAR);
				int month = c.get(Calendar.MONTH) + 1;
				long millis = c.getTimeInMillis();
				String fileName = "/" + year + "/" + month + "/" + millis
						+ ".jpg";
				File file = new File(path + "/" + fileName);
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
				Image srcImg = ImageIO.read(entity.getContent());
				int w = srcImg.getWidth(null);
				int h = srcImg.getHeight(null);
				BufferedImage buffImg = new BufferedImage(
						srcImg.getWidth(null), srcImg.getHeight(null),
						BufferedImage.TYPE_INT_RGB);
				Graphics2D g = buffImg.createGraphics();
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g.drawImage(srcImg.getScaledInstance(w, h, Image.SCALE_SMOOTH),
						0, 0, null);
				Image img = ImageIO
						.read(FetchPic.class.getResourceAsStream("logo.png"));
				// ImageIcon imgIcon = new ImageIcon(
				// "D:/data/workspace/pri/java/joke_admin/WebContent/logo.png");
				// Image img = imgIcon.getImage();
				int _w = img.getWidth(null);
				int _h = img.getHeight(null);
				g.setComposite(AlphaComposite.getInstance(
						AlphaComposite.SRC_ATOP, 1f));
				int __h = _h > (int) (0.2 * h) ? (int) (0.2 * h) : _h;
				int __w = (int) ((double) __h / _h * _w);
				g.drawImage(
						img.getScaledInstance(__w, __h, Image.SCALE_SMOOTH), w
								- __w - 10, h - __h - 10, null);
				g.setComposite(AlphaComposite
						.getInstance(AlphaComposite.SRC_OVER));
				g.dispose();
				fout = new FileOutputStream(file);
				ImageIO.write(buffImg, "JPG", fout);
				sum++;
				jdbcTemplate
						.update("update joke_article set pic = ?, status = ? where id = ?",
								fileName, 1, id);
				jdbcTemplate
						.update("insert into joke_upload_pic(article_id, pic) values(?,?)",
								id, fileName);
			}catch(SocketTimeoutException ste){
				log.error(url);
			}catch (Exception e) {
				log.error(e, e);
			} finally {
				EntityUtils.consumeQuietly(entity);
				IOUtils.closeQuietly(fout);
			}
		}
		if(log.isInfoEnabled()){
			log.info("fetch pic:"+sum);
		}
	}
	public static void main(String[] args) {
		System.out.println(ClassLoader.getSystemResourceAsStream("logo.png"));
	}
}
