package conf.live.cfp.event.adapter.in.web;

import conf.live.cfp.event.domain.model.Event;
import conf.live.cfp.event.domain.model.InvalidEventException;
import conf.live.cfp.event.domain.port.in.CreateEventUseCase;
import conf.live.cfp.event.domain.port.in.ListEventsUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
class EventControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CreateEventUseCase createEventUseCase;

	@MockitoBean
	private ListEventsUseCase listEventsUseCase;

	@Test
	void should_return_201_with_the_created_event_when_the_request_is_valid() throws Exception {
		Event created = Event.rehydrate("event-1", "My Conf");
		when(createEventUseCase.create(any())).thenReturn(created);

		mockMvc.perform(post("/api/events")
						.contentType("application/json")
						.content("""
								{
								  "name": "My Conf"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/api/events/event-1"))
				.andExpect(jsonPath("$.id").value("event-1"))
				.andExpect(jsonPath("$.name").value("My Conf"));
	}

	@Test
	void should_return_400_when_the_name_is_blank() throws Exception {
		mockMvc.perform(post("/api/events")
						.contentType("application/json")
						.content("""
								{
								  "name": ""
								}
								"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void should_return_400_with_the_domain_message_when_the_use_case_rejects_the_event() throws Exception {
		when(createEventUseCase.create(any())).thenThrow(new InvalidEventException("Event name must not be blank"));

		mockMvc.perform(post("/api/events")
						.contentType("application/json")
						.content("""
								{
								  "name": "My Conf"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Event name must not be blank"));
	}

	@Test
	void should_return_200_with_all_events() throws Exception {
		Event first = Event.rehydrate("event-1", "My Conf");
		Event second = Event.rehydrate("event-2", "Other Conf");
		when(listEventsUseCase.listAll()).thenReturn(List.of(first, second));

		mockMvc.perform(get("/api/events"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value("event-1"))
				.andExpect(jsonPath("$[0].name").value("My Conf"))
				.andExpect(jsonPath("$[1].id").value("event-2"))
				.andExpect(jsonPath("$[1].name").value("Other Conf"));
	}
}
