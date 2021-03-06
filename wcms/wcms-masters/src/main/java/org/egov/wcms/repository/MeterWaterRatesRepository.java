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

import org.egov.wcms.model.MeterWaterRates;
import org.egov.wcms.model.Slab;
import org.egov.wcms.model.enums.BillingType;
import org.egov.wcms.repository.builder.MeterWaterRatesQueryBuilder;
import org.egov.wcms.repository.builder.PropertyPipeSizeQueryBuilder;
import org.egov.wcms.repository.rowmapper.MeterWaterRatesRowMapper;
import org.egov.wcms.service.RestWaterExternalMasterService;
import org.egov.wcms.web.contract.MeterWaterRatesGetRequest;
import org.egov.wcms.web.contract.MeterWaterRatesRequest;
import org.egov.wcms.web.contract.PropertyTaxResponseInfo;
import org.egov.wcms.web.contract.UsageTypeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class MeterWaterRatesRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MeterWaterRatesQueryBuilder meterWaterRatesQueryBuilder;

    @Autowired
    private MeterWaterRatesRowMapper meterWaterRatesRowMapper;

    @Autowired
    private RestWaterExternalMasterService restExternalMasterService;

    public MeterWaterRatesRequest persistCreateMeterWaterRates(final MeterWaterRatesRequest meterWaterRatesRequest) {
        log.info("MeterWaterRatesRequest::" + meterWaterRatesRequest);
        final String meterWaterRatesInsertQuery = MeterWaterRatesQueryBuilder.insertMeterWaterRatesQuery();
        final String pipesizeQuery = PropertyPipeSizeQueryBuilder.getPipeSizeIdQuery();
        final String sourcetypeQuery = MeterWaterRatesQueryBuilder.getSourceTypeIdQuery();
        for (final MeterWaterRates meterWaterRates : meterWaterRatesRequest.getMeterWaterRates()) {
            Long pipesizeId = 0L;
            try {
                pipesizeId = jdbcTemplate.queryForObject(pipesizeQuery,
                        new Object[] { meterWaterRates.getPipeSize(), meterWaterRates.getTenantId() },
                        Long.class);
            } catch (final EmptyResultDataAccessException e) {
                log.info("EmptyResultDataAccessException: Query returned empty result set");
            }
            if (pipesizeId == null)
                log.info("Invalid input.");
            Long sourcetypeId = 0L;
            try {
                sourcetypeId = jdbcTemplate.queryForObject(sourcetypeQuery,
                        new Object[] { meterWaterRates.getSourceTypeName(), meterWaterRates.getTenantId() },
                        Long.class);
            } catch (final EmptyResultDataAccessException e) {
                log.info("EmptyResultDataAccessException: Query returned empty result set");
            }
            if (sourcetypeId == null)
                log.info("Invalid input.");
            meterWaterRates.setBillingtype(BillingType.METERED.toString());
            final Object[] obj = new Object[] { Long.valueOf(meterWaterRates.getCode()), meterWaterRates.getCode(),
                    meterWaterRates.getBillingtype(), meterWaterRates.getUsageTypeId(),
                    sourcetypeId, pipesizeId, meterWaterRates.getFromDate(),
                    meterWaterRates.getToDate(),
                    meterWaterRates.getActive(),
                    Long.valueOf(meterWaterRatesRequest.getRequestInfo().getUserInfo().getId()),
                    Long.valueOf(meterWaterRatesRequest.getRequestInfo().getUserInfo().getId()),
                    new Date(new java.util.Date().getTime()), new Date(new java.util.Date().getTime()),
                    meterWaterRates.getTenantId() };
            jdbcTemplate.update(meterWaterRatesInsertQuery, obj);

            final List<Slab> slabList = meterWaterRates.getSlab();
            if (slabList != null && !slabList.isEmpty()) {
                final String slabInsertQuery = MeterWaterRatesQueryBuilder.insertSlabQuery();
                log.info("the insert query for slab : " + slabInsertQuery);
                final List<Object[]> slabBatchArgs = new ArrayList<>();

                for (final Slab slab : slabList) {
                    final Object[] slabRecord = { Long.valueOf(meterWaterRates.getCode()), slab.getFromUnit(), slab.getToUnit(),
                            slab.getUnitRate(),
                            meterWaterRates.getTenantId() };
                    slabBatchArgs.add(slabRecord);
                }

                try {
                    jdbcTemplate.batchUpdate(slabInsertQuery, slabBatchArgs);
                } catch (final DataAccessException ex) {
                    ex.printStackTrace();
                    throw new RuntimeException(ex.getMessage());
                }
            }
        }

        return meterWaterRatesRequest;
    }

    public MeterWaterRatesRequest persistUpdateMeterWaterRates(final MeterWaterRatesRequest meterWaterRatesRequest) {
        log.info("MeterWaterRatesRequest::" + meterWaterRatesRequest);
        final String meterWaterRatesUpdateQuery = MeterWaterRatesQueryBuilder.updateMeterWaterRatesQuery();
        final String pipesizeQuery = PropertyPipeSizeQueryBuilder.getPipeSizeIdQuery();
        final String sourcetypeQuery = MeterWaterRatesQueryBuilder.getSourceTypeIdQuery();
        for (final MeterWaterRates meterWaterRates : meterWaterRatesRequest.getMeterWaterRates()) {
            Long pipesizeId = 0L;
            try {
                pipesizeId = jdbcTemplate.queryForObject(pipesizeQuery,
                        new Object[] { meterWaterRates.getPipeSize(), meterWaterRates.getTenantId() },
                        Long.class);
            } catch (final EmptyResultDataAccessException e) {
                log.info("EmptyResultDataAccessException: Query returned empty result set");
            }
            if (pipesizeId == null)
                log.info("Invalid input.");
            Long sourcetypeId = 0L;
            try {
                sourcetypeId = jdbcTemplate.queryForObject(sourcetypeQuery,
                        new Object[] { meterWaterRates.getSourceTypeName(), meterWaterRates.getTenantId() },
                        Long.class);
            } catch (final EmptyResultDataAccessException e) {
                log.info("EmptyResultDataAccessException: Query returned empty result set");
            }
            if (sourcetypeId == null)
                log.info("Invalid input.");
            meterWaterRates.setBillingtype(BillingType.METERED.toString());
            final Object[] obj = new Object[] {
                    meterWaterRates.getBillingtype(), meterWaterRates.getUsageTypeId(),
                    sourcetypeId, pipesizeId, meterWaterRates.getFromDate(),
                    meterWaterRates.getToDate(),
                    meterWaterRates.getActive(),
                    Long.valueOf(meterWaterRatesRequest.getRequestInfo().getUserInfo().getId()),
                    new Date(new java.util.Date().getTime()), meterWaterRates.getCode(),
                    meterWaterRates.getTenantId() };
            jdbcTemplate.update(meterWaterRatesUpdateQuery, obj);

            final List<Slab> slabList = meterWaterRates.getSlab();
            if (slabList != null && !slabList.isEmpty()) {
                final String slabInsertQuery = MeterWaterRatesQueryBuilder.insertSlabQuery();
                log.info("the insert query for slab : " + slabInsertQuery);
                final List<Object[]> slabBatchArgs = new ArrayList<>();

                for (final Slab slab : slabList) {
                    final Object[] slabRecord = { Long.valueOf(meterWaterRates.getCode()), slab.getFromUnit(), slab.getToUnit(),
                            slab.getUnitRate(),
                            meterWaterRates.getTenantId() };
                    slabBatchArgs.add(slabRecord);
                }
                try {
                    jdbcTemplate.batchUpdate(slabInsertQuery, slabBatchArgs);
                } catch (final DataAccessException ex) {
                    ex.printStackTrace();
                    throw new RuntimeException(ex.getMessage());
                }
            }
        }

        return meterWaterRatesRequest;
    }

    public List<MeterWaterRates> findForCriteria(final MeterWaterRatesGetRequest meterWaterRatesGetRequest) {
        final List<Object> preparedStatementValues = new ArrayList<>();
        final List<Integer> usageTypeIdsList = new ArrayList<>();
        final String queryStr = meterWaterRatesQueryBuilder.getQuery(meterWaterRatesGetRequest, preparedStatementValues);
        final List<MeterWaterRates> meterWaterRatesList = jdbcTemplate.query(queryStr,
                preparedStatementValues.toArray(), meterWaterRatesRowMapper);

        // fetch usage type Id and set the usage type name here
        for (final MeterWaterRates meterWaterRates : meterWaterRatesList)
            usageTypeIdsList.add(Integer.valueOf(meterWaterRates.getUsageTypeId()));
        final Integer[] usageTypeIds = usageTypeIdsList.toArray(new Integer[usageTypeIdsList.size()]);
        final UsageTypeResponse usageResponse = restExternalMasterService.getUsageNameFromPTModule(
                usageTypeIds, meterWaterRatesGetRequest.getTenantId());
        for (final MeterWaterRates meterWaterRatesObj : meterWaterRatesList)
            for (final PropertyTaxResponseInfo propertyResponse : usageResponse.getUsageMasters())
                if (propertyResponse.getId().equals(meterWaterRatesObj.getUsageTypeId()))
                    meterWaterRatesObj.setUsageTypeName(propertyResponse.getName());
        return meterWaterRatesList;
    }

    public boolean checkMeterWaterRatesExists(final String code, final String usageTypeId,
            final String sourceTypeName, final Double pipeSize, final String tenantId) {
        final List<Object> preparedStatementValues = new ArrayList<>();
        final String pipesizeQuery = PropertyPipeSizeQueryBuilder.getPipeSizeIdQuery();
        Long pipesizeId = 0L;
        try {
            pipesizeId = jdbcTemplate.queryForObject(pipesizeQuery,
                    new Object[] { pipeSize, tenantId },
                    Long.class);
        } catch (final EmptyResultDataAccessException e) {
            log.info("EmptyResultDataAccessException: Query returned empty result set while update");
        }
        if (pipesizeId == null)
            log.info("Invalid input.");
        final String sourcetypeQuery = MeterWaterRatesQueryBuilder.getSourceTypeIdQuery();
        Long sourcetypeId = 0L;
        try {
            sourcetypeId = jdbcTemplate.queryForObject(sourcetypeQuery,
                    new Object[] { sourceTypeName, tenantId },
                    Long.class);
        } catch (final EmptyResultDataAccessException e) {
            log.info("EmptyResultDataAccessException: Query returned empty result set while update");
        }
        if (pipesizeId == null)
            log.info("Invalid input.");
        preparedStatementValues.add(usageTypeId);
        preparedStatementValues.add(sourcetypeId);
        preparedStatementValues.add(pipesizeId);
        preparedStatementValues.add(tenantId);
        final String query;
        if (code == null)
            query = MeterWaterRatesQueryBuilder.selectMeterWaterRatesByCodeQuery();
        else {
            preparedStatementValues.add(code);
            query = MeterWaterRatesQueryBuilder.selectMeterWaterRatesByCodeNotInQuery();
        }
        final List<Map<String, Object>> meterWaterRates = jdbcTemplate.queryForList(query,
                preparedStatementValues.toArray());
        if (!meterWaterRates.isEmpty())
            return false;

        return true;
    }

    public boolean checkPipeSizeExists(final Double pipeSize, final String tenantId) {
        final List<Object> preparedStatementValues = new ArrayList<>();
        preparedStatementValues.add(pipeSize);
        preparedStatementValues.add(tenantId);
        final String query = PropertyPipeSizeQueryBuilder.getPipeSizeIdQuery();
        final List<Map<String, Object>> pipeSizes = jdbcTemplate.queryForList(query,
                preparedStatementValues.toArray());
        if (!pipeSizes.isEmpty())
            return false;

        return true;
    }

    public boolean checkSourceTypeExists(final String sourceTypeName, final String tenantId) {
        final List<Object> preparedStatementValues = new ArrayList<>();
        preparedStatementValues.add(sourceTypeName);
        preparedStatementValues.add(tenantId);
        final String query = MeterWaterRatesQueryBuilder.getSourceTypeIdQuery();
        final List<Map<String, Object>> sourceTypes = jdbcTemplate.queryForList(query,
                preparedStatementValues.toArray());
        if (!sourceTypes.isEmpty())
            return false;

        return true;
    }
}
