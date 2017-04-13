package org.egov.tenant.persistence.repository;

import org.egov.tenant.domain.model.Tenant;
import org.egov.tenant.domain.model.TenantSearchCriteria;
import org.egov.tenant.persistence.repository.builder.TenantQueryBuilder;
import org.egov.tenant.persistence.rowmapper.TenantRowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public class TenantRepository {

    private static final Logger logger = LoggerFactory.getLogger(TenantRepository.class);

    private JdbcTemplate jdbcTemplate;

    private TenantQueryBuilder tenantQueryBuilder;

    public TenantRepository(JdbcTemplate jdbcTemplate,TenantQueryBuilder tenantQueryBuilder) {
        this.jdbcTemplate = jdbcTemplate;
        this.tenantQueryBuilder = tenantQueryBuilder;
    }


    public List<Tenant> findForCriteria(TenantSearchCriteria tenantSearchCriteria) {
        final String queryStr = tenantQueryBuilder.getQuery(tenantSearchCriteria);
        final List<Tenant> actions = jdbcTemplate.query(queryStr,
                new TenantRowMapper());
        return actions;
    }



    public Tenant saveTenant(final Tenant tenant) {
        final String tenantInsertQuery = tenantQueryBuilder.insertTenantQuery();
        Object[] obj = new Object[] {tenant.getCode(),tenant.getName(),tenant.getDescription(),tenant.getDomainUrl(),tenant.getLocalName(),tenant.isActive(),
        0,tenant.getLatitude(),tenant.getLongitude(),tenant.getLogoId(),tenant.getBackgroundId(),1,new Date(),1,new Date(),tenant.getGrade(),tenant.getRegionName()
        ,tenant.getDistrictCode(),tenant.getDistrictName()};
        jdbcTemplate.update(tenantInsertQuery, obj);
        return tenant;
    }
}