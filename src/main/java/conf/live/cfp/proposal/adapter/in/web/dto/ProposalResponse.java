package conf.live.cfp.proposal.adapter.in.web.dto;

import conf.live.cfp.proposal.domain.model.Proposal;
import conf.live.cfp.proposal.domain.model.ProposalStatus;

/**
 * HTTP response body representing a proposal.
 */
public record ProposalResponse(String id, String title, String description, String speakerId, ProposalStatus status, String eventId) {

	public static ProposalResponse from(Proposal proposal) {
		return new ProposalResponse(proposal.id(), proposal.title(), proposal.description(), proposal.speakerId(), proposal.status(), proposal.eventId());
	}
}
