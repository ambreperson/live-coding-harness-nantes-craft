package conf.live.cfp.proposal.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link ProposalEntity}.
 */
public interface ProposalJpaRepository extends JpaRepository<ProposalEntity, String> {
}
