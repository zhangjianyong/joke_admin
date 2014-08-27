package com.doumiao.joke.schedule.spider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.doumiao.joke.enums.ArticleType;
import com.doumiao.joke.lang.HttpClientHelper;
import com.doumiao.joke.schedule.Config;
import com.doumiao.joke.schedule.RandFetchMember;

@Component
public class AshamedQiushibaike {
	private static final Log log = LogFactory.getLog(AshamedQiushibaike.class);

	@Resource
	private DataSource dataSource;

	@Resource
	private RandFetchMember randFetchMember;

	@Scheduled(fixedDelay = 240000)
	@Test
	public void fetch() {
		int maxPage = Integer.parseInt(Config.get("fetch_pages_qiushi_qiushibaike"));
		int count = maxPage;
		String site = "qiushibaike.com";
		Connection con = null;
		PreparedStatement stmt_insert = null;
		PreparedStatement stmt_select = null;
		ResultSet rs = null;
		try {
			con = dataSource.getConnection();
			stmt_insert = con
					.prepareStatement("insert into joke_article(title, content, type, fetch_site, fetch_site_pid, member_id, member_nick ) values(?,?,?,?,?,?,?)");
			stmt_select = con
					.prepareStatement("select count(1) c from joke_article where fetch_site = ? and fetch_site_pid = ? and type = ? ");
			con.setAutoCommit(false);
			for (int page = maxPage; page > maxPage - count; page--) {
				String url = "http://www.qiushibaike.com/late/page/" + page;
				if (log.isDebugEnabled()) {
					log.debug("fetching " + url);
				}
				HttpClient client = HttpClientHelper.getClient();
				HttpGet get = new HttpGet(url);
				try {
					HttpResponse response = client.execute(get);
					HttpEntity entity = response.getEntity();

					// Document listDoc = Jsoup.connect(url).get();
					Document listDoc = Jsoup
							.parse(EntityUtils.toString(entity));
					Elements es = listDoc
							.select("div.article.block.untagged.mb15");
					for (int i = 0; i < es.size(); i++) {
						Element e = es.get(i);
						String id = e.attr("id").replace("qiushi_tag_", "");
						Element contentE = e.select("div.content").first();
						Element img = e.select("div.thumb").first();
						String content = contentE.text();
						if (img != null && content.length() < 100) {
							continue;
						}
						stmt_select.setString(1, site);
						stmt_select.setString(2, id);
						stmt_select.setString(3, ArticleType.ASHAMED.name());
						rs = stmt_select.executeQuery();
						rs.next();
						if (rs.getInt("c") > 0) {
							continue;
						}
						int col = 0;
						Map<String, Object> me = randFetchMember.next();
						stmt_insert.setString(++col, null);
						stmt_insert.setString(++col, content);
						stmt_insert
								.setString(++col, ArticleType.ASHAMED.name());
						stmt_insert.setString(++col, site);
						stmt_insert.setString(++col, id);
						stmt_insert.setInt(++col, (Integer) me.get("id"));
						stmt_insert.setString(++col, (String) me.get("nick"));
						stmt_insert.addBatch();
					}
					stmt_insert.executeBatch();
					con.commit();
				} catch (Exception e) {
					log.error(url);
				}
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
