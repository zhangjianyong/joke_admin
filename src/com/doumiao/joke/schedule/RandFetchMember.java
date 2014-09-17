package com.doumiao.joke.schedule;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections.ListUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class RandFetchMember {
	@Resource
	private JdbcTemplate jdbcTemplate;

	@SuppressWarnings("unchecked")
	private static List<Integer> members = ListUtils.EMPTY_LIST;
	private long count = 10000;

	public int next() {
		if (members == null || members.isEmpty()) {
			synchronized (members) {
				if (members == null || members.isEmpty()) {
					members = jdbcTemplate.queryForList(
							"select id from uc_member limit ?", Integer.class,count);
				}
			}
		}
		return members.get((int)(Math.random()*count));
	}
}
