package conf.live.cfp.event.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventTest {

	@Test
	void should_create_an_event_with_the_given_name() {
		Event event = Event.create("My Conf");

		assertThat(event.name()).isEqualTo("My Conf");
	}

	@Test
	void should_assign_a_unique_id_to_each_created_event() {
		Event first = Event.create("My Conf");
		Event second = Event.create("My Conf");

		assertThat(first.id()).isNotNull();
		assertThat(second.id()).isNotNull();
		assertThat(first.id()).isNotEqualTo(second.id());
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "\t"})
	void should_reject_a_blank_name(String blankName) {
		assertThatThrownBy(() -> Event.create(blankName))
				.isInstanceOf(InvalidEventException.class)
				.hasMessage("Event name must not be blank");
	}

	@Test
	void should_rehydrate_an_event_without_re_validating_it() {
		Event event = Event.rehydrate("event-1", "My Conf");

		assertThat(event.id()).isEqualTo("event-1");
		assertThat(event.name()).isEqualTo("My Conf");
	}
}
