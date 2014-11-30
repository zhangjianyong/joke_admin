package com.doumiao.joke.schedule.spider;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.doumiao.joke.enums.ArticleType;
import com.doumiao.joke.lang.Article;
import com.doumiao.joke.schedule.Config;
import com.doumiao.joke.schedule.RandFetchMember;

@Component
public class TextBudejie {
	private static final Log log = LogFactory.getLog(TextBudejie.class);

	@Resource
	private DataSource dataSource;

	@Resource
	private RandFetchMember randFetchMember;

	@Scheduled(fixedDelay = 180000)
	public void fetch() {
		int maxPage = Config.getInt("fetch_pages_text_budejie", 10);
		int count = maxPage;
		String site = "budejie.com/";
		Connection con = null;
		PreparedStatement stmt_insert = null;
		PreparedStatement stmt_select = null;
		ResultSet rs = null;
		try {
			con = dataSource.getConnection();
			stmt_insert = con
					.prepareStatement("insert into joke_article(content, type, fetch_site, fetch_site_pid, member_id) values(?,?,?,?,?)");
			stmt_select = con
					.prepareStatement("select count(1) c from joke_article where fetch_site = ? and fetch_site_pid = ? and type = ? ");
			con.setAutoCommit(false);
			int fetch = 0, insert = 0, error = 0;
			for (int page = maxPage; page > maxPage - count; page--) {
				String url = "http://www.budejie.com/new-d/" + page;
				try {
					List<Article> articles = fetch(url);
					if (log.isDebugEnabled()) {
						log.debug(url + ":" + articles.size());
					}
					for (Article a : articles) {
						stmt_select.setString(1, site);
						stmt_select.setString(2, a.getFetchSitePid());
						stmt_select.setString(3, ArticleType.TEXT.name());
						rs = stmt_select.executeQuery();
						rs.next();
						if (rs.getInt("c") > 0) {
							continue;
						}
						insert++;
						int col = 0;
						stmt_insert.setString(++col, a.getContent());

						stmt_insert.setString(++col, ArticleType.TEXT.name());
						stmt_insert.setString(++col, site);
						stmt_insert.setString(++col, a.getFetchSitePid());
						stmt_insert.setInt(++col, randFetchMember.next());
						stmt_insert.addBatch();
					}
					stmt_insert.executeBatch();
					con.commit();
				} catch (SocketTimeoutException ste) {
					error++;
					log.error(url);
					log.error(ste.getMessage());
				} catch (Exception e) {
					error++;
					log.error(url);
					log.error(e, e);
				}
			}
			if (log.isInfoEnabled()) {
				log.info("fetch:" + fetch + " insert:" + insert + " error:"
						+ error);
			}
		} catch (Exception e) {
			log.error(e, e);
		} finally {
			JdbcUtils.closeConnection(con);
			JdbcUtils.closeResultSet(rs);
			JdbcUtils.closeStatement(stmt_insert);
			JdbcUtils.closeStatement(stmt_select);
		}
	}

	@SuppressWarnings("unchecked")
	public List<Article> fetch(String url) throws Exception {
		Document listDoc;
		try {
			listDoc = Jsoup.connect(url).get();
		} catch (IOException e) {
			log.error(e, e);
			return ListUtils.EMPTY_LIST;
		}
		Elements es = listDoc
				.select("div.web_left.floatl.test");
		List<Article> l = new ArrayList<Article>(es.size());
		for (int i = 0; i < es.size(); i++) {
			Article a = new Article();
			Element e = es.get(i);
			String id = e.attr("id").replace("title-", "");
			String content = e.text();
			if (StringUtils.isBlank(content)) {
				continue;
			}
			a.setFetchSitePid(id);
			a.setContent(content);
			l.add(a);
		}
		return l;
	}
}
