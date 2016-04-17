package com.doumiao.joke.schedule.spider;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.doumiao.joke.lang.Article;
import com.doumiao.joke.schedule.Config;
import com.doumiao.joke.service.ArticleService;

@Component
public class PicMahua {
	private static final Log log = LogFactory.getLog(PicMahua.class);

	@Resource
	private ArticleService articleService;
	private String site = "mahua.com";

	@Scheduled(fixedDelay = 180000)
	public void fetch() {
		int maxPage = 10;
		int count = Config.getInt("fetch_pages_pic_mahua", 10);
		String url = null;
		try {
			int fetch = 0;
			for (int page = maxPage; page > maxPage - count; page--) {
				url = "http://www.mahua.com/newjokes/pic/index_" + page
						+ ".htm";
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
		Document listDoc = Jsoup.connect(url).get();
		Elements es = listDoc.select("dl.mahua");
		List<Article> l = new ArrayList<Article>(es.size());
		for (int i = 0; i < es.size(); i++) {
			Article a = new Article();
			a.setFetchSite(site);
			a.setStatus(0);
			Element e = es.get(i);
			String id = e.attr("mahua");
			Element titleE = e.select("dt span.joke-title a").first();
			Element imgE = e.select("dd.content img").first();
			if (imgE == null) {
				continue;
			}
			String title = titleE.text();
			String picOri = imgE.attr("src");
			String picOri_lazy = imgE.attr("mahuaImg");
			if(StringUtils.isEmpty(picOri)){
				picOri = picOri_lazy;
			}
			if(StringUtils.isEmpty(picOri)){
				log.debug("pic is empty:"+ url + ",title:" + title);
			}
			if (log.isDebugEnabled()) {
				log.debug("url:" + url + ",title:" + title + ",picOri:"
						+ picOri);
			}
			a.setFetchSitePid(id);
			a.setTitle(title);
			a.setPicOri(picOri);
			l.add(a);
		}
		return l;
	}
}
