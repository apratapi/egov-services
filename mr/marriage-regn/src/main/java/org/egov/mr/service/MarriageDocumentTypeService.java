package org.egov.mr.service;

import java.util.ArrayList;
import java.util.List;

import org.egov.mr.config.PropertiesManager;
import org.egov.mr.model.MarriageDocumentType;
import org.egov.mr.repository.MarriageDocumentTypeRepository;
import org.egov.mr.util.SequenceIdGenService;
import org.egov.mr.web.contract.MarriageDocTypeRequest;
import org.egov.mr.web.contract.MarriageDocTypeResponse;
import org.egov.mr.web.contract.MarriageDocumentTypeSearchCriteria;
import org.egov.mr.web.contract.RequestInfo;
import org.egov.mr.web.contract.ResponseInfo;
import org.egov.tracer.kafka.LogAwareKafkaTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MarriageDocumentTypeService {

	@Autowired
	private MarriageDocumentTypeRepository marriageDocumentTypeRepository;

	@Autowired
	private LogAwareKafkaTemplate<String, Object> kafkaTemplate;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private SequenceIdGenService sequenceIdGenService;

	public ResponseEntity<?> search(MarriageDocumentTypeSearchCriteria marriageDocumentTypeSearchCriteria,
			RequestInfo requestInfo) {
		List<MarriageDocumentType> marriageDocTypesList = marriageDocumentTypeRepository
				.search(marriageDocumentTypeSearchCriteria);
		return getSuccessResponse(marriageDocTypesList, requestInfo);
	}

	// CreateAsync Kafka
	public ResponseEntity<?> createAsync(MarriageDocTypeRequest marriageDocTypeRequest) {
		log.info("Service:: MarriageDocTypeRequest: " + marriageDocTypeRequest);
		RequestInfo requestInfo = marriageDocTypeRequest.getRequestInfo();
		List<MarriageDocumentType> marriageDocumentTypes = marriageDocTypeRequest.getMarriageDocTypes();
		Integer size = marriageDocumentTypes.size();

		// Get Ids From SequenceGenService
		List<Long> ids = sequenceIdGenService.idSeqGen(size, "seq_marriage_document_type");
		for (int index = 0; index < size; index++) {
			marriageDocumentTypes.get(index).setId(ids.get(index));
		}
		log.info("Service after IDs set:: MarriageDocTypeRequest: " + marriageDocTypeRequest);
		kafkaTemplate.send(propertiesManager.getCreateMarriageDocumentTypeTopicName(), marriageDocTypeRequest);
		return getSuccessResponse(marriageDocTypeRequest.getMarriageDocTypes(), requestInfo);
	}

	// UpdateAsync Kafka
	public ResponseEntity<?> updateAsync(MarriageDocTypeRequest marriageDocTypeRequest) {
		log.info("Service:: MarriageDocTypeRequest: " + marriageDocTypeRequest);
		RequestInfo requestInfo = marriageDocTypeRequest.getRequestInfo();
		List<Long> ids = marriageDocumentTypeRepository.getIds(marriageDocTypeRequest.getMarriageDocTypes());

		List<MarriageDocumentType> updateMarriageDocumentType = new ArrayList<>();
		List<MarriageDocumentType> insertMarriageDocumentType = new ArrayList<>();

		for (MarriageDocumentType marriageDocumentType : marriageDocTypeRequest.getMarriageDocTypes()) {
			if (ids.contains(marriageDocumentType.getId())) {
				updateMarriageDocumentType.add(marriageDocumentType);
			} else {
				insertMarriageDocumentType.add(marriageDocumentType);
			}
		}
		if (!insertMarriageDocumentType.isEmpty() && insertMarriageDocumentType != null) {
			MarriageDocTypeRequest mdtReq = new MarriageDocTypeRequest();
			mdtReq.setRequestInfo(requestInfo);
			mdtReq.setMarriageDocTypes(insertMarriageDocumentType);
			createAsync(mdtReq);
		}
		log.info("Service after IDs set:: MarriageDocTypeRequest: " + marriageDocTypeRequest);
		kafkaTemplate.send(propertiesManager.getUpdateMarriageDocumentTypeTopicName(), marriageDocTypeRequest);
		return getSuccessResponse(marriageDocTypeRequest.getMarriageDocTypes(), requestInfo);
	}

	// from Consumer
	public void create(MarriageDocTypeRequest marriageDocTypeRequest) {
		marriageDocumentTypeRepository.create(marriageDocTypeRequest.getMarriageDocTypes());
	}

	public void update(MarriageDocTypeRequest marriageDocTypeRequest) {
		marriageDocumentTypeRepository.update(marriageDocTypeRequest.getMarriageDocTypes());
	}

	// returning the Response to method in the same class
	private ResponseEntity<?> getSuccessResponse(List<MarriageDocumentType> marriageDocTypesList,
			RequestInfo requestInfo) {
		// Setting ResponseInfo
		ResponseInfo responseInfo = new ResponseInfo();
		responseInfo.setApiId(requestInfo.getApiId());
		responseInfo.setKey(requestInfo.getKey());
		responseInfo.setResMsgId(requestInfo.getMsgId());
		responseInfo.setStatus(HttpStatus.OK.toString());
		responseInfo.setTenantId(requestInfo.getTenantId());
		responseInfo.setTs(requestInfo.getTs());
		responseInfo.setVer(requestInfo.getVer());

		// Setting regnUnitResponse responseInfo
		MarriageDocTypeResponse marriageDocTypeResponse = new MarriageDocTypeResponse();
		marriageDocTypeResponse.setResponseInfo(responseInfo);
		// Setting regnUnitResponse registrationUnitsList
		marriageDocTypeResponse.setMarriageDocTypes(marriageDocTypesList);
		return new ResponseEntity<MarriageDocTypeResponse>(marriageDocTypeResponse, HttpStatus.OK);
	}
}
