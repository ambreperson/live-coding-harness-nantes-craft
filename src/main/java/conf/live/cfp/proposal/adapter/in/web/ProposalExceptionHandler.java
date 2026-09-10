package conf.live.cfp.proposal.adapter.in.web;

import conf.live.cfp.proposal.domain.model.EventNotFoundException;
import conf.live.cfp.proposal.domain.model.InvalidProposalException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates proposal domain errors into HTTP responses, keeping the domain
 * itself free of any HTTP concern.
 */
@RestControllerAdvice(assignableTypes = ProposalController.class)
public class ProposalExceptionHandler {

	@ExceptionHandler(InvalidProposalException.class)
	public ProblemDetail handleInvalidProposal(InvalidProposalException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
	}

	@ExceptionHandler(EventNotFoundException.class)
	public ProblemDetail handleEventNotFound(EventNotFoundException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
	}
}
