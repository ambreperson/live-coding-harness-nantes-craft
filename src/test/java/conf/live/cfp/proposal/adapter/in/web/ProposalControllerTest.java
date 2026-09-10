package conf.live.cfp.proposal.adapter.in.web;

import conf.live.cfp.proposal.domain.model.InvalidProposalException;
import conf.live.cfp.proposal.domain.model.Proposal;
import conf.live.cfp.proposal.domain.port.in.SubmitProposalCommand;
import conf.live.cfp.proposal.domain.port.in.SubmitProposalUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProposalController.class)
class ProposalControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private SubmitProposalUseCase submitProposalUseCase;

	@Test
	void should_return_201_with_the_created_proposal_when_the_request_is_valid() throws Exception {
		Proposal created = Proposal.rehydrate("proposal-1", "Title", "Description", "speaker-1", conf.live.cfp.proposal.domain.model.ProposalStatus.DRAFT, "event-1");
		when(submitProposalUseCase.submit(any())).thenReturn(created);

		mockMvc.perform(post("/api/proposals")
						.contentType("application/json")
						.content("""
								{
								  "title": "Title",
								  "description": "Description",
								  "speakerId": "speaker-1"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/api/proposals/proposal-1"))
				.andExpect(jsonPath("$.id").value("proposal-1"))
				.andExpect(jsonPath("$.title").value("Title"))
				.andExpect(jsonPath("$.description").value("Description"))
				.andExpect(jsonPath("$.speakerId").value("speaker-1"))
				.andExpect(jsonPath("$.status").value("DRAFT"));

		verify(submitProposalUseCase).submit(eq(new SubmitProposalCommand("Title", "Description", "speaker-1", null)));
	}

	@Test
	void should_return_400_when_the_title_is_blank() throws Exception {
		mockMvc.perform(post("/api/proposals")
						.contentType("application/json")
						.content("""
								{
								  "title": "",
								  "description": "Description",
								  "speakerId": "speaker-1"
								}
								"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void should_return_400_with_the_domain_message_when_the_use_case_rejects_the_proposal() throws Exception {
		when(submitProposalUseCase.submit(any())).thenThrow(new InvalidProposalException("Proposal speaker id must not be blank"));

		mockMvc.perform(post("/api/proposals")
						.contentType("application/json")
						.content("""
								{
								  "title": "Title",
								  "description": "Description",
								  "speakerId": "speaker-1"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Proposal speaker id must not be blank"));
	}
}
