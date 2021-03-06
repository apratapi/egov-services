package org.egov.models;

import java.util.List;

import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
/**
 * 
 * 
 * @author Prasad Khandagale
 *
 */
public class Notice {

	@JsonProperty("upicNo")
	private String upicNo;

	@JsonProperty("tenantId")
	@Size(min=4,max=128)
	private String tenantId;

	@JsonProperty("ulbName")
	private String ulbName;

	@JsonProperty("ulbLogo")
	private String ulbLogo;

	@JsonProperty("noticeDate")
	private String noticeDate;

	@JsonProperty("noticeNumber")
	private String noticeNumber;

	@JsonProperty("address")
	private Address address;

	@JsonProperty("owners")
	private List<User> owners;

	@JsonProperty("applicationNo")
	private String applicationNo;

	@JsonProperty("applicationDate")
	private String applicationDate;
	
	@JsonProperty("floors")
	private List<FloorSpec> floors;
	
	@JsonProperty("taxDetails")
	private TotalTax taxDetails;

}