package com.doumiao.joke.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class Admin {
	@Resource
	private JdbcTemplate jdbcTemplate;
	@Resource
	private ObjectMapper objectMapper;

	@ResponseBody
	@RequestMapping("/function.action")
	public List<Map<String,Object>> tasks(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "uid", required = false, defaultValue = "0") int uid){
		List<Integer> ridL =  jdbcTemplate.queryForList("select rid from admin_user_role where uid = ?",Integer.class,uid);
		String ridS = StringUtils.join(ridL,",");
		List<Integer> fidL =  jdbcTemplate.queryForList("select fid from admin_role_function where rid in (?)",Integer.class,ridS);
		String fidS = StringUtils.join(fidL,",");
		List<Map<String,Object>> fs =  jdbcTemplate.queryForList("select id,pid,`key`,leaf,`name` from admin_function where id in (?)",fidS);
		Map<String,Map<String,Object>> parents = new HashMap<String,Map<String,Object>>();
		List<Map<String,Object>> tree = new ArrayList<Map<String,Object>>();
		for (Map<String, Object> f : fs) {
			String pid = String.valueOf(f.get("pid"));
			if(!parents.containsKey(pid)){
				Map<String,Object> p = new HashMap<String,Object>();
				List<Map<String,Object>> children = new ArrayList<Map<String,Object>>();
				String pName =  jdbcTemplate.queryForObject("select `name` from admin_function where id = ?",String.class,pid);
				p.put("text", pName);
				p.put("expanded", true);
				p.put("children",children);
				parents.put(pid, p);
				tree.add(p);
			}
			@SuppressWarnings("unchecked")
			List<Map<String,Object>> children = (List<Map<String,Object>>)(parents.get(pid).get("children"));
			Map<String,Object> child = new HashMap<String,Object>();
			child.put("id", f.get("key"));
			child.put("text", f.get("name"));
			child.put("leaf", true);
			children.add(child);
		}
		return tree;
	}
}
