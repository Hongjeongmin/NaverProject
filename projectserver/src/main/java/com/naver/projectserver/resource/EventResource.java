package com.naver.projectserver.resource;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.naver.projectserver.controller.EventController;
import com.naver.projectserver.mapper.Event;

import lombok.Getter;

@Getter
public class EventResource extends EntityModel<Event>{
	
	@JsonUnwrapped
	Event event;
	
	public EventResource(Event event) {
		this.event = event;
		add(linkTo(EventController.class).slash(event.getId()).withSelfRel());
	}
}
