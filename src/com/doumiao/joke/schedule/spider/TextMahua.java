package com.doumiao.joke.schedule.spider;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.doumiao.joke.lang.Article;
import com.doumiao.joke.lang.HttpClientHelper;
import com.doumiao.joke.schedule.Config;
import com.doumiao.joke.service.ArticleService;

@Component
public class TextMahua {
	private static final Log log = LogFactory.getLog(TextMahua.class);

	String site = "mahua.com";
	@Resource
	private ArticleService articleService;

	@Scheduled(fixedDelay = 180000)
	public void fetch() {
		int maxPage = 10;
		int count = Config.getInt("fetch_pages_text_mahua", 10);

		try {
			int fetch = 0;
			for (int page = maxPage; page > maxPage - count; page--) {
				String url = "http://www.mahua.com/newjokes/text/index_" + page
						+ ".htm";
				if (log.isDebugEnabled()) {
					log.debug("fetching " + url);
				}
				try {
					List<Article> articles = fetch(url);
					fetch += articles.size();
					if (log.isDebugEnabled()) {
						log.debug(url + ":" + articles.size());
					}
					articleService.insertTextArticles(articles);
				} catch (SocketTimeoutException ste) {
					log.error(url);
					log.error(ste.getMessage());
				} catch (Exception e) {
					log.error(url);
					log.error(e, e);
				}
			}
			if (log.isInfoEnabled()) {
				log.info("fetch:" + fetch);
			}
		} catch (Exception e) {
			log.error(e, e);
		}
	}

	public List<Article> fetch(String url) throws Exception {
		HttpClient client = HttpClientHelper.getClient();
		HttpGet get = new HttpGet(url);
		HttpResponse response = client.execute(get);
		Document listDoc = Jsoup.parse(EntityUtils.toString(
				response.getEntity(), "utf-8"));
		Elements es = listDoc.select("dl.mahua");
		List<Article> l = new ArrayList<Article>(es.size());
		for (int i = 0; i < es.size(); i++) {
			Article a = new Article();
			a.setFetchSite(site);
			a.setStatus(0);

			Element e = es.get(i);
			String id = e.attr("mahua");
			Element titleE = e.select("dt span.joke-title a").first();
			Element content = e.select("dd.content").first();
			String title = titleE.text();
			String text = content.text();
			if (StringUtils.isBlank(text)) {
				continue;
			}
			a.setTitle(title);
			a.setFetchSitePid(id);
			a.setContent(text);
			l.add(a);
		}
		return l;
	}
}
