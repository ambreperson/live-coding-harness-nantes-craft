package conf.live.cfp.proposal.application;

import conf.live.cfp.event.domain.model.EventNotFoundException;
import conf.live.cfp.event.domain.port.out.EventRepository;
import conf.live.cfp.proposal.domain.model.Proposal;
import conf.live.cfp.proposal.domain.port.in.SubmitProposalCommand;
import conf.live.cfp.proposal.domain.port.in.SubmitProposalUseCase;
import conf.live.cfp.proposal.domain.port.out.ProposalRepository;
import org.springframework.stereotype.Service;

/**
 * Application service implementing the {@link SubmitProposalUseCase}:
 * checks that the referenced event exists, builds a new draft proposal from
 * the command and delegates its persistence to the {@link ProposalRepository}
 * output port.
 */
@Service
public class SubmitProposalService implements SubmitProposalUseCase {

	private final ProposalRepository proposalRepository;
	private final EventRepository eventRepository;

	public SubmitProposalService(ProposalRepository proposalRepository, EventRepository eventRepository) {
		this.proposalRepository = proposalRepository;
		this.eventRepository = eventRepository;
	}

	@Override
	public Proposal submit(SubmitProposalCommand command) {
		eventRepository.findById(command.eventId())
				.orElseThrow(() -> new EventNotFoundException("Event not found: " + command.eventId()));

		Proposal proposal = Proposal.submit(command.title(), command.description(), command.speakerId(), command.eventId());
		return proposalRepository.save(proposal);
	}
}
