package conf.live.cfp.event.adapter.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA persistence model for a {@link conf.live.cfp.event.domain.model.Event}.
 * Kept separate from the domain aggregate so the domain stays free of persistence concerns.
 */
@Entity
@Table(name = "event")
public class EventEntity {

	@Id
	private String id;

	private String name;

	protected EventEntity() {
		// required by JPA
	}

	public EventEntity(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
