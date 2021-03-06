
package org.egov.asset.service;

import java.util.ArrayList;
import java.util.List;

import org.egov.asset.config.ApplicationProperties;
import org.egov.asset.contract.AssetCategoryRequest;
import org.egov.asset.contract.AssetCategoryResponse;
import org.egov.asset.model.AssetCategory;
import org.egov.asset.model.AssetCategoryCriteria;
import org.egov.asset.model.enums.KafkaTopicName;
import org.egov.asset.repository.AssetCategoryRepository;
import org.egov.asset.web.wrapperfactory.ResponseInfoFactory;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.kafka.LogAwareKafkaTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AssetCategoryService {

    private static final Logger logger = LoggerFactory.getLogger(AssetCategoryService.class);

    @Autowired
    private AssetCategoryRepository assetCategoryRepository;

    @Autowired
    private LogAwareKafkaTemplate<String, Object> logAwareKafkaTemplate;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private ResponseInfoFactory responseInfoFactory;

    @Autowired
    private AssetCommonService assetCommonService;

    public List<AssetCategory> search(final AssetCategoryCriteria assetCategoryCriteria) {
        return assetCategoryRepository.search(assetCategoryCriteria);
    }

    public void create(final AssetCategoryRequest assetCategoryRequest) {

        try {
            assetCategoryRepository.create(assetCategoryRequest);
        } catch (final Exception ex) {
            ex.printStackTrace();
            logger.info("AssetCategoryService create");
        }
    }

    public AssetCategoryResponse createAsync(final AssetCategoryRequest assetCategoryRequest) {

        final AssetCategory assetCategory = assetCategoryRequest.getAssetCategory();

        assetCategory.setDepreciationRate(assetCommonService.getDepreciationRate(assetCategory.getDepreciationRate()));
        assetCategory.setCode(assetCategoryRepository.getAssetCategoryCode());
        logger.info("AssetCategoryService createAsync" + assetCategoryRequest);
        logAwareKafkaTemplate.send(applicationProperties.getCreateAssetCategoryTopicName(),
                KafkaTopicName.SAVEASSETCATEGORY.toString(), assetCategoryRequest);

        final List<AssetCategory> assetCategories = new ArrayList<AssetCategory>();
        assetCategories.add(assetCategory);

        return getAssetCategoryResponse(assetCategories, assetCategoryRequest.getRequestInfo());
    }

    public AssetCategoryResponse update(final AssetCategoryRequest assetCategoryRequest) {
        final AssetCategory assetCategory = assetCategoryRepository.update(assetCategoryRequest);
        final List<AssetCategory> assetCategories = new ArrayList<AssetCategory>();
        assetCategories.add(assetCategory);
        return getAssetCategoryResponse(assetCategories, new RequestInfo());
    }

    public AssetCategoryResponse updateAsync(final AssetCategoryRequest assetCategoryRequest) {
        final AssetCategory assetCategory = assetCategoryRequest.getAssetCategory();
        assetCategory.setDepreciationRate(assetCommonService.getDepreciationRate(assetCategory.getDepreciationRate()));
        logger.info("AssetCategoryService updateAsync" + assetCategoryRequest);
        logAwareKafkaTemplate.send(applicationProperties.getUpdateAssetCategoryTopicName(),
                KafkaTopicName.UPDATEASSETCATEGORY.toString(), assetCategoryRequest);
        final List<AssetCategory> assetCategories = new ArrayList<AssetCategory>();
        assetCategories.add(assetCategory);

        return getAssetCategoryResponse(assetCategories, assetCategoryRequest.getRequestInfo());
    }

    private AssetCategoryResponse getAssetCategoryResponse(final List<AssetCategory> assetCategories,
            final RequestInfo requestInfo) {
        final AssetCategoryResponse assetCategoryResponse = new AssetCategoryResponse();
        assetCategoryResponse.setAssetCategory(assetCategories);
        assetCategoryResponse.setResponseInfo(responseInfoFactory.createResponseInfoFromRequestHeaders(requestInfo));
        return assetCategoryResponse;
    }
}
