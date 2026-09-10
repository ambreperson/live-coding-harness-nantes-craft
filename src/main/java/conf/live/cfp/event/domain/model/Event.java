package conf.live.cfp.event.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate root of the event domain: a conference for which speakers can submit proposals.
 *
 * <p>The aggregate is immutable and framework-agnostic: it knows nothing about
 * persistence, HTTP or any other infrastructure concern.</p>
 */
public final class Event {

	private final String id;
	private final String name;

	private Event(String id, String name) {
		this.id = id;
		this.name = name;
	}

	/**
	 * Creates a new event.
	 *
	 * @throws InvalidEventException if the name is blank
	 */
	public static Event create(String name) {
		requireNonBlank(name, "Event name must not be blank");

		return new Event(UUID.randomUUID().toString(), name);
	}

	/**
	 * Rehydrates an existing event, typically from persistence. No validation is performed:
	 * the invariants were already enforced when the event was first created.
	 */
	public static Event rehydrate(String id, String name) {
		return new Event(id, name);
	}

	private static void requireNonBlank(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new InvalidEventException(message);
		}
	}

	public String id() {
		return id;
	}

	public String name() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Event event)) return false;
		return Objects.equals(id, event.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "Event{id='%s', name='%s'}".formatted(id, name);
	}
}
