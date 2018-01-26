package db;

import java.sql.*;
import java.io.File;
import java.util.Scanner;
import java.util.regex.*;
import java.util.Vector;

import java.io.FileNotFoundException;

/**
 * Main driver for the database.
 */
public class Db {
    /* PROPERTIES */
    /** The name of the database. Do not include ".mv.db" extension.
     */
    Connection conn;
    private String name;
    private File file;
    private boolean isOpen;

    /* CONSTRUCTORS */
    public Db (String name_init) {
        name = name_init;
        file = new File(name + "mv.db");
        conn = null;
        open();
    }

    /* GETTERS */
    public boolean isOpen () {
        return isOpen;
    }

    /* INITIALIZATION METHODS */
    /** @brief Create a new database.
     *
     * @return Success or failure.
     */
    protected boolean create () {
        try {
        for (String sql : readSqlFromFile("db/create_tables.sql"))
            conn.prepareStatement(sql).execute();
        }
        catch (SQLException e) {
            System.err.println(e);
            return false;
        }
        return true;
    }

    /** @brief Open the database connection.
     *
     * Called automatically upon object creation.
     */
    public void open () {
        boolean needsPopulation = !(file.exists() && !file.isDirectory());

        try {
            Class.forName("org.h2.Driver");
        }
        catch (ClassNotFoundException e) {
            System.out.println(e);
            return;
        }

        try {
            conn = DriverManager.getConnection("jdbc:h2:" + name);
        }
        catch (SQLException e) {
            System.out.println(e);
            return;
        }

        if (needsPopulation) // if the database needs tables
            if (!create()) // populate it, and on failure…
                return;

        isOpen = true;
    }

    /** @brief Close the database connection.
     *
     * This is called when the object is sent to garbage collection (in @ref
     * finalize), but may be useful for managing connections.
     */
    public void close () throws SQLException {
        conn.close();
        isOpen = false;
    }

    /* DIRECT DATABASE INTERACTION METHODS */
    ResultSet executeQuery (String sql) throws SQLException {
        return conn.prepareStatement(sql).executeQuery();
    }

    int executeUpdate (String sql) throws SQLException {
        return conn.prepareStatement(sql).executeUpdate();
    }

    /* QUERY METHODS */

    /* HELPER FUNCTIONS */
    public static Vector<String> readSqlFromFile (String sqlFileName) {
        Scanner sqlScanner;
        Pattern p = Pattern.compile(".*\\S.*", Pattern.DOTALL);
            // pattern to match strings with at least one non-whitespace
            // character
        Vector<String> sqlStatements = new Vector<String>();
        String inStr, sqlStr;

        try {
            sqlScanner = new Scanner(new File(sqlFileName));
            sqlScanner.useDelimiter(";");

            while (sqlScanner.hasNext(p))
                sqlStatements.add(sqlScanner.next(p) + ";");

            sqlScanner.close();
        }
        catch (FileNotFoundException e) {
            System.out.println(e);
        }

        return sqlStatements;
    }
}