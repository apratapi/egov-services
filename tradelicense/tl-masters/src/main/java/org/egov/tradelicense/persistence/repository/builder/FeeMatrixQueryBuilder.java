package org.egov.tradelicense.persistence.repository.builder;

import java.util.List;

import org.egov.tradelicense.util.ConstantUtility;

/**
 * This Class contains INSERT, UPDATE and SELECT queries for FeeMatrix API's
 * 
 * @author Pavan Kumar Kamma
 */
public class FeeMatrixQueryBuilder {

	private static final String feeMatrixTableName = ConstantUtility.FEE_MATRIX_TABLE_NAME;
	private static final String feeMatrixDetailTableName = ConstantUtility.FEE_MATRIX_DETAIL_TABLE_NAME;

	public static final String INSERT_FEE_MATRIX_QUERY = "INSERT INTO " + feeMatrixTableName
			+ " (tenantId, applicationType, categoryId, businessNature, subCategoryId, financialYear, effectiveFrom, effectiveTo, createdBy, lastModifiedBy, createdTime, lastModifiedTime)"
			+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";

	public static final String UPDATE_FEE_MATRIX_QUERY = "UPDATE " + feeMatrixTableName
			+ " SET tenantId = ?, applicationType = ?, categoryId = ?, businessNature = ?,"
			+ " subCategoryId = ?, financialYear = ?, effectiveFrom = ?, effectiveTo = ?, lastModifiedBy = ?, lastModifiedTime = ?"
			+ " WHERE id = ?";

	public static final String INSERT_FEE_MATRIX_DETAIL_QUERY = "INSERT INTO " + feeMatrixDetailTableName
			+ " (feeMatrixId, uomFrom, uomTo, amount, createdBy, lastModifiedBy, createdTime, lastModifiedTime)"
			+ " VALUES(?,?,?,?,?,?,?,?)";

	public static final String UPDATE_FEE_MATRIX_DETAIL_QUERY = "UPDATE " + feeMatrixDetailTableName
			+ " SET feeMatrixId = ?, uomFrom = ?, uomTo = ?, amount = ?," + " lastModifiedBy = ?, lastModifiedTime = ?"
			+ " WHERE id = ?";

	public static final String buildFeeMatrixDetailSearchQuery(Long feeMatrixId, List<Object> preparedStatementValues) {

		StringBuffer searchSql = new StringBuffer();
		searchSql.append("select * from " + feeMatrixDetailTableName + " where ");
		if (feeMatrixId != null) {
			searchSql.append(" feeMatrixId = ? ");
			preparedStatementValues.add(feeMatrixId);
		}

		return searchSql.toString();
	}

	public static String buildSearchQuery(String tenantId, Integer[] ids, Integer categoryId, Integer subCategoryId,
			String financialYear, String applicationType, String businessNature, Integer pageSize, Integer offSet,
			List<Object> preparedStatementValues) {

		StringBuffer searchSql = new StringBuffer();
		searchSql.append("select * from " + feeMatrixTableName + " where ");
		searchSql.append(" tenantId = ? ");
		preparedStatementValues.add(tenantId);

		if (ids != null && ids.length > 0) {

			String searchIds = "";
			int count = 1;
			for (Integer id : ids) {

				if (count < ids.length)
					searchIds = searchIds + id + ",";
				else
					searchIds = searchIds + id;

				count++;
			}
			searchSql.append(" AND id IN (" + searchIds + ") ");
		}

		if (categoryId != null) {
			searchSql.append(" AND categoryId =? ");
			preparedStatementValues.add(categoryId);
		}

		if (subCategoryId != null) {
			searchSql.append(" AND subCategoryId =? ");
			preparedStatementValues.add(subCategoryId);
		}

		if (financialYear != null && !financialYear.isEmpty()) {
			searchSql.append(" AND financialYear =? ");
			preparedStatementValues.add(financialYear);
		}

		if (applicationType != null && !applicationType.isEmpty()) {
			searchSql.append(" AND applicationType =? ");
			preparedStatementValues.add(applicationType);
		}

		if (businessNature != null && !businessNature.isEmpty()) {
			searchSql.append(" AND businessNature =? ");
			preparedStatementValues.add(businessNature);
		}

		if (pageSize != null) {
			searchSql.append(" limit ? ");
			preparedStatementValues.add(pageSize);
		}

		if (offSet != null) {
			searchSql.append(" offset ? ");
			preparedStatementValues.add(offSet);
		}

		return searchSql.toString();
	}
}