package org.egov;
import java.io.File;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.egov.domain.model.ReportDefinitions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;



@SpringBootApplication
public class ReportApp {
	
	
	
	@Autowired
	public ReportDefinitions reportDefintions;
	
	@Autowired
	public static ResourceLoader resourceLoader;

	
    public ReportApp(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

	public static void main(String[] args) throws Exception {
		SpringApplication.run(ReportApp.class, args);
	}
	
	@Bean
    public MappingJackson2HttpMessageConverter jacksonConverter() {
        final MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        converter.setObjectMapper(mapper);
        return converter;
    }
	 
	  
	  @Bean("reportDefintions")
	  public static ReportDefinitions loadYaml() {
	      ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    try {
    	Resource resource = resourceLoader.getResource("classpath:application.yml");
        File yamlFile = resource.getFile();
        ReportDefinitions reportDefinitions = mapper.readValue(yamlFile, ReportDefinitions.class);
        return reportDefinitions;
    } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    return null;
    }
}