import java.util.List;

import org.junit.Test;

import com.doumiao.joke.lang.Article;
import com.doumiao.joke.schedule.spider.TextBudejie;

public class TestSpider {
	@Test
	public void testTextBudejie() throws Exception {
		TextBudejie budejie = new TextBudejie();
		List<Article> articles = budejie.fetch("http://www.budejie.com/new-d/");
		for (Article article : articles) {
			System.out.println(article.getContent());
		}
	}
}
