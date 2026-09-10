package conf.live.cfp.event.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * HTTP request body to create a new event.
 */
public record CreateEventRequest(@NotBlank(message = "name must not be blank") String name) {
}
