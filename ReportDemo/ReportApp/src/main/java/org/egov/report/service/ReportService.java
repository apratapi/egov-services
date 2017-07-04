package org.egov.report.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.egov.domain.model.ReportDefinitions;
import org.egov.domain.model.ReportYamlMetaData;
import org.egov.domain.model.ReportYamlMetaData.sourceColumns;
import org.egov.report.repository.ReportRepository;
import org.egov.swagger.model.ReportRequest;
import org.egov.swagger.model.ReportResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportService {
	
	@Autowired
	public ReportDefinitions reportDefinitions;
	
	@Autowired
	private ReportRepository reportRepository;

	public ReportResponse getReportData(ReportRequest reportRequest){
		List<ReportYamlMetaData> reportYamlMetaDatas = reportDefinitions.getReportDefinitions();
		ReportYamlMetaData reportYamlMetaData = reportYamlMetaDatas.stream().
				filter(t -> t.getReportName().equals(reportRequest.getReportName())).findFirst().orElse(null);
		List<Map<String, Object>> maps = reportRepository.getData(reportRequest, reportYamlMetaData);
		List<sourceColumns> columns = reportYamlMetaData.getSourceColumns();
		System.out.println("columns::"+columns);
		return populateData(columns, maps);
	}
	
	private ReportResponse populateData(List<sourceColumns> columns, List<Map<String, Object>> maps){
		
		ReportResponse reportResponse = new ReportResponse();
		//reportResponse.setReportHeader(reportHeader);
		List<List<Object>> lists = new ArrayList<>();
		
		for(int i=0; i<maps.size(); i++){
			List<Object> objects = new ArrayList<>();
			Map<String, Object> map = maps.get(i);
			System.out.println("map:"+map);
			for(sourceColumns sourceColm : columns){
				System.out.println("sourceColm:"+sourceColm);
				objects.add(map.get(sourceColm.getName()));
			}
			lists.add(objects);
		}
		reportResponse.setReportData(lists);
		
		return reportResponse;
	}
}