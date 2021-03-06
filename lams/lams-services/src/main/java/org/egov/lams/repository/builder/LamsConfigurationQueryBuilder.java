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

package org.egov.lams.repository.builder;

import java.util.List;

import org.egov.lams.web.contract.LamsConfigurationGetRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LamsConfigurationQueryBuilder {

	private static final Logger logger = LoggerFactory.getLogger(LamsConfigurationQueryBuilder.class);


	private static final String BASE_QUERY = "SELECT ck.keyName as key, cv.value as value"
			+ " FROM eglams_lamsConfiguration ck JOIN eglams_lamsConfigurationValues cv ON ck.id = cv.keyId";

	@SuppressWarnings("rawtypes")
	public String getQuery(LamsConfigurationGetRequest lamsConfigurationGetRequest, List preparedStatementValues) {
		StringBuilder selectQuery = new StringBuilder(BASE_QUERY);

		addWhereClause(selectQuery, preparedStatementValues, lamsConfigurationGetRequest);
		addOrderByClause(selectQuery, lamsConfigurationGetRequest);
		addPagingClause(selectQuery, preparedStatementValues, lamsConfigurationGetRequest);

		logger.info("Query : " + selectQuery);
		return selectQuery.toString();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void addWhereClause(StringBuilder selectQuery, List preparedStatementValues,
			LamsConfigurationGetRequest lamsConfigurationGetRequest) {

		if (lamsConfigurationGetRequest.getId() == null && lamsConfigurationGetRequest.getEffectiveFrom() == null
				&& lamsConfigurationGetRequest.getName() == null && lamsConfigurationGetRequest.getTenantId() == null)
			return;

		selectQuery.append(" WHERE");
		boolean isAppendAndClause = false;

		if (lamsConfigurationGetRequest.getTenantId() != null) {
			isAppendAndClause = true;
			selectQuery.append(" ck.tenantId = ?");
			preparedStatementValues.add(lamsConfigurationGetRequest.getTenantId());
			isAppendAndClause = addAndClauseIfRequired(isAppendAndClause, selectQuery);
			selectQuery.append(" cv.tenantId = ?");
			preparedStatementValues.add(lamsConfigurationGetRequest.getTenantId());
		}

		if (lamsConfigurationGetRequest.getId() != null) {
			isAppendAndClause = addAndClauseIfRequired(isAppendAndClause, selectQuery);
			selectQuery.append(" ck.id IN " + getIdQuery(lamsConfigurationGetRequest.getId()));
		}

		if (lamsConfigurationGetRequest.getName() != null) {
			isAppendAndClause = addAndClauseIfRequired(isAppendAndClause, selectQuery);
			selectQuery.append(" ck.keyName = ?");
			preparedStatementValues.add(lamsConfigurationGetRequest.getName());
		}

		if (lamsConfigurationGetRequest.getEffectiveFrom() != null) {
			isAppendAndClause = addAndClauseIfRequired(isAppendAndClause, selectQuery);
			selectQuery.append(" cv.effectiveFrom = ?");
			preparedStatementValues.add(lamsConfigurationGetRequest.getEffectiveFrom());
		}
	}

	private void addOrderByClause(StringBuilder selectQuery, LamsConfigurationGetRequest lamsConfigurationGetRequest) {
		String sortBy = (lamsConfigurationGetRequest.getSortBy() == null ? "keyName"
				: lamsConfigurationGetRequest.getSortBy());
		String sortOrder = (lamsConfigurationGetRequest.getSortOrder() == null ? "ASC"
				: lamsConfigurationGetRequest.getSortOrder());
		selectQuery.append(" ORDER BY " + sortBy + " " + sortOrder);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void addPagingClause(StringBuilder selectQuery, List preparedStatementValues,
			LamsConfigurationGetRequest lamsConfigurationGetRequest) {
		// handle limit(also called pageSize) here
		selectQuery.append(" LIMIT ?");
		long pageSize = 500l;
		//FIXME GET PAGESIZE FROM APPLICATION PROPERTIEs 
		//Integer.parseInt(applicationProperties.lamsSearchPageSizeDefault());
		if (lamsConfigurationGetRequest.getPageSize() != null)
			pageSize = lamsConfigurationGetRequest.getPageSize();
		preparedStatementValues.add(pageSize); // Set limit to pageSize

		// handle offset here
		selectQuery.append(" OFFSET ?");
		int pageNumber = 0; // Default pageNo is zero meaning first page
		if (lamsConfigurationGetRequest.getPageNumber() != null)
			pageNumber = lamsConfigurationGetRequest.getPageNumber() - 1;
		preparedStatementValues.add(pageNumber * pageSize); // Set offset to pageNo * pageSize
	}

	/**
	 * This method is always called at the beginning of the method so that and
	 * is prepended before the field's predicate is handled.
	 * 
	 * @param appendAndClauseFlag
	 * @param queryString
	 * @return boolean indicates if the next predicate should append an "AND"
	 */
	private boolean addAndClauseIfRequired(boolean appendAndClauseFlag, StringBuilder queryString) {
		if (appendAndClauseFlag)
			queryString.append(" AND");

		return true;
	}

	private static String getIdQuery(List<Long> idList) {
		StringBuilder query = new StringBuilder("(");
		if (!idList.isEmpty()) {
			query.append(idList.get(0).toString());
			for (int i = 1; i < idList.size(); i++) {
				query.append(", " + idList.get(i));
			}
		}
		return query.append(")").toString();
	}
}
