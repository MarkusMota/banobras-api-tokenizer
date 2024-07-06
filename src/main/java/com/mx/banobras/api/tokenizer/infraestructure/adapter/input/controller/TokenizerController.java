package com.mx.banobras.api.tokenizer.infraestructure.adapter.input.controller;

/**
 * TokenizerController.java:
 * 
 * Clase controller que expone los servicios Rest para validar y generar el
 * token
 * 
 * @author Marcos Gonzalez
 * @version 1.0, 13/06/2024
 * @see documento "MAR - Marco Arquitectonico de Referencia"
 * @since jdk 17
 */

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.Date;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.mx.banobras.api.tokenizer.application.inputport.ITokenizerInputPort;
import com.mx.banobras.api.tokenizer.common.util.CommonConstant;
import com.mx.banobras.api.tokenizer.dominio.model.TokenizerDTO;
import com.mx.banobras.api.tokenizer.infraestructure.adapter.input.dto.ErrorMessageDTO;
import com.mx.banobras.api.tokenizer.infraestructure.adapter.input.dto.TokenizerResponseDTO;

import jakarta.servlet.http.HttpServletRequest;

@CrossOrigin(originPatterns = { "*" })
@RestController
@RequestMapping("/banobras/tokenizer/v1/security")
public class TokenizerController {

	/** Trazas de la aplicación */
	Logger log = LogManager.getLogger(TokenizerController.class);

	/** Injection variable del objeto HttpServletRequest */
	private HttpServletRequest httRequest;

	/** Injection variable para la interfaz iTokenizerInputPort */
	private ITokenizerInputPort iTokenizerInputPort;

	/** Consturctor de las interfaces que usa el controller */
	public TokenizerController(ITokenizerInputPort iTokenizerInputPort, HttpServletRequest httRequest) {
		this.iTokenizerInputPort = iTokenizerInputPort;
		this.httRequest = httRequest;

	}

	/**
	 * Metodo para obtener el Token, para el consumo de los microservicios.
	 * 
	 * @param userName        - Alias del usuario.
	 * @param consumer-api-id - Nombre de la intefaz que lo va a consumir el
	 *                          microservicio.
	 * @param functional-id   - Acronimo de la funcionalidad.
	 * @param transaction-id  - Identificador de la transacción generado por UUDI.
	 * @param refresh-token   - Tiempo de duracion del refresh Token, si el valor es
	 *                          Cero, no se genera.
	 * 
	 * @return regresa el objeto TokenizerResponseDTO con los datos del Token
	 * @throws Exception Excepción durante el proceso de generar el Token.
	 * 
	 */
	@PostMapping("/token")
	public ResponseEntity<TokenizerResponseDTO> getToken(
			@RequestHeader(value = "credentials") String credentials,
			@RequestHeader(value = "consumer-api-id") String consumerApiId,
			@RequestHeader(value = "functional-id") String functionalId,
			@RequestHeader(value = "transaction-id") String transactionId,
			@RequestHeader(value = "refresh-token") Integer refreshToken) {

		TokenizerResponseDTO tokenizerResponseDTO = null;
		TokenizerDTO tokenizerDTO = null;
		ErrorMessageDTO errorMessage = null;
		log.info("Inicia crear token");
		try {
			/** String remoteHost = request.getRemoteHost(); */
			String remoteHost = getIpRemoteAdress();
			// Agrega parametros para que se muestren en el Log
			ThreadContext.put("transaction-id", transactionId);
			ThreadContext.put("ip", remoteHost);
			
			/** Inicializa el obejto con los valores de header */
			tokenizerDTO = new TokenizerDTO(credentials, null, null, null, consumerApiId, functionalId,
					transactionId, refreshToken);

			log.info("Invoca la intefaz para obtiener el token");
			tokenizerResponseDTO = iTokenizerInputPort.createToken(tokenizerDTO);

			// Valida el resultado en la generacio del token
			if (tokenizerResponseDTO.getStatusCode() == HttpStatus.OK.value()) {
				log.info("Se genera el token correctamente");
				return new ResponseEntity<>(tokenizerResponseDTO, HttpStatus.OK);
			} else {
				log.info("No se genera el token");
				return new ResponseEntity<>(tokenizerResponseDTO,
						HttpStatus.valueOf(tokenizerResponseDTO.getStatusCode()));
			}
		} catch (NullPointerException enull) {
			log.error(CommonConstant.NULL_POINTER_EXCEPTION.getName(), enull);
			errorMessage = new ErrorMessageDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(), new Date(), CommonConstant.MSG_ERROR_500.getName());
			tokenizerResponseDTO = new TokenizerResponseDTO();
			tokenizerResponseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			tokenizerResponseDTO.setErrorMessageDTO(errorMessage);
			return new ResponseEntity<>(tokenizerResponseDTO, HttpStatus.valueOf(tokenizerResponseDTO.getStatusCode()));
		} catch (IllegalArgumentException eil) {
			log.error(CommonConstant.ILLEGAL_ARG_EXCEPTION.getName(), eil);
			errorMessage = new ErrorMessageDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(), new Date(), eil.getMessage());
			tokenizerResponseDTO = new TokenizerResponseDTO();
			tokenizerResponseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			tokenizerResponseDTO.setErrorMessageDTO(errorMessage);
			return new ResponseEntity<>(tokenizerResponseDTO, HttpStatus.valueOf(tokenizerResponseDTO.getStatusCode()));
		} catch (Exception e) {
			log.error(CommonConstant.EXCEPTION.getName(), e);
			errorMessage = new ErrorMessageDTO(HttpStatus.SERVICE_UNAVAILABLE.value(), new Date(), CommonConstant.MSG_ERROR_503.getName());
			tokenizerResponseDTO = new TokenizerResponseDTO();
			tokenizerResponseDTO.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE.value());
			tokenizerResponseDTO.setErrorMessageDTO(errorMessage);
			return new ResponseEntity<>(tokenizerResponseDTO, HttpStatus.valueOf(tokenizerResponseDTO.getStatusCode()));
		} finally {
			ThreadContext.clearStack();
			log.info("Finaliza crear token");
		}
	}

	/**
	 * Metodo para obtener el Token, para el consumo de los microservicios.
	 * 
	 * @param userName        - Alias del usuario.
	 * @param auth-token      - Token generado por JWT, para su validacion.
	 * @param x-api-id        - Id de servicio que se va a consumir.
	 * @param consumer-api-id - Nombre de la intefaz que lo va a consumir el
	 *                        microservicio.
	 * @param functional-id   - Acronimo de la funcionalidad.
	 * @param transaction-id  - Identificador de la transacción generado por UUDI.
	 * 
	 * @throws Exception Excepción durante el proceso de generar el Token.
	 */

	@PostMapping("/valid")
	public ResponseEntity<TokenizerResponseDTO> validToken(
			@RequestHeader(value = "credentials") String credentials,
			@RequestHeader(value = "auth-token") String jwtToken, 
			@RequestHeader(value = "consumer-api-id") String consumerApiId,
			@RequestHeader(value = "functional-id") String functionalId,
			@RequestHeader(value = "transaction-id") String transactionId) {

		TokenizerResponseDTO tokenizerResponseDTO = null;
		TokenizerDTO tokenizerDTO = null;
		ErrorMessageDTO errorMessage = null;

		try {

			String remoteHost = getIpRemoteAdress();
			ThreadContext.put("transaction-id", transactionId);
			ThreadContext.put("ip", remoteHost);
			log.info("Inicia :: validar token");
			tokenizerDTO = new TokenizerDTO(credentials, null, null, jwtToken, consumerApiId, functionalId,
					transactionId, null);

			tokenizerResponseDTO = iTokenizerInputPort.validateToken(tokenizerDTO);

			if (tokenizerResponseDTO.getStatusCode() == 200) {
				log.info("Se genera el token correctamente");
				return new ResponseEntity<>(tokenizerResponseDTO, HttpStatus.OK);
			} else {
				log.info("No tiene los permisos para generar el token");
				return new ResponseEntity<>(tokenizerResponseDTO,
						HttpStatus.valueOf(tokenizerResponseDTO.getStatusCode()));
			}

		} catch (NullPointerException enull) {
			log.error(CommonConstant.NULL_POINTER_EXCEPTION.getName(), enull);
			errorMessage = new ErrorMessageDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(), new Date(), CommonConstant.MSG_ERROR_500.getName());
			tokenizerResponseDTO = new TokenizerResponseDTO();
			tokenizerResponseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			tokenizerResponseDTO.setErrorMessageDTO(errorMessage);
			return new ResponseEntity<>(tokenizerResponseDTO, HttpStatus.valueOf(tokenizerResponseDTO.getStatusCode()));
		} catch (IllegalArgumentException eil) {
			log.error(CommonConstant.ILLEGAL_ARG_EXCEPTION.getName(), eil);
			errorMessage = new ErrorMessageDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(), new Date(), eil.getMessage());
			tokenizerResponseDTO = new TokenizerResponseDTO();
			tokenizerResponseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			tokenizerResponseDTO.setErrorMessageDTO(errorMessage);
			return new ResponseEntity<>(tokenizerResponseDTO, HttpStatus.valueOf(tokenizerResponseDTO.getStatusCode()));
		} catch (Exception e) {
			log.error(CommonConstant.EXCEPTION.getName(), e);
			errorMessage = new ErrorMessageDTO(HttpStatus.SERVICE_UNAVAILABLE.value(), new Date(), e.getMessage());
			tokenizerResponseDTO = new TokenizerResponseDTO();
			tokenizerResponseDTO.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE.value());
			tokenizerResponseDTO.setErrorMessageDTO(errorMessage);
			return new ResponseEntity<>(tokenizerResponseDTO, HttpStatus.valueOf(tokenizerResponseDTO.getStatusCode()));
		} finally {
			ThreadContext.clearStack();
			log.info("Finaliza :: validar token");
		}
	}

	/**
	 * Metodo para obtener la IP del srevicio que invoca al controller.
	 * 
	 * @throws UnknownHostException
	 * 
	 * @throws Exception            Excepción durante el proceso de generar el
	 *                              Token.
	 */
	private String getIpRemoteAdress() throws UnknownHostException {
		log.info(new StringBuilder().append("Busca IP :: ").append(httRequest.getRemoteHost()));
		log.info(new StringBuilder().append(CommonConstant.BUSCA_POR.getName()).append(CommonConstant.H_X_FORWARDED_FOR.getName()));
		String ipAddress = "";
		if (buscaEnHeaderPor(CommonConstant.H_X_FORWARDED_FOR.getName())) {
			ipAddress = httRequest.getHeader(CommonConstant.H_X_FORWARDED_FOR.getName());
		} else {
			if (buscaEnHeaderPor(CommonConstant.H_PROXY_CLIENT_IP.name())) {
				ipAddress = httRequest.getHeader(CommonConstant.H_PROXY_CLIENT_IP.getName());
			} else {
				if (buscaEnHeaderPor(CommonConstant.H_WL_PROXY_CLIENT_IP.getName())) {
					ipAddress = httRequest.getHeader(CommonConstant.H_WL_PROXY_CLIENT_IP.getName());
				} else {
					if (buscaPorRemoteAddr()) {
						ipAddress = obtenValorPorRemoteAddr();
					}
				}
			}
		}
		log.info(new StringBuilder().append("La ip remota es :: ").append(ipAddress));
		return ipAddress;
	}

	/**
	 * Metodo para obtener la IP del srevicio que invoca al controller en el Header
	 * de la petición
	 * 
	 * @param cadenaABuscar cadena que contien el filtro a buscar en el header
	 * @return regresa un valor boolean, true si lo encuentra el valor
	 * 
	 */
	private boolean buscaEnHeaderPor(String cadenaABuscar) {
		String ipAddress = httRequest.getHeader(cadenaABuscar);
		boolean bandera = true;
		if (ipAddress == null || ipAddress.isEmpty() || CommonConstant.UNKNOWN.getName().equalsIgnoreCase(ipAddress)) {
			bandera = false;
		}
		return bandera;
	}

	/**
	 * Metodo para obtener la IP del srevicio que invoca al controller con el metodo
	 * geRemoteAdrr() de la petición
	 * 
	 * @return regresa un valor boolean, true si encuentra el valor
	 * 
	 */
	private boolean buscaPorRemoteAddr() throws UnknownHostException {
		String ipAddress = httRequest.getRemoteAddr();
		boolean bandera = false;
		if (CommonConstant.LOCALHOST_IPV4.getName().equals(ipAddress) || CommonConstant.LOCALHOST_IPV6.getName().equals(ipAddress)) {
			log.info("Busca por getLocalHost()");
			InetAddress inetAddress = InetAddress.getLocalHost();
			ipAddress = inetAddress.getHostAddress();
			bandera = true;
		}
		if (ipAddress != null && ipAddress.length() > 15 && ipAddress.contains(CommonConstant.COMMA.getName())) {
			bandera = true;
		}
		return bandera;
	}

	/**
	 * Metodo para obtener la IP del srevicio que invoca al controller con el metodo
	 * geRemoteAdrr() de la petición
	 * 
	 * @return regresa un valor de la IP en formato IPV4 o IPV6
	 * 
	 * @throws UnknownHostException Exception en caso de no poder validar los datos.
	 */
	private String obtenValorPorRemoteAddr() throws UnknownHostException {
		String ipAddress = httRequest.getRemoteAddr();
		if (ipAddress.equals(CommonConstant.LOCALHOST_IPV4.getName()) || ipAddress.equals(CommonConstant.LOCALHOST_IPV6.getName())) {
			log.info("Busca por getLocalHost()");
			InetAddress inetAddress = InetAddress.getLocalHost();
			ipAddress = inetAddress.getHostAddress();
		}
		if (ipAddress != null && ipAddress.length() > 15 && ipAddress.contains(CommonConstant.COMMA.getName())) {
			ipAddress = ipAddress.substring(0, ipAddress.indexOf(CommonConstant.COMMA.getName()));
		}
		return ipAddress;
	}


}
