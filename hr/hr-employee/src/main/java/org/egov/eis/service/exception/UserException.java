package org.egov.eis.service.exception;

import lombok.Getter;
import org.egov.eis.web.contract.RequestInfo;
import org.egov.eis.web.errorhandler.UserErrorResponse;

public class UserException extends RuntimeException {

	private static final long serialVersionUID = -1068942413966014777L;

	@Getter
	private UserErrorResponse userErrorResponse;

	@Getter
	private RequestInfo requestInfo;

	public UserException(UserErrorResponse userErrorResponse, RequestInfo requestInfo) {
		super(userErrorResponse != null ? userErrorResponse.getError().getMessage()
				: "Unknown Error Occurred While Calling User Service");
		this.userErrorResponse = userErrorResponse;
		this.requestInfo = requestInfo;
	}

}
