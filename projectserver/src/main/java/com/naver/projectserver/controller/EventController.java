package com.naver.projectserver.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.naver.projectserver.dto.EventDto;
import com.naver.projectserver.mapper.Event;
import com.naver.projectserver.resource.BaseResource;
import com.naver.projectserver.resource.EventResource;
import com.naver.projectserver.resource.EventsResource;
import com.naver.projectserver.service.EventService;

/*
 * 생산 가능한 MediaType은 JSON 입니다.
 */
@RestController
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_VALUE)
public class EventController {

	@Autowired
	EventService eventService;

	@Autowired
	ModelMapper modelMapper;
	
	/*
	 * access_Token에 등록된 Id가 만든 모든 Event를 조회합니다.
	 */
	@GetMapping
	public ResponseEntity eventSearch(Principal principal) {
		String owner = principal.getName();
		List<Event> events = eventService.selectAll(owner);

		/*
		 * OK로 반환한다. 조회된 결과가 없을 경우에는 status 상태를 empty로 출력하고 link에 현재상태를 표시한다.
		 */
		if (events == null) {
			BaseResource baseResource = new BaseResource("empty", linkTo(EventController.class).withSelfRel());
			baseResource.add(Link.of("http://test.docs.html").withRel("profile"));
			return ResponseEntity.ok(baseResource);
		}
		
		/*
		 * 조회된 결과가 있을 경우 eventResources link정보과 함께담긴 EventResource를 하나하나 add한다.
		 */
		List<EventResource> eventResources = new ArrayList<>();
		for (Event event : events) {
			eventResources.add(new EventResource(event));
		}

		EventsResource eventsResource = new EventsResource(eventResources);

		// TODO events객체를 나타내는 profile 추가해야한다. 현재는 test.docs.html 반환합니다.
		eventsResource.add(Link.of("http://test.docs.html").withRel("profile"));
		return ResponseEntity.ok(eventsResource);
	}
	
	/*
	 * id 값으로 등록된 Event를 조회합니다.
	 * 조회된 id값이 없는 경우에는 status에 empty를 반환합니다.
	 */
	@GetMapping("/{id}")
	public ResponseEntity eventDetail(@PathVariable("id") String id) {
		Event event = eventService.selectOne(id);
		/*
		 * OK로 반환한다. 조회된 결과가 없을 경우에는 status 상태를 empty로 출력하고 link에 현재상태를 표시한다.
		 */
		if (event == null) {
			BaseResource baseResource = new BaseResource("empty",
					linkTo(EventController.class).slash(id).withSelfRel());
			return ResponseEntity.ok(baseResource);
		}

		EventResource eventResource = new EventResource(event);

		// TODO event객체를 나타내는 profile 추가해야한다. 현재는 test.docs.html 반환합니다.
		eventResource.add(Link.of("http://test.docs.html").withRel("profile"));

		return ResponseEntity.ok(eventResource);
	}
	
	/*
	 * 새로운 Event를 등록합니다. Event.owner는 access_Token에서 가져옵니다.
	 * 전달받은 파라미터가 옳바르지 않은 경우 badRequest를 반환합니다.
	 * 성공적으로 등록이 된다면 OK , body에는 등록된 Event의 결과를 반환합니다.
	 */
	@PostMapping
	public ResponseEntity eventRegister(Principal principal, @RequestBody @Valid EventDto eventDto, Errors errors) {
		Event event = modelMapper.map(eventDto, Event.class);

		if (errors.hasErrors()) {
			return ResponseEntity.badRequest().body(errors);
		}

		// Online 인지 , Offline 인지 상태를 보고 결정합니다.
		event.update();
		event.setOwner(principal.getName());

		// Event 시작시간이 이벤트 끝 시간보다 빠를 경우 BadRequest 반환 합니다.
		if (event.getEndEventDateTime() != null && event.getBeginEventDateTime() != null
				&& event.getEndEventDateTime().isBefore(event.getBeginEventDateTime())) {
			errors.reject("wrongDateTime", "Values fo DateTime are wrong");
			return ResponseEntity.badRequest().body(errors);
		}

		// Event base가격이 Event Max가격보다 큰경우 BadRequest 반환 합니다.
		if (event.getBasePrice() != 0 && event.getBasePrice() > event.getMaxPrice()) {
			errors.reject("wrongPrice", "Values fo Price are wrong");
			return ResponseEntity.badRequest().body(errors);
		}

		// Event가 성공적으로 insert 된 경우 isCreate
		if (eventService.insert(event)) {
			EventResource eventResource = new EventResource(event);

			// TODO event객체를 나타내는 profile 추가해야한다. 현재는 test.docs.html
			eventResource.add(Link.of("http://test.docs.html").withRel("profile"));
			return ResponseEntity.created(linkTo(EventController.class).toUri()).body(eventResource);
		}

		// Event가 등록되지 못한 경우 EXPECTATION_FAILED
		errors.reject("insertError", "Failed to insert into database");
		return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(errors);
	}
	
	/*
	 * Event의 description을 수정합니다.
	 * 존재하지 않는 이벤트이거나 Event.owner 와 access_Token의 Id 가 일치하지 않으면 badRequest와 status에 failed를 반환합니다.
	 */
	@PutMapping("/{id}")
	public ResponseEntity eventUpdate(Principal principal, @PathVariable("id") int id, @RequestBody EventDto eventDto) {
		Event event = modelMapper.map(eventDto, Event.class);
		event.setOwner(principal.getName());
		event.setId(id);

		if (eventService.update(event)) {
			/*
			 * 성공시 바뀐 정보를 함께 반환합니다.
			 */
			EventResource eventResource = new EventResource(event);
			// TODO event객체를 나타내는 profile 추가해야한다. 현재는 test.docs.html
			eventResource.add(Link.of("http://test.docs.html").withRel("profile"));
			return ResponseEntity.ok(eventResource);
		}
		/*
		 * FAILED_DEPENDENCY 반 update에 실패하면 status를 failed로 반환합니다.
		 */

		BaseResource baseResource = new BaseResource("failed", linkTo(EventController.class).slash(id).withSelfRel());

		return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(baseResource);
	}
	
	/*
	 * Event를 삭제합니다. 성공시 status : success 를 반환합니다.
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity eventDelete(Principal principal, @PathVariable("id") int id, Event event) {
		event.setOwner(principal.getName());

		if (eventService.delete(event)) {
			return ResponseEntity.ok(new BaseResource("success", linkTo(EventController.class).slash(id).withSelfRel()));
		}

		return ResponseEntity.badRequest().body(new BaseResource("failed", linkTo(EventController.class).slash(id).withSelfRel()));
	}

}
