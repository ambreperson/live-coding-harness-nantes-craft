package conf.live.cfp.proposal.domain.port.out;

import conf.live.cfp.proposal.domain.model.Proposal;

/**
 * Output port: persistence abstraction for {@link Proposal} aggregates.
 * Implemented by an adapter in the infrastructure layer.
 */
public interface ProposalRepository {

	Proposal save(Proposal proposal);
}
