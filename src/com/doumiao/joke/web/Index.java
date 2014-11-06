package com.doumiao.joke.web;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class Index {
	@Resource
	private JdbcTemplate jdbcTemplate;
	@Resource
	private ObjectMapper objectMapper;

	@RequestMapping("/index")
	public String index(HttpServletRequest request, HttpServletResponse response){
		Object admin = request.getSession().getAttribute("joke_admin");
		if(admin==null){
			//return "redirect:/login";
		}
		return "/index";
	}
}
