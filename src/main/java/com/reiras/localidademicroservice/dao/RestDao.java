package com.reiras.localidademicroservice.dao;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.reiras.localidademicroservice.domain.Localidade;

@Component
public class RestDao implements Dao {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RestDao.class);

	private static final String LOCALIDADES_ENDPOINT_URL = "https://servicodados.ibge.gov.br/api/v1/localidades/estados/{UF}/municipios";

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private CircuitBreakerFactory circuitBreakerFactory;
	
	@Override
	public Optional<Localidade> findLocalidadeBySiglaEstadoAndNomeCidade(String siglaEstado, String nomeCidade) {

		List<Localidade> localidadesList = this.findLocalidadeBySiglaEstado(siglaEstado);
		Object[] localidadeArray = localidadesList.stream().filter(localidade -> localidade.getNomeCidade().equalsIgnoreCase(nomeCidade)).toArray();

		if (localidadeArray.length == 0)
			return Optional.empty();

		Localidade localidade = (Localidade) localidadeArray[0];
		
		LOGGER.info(new StringBuffer("[findLocalidadeBySiglaEstadoAndNomeCidade]")
				.append(" Input=>{siglaEstado=").append(siglaEstado).append("}")
				.append("{nomeCidade=").append(nomeCidade).append("}")
				.append(" Output=>{").append(localidade).append("}").toString());
		
		return Optional.of(localidade);		
	}

	@Override
	public List<Localidade> findLocalidadeBySiglaEstado(String siglaEstado) {
		
		CircuitBreaker circuitBreaker = circuitBreakerFactory.create("circuitbreaker");
		
		ResponseEntity<List<Localidade>> response = circuitBreaker.run(() -> this.runLocalidadesEndpoint(siglaEstado));
		
		List<Localidade> localidadesList = response.getBody();
		
		LOGGER.info(new StringBuffer("[findLocalidadeBySiglaEstado]")
				.append(" Input=>{siglaEstado=").append(siglaEstado).append("}")
				.append(" Output=>{").append(localidadesList.getClass())
				.append(":").append(localidadesList.size()).append("items}").toString());
		
		return localidadesList;
	}

	private ResponseEntity<List<Localidade>> runLocalidadesEndpoint(String siglaEstado) {
		
		return restTemplate.exchange(
				LOCALIDADES_ENDPOINT_URL,
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<List<Localidade>>() { },
				siglaEstado);
	}
	
}
