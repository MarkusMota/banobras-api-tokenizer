package com.mx.banobras.api.tokenizer.dominio.model;

/**
 * Tokenizer.java:
 * 
 * Clase de dominio para la creación y validación del token. 
 *  
 * @author Marcos Gonzalez
 * @version 1.0, 13/06/2024
 * @see documento "MAR - Marco Arquitectonico de Referencia"
 * @since JDK 17
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.mx.banobras.api.tokenizer.common.util.CommonConstant;
import com.mx.banobras.api.tokenizer.infraestructure.adapter.input.dto.ErrorMessageDTO;
import com.mx.banobras.api.tokenizer.infraestructure.adapter.input.dto.TokenDTO;
import com.mx.banobras.api.tokenizer.infraestructure.adapter.input.dto.TokenizerResponseDTO;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;

@Data
@Component
public class Tokenizer {

	/** Trazas de la aplicación */
	Logger log = LogManager.getLogger(Tokenizer.class);
	
	/** Variable que contiene el metodo de autenticacion */
	@Value("${app.method.auth}")
	String methodAuth;

	/** Variable que contiene la ruta del certificado privado */
	@Value("${app.file.privatekey}")
	String filePrivateKey;
	/** Variable que contiene la ruta del certificado publico */
	@Value("${app.file.publickey}")
	String filePublicKey;

	/** Variable que contiene el password del certificado */
	@Value("${app.cert.password}")
	String certPassword;

	/** Variable que contiene la llave secreta */
	@Value("${app.secret.key}")
	String superScretKey;
	

	/**
	 * Constante que contiene el tiempo de expiración del token, igual a 1 minuto
	 */
	static final long TOKEN_EXPIRATION_TIME = 60000;
	/**
	 * Constante que contiene el tiempo de expiración del tokenRefreh, , igual a 1
	 * minuto
	 */
	static final long TOKEN_EXPIRATION_TIME_REFRESH = 60000;
	/** Constante que contiene el string Bearer para agregarla al token */
	static final String BEARER = "Bearer ";
	/** Constante que contiene el string username para buscar el token */
	static final String USER_NAME = "username";

	/** Constante que contiene el string BANOBRAS para crear el token */
	static final String ID_BANOBRAS = "BANOBRAS";
	
	
	/**
	 * Metodo para crear el Token
	 * 
	 * @param tokenizerDTO Objeto que contiene los datos para crear el token.
	 * 
	 * @return regresa el obejto TokenizerResponseDTO con los datos del Token
	 * 
	 */
	public TokenizerResponseDTO getToken(TokenizerDTO tokenizerDTO) {
		/** Variable para guardar el token */
		String token = null;
		
		/** Variable para guardar el refreshToken */
		String refreshToken = null;
		
		/** Variable para guardar el tipo de generación del token */
		Key signedKey = null;
		
		/**
		 * Variable para guardar el tipo de firma de algoritmo de generación del token
		 */
		SignatureAlgorithm signatureAlgorithm = null;
		
		/** Variable para los datos del respuesta de generación del token */
		TokenizerResponseDTO tokenizerResponseDTO = null;
		log.info(new StringBuffer().append("Metodo token - ").append(methodAuth));
		/** Valida el tipo de metodo de generación del token */
		if (methodAuth.equalsIgnoreCase("RSA")) {
			/** Gneración del token de tipo RSA */
			signedKey = getRSAPrivateKey();
			signatureAlgorithm = SignatureAlgorithm.RS256;
		} else {
			/** Gneración del token por llave secreta */
			signedKey = getSignedKey(superScretKey);
			signatureAlgorithm = SignatureAlgorithm.HS512;
		}
		try {
			token = createToken(tokenizerDTO, signedKey, signatureAlgorithm, TOKEN_EXPIRATION_TIME);
			token = BEARER + token;
			if (tokenizerDTO.getRefreshToken() > 0) {
				refreshToken = createToken(tokenizerDTO, signedKey, signatureAlgorithm,
						TOKEN_EXPIRATION_TIME * tokenizerDTO.getRefreshToken());
				refreshToken = BEARER + refreshToken;
			}
			tokenizerResponseDTO = new TokenizerResponseDTO();
			tokenizerResponseDTO.setStatusCode(HttpStatus.OK.value());
			tokenizerResponseDTO.setTokenDTO(new TokenDTO(true, token, refreshToken));
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error(CommonConstant.EXCEPTION.getName(), ex);
			Date date = new Date();
			ErrorMessageDTO message = new ErrorMessageDTO(500, date, ex.getMessage());
			tokenizerResponseDTO = new TokenizerResponseDTO();
			tokenizerResponseDTO.setStatusCode(HttpStatus.FORBIDDEN.value());
			tokenizerResponseDTO.setErrorMessageDTO(message);
		}
		
		return tokenizerResponseDTO;
	}

	/**
	 * Metodo para validar el Token
	 * 
	 * @param tokenizerDTO Objeto que contiene los datos para crear el token.
	 * 
	 * @return regresa el obejto TokenizerResponseDTO con los datos del Token
	 * 
	 */
	public TokenizerResponseDTO validaToken(TokenizerDTO tokenizerDTO) {

		TokenizerResponseDTO tokenizerResponseDTO = null;
		
		try {
			
			tokenizerResponseDTO = validaUsuarioToken(tokenizerDTO );
			if (tokenizerResponseDTO== null){
				log.info("tokenizerResponseDTO == null");
				Date date = new Date();
				ErrorMessageDTO message = new ErrorMessageDTO(HttpStatus.FORBIDDEN.value(), date,
						CommonConstant.MSG_TOKEN_NULO.getName());
				tokenizerResponseDTO = new TokenizerResponseDTO();
				tokenizerResponseDTO.setStatusCode(HttpStatus.FORBIDDEN.value());
				tokenizerResponseDTO.setErrorMessageDTO(message);
			}

		} catch (ExpiredJwtException ex) {
			ex.printStackTrace();
			log.error(CommonConstant.EXPIRED_JWT_EXCEPTION.getName(), ex);
			ErrorMessageDTO message = new ErrorMessageDTO(HttpStatus.UNAUTHORIZED.value(), new Date(), CommonConstant.MSG_TOKEN_EXPIRED.getName());
			tokenizerResponseDTO = new TokenizerResponseDTO();
			tokenizerResponseDTO.setStatusCode(HttpStatus.UNAUTHORIZED.value());
			tokenizerResponseDTO.setErrorMessageDTO(message);
		} catch (MalformedJwtException ex) {
			ex.printStackTrace();
			log.error(CommonConstant.MALFORMED_JWT_EXCEPTION.getName(), ex);
			ErrorMessageDTO message = new ErrorMessageDTO(HttpStatus.FORBIDDEN.value(), new Date(), CommonConstant.MSG_TOKEN_NULO.getName());
			tokenizerResponseDTO = new TokenizerResponseDTO();
			tokenizerResponseDTO.setStatusCode(HttpStatus.FORBIDDEN.value());
			tokenizerResponseDTO.setErrorMessageDTO(message);

		} catch (Exception ex) {
			ex.printStackTrace();
			log.error(CommonConstant.EXCEPTION.getName(), ex);
			ErrorMessageDTO message = new ErrorMessageDTO(HttpStatus.FORBIDDEN.value(), new Date(), CommonConstant.MSG_TOKEN_NULO.getName());
			tokenizerResponseDTO = new TokenizerResponseDTO();
			tokenizerResponseDTO.setStatusCode(HttpStatus.FORBIDDEN.value());
			tokenizerResponseDTO.setErrorMessageDTO(message);
		}
		
		return tokenizerResponseDTO;

	}

	public static Key getSignedKey(String secretKey) {
		byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public String extractTransaction(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	private Claims extractAllClaims(String token) {

		if (methodAuth.equalsIgnoreCase("RSA")) {
			return Jwts.parserBuilder().setSigningKey(getRSAPublicKey()).build().parseClaimsJws(token).getBody();
		} else {
			return Jwts.parserBuilder().setSigningKey(getSignedKey(superScretKey)).build().parseClaimsJws(token)
					.getBody();
		}

	}

	private static String createToken(TokenizerDTO tokenizerDTO, Key signedKey, SignatureAlgorithm signatureAlgorithm,
			long expirationTime) {
		String token = null;
		token = Jwts.builder().setHeaderParam(Header.TYPE, Header.JWT_TYPE)
				.setHeaderParam(JwsHeader.ALGORITHM, signatureAlgorithm.toString())
				.setId(ID_BANOBRAS)
				.setSubject(tokenizerDTO.getUserName())
				.setAudience(tokenizerDTO.getTransactionalId())
				.setIssuer(tokenizerDTO.getUserName())
				.claim(USER_NAME, tokenizerDTO.getUserName())
				.claim("consumerId", tokenizerDTO.getConsumerId())
				.claim("functionalId", tokenizerDTO.getFunctionalId())
				.claim("transaccionId", tokenizerDTO.getTransactionalId())
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + expirationTime))
				.signWith(signedKey, signatureAlgorithm).compact();

		return token;
	}

	private Key getRSAPrivateKey() {
		Key key = null;
		
		try(InputStream targetStream = new FileInputStream(new File(filePrivateKey))) {
			KeyStore keystore;
			keystore = KeyStore.getInstance("PKCS12");
			keystore.load(targetStream, certPassword.toCharArray());
			key = keystore.getKey("5", certPassword.toCharArray());
		} catch (Exception e) {
			e.printStackTrace();
			log.error(CommonConstant.EXCEPTION.getName(), e);
		}
		return key;
	}

	private Jws<Claims> verifyJWTRSA(String jwtToken, TokenizerDTO tokenizerDTO) {
		return Jwts.parserBuilder()
				.requireAudience(tokenizerDTO.getTransactionalId())
				.requireIssuer(tokenizerDTO.getUserName())
				.require("transaccionId", tokenizerDTO.getTransactionalId())
				.setSigningKey(getRSAPublicKey()).build().parseClaimsJws(jwtToken);
	}

	private Key getRSAPublicKey() {
		Key key = null;
		
		try(InputStream targetStream = new FileInputStream(new File(filePublicKey))) {
			CertificateFactory fact = CertificateFactory.getInstance("X.509");
			X509Certificate cer = (X509Certificate) fact.generateCertificate(targetStream);
			key = cer.getPublicKey();

		} catch (Exception e) {
			e.printStackTrace();
			log.error(CommonConstant.EXCEPTION.getName(), e);
		}
		return key;
	}

	
	private TokenizerResponseDTO validaUsuarioToken(TokenizerDTO tokenizerDTO) {
		log.info("Busca usuario en el token.");
		String jwtUsername = extractUsername(tokenizerDTO.getJwtToken().replace(BEARER, ""));
		
		TokenizerResponseDTO tokenizerResponseDTO = null;
		
		Claims claims = null;
		Jws<Claims> claimsJws = null;
		log.info("Valida el usuario en el token.");
		if (jwtUsername.contains(tokenizerDTO.getUserName())) {
			log.info("Token y usuario correctos");
			String jwtToken = tokenizerDTO.getJwtToken().replace(BEARER, "");

			if (methodAuth.equalsIgnoreCase("RSA")) {
				claimsJws = verifyJWTRSA(jwtToken, tokenizerDTO);

				for (String key : claimsJws.getBody().keySet()) {
					if (USER_NAME.equals(key)) {
						tokenizerResponseDTO = new TokenizerResponseDTO();
						tokenizerResponseDTO.setStatusCode(HttpStatus.OK.value());
						tokenizerResponseDTO.setTokenDTO(new TokenDTO(true, null, null));
						break;
					}
				}

			} else {

				claims = Jwts.parserBuilder().setSigningKey(getSignedKey(superScretKey)).build()
						.parseClaimsJws(jwtToken).getBody();

				if (claims != null && claims.get(USER_NAME) != null) {
					tokenizerResponseDTO = new TokenizerResponseDTO();
					tokenizerResponseDTO.setStatusCode(HttpStatus.OK.value());
					tokenizerResponseDTO.setTokenDTO(new TokenDTO(true,  null, null));
				}
			}
		}
		return tokenizerResponseDTO;
	}
}
