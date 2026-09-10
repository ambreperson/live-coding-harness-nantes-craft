package conf.live.cfp.event.application;

import conf.live.cfp.event.domain.model.Event;
import conf.live.cfp.event.domain.port.out.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckEventExistsServiceTest {

	@Mock
	private EventRepository eventRepository;

	@Test
	void should_return_true_when_the_event_exists() {
		CheckEventExistsService service = new CheckEventExistsService(eventRepository);
		when(eventRepository.findById("event-1")).thenReturn(Optional.of(Event.rehydrate("event-1", "My Conf")));

		boolean exists = service.exists("event-1");

		assertThat(exists).isTrue();
	}

	@Test
	void should_return_false_when_the_event_does_not_exist() {
		CheckEventExistsService service = new CheckEventExistsService(eventRepository);
		when(eventRepository.findById("missing-event")).thenReturn(Optional.empty());

		boolean exists = service.exists("missing-event");

		assertThat(exists).isFalse();
	}
}
