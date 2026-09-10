package conf.live.cfp.proposal.domain.port.in;

/**
 * Input of the {@link SubmitProposalUseCase}: the raw data required to submit a new proposal.
 */
public record SubmitProposalCommand(String title, String description, String speakerId) {
}
