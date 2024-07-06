package com.mx.banobras.api.tokenizer;

/**
 * BanobrasApiTokenizerApplication.java:
 * 
 * Clase principal para levantar la aplicacion de Api Tokenizer.
 * 
 * @author Marcos Gonzalez
 * @version 1.0, 13/06/2024
 * @see Documento "MAR - Marco Arquitectonico de Referencia"
 * @since JDK 17
 */

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class })
public class BanobrasApiTokenizerApplication implements CommandLineRunner{
	
	/** Variable para imprimir los logs */
	private static final Log LOG = LogFactory.getLog(BanobrasApiTokenizerApplication.class);

	/**
	 * Metodo principal para levantar la aplicacion.
	 * 
	 * @param String[] - Parametros 
	 * 
	 */
	public static void main(String[] args) {
		SpringApplication.run(BanobrasApiTokenizerApplication.class, args);
	}

	
	/**
	 * Metodo imnprimer el inicio de Api Tokenizer.
	 * 
	 * @param String[] - Parametros 
	 * 
	 * @throws Exception Excepción durante el proceso de generar el Token.
	 */
	@Override
	public void run(String... args) throws Exception {
		/** Variable para imprimir la diagonal inversa "\" */
		final char dInv = 92;
		final String SPACES ="      "; 
		LOG.info(SPACES);
		LOG.info("            .   ______       _  __ _ _");
		LOG.info("           " + "/" + dInv + dInv + " |  __  |_____(_) " + dInv + " " + dInv + " " + dInv + " " + dInv );
		LOG.info("          ( ( )| |__| |  _  | |  " + dInv + " " + dInv + " " + dInv + " " + dInv);
		LOG.info("           " + dInv +  dInv + "/ |  __  | |_| | |   ) ) ) )");
		LOG.info("            '  | |  | |  ___|_|  / / / /");
		LOG.info("           ===========|_|======/_/_/_/");
		LOG.info("");
		LOG.info("    _ _ __  _______    _                  _");
		LOG.info("   / / / / |__   __|__| | __ ____ _  ___ (_)_____ ____ _____   ");
		LOG.info("  / / / /     | |  _  | |/ /|  __| " + dInv + "/__ " + dInv +"| |___  |  __|  _  |   .");
		LOG.info(" / / / /      | | | | |   / | |__| /   | | |  / /| |__| |_| |  /" + dInv + dInv  );
		LOG.info("( ( ( (       | | | | | | " + dInv + " |  __| |   | | | / / |  __|    _| ( ( )");
		LOG.info(" " + dInv + " " + dInv + " " + dInv + " " + dInv + "      | | |_| | |" + dInv + " " + dInv + "| |__| |   | | |/ /__| |__| |" + dInv + " " + dInv + "   "+ dInv +  dInv +  "/");
		LOG.info("  " + dInv +" " + dInv + " " + dInv + " " + dInv + "     |_|_____|_| " +  dInv + "_|____|_|   |_|_|_____|____|_| " + dInv + "_" + dInv + "   '");
		LOG.info("   " + dInv + " " + dInv + " " + dInv + " " + dInv + "====================================================");
		LOG.info("        **                    Api Tokenizer is online! ");
		LOG.info("REST APP SYNC ::::");		
	}

}
