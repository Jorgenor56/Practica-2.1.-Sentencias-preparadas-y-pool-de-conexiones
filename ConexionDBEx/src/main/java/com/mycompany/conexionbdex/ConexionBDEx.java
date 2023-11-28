package com.mycompany.conexionbdex;

import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

import java.sql.*;
import java.util.Scanner;

public class ConexionBDEx {
    private static final PoolDataSource pds;

    static {
        try {
            pds = PoolDataSourceFactory.getPoolDataSource();
            pds.setConnectionFactoryClassName("com.mysql.cj.jdbc.MysqlDataSource");
            pds.setURL("jdbc:mysql://localhost:3306/jcvd");
            pds.setUser("dam2");
            pds.setPassword("1234");
            pds.setInitialPoolSize(5);
            pds.setMinPoolSize(5);
            pds.setMaxPoolSize(20);
        } catch (SQLException e) {
            throw new RuntimeException("Error al configurar el pool de conexiones", e);
        }
    }

    public static void main(String[] args) {
        ConexionBDEx bdex = new ConexionBDEx();

        if (bdex.buscaNombre("UnNombreDeJuego")) {
            System.out.println("El juego existe en la base de datos.");
        }

        bdex.lanzaConsulta("SELECT * FROM videojuegos");

        bdex.nuevoRegistro("NuevoJuego", "Aventura", "2023-01-01",
        "NuevaCompañia", 59.99);

        bdex.nuevoRegistroPorTeclado();

        if (bdex.eliminarRegistro("UnNombreDeJuego")) {
            System.out.println("El juego ha sido eliminado correctamente.");
        }
    }

    public boolean buscaNombre(String nombre) {
        String query = "SELECT 1 FROM videojuegos WHERE Nombre = ?";
        try (
            Connection conn = pds.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query)
        ) {
            pstmt.setString(1, nombre);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void lanzaConsulta(String consulta) {
        try (
            Connection conn = pds.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(consulta)
        ) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(metaData.getColumnName(i) + ": " + rs.getString(i) + "\t");
                }
                System.out.println();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void nuevoRegistro(String nombre, String genero, String fechaLanzamiento, String compania, double precio) {
        String insertQuery = "INSERT INTO videojuegos (Nombre, Genero, FechaLanzamiento, Compañia, Precio) VALUES (?, ?, ?, ?, ?)";
        try (
            Connection conn = pds.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(insertQuery)
        ) {
            pstmt.setString(1, nombre);
            pstmt.setString(2, genero);
            pstmt.setDate(3, Date.valueOf(fechaLanzamiento));
            pstmt.setString(4, compania);
            pstmt.setDouble(5, precio);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void nuevoRegistroPorTeclado() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Introduce el nombre del videojuego:");
            String nombre = scanner.nextLine();
            System.out.println("Introduce el género del videojuego:");
            String genero = scanner.nextLine();
            System.out.println("Introduce la fecha de lanzamiento del videojuego (YYYY-MM-DD):");
            String fechaLanzamiento = scanner.nextLine();
            System.out.println("Introduce la compañía del videojuego:");
            String compania = scanner.nextLine();
            System.out.println("Introduce el precio del videojuego:");
            double precio = Double.parseDouble(scanner.nextLine());

            String insertQuery = "INSERT INTO videojuegos (Nombre, Genero, FechaLanzamiento, Compañia, Precio) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = pds.getConnection(); PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                pstmt.setString(1, nombre);
                pstmt.setString(2, genero);
                pstmt.setDate(3, Date.valueOf(fechaLanzamiento));
                pstmt.setString(4, compania);
                pstmt.setDouble(5, precio);
                pstmt.executeUpdate();
                System.out.println("Registro agregado exitosamente.");
            }
        } catch (SQLException e) {
            System.out.println("Error al acceder a la base de datos.");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Error al leer la entrada.");
            e.printStackTrace();
        }
    }

    public boolean eliminarRegistro(String nombre) {
        String deleteQuery = "DELETE FROM videojuegos WHERE Nombre = ?";
        try (Connection conn = pds.getConnection(); PreparedStatement pstmt = conn.prepareStatement(deleteQuery)) {
            pstmt.setString(1, nombre);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("El juego ha sido eliminado correctamente.");
                return true;
            } else {
                System.out.println("No se encontró el juego con ese nombre.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error al eliminar el juego.");
            e.printStackTrace();
            return false;
        }
    }
}
