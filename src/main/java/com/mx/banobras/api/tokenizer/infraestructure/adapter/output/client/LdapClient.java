package com.mx.banobras.api.tokenizer.infraestructure.adapter.output.client;



/**
 * LdapClient.java:
 * 
 * Clase para conectarse en al directorio activo y validar el usuario . 
 *  
 * @author Marcos Gonzalez
 * @version 1.0, 13/06/2024
 * @see documento "MAR - Marco Arquitectonico de Referencia"
 * @since JDK 17
 */

import java.util.Hashtable;


import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.mx.banobras.api.tokenizer.application.outputport.ILdapOutPort;
import com.mx.banobras.api.tokenizer.dominio.model.TokenizerDTO;


@Component
public class LdapClient implements ILdapOutPort {

	/** Variable para las trazas de la clase */
	Logger log = LogManager.getLogger(LdapClient.class);

	/** Variable que contiene la url del ldap */
	@Value("${app.ldap.server}")
	String ldapServer;

	/** Variable que contiene el filtro para la busqueda en ldap */
	@Value("${app.ldap.search.base}")
	String ldapSearchBase;

	/** Variable que contiene el nombre de usuario de conexion en ldap */
	@Value("${app.ldap.username}")
	String ldapUsername;

	/** Variable que contiene el password de conexión de ldap */
	@Value("${app.ldap.password}")
	String ldapPassword;

	/** Variable para realizar la validacion en LDAP, si es igual a 1 */
	@Value("${app.ldap.validate}")
	boolean ldapValidate;

	/** Variable que contiene el valor del usuario a buscar en ldap */
	@Value("${app.ldap.username.search}")
	String ldapUserNameSearch;

	/**
	 * Metodo para buscar el usuario en LDAP.
	 * 
	 * @param userName - Alias del usuario.
	 * 
	 * @return regresa un valor booleano, si el valor es verdadero si encotro al
	 *         usario.
	 * @throws NamingException
	 * 
	 */
	
	public LdapVO findByUsername(TokenizerDTO tokenizerDTO) throws NamingException {
		
		/** Objeto para guardar los datos que provienen de LDAP */
		LdapVO dataLdapVO = null;
		String userName = null;

		/** Condicion para validar en LDAP */
		if (ldapValidate) {
			log.info("Se valida usuario en LDAP");
			if (ldapUserNameSearch != null && !ldapUserNameSearch.isEmpty() ) {
				log.info("La validacion es por usuario de prueba");
				userName = ldapUserNameSearch;
			}else {
				log.info("La validacion es por usuario en credentials.");
				userName = tokenizerDTO.getUserName();
				
			}

			Hashtable env = new Hashtable();
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			if (ldapUsername != null) {
				env.put(Context.SECURITY_PRINCIPAL, ldapUsername);
			}
			if (ldapPassword != null) {
				env.put(Context.SECURITY_CREDENTIALS, ldapPassword);
			}
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			env.put(Context.PROVIDER_URL, ldapServer);

			InitialDirContext ctx = new InitialDirContext(env);

			/** Busca un usuario en especifico */
			String searchFilter = "(samaccountName=" + userName + ")";
			
			
			/** crea los filtros a buscar en LDAP */
			String[] reqAtt = { "uid","cn", "sn", "initials","displayname", 
					            "mail", "department", "company", "samaccountname",
					            "userprincipalname", "title", "mailNickname",
					            "telephoneNumber", "initials"};
			SearchControls controls = new SearchControls();
			controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			controls.setReturningAttributes(reqAtt);
			
			NamingEnumeration<SearchResult> objs = ctx.search(ldapSearchBase, searchFilter, controls);
			if (objs.hasMoreElements()) {
				log.info(new StringBuilder().append("Si existe el usuario en LDAP: ").append(userName));
				dataLdapVO = new LdapVO(null, null, "123456", "1222",
						"PEREZ PEREZ, MARIO", "EXPERTO TEXNICO", "SUBGERENCIA DE CARRETERAS ESTATALES", "1432", 1,
						" mperez@banobras.gob.mx", "Mario.perez@banobras.gob.mx", null);
				
				
			} else {
				log.info(new StringBuilder().append("No existe el usuario en LDAP."));
				dataLdapVO = null;
			}

		} else {
			log.info("No!! se valida por LDAP - se usa datos dummy.");
			dataLdapVO = new LdapVO(null, null, "123456", "1222",
					"PEREZ PEREZ, MARIO", "EXPERTO TEXNICO", "SUBGERENCIA DE CARRETERAS ESTATALES", "1432", 1,
					" mperez@banobras.gob.mx", "Mario.perez@banobras.gob.mx", null);

		}
		
		return dataLdapVO;
	}

}
