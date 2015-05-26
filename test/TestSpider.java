import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import com.doumiao.joke.lang.Article;
import com.doumiao.joke.lang.HttpClientHelper;
import com.doumiao.joke.schedule.spider.AshamedQiushibaike;
import com.doumiao.joke.schedule.spider.Pic0824;
import com.doumiao.joke.schedule.spider.PicJuyouqu;
import com.doumiao.joke.schedule.spider.PicMahua;
import com.doumiao.joke.schedule.spider.TextBudejie;
import com.doumiao.joke.schedule.spider.TextMahua;
import com.doumiao.joke.schedule.spider.TextXiaohuaZol;

public class TestSpider {

	@Test
	public void testPicJuyouqu() throws Exception {
		PicJuyouqu spider = new PicJuyouqu();
		List<Article> articles = spider.fetch("http://www.juyouqu.com/page/1");
		for (Article article : articles) {
			System.out.println(article.toString());
		}
	}
	
	@Test
	public void testPic0824() throws Exception {
		Pic0824 spider = new Pic0824();
		List<Article> articles = spider.fetch("http://www.0824.com/egao_1/");
		for (Article article : articles) {
			System.out.println(article.toString());
		}
	}
	
	@Test
	public void testPicMahua() throws Exception {
		PicMahua spider = new PicMahua();
		List<Article> articles = spider.fetch("http://www.mahua.com/newjokes/pic/index_1.htm");
		for (Article article : articles) {
			System.out.println(article.toString());
		}
	}
	
	@Test
	public void testTextBudejie() throws Exception {
		TextBudejie spider = new TextBudejie();
		List<Article> articles = spider.fetch("http://www.budejie.com/new-d/");
		for (Article article : articles) {
			System.out.println(article.toString());
		}
	}
	
	@Test
	public void testTextMahua() throws Exception {
		TextMahua spider = new TextMahua();
		List<Article> articles = spider.fetch("http://www.mahua.com/newjokes/text/index_1.htm");
		for (Article article : articles) {
			System.out.println(article.toString());
		}
	}
	
	@Test
	public void testTextXiaohuaZol() throws Exception {
		TextXiaohuaZol spider = new TextXiaohuaZol();
		List<Article> articles = spider.fetch("http://xiaohua.zol.com.cn/new/1.html");
		for (Article article : articles) {
			System.out.println(article.toString());
		}
	}
	
	@Test
	public void testAshamedQiushibaike() throws Exception {
		AshamedQiushibaike spider = new AshamedQiushibaike();
		List<Article> articles = spider.fetch("http://www.qiushibaike.com/late/page/1");
		for (Article article : articles) {
			System.out.println(article.toString());
		}
	}
	@Test
	public void testAlima() throws Exception {
		HttpClient client = HttpClientHelper.getClient();
		HttpGet get = new HttpGet("http://ai.taobao.com/auction/edetail.htm?e=D4VOKgmnuya6k0Or%2B%2BH4tF8JomzViWxGnnDOe2U0TNmLltG5xFicOcyvbMN%2FjluyDPIwxrc30riHmuQ7qhehfguw8cKeRvyhtuL%2BTYxC3iphnBGBH8ep6lFE%2BBP3Qa7QUa6whSeFsYBt2myTO7A4Ns6l7saVk%2BlHzz9qK%2BVhXaI%3D&ptype=100010&unid=150i2060eTBKGWzc10F100000&from=basic&clk1=3d6c63af274c09d4e8239df0df52bf84&upsid=3d6c63af274c09d4e8239df0df52bf84");
		Document listDoc = null;
		try {
			HttpResponse response = client.execute(get);
			listDoc = Jsoup.parse(EntityUtils.toString(response.getEntity(),
					"utf-8"));
			System.out.println(listDoc);
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			get.releaseConnection();
		}
	}
}
