package conf.live.cfp.event.adapter.out.persistence;

import conf.live.cfp.event.domain.model.Event;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventRepositoryAdapterTest {

	@Mock
	private EventJpaRepository eventJpaRepository;

	@Test
	void should_map_the_event_to_an_entity_and_persist_it() {
		EventRepositoryAdapter adapter = new EventRepositoryAdapter(eventJpaRepository);
		Event event = Event.create("Devoxx");
		when(eventJpaRepository.save(any(EventEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

		adapter.save(event);

		ArgumentCaptor<EventEntity> captor = ArgumentCaptor.forClass(EventEntity.class);
		verify(eventJpaRepository).save(captor.capture());
		EventEntity persistedEntity = captor.getValue();
		assertThat(persistedEntity.getId()).isEqualTo(event.id());
		assertThat(persistedEntity.getName()).isEqualTo(event.name());
	}

	@Test
	void should_map_the_persisted_entity_back_to_a_domain_event() {
		EventRepositoryAdapter adapter = new EventRepositoryAdapter(eventJpaRepository);
		Event event = Event.create("Devoxx");
		EventEntity savedEntity = new EventEntity(event.id(), event.name());
		when(eventJpaRepository.save(any(EventEntity.class))).thenReturn(savedEntity);

		Event result = adapter.save(event);

		assertThat(result).isEqualTo(event);
		assertThat(result.name()).isEqualTo("Devoxx");
	}

	@Test
	void should_return_the_event_when_found_by_id() {
		EventRepositoryAdapter adapter = new EventRepositoryAdapter(eventJpaRepository);
		EventEntity entity = new EventEntity("event-1", "Devoxx");
		when(eventJpaRepository.findById("event-1")).thenReturn(Optional.of(entity));

		Optional<Event> result = adapter.findById("event-1");

		assertThat(result).isEqualTo(Optional.of(Event.rehydrate("event-1", "Devoxx")));
	}

	@Test
	void should_return_empty_when_no_event_matches_the_id() {
		EventRepositoryAdapter adapter = new EventRepositoryAdapter(eventJpaRepository);
		when(eventJpaRepository.findById("missing")).thenReturn(Optional.empty());

		Optional<Event> result = adapter.findById("missing");

		assertThat(result).isEmpty();
	}

	@Test
	void should_return_all_events() {
		EventRepositoryAdapter adapter = new EventRepositoryAdapter(eventJpaRepository);
		EventEntity entity1 = new EventEntity("event-1", "Devoxx");
		EventEntity entity2 = new EventEntity("event-2", "Sunny Tech");
		when(eventJpaRepository.findAll()).thenReturn(List.of(entity1, entity2));

		List<Event> result = adapter.findAll();

		assertThat(result).containsExactly(Event.rehydrate("event-1", "Devoxx"), Event.rehydrate("event-2", "Sunny Tech"));
	}
}
