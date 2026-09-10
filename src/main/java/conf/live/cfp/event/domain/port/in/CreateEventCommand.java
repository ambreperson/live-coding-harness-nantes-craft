package conf.live.cfp.event.domain.port.in;

/**
 * Input of the {@link CreateEventUseCase}: the raw data required to create a new event.
 */
public record CreateEventCommand(String name) {
}
