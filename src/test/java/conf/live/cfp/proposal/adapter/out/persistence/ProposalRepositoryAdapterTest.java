package conf.live.cfp.proposal.adapter.out.persistence;

import conf.live.cfp.proposal.domain.model.Proposal;
import conf.live.cfp.proposal.domain.model.ProposalStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProposalRepositoryAdapterTest {

	@Mock
	private ProposalJpaRepository proposalJpaRepository;

	@Test
	void should_map_the_proposal_to_an_entity_and_persist_it() {
		ProposalRepositoryAdapter adapter = new ProposalRepositoryAdapter(proposalJpaRepository);
		Proposal proposal = Proposal.submit("Title", "Description", "speaker-1");
		when(proposalJpaRepository.save(any(ProposalEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

		adapter.save(proposal);

		ArgumentCaptor<ProposalEntity> captor = ArgumentCaptor.forClass(ProposalEntity.class);
		verify(proposalJpaRepository).save(captor.capture());
		ProposalEntity persistedEntity = captor.getValue();
		assertThat(persistedEntity.getId()).isEqualTo(proposal.id());
		assertThat(persistedEntity.getTitle()).isEqualTo(proposal.title());
		assertThat(persistedEntity.getDescription()).isEqualTo(proposal.description());
		assertThat(persistedEntity.getSpeakerId()).isEqualTo(proposal.speakerId());
		assertThat(persistedEntity.getStatus()).isEqualTo(ProposalStatus.DRAFT);
	}

	@Test
	void should_map_the_persisted_entity_back_to_a_domain_proposal() {
		ProposalRepositoryAdapter adapter = new ProposalRepositoryAdapter(proposalJpaRepository);
		Proposal proposal = Proposal.submit("Title", "Description", "speaker-1");
		ProposalEntity savedEntity = new ProposalEntity(proposal.id(), proposal.title(), proposal.description(), proposal.speakerId(), proposal.status());
		when(proposalJpaRepository.save(any(ProposalEntity.class))).thenReturn(savedEntity);

		Proposal result = adapter.save(proposal);

		assertThat(result).isEqualTo(proposal);
		assertThat(result.title()).isEqualTo("Title");
		assertThat(result.description()).isEqualTo("Description");
		assertThat(result.speakerId()).isEqualTo("speaker-1");
		assertThat(result.status()).isEqualTo(ProposalStatus.DRAFT);
	}
}
