package conf.live.cfp.proposal.domain.model;

/**
 * Raised when a {@link Proposal} references an event id that does not match any existing event.
 * This is a domain error owned by the proposal domain: it never depends on any framework or
 * infrastructure type, and never reaches into the event domain's own model or adapter packages.
 */
public class EventNotFoundException extends RuntimeException {

	public EventNotFoundException(String message) {
		super(message);
	}
}
