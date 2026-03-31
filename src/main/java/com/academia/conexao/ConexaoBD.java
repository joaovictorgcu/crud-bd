package com.academia.conexao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoBD {

    private static final String URL = "jdbc:postgresql://localhost:5432/academia_db";
    private static final String USUARIO = "postgres";
    private static final String SENHA = "redoxon";

    public static Connection getConexao() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver PostgreSQL não encontrado no classpath", e);
        }
        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }

    public static void fechar(AutoCloseable... recursos) {
        for (AutoCloseable recurso : recursos) {
            if (recurso != null) {
                try {
                    recurso.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
