package conf.live.cfp.event.domain.model;

/**
 * Raised when an {@link Event} is built from invalid data.
 * This is a domain error: it must never depend on any framework or infrastructure type.
 */
public class InvalidEventException extends RuntimeException {

	public InvalidEventException(String message) {
		super(message);
	}
}
