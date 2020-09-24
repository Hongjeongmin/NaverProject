package com.naver.projectserver.service;

import com.naver.projectserver.mapper.User;

public interface UserService {
	User login(String id);
	boolean signup(User user);
	public boolean update(User user);
	public User select(String id);
	public boolean updateNickname(User user);
}
