package org.egov.egf.instrument.domain.service;

import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.domain.exception.CustomBindException;
import org.egov.common.domain.model.Pagination;
import org.egov.egf.instrument.domain.model.SurrenderReason;
import org.egov.egf.instrument.domain.model.SurrenderReasonSearch;
import org.egov.egf.instrument.domain.repository.SurrenderReasonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.SmartValidator;

@Service
@Transactional(readOnly = true)
public class SurrenderReasonService {

	public static final String ACTION_CREATE = "create";
	public static final String ACTION_UPDATE = "update";
	public static final String ACTION_VIEW = "view";
	public static final String ACTION_EDIT = "edit";
	public static final String ACTION_SEARCH = "search";

	@Autowired
	private SurrenderReasonRepository surrenderReasonRepository;

	@Autowired
	private SmartValidator validator;

	@Autowired
	public SurrenderReasonService(SmartValidator validator, SurrenderReasonRepository surrenderReasonRepository) {
		this.validator = validator;
		this.surrenderReasonRepository = surrenderReasonRepository;
	}

	@Transactional
	public List<SurrenderReason> create(List<SurrenderReason> surrenderReasons, BindingResult errors,
			RequestInfo requestInfo) {

		try {

			surrenderReasons = fetchRelated(surrenderReasons);

			validate(surrenderReasons, ACTION_CREATE, errors);

			if (errors.hasErrors()) {
				throw new CustomBindException(errors);
			}

		} catch (CustomBindException e) {

			throw new CustomBindException(errors);
		}

		return surrenderReasonRepository.save(surrenderReasons, requestInfo);

	}

	@Transactional
	public List<SurrenderReason> update(List<SurrenderReason> surrenderReasons, BindingResult errors,
			RequestInfo requestInfo) {

		try {

			surrenderReasons = fetchRelated(surrenderReasons);

			validate(surrenderReasons, ACTION_UPDATE, errors);

			if (errors.hasErrors()) {
				throw new CustomBindException(errors);
			}

		} catch (CustomBindException e) {

			throw new CustomBindException(errors);
		}

		return surrenderReasonRepository.update(surrenderReasons, requestInfo);

	}

	private BindingResult validate(List<SurrenderReason> surrenderreasons, String method, BindingResult errors) {

		try {
			switch (method) {
			case ACTION_VIEW:
				// validator.validate(surrenderReasonContractRequest.getSurrenderReason(),
				// errors);
				break;
			case ACTION_CREATE:
				Assert.notNull(surrenderreasons, "SurrenderReasons to create must not be null");
				for (SurrenderReason surrenderReason : surrenderreasons) {
					validator.validate(surrenderReason, errors);
				}
				break;
			case ACTION_UPDATE:
				Assert.notNull(surrenderreasons, "SurrenderReasons to update must not be null");
				for (SurrenderReason surrenderReason : surrenderreasons) {
					validator.validate(surrenderReason, errors);
				}
				break;
			default:

			}
		} catch (IllegalArgumentException e) {
			errors.addError(new ObjectError("Missing data", e.getMessage()));
		}
		return errors;

	}

	public List<SurrenderReason> fetchRelated(List<SurrenderReason> surrenderreasons) {

		return surrenderreasons;
	}

	public Pagination<SurrenderReason> search(SurrenderReasonSearch surrenderReasonSearch) {
		return surrenderReasonRepository.search(surrenderReasonSearch);
	}

	@Transactional
	public SurrenderReason save(SurrenderReason surrenderReason) {
		return surrenderReasonRepository.save(surrenderReason);
	}

	@Transactional
	public SurrenderReason update(SurrenderReason surrenderReason) {
		return surrenderReasonRepository.update(surrenderReason);
	}

}