package com.mx.banobras.api.tokenizer.infraestructure.adapter.input.dto;
/**
 * TokenizerResponseDTO.java:
 * 
 * Objeto que contiene los datos de respuesta de generacion del Token. 
 *  
 * @author Marcos Gonzalez
 * @version 1.0, 13/06/2024
 * @see Documento "MAR - Marco Arquitectonico de Referencia"
 * @since JDK 17
 */
import lombok.Data;

@Data
public class TokenizerResponseDTO {
	private Integer statusCode;
	private TokenDTO tokenDTO;
	private ErrorMessageDTO errorMessageDTO;
}


