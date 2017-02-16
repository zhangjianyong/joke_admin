package com.doumiao.joke.schedule;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ThirdPlatAccountBlacklistd {
	private static final Log log = LogFactory.getLog(ThirdPlatAccountBlacklistd.class);
	@Resource
	private JdbcTemplate jdbcTemplate;

	@Scheduled(cron = "0 10 0 * * ?")
	protected void blacklist() {
		if (log.isInfoEnabled()) {
			log.info("blacklist");
		}
		// 对应10个以上用户的提现账号
		List<String> accounts = jdbcTemplate.queryForList(
				"SELECT account FROM `uc_thirdplat_account` GROUP BY account HAVING COUNT(id)>=5", String.class);
		for (String account : accounts) {
			// 提现账号对应的未冻结的用户
			List<Integer> mids = jdbcTemplate.queryForList(
					"SELECT member_id FROM uc_thirdplat_account a WHERE a.account = ? AND NOT EXISTS (SELECT * FROM uc_member u WHERE u.status = 1 AND a.member_id = u.id)",
					Integer.class, account);
			String sql = "SELECT count(*) FROM uc_account_log WHERE wealth_type = 'DRAW' AND account = 'S3' "
					+ "AND create_time BETWEEN  DATE_ADD(CURDATE(), INTERVAL -7 DAY) AND CURDATE() "
					+ "AND member_id IN(" + StringUtils.join(mids, ",") + ")";
			// 以上用户在过去一周内总的抽奖次数
			int count = jdbcTemplate.queryForInt(sql);
			int average = count / 7;
			if (average > 5) {
				log.warn("account " + account + " draw " + average + " per day, user count " + mids.size());
				for (Integer mid : mids) {
					jdbcTemplate.update("update uc_member set status = 1, remark = '超多账号登录，账号冻结' where id = ?", mid);
				}
			}
		}
	}
}
