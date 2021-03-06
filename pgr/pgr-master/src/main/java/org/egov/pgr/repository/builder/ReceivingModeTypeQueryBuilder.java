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

package org.egov.pgr.repository.builder;

import java.util.List;

import org.egov.pgr.web.contract.ReceivingModeTypeGetReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ReceivingModeTypeQueryBuilder {

	private static final Logger logger = LoggerFactory.getLogger(ReceivingModeTypeQueryBuilder.class);

	private static final String BASE_QUERY = "SELECT modeType.id,modeType.code,modeType.name,modeType.description,modeType.tenantId,modeType.active,channel.channel from egpgr_receivingmode modeType left join egpgr_receivingmode_channel channel"
                                              +" on channel.receivingmodecode = modeType.code";

	@SuppressWarnings("rawtypes")
	public String getQuery(final ReceivingModeTypeGetReq modeTypeRequest, final List preparedStatementValues) {
		final StringBuilder selectQuery = new StringBuilder(BASE_QUERY);
		addWhereClause(selectQuery, preparedStatementValues, modeTypeRequest);
		addOrderByClause(selectQuery, modeTypeRequest);
		addPagingClause(selectQuery, preparedStatementValues, modeTypeRequest);
		logger.debug("Query : " + selectQuery);
		return selectQuery.toString();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void addWhereClause(final StringBuilder selectQuery, final List preparedStatementValues,
			final ReceivingModeTypeGetReq modeTypeRequest) {

		if (modeTypeRequest.getId() == null && modeTypeRequest.getName() == null && modeTypeRequest.getActive() == null
				&& modeTypeRequest.getTenantId() == null && modeTypeRequest.getCode() ==null)
			return;

		selectQuery.append(" WHERE");
		boolean isAppendAndClause = false;

		if (modeTypeRequest.getTenantId() != null) {
			isAppendAndClause = true;
			selectQuery.append(" modeType.tenantId = ?");
			preparedStatementValues.add(modeTypeRequest.getTenantId());
		}

		if (modeTypeRequest.getId() != null) {
			isAppendAndClause = addAndClauseIfRequired(isAppendAndClause, selectQuery);
			selectQuery.append(" modeType.id IN " + getIdQuery(modeTypeRequest.getId()));
		}

		if (modeTypeRequest.getName() != null) {
			isAppendAndClause = addAndClauseIfRequired(isAppendAndClause, selectQuery);
			selectQuery.append(" modeType.name = ?");
			preparedStatementValues.add(modeTypeRequest.getName());
		}

		if (modeTypeRequest.getCode() != null) {
			isAppendAndClause = addAndClauseIfRequired(isAppendAndClause, selectQuery);
			selectQuery.append(" modeType.code = ?");
			preparedStatementValues.add(modeTypeRequest.getCode());
		}

		if (modeTypeRequest.getActive() != null) {
			isAppendAndClause = addAndClauseIfRequired(isAppendAndClause, selectQuery);
			selectQuery.append(" modeType.active = ?");
			preparedStatementValues.add(modeTypeRequest.getActive());
		}
	}

	/**
	 * This method is always called at the beginning of the method so that and
	 * is prepended before the field's predicate is handled.
	 *
	 * @param appendAndClauseFlag
	 * @param queryString
	 * @return boolean indicates if the next predicate should append an "AND"
	 */
	private boolean addAndClauseIfRequired(final boolean appendAndClauseFlag, final StringBuilder queryString) {
		if (appendAndClauseFlag)
			queryString.append(" AND");

		return true;
	}

	private static String getIdQuery(final List<Long> idList) {
		final StringBuilder query = new StringBuilder("(");
		if (idList.size() >= 1) {
			query.append(idList.get(0).toString());
			for (int i = 1; i < idList.size(); i++)
				query.append(", " + idList.get(i));
		}
		return query.append(")").toString();
	}

	private void addOrderByClause(final StringBuilder selectQuery, final ReceivingModeTypeGetReq modeTypeGetRequest) {
		final String sortBy = modeTypeGetRequest.getSortBy() == null ? "modeType.code"
				: "modeType." + modeTypeGetRequest.getSortBy();
		final String sortOrder = modeTypeGetRequest.getSortOrder() == null ? "DESC" : modeTypeGetRequest.getSortOrder();
		selectQuery.append(" ORDER BY " + sortBy + " " + sortOrder);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void addPagingClause(final StringBuilder selectQuery, final List preparedStatementValues,
			final ReceivingModeTypeGetReq modeTypeGetRequest) {
		// handle limit(also called pageSize) here
		selectQuery.append(" LIMIT ?");
		long pageSize = Integer.parseInt("100");
		if (modeTypeGetRequest.getPageSize() != null)
			pageSize = modeTypeGetRequest.getPageSize();
		preparedStatementValues.add(pageSize); // Set limit to pageSize

		// handle offset here
		selectQuery.append(" OFFSET ?");
		int pageNumber = 0; // Default pageNo is zero meaning first page
		if (modeTypeGetRequest.getPageNumber() != null)
			pageNumber = modeTypeGetRequest.getPageNumber() - 1;
		preparedStatementValues.add(pageNumber * pageSize); // Set offset to
		// pageNo * pageSize
	}

	public static String insertReceivingModeTypeQuery() {
		return "INSERT INTO egpgr_receivingmode(id,code,name,description,active,createdby,lastmodifiedby,createddate,lastmodifieddate,tenantid) values "
				+ "(nextval('seq_egpgr_receivingmode'),?,?,?,?,?,?,?,?,?)";
	}

	public static String updateReceivingModeTypeQuery() {
		return "UPDATE egpgr_receivingmode SET name = ?,description = ?,"
				+ "active=?,lastmodifiedby = ?,lastmodifieddate = ? where code = ?";
	}

	public static String checkReceivingModeTypeByName() {

		return "select id from egpgr_receivingmode where name=? and tenantid=?";
	}

	public static String checkReceivinModeTypeByNameAndCode() {

		return "select code from egpgr_receivingmode where tenantid=? and trim(code)=? ";

	}
	
	public static String checkReceivinModeTypeByName() {

		return "select code from egpgr_receivingmode where tenantid=? and trim(upper(name)) =? ";

	}
	
   public static String deleteReceivingModeChannelQuery(){
		
		return "delete from egpgr_receivingmode_channel where receivingmodecode=? ";
	}
	
	public static String insertReceivingModeChannelQuery(){
		
		return "INSERT INTO egpgr_receivingmode_channel(id,receivingmodecode,channel) values (nextval('seq_egpgr_receivingmode_channel'),?,?) ";

	}
	
	public static String checkReceivingModeChannelExist(){
		
		return "select id from egpgr_receivingmode_channel where receivingmodecode=? and channel=?";
	}

	public static String getAllNameAndCodeReceivingMode() {

		return "select code,name from egpgr_receivingmode  ";

	}
}
