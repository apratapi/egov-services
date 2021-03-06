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

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ErrorField;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.pgr.config.ApplicationProperties;
import org.egov.pgr.domain.model.GrievanceType;
import org.egov.pgr.service.GrievanceTypeService;
import org.egov.pgr.util.PgrMasterConstants;
import org.egov.pgr.web.contract.RequestInfoWrapper;
import org.egov.pgr.web.contract.ServiceGetRequest;
import org.egov.pgr.web.contract.ServiceRequest;
import org.egov.pgr.web.contract.ServiceResponse;
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
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/service")
public class GrievanceTypeController {

    private static final Logger logger = LoggerFactory.getLogger(GrievanceTypeController.class);

    @Autowired
    private GrievanceTypeService grievanceTypeService;

    @Autowired
    private ErrorHandler errHandler;

    @Autowired
    private ResponseInfoFactory responseInfoFactory;

    @Autowired
    private ApplicationProperties applicationProperties;

    @PostMapping(value = "/v1/_create")
    @ResponseBody
    public ResponseEntity<?> create(@RequestBody @Valid final ServiceRequest serviceTypeRequest,
                                    final BindingResult errors) {
        if (errors.hasErrors()) {
            final ErrorResponse errRes = populateErrors(errors);
            return new ResponseEntity<>(errRes, HttpStatus.BAD_REQUEST);
        }
        logger.info("Service Create : Request::" + serviceTypeRequest);

        final List<ErrorResponse> errorResponses = validateServiceRequest(serviceTypeRequest);
        if (!errorResponses.isEmpty())
            return new ResponseEntity<>(errorResponses, HttpStatus.BAD_REQUEST);

        final GrievanceType grievanceType = grievanceTypeService.createServiceType(applicationProperties.getCreateGrievanceTypeTopicName(), applicationProperties.getCreateGrievanceTypeTopicKey(), serviceTypeRequest);
        final List<GrievanceType> grievanceTypes = new ArrayList<>();
        grievanceTypes.add(grievanceType);
        return getSuccessResponse(grievanceTypes, serviceTypeRequest.getRequestInfo());

    }

    @PostMapping(value = "/v1/_update")
    @ResponseBody
    public ResponseEntity<?> update(@RequestBody @Valid final ServiceRequest serviceTypeRequest,
                                    final BindingResult errors) {
        if (errors.hasErrors()) {
            final ErrorResponse errRes = populateErrors(errors);
            return new ResponseEntity<>(errRes, HttpStatus.BAD_REQUEST);
        }
        logger.info("Service Update : Request::" + serviceTypeRequest);
        if (serviceTypeRequest.getService().getServiceCode() == null || serviceTypeRequest.getService().getServiceCode().equals("")) {
            final ErrorResponse errRes = populateErrors(errors);
            return new ResponseEntity<>(errRes, HttpStatus.BAD_REQUEST);
        }
        final List<ErrorResponse> errorResponses = validateUpdateServiceRequest(serviceTypeRequest);
        if (!errorResponses.isEmpty())
            return new ResponseEntity<>(errorResponses, HttpStatus.BAD_REQUEST);

        final GrievanceType service = grievanceTypeService.updateServices(applicationProperties.getUpdateGrievanceTypeTopicName(), applicationProperties.getUpdateGrievanceTypeTopicKey(), serviceTypeRequest);
        final List<GrievanceType> services = new ArrayList<>();
        services.add(service);
        return getSuccessResponse(services, serviceTypeRequest.getRequestInfo());
    }

    @PostMapping("/v1/_search")
    @ResponseBody
    public ResponseEntity<?> search(@ModelAttribute @Valid final ServiceGetRequest serviceTypeGetRequest,
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
        List<GrievanceType> grievanceTypeList = null;
        try {
            grievanceTypeList = grievanceTypeService.getServiceTypes(serviceTypeGetRequest);
        } catch (final Exception exception) {
            logger.error("Error while processing request " + serviceTypeGetRequest, exception);
            return errHandler.getResponseEntityForUnexpectedErrors(requestInfo);
        }

        return getSuccessResponse(grievanceTypeList, requestInfo);

    }

    private List<ErrorResponse> validateServiceRequest(final ServiceRequest serviceTypeRequest) {
        final List<ErrorResponse> errorResponses = new ArrayList<>();
        final ErrorResponse errorResponse = new ErrorResponse();
        final List<ErrorField> errorFields = getErrorFields(serviceTypeRequest);
        final Error error = Error.builder().code(HttpStatus.BAD_REQUEST.value())
                .message(PgrMasterConstants.INVALID_SERVICETYPE_REQUEST_MESSAGE).errorFields(errorFields)
                .build();
        errorResponse.setError(error);
        if (!errorResponse.getErrorFields().isEmpty())
            errorResponses.add(errorResponse);
        return errorResponses;
    }

    private List<ErrorResponse> validateUpdateServiceRequest(final ServiceRequest serviceTypeRequest) {
        final List<ErrorResponse> errorResponses = new ArrayList<>();
        final ErrorResponse errorResponse = new ErrorResponse();
        final List<ErrorField> errorFields = getUpdateErrorFields(serviceTypeRequest);
        final Error error = Error.builder().code(HttpStatus.BAD_REQUEST.value())
                .message(PgrMasterConstants.INVALID_SERVICETYPE_REQUEST_MESSAGE).errorFields(errorFields)
                .build();
        errorResponse.setError(error);
        if (!errorResponse.getErrorFields().isEmpty())
            errorResponses.add(errorResponse);
        return errorResponses;
    }


    private List<ErrorField> getErrorFields(final ServiceRequest serviceTypeRequest) {
        final List<ErrorField> errorFields = new ArrayList<>();
        addGrievanceNameValidationErrors(serviceTypeRequest, errorFields);
        addGrievanceNameValidator(serviceTypeRequest, errorFields);
        addTeanantIdValidationErrors(serviceTypeRequest, errorFields);
        checkMetadataExists(serviceTypeRequest, errorFields);
        checkCategorySLAValues(serviceTypeRequest, errorFields);
        checkServiceCodeExists(serviceTypeRequest, errorFields);
        return errorFields;
    }

    private List<ErrorField> getUpdateErrorFields(final ServiceRequest serviceTypeRequest) {
        final List<ErrorField> errorFields = new ArrayList<>();
        addGrievanceNameValidationErrors(serviceTypeRequest, errorFields);
        addGrievanceNameValidator(serviceTypeRequest, errorFields);
        addTeanantIdValidationErrors(serviceTypeRequest, errorFields);
        checkMetadataExists(serviceTypeRequest, errorFields);
        checkCategorySLAValues(serviceTypeRequest, errorFields);
        return errorFields;
    }

    private void addGrievanceNameValidationErrors(final ServiceRequest serviceTypeRequest,
                                                  final List<ErrorField> errorFields) {
        final GrievanceType service = serviceTypeRequest.getService();
        if (service.getServiceName() == null || service.getServiceName().isEmpty()) {
            final ErrorField errorField = ErrorField.builder()
                    .code(PgrMasterConstants.GRIEVANCETYPE_NAME_MANDATORY_CODE)
                    .message(PgrMasterConstants.GRIEVANCETYPE_NAME_MANADATORY_ERROR_MESSAGE)
                    .field(PgrMasterConstants.GRIEVANCETYPE_NAME_MANADATORY_FIELD_NAME).build();
            errorFields.add(errorField);
        }
        return;
    }

    private void addGrievanceNameValidator(final ServiceRequest serviceTypeRequest,
                                           final List<ErrorField> errorFields) {
        if (errorFields.size() == 0) {
            if (grievanceTypeService.checkComplaintNameIfExists(serviceTypeRequest.getService().getServiceName(),
                    serviceTypeRequest.getService().getTenantId(),serviceTypeRequest.getService().getServiceCode())) {
                final ErrorField errorField = ErrorField.builder()
                        .code(PgrMasterConstants.GRIEVANCETYPE_NAME_UNIQUE_CODE)
                        .message(PgrMasterConstants.GRIEVANCETYPE_NAME_UNIQUE_ERROR_MESSAGE)
                        .field(PgrMasterConstants.GRIEVANCETYPE_NAME_UNIQUE_FIELD_NAME).build();
                errorFields.add(errorField);
            }
        }
    }

    private void addTeanantIdValidationErrors(final ServiceRequest serviceTypeRequest,
                                              final List<ErrorField> errorFields) {
        final GrievanceType grievanceType = serviceTypeRequest.getService();
        if (grievanceType.getTenantId() == null || grievanceType.getTenantId().isEmpty()) {
            final ErrorField errorField = ErrorField.builder()
                    .code(PgrMasterConstants.TENANTID_MANDATORY_CODE)
                    .message(PgrMasterConstants.TENANTID_MANADATORY_ERROR_MESSAGE)
                    .field(PgrMasterConstants.TENANTID_MANADATORY_FIELD_NAME)
                    .build();
            errorFields.add(errorField);
        } else
            return;
    }

    private void checkMetadataExists(final ServiceRequest serviceTypeRequest,
                                     final List<ErrorField> errorFields) {
        final GrievanceType grievanceType = serviceTypeRequest.getService();
        if (grievanceType.isMetadata()) {
            if (null == grievanceType.getAttributes() || grievanceType.getAttributes().size() <= 0) {
                final ErrorField errorField = ErrorField.builder().code(PgrMasterConstants.ATTRIBUTE_DETAILS_MANDATORY_CODE)
                        .message(PgrMasterConstants.ATTRIBUTE_DETAILS_MANADATORY_ERROR_MESSAGE)
                        .field(PgrMasterConstants.ATTRIBUTE_DETAILS_MANADATORY_FIELD_NAME).build();
                errorFields.add(errorField);
            }
        } else
            return;
    }

    private void checkCategorySLAValues(final ServiceRequest serviceTypeRequest,
                                        final List<ErrorField> errorFields) {
        final GrievanceType grievanceType = serviceTypeRequest.getService();
        if (null == grievanceType.getCategory()) {
            final ErrorField errorField = ErrorField.builder()
                    .code(PgrMasterConstants.CATEGORY_ID_MANDATORY_CODE)
                    .message(PgrMasterConstants.CATEGORY_ID_MANDATORY_ERROR_MESSAGE)
                    .field(PgrMasterConstants.CATEGORY_ID_MANDATORY_FIELD_NAME)
                    .build();
            errorFields.add(errorField);
        } else if (null == grievanceType.getSlaHours()) {
            final ErrorField errorField = ErrorField.builder()
                    .code(PgrMasterConstants.SLA_HOURS_MANDATORY_CODE)
                    .message(PgrMasterConstants.SLA_HOURS_MANDATORY_ERROR_MESSAGE)
                    .field(PgrMasterConstants.SLA_HOURS_MANDATORY_FIELD_NAME)
                    .build();
            errorFields.add(errorField);
        }
        return;
    }

    private void checkServiceCodeExists(final ServiceRequest serviceTypeRequest,
                                        final List<ErrorField> errorFields) {
        final GrievanceType grievanceType = serviceTypeRequest.getService();
        if (grievanceTypeService.checkServiceCodeIfExists(grievanceType.getServiceCode(), grievanceType.getTenantId())) {
            final ErrorField errorField = ErrorField.builder()
                    .code(PgrMasterConstants.SERVICETYPE_TENANTID_NAME_UNIQUE_CODE)
                    .message(PgrMasterConstants.SERVICETYPE_TENANTID_NAME_UNIQUE_ERROR_MESSAGE)
                    .field(PgrMasterConstants.SERVICETYPE_TENANTID_NAME_UNIQUE_FIELD_NAME)
                    .build();
            errorFields.add(errorField);
        }
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

    private ResponseEntity<?> getSuccessResponse(final List<GrievanceType> grievanceTypeList, final RequestInfo requestInfo) {
        final ServiceResponse serviceTypeResponse = new ServiceResponse();
        serviceTypeResponse.setServices(grievanceTypeList);
        final ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(requestInfo, true);
        responseInfo.setStatus(HttpStatus.OK.toString());
        serviceTypeResponse.setResponseInfo(responseInfo);
        return new ResponseEntity<>(serviceTypeResponse, HttpStatus.OK);

    }

}
