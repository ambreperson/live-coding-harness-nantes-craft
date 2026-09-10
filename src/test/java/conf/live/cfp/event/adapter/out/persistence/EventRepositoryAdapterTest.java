package conf.live.cfp.event.adapter.out.persistence;

import conf.live.cfp.event.domain.model.Event;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}
