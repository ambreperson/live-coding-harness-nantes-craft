package conf.live.cfp.proposal.adapter.out.persistence;

import conf.live.cfp.proposal.domain.model.ProposalStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA persistence model for a {@link conf.live.cfp.proposal.domain.model.Proposal}.
 * Kept separate from the domain aggregate so the domain stays free of persistence concerns.
 */
@Entity
@Table(name = "proposal")
public class ProposalEntity {

	@Id
	private String id;

	private String title;

	private String description;

	private String speakerId;

	@Enumerated(EnumType.STRING)
	private ProposalStatus status;

	protected ProposalEntity() {
		// required by JPA
	}

	public ProposalEntity(String id, String title, String description, String speakerId, ProposalStatus status) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.speakerId = speakerId;
		this.status = status;
	}

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getSpeakerId() {
		return speakerId;
	}

	public ProposalStatus getStatus() {
		return status;
	}
}
