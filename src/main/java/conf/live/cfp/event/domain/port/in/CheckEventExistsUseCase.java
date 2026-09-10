package conf.live.cfp.event.domain.port.in;

/**
 * Input port: check whether an event id refers to an existing event.
 *
 * <p>Exposed so other domains (e.g. {@code proposal}) can validate a reference to an
 * event without reaching into this domain's {@code domain.model} or {@code adapter}
 * packages — only this {@code port.in} contract crosses the domain boundary.</p>
 */
public interface CheckEventExistsUseCase {

	boolean exists(String eventId);
}
