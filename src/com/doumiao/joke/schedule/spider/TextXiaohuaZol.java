package com.doumiao.joke.schedule.spider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.springframework.stereotype.Component;

import com.doumiao.joke.enums.ArticleType;
import com.doumiao.joke.schedule.Config;
import com.doumiao.joke.schedule.RandFetchMember;

@Component
public class TextXiaohuaZol {
	private static final Log log = LogFactory.getLog(TextXiaohuaZol.class);

	@Resource
	private DataSource dataSource;

	@Resource
	private RandFetchMember randFetchMember;

	//@Scheduled(fixedDelay = 60000)
	@Test
	public void fetch() {
		int maxPage = Config.getInt("fetch_pages_text_xiaohua",10);
		int count = maxPage;
		String site = "xiaohua.zol.com.cn";
		Connection con = null;
		PreparedStatement stmt_insert = null;
		PreparedStatement stmt_select = null;
		ResultSet rs = null;
		try {
			con = dataSource.getConnection();
			stmt_insert = con
					.prepareStatement("insert into joke_article(title, content, type, fetch_site, fetch_site_pid, member_id) values(?,?,?,?,?,?)");
			stmt_select = con
					.prepareStatement("select count(1) c from joke_article where fetch_site = ? and fetch_site_pid = ? and type = ? ");
			con.setAutoCommit(false);
			int sum = 0;
			for (int page = maxPage; page > maxPage - count; page--) {
				String url = "http://xiaohua.zol.com.cn/new/" + page + ".html";
				if (log.isDebugEnabled()) {
					log.debug("fetching " + url);
				}
				try {
					Document listDoc = Jsoup.connect(url).get();
					Elements es = listDoc
							.select("li.article-summary span.article-title a");
					for (int i = 0; i < es.size(); i++) {
						Element e = es.get(i);
						String uri = e.attr("href");
						Pattern pa = Pattern.compile("(?:.*)/(\\d*).html");
						Matcher m = pa.matcher(uri);
						if (m.find()) {
							String id = m.group(1);
							stmt_select.setString(1, site);
							stmt_select.setString(2, id);
							stmt_select.setString(3, ArticleType.TEXT.name());
							rs = stmt_select.executeQuery();
							rs.next();
							if (rs.getInt("c") > 0) {
								continue;
							}
							sum++;
							Document single = Jsoup.connect(
									"http://xiaohua.zol.com.cn/detail43/" + id
											+ ".html").get();
							Element titleE = single.select("h1.article-title")
									.last();
							Element content = single.select("div.article-text")
									.first();
							String title = titleE.text();

							String text = content.text();
							int col = 0;
							stmt_insert.setString(++col, title);
							stmt_insert.setString(++col, text);
							stmt_insert.setString(++col,
									ArticleType.TEXT.name());
							stmt_insert.setString(++col, site);
							stmt_insert.setString(++col, id);
							stmt_insert.setInt(++col, randFetchMember.next());
							stmt_insert.addBatch();
						}
					}
					stmt_insert.executeBatch();
					con.commit();
				} catch (Exception e) {
					log.error(url);
				}
			}
			if(log.isInfoEnabled()){
				log.info("fetch new article:"+sum);
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
}
