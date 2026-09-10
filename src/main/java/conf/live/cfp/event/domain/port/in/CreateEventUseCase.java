package conf.live.cfp.event.domain.port.in;

import conf.live.cfp.event.domain.model.Event;

/**
 * Input port: create a new conference event.
 */
public interface CreateEventUseCase {

	Event create(CreateEventCommand command);
}
