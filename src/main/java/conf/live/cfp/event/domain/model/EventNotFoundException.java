package conf.live.cfp.event.domain.model;

/**
 * Raised when an {@link Event} is referenced by an id that does not match any existing event.
 * This is a domain error: it must never depend on any framework or infrastructure type.
 */
public class EventNotFoundException extends RuntimeException {

	public EventNotFoundException(String message) {
		super(message);
	}
}
