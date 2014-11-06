package com.doumiao.joke.web;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class Login {
	@Resource
	private JdbcTemplate jdbcTemplate;
	@Resource
	private ObjectMapper objectMapper;

	@RequestMapping("/login")
	public String login(HttpServletRequest request, HttpServletResponse response){
		return "login";
	}
	
	@RequestMapping("/i/login")
	public String iLogin(HttpServletRequest request, HttpServletResponse response){
		return "login";
	}
}
