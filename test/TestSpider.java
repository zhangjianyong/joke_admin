import java.util.List;

import org.junit.Test;

import com.doumiao.joke.lang.Article;
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
}
