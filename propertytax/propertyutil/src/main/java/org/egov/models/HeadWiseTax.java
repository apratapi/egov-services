package org.egov.models;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HeadWiseTax {

    @NotNull
    private String taxName;

    private Integer taxDays;

    @NotNull
    private Double taxValue;
}
