package com.doumiao.joke.schedule.spider;

import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
public class PicJuyouqu {
	private static final Log log = LogFactory.getLog(PicJuyouqu.class);

	@Resource
	private DataSource dataSource;

	@Resource
	private RandFetchMember randFetchMember;

	@Scheduled(fixedDelay = 180000)
	@Test
	public void fetch() {
		int maxPage = Config.getInt("fetch_pages_pic_juyouqu", 10);
		int count = maxPage;
		String site = "juyouqu.com";
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
			int fetch = 0, insert = 0, error = 0;
			for (int page = maxPage; page > maxPage - count; page--) {
				url = "http://www.juyouqu.com/page/" + page;
				if (log.isDebugEnabled()) {
					log.debug("fetching " + url);
				}
				try {
					Document listDoc = Jsoup.connect(url).get();
					Elements es = listDoc.select("div.article.page1");
					for (int i = 0; i < es.size(); i++) {
						Element e = es.get(i);
						Element titleE = e.select(
								"div.title div.itemTitle h2 a").first();
						Element imgE = e
								.select("div.postContainer div.animatedContainerStatic a img")
								.first();
						if (imgE == null) {
							continue;
						}
						String title = titleE.text();
						String content = imgE.attr("src");
						String id = titleE.attr("href").replace("/qu/", "");
						
						fetch++;
						
						stmt_select.setString(1, site);
						stmt_select.setString(2, id);
						stmt_select.setString(3, ArticleType.PIC.name());
						rs = stmt_select.executeQuery();
						rs.next();
						if (rs.getInt("c") > 0) {
							continue;
						}
						insert++;
						int col = 0;
						stmt_insert.setString(++col, title);
						if (content.endsWith(".gifstatic")) {
							stmt_insert.setString(++col,
									content.replace(".gifstatic", ".gif"));
						} else if (content.endsWith("!w500")) {
							stmt_insert.setString(++col,
									content.replace("!w500", ""));
						} else {
							stmt_insert.setString(++col, content);
						}

						stmt_insert.setString(++col, ArticleType.PIC.name());
						stmt_insert.setString(++col, site);
						stmt_insert.setString(++col, id);
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
}
