package conf.live.cfp.proposal.domain.port.in;

import conf.live.cfp.proposal.domain.model.Proposal;

/**
 * Input port: submit a new talk proposal to the call for paper.
 */
public interface SubmitProposalUseCase {

	Proposal submit(SubmitProposalCommand command);
}
