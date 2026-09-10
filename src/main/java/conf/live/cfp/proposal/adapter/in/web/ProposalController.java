package conf.live.cfp.proposal.adapter.in.web;

import conf.live.cfp.proposal.adapter.in.web.dto.ProposalResponse;
import conf.live.cfp.proposal.adapter.in.web.dto.SubmitProposalRequest;
import conf.live.cfp.proposal.domain.model.Proposal;
import conf.live.cfp.proposal.domain.port.in.SubmitProposalCommand;
import conf.live.cfp.proposal.domain.port.in.SubmitProposalUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Input adapter exposing the proposal use cases over HTTP.
 */
@RestController
@RequestMapping("/api/proposals")
public class ProposalController {

	private final SubmitProposalUseCase submitProposalUseCase;

	public ProposalController(SubmitProposalUseCase submitProposalUseCase) {
		this.submitProposalUseCase = submitProposalUseCase;
	}

	@PostMapping
	public ResponseEntity<ProposalResponse> submit(@Valid @RequestBody SubmitProposalRequest request) {
		Proposal proposal = submitProposalUseCase.submit(new SubmitProposalCommand(request.title(), request.description(), request.speakerId()));
		return ResponseEntity.created(URI.create("/api/proposals/" + proposal.id()))
				.body(ProposalResponse.from(proposal));
	}
}
