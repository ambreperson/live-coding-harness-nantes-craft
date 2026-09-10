package conf.live.cfp.event.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link EventEntity}.
 */
public interface EventJpaRepository extends JpaRepository<EventEntity, String> {
}
