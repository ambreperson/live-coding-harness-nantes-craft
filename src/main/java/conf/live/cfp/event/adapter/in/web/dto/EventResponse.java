package conf.live.cfp.event.adapter.in.web.dto;

import conf.live.cfp.event.domain.model.Event;

/**
 * HTTP response body representing an event.
 */
public record EventResponse(String id, String name) {

	public static EventResponse from(Event event) {
		return new EventResponse(event.id(), event.name());
	}
}
