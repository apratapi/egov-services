package org.egov.demand.repository.querybuilder;

import java.util.List;

import org.egov.demand.model.DemandCriteria;
import org.egov.demand.model.DemandDetailCriteria;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DemandQueryBuilder {

	private DemandQueryBuilder() {
		//private constructor to avoid instantiation
	}

	public static final String SEQ_EGBS_QUERY = "SELECT NEXTVAL('sequencename') FROM GENERATE_SERIES(1,?)";

	public static final String BASE_DEMAND_QUERY = "SELECT demand.id AS did,demand.consumercode AS dconsumercode,"
			+ "demand.consumertype AS dconsumertype,demand.businessservice AS dbusinessservice,demand.owner AS downer,"
			+ "demand.taxperiodfrom AS dtaxperiodfrom,demand.taxperiodto AS dtaxperiodto,"
			+ "demand.minimumamountpayable AS dminimumamountpayable,demand.createdby AS dcreatedby,"
			+ "demand.lastmodifiedby AS dlastmodifiedby,demand.createdtime AS dcreatedtime,"
			+ "demand.lastmodifiedtime AS dlastmodifiedtime,demand.tenantid AS dtenantid,demand.type AS dtype,"

			+ "demanddetail.id AS dlid,demanddetail.demandid AS dldemandid,demanddetail.taxheadcode AS dltaxheadcode,"
			+ "demanddetail.taxamount AS dltaxamount,demanddetail.collectionamount AS dlcollectionamount,"
			+ "demanddetail.createdby AS dlcreatedby,demanddetail.lastModifiedby AS dllastModifiedby,"
			+ "demanddetail.createdtime AS dlcreatedtime,demanddetail.lastModifiedtime AS dllastModifiedtime,"
			+ "demanddetail.tenantid AS dltenantid " + "FROM egbs_demand demand "
			+ "INNER JOIN egbs_demanddetail demanddetail ON demand.id=demanddetail.demandid "
			+ "AND demand.tenantid=demanddetail.tenantid WHERE ";

	public static final String BASE_DEMAND_DETAIL_QUERY = "SELECT "
			+ "demanddetail.id AS dlid,demanddetail.demandid AS dldemandid,demanddetail.taxheadcode AS dltaxheadcode,"
			+ "demanddetail.taxamount AS dltaxamount,demanddetail.collectionamount AS dlcollectionamount,"
			+ "demanddetail.createdby AS dlcreatedby,demanddetail.lastModifiedby AS dllastModifiedby,"
			+ "demanddetail.createdtime AS dlcreatedtime,demanddetail.lastModifiedtime AS dllastModifiedtime,"
			+ "demanddetail.tenantid AS dltenantid " + " FROM egbs_demanddetail demanddetail "
					+ "INNER JOIN egbs_demand demand ON demanddetail.demandid=demand.id AND "
					+ "demanddetail.tenantid=demand.tenantid WHERE ";

	public static final String DEMAND_QUERY_ORDER_BY_CLAUSE = "demand.id";

	public static final String BASE_DEMAND_DETAIL_QUERY_ORDER_BY_CLAUSE = "demanddetail.id";

	public static final String DEMAND_INSERT_QUERY = "INSERT INTO egbs_demand "
			+ "(id,consumerCode,consumerType,businessService,owner,taxPeriodFrom,taxPeriodTo,"
			+ "minimumAmountPayable,type,createdby,lastModifiedby,createdtime,lastModifiedtime,tenantid) "
			+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?);";

	public static final String DEMAND_DETAIL_INSERT_QUERY = "INSERT INTO egbs_demanddetail "
			+ "(id,demandid,taxHeadCode,taxamount,collectionamount,"
			+ "createdby,lastModifiedby,createdtime,lastModifiedtime,tenantid)" 
			+ " VALUES (?,?,?,?,?,?,?,?,?,?);";

	// FIX ME REMOVE CREATED BY FROM UPDATE
	public static final String DEMAND_UPDATE_QUERY = "UPDATE TABLE egbs_demand SET "
			+ "id=?,consumerCode=?,consumerType=?,businessService=?,owner=?,taxPeriodFrom=?,"
			+ "taxPeriodTo=?,minimumAmountPayable=?,type=?,lastModifiedby=?," + "lastModifiedtime=?,tenantid=?) "
			+ "WHERE id=? AND tenantid=?;";

	public static final String DEMAND_DETAIL_UPDATE_QUERY = "UPDATE TABLE egbs_demanddetail SET "
			+ "id=?,demandid=?,taxHeadCode=?,taxamount=?,collectionamount=?,"
			+ "lastModifiedby=?,lastModifiedtime=?,tenantid=? WHERE id=? AND tenantid=?;";

	public static String getDemandQuery(DemandCriteria demandCriteria, List<Object> preparedStatementValues) {

		StringBuilder demandQueryBuilder = new StringBuilder(BASE_DEMAND_QUERY);

		demandQueryBuilder.append("demand.tenantid=?");
		preparedStatementValues.add(demandCriteria.getTenantId());

		if (demandCriteria.getDemandId() != null) {
			addAndClause(demandQueryBuilder);
			demandQueryBuilder.append("demand.id=?");
			preparedStatementValues.add(demandCriteria.getDemandId());
		}
		if (demandCriteria.getBusinessService() != null) {
			addAndClause(demandQueryBuilder);
			demandQueryBuilder.append("demand.businessservice=?");
			preparedStatementValues.add(demandCriteria.getBusinessService());
		}
		if (demandCriteria.getConsumerCode() != null) {
			addAndClause(demandQueryBuilder);
			demandQueryBuilder.append("demand.consumercode=?");
			preparedStatementValues.add(demandCriteria.getConsumerCode());
		}
		if (demandCriteria.getDemandFrom() != null) {
			addAndClause(demandQueryBuilder);
			demandQueryBuilder.append("demanddetail.taxamount>=?");
			preparedStatementValues.add(demandCriteria.getDemandFrom());
		}
		if (demandCriteria.getDemandTo() != null) {
			addAndClause(demandQueryBuilder);
			demandQueryBuilder.append("demanddetail.taxamount<=?");
			preparedStatementValues.add(demandCriteria.getDemandTo());
		}
		if (demandCriteria.getType() != null) {
			addAndClause(demandQueryBuilder);
			demandQueryBuilder.append("demand.type=?");
			preparedStatementValues.add(demandCriteria.getType());
		}
		addOrderByClause(demandQueryBuilder, DEMAND_QUERY_ORDER_BY_CLAUSE);
		addPagingClause(demandQueryBuilder, preparedStatementValues);

		log.info("the query String for demand : " + demandQueryBuilder.toString());
		return demandQueryBuilder.toString();
	}

	public static String getDemandDetailQuery(DemandDetailCriteria demandDetailCriteria,
			List<Object> preparedStatementValues) {

		StringBuilder demandQueryBuilder = new StringBuilder(BASE_DEMAND_DETAIL_QUERY);

		demandQueryBuilder.append("demanddetail.tenantid=?");
		preparedStatementValues.add(demandDetailCriteria.getTenantId());

		if (demandDetailCriteria.getDemandId() != null) {
			addAndClause(demandQueryBuilder);
			demandQueryBuilder.append("demanddetail.demandid=?");
			preparedStatementValues.add(demandDetailCriteria.getDemandId());
		}
		if (demandDetailCriteria.getTaxHeadCode() != null) {
			addAndClause(demandQueryBuilder);
			demandQueryBuilder.append("demanddetail.taxheadcode=?");
			preparedStatementValues.add(demandDetailCriteria.getTaxHeadCode());
		}
		if (demandDetailCriteria.getPeriod() != null) {
			addAndClause(demandQueryBuilder);
			demandQueryBuilder.append("demand.taxPeriodFrom<=?");
			preparedStatementValues.add(demandDetailCriteria.getPeriod());
			addAndClause(demandQueryBuilder);
			demandQueryBuilder.append("demand.taxPeriodTo>=?");
			preparedStatementValues.add(demandDetailCriteria.getPeriod());
		}
		addOrderByClause(demandQueryBuilder, BASE_DEMAND_DETAIL_QUERY_ORDER_BY_CLAUSE);
		addPagingClause(demandQueryBuilder, preparedStatementValues);
		log.info("the query String for demand detail: " + demandQueryBuilder.toString());
		return demandQueryBuilder.toString();
	}

	private static void addOrderByClause(StringBuilder demandQueryBuilder,String columnName) {
		demandQueryBuilder.append(" ORDER BY " + columnName);
	}

	private static void addPagingClause(StringBuilder demandQueryBuilder, List<Object> preparedStatementValues) {
		demandQueryBuilder.append(" LIMIT ?");
		preparedStatementValues.add(500);
		demandQueryBuilder.append(" OFFSET ?");
		preparedStatementValues.add(0);
	}

	private static boolean addAndClause(StringBuilder queryString) {
		queryString.append(" AND ");
		return true;
	}
}
