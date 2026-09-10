package conf.live.cfp.event.application;

import conf.live.cfp.event.domain.model.Event;
import conf.live.cfp.event.domain.port.out.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListEventsServiceTest {

	@Mock
	private EventRepository eventRepository;

	@Test
	void should_return_all_events_from_the_repository() {
		ListEventsService service = new ListEventsService(eventRepository);
		List<Event> events = List.of(
				Event.rehydrate("event-1", "My Conf"),
				Event.rehydrate("event-2", "Another Conf")
		);
		when(eventRepository.findAll()).thenReturn(events);

		List<Event> result = service.listAll();

		assertThat(result).isEqualTo(events);
	}
}
