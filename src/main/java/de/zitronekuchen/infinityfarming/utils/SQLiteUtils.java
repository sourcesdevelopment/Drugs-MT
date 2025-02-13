package de.zitronekuchen.infinityfarming.utils;

import de.zitronekuchen.infinityfarming.Main;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

/**
 * Utility class for managing SQLite database connections and operations.
 */
public class SQLiteUtils {
   // Path to the SQLite database file
   private static final String DATABASE_PATH;

   // Persistent database connection
   private static Connection connection = null;

   static {
      // Initialize the database path
      DATABASE_PATH = Main.getInstance().getDataFolder().getAbsolutePath() + File.separator + "data.db";
   }

   /**
    * Initializes the SQLite connection.
    * This method should be called during the plugin's onEnable phase.
    *
    * @throws SQLException           If a database access error occurs.
    * @throws ClassNotFoundException If the SQLite JDBC driver class is not found.
    */
   public static synchronized void initialize() throws SQLException, ClassNotFoundException {
      if (connection == null || connection.isClosed()) {
         // Load the SQLite JDBC driver
         Class.forName("org.sqlite.JDBC");
         // Establish the connection
         connection = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_PATH);
         System.out.println("SQLite connection established successfully.");
      }
   }

   /**
    * Retrieves the persistent SQLite connection.
    * Ensure that initialize() has been called before using this method.
    *
    * @return The active SQLite connection.
    * @throws SQLException If the connection is not initialized or is closed.
    */
   public static synchronized Connection getConnection() throws SQLException {
      if (connection == null || connection.isClosed()) {
         throw new SQLException("SQLite connection is not initialized or has been closed.");
      }
      return connection;
   }

   /**
    * Closes the persistent SQLite connection.
    * This method should be called during the plugin's onDisable phase.
    */
   public static synchronized void closeConnection() {
      if (connection != null) {
         try {
            connection.close();
            connection = null;
            System.out.println("SQLite connection closed successfully.");
         } catch (SQLException e) {
            System.err.println("Error closing SQLite connection:");
            e.printStackTrace();
         }
      }
   }

   /**
    * Executes a synchronous SQL update operation (INSERT, UPDATE, DELETE).
    *
    * @param sql    The SQL statement to execute.
    * @param params The parameters to set in the PreparedStatement.
    * @return True if the operation was successful, false otherwise.
    */
   public static synchronized boolean updateSync(String sql, Object... params) {
      try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
         setParameters(statement, params);
         statement.executeUpdate();
         return true;
      } catch (SQLException e) {
         System.err.println("Error executing updateSync:");
         e.printStackTrace();
         return false;
      }
   }

   /**
    * Executes a synchronous SQL query operation.
    *
    * @param sql    The SQL query to execute.
    * @param params The parameters to set in the PreparedStatement.
    * @return The ResultSet containing the query results, or null if an error occurs.
    */
   public static synchronized ResultSet querySync(String sql, Object... params) {
      try {
         PreparedStatement statement = getConnection().prepareStatement(sql);
         setParameters(statement, params);
         return statement.executeQuery();
      } catch (SQLException e) {
         System.err.println("Error executing querySync:");
         e.printStackTrace();
         return null;
      }
   }

   /**
    * Executes an asynchronous SQL query operation.
    * (Retained for non-critical operations)
    *
    * @param sql    The SQL query to execute.
    * @param params The parameters to set in the PreparedStatement.
    * @return A CompletableFuture containing the CachedRowSet with the query results.
    */
   public static CompletableFuture<CachedRowSet> query(String sql, Object... params) {
      return CompletableFuture.supplyAsync(() -> {
         Connection connection = null;
         PreparedStatement statement = null;
         ResultSet resultSet = null;
         CachedRowSet cachedRowSet = null;

         try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);

            for (int i = 0; i < params.length; ++i) {
               statement.setObject(i + 1, params[i]);
            }

            resultSet = statement.executeQuery();
            cachedRowSet = RowSetProvider.newFactory().createCachedRowSet();
            cachedRowSet.populate(resultSet);
         } catch (SQLException e) {
            System.err.println("Error executing asynchronous query:");
            e.printStackTrace();
         } finally {
            try {
               if (resultSet != null) {
                  resultSet.close();
               }

               if (statement != null) {
                  statement.close();
               }

               // Do not close the connection here as it's persistent
            } catch (SQLException e) {
               System.err.println("Error closing resources in asynchronous query:");
               e.printStackTrace();
            }
         }

         return cachedRowSet;
      });
   }

   /**
    * Executes an asynchronous SQL update operation (INSERT, UPDATE, DELETE).
    * (Retained for non-critical operations)
    *
    * @param sql    The SQL statement to execute.
    * @param params The parameters to set in the PreparedStatement.
    * @return A CompletableFuture containing a Boolean indicating success.
    */
   public static CompletableFuture<Boolean> update(String sql, Object... params) {
      return CompletableFuture.supplyAsync(() -> {
         Connection connection = null;
         PreparedStatement statement = null;
         boolean result = false;

         try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);

            for (int i = 0; i < params.length; ++i) {
               statement.setObject(i + 1, params[i]);
            }

            statement.executeUpdate();
            result = true;
         } catch (SQLException e) {
            System.err.println("Error executing asynchronous update:");
            e.printStackTrace();
            result = false;
         } finally {
            try {
               if (statement != null) {
                  statement.close();
               }

               // Do not close the connection here as it's persistent
            } catch (SQLException e) {
               System.err.println("Error closing resources in asynchronous update:");
               e.printStackTrace();
            }
         }

         return result;
      });
   }

   /**
    * Sets the parameters for a PreparedStatement.
    *
    * @param statement The PreparedStatement object.
    * @param params    The parameters to set.
    * @throws SQLException If setting a parameter fails.
    */
   private static void setParameters(PreparedStatement statement, Object... params) throws SQLException {
      if (params != null) {
         for (int i = 0; i < params.length; ++i) {
            statement.setObject(i + 1, params[i]);
         }
      }
   }
}
