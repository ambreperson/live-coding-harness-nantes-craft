package conf.live.cfp.proposal.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * HTTP request body to submit a new proposal.
 */
public record SubmitProposalRequest(
		@NotBlank(message = "title must not be blank") String title,
		@NotBlank(message = "description must not be blank") String description,
		@NotBlank(message = "speakerId must not be blank") String speakerId,
		@NotBlank(message = "eventId must not be blank") String eventId) {
}
