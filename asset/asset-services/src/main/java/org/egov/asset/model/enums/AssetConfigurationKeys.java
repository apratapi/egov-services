package org.egov.asset.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AssetConfigurationKeys {

	ENABLEVOUCHERGENERATION("EnableVoucherGeneration"), REVALUATIONVOUCHERNAME(
			"AssetRevaluationVoucherName"), REVALUATIONVOUCHERDESCRIPTION(
					"AssetRevaluationVoucherDescription"), DISPOSALVOUCHERNAME(
							"AssetDisposalVoucherName"), DISPOSALVOUCHERDESCRIPTION(
									"AssetDisposalVoucherDescription"), ASSETDEFAULTCAPITALIZEDVALUE(
											"AssetDefaultCapitalizedValue"), ASSETMINIMUMVALUE(
													"AssetMinimumValue"), DEPRECIATIONSEPARATIONDATE(
															"DepreciationSeparationDate");

    private String value;

    AssetConfigurationKeys(final String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static AssetConfigurationKeys fromValue(final String text) {
        for (final AssetConfigurationKeys b : AssetConfigurationKeys.values())
            if (String.valueOf(b.value).equals(text))
                return b;
        return null;
    }

}
