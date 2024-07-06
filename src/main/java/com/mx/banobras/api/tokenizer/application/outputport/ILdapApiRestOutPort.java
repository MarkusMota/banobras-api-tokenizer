package com.mx.banobras.api.tokenizer.application.outputport;

import java.io.IOException;
import javax.naming.NamingException;
import com.mx.banobras.api.tokenizer.dominio.model.TokenizerDTO;
import com.mx.banobras.api.tokenizer.infraestructure.adapter.output.client.LdapVO;

/**
 * ILdapOutPort.java:
 * 
 * Interface que contiene los metodos para buscar el usuario en ldap.
 * 
 * @author Marcos Gonzalez
 * @version 1.0, 13/06/2024
 * @see documento "MAR - Marco Arquitectonico de Referencia"
 * @since JDK 17
 */
public interface ILdapApiRestOutPort {

	/**
	 * Metodo para buscar el usuario en LDAP, mediante el servicio rest.
	 * 
	 * @param tokenizerDTO contiene los datos de usuario y password a buscar.
	 * @return HttpResponse<String> regresa el objeto con datos, si existe el usuario.
	 * 
	 * @throws NamingException excepción durante el proceso de generar el Token.
	 */
	public boolean authorizationLDAP(TokenizerDTO tokenizerDTO)  throws IOException, InterruptedException ;
	
}
