package conf.live.cfp.proposal.application;

import conf.live.cfp.proposal.domain.model.Proposal;
import conf.live.cfp.proposal.domain.port.in.SubmitProposalCommand;
import conf.live.cfp.proposal.domain.port.in.SubmitProposalUseCase;
import conf.live.cfp.proposal.domain.port.out.ProposalRepository;
import org.springframework.stereotype.Service;

/**
 * Application service implementing the {@link SubmitProposalUseCase}:
 * builds a new draft proposal from the command and delegates its persistence
 * to the {@link ProposalRepository} output port.
 */
@Service
public class SubmitProposalService implements SubmitProposalUseCase {

	private final ProposalRepository proposalRepository;

	public SubmitProposalService(ProposalRepository proposalRepository) {
		this.proposalRepository = proposalRepository;
	}

	@Override
	public Proposal submit(SubmitProposalCommand command) {
		Proposal proposal = Proposal.submit(command.title(), command.description(), command.speakerId(), command.eventId());
		return proposalRepository.save(proposal);
	}
}
