package com.progra3_grupo2.tpo.Test;
import org.neo4j.cypherdsl.core.renderer.Configuration;
import org.neo4j.cypherdsl.core.renderer.Dialect;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;


public class PruebaConexion {

    public void run(String... args) throws Exception {
        try (var driver = GraphDatabase.driver(
                "bolt://localhost:7687",
                AuthTokens.basic("neo4j", "pepe1234")
        )) {
            driver.verifyConnectivity();
            System.out.println("Connection established."    );
        }
    }

    public static void main(String[] args) throws Exception {
        PruebaConexion pruebaConexion = new PruebaConexion();
        pruebaConexion.run();
    }
}

