package com.doumiao.joke.schedule;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

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
		List<String> accounts = jdbcTemplate.queryForList("SELECT account FROM `uc_thirdplat_account` GROUP BY account HAVING COUNT(id)>=10", String.class);
		for (String account : accounts) {
			List<Integer> mids = jdbcTemplate.queryForList("SELECT member_id FROM uc_thirdplat_account a WHERE a.account = ? AND NOT EXISTS (SELECT * FROM uc_member u WHERE u.status = 1 AND a.member_id = u.id)", Integer.class, account);
			for(Integer mid : mids){
				jdbcTemplate.update("update uc_member set status = 1, remark = '超多账号登录，账号冻结' where id = ?", mid);
			}
		}
	}
}
