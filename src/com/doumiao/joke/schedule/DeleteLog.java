package com.doumiao.joke.schedule;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DeleteLog {
	private static final Log log = LogFactory.getLog(DeleteLog.class);

	@Resource
	private JdbcTemplate jdbcTemplate;

	@Scheduled(cron = "0 1 0 * * ?")
	public void deleteIdentifyCode() {
		int count = 0;
		try {
			count = jdbcTemplate.update("DELETE FROM `uc_identify_code` WHERE create_time < CURDATE()");
		} catch (Exception e) {
			log.error(e, e);
		}
		if (log.isInfoEnabled()) {
			log.info("delete uc_identify_code count:" + count);
		}
	}
	
	@Scheduled(cron = "0 10 0 * * ?")
	public void deleteUcAccountLog() {
		int count = 0;
		try {
			count = jdbcTemplate.update("DELETE FROM `uc_account_log` WHERE account = 'S1' AND create_time < CURDATE()");
		} catch (Exception e) {
			log.error(e, e);
		}
		if (log.isInfoEnabled()) {
			log.info("delete uc_account_log count:" + count);
		}
	}
}
