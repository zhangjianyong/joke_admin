package com.doumiao.joke.schedule.spider;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class TextXiaohuaZol {
	private static final Log log = LogFactory.getLog(TextXiaohuaZol.class);

	@Resource
	private ArticleService articleService;
	String site = "xiaohua.zol.com.cn";

	@Scheduled(fixedDelay = 180000)
	public void fetch() {
		int maxPage = Config.getInt("fetch_pages_text_xiaohua", 10);
		int count = maxPage;
		try {
			int fetch = 0;
			for (int page = maxPage; page > maxPage - count; page--) {
				String url = "http://xiaohua.zol.com.cn/new/" + page + ".html";
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
				} catch (Exception e) {
					log.error(url);
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
		Elements es = listDoc.select("li.article-summary");
		List<Article> l = new ArrayList<Article>(es.size());
		for (int i = 0; i < es.size(); i++) {
			Article a = new Article();
			a.setFetchSite(site);
			a.setStatus(2);
			
			Element e = es.get(i);
			Element idE = e.select("div.article-commentbar.articleCommentbar.clearfix").first();
			String id = idE.attr("data-id");
			Element titleE = e.select("span.article-title a").first();
			Element contentE = e.select("div.summary-text").first();
			
			
			String title = titleE.text();

			String content = contentE.text();
			if (StringUtils.isBlank(content)) {
				continue;
			}
			a.setTitle(title);
			a.setFetchSitePid(id);
			a.setContent(content);
			l.add(a);
		}
		return l;
	}
}
