package com.doumiao.joke.service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.doumiao.joke.enums.ArticleType;
import com.doumiao.joke.lang.Article;
import com.doumiao.joke.schedule.RandFetchMember;

@Service
public class ArticleService {

	@Resource
	private JdbcTemplate jdbcTemplate;

	@Resource
	private RandFetchMember randFetchMember;

	/**
	 * 批量录入图片笑话
	 * 
	 * @param articles
	 * @throws Exception
	 */
	@Transactional(timeout = 1000, rollbackForClassName = { "RuntimeException",
			"Exception" }, propagation = Propagation.REQUIRED)
	public synchronized int[] insertPicArticles(final List<Article> articles)
			throws Exception {
		String sql = "insert ignore into joke_article(title, pic_ori, type, fetch_site, fetch_site_pid, `status`, member_id ) values(?,?,?,?,?,?,?)";
		BatchPreparedStatementSetter setter = new BatchPreparedStatementSetter() {
			public void setValues(PreparedStatement ps, int i)
					throws SQLException {
				Article article = (Article) articles.get(i);
				ps.setString(1, article.getTitle());
				ps.setString(2, article.getPicOri());
				ps.setString(3, ArticleType.PIC.name());
				ps.setString(4, article.getFetchSite());
				ps.setString(5, article.getFetchSitePid());
				ps.setInt(6, article.getStatus());
				ps.setInt(7,
						article.getMemberId() <= 0 ? randFetchMember.next()
								: article.getMemberId());
			}

			public int getBatchSize() {
				return articles.size();
			}
		};
		return jdbcTemplate.batchUpdate(sql, setter);
	}

	/**
	 * 批量录入文本笑话
	 * 
	 * @param articles
	 * @throws Exception
	 */
	@Transactional(timeout = 1000, rollbackForClassName = { "RuntimeException",
			"Exception" }, propagation = Propagation.REQUIRED)
	public synchronized int[] insertTextArticles(final List<Article> articles)
			throws Exception {
		String sql = "insert ignore into joke_article(content, type, fetch_site, fetch_site_pid, `status`, member_id) values(?,?,?,?,?,?)";
		BatchPreparedStatementSetter setter = new BatchPreparedStatementSetter() {
			public void setValues(PreparedStatement ps, int i)
					throws SQLException {
				Article article = (Article) articles.get(i);
				ps.setString(1, article.getContent());
				ps.setString(2, ArticleType.TEXT.name());
				ps.setString(3, article.getFetchSite());
				ps.setString(4, article.getFetchSitePid());
				ps.setInt(5, article.getStatus());
				ps.setInt(6,
						article.getMemberId() <= 0 ? randFetchMember.next()
								: article.getMemberId());
			}

			public int getBatchSize() {
				return articles.size();
			}
		};
		return jdbcTemplate.batchUpdate(sql, setter);
	}

	/**
	 * 批量录入糗事笑话
	 * 
	 * @param articles
	 * @throws Exception
	 */
	@Transactional(timeout = 1000, rollbackForClassName = { "RuntimeException",
			"Exception" }, propagation = Propagation.REQUIRED)
	public synchronized int[] insertAshamedArticles(final List<Article> articles)
			throws Exception {
		String sql = "insert ignore into joke_article(title, content, type, fetch_site, fetch_site_pid, `status`, member_id ) values(?,?,?,?,?,?,?)";
		BatchPreparedStatementSetter setter = new BatchPreparedStatementSetter() {
			public void setValues(PreparedStatement ps, int i)
					throws SQLException {
				Article article = (Article) articles.get(i);
				ps.setString(1, article.getTitle());
				ps.setString(2, article.getContent());
				ps.setString(3, ArticleType.ASHAMED.name());
				ps.setString(4, article.getFetchSite());
				ps.setString(5, article.getFetchSitePid());
				ps.setInt(6, article.getStatus());
				ps.setInt(7,
						article.getMemberId() <= 0 ? randFetchMember.next()
								: article.getMemberId());
			}

			public int getBatchSize() {
				return articles.size();
			}
		};
		return jdbcTemplate.batchUpdate(sql, setter);
	}
}
