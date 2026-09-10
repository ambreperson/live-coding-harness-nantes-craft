package conf.live.cfp.event.adapter.out.persistence;

import conf.live.cfp.event.domain.model.Event;
import conf.live.cfp.event.domain.port.out.EventRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Output adapter implementing the {@link EventRepository} port on top of Spring Data JPA.
 * Translates between the domain aggregate and its JPA persistence model.
 */
@Repository
public class EventRepositoryAdapter implements EventRepository {

	private final EventJpaRepository eventJpaRepository;

	public EventRepositoryAdapter(EventJpaRepository eventJpaRepository) {
		this.eventJpaRepository = eventJpaRepository;
	}

	@Override
	public Event save(Event event) {
		EventEntity entity = toEntity(event);
		EventEntity savedEntity = eventJpaRepository.save(entity);
		return toDomain(savedEntity);
	}

	@Override
	public Optional<Event> findById(String id) {
		return eventJpaRepository.findById(id).map(EventRepositoryAdapter::toDomain);
	}

	@Override
	public List<Event> findAll() {
		return eventJpaRepository.findAll().stream().map(EventRepositoryAdapter::toDomain).toList();
	}

	private static EventEntity toEntity(Event event) {
		return new EventEntity(event.id(), event.name());
	}

	private static Event toDomain(EventEntity entity) {
		return Event.rehydrate(entity.getId(), entity.getName());
	}
}
