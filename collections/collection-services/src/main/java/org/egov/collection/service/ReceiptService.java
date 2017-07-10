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

import java.util.List;

import org.egov.collection.model.ReceiptHeader;
import org.egov.collection.model.ReceiptSearchCriteria;
import org.egov.collection.repository.ReceiptRepository;
import org.egov.collection.web.contract.BillDetail;
import org.egov.collection.web.contract.BillDetailsWrapper;
import org.egov.collection.web.contract.Receipt;
import org.egov.collection.web.contract.ReceiptReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jayway.jsonpath.JsonPath;


@Service
public class ReceiptService {

	public static final Logger logger = LoggerFactory.getLogger(ReceiptService.class);

	
	@Autowired
	private ReceiptRepository receiptRepository;
	

	public List<ReceiptHeader> getReceipts
	(ReceiptSearchCriteria receiptSearchCriteria) {
		return receiptRepository.find(receiptSearchCriteria);
	}
	
	public Receipt pushToQueue(ReceiptReq receiptReq){
		logger.info("Pushing recieptdetail to kafka queue");
		
		for(BillDetailsWrapper billdetails: receiptReq.getReceipt().getBillInfoWrapper().getBillInfo().getBillDetailsWrapper()){	
			String receiptNumber = receiptRepository.generateReceiptNumber(receiptReq);
			logger.info("Receipt Number generated is: "+receiptNumber);
			billdetails.setReceiptNumber(receiptNumber);
			Object businessDetails = receiptRepository.getBusinessDetails(billdetails.getBusinessDetailsCode(), receiptReq);
			String fund = null;
			String fundSource = null;
			String function = null;
			String department = null;
			try{
				fund = JsonPath.read(businessDetails, "$.BusinessDetailsInfo[0].fund");
				fundSource = JsonPath.read(businessDetails, "$.BusinessDetailsInfo[0].fundSource");
				function= JsonPath.read(businessDetails, "$.BusinessDetailsInfo[0].function");
				department = JsonPath.read(businessDetails, "$.BusinessDetailsInfo[0].department");
			}catch(Exception e){
	        	logger.info("FUND: "+fund+" FUNDSOURCE: "+fundSource+" FUNCTION: "+function+" DEPARTMENT: "+department);
				logger.error("All business details fields are not available: "+e.getCause());
				return null;
			}
        	logger.info("FUND: "+fund+" FUNDSOURCE: "+fundSource+" FUNCTION: "+function+" DEPARTMENT: "+department);
		}	
		return receiptRepository.pushToQueue(receiptReq);
	}
	
	public Receipt create(ReceiptReq receiptReq){
		logger.info("Persisting recieptdetail");
		try{
			receiptRepository.persistCreateRequest(receiptReq);
		}catch(Exception e){
			logger.error("Exception caused at the Repository layer: "+e.getCause());
		}

		return receiptReq.getReceipt();
	}
	
}
	