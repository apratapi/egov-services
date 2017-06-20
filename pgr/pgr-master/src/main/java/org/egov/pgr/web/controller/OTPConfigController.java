/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 * accountability and the service delivery of the government  organizations.
 *
 *  Copyright (C) 2016  eGovernments Foundation
 *
 *  The updated version of eGov suite of products as by eGovernments Foundation
 *  is available at http://www.egovernments.org
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see http://www.gnu.org/licenses/ or
 *  http://www.gnu.org/licenses/gpl.html .
 *
 *  In addition to the terms of the GPL license to be adhered to in using this
 *  program, the following additional terms are to be complied with:
 *
 *      1) All versions of this program, verbatim or modified must carry this
 *         Legal Notice.
 *
 *      2) Any misrepresentation of the origin of the material is prohibited. It
 *         is required that all modified versions of this material be marked in
 *         reasonable ways as different from the original version.
 *
 *      3) This license does not grant any rights to any user of the program
 *         with regards to rights under trademark law for use of the trade names
 *         or trademarks of eGovernments Foundation.
 *
 *  In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */
package org.egov.pgr.web.controller;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ErrorField;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.pgr.config.ApplicationProperties;
import org.egov.pgr.model.AuditDetails;
import org.egov.pgr.service.OTPConfigService;
import org.egov.pgr.util.PgrMasterConstants;
import org.egov.pgr.web.contract.OTPConfig;
import org.egov.pgr.web.contract.OTPConfigGetReq;
import org.egov.pgr.web.contract.OTPConfigReq;
import org.egov.pgr.web.contract.OTPConfigRes;
import org.egov.pgr.web.contract.RequestInfoWrapper;
import org.egov.pgr.web.contract.factory.ResponseInfoFactory;
import org.egov.pgr.web.errorhandlers.Error;
import org.egov.pgr.web.errorhandlers.ErrorHandler;
import org.egov.pgr.web.errorhandlers.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/OTPConfig")
public class OTPConfigController {
	
	private static final Logger logger = LoggerFactory.getLogger(OTPConfigController.class);

	@Autowired
	private ResponseInfoFactory responseInfoFactory;
	
	@Autowired
	private ApplicationProperties applicationProperties;
	
	@Autowired
	private OTPConfigService otpConfigService; 
	
	@Autowired
    private ErrorHandler errHandler;
	
	@PostMapping(value = "/_create")
	@ResponseBody
	public ResponseEntity<?> create(@RequestBody @Valid final OTPConfigReq otpConfigRequest,
			final BindingResult errors) {
		if (errors.hasErrors()) {
			final ErrorResponse errRes = populateErrors(errors);
			return new ResponseEntity<>(errRes, HttpStatus.BAD_REQUEST);
		}
		logger.info("OTP Configuration Request::" + otpConfigRequest);
		final List<ErrorResponse> errorResponses = validateServiceGroupRequest(otpConfigRequest);
		if (!errorResponses.isEmpty())
			return new ResponseEntity<>(errorResponses, HttpStatus.BAD_REQUEST);
		org.egov.pgr.model.OTPConfig otpConfig = contractToModel(otpConfigRequest);
		otpConfigService.createOTPConfig(applicationProperties.getCreateOtpConfigTopicName(), applicationProperties.getCreateOtpConfigTopicKey(), otpConfig); 
		final List<OTPConfig> OTPConfigs = new ArrayList<>();
		OTPConfigs.add(otpConfigRequest.getOtpConfig());
		return getSuccessResponse(OTPConfigs, otpConfigRequest.getRequestInfo());
	}
	
	@PostMapping(value = "/{tenantid}/_update")
	@ResponseBody
	public ResponseEntity<?> update(@RequestBody @Valid final OTPConfigReq otpConfigRequest,
			@PathVariable("tenantid") final String tenantId, final BindingResult errors) {
		if (errors.hasErrors()) {
			final ErrorResponse errRes = populateErrors(errors);
			return new ResponseEntity<>(errRes, HttpStatus.BAD_REQUEST);
		}
		otpConfigRequest.getOtpConfig().setTenantId(tenantId);
		logger.info("OTP Configuration Request::" + otpConfigRequest);
		final List<ErrorResponse> errorResponses = validateServiceGroupRequest(otpConfigRequest);
		if (!errorResponses.isEmpty())
			return new ResponseEntity<>(errorResponses, HttpStatus.BAD_REQUEST);
		org.egov.pgr.model.OTPConfig otpConfig = contractToModel(otpConfigRequest);
		otpConfigService.updateOTPConfig(applicationProperties.getUpdateOtpConfigTopicName(), applicationProperties.getUpdateOtpConfigTopicKey(), otpConfig); 
		final List<OTPConfig> OTPConfigs = new ArrayList<>();
		OTPConfigs.add(otpConfigRequest.getOtpConfig());
		return getSuccessResponse(OTPConfigs, otpConfigRequest.getRequestInfo());
	}
	
	@PostMapping("/_search")
    @ResponseBody
    public ResponseEntity<?> search(@ModelAttribute @Valid final OTPConfigGetReq otpConfigGetRequest,
            final BindingResult modelAttributeBindingResult, @RequestBody @Valid final RequestInfoWrapper requestInfoWrapper,
            final BindingResult requestBodyBindingResult) {
        final RequestInfo requestInfo = requestInfoWrapper.getRequestInfo();

        // validate input params
        if (modelAttributeBindingResult.hasErrors())
            return errHandler.getErrorResponseEntityForMissingParameters(modelAttributeBindingResult, requestInfo);

        // validate input params
        if (requestBodyBindingResult.hasErrors())
            return errHandler.getErrorResponseEntityForMissingRequestInfo(requestBodyBindingResult, requestInfo);

        // Call service
        List<org.egov.pgr.web.contract.OTPConfig> otpConfigList = null;
        try {
        	otpConfigList = otpConfigService.getAllOtpConfig(otpConfigGetRequest.getTenantId());
        } catch (final Exception exception) {
            logger.error("Error while processing request " + otpConfigGetRequest, exception);
            return errHandler.getResponseEntityForUnexpectedErrors(requestInfo);
        }

        return getSuccessResponse(otpConfigList, requestInfo);

    }
	
	private List<ErrorResponse> validateServiceGroupRequest(final OTPConfigReq otpConfigRequest) {
		final List<ErrorResponse> errorResponses = new ArrayList<>();
		final ErrorResponse errorResponse = new ErrorResponse();
		final Error error = getError(otpConfigRequest);
		errorResponse.setError(error);
		if (!errorResponse.getErrorFields().isEmpty())
			errorResponses.add(errorResponse);
		return errorResponses;
	}

	private Error getError(final OTPConfigReq otpConfigRequest) {
		final List<ErrorField> errorFields = getErrorFields(otpConfigRequest);
		return Error.builder().code(HttpStatus.BAD_REQUEST.value())
				.message(PgrMasterConstants.INVALID_ESCALATIONTIMETYPE_REQUEST_MESSAGE).errorFields(errorFields).build();
	}

	private List<ErrorField> getErrorFields(final OTPConfigReq otpConfigRequest) {
		final List<ErrorField> errorFields = new ArrayList<>();
		return errorFields;
	}
	
	private ErrorResponse populateErrors(final BindingResult errors) {
		final ErrorResponse errRes = new ErrorResponse();

		final Error error = new Error();
		error.setCode(1);
		error.setDescription("Error while binding request");
		if (errors.hasFieldErrors())
			for (final FieldError fieldError : errors.getFieldErrors())
				error.getFields().put(fieldError.getField(), fieldError.getRejectedValue());
		errRes.setError(error);
		return errRes;
	}

	private ResponseEntity<?> getSuccessResponse(final List<OTPConfig> otpConfigList, final RequestInfo requestInfo) {
		final OTPConfigRes otpConfigRes = new OTPConfigRes();
		final ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(requestInfo, true);
		responseInfo.setStatus(HttpStatus.OK.toString());
		otpConfigRes.setResponseInfo(responseInfo);
		otpConfigRes.setOtgConfigs(otpConfigList);
		return new ResponseEntity<>(otpConfigRes, HttpStatus.OK);
	}
	
	private org.egov.pgr.model.OTPConfig contractToModel(OTPConfigReq otpConfigRequest){
		AuditDetails auditDetails = new AuditDetails();
		auditDetails.setCreatedBy(otpConfigRequest.getRequestInfo().getUserInfo().getId());
		org.egov.pgr.model.OTPConfig otpConfig = new org.egov.pgr.model.OTPConfig();
		otpConfig.setTenantId(otpConfigRequest.getOtpConfig().getTenantId());
		otpConfig.setOtpConfigEnabled(otpConfigRequest.getOtpConfig().isOtpEnabledForAnonymousComplaint());
		otpConfig.setAuditDetails(auditDetails);
		return otpConfig;
	}


}
