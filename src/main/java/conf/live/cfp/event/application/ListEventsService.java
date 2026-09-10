package conf.live.cfp.event.application;

import conf.live.cfp.event.domain.model.Event;
import conf.live.cfp.event.domain.port.in.ListEventsUseCase;
import conf.live.cfp.event.domain.port.out.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Application service implementing the {@link ListEventsUseCase}:
 * delegates listing to the {@link EventRepository} output port.
 */
@Service
public class ListEventsService implements ListEventsUseCase {

	private final EventRepository eventRepository;

	public ListEventsService(EventRepository eventRepository) {
		this.eventRepository = eventRepository;
	}

	@Override
	public List<Event> listAll() {
		return eventRepository.findAll();
	}
}
