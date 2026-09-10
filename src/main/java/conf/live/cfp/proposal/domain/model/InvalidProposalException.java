package conf.live.cfp.proposal.domain.model;

/**
 * Raised when a {@link Proposal} is built from invalid data.
 * This is a domain error: it must never depend on any framework or infrastructure type.
 */
public class InvalidProposalException extends RuntimeException {

	public InvalidProposalException(String message) {
		super(message);
	}
}
