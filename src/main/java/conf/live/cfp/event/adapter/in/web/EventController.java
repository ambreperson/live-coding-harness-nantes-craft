package conf.live.cfp.event.adapter.in.web;

import conf.live.cfp.event.adapter.in.web.dto.CreateEventRequest;
import conf.live.cfp.event.adapter.in.web.dto.EventResponse;
import conf.live.cfp.event.domain.model.Event;
import conf.live.cfp.event.domain.port.in.CreateEventCommand;
import conf.live.cfp.event.domain.port.in.CreateEventUseCase;
import conf.live.cfp.event.domain.port.in.ListEventsUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * Input adapter exposing the event use cases over HTTP.
 */
@RestController
@RequestMapping("/api/events")
public class EventController {

	private final CreateEventUseCase createEventUseCase;
	private final ListEventsUseCase listEventsUseCase;

	public EventController(CreateEventUseCase createEventUseCase, ListEventsUseCase listEventsUseCase) {
		this.createEventUseCase = createEventUseCase;
		this.listEventsUseCase = listEventsUseCase;
	}

	@PostMapping
	public ResponseEntity<EventResponse> create(@Valid @RequestBody CreateEventRequest request) {
		Event event = createEventUseCase.create(new CreateEventCommand(request.name()));
		return ResponseEntity.created(URI.create("/api/events/" + event.id()))
				.body(EventResponse.from(event));
	}

	@GetMapping
	public List<EventResponse> listAll() {
		return listEventsUseCase.listAll().stream()
				.map(EventResponse::from)
				.toList();
	}
}
