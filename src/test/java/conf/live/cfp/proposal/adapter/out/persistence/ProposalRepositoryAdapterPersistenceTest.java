package conf.live.cfp.proposal.adapter.out.persistence;

import conf.live.cfp.proposal.domain.model.Proposal;
import conf.live.cfp.proposal.domain.model.ProposalStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test verifying that a {@link Proposal} really round-trips through the
 * real H2 database via Spring Data JPA, on top of the unit-tested mapping logic.
 */
@DataJpaTest
@Import(ProposalRepositoryAdapter.class)
class ProposalRepositoryAdapterPersistenceTest {

	@Autowired
	private ProposalRepositoryAdapter proposalRepositoryAdapter;

	@Autowired
	private ProposalJpaRepository proposalJpaRepository;

	@Test
	void should_persist_a_proposal_and_make_it_retrievable() {
		Proposal proposal = Proposal.submit("Hexagonal architecture in practice", "A deep dive into ports and adapters", "speaker-1", "event-1");

		proposalRepositoryAdapter.save(proposal);

		Optional<ProposalEntity> found = proposalJpaRepository.findById(proposal.id());
		assertThat(found).isPresent();
		assertThat(found.get().getTitle()).isEqualTo("Hexagonal architecture in practice");
		assertThat(found.get().getDescription()).isEqualTo("A deep dive into ports and adapters");
		assertThat(found.get().getSpeakerId()).isEqualTo("speaker-1");
		assertThat(found.get().getStatus()).isEqualTo(ProposalStatus.DRAFT);
	}
}
