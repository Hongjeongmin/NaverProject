package com.naver.projectserver.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.naver.projectserver.mapper.Event;
import com.naver.projectserver.repo.EventRepo;

@Service
public class EventServiceImpl implements EventService {

	@Autowired
	private EventRepo eventRepo;

	@Override
	public boolean insert(Event event) {
		return eventRepo.insert(event);
	}
	@Override
	public boolean update(Event event) {
		return eventRepo.update(event);
	}
	@Override
	public boolean delete(Event event) {
		return eventRepo.delete(event);
	}
	@Override
	public Event selectOne(String id) {
		return eventRepo.selectOne(id);
	}
	@Override
	public List<Event> selectAll(String owner) {
		return eventRepo.selectAll(owner);
	}

}
