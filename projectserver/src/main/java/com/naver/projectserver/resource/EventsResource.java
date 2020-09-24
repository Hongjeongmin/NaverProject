package com.naver.projectserver.resource;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import java.util.List;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.naver.projectserver.controller.EventController;

import lombok.Getter;

@Getter
public class EventsResource extends EntityModel<List<EventResource>> {
	
	//TODO List 형태의 객체는 JsonUnwrapped 이 적용되지 않음 다른 방법을 찾아야합니다.
	@JsonUnwrapped
	List<EventResource> eventsResources;

	public EventsResource(List<EventResource> eventsResources) {
		this.eventsResources = eventsResources;
		add(linkTo(EventController.class).withSelfRel());
	}

	@Override
	public EntityModel<List<EventResource>> add(Link... links) {
		return super.add(links);
	}
}
