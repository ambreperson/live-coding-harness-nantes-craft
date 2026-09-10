package conf.live.cfp.proposal.domain.model;

/**
 * Lifecycle status of a {@link Proposal}.
 * Only {@code DRAFT} is used for now; further states (SUBMITTED, ACCEPTED, REJECTED, ...)
 * will be introduced along with the use cases that transition to them.
 */
public enum ProposalStatus {
	DRAFT
}
