package org.egov.asset.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.egov.asset.model.AuditDetails;
import org.egov.asset.model.DepreciationDetail;
import org.egov.asset.model.enums.DepreciationStatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class DepreciationDetailRowMapper implements RowMapper<DepreciationDetail> {

	@Override
	public DepreciationDetail mapRow(ResultSet rs, int rowNum) throws SQLException {

		AuditDetails auditDetails = AuditDetails.builder()
				.createdBy(rs.getString("createdby")).createdDate(rs.getLong("createddate"))
				.lastModifiedBy(rs.getString("lastmodifiedby"))
				.lastModifiedDate(rs.getLong("lastmodifieddate")).build();
		
		return DepreciationDetail.builder()/*.auditDetails(auditDetails)*/.assetId(rs.getLong("assetid"))
				.depreciationRate(rs.getDouble("depreciationrate")).depreciationValue(rs.getBigDecimal("depreciationvalue"))
				.id(rs.getLong("id")).status(DepreciationStatus.fromValue(rs.getString("status")))
				/*.tenantId(rs.getString("tenantid"))*/.valueAfterDepreciation(rs.getBigDecimal("valueafterdepreciation"))
				.valueBeforeDepreciation(rs.getBigDecimal("valuebeforedepreciation")).build();
	}
}
