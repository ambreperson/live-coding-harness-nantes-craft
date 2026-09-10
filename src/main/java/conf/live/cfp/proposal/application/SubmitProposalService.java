package conf.live.cfp.proposal.application;

import conf.live.cfp.event.domain.port.in.CheckEventExistsUseCase;
import conf.live.cfp.proposal.domain.model.EventNotFoundException;
import conf.live.cfp.proposal.domain.model.Proposal;
import conf.live.cfp.proposal.domain.port.in.SubmitProposalCommand;
import conf.live.cfp.proposal.domain.port.in.SubmitProposalUseCase;
import conf.live.cfp.proposal.domain.port.out.ProposalRepository;
import org.springframework.stereotype.Service;

/**
 * Application service implementing the {@link SubmitProposalUseCase}:
 * checks that the referenced event exists (via the event domain's
 * {@link CheckEventExistsUseCase} port.in, never reaching into its model or
 * adapter packages directly), builds a new draft proposal from the command
 * and delegates its persistence to the {@link ProposalRepository} output port.
 */
@Service
public class SubmitProposalService implements SubmitProposalUseCase {

	private final ProposalRepository proposalRepository;
	private final CheckEventExistsUseCase checkEventExistsUseCase;

	public SubmitProposalService(ProposalRepository proposalRepository, CheckEventExistsUseCase checkEventExistsUseCase) {
		this.proposalRepository = proposalRepository;
		this.checkEventExistsUseCase = checkEventExistsUseCase;
	}

	@Override
	public Proposal submit(SubmitProposalCommand command) {
		if (!checkEventExistsUseCase.exists(command.eventId())) {
			throw new EventNotFoundException("Event not found: " + command.eventId());
		}

		Proposal proposal = Proposal.submit(command.title(), command.description(), command.speakerId(), command.eventId());
		return proposalRepository.save(proposal);
	}
}
