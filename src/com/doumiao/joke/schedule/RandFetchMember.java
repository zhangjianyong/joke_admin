package com.doumiao.joke.schedule;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.ListUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class RandFetchMember {
	@Resource
	private JdbcTemplate jdbcTemplate;

	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> members = ListUtils.EMPTY_LIST;
	private long count = 10000;

	public Map<String,Object> next() {
		if (members == null || members.isEmpty()) {
			synchronized (members) {
				if (members == null || members.isEmpty()) {
					members = jdbcTemplate.queryForList(
							"select id, nick from uc_member limit ?", count);
				}
			}
		}
		return members.get((int)(Math.random()*count));
	}
}
