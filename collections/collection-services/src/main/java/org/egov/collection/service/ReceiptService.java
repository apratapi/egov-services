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

package org.egov.collection.service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.egov.collection.config.CollectionServiceConstants;
import org.egov.collection.exception.CustomException;
import org.egov.collection.model.AuditDetails;
import org.egov.collection.model.Instrument;
import org.egov.collection.model.PositionSearchCriteria;
import org.egov.collection.model.PositionSearchCriteriaWrapper;
import org.egov.collection.model.ReceiptCommonModel;
import org.egov.collection.model.ReceiptSearchCriteria;
import org.egov.collection.model.enums.CollectionType;
import org.egov.collection.model.enums.ReceiptStatus;
import org.egov.collection.repository.BillingServiceRepository;
import org.egov.collection.repository.BusinessDetailsRepository;
import org.egov.collection.repository.ChartOfAccountsRepository;
import org.egov.collection.repository.IdGenRepository;
import org.egov.collection.repository.InstrumentRepository;
import org.egov.collection.repository.ReceiptRepository;
import org.egov.collection.web.contract.Bill;
import org.egov.collection.web.contract.BillAccountDetail;
import org.egov.collection.web.contract.BillDetail;
import org.egov.collection.web.contract.BusinessDetailsRequestInfo;
import org.egov.collection.web.contract.BusinessDetailsResponse;
import org.egov.collection.web.contract.ChartOfAccount;
import org.egov.collection.web.contract.Receipt;
import org.egov.collection.web.contract.ReceiptReq;
import org.egov.collection.web.contract.WorkflowDetailsRequest;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


@Service
public class ReceiptService {

	public static final Logger LOGGER = LoggerFactory
			.getLogger(ReceiptService.class);

	@Autowired
	private ReceiptRepository receiptRepository;

	@Autowired
	private BusinessDetailsRepository businessDetailsRepository;

	@Autowired
	private ChartOfAccountsRepository chartOfAccountsRepository;

	@Autowired
	private CollectionApportionerService collectionApportionerService;

	@Autowired
	private BillingServiceRepository billingServiceRepository;

	@Autowired
	private InstrumentRepository instrumentRepository;
	
	@Autowired
	private IdGenRepository idGenRepository;
	
	@Autowired
	private WorkflowService workflowService;

	public ReceiptCommonModel getReceipts(
			ReceiptSearchCriteria receiptSearchCriteria) throws ParseException {
		return receiptRepository
				.findAllReceiptsByCriteria(receiptSearchCriteria);
	}

	public Receipt apportionAndCreateReceipt(ReceiptReq receiptReq) {
		Receipt receipt = receiptReq.getReceipt().get(0);
		Bill bill = receipt.getBill().get(0);
		bill.setBillDetails(apportionPaidAmount(
					receiptReq.getRequestInfo(), bill, receiptReq.getTenantId()));
		// return receiptRepository.pushToQueue(receiptReq); //async call

		receipt = create(bill, receiptReq.getRequestInfo(),
				receiptReq.getTenantId(), receipt.getInstrument()); // sync call
		if(null != receipt.getBill()){
			LOGGER.info("Pushing receipt to kafka queue");
			receiptReq.setReceipt(Arrays.asList(receipt));
			receipt = receiptRepository.pushToQueue(receiptReq);
		}
		return receipt;
	}

	private List<BillDetail> apportionPaidAmount(RequestInfo requestInfo,
			Bill bill, String tenantId) {
		Bill apportionBill = new Bill(bill.getId(), bill.getPayeeName(),
				bill.getPayeeAddress(), bill.getPayeeEmail(),
				bill.getIsActive(), bill.getIsCancelled(), bill.getPaidBy(),
				null, tenantId);
		Boolean callBackForApportion = false;
		List<BillDetail> apportionBillDetails = new ArrayList<>();
		for (BillDetail billDetail : bill.getBillDetails()) {
			if(billDetail.getAmountPaid().longValueExact() > 0){
			BusinessDetailsResponse businessDetailsRes = getBusinessDetails(
					billDetail.getBusinessService(), requestInfo, tenantId);
			if (billDetail.getAmountPaid() != billDetail.getTotalAmount()) {
				if (businessDetailsRes.getBusinessDetails().get(0)
						.getCallBackForApportioning()) {
					apportionBill.getBillDetails().add(billDetail);
					callBackForApportion = true;
				} else {
					billDetail
							.setBillAccountDetails(collectionApportionerService
									.apportionPaidAmount(
											billDetail.getAmountPaid(),
											billDetail.getBillAccountDetails()));
				}
			}
			apportionBillDetails.add(billDetail);
		  }
		}
		if (callBackForApportion) {
			apportionBillDetails.addAll(billingServiceRepository
					.getApportionListFromBillingService(requestInfo,
							apportionBill).getBill().get(0).getBillDetails());
		}
		return apportionBillDetails;
	}

	public boolean validateGLCode(RequestInfo requestInfo, String tenantId,
			BillDetail billdetails) {
		for (BillAccountDetail billAccountDetail : billdetails
				.getBillAccountDetails()) {

			List<ChartOfAccount> chartOfAccounts = chartOfAccountsRepository
					.getChartOfAccounts(
							Arrays.asList(billAccountDetail.getGlcode()),
							tenantId, requestInfo);
			LOGGER.info("chartOfAccount: " + chartOfAccounts);
			if (chartOfAccounts.isEmpty()) {
				LOGGER.error("Glcode invalid!: "
						+ billAccountDetail.getGlcode());
				return false;
			}
		}
		return true;
	}

	public Boolean validateFundAndDept(
			BusinessDetailsResponse businessDetailsRes) {
		BusinessDetailsRequestInfo businessDetails;
		if (null != businessDetailsRes && !businessDetailsRes.getBusinessDetails().isEmpty()) {
			businessDetails = businessDetailsRes.getBusinessDetails().get(0);
			if (null != businessDetails) {
				String fund = businessDetails.getFund();
				String department = businessDetails.getDepartment();
				if (StringUtils.isBlank(fund)) {
					LOGGER.error("Fund is not available");
					return false;
				}
				if (StringUtils.isBlank(department)) {
					LOGGER.error("Department not available");
					return false;
				}
				LOGGER.info("FUND: " + fund + " DEPARTMENT: " + department);
			}
			return true;
		}
		LOGGER.info("Returning false");
		return false;
	}

	private AuditDetails getAuditDetails(User user) {
		AuditDetails auditDetails = new AuditDetails();
		auditDetails.setCreatedBy(user.getId());
		auditDetails.setLastModifiedBy(user.getId());
		auditDetails.setCreatedDate(new Date().getTime());
		auditDetails.setLastModifiedDate(new Date().getTime());
		return auditDetails;
	}

	public Receipt create(Bill bill, RequestInfo requestInfo, String tenantId,
			Instrument instrument) {
		LOGGER.info("Persisting recieptdetail");
		Receipt receipt = new Receipt();
		AuditDetails auditDetail = getAuditDetails(requestInfo.getUserInfo());
        String transactionId = idGenRepository.generateTransactionNumber(requestInfo,tenantId);
		for (BillDetail billDetail : bill.getBillDetails()) {
			if(billDetail.getAmountPaid().longValueExact() > 0){
				String instrumentId = null;
				Long receiptHeaderId = receiptRepository.getNextSeqForRcptHeader();
			try{
				instrumentId = instrumentRepository.createInstrument(
						requestInfo, instrument);
			}catch(Exception e){
				throw new CustomException(Long.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.toString()),
						CollectionServiceConstants.INSTRUMENT_EXCEPTION_MSG, CollectionServiceConstants.INSTRUMENT_EXCEPTION_DESC);
			}
				
				billDetail.setCollectionType(CollectionType.COUNTER);
				billDetail.setStatus(ReceiptStatus.TOBESUBMITTED.toString());
				billDetail.setReceiptDate(new Date().getTime());
				billDetail.setReceiptNumber(idGenRepository.generateReceiptNumber(requestInfo,
						tenantId));
				Map<String, Object> parametersMap;
				BusinessDetailsResponse businessDetailsRes = getBusinessDetails(
						billDetail.getBusinessService(), requestInfo, tenantId);
	
				if (validateFundAndDept(businessDetailsRes)
						&& validateGLCode(requestInfo, tenantId, billDetail)) {
					BusinessDetailsRequestInfo businessDetails = businessDetailsRes
							.getBusinessDetails().get(0);
					parametersMap = prepareReceiptHeader(bill, tenantId,
							auditDetail, billDetail, receiptHeaderId,
							businessDetails);
                    parametersMap.put("transactionid",transactionId);
					LOGGER.info("Rcpt no generated: " + billDetail.getReceiptNumber());
					LOGGER.info("InstrumentId: " + instrumentId
							+ " ReceiptHeaderId: " + receiptHeaderId);
	
					Map<String, Object>[] parametersReceiptDetails = prepareReceiptDetails(
							requestInfo, tenantId, billDetail, receiptHeaderId);
					try {
						receiptRepository.persistReceipt(parametersMap,
								parametersReceiptDetails, receiptHeaderId,
								instrumentId);
					} catch (Exception e) {
						LOGGER.error("Persisting receipt FAILED! ", e);
						return receipt;
					}
					startWokflow(requestInfo, tenantId, receiptHeaderId);
				}
		}
		}
		receipt.setBill(Arrays.asList(bill));
		receipt.setAuditDetails(auditDetail);
        receipt.setTransactionId(transactionId);
		return receipt;
	}

	private Map<String, Object> prepareReceiptHeader(Bill bill,
			String tenantId, AuditDetails auditDetail, BillDetail billDetail,
			Long receiptHeaderId, BusinessDetailsRequestInfo businessDetails) {
		final Map<String, Object> parametersMap = new HashMap<>();
		String collectionModesNotAllowed = billDetail
				.getCollectionModesNotAllowed().toString().replace("[", "");
		collectionModesNotAllowed = collectionModesNotAllowed.replace("]", "");
		parametersMap.put("id", receiptHeaderId);
		parametersMap.put("payeename", bill.getPayeeName());
		parametersMap.put("payeeaddress", bill.getPayeeAddress());
		parametersMap.put("payeeemail", bill.getPayeeEmail());
		parametersMap.put("paidby", bill.getPaidBy());
		parametersMap.put("referencenumber", billDetail.getBillNumber());
		parametersMap.put("receipttype", businessDetails.getBusinessType());
		parametersMap.put("receiptdate", billDetail.getReceiptDate());
		parametersMap.put("receiptnumber", billDetail.getReceiptNumber());
		parametersMap.put("businessdetails", billDetail.getBusinessService());
		parametersMap.put("collectiontype", billDetail.getCollectionType()
				.toString());
		parametersMap.put("reasonforcancellation",
				billDetail.getReasonForCancellation());
		parametersMap.put("minimumamount", billDetail.getMinimumAmount());
		parametersMap.put("totalamount", billDetail.getAmountPaid());
		parametersMap.put("collmodesnotallwd",collectionModesNotAllowed);
		parametersMap.put("consumercode", billDetail.getConsumerCode());
		parametersMap.put("channel", billDetail.getChannel());
		parametersMap.put("fund", businessDetails.getFund());
		parametersMap.put("fundsource", businessDetails.getFundSource());
		parametersMap.put("function", businessDetails.getFunction());
		parametersMap.put("department", businessDetails.getDepartment());
		parametersMap.put("boundary", billDetail.getBoundary());
		parametersMap.put("voucherheader", billDetail.getVoucherHeader());
		// Deposited bank need not be persisted for every receipt it exists only
		// in few use cases
		parametersMap.put("depositedbranch", null);
		parametersMap.put("createdby", auditDetail.getCreatedBy());
		parametersMap.put("createddate", auditDetail.getCreatedDate());
		parametersMap.put("lastmodifiedby", auditDetail.getLastModifiedBy());
		parametersMap
				.put("lastmodifieddate", auditDetail.getLastModifiedDate());
		parametersMap.put("tenantid", tenantId);
		parametersMap.put("referencedate", billDetail.getBillDate());
		parametersMap.put("referencedesc", billDetail.getBillDescription());
		parametersMap.put("manualreceiptnumber", null);
		parametersMap.put("manualreceiptdate", null);
		parametersMap.put("reference_ch_id", null);
		parametersMap.put("stateid", null);
		parametersMap.put("location", null);
		parametersMap.put("isreconciled", false);
		parametersMap.put("status", billDetail.getStatus());
		return parametersMap;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object>[] prepareReceiptDetails(
			RequestInfo requestInfo, String tenantId, BillDetail billDetail,
			Long receiptHeaderId) {
		Map<String, Object>[] parametersReceiptDetails = new Map[billDetail
				.getBillAccountDetails().size()];
		int parametersReceiptDetailsCount = 0;
		for (BillAccountDetail billAccountDetails : billDetail
				.getBillAccountDetails()) {
			final Map<String, Object> parameterMap = new HashMap<>();
			List<ChartOfAccount> chartOfAccounts = chartOfAccountsRepository
					.getChartOfAccounts(
							Arrays.asList(billAccountDetails.getGlcode()),
							tenantId, requestInfo);
			if (!chartOfAccounts.isEmpty()) {
				parameterMap.put("chartofaccount",
						billAccountDetails.getGlcode());
				parameterMap.put("dramount",
						billAccountDetails.getDebitAmount());
				parameterMap.put("cramount",
						billAccountDetails.getCreditAmount());
				parameterMap.put("ordernumber", billAccountDetails.getOrder());
				parameterMap.put("receiptheader", receiptHeaderId);
				parameterMap.put("actualcramounttobepaid",
						billAccountDetails.getCrAmountToBePaid());
				parameterMap.put("description",
						billAccountDetails.getAccountDescription());
				parameterMap.put("financialyear", null);
				parameterMap.put("isactualdemand",
						billAccountDetails.getIsActualDemand());
				parameterMap.put("purpose", billAccountDetails.getPurpose()
						.toString());
				parameterMap.put("tenantid", tenantId);
				parameterMap.put("receiptheader", receiptHeaderId);

				parametersReceiptDetails[parametersReceiptDetailsCount] = parameterMap;
				parametersReceiptDetailsCount++;
			} else {
				LOGGER.info("Glcode invalid, Hence record not inserted for COA/Gl Code: "
						+ billAccountDetails.getGlcode());
				break;
			}
		}
		return parametersReceiptDetails;
	}

	public BusinessDetailsResponse getBusinessDetails(
			String businessDetailsCode, RequestInfo requestInfo, String tenantId) {
		LOGGER.info("Searching for fund aand other businessDetails based on code.");
		BusinessDetailsResponse businessDetailsResponse = new BusinessDetailsResponse();
		try {
			businessDetailsResponse = businessDetailsRepository
					.getBusinessDetails(Arrays.asList(businessDetailsCode),
							tenantId, requestInfo);
		} catch (Exception e) {
			throw new CustomException(Long.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.toString()),
					CollectionServiceConstants.BUSINESSDETAILS_EXCEPTION_MSG, CollectionServiceConstants.BUSINESSDETAILS_EXCEPTION_DESC);

		}
		if(businessDetailsResponse.getBusinessDetails().isEmpty()){
			throw new CustomException(Long.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.toString()),
					CollectionServiceConstants.BUSINESSDETAILS_EXCEPTION_MSG, CollectionServiceConstants.BUSINESSDETAILS_EXCEPTION_DESC);
		}
		LOGGER.info("Response from coll-master: " + businessDetailsResponse);
		return businessDetailsResponse;
	}

	public Boolean checkVoucherCreation(Boolean voucherCreation,
			Date voucherCutoffDate, Date receiptDate) {
		Boolean createVoucherForBillingService = Boolean.FALSE;
		if (voucherCutoffDate != null
				&& receiptDate.compareTo(voucherCutoffDate) > 0) {
			if (voucherCreation != null)
				createVoucherForBillingService = voucherCreation;
		} else if (voucherCutoffDate == null && voucherCreation != null)
			createVoucherForBillingService = voucherCreation;
		return createVoucherForBillingService;
	}

	public List<Receipt> cancelReceiptBeforeRemittance(ReceiptReq receiptRequest) {
		ReceiptReq request = receiptRepository.cancelReceipt(receiptRequest);
		return request.getReceipt();
	}

	public List<Receipt> cancelReceiptPushToQueue(ReceiptReq receiptRequest) {
		LOGGER.info("Pushing recieptdetails to kafka queue");
		return receiptRepository
				.pushReceiptCancelDetailsToQueue(receiptRequest);
	}

	public WorkflowDetailsRequest updateStateId(WorkflowDetailsRequest workflowDetailsRequest) {
		LOGGER.info("WorkflowDetailsRequest: " + workflowDetailsRequest.toString());
		try {
			pushUpdateReceiptDetailsToQueque(workflowDetailsRequest);
		} catch (Exception e) {
			LOGGER.error("Couldn't update stateId and status" + e);
			return null;
		}
		return workflowDetailsRequest;
	}

	public List<User> getReceiptCreators(final RequestInfo requestInfo,
			final String tenantId) {
		return receiptRepository.getReceiptCreators(requestInfo, tenantId);
	}

	public List<String> getReceiptStatus(final String tenantId) {
		return receiptRepository.getReceiptStatus(tenantId);
	}

	public void updateReceipt(WorkflowDetailsRequest workflowDetails){
		receiptRepository.updateReceipt(workflowDetails);
	}

	public List<BusinessDetailsRequestInfo> getBusinessDetails(
			final String tenantId, final RequestInfo requestInfo) {
		return receiptRepository.getBusinessDetails(requestInfo, tenantId);
	}

	public List<ChartOfAccount> getChartOfAccountsForByGlCodes(
			final String tenantId, final RequestInfo requestInfo) {
		return receiptRepository.getChartOfAccounts(tenantId, requestInfo);
	}
	
	public void pushUpdateReceiptDetailsToQueque(WorkflowDetailsRequest workFlowDetailsRequest) {
		   LOGGER.info("WorkflowDetailsRequest :"+workFlowDetailsRequest);
			receiptRepository.pushUpdateDetailsToQueque(workFlowDetailsRequest);
			}

	private void startWokflow(RequestInfo requestInfo, String tenantId, Long receiptHeaderId){
		LOGGER.info("Internally triggering workflow for receipt: "+receiptHeaderId);
		
		WorkflowDetailsRequest workflowDetails = new WorkflowDetailsRequest();
		
		workflowDetails.setReceiptHeaderId(receiptHeaderId);
		workflowDetails.setTenantId(tenantId);
		workflowDetails.setState("NEW");
		workflowDetails.setAction("Create");
		workflowDetails.setAssignee(requestInfo.getUserInfo().getId());
		workflowDetails.setInitiatorPosition(requestInfo.getUserInfo().getId());
		workflowDetails.setRequestInfo(requestInfo);
		
		PositionSearchCriteriaWrapper positionSearchCriteriaWrapper = new PositionSearchCriteriaWrapper();
		PositionSearchCriteria positionSearchCriteria = new PositionSearchCriteria();
		positionSearchCriteria.setEmployeeId(requestInfo.getUserInfo().getId());
		positionSearchCriteria.setTenantId(tenantId);
		positionSearchCriteriaWrapper.setPositionSearchCriteria(positionSearchCriteria);
		positionSearchCriteriaWrapper.setRequestInfo(requestInfo);
		
		
		try{
	//		workflowDetails.setAssignee(workflowService.getPositionForUser(positionSearchCriteriaWrapper));	
	//		workflowDetails.setInitiatorPosition(workflowService.getPositionForUser(positionSearchCriteriaWrapper));
			workflowService.start(workflowDetails);
		}catch(Exception e){
			LOGGER.error("Starting workflow failed: "+e.getCause());
		}
		
		
	}

}