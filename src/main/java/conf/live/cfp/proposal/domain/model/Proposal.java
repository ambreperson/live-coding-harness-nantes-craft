package conf.live.cfp.proposal.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate root of the proposal domain: a talk proposal submitted by a speaker
 * for a conference's call for paper.
 *
 * <p>The aggregate is immutable and framework-agnostic: it knows nothing about
 * persistence, HTTP or any other infrastructure concern.</p>
 */
public final class Proposal {

	private final String id;
	private final String title;
	private final String description;
	private final String speakerId;
	private final ProposalStatus status;

	private Proposal(String id, String title, String description, String speakerId, ProposalStatus status) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.speakerId = speakerId;
		this.status = status;
	}

	/**
	 * Submits a new proposal as a draft.
	 *
	 * @throws InvalidProposalException if the title, description or speaker id is blank
	 */
	public static Proposal submit(String title, String description, String speakerId) {
		requireNonBlank(title, "Proposal title must not be blank");
		requireNonBlank(description, "Proposal description must not be blank");
		requireNonBlank(speakerId, "Proposal speaker id must not be blank");

		return new Proposal(UUID.randomUUID().toString(), title, description, speakerId, ProposalStatus.DRAFT);
	}

	/**
	 * Rehydrates an existing proposal, typically from persistence. No validation is performed:
	 * the invariants were already enforced when the proposal was first submitted.
	 */
	public static Proposal rehydrate(String id, String title, String description, String speakerId, ProposalStatus status) {
		return new Proposal(id, title, description, speakerId, status);
	}

	private static void requireNonBlank(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new InvalidProposalException(message);
		}
	}

	public String id() {
		return id;
	}

	public String title() {
		return title;
	}

	public String description() {
		return description;
	}

	public String speakerId() {
		return speakerId;
	}

	public ProposalStatus status() {
		return status;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Proposal proposal)) return false;
		return Objects.equals(id, proposal.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "Proposal{id='%s', title='%s', status=%s}".formatted(id, title, status);
	}
}
