package com.doumiao.joke.schedule.spider;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.doumiao.joke.enums.ArticleType;
import com.doumiao.joke.lang.Article;
import com.doumiao.joke.schedule.Config;
import com.doumiao.joke.schedule.RandFetchMember;

@Service
public class Picmahua {
	private static final Log log = LogFactory.getLog(Picmahua.class);

	@Resource
	private DataSource dataSource;

	@Resource
	private RandFetchMember randFetchMember;

	@Scheduled(fixedDelay = 60000)
	@Test
	public void fetch() {
		int maxPage = 10;
		int count = Config.getInt("fetch_pages_pic_mahua", 10);
		String site = "mahua.com";

		Connection con = null;
		PreparedStatement stmt_insert = null;
		PreparedStatement stmt_select = null;
		ResultSet rs = null;
		String url = null;
		try {
			con = dataSource.getConnection();
			stmt_insert = con
					.prepareStatement("insert into joke_article(title, pic_ori, type, fetch_site, fetch_site_pid, `status`, member_id ) values(?,?,?,?,?,0,?)");
			stmt_select = con
					.prepareStatement("select count(1) c from joke_article where fetch_site = ? and fetch_site_pid = ? and type = ? ");
			con.setAutoCommit(false);
			int sum = 0;
			for (int page = maxPage; page > maxPage - count; page--) {
				url = "http://www.mahua.com/newjokes/pic/index_" + page
						+ ".htm";
				if (log.isDebugEnabled()) {
					log.debug("fetching " + url);
				}
				try {
					Document listDoc = Jsoup.connect(url).get();
					Elements es = listDoc.select("div.mahua");
					for (int i = 0; i < es.size(); i++) {
						Element e = es.get(i);
						String id = e.attr("id").replace("j_", "");
						Element titleE = e.select("h3 a").first();
						Element imgE = e.select("div.content p img").first();
						if (imgE == null) {
							continue;
						}
						String title = titleE.text();
						String content = imgE.attr("src");
						stmt_select.setString(1, site);
						stmt_select.setString(2, id);
						stmt_select.setString(3, ArticleType.PIC.name());
						rs = stmt_select.executeQuery();
						rs.next();
						if (rs.getInt("c") > 0) {
							continue;
						}
						sum++;
						int col = 0;
						stmt_insert.setString(++col, title);
						stmt_insert.setString(++col, content);

						stmt_insert.setString(++col, ArticleType.PIC.name());
						stmt_insert.setString(++col, site);
						stmt_insert.setString(++col, id);
						stmt_insert.setInt(++col, randFetchMember.next());
						stmt_insert.addBatch();
					}
					stmt_insert.executeBatch();
					con.commit();
				} catch (Exception e) {
					log.error(e, e);
					log.error(url);
				}
			}
			if (log.isInfoEnabled()) {
				log.info("fetch new article:" + sum);
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

	@Test
	private List<Article> fetch(String url) {
		Document listDoc;
		try {
			listDoc = Jsoup.connect(url).get();
		} catch (IOException e) {
			log.error(e,e);
			return null;
		}
		Elements es = listDoc.select("div.mahua");
		List<Article> l = new ArrayList<Article>(es.size());
		for (int i = 0; i < es.size(); i++) {
			Article a = new Article();
			Element e = es.get(i);
			String id = e.attr("id").replace("j_", "");
			Element titleE = e.select("h3 a").first();
			Element imgE = e.select("div.content p img").first();
			if (imgE == null) {
				continue;
			}
			String title = titleE.text();
			String content = imgE.attr("src");
			l.add(a);
		}
		return l;
	}
}
