package org.egov.asset.service;

import java.util.ArrayList;
import java.util.List;

import org.egov.asset.config.ApplicationProperties;
import org.egov.asset.contract.AssetCurrentValueRequest;
import org.egov.asset.contract.RevaluationRequest;
import org.egov.asset.contract.RevaluationResponse;
import org.egov.asset.contract.VoucherRequest;
import org.egov.asset.model.Asset;
import org.egov.asset.model.AssetCategory;
import org.egov.asset.model.AssetCriteria;
import org.egov.asset.model.AssetCurrentValue;
import org.egov.asset.model.ChartOfAccountDetailContract;
import org.egov.asset.model.Revaluation;
import org.egov.asset.model.RevaluationCriteria;
import org.egov.asset.model.VouchercreateAccountCodeDetails;
import org.egov.asset.model.enums.AssetConfigurationKeys;
import org.egov.asset.model.enums.KafkaTopicName;
import org.egov.asset.model.enums.TransactionType;
import org.egov.asset.model.enums.TypeOfChangeEnum;
import org.egov.asset.repository.AssetRepository;
import org.egov.asset.repository.RevaluationRepository;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.kafka.LogAwareKafkaTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RevaluationService {

    @Autowired
    private RevaluationRepository revaluationRepository;

    @Autowired
    private LogAwareKafkaTemplate<String, Object> logAwareKafkaTemplate;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private VoucherService voucherService;

    @Autowired
    private AssetConfigurationService assetConfigurationService;

    @Autowired
    private AssetCommonService assetCommonService;

    @Autowired
    private CurrentValueService currentValueService;

    public RevaluationResponse createAsync(final RevaluationRequest revaluationRequest, final HttpHeaders headers) {
        final Revaluation revaluation = revaluationRequest.getRevaluation();
        log.debug("RevaluationService createAsync revaluationRequest:" + revaluationRequest);

        revaluation.setId(Long.valueOf(revaluationRepository.getNextRevaluationId().longValue()));

        if (revaluation.getAuditDetails() == null)
            revaluation.setAuditDetails(assetCommonService.getAuditDetails(revaluationRequest.getRequestInfo()));

        if (assetConfigurationService.getEnabledVoucherGeneration(AssetConfigurationKeys.ENABLEVOUCHERGENERATION,
                revaluation.getTenantId()))
            try {
                log.info("Commencing Voucher Generation for Asset Revaluation");
                final Long voucherId = createVoucherForRevaluation(revaluationRequest, headers);

                if (voucherId != null)
                    revaluation.setVoucherReference(voucherId);
            } catch (final Exception e) {
                throw new RuntimeException("Voucher Generation is failed due to :" + e.getMessage());
            }

        logAwareKafkaTemplate.send(applicationProperties.getCreateAssetRevaluationTopicName(),
                KafkaTopicName.SAVEREVALUATION.toString(), revaluationRequest);

        final List<Revaluation> revaluations = new ArrayList<Revaluation>();
        revaluations.add(revaluation);
        return getRevaluationResponse(revaluations);
    }

    public void create(final RevaluationRequest revaluationRequest) {
        revaluationRepository.create(revaluationRequest);
        saveRevaluationAmountToCurrentAmount(revaluationRequest);
    }

    public void saveRevaluationAmountToCurrentAmount(final RevaluationRequest revaluationRequest) {

        final Revaluation revaluation = revaluationRequest.getRevaluation();
        final List<AssetCurrentValue> assetCurrentValues = new ArrayList<AssetCurrentValue>();
        final AssetCurrentValue assetCurrentValue = new AssetCurrentValue();
        assetCurrentValue.setAssetId(revaluation.getAssetId());
        assetCurrentValue.setAssetTranType(TransactionType.REVALUATION);
        assetCurrentValue.setCurrentAmount(revaluation.getValueAfterRevaluation());
        assetCurrentValue.setTenantId(revaluation.getTenantId());
        assetCurrentValues.add(assetCurrentValue);
        final AssetCurrentValueRequest assetCurrentValueRequest = new AssetCurrentValueRequest();
        assetCurrentValueRequest.setRequestInfo(revaluationRequest.getRequestInfo());
        assetCurrentValueRequest.setAssetCurrentValues(assetCurrentValues);
        currentValueService.createCurrentValueAsync(assetCurrentValueRequest);
    }

    public RevaluationResponse search(final RevaluationCriteria revaluationCriteria) {
        List<Revaluation> revaluations = new ArrayList<Revaluation>();
        try {
            revaluations = revaluationRepository.search(revaluationCriteria);
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
        return getRevaluationResponse(revaluations);
    }

    private Long createVoucherForRevaluation(final RevaluationRequest revaluationRequest, final HttpHeaders headers) {
        final Revaluation revaluation = revaluationRequest.getRevaluation();
        final List<Long> assetIds = new ArrayList<>();
        final RequestInfo requestInfo = revaluationRequest.getRequestInfo();
        final String tenantId = revaluation.getTenantId();
        assetIds.add(revaluation.getAssetId());
        final Asset asset = assetRepository
                .findForCriteria(AssetCriteria.builder().tenantId(tenantId).id(assetIds).build()).get(0);
        log.debug("asset for revaluation :: " + asset);

        final AssetCategory assetCategory = asset.getAssetCategory();

        if (revaluation.getTypeOfChange().equals(TypeOfChangeEnum.INCREASED)) {
            log.info("subledger details check for Type of change INCREASED");

            final List<ChartOfAccountDetailContract> subledgerDetailsForAssetAccount = voucherService
                    .getSubledgerDetails(requestInfo, tenantId, assetCategory.getAssetAccount());
            final List<ChartOfAccountDetailContract> subledgerDetailsForRevaluationReserverAccount = voucherService
                    .getSubledgerDetails(requestInfo, tenantId, assetCategory.getRevaluationReserveAccount());

            if (subledgerDetailsForAssetAccount != null && subledgerDetailsForRevaluationReserverAccount != null
                    && !subledgerDetailsForAssetAccount.isEmpty()
                    && !subledgerDetailsForRevaluationReserverAccount.isEmpty())
                throw new RuntimeException("Subledger Details Should not be present for Chart Of Accounts");

        } else if (revaluation.getTypeOfChange().equals(TypeOfChangeEnum.DECREASED)) {
            log.info("subledger details check for Type of change DECREASED");

            final List<ChartOfAccountDetailContract> subledgerDetailsForAssetAccount = voucherService
                    .getSubledgerDetails(requestInfo, tenantId, assetCategory.getAssetAccount());
            final List<ChartOfAccountDetailContract> subledgerDetailsForFixedAssetWrittenOffAccount = voucherService
                    .getSubledgerDetails(requestInfo, tenantId, revaluation.getFixedAssetsWrittenOffAccount());

            if (subledgerDetailsForAssetAccount != null && subledgerDetailsForFixedAssetWrittenOffAccount != null
                    && !subledgerDetailsForAssetAccount.isEmpty()
                    && !subledgerDetailsForFixedAssetWrittenOffAccount.isEmpty())
                throw new RuntimeException("Subledger Details Should not be present for Chart Of Accounts");
        }
        final List<VouchercreateAccountCodeDetails> accountCodeDetails = getAccountDetails(revaluation, assetCategory,
                requestInfo);

        log.debug("Voucher Create Account Code Details :: " + accountCodeDetails);

        final VoucherRequest voucherRequest = voucherService.createVoucherRequest(revaluation, revaluation.getFund(),
                asset.getDepartment().getId(), accountCodeDetails, requestInfo, tenantId);
        log.debug("Voucher Request for Revaluation :: " + voucherRequest);

        return voucherService.createVoucher(voucherRequest, tenantId, headers);

    }

    private List<VouchercreateAccountCodeDetails> getAccountDetails(final Revaluation revaluation,
            final AssetCategory assetCategory, final RequestInfo requestInfo) {
        final List<VouchercreateAccountCodeDetails> accountCodeDetails = new ArrayList<VouchercreateAccountCodeDetails>();
        final Long functionId = revaluation.getFunction();
        final String tenantId = revaluation.getTenantId();
        if (assetCategory != null && revaluation.getTypeOfChange().equals(TypeOfChangeEnum.INCREASED)) {
            accountCodeDetails.add(voucherService.getGlCodes(requestInfo, tenantId, assetCategory.getAssetAccount(),
                    revaluation.getRevaluationAmount(), functionId, false, true));
            accountCodeDetails
                    .add(voucherService.getGlCodes(requestInfo, tenantId, assetCategory.getRevaluationReserveAccount(),
                            revaluation.getRevaluationAmount(), functionId, true, false));
        } else if (assetCategory != null && revaluation.getTypeOfChange().equals(TypeOfChangeEnum.DECREASED)) {
            accountCodeDetails
                    .add(voucherService.getGlCodes(requestInfo, tenantId, revaluation.getFixedAssetsWrittenOffAccount(),
                            revaluation.getRevaluationAmount(), functionId, false, true));
            accountCodeDetails.add(voucherService.getGlCodes(requestInfo, tenantId, assetCategory.getAssetAccount(),
                    revaluation.getRevaluationAmount(), functionId, true, false));

        }
        return accountCodeDetails;
    }

    private RevaluationResponse getRevaluationResponse(final List<Revaluation> revaluations) {
        final RevaluationResponse revaluationResponse = new RevaluationResponse();
        revaluationResponse.setRevaluations(revaluations);
        return revaluationResponse;
    }

}
