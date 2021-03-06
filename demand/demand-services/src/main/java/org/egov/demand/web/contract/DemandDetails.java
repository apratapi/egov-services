package org.egov.demand.web.contract;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class DemandDetails {
	private Long id;
	private BigDecimal taxAmount;
	private BigDecimal collectionAmount;
	private BigDecimal rebateAmount;
	private String taxReason;
	private String taxPeriod;
	@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "IST")
	private Date periodStartDate;
	@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "IST")
	private Date periodEndDate;
	private String glCode;
	private Integer isActualDemand;
	private String tenantId;
}
