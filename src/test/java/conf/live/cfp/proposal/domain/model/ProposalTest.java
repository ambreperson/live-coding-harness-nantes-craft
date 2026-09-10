package conf.live.cfp.proposal.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProposalTest {

	@Test
	void should_create_a_draft_proposal_with_the_given_data() {
		Proposal proposal = Proposal.submit("Hexagonal architecture in practice", "A deep dive into ports and adapters", "speaker-1", "event-1");

		assertThat(proposal.title()).isEqualTo("Hexagonal architecture in practice");
		assertThat(proposal.description()).isEqualTo("A deep dive into ports and adapters");
		assertThat(proposal.speakerId()).isEqualTo("speaker-1");
		assertThat(proposal.status()).isEqualTo(ProposalStatus.DRAFT);
		assertThat(proposal.eventId()).isEqualTo("event-1");
	}

	@Test
	void should_assign_a_unique_id_to_each_submitted_proposal() {
		Proposal first = Proposal.submit("Title", "Description", "speaker-1", "event-1");
		Proposal second = Proposal.submit("Title", "Description", "speaker-1", "event-1");

		assertThat(first.id()).isNotNull();
		assertThat(second.id()).isNotNull();
		assertThat(first.id()).isNotEqualTo(second.id());
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "\t"})
	void should_reject_a_blank_title(String blankTitle) {
		assertThatThrownBy(() -> Proposal.submit(blankTitle, "Description", "speaker-1", "event-1"))
				.isInstanceOf(InvalidProposalException.class)
				.hasMessage("Proposal title must not be blank");
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "\t"})
	void should_reject_a_blank_description(String blankDescription) {
		assertThatThrownBy(() -> Proposal.submit("Title", blankDescription, "speaker-1", "event-1"))
				.isInstanceOf(InvalidProposalException.class)
				.hasMessage("Proposal description must not be blank");
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "\t"})
	void should_reject_a_blank_speaker_id(String blankSpeakerId) {
		assertThatThrownBy(() -> Proposal.submit("Title", "Description", blankSpeakerId, "event-1"))
				.isInstanceOf(InvalidProposalException.class)
				.hasMessage("Proposal speaker id must not be blank");
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "\t"})
	void should_reject_a_blank_event_id(String blankEventId) {
		assertThatThrownBy(() -> Proposal.submit("Title", "Description", "speaker-1", blankEventId))
				.isInstanceOf(InvalidProposalException.class)
				.hasMessage("Proposal event id must not be blank");
	}

	@Test
	void should_rehydrate_a_proposal_without_re_validating_it() {
		Proposal proposal = Proposal.rehydrate("proposal-1", "Title", "Description", "speaker-1", ProposalStatus.DRAFT, "event-1");

		assertThat(proposal.id()).isEqualTo("proposal-1");
		assertThat(proposal.title()).isEqualTo("Title");
		assertThat(proposal.description()).isEqualTo("Description");
		assertThat(proposal.speakerId()).isEqualTo("speaker-1");
		assertThat(proposal.status()).isEqualTo(ProposalStatus.DRAFT);
		assertThat(proposal.eventId()).isEqualTo("event-1");
	}
}
