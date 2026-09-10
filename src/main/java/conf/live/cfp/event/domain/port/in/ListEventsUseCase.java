package conf.live.cfp.event.domain.port.in;

import conf.live.cfp.event.domain.model.Event;

import java.util.List;

/**
 * Input port: list all existing conference events.
 */
public interface ListEventsUseCase {

	List<Event> listAll();
}
