package conf.live.cfp.proposal.adapter.out.persistence;

import conf.live.cfp.proposal.domain.model.Proposal;
import conf.live.cfp.proposal.domain.port.out.ProposalRepository;
import org.springframework.stereotype.Repository;

/**
 * Output adapter implementing the {@link ProposalRepository} port on top of Spring Data JPA.
 * Translates between the domain aggregate and its JPA persistence model.
 */
@Repository
public class ProposalRepositoryAdapter implements ProposalRepository {

	private final ProposalJpaRepository proposalJpaRepository;

	public ProposalRepositoryAdapter(ProposalJpaRepository proposalJpaRepository) {
		this.proposalJpaRepository = proposalJpaRepository;
	}

	@Override
	public Proposal save(Proposal proposal) {
		ProposalEntity entity = toEntity(proposal);
		ProposalEntity savedEntity = proposalJpaRepository.save(entity);
		return toDomain(savedEntity);
	}

	private static ProposalEntity toEntity(Proposal proposal) {
		return new ProposalEntity(proposal.id(), proposal.title(), proposal.description(), proposal.speakerId(), proposal.status(), proposal.eventId());
	}

	private static Proposal toDomain(ProposalEntity entity) {
		return Proposal.rehydrate(entity.getId(), entity.getTitle(), entity.getDescription(), entity.getSpeakerId(), entity.getStatus(), entity.getEventId());
	}
}
