package conf.live.cfp.event.application;

import conf.live.cfp.event.domain.model.Event;
import conf.live.cfp.event.domain.port.in.CreateEventCommand;
import conf.live.cfp.event.domain.port.out.EventRepository;
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
class CreateEventServiceTest {

	@Mock
	private EventRepository eventRepository;

	@Test
	void should_create_an_event_built_from_the_command_and_persist_it() {
		CreateEventService service = new CreateEventService(eventRepository);
		CreateEventCommand command = new CreateEventCommand("My Conf");
		when(eventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		Event created = service.create(command);

		assertThat(created.name()).isEqualTo("My Conf");

		ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
		verify(eventRepository).save(captor.capture());
		assertThat(captor.getValue()).isEqualTo(created);
	}

	@Test
	void should_return_the_event_persisted_by_the_repository() {
		CreateEventService service = new CreateEventService(eventRepository);
		CreateEventCommand command = new CreateEventCommand("My Conf");
		Event persisted = Event.rehydrate("event-1", "My Conf");
		when(eventRepository.save(any())).thenReturn(persisted);

		Event result = service.create(command);

		assertThat(result).isEqualTo(persisted);
	}
}
