package org.egov.pgr.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class AttributeDefinition {

    private boolean readOnly;
    private String dataType;
    private boolean required;
    private String dataTypeDescription;
    private int order;
    private String description;
    private String code;
    private String url;
    private String groupCode;

}

