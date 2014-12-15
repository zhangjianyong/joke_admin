package com.doumiao.joke.schedule.spider;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

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
public class AshamedQiushibaike {
	private static final Log log = LogFactory.getLog(AshamedQiushibaike.class);

	String site = "qiushibaike.com";
	@Resource
	private ArticleService articleService;

	@Scheduled(fixedDelay = 180000)
	public void fetch() {
		int maxPage = Config.getInt("fetch_pages_qiushi_qiushibaike", 10);
		int count = maxPage;
		try {
			int fetch = 0;
			for (int page = maxPage; page > maxPage - count; page--) {
				String url = "http://www.qiushibaike.com/late/page/" + page;
				if (log.isDebugEnabled()) {
					log.debug("fetching " + url);
				}
				try {
					List<Article> articles = fetch(url);
					fetch += articles.size();
					if (log.isDebugEnabled()) {
						log.debug(url + ":" + articles.size());
					}
					articleService.insertAshamedArticles(articles);
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
		Document listDoc = null;
		try {
			HttpResponse response = client.execute(get);
			listDoc = Jsoup.parse(EntityUtils.toString(response.getEntity(),
					"utf-8"));
		} catch (Exception e) {
			log.error(e, e);
		} finally {
			get.releaseConnection();
		}
		Elements es = listDoc.select("div.article.block.untagged.mb15");
		List<Article> l = new ArrayList<Article>(es.size());
		for (int i = 0; i < es.size(); i++) {
			Article a = new Article();
			a.setFetchSite(site);
			a.setStatus(2);
			Element e = es.get(i);
			String id = e.attr("id").replace("qiushi_tag_", "");
			Element contentE = e.select("div.content").first();
			Element img = e.select("div.thumb").first();
			String content = contentE.text();
			if (img != null && content.length() < 100) {
				continue;
			}
			a.setFetchSitePid(id);
			a.setContent(content);
			l.add(a);
		}
		return l;
	}
}
