package conf.live.cfp.proposal.application;

import conf.live.cfp.proposal.domain.model.Proposal;
import conf.live.cfp.proposal.domain.model.ProposalStatus;
import conf.live.cfp.proposal.domain.port.in.SubmitProposalCommand;
import conf.live.cfp.proposal.domain.port.out.ProposalRepository;
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
class SubmitProposalServiceTest {

	@Mock
	private ProposalRepository proposalRepository;

	@Test
	void should_submit_a_draft_proposal_built_from_the_command_and_persist_it() {
		SubmitProposalService service = new SubmitProposalService(proposalRepository);
		SubmitProposalCommand command = new SubmitProposalCommand("Title", "Description", "speaker-1");
		when(proposalRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		Proposal submitted = service.submit(command);

		assertThat(submitted.title()).isEqualTo("Title");
		assertThat(submitted.description()).isEqualTo("Description");
		assertThat(submitted.speakerId()).isEqualTo("speaker-1");
		assertThat(submitted.status()).isEqualTo(ProposalStatus.DRAFT);

		ArgumentCaptor<Proposal> captor = ArgumentCaptor.forClass(Proposal.class);
		verify(proposalRepository).save(captor.capture());
		assertThat(captor.getValue()).isEqualTo(submitted);
	}

	@Test
	void should_return_the_proposal_persisted_by_the_repository() {
		SubmitProposalService service = new SubmitProposalService(proposalRepository);
		SubmitProposalCommand command = new SubmitProposalCommand("Title", "Description", "speaker-1");
		Proposal persisted = Proposal.submit("Title", "Description", "speaker-1");
		when(proposalRepository.save(any())).thenReturn(persisted);

		Proposal result = service.submit(command);

		assertThat(result).isEqualTo(persisted);
	}
}
