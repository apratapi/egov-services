package org.egov.lams.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.egov.lams.model.Notice;
import org.egov.lams.model.NoticeCriteria;
import org.egov.lams.repository.builder.NoticeQueryBuilder;
import org.egov.lams.repository.rowmapper.NoticeRowMapper;
import org.egov.lams.web.contract.NoticeRequest;
import org.egov.lams.web.contract.RequestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class NoticeRepository {
	
	public static final Logger LOGGER = LoggerFactory.getLogger(NoticeRepository.class);
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	public Notice createNotice(NoticeRequest noticeRequest) {
		
		RequestInfo requestInfo = noticeRequest.getRequestInfo();
		Notice notice= noticeRequest.getNotice();
		notice.setNoticeNo(getNoticeNo());
		
		Object[] obj = new Object[] { notice.getNoticeNo(), notice.getNoticeDate(),notice.getAgreementNumber(),
				notice.getAssetCategory(), notice.getAcknowledgementNumber(),notice.getAssetNo(), notice.getAllotteeName(), 
				notice.getAllotteeAddress(),notice.getAllotteeMobileNumber(),notice.getAgreementPeriod(),notice.getCommencementDate(),
				notice.getTemplateVersion(),notice.getExpiryDate(),notice.getRent(),notice.getSecurityDeposit(),notice.getCommissionerName(),
				notice.getZone(),notice.getWard(),notice.getStreet(),notice.getElectionward(),notice.getLocality(),notice.getBlock(),
				requestInfo.getRequesterId(), new Date(), requestInfo.getRequesterId(), new Date(),
				notice.getTenantId(),notice.getRentInWord()};
		
		try{
			jdbcTemplate.update(NoticeQueryBuilder.INSERT_NOTICE_QUERY, obj);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
		return notice;
	}
	
	//public List<Notice> get
	
	private String getNoticeNo() {
		
		Integer result = jdbcTemplate.queryForObject(NoticeQueryBuilder.SEQ_NOTICE_NO,Integer.class);
		StringBuilder noticeNo=null;
		
		try{
			noticeNo = new StringBuilder(String.format("%09d", result));
		}catch(Exception ex){
			LOGGER.info("the exception from notice repo getnoticeno :: "+ex);
			ex.printStackTrace();
		}
		
		return noticeNo.toString();
	}	
	
	public List<Notice> getNotices(NoticeCriteria noticeCriteria){
		
		List<Notice> notices = null;
		List<Object> preparedStatementValues = new ArrayList<>();
		String queryString = NoticeQueryBuilder.getNoticeQuery(noticeCriteria, preparedStatementValues);
		try {
			notices = jdbcTemplate.query(queryString, preparedStatementValues.toArray(),new NoticeRowMapper());
		} catch (DataAccessException e) {
			LOGGER.info("the exception from notice repo query :: "+e);
			throw new RuntimeException(e.getMessage());
		}
		return notices;
	}

}
