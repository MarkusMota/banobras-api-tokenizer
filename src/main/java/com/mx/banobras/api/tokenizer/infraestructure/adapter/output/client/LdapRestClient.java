package com.mx.banobras.api.tokenizer.infraestructure.adapter.output.client;


/**
 * LdapRestClient.java:
 * 
 * Clase para conectarse la conexion con el servicio de autenticacion en LDAP. 
 *  
 * @author Marcos Gonzalez
 * @version 1.0, 13/06/2024
 * @see documento "MAR - Marco Arquitectonico de Referencia"
 * @since JDK 17
 */

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import javax.naming.NamingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mx.banobras.api.tokenizer.application.outputport.ILdapApiRestOutPort;
import com.mx.banobras.api.tokenizer.application.outputport.ILdapOutPort;
import com.mx.banobras.api.tokenizer.dominio.model.TokenizerDTO;

@Component
public class LdapRestClient implements ILdapApiRestOutPort {

	
	/** Trazas de la aplicación */
	Logger log = LogManager.getLogger(LdapRestClient.class);

	/** Variable que contiene la URL de conexion con el servicio de autenticacion con ldap */
	@Value("${app.url.ldap.auth}")
	String urlLdapAuth;

	/**
	 * Metodo para validar el usuario que exista en LDAP.
	 * 
	 * @param securityAuthInDTO componente que contiene los datos del token.
	 * 
	 * @return HttpResponse<String> regresa un objeto con los datos del token validado
	 * @throws InterruptedException 
	 * @throws IOException, InterruptedException
	 * 
	 */
	@Override
	public boolean authorizationLDAP(TokenizerDTO tokenizerDTO) throws IOException, InterruptedException{
		boolean resultResponse = false;
		HttpClient client = HttpClient.newHttpClient();
		HttpResponse<String> response = null;
		log.info("Inicia rest cliente LDAP");
		HttpRequest request = HttpRequest.newBuilder()
				.setHeader("credentials", tokenizerDTO.getCredentials())
				.setHeader("application", "")
				.setHeader("consumer-api-id", tokenizerDTO.getConsumerId())
				.setHeader("functional-id", tokenizerDTO.getFunctionalId())
				.setHeader("transaction-id", tokenizerDTO.getTransactionalId())
				.uri(URI.create(urlLdapAuth))
				.POST(HttpRequest.BodyPublishers.noBody()).build();

		response = client.send(request, HttpResponse.BodyHandlers.ofString());
		log.info(new StringBuilder().append("StatusCode: ").append(response.statusCode()));
		log.info("Finaliza rest cliente LDAP");
		
		if(response.statusCode() == 200) {
			resultResponse = true;
		}
		return resultResponse;
	}
	
}
