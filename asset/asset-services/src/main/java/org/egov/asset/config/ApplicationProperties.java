
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

package org.egov.asset.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import lombok.Getter;

@Configuration
@PropertySource(value = { "classpath:config/application-config.properties" }, ignoreResourceNotFound = true)
@Order(0)
@Getter
public class ApplicationProperties {

    private final String searchPageSizeDefault = "search.pagesize.default";
    private final String searchPagenoMax = "search.pageno.max";
    private final String searchPageSizeMax = "search.pagesize.max";

    public String getSearchPageSizeDefault() {
        return environment.getProperty(searchPageSizeDefault);
    }

    public String getSearchPagenoMax() {
        return environment.getProperty(searchPagenoMax);
    }

    public String getSearchPageSizeMax() {
        return environment.getProperty(searchPageSizeMax);
    }

    @Value("${egov.assetcategory.async}")
    private Boolean assetCategoryAsync;

    @Value("${kafka.topics.save.asset}")
    private String createAssetTopicName;

    @Value("${kafka.topics.update.asset}")
    private String updateAssetTopicName;

    @Value("${kafka.topics.save.assetcategory}")
    private String createAssetCategoryTopicName;

    @Value("${kafka.topics.update.assetcategory}")
    private String updateAssetCategoryTopicName;

    @Value("${kafka.topics.save.revaluation}")
    private String createAssetRevaluationTopicName;

    @Value("${kafka.topics.save.disposal}")
    private String createAssetDisposalTopicName;

    @Value("${egov.services.egf_service.hostname}")
    private String egfServiceHostName;

    @Value("${egov.services.egf_service.chartofaccounts.searchpath}")
    private String egfServiceChartOfAccountsSearchPath;

    @Value("${egov.services.egf_service.chartofaccountsdetails.searchpath}")
    private String egfServiceChartOfAccountsDetailsSearchPath;

    @Value("${egov.services.egf_service.voucher.createpath}")
    private String egfServiceVoucherCreatePath;

    @Value("${egov.municipality.host}")
    private String municipalityHostName;

    @Value("${egov.services.asset.batchsize}")
    private String batchSize;

    @Value("${egov.services.egf_service.financialyear.searchpath}")
    private String egfFinancialYearSearchPath;

    @Value("${kafka.topics.save.depreciation}")
    private String saveDepreciationTopic;

    @Value("${kafka.topics.save.currentvalue}")
    private String saveCurrentvalueTopic;

    @Autowired
    private Environment environment;

}