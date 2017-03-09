package odbg.org.libs;

import java.sql.*;
import java.util.Properties;

/**
 * 
 * @author Orlando Bohorquez
 * @version Version 2.7
 *	
 *	<p>
 *		Clase JDBC es util cuando se necesiten hacer conexiones
 *		con la base de datos, pudiendo ser estas PostgreSQL o MySQL
 *		que son las que por el momento estan programadas para funcionar
 *	</p>
 */
public class JDBC {
    
    private Properties dataSource = null;
    private String driver = null;

    private Connection conn = null;

    /**
     * <p>
     * 		Host por default de los manejadores de bases de datos
     * </p>
     */
    public static final String HOST = "localhost";

    /**
     * <p>
     * 		Jdbc por default del manejador de base de datos postgres
     * </p>
     */
    public static final String JDBC_POSTGRESQL = "postgresql";
    
    /**
     * <p>
     * 		Usuario por default del manejador de base de datos postgres
     * </p>
     */
    public static final String USER_POSTGRESQL = "postgres";
    
    /**
     * <p>
     * 		Puerto por default del manejador de base de datos postgres
     * </p>
     */
    public static final int PORT_POSTGRESQL = 5433;

    /**
     * <p>
     * 		Jdbc por default del manejador de base de datos mysql
     * </p>
     */
    public static final String JDBC_MYSQL = "mysql";
    
    /**
     * <p>
     * 		Usuario por default del manejador de base de datos mysql
     * </p>
     */
    public static final String USER_MYSQL = "root";
    
    /**
     * <p>
     * 		Puerto por default del manejador de base de datos mysql
     * </p>
     */
    public static final int PORT_MYSQL = 3306;

    /**
     * <p>
     * 		Constructor sin parametros en caso de que se quiera instanciar un
     * 		objeto JDBC y generar la conexion a la base de datos mas tarde
     * </p>
     */
    public JDBC() {}
    
    /**
     * <p>
     * 		Constructor con parametros por valor, si el manejador a usar aun tiene
     * 		los valores por default lo aconsejable es usar las variables estaticas
     * 		Ejemplo:
     * 			JDBC pg = new JDBC(	JDBC.JDBC_POSTGRESQL, 
     *				JDBC.HOST, 
     *				JDBC.USER_POSTGRESQL, 
     *				"****", 
     *				JDBC.PORT_POSTGRESQL
     * 			);
     * </p>
     * 
     * @param jdbc Jdbc de la base de datos
     * @param host Host de la base de datos
     * @param user Usuario de la base de datos
     * @param pass Contraseña de la base de datos
     * @param db Nombre de la base de datos
     * @param port Puerto de la base de datos
     */
    public JDBC(String jdbc, String host, String user, String pass, String db, int port) {
    	Properties dataSource = new Properties();
    	
    	dataSource.setProperty("jdbc", jdbc);
    	dataSource.setProperty("host", host);
    	dataSource.setProperty("user", user);
    	dataSource.setProperty("pass", pass);
    	dataSource.setProperty("db", db);
    	dataSource.setProperty("port", String.valueOf(port));
    	
    	this.setDataSource(dataSource);
    }
    
    /**
     * <p>
     * 		Constructor con properties file 
     * 		Ejemplo:
     * 			InputFileStream file = new InputFileStream( url );
     * 			Properties prop = new Properties();			
     * 			prop.load(file);
     * 
     * 			JDBC pg = new JDBC(	prop );
     * </p>
     * 
     * @param prop Archivo con las variables para la conexion a la base de datos
     */
    public JDBC(Properties dataSource) {
    	this.setDataSource(dataSource);
    }
    
    public void setDataSource(Properties dataSource) {
        if( !dataSource.getProperty("jdbc").isEmpty() 	&&
        		!dataSource.getProperty("host").isEmpty() && 
        		!dataSource.getProperty("user").isEmpty() && 
        		!dataSource.getProperty("pass").isEmpty() && 
        		!dataSource.getProperty("db").isEmpty() 	&& 
        		!dataSource.getProperty("port").isEmpty()
        ) {
        	this.dataSource = dataSource;
        	this.createConnection();
        }
        else {
        	throw new Error("Properties file is not well configured");
        }
    }
    
    private void createConnection() {
        try {
        	switch (this.dataSource.getProperty("jdbc").toLowerCase()) {
	            case "postgresql":
	                this.driver = "org.postgresql.Driver";
	                break;
	            case "mysql":
	            	this.driver = "com.mysql.jdbc.Driver";
	                break;
	            default:
	                throw new Error("Can't load jdbc driver");
	        }
        	Class.forName(this.driver);
            String connString = this.getConnString();
            this.conn = DriverManager.getConnection(connString, 
            		this.dataSource.getProperty("user"), 
            		this.dataSource.getProperty("pass"));
            
        } catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean getConnectionStatus() {
        return this.conn != null;
    }

    public void execute( String query, Object... values ) {
		if( this.getConnectionStatus() ) {
    		try {
	            PreparedStatement pstmt = this.setValues(query, values);
	            pstmt.execute();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
    	}
    	else {
    		throw new Error("There isn't connection with database");
    	}
	}

    public Object[][] executeQuery( String query, Object... values ) {
    	if( this.getConnectionStatus() ) {
    		ResultSet rs = null;
	        try {
	            PreparedStatement pstmt = this.setValues(query, values);
	            rs = pstmt.executeQuery();
	        } catch (SQLException e) {	
	            e.printStackTrace();
	        }
	        return this.RSToTable(rs);
    	}
        else {
        	throw new Error("There isn't connection with database");
    	}
    }
    
    private String getConnString() {
    	StringBuilder connString = new StringBuilder();
    	connString
    		.append("jdbc:")
    		.append(this.dataSource.getProperty("jdbc"))
    		.append("://")
    		.append(this.dataSource.getProperty("host"))
    		.append(":")
    		.append(this.dataSource.getProperty("port"))
    		.append("/")
    		.append(this.dataSource.getProperty("db"));
    	
    	return connString.toString();
    }

    private PreparedStatement setValues( String query, Object... values ) {
        PreparedStatement pstmt = null;
        try {
            pstmt = this.conn.prepareStatement(
                    query,
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY
            );
            for( int i = 0; i < values.length; i++ ) {
                pstmt.setObject(i+1, values[i]);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pstmt;
    }

    private Object[][] RSToTable( ResultSet rs ) {
        Object table[][] = null;
        try {
            int numRows = 0;
            if (rs.last()) {
                numRows = rs.getRow();
                rs.beforeFirst();
            }

            ResultSetMetaData rsmd = rs.getMetaData();
            int numCols = rsmd.getColumnCount();

            table = new Object[numRows + 1][numCols];

            String[] labels = new String[numCols];

            for(int i = 0; i < numCols; i++ ) {
                labels[i] = rsmd.getColumnLabel(i + 1);
            }

            table[0] = labels;

            while( rs.next() ) {
                int rowNum = rs.getRow();
                for(int i = 0; i < numCols; i++ )
                    table[rowNum][i] = rs.getObject(i+1);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return table;
    }
    
    public boolean compareDataSource(Properties dataSource) {
    	return this.dataSource == dataSource;
    }
    
    public boolean close() {
    	boolean isClosed = false;
    	try {
			this.conn.close();
			
			isClosed = this.conn.isClosed();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	return isClosed;
    	
    }

}