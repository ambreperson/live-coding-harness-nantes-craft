package conf.live.cfp.event.adapter.in.web;

import conf.live.cfp.event.domain.model.InvalidEventException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates event domain errors into HTTP responses, keeping the domain
 * itself free of any HTTP concern.
 */
@RestControllerAdvice(assignableTypes = EventController.class)
public class EventExceptionHandler {

	@ExceptionHandler(InvalidEventException.class)
	public ProblemDetail handleInvalidEvent(InvalidEventException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
	}
}
