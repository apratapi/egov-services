package org.egov.egf.instrument.web.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.egov.common.domain.model.Pagination;
import org.egov.egf.instrument.TestConfiguration;
import org.egov.egf.instrument.domain.model.InstrumentAccountCode;
import org.egov.egf.instrument.domain.model.InstrumentAccountCodeSearch;
import org.egov.egf.instrument.domain.model.InstrumentType;
import org.egov.egf.instrument.domain.service.InstrumentAccountCodeService;
import org.egov.egf.instrument.persistence.queue.repository.InstrumentAccountCodeQueueRepository;
import org.egov.egf.instrument.utils.RequestJsonReader;
import org.egov.egf.instrument.web.requests.InstrumentAccountCodeRequest;
import org.egov.egf.master.web.contract.ChartOfAccountContract;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;

@RunWith(SpringRunner.class)
@WebMvcTest(InstrumentAccountCodeController.class)
@Import(TestConfiguration.class)
public class InstrumentAccountCodeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private InstrumentAccountCodeService instrumentAccountCodeService;

	@MockBean
	private InstrumentAccountCodeQueueRepository instrumentAccountCodeQueueRepository;

	@Captor
	private ArgumentCaptor<InstrumentAccountCodeRequest> captor;

	private RequestJsonReader resources = new RequestJsonReader();

	@Test
	public void test_create_with_kafka() throws IOException, Exception {

		ReflectionTestUtils.setField(InstrumentAccountCodeController.class, "persistThroughKafka", "yes");

		when(instrumentAccountCodeService.fetchAndValidate(any(List.class), any(BindingResult.class),
				any(String.class))).thenReturn(getInstrumentAccountCodes());

		mockMvc.perform(post("/instrumentaccountcodes/_create")
				.content(resources.readRequest("instrumentaccountcode/instrumentaccountcode_create_valid_request.json"))
				.contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(status().is(201))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(content().json(resources
						.readResponse("instrumentaccountcode/instrumentaccountcode_create_valid_response.json")));

		verify(instrumentAccountCodeQueueRepository).addToQue(captor.capture());

		final InstrumentAccountCodeRequest actualRequest = captor.getValue();

		assertEquals("instrumenttype", actualRequest.getInstrumentAccountCodes().get(0).getInstrumentType().getName());
		assertEquals(true, actualRequest.getInstrumentAccountCodes().get(0).getInstrumentType().getActive());
		assertEquals("glcode", actualRequest.getInstrumentAccountCodes().get(0).getAccountCode().getGlcode());
		assertEquals("default", actualRequest.getInstrumentAccountCodes().get(0).getTenantId());
	}

	@Test
	public void test_create_without_kafka() throws IOException, Exception {

		ReflectionTestUtils.setField(InstrumentAccountCodeController.class, "persistThroughKafka", "no");

		when(instrumentAccountCodeService.save(any(List.class), any(BindingResult.class)))
				.thenReturn(getInstrumentAccountCodes());

		mockMvc.perform(post("/instrumentaccountcodes/_create")
				.content(resources.readRequest("instrumentaccountcode/instrumentaccountcode_create_valid_request.json"))
				.contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(status().is(201))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(content().json(resources
						.readResponse("instrumentaccountcode/instrumentaccountcode_create_valid_response.json")));

		verify(instrumentAccountCodeQueueRepository).addToSearchQue(captor.capture());

		final InstrumentAccountCodeRequest actualRequest = captor.getValue();

		assertEquals("instrumenttype", actualRequest.getInstrumentAccountCodes().get(0).getInstrumentType().getName());
		assertEquals(true, actualRequest.getInstrumentAccountCodes().get(0).getInstrumentType().getActive());
		assertEquals("glcode", actualRequest.getInstrumentAccountCodes().get(0).getAccountCode().getGlcode());
		assertEquals("default", actualRequest.getInstrumentAccountCodes().get(0).getTenantId());
	}

	@Test
	public void test_create_error() throws IOException, Exception {

		when(instrumentAccountCodeService.fetchAndValidate(any(List.class), any(BindingResult.class),
				any(String.class))).thenReturn((getInstrumentAccountCodes()));

		mockMvc.perform(post("/instrumentaccountcodes/_create")
				.content(resources
						.readRequest("instrumentaccountcode/instrumentaccountcode_create_invalid_field_value.json"))
				.contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(status().is5xxServerError());

	}

	@Test
	public void test_update_with_kafka() throws IOException, Exception {

		ReflectionTestUtils.setField(InstrumentAccountCodeController.class, "persistThroughKafka", "yes");

		List<InstrumentAccountCode> instrumentAccountCodes = getInstrumentAccountCodes();
		instrumentAccountCodes.get(0).setId("1");

		when(instrumentAccountCodeService.fetchAndValidate(any(List.class), any(BindingResult.class),
				any(String.class))).thenReturn(instrumentAccountCodes);

		mockMvc.perform(post("/instrumentaccountcodes/_update")
				.content(resources.readRequest("instrumentaccountcode/instrumentaccountcode_update_valid_request.json"))
				.contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(status().is(201))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(content().json(resources
						.readResponse("instrumentaccountcode/instrumentaccountcode_update_valid_response.json")));

		verify(instrumentAccountCodeQueueRepository).addToQue(captor.capture());

		final InstrumentAccountCodeRequest actualRequest = captor.getValue();

		assertEquals("instrumenttype", actualRequest.getInstrumentAccountCodes().get(0).getInstrumentType().getName());
		assertEquals(true, actualRequest.getInstrumentAccountCodes().get(0).getInstrumentType().getActive());
		assertEquals("glcode", actualRequest.getInstrumentAccountCodes().get(0).getAccountCode().getGlcode());
		assertEquals("default", actualRequest.getInstrumentAccountCodes().get(0).getTenantId());
	}

	@Test
	public void test_update_without_kafka() throws IOException, Exception {

		ReflectionTestUtils.setField(InstrumentAccountCodeController.class, "persistThroughKafka", "no");

		List<InstrumentAccountCode> instrumentAccountCodes = getInstrumentAccountCodes();
		instrumentAccountCodes.get(0).setId("1");

		when(instrumentAccountCodeService.update(any(List.class), any(BindingResult.class)))
				.thenReturn(instrumentAccountCodes);

		mockMvc.perform(post("/instrumentaccountcodes/_update")
				.content(resources.readRequest("instrumentaccountcode/instrumentaccountcode_update_valid_request.json"))
				.contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(status().is(201))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(content().json(resources
						.readResponse("instrumentaccountcode/instrumentaccountcode_update_valid_response.json")));

		verify(instrumentAccountCodeQueueRepository).addToSearchQue(captor.capture());

		final InstrumentAccountCodeRequest actualRequest = captor.getValue();

		assertEquals("instrumenttype", actualRequest.getInstrumentAccountCodes().get(0).getInstrumentType().getName());
		assertEquals(true, actualRequest.getInstrumentAccountCodes().get(0).getInstrumentType().getActive());
		assertEquals("glcode", actualRequest.getInstrumentAccountCodes().get(0).getAccountCode().getGlcode());
		assertEquals("default", actualRequest.getInstrumentAccountCodes().get(0).getTenantId());
	}

	@Test
	public void test_update_error() throws IOException, Exception {

		when(instrumentAccountCodeService.fetchAndValidate(any(List.class), any(BindingResult.class),
				any(String.class))).thenReturn((getInstrumentAccountCodes()));

		mockMvc.perform(post("/instrumentaccountcodes/_update")
				.content(resources
						.readRequest("instrumentaccountcode/instrumentaccountcode_create_invalid_field_value.json"))
				.contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(status().is5xxServerError());

	}

	@Test
	public void test_search() throws IOException, Exception {

		Pagination<InstrumentAccountCode> page = new Pagination<>();
		page.setTotalPages(1);
		page.setTotalResults(1);
		page.setCurrentPage(0);
		page.setPagedData(getInstrumentAccountCodes());
		page.getPagedData().get(0).setId("1");

		when(instrumentAccountCodeService.search(any(InstrumentAccountCodeSearch.class))).thenReturn(page);

		mockMvc.perform(post("/instrumentaccountcodes/_search").content(resources.getRequestInfo())
				.contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(status().is(200))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(content().json(resources
						.readResponse("instrumentaccountcode/instrumentaccountcode_search_valid_response.json")));

	}

	private List<InstrumentAccountCode> getInstrumentAccountCodes() {
		List<InstrumentAccountCode> instrumentAccountCodes = new ArrayList<InstrumentAccountCode>();
		InstrumentAccountCode instrumentAccountCode = InstrumentAccountCode.builder()
				.instrumentType(InstrumentType.builder().active(true).name("instrumenttype").build())
				.accountCode(ChartOfAccountContract.builder().glcode("glcode").build()).build();
		instrumentAccountCode.setTenantId("default");
		instrumentAccountCodes.add(instrumentAccountCode);
		return instrumentAccountCodes;
	}

}