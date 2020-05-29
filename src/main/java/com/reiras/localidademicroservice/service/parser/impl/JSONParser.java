package com.reiras.localidademicroservice.service.parser.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reiras.localidademicroservice.domain.Localidade;
import com.reiras.localidademicroservice.exception.FileHandlingException;
import com.reiras.localidademicroservice.service.parser.Parser;

@Component("JSON")
public class JSONParser implements Parser {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CSVParser.class);
	
	private static final String OBJECT_SEPARATOR = ",";

	@Override
	public InputStream parse(List<Localidade> localidadeList) {

		ObjectMapper objectMapper = new ObjectMapper();
		StringBuffer strJson = new StringBuffer("[");
		
		if (localidadeList.isEmpty()) {
			strJson.append("]");
		} else {
			for (Localidade localidade : localidadeList) {

				try {
					strJson.append(objectMapper.writeValueAsString(localidade));
					strJson.append(OBJECT_SEPARATOR);
				} catch (JsonProcessingException e) {
					LOGGER.error("[parse] Error parsing Localidade to JSON", e.getMessage());
					throw new FileHandlingException("Error parsing Localidade to JSON", e);
				}

			}
			strJson.replace(strJson.length() - 1, strJson.length(), "]");
		}
		
		InputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(strJson.toString().getBytes()));

		LOGGER.info(new StringBuffer("[parse]")
				.append(" Input=>{localidadeList=").append(localidadeList.getClass()).append("}")
				.append(" List Size=>{").append(localidadeList.size()).append("}")
				.append(" Output=>{").append(inputStream).append("}").toString());

		return inputStream;
	}

}
