/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) <2015>  eGovernments Foundation
 *
 *     The updated version of eGov suite of products as by eGovernments Foundation
 *     is available at http://www.egovernments.org
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see http://www.gnu.org/licenses/ or
 *     http://www.gnu.org/licenses/gpl.html .
 *
 *     In addition to the terms of the GPL license to be adhered to in using this
 *     program, the following additional terms are to be complied with:
 *
 *         1) All versions of this program, verbatim or modified must carry this
 *            Legal Notice.
 *
 *         2) Any misrepresentation of the origin of the material is prohibited. It
 *            is required that all modified versions of this material be marked in
 *            reasonable ways as different from the original version.
 *
 *         3) This license does not grant any rights to any user of the program
 *            with regards to rights under trademark law for use of the trade names
 *            or trademarks of eGovernments Foundation.
 *
 *   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */
package org.egov.wcms.repository;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.egov.wcms.model.PropertyTypeUsageType;
import org.egov.wcms.repository.builder.PropertyUsageTypeQueryBuilder;
import org.egov.wcms.repository.rowmapper.PropertyUsageTypeRowMapper;
import org.egov.wcms.service.RestWaterExternalMasterService;
import org.egov.wcms.web.contract.PropertyTaxResponseInfo;
import org.egov.wcms.web.contract.PropertyTypeResponse;
import org.egov.wcms.web.contract.PropertyTypeUsageTypeGetReq;
import org.egov.wcms.web.contract.PropertyTypeUsageTypeReq;
import org.egov.wcms.web.contract.UsageTypeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class PropertyUsageTypeRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PropertyUsageTypeRowMapper propUsageTypeMapper;

    @Autowired
    private PropertyUsageTypeQueryBuilder propUsageTypeQueryBuilder;

    @Autowired
    private RestWaterExternalMasterService restExternalMasterService;

    public PropertyTypeUsageTypeReq persistCreateUsageType(final PropertyTypeUsageTypeReq propUsageTypeRequest) {
        log.info("Property Usage Type Request::" + propUsageTypeRequest);
        final String propUsageTypeInsert = propUsageTypeQueryBuilder.getPersistQuery();
        final Object[] obj = new Object[] { propUsageTypeRequest.getPropertyTypeUsageType().getPropertyTypeId(),
                propUsageTypeRequest.getPropertyTypeUsageType().getUsageTypeId(),
                propUsageTypeRequest.getPropertyTypeUsageType().getActive(),
                propUsageTypeRequest.getPropertyTypeUsageType().getTenantId(),
                Long.valueOf(propUsageTypeRequest.getRequestInfo().getUserInfo().getId()),
                Long.valueOf(propUsageTypeRequest.getRequestInfo().getUserInfo().getId()),
                new Date(new java.util.Date().getTime()), new Date(new java.util.Date().getTime()) };
        jdbcTemplate.update(propUsageTypeInsert, obj);
        return propUsageTypeRequest;
    }

    public PropertyTypeUsageTypeReq persistUpdateUsageType(final PropertyTypeUsageTypeReq propUsageTypeRequest) {
        log.info("Property Usage Type Request::" + propUsageTypeRequest);
        final String propUsageTypeUpdate = PropertyUsageTypeQueryBuilder.updatePropertyUsageQuery();
        final Object[] obj = new Object[] { propUsageTypeRequest.getPropertyTypeUsageType().getPropertyTypeId(),
                propUsageTypeRequest.getPropertyTypeUsageType().getUsageTypeId(),
                propUsageTypeRequest.getPropertyTypeUsageType().getActive(),
                Long.valueOf(propUsageTypeRequest.getRequestInfo().getUserInfo().getId()),
                new Date(new java.util.Date().getTime()), propUsageTypeRequest.getPropertyTypeUsageType().getId() };
        jdbcTemplate.update(propUsageTypeUpdate, obj);
        return propUsageTypeRequest;
    }

    public List<PropertyTypeUsageType> getPropertyUsageType(final PropertyTypeUsageTypeGetReq propUsageTypeGetRequest) {
        final List<Object> preparedStatementValues = new ArrayList<>();
        final List<Integer> propertyTypeIdsList = new ArrayList<>();
        final List<Integer> usageTypeIdsList = new ArrayList<>();
        final String queryStr = propUsageTypeQueryBuilder.getQuery(propUsageTypeGetRequest, preparedStatementValues);
        final List<PropertyTypeUsageType> propUsageTypes = jdbcTemplate.query(queryStr,
                preparedStatementValues.toArray(), propUsageTypeMapper);

        // fetch property type Id and set the property type name here
        for (final PropertyTypeUsageType propertyTypeUsage : propUsageTypes)
            propertyTypeIdsList.add(Integer.valueOf(propertyTypeUsage.getPropertyTypeId()));
        final Integer[] propertypeIds = propertyTypeIdsList.toArray(new Integer[propertyTypeIdsList.size()]);
        final PropertyTypeResponse propertyTypes = restExternalMasterService.getPropertyNameFromPTModule(
                propertypeIds, propUsageTypeGetRequest.getTenantId());
        for (final PropertyTypeUsageType propertyTypeUsageType : propUsageTypes)
            for (final PropertyTaxResponseInfo propertyResponse : propertyTypes.getPropertyTypes())
                if (propertyResponse.getId().equals(propertyTypeUsageType.getPropertyTypeId()))
                    propertyTypeUsageType.setPropertyType(propertyResponse.getName());

        // fetch usage type Id and set the usage type name here
        for (final PropertyTypeUsageType propertyTypeUsage : propUsageTypes)
            usageTypeIdsList.add(Integer.valueOf(propertyTypeUsage.getUsageTypeId()));
        final Integer[] usageTypeIds = usageTypeIdsList.toArray(new Integer[usageTypeIdsList.size()]);
        final UsageTypeResponse usageResponse = restExternalMasterService.getUsageNameFromPTModule(
                usageTypeIds, propUsageTypeGetRequest.getTenantId());
        for (final PropertyTypeUsageType propertyTypeUsageType : propUsageTypes)
            for (final PropertyTaxResponseInfo propertyResponse : usageResponse.getUsageMasters())
                if (propertyResponse.getId().equals(propertyTypeUsageType.getUsageTypeId()))
                    propertyTypeUsageType.setUsageType(propertyResponse.getName());
        return propUsageTypes;
    }

    public boolean checkPropertyUsageTypeExists(final Long id, final String propertyTypeId, final String usageTypeId,
            final String tenantId) {
        final List<Object> preparedStatementValues = new ArrayList<>();
        preparedStatementValues.add(propertyTypeId);
        preparedStatementValues.add(usageTypeId);
        preparedStatementValues.add(tenantId);
        final String query;
        if (id == null)
            query = PropertyUsageTypeQueryBuilder.selectPropertyByUsageTypeQuery();
        else {
            preparedStatementValues.add(id);
            query = PropertyUsageTypeQueryBuilder.selectPropertyByUsageTypeNotInQuery();
        }
        final List<Map<String, Object>> propertyUsages = jdbcTemplate.queryForList(query,
                preparedStatementValues.toArray());
        if (!propertyUsages.isEmpty())
            return false;

        return true;
    }
}
