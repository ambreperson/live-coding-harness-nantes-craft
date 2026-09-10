package conf.live.cfp.event.domain.port.out;

import conf.live.cfp.event.domain.model.Event;

import java.util.List;
import java.util.Optional;

/**
 * Output port: persistence abstraction for {@link Event} aggregates.
 * Implemented by an adapter in the infrastructure layer.
 */
public interface EventRepository {

	Event save(Event event);

	Optional<Event> findById(String id);

	List<Event> findAll();
}
