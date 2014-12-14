package com.doumiao.joke.schedule.spider;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class Pic0824 {
	private static final Log log = LogFactory.getLog(Pic0824.class);

	@Resource
	private ArticleService articleService;
	String site = "0824.com";

	@Scheduled(fixedDelay = 180000)
	public void fetch() {
		int maxPage = Config.getInt("fetch_pages_pic_0824", 10);
		int count = maxPage;
		String url = null;
		try {
			int fetch = 0;
			String[] cats = new String[] { "neihan", "qiushi", "egao" };
			for (String cat : cats) {
				for (int page = maxPage; page > maxPage - count; page--) {
					url = "http://www.0824.com/" + cat + "_" + page + "/";
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
		Elements es = listDoc.select("div.rt");
		List<Article> l = new ArrayList<Article>(es.size());
		Pattern p = Pattern.compile("/(\\d+).html");
		for (int i = 0; i < es.size(); i++) {
			Article a = new Article();
			a.setFetchSite(site);
			a.setStatus(0);
			Element e = es.get(i);
			Element titleE = e.select("div.titl a").first();
			Element imgE = e.select("div.desc a img").first();
			if (imgE == null) {
				continue;
			}
			String title = titleE.text();
			String picOri = imgE.attr("src");
			Matcher m = p.matcher(titleE.attr("href"));
			if (m.find()) {
				a.setFetchSitePid(m.group(1));
			} else {
				continue;
			}
			a.setTitle(title);
			a.setPicOri(picOri);
			l.add(a);
		}
		return l;
	}
}
