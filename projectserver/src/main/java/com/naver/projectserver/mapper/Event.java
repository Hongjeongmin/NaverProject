package com.naver.projectserver.mapper;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/*
 * REST API TEST Data
 * 
 */

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class Event {
	private Integer id;
	private String name;
	private String owner;
	private String description;
	private String location; // (optional)
	private int basePrice; // (optional)
	private int maxPrice; // (optional)
//	@DateTimeFormat(pattern = "yyyy-MM-dd kk:mm:ss")
	private LocalDateTime beginEventDateTime;
//	@DateTimeFormat(pattern = "yyyy-MM-dd kk:mm:ss")
	private LocalDateTime endEventDateTime;
	private boolean online;
	private boolean free;

	/*
	 * 입력 값으로 online 과 free 의 상태를 변형한다.
	 */
	public void update() {
		/* 
		 * basePrice 와 maxPrice 가 0인 경우 공짜 이벤트
		 */
		this.free = basePrice == 0 && maxPrice == 0;
		/*
		 * location이 null이거나 비어 있으면 online 이벤트
		 */
		this.online = location == null || location.isEmpty();
	}
}
