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
public class PicJuyouqu {
	private static final Log log = LogFactory.getLog(PicJuyouqu.class);

	@Resource
	private ArticleService articleService;

	String site = "juyouqu.com";

	@Scheduled(fixedDelay = 18000)
	public void fetch() {
		int maxPage = Config.getInt("fetch_pages_pic_juyouqu", 10);
		int count = maxPage;

		String url = null;
		try {
			int fetch = 0;
			for (int page = maxPage; page > maxPage - count; page--) {
				url = "http://www.juyouqu.com/page/" + page;
				if (log.isDebugEnabled()) {
					log.debug("fetching " + url);
				}
				try {
					List<Article> articles = fetch(url);
					fetch += articles.size();
					if (log.isDebugEnabled()) {
						log.debug(url + ":" + articles.size());
					}

					articleService.insertPicArticles(articles);
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
		Document listDoc = Jsoup.parse(EntityUtils.toString(response.getEntity(),
				"utf-8"));
		Elements es = listDoc.select("div.entryCollection div.article");
		List<Article> l = new ArrayList<Article>(es.size());
		for (int i = 0; i < es.size(); i++) {
			Article a = new Article();
			Element e = es.get(i);
			Element titleE = e.select("div.title div.itemTitle h2 a").first();
			Element imgE = e.select(
					"div.postContainer div.animatedContainerStatic a img")
					.first();
			if (imgE == null) {
				continue;
			}
			a.setFetchSite(site);
			a.setStatus(0);
			String title = titleE.text();
			a.setTitle(title);
			String img = imgE.attr("src");
			String temp_img = imgE.attr("data-src");
			img = StringUtils.defaultIfBlank(img, temp_img);
			if (img.endsWith(".gifstatic")) {
				a.setPicOri(img.replace(".gifstatic", ".gif"));
			} else if (img.endsWith("!w500")) {
				a.setPicOri(img.replace("!w500", ""));
			} else {
				a.setPicOri(img);
			}
			String id = titleE.attr("href").replace("/qu/", "");
			a.setFetchSitePid(id);
			l.add(a);
		}
		return l;
	}
}
