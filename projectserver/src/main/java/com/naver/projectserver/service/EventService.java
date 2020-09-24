package com.naver.projectserver.service;

import java.util.List;

import com.naver.projectserver.mapper.Event;

public interface EventService {
	boolean insert(Event event);
	boolean update(Event event);
	boolean delete(Event event);
	Event selectOne(String id);
	List<Event> selectAll(String owner);
}
