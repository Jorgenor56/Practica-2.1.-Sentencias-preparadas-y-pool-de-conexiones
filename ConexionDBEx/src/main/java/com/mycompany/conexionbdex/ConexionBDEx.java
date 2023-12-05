package com.mycompany.conexionbdex;

import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import java.sql.*;
import java.util.Scanner;

public class ConexionBDEx {

    //Declara un PoolDataSource estático
    private static final PoolDataSource pds;

    //configurar el pool de conexiones
    static {
        try {
            //Creo el pool de conexiones
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
        //Creo una instancia de la clase y realizo varias operaciones en base de datos
        ConexionBDEx bdex = new ConexionBDEx();

        //Verifico si un juego específico (con el nombre "UnNombreDeJuego") existe en la base de datos
        if (bdex.buscaNombre("UnNombreDeJuego")) {
            //Si el juego existe, imprimo un mensaje confirmando su existencia
            System.out.println("El juego existe en la base de datos.");
        }

        //Ejecuto una consulta SQL para seleccionar y mostrar todos los registros de la tabla videojuegos
        bdex.lanzaConsulta("SELECT * FROM videojuegos");

        //Inserto un nuevo registro en la tabla videojuegos con los detalles proporcionados
        bdex.nuevoRegistro("NuevoJuego", "Aventura", "2023-01-01", "NuevaCompañia", 59.99);

        //Solicito al usuario que introduzca los detalles de un nuevo juego y lo inserta en la base de datos
        bdex.nuevoRegistroPorTeclado();

        //Elimino un juego específico de la base de datos
        if (bdex.eliminarRegistro("UnNombreDeJuego")) {
            //Imprimo un mensaje confirmando la eliminación
            System.out.println("El juego ha sido eliminado correctamente.");
        }
    }

    public boolean buscaNombre(String nombre) {
        //Defino una consulta SQL que busca un registro específico en la tabla videojuegos
        String query = "SELECT 1 FROM videojuegos WHERE Nombre = ?";

        //Inicio un bloque try-with-resources para manejar la conexión y la declaración SQL
        try (
                //Obtengo una conexión del pool de conexiones
                 Connection conn = pds.getConnection(); //Preparo la declaración SQL
                  PreparedStatement pstmt = conn.prepareStatement(query)) {
            //Establezco el nombre a buscar
            pstmt.setString(1, nombre);

            //Ejecuto la consulta y maneja el resultado
            try ( ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); //Devuelvo true si se encuentra al menos un registro
            }
        } catch (SQLException e) {
            e.printStackTrace(); //Imprimo la traza de error en caso de excepción SQL
            return false; //Devuelvo false si se produce una excepcion
        }
    }

    public void lanzaConsulta(String consulta) {
        //Inicio un bloque try-with-resources para manejar la conexión, la declaración y el resultado SQL
        try (
                //Obtengo una conexión del pool de conexiones
                 Connection conn = pds.getConnection(); //Creo una declaración SQL
                  Statement stmt = conn.createStatement(); //Ejecuto la consulta SQL proporcionada y obtiene el resultado
                  ResultSet rs = stmt.executeQuery(consulta)) {
            //Obtengo los metadatos del conjunto de resultados
            ResultSetMetaData metaData = rs.getMetaData();
            //Determino el número de columnas en el conjunto de resultados
            int columnCount = metaData.getColumnCount();

            //Itero sobre cada fila del conjunto de resultados
            while (rs.next()) {
                //Itero sobre cada columna en la fila actual
                for (int i = 1; i <= columnCount; i++) {
                    //Imprimo el nombre de la columna y su valor
                    System.out.print(metaData.getColumnName(i) + ": " + rs.getString(i) + "\t");
                }
                System.out.println(); //Imprimo un salto de línea después de cada fila
            }
        } catch (SQLException e) {
            e.printStackTrace(); //Imprimo el error en caso de una excepción SQL
        }
    }

    public void nuevoRegistro(String nombre, String genero, String fechaLanzamiento, String compania, double precio) {
        //Defino una consulta SQL para insertar un nuevo registro en la tabla videojuegos
        String insertQuery = "INSERT INTO videojuegos (Nombre, Genero, FechaLanzamiento, Compañia, Precio) VALUES (?, ?, ?, ?, ?)";

        //Inicio un bloque try-with-resources para manejar la conexión y la declaración SQL
        try (
                //Obtengo una conexión del pool de conexiones
                 Connection conn = pds.getConnection(); //Preparo la declaración SQL con la consulta
                  PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
            //Establezco los parámetros de la consulta con los valores proporcionados
            //Asigno el nombre al primer parámetro, igual en los demás
            pstmt.setString(1, nombre);
            pstmt.setString(2, genero);
            //Convierto la fecha de lanzamiento a SQL Date
            pstmt.setDate(3, Date.valueOf(fechaLanzamiento));
            pstmt.setString(4, compania);
            pstmt.setDouble(5, precio);

            pstmt.executeUpdate(); //Ejecuto la consulta de inserción
        } catch (SQLException e) {
            e.printStackTrace(); //Imprimo la traza de error en caso de una excepción SQL
        }
    }

    public void nuevoRegistroPorTeclado() {
        //Inicio un bloque try-with-resources
        try ( Scanner scanner = new Scanner(System.in)) {
            //Pido al usuario que introduzca el nombre del videojuego y lo almacena en una variable, igual en los demás
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

            //Defino una consulta SQL para insertar el nuevo registro en la base de datos
            String insertQuery = "INSERT INTO videojuegos (Nombre, Genero, FechaLanzamiento, Compañia, Precio) VALUES (?, ?, ?, ?, ?)";

            //Inicio otro bloque try-with-resources para manejar la conexión y la declaración SQL
            try ( Connection conn = pds.getConnection();  PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                //Establezco los parámetros de la consulta con los valores introducidos por el usuario
                pstmt.setString(1, nombre);
                pstmt.setString(2, genero);
                pstmt.setDate(3, Date.valueOf(fechaLanzamiento));
                pstmt.setString(4, compania);
                pstmt.setDouble(5, precio);

                //Ejecuto la consulta de inserción
                pstmt.executeUpdate();
                System.out.println("Registro agregado exitosamente."); //Imprimo un mensaje de éxito
            }
        } catch (SQLException e) {
            System.out.println("Error al acceder a la base de datos."); //Imprimo un mensaje de error en caso de excepción SQL
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Error al leer la entrada."); //Imprimo un mensaje de error en caso de las demás excepciones
            e.printStackTrace();
        }
    }

    public boolean eliminarRegistro(String nombre) {
        //Defino una consulta SQL para eliminar un registro de la tabla videojuegos basándose en el nombre
        String deleteQuery = "DELETE FROM videojuegos WHERE Nombre = ?";

        //Inicio un bloque try-with-resources para manejar la conexión y la declaración SQL
        try (
                //Obtengo una conexión del pool de conexiones
                 Connection conn = pds.getConnection(); 
                 //Preparo la declaración SQL con la consulta
                  PreparedStatement pstmt = conn.prepareStatement(deleteQuery)
                ) {
            //Establezco el parámetro del nombre en la consulta SQL
            pstmt.setString(1, nombre); 

            //Ejecuto la consulta y consigo el número de filas afectadas
            int affectedRows = pstmt.executeUpdate();

            //Verifico si se elimino alguna fila
            if (affectedRows > 0) {
                System.out.println("El juego ha sido eliminado correctamente."); //Imprimo un mensaje de éxito
                return true; //Devuelvo true indicando que la eliminación fue exitosa
            } else {
                System.out.println("No se encontró el juego con ese nombre."); //Imprimo un mensaje si no se encontró el juego
                return false; //Devuelvo false indicando que no se eliminó ningún registro
            }
        } catch (SQLException e) {
            System.out.println("Error al eliminar el juego."); //Imprime un mensaje de error en caso de excepción SQL
            e.printStackTrace(); //Imprimo la traza de error
            return false; //Devuelvo false en caso de error
        }
    }
}
