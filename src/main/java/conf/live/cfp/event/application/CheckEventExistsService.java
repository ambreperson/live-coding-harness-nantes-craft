package conf.live.cfp.event.application;

import conf.live.cfp.event.domain.port.in.CheckEventExistsUseCase;
import conf.live.cfp.event.domain.port.out.EventRepository;
import org.springframework.stereotype.Service;

/**
 * Application service implementing the {@link CheckEventExistsUseCase}:
 * delegates the lookup to the {@link EventRepository} output port.
 */
@Service
public class CheckEventExistsService implements CheckEventExistsUseCase {

	private final EventRepository eventRepository;

	public CheckEventExistsService(EventRepository eventRepository) {
		this.eventRepository = eventRepository;
	}

	@Override
	public boolean exists(String eventId) {
		return eventRepository.findById(eventId).isPresent();
	}
}
