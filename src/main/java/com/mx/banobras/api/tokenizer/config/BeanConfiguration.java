package com.mx.banobras.api.tokenizer.config;

/**
 * BeanConfiguration.java:
 * 
 * Clase de configuracion de BEANS.
 *  
 * @author Marcos Gonzalez
 * @version 1.0, 13/06/2024
 * @see Documento "MAR - Marco Arquitectonico de Referencia"
 * @since JDK 17
 */

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.mx.banobras.api.tokenizer.application.service.TokenizerUseCaseService;
import com.mx.banobras.api.tokenizer.common.util.CipherAESCommon;
import com.mx.banobras.api.tokenizer.dominio.model.Tokenizer;
import com.mx.banobras.api.tokenizer.infraestructure.adapter.output.client.LdapRestClient;
import com.mx.banobras.api.tokenizer.application.outputport.ILdapApiRestOutPort;
import com.mx.banobras.api.tokenizer.application.outputport.ILdapOutPort;


@Configuration
public class BeanConfiguration {

	/**
	 * Metodo para obtener crear la inyección del caso de uso del Tokenizer.
	 * 
	 * @param tokenizer - Objeto Toenizer para crear el token.
	 * @param iLdapApiRestOutPort Interfaz para validar con LDAP el usuario.
	 * @param CipherAESCommon - Componente para cifrar datos.
	 * 
	 * @throws none.
	 */
	@Bean
	TokenizerUseCaseService tokenizerUseCaseService(
			Tokenizer tokenizer, 
			ILdapOutPort iLdapOutPort,
			ILdapApiRestOutPort iLdapApiRestOutPort,
			CipherAESCommon cipherAESCommon) {
		return new TokenizerUseCaseService(	tokenizer, iLdapOutPort, iLdapApiRestOutPort,
				 cipherAESCommon);
	}
}
