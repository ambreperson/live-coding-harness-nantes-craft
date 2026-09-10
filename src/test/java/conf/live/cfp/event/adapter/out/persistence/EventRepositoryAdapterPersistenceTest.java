package conf.live.cfp.event.adapter.out.persistence;

import conf.live.cfp.event.domain.model.Event;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test verifying that an {@link Event} really round-trips through the
 * real H2 database via Spring Data JPA, on top of the unit-tested mapping logic.
 */
@DataJpaTest
@Import(EventRepositoryAdapter.class)
class EventRepositoryAdapterPersistenceTest {

	@Autowired
	private EventRepositoryAdapter eventRepositoryAdapter;

	@Autowired
	private EventJpaRepository eventJpaRepository;

	@Test
	void should_persist_an_event_and_make_it_retrievable() {
		Event event = Event.create("Devoxx");

		eventRepositoryAdapter.save(event);

		Optional<EventEntity> found = eventJpaRepository.findById(event.id());
		assertThat(found).isPresent();
		assertThat(found.get().getName()).isEqualTo("Devoxx");
	}
}
