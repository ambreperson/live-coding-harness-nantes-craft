package conf.live.cfp.event.application;

import conf.live.cfp.event.domain.model.Event;
import conf.live.cfp.event.domain.port.in.CreateEventCommand;
import conf.live.cfp.event.domain.port.in.CreateEventUseCase;
import conf.live.cfp.event.domain.port.out.EventRepository;
import org.springframework.stereotype.Service;

/**
 * Application service implementing the {@link CreateEventUseCase}:
 * builds a new event from the command and delegates its persistence
 * to the {@link EventRepository} output port.
 */
@Service
public class CreateEventService implements CreateEventUseCase {

	private final EventRepository eventRepository;

	public CreateEventService(EventRepository eventRepository) {
		this.eventRepository = eventRepository;
	}

	@Override
	public Event create(CreateEventCommand command) {
		Event event = Event.create(command.name());
		return eventRepository.save(event);
	}
}
