import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.DatabaseMetaData;
import com.mysql.jdbc.Statement;

/*
 * Project Name: prjSQL
 * File Name:PrjSQL
 * Author: Mat Duck (Mat.Duck@gmail.com)
 * Date created: 11/14/2014	
 * Modified: 
 * 
 * 
 * 
 * 
 */

public class PrjSQL {

	// Setting up constants for use in the project.
	
	// Setting a up a variable to hold the name of the database.
	private final static String DB_NAME = "prjSQL";
	
	// For use modifying a table that already exists
	private final static String EXISTING_TABLE = "existingERPTable";
	
	// To be used to create a new table.
	private final static String NEW_TABLE = "createdERPTable";
	
	// Setting up variables to store login infomation to access our mysql server.
	private final String userName = "root";
	private final String password = "mysql";
	private final String serverName = "localhost";
	private final int portNumber = 3306;
	
	
	
/////////////////////////////////////////////////////////////////////////////////////////////	
						// Main Method //
	
	public static void main(String[] args) {
		
		// Creating an instance of the PrjSQL class.
		// We will use this instance to perform tasks to satisfy the project requirements.
		PrjSQL project = new PrjSQL();
		
		// I used this command to create the existing table.
		project.createTable(EXISTING_TABLE);
		
		// Creating an array of information to simulate user input.
		String [] userInput = {"A printing press", "01AA", "9", "4000.00", "3500.99" };
		
		// Running a SQL statement that will not return any data (except UX from the system confirming the run)
		// This is being ran on the existing database.
		project.insertData(userInput, EXISTING_TABLE);
		
		
		
		// Creating a new array for updated information.
		String [] userInputUpdate = {"A broken printing press", "01AA", "0", "4000.00", "0" };
		
		// Call the updateData method, passing in the array of data we want updated, the id of the record we want updated
		// and the name of the table where we want to perform the action. 
		project.updateData(userInputUpdate, 1, EXISTING_TABLE);
		
		// Deleting that same record.
		//project.deleteRecord(1, EXISTING_TABLE);
		
		// Creating a new table.
		project.createTable(NEW_TABLE);
		
		// Filling with some information.
		String [] userInput2 = {"Wedding Invite", "01BB", "133", "200", "150.99" };
		project.insertData(userInput2, NEW_TABLE);
		String [] userInput3 = {"Dance Invite", "02BB", "200", "350", "250.99" };
		project.insertData(userInput3, NEW_TABLE);
		String [] userInput4 = {"Grad Notice", "01CC", "1000", "900", "1000" };
		project.insertData(userInput4, NEW_TABLE);
		
		// Placing a SQL command to select everything from the NEW_TABLE and placing it into a variable.
		String sqlForDisplayTable = "SELECT * FROM " + NEW_TABLE;
		
		// Using that variable in conjunction with the displayTable method to dispaly the table. 
		project.displayTable(NEW_TABLE, sqlForDisplayTable);
		
		// By changing the SQL command we place in the string, we can display only the records with 
		// with product numbers ending in BB.
		String sqlForDisplayTableBB = "Select * FROM " + NEW_TABLE + " WHERE partNumber LIKE '%BB'";
		
		// Running the displayTable method with the new string.
		project.displayTable(NEW_TABLE, sqlForDisplayTableBB);
		
		
		
		
		// DEBUG I used this line may times while working on the project
		// This kept me from having to switch over the myphpadmin every time I wanted to scratch.
		//project.dropTable(EXISTING_TABLE);
		//project.dropTable(NEW_TABLE);
		
		
	} // end of main.


////////////////////////////////////////////////////////////////////////////////////////////


	
	
////////////////////////////////////////////////////////////////////////////////////////////
						// Support Methods // 
	
	
	// A method for creating a new table.
	public void createTable(String tableName)
	{
		// Creating some variables to store connection information, returned data, and a boolean/
		Connection conn = null;
		ResultSet rs = null;
		boolean createTable = true;
		
		try
		{
			// Establish the connection to the database. 
			conn = this.getConnection();
			
		}
		catch (SQLException e)
		{
			// If an exception was caught, inform the user and print the error messages.
			System.out.println("WARNING: Could not connect to database. Check error below.");
			e.printStackTrace();
			return;
		}
		
		try
		{
			// In order to avoid error messages, we need to make sure the table the user wants to
			// created has not been created already
			
			// Creating a metadata object to store information about the connection (which includes the database).
			java.sql.DatabaseMetaData meta = conn.getMetaData();
			// Getting the tables from the meta data and placing it in the result set variable we set up.
			rs = meta.getTables(null, null, "%", null);
			
			// A while loop to look through the meta data results
			while (rs.next())
			{
				// If we find the same table name the user wants to create,
				if(rs.getString(3).equals(tableName))
				{
					// Set the create table flag to false,
					createTable=false;
					// and tell the user that the table already exits.
					System.out.println("Did not create table. Table already exsists:" + tableName);
					break;
				}
			} // end of while loop
			
			// If the code makes it to this point, the createTable flag is still true,
			// which means the database has passed our validation.
			
			// If createTable = true,
			if(createTable)
			{
				// Creating a variable to place our hard coded (other than the table name) SQL statment.
				String createTableSQL = "CREATE TABLE " + tableName + " ( " +
						"id INTEGER NOT NULL AUTO_INCREMENT, " +
						"description varchar(150) NOT NULL, " +
						"partNumber varchar(4) NOT NULL, " +
						"quantity int NOT NULL, " +
						"originalCost DECIMAL(6,2) NOT NULL, " +
						"sellingPrice DECIMAL(6,2) NOT NULL, " +
						"PRIMARY KEY(id))";
				
				// Executing our statement
				this.executeUpdate(conn, createTableSQL);
				
				// UX to tell the user the table has been created.
				System.out.println("Created table named: " + tableName);
							
			}
				
		
		}
		catch (SQLException e)
		{
			// UX for reporting to the user that an error occurred. 
			System.out.println("ERROR: Could not create table named: " + tableName + ". See error messages.");
			e.printStackTrace();
			return;
		}
		
		// Close the connection and clear any resources involved with connecting to the database. 
		finally { releaseResource(rs,null,conn); }
	
	} // End of createTable()
	
	
	// A method to display the entries in a table.
	// This code was provided in peter's Java SQL lab.
	// I modified it to take different SQL commands from the calling object.
	public void displayTable(String tableName, String sql)
	{
		// Variables for use in the creation of our table. 
		java.sql.Statement stmt = null;
		ResultSet rs = null;
		int id = 0;
		String description= "";
		String partNumber = "";
		String quantity = "";
		String originalCost = "";
		String sellingPrice = "";
		
		// Set up a connection variable.
		Connection conn = null;
		try{
			// Connect to the database.
			conn = this.getConnection();	
		}
	
		catch (SQLException e)
		{
			// Error reporting0
			System.out.println("ERROR: Could not connect to the database");
			e.printStackTrace();
		}
		
		// Select the data
		try
		{
			// Create a sql variable containing a command that will pull all the values from the database.
			//sql = "SELECT * FROM createdERPTable";
			
			// Create a statement in our connection.
			stmt = conn.createStatement();
			
			// Take the information returned from the query and place it in a return set.
			rs = stmt.executeQuery(sql);
			
			// 'Format' the top of the table for the user.
			//System.out.println("\nID\tDescription\t\tpartNumber\t\t\tquantity - originalCost - sellingPrice");
			System.out.println("\nID\tDescription\t\tpartNumber\tquantity\toriginalCost\tsellingPrice");
			System.out.println("*********************************************************");
			
			// Loop through the return set
			while(rs.next())
			{
				// Pull the information out of the return set for each entry,
				// placing them in the variables we declared above.
				id = rs.getInt("id");
				description		= rs.getString("description");
				partNumber		= rs.getString("partNumber");
				quantity		= rs.getString("quantity");
				originalCost	= rs.getString("originalCost");
				sellingPrice	= rs.getString("sellingPrice");
				
				// Print the variables we filled above for the user.
				System.out.printf("%d\t%s\t\t%s\t\t%s \t\t%s \t\t%s. \n",
						id, description, partNumber, quantity, originalCost, sellingPrice);
			}
		}
		catch(SQLException e)
		{
			// Error reporting.
			System.out.println("ERROR: Could not SELECT data using this SQL: " + sql);
			e.printStackTrace();		
		}
		
		// Close the connection and clear any resources involved with connecting to the database. 
		finally { releaseResource(rs, stmt, conn); }
		
		
		
	} // End of Display Table
	
	
	

	
	// A method for inserting data into a database.
	public  void insertData(String[] userInput, String thisTable)
	{
		// Setting up a connection variable.
		Connection conn = null;
		
		// Setting up a string to store our SQL commands
		String sql = "";
		
		try
		{
			// Creating a connection.
			conn = this.getConnection();
		}	
		
		// Error handling if the connection fails.
		catch(SQLException e){
			System.out.println("ERROR: Could not connect to the database");
			e.printStackTrace();
		}
		
		// Insert the data
		try
		{
			// Placing SQL our SQL statement (along with the user defined array information) into the sql variable. 
			sql = "INSERT INTO " + thisTable + " (description,partNumber,quantity,originalCost,sellingPrice) VALUES("
					+ "'" + userInput[0] + "',"
					+ "'" + userInput[1] + "',"
					+ "'" + userInput[2] + "',"
					+ "'" + userInput[3] + "',"
					+ "'" + userInput[4] + "')";
			
			// Executing the statement stored in the sql variable using our connection.
			this.executeUpdate(conn, sql);
			
			// UX letting the user know the insert is successful.
			System.out.println("Insert for part number " + userInput[1] + " complete!" );
		}
		
		// Error Reporting
		catch (SQLException e)
		{
			System.out.println("ERROR: Could not insert the data using this SQL: " + sql);
			e.printStackTrace();
		}
		// Close the connection and clear any resources involved with connecting to the database. 
		finally { releaseResource(null,null,conn); }
		
	} // End of insertData()
	
	
	// A method for dropping a table.
	// While this was not required for the project, I found I could not work without it.
	// If I corrupted or damaged my table, I would just drop it. 
	public void dropTable(String tableName)
	{
		// Setting up connection and SQL variables.
		Connection conn = null;
		String sql = "";
		try
		{
			// Establishing connection
			conn = this.getConnection();
		}	catch(SQLException e){
			System.out.println("ERROR: Could not connect to the database");
			e.printStackTrace();
		}
		try
		{
			// Placing a very powerful SQL command into the sql variable.
			sql = "DROP TABLE " + tableName;
			
			// Executing the SQL through our connection.
			this.executeUpdate(conn, sql);
			
			// UX letting the user know we dropped the table. 
			System.out.println("Dropped table: " + tableName);
		}
		catch (SQLException e)
		{
			// UX for error reporting.
			System.out.println("Error could not drop the table using this sql: " + sql);
			e.printStackTrace();
			return;
		}
		
		// Close the connection and clear any resources involved with connecting to the database. 
		finally { releaseResource(null,null,conn); }
	
	} // end of DropTable
	
	
	public void updateData(String[] userInput, int thisID, String thisTable)
	{
		// Establish the connection
		// Create a sql variable.
		Connection conn = null;
		String sql = "";
		try
		{
			// COnnect to the database.
			conn = this.getConnection();
		}	catch(SQLException e){
			System.out.println("ERROR: Could not connect to the database");
			e.printStackTrace();
		}
		
		try
		{
			// Fill the sql variable with a SQL statement populated from the passed in array. 
			// Notice the single quotes. We need to surround the value that we are applying to the attribute in single quotes.
			// Double quotes around the SQL statement ("), signgle quotes around the value we are applying. 
			sql = "UPDATE " + thisTable
					+ " SET description='" + userInput[0] + "', "
					+ "partNumber='" 	+ userInput[1] + "', "
					+ "quantity='" 		+ userInput[2] + "', "
					+ "originalCost='" 	+ userInput[3] + "', "
					+ "sellingPrice='" 	+ userInput[4] + "' "
					+ "WHERE id=" 	+ thisID;
				
				// Execute the sql with our connection.
				this.executeUpdate(conn, sql);
				System.out.println(thisID + " has been updated successfully. ");
		}
		catch (SQLException e)
		{
			// Error reporting. 
			System.out.println("ERROR: Could not update the record using this sql: " + sql);
			e.printStackTrace();
		}
		// Close the connection and clear any resources involved with connecting to the database. 
		finally { releaseResource(null,null,conn); }
		
	} // end of updateData()
	
	
	public void deleteRecord(int thisID, String thisTable)
	{
		// Establish the connection
		// Create a sql variable.
		Connection conn = null;
		String sql = "";
		try
		{
			// Establish the connection
			conn = this.getConnection();
		}	
			// Error reporting.
			catch(SQLException e){
			System.out.println("ERROR: Could not connect to the database");
			e.printStackTrace();
		}
	
		
		try
		{
			// Placing another powerful SQL command in to the sql variable.
			// This will delete a record from a user defined table, that has a user defined ID. 
			sql = "DELETE FROM " + thisTable + " WHERE id = " + thisID;
			
			// Execute the command with our connection. 
			this.executeUpdate(conn, sql);
		}
		catch (SQLException e)
		{
			// Error reporting.
			System.out.println("ERROR: Could not delete the record using this SQL: " + sql);
			e.printStackTrace();
		}
		// Close the connection and clear any resources involved with connecting to the database. 
		finally { releaseResource(null,null,conn); }
		
	
	} // end of deleteRecord()
	
	
	
	
	
	
	
	
	
	
////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	
	




//////////////////////////////////////////////////////////////////
//		Utility Methods											//
// These are methods that are used by multiple other methods.	//
// In the JavaSQL lab, these were called heavy lifting. 		//
//////////////////////////////////////////////////////////////////

	/**
	 * getConnection() - A method to create a database connection.
	 * 
	 * @return Connection
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException
	{
		// Create a connection variable
		Connection conn = null;
		
		// Create properties for the connection.
		// Populate them with our credentials.
		Properties connectionProps = new Properties();
		connectionProps.put("user", this.userName);
		connectionProps.put("password",this.password);
		
		// Establish the connection
		conn = (Connection) DriverManager.getConnection("jdbc:mysql://" + this.serverName + ":" + this.portNumber + "/" + DB_NAME,connectionProps);
		
		// Return the established connection to the calling method.
		return conn;
	} // end of getConnection()

	
	/**
	 * execute Query - runs a SQL query that is passed into it from the calling method.
	 * Returns the result of the query that was run. 
	 * 
	 * 
	 * @throws SQLException 
	 * @return Resultset
	 */
	
	public ResultSet executeQuery(Connection conn, String command) throws SQLException
	{
		// Creating a result set variable, to store the information returned from our commands.
		ResultSet rs;
		// Creating a statement variable.
		java.sql.Statement stmt = null;
		
		// A try catch for error reporting
		try
		{
			// Creating a connection.
			stmt = conn.createStatement();
			// Execute the SQL command that was passed into the method. If it fails, it will throw an exception.
			rs = stmt.executeQuery(command); 
			
			// Return the results of the executed query to the calling function.
			return rs;
		}
		finally
		{
			// Regardless of errors or not, close the connection. 
			if (stmt != null) { stmt.close(); }
		}
	} // end of executeQuery

	/**
	 * executeUpdate() - runs a SQL command that does not return information.
	 * This could be a: CREATE, INSERT, UPDATE, DELETE or DROP statment. 
	 * 
	 * 
	 * @throws SQLException 
	 * @return boolean (letting the calling function know if the command was successful. 
	 * 
	 */

	public boolean executeUpdate(Connection conn,String command) throws SQLException
	{
		// Create a statement variable.
		java.sql.Statement stmt = null;
		try
		{
			// Create create a statement for the connection. 
			stmt = conn.createStatement();
			// Run the SQL command that we passed into it. 
			stmt.executeUpdate(command);
			// Return a true boolean to the calling method, letting them know it was successful
			// If it was not, we will throw a SQL exception. 
			return true;
		} // End of try
		finally
		{
			// Regardless of errors or not, close the connection. 
			if (stmt != null) { stmt.close();}
		}
	} // End of executeUpdate()

	/**
	 * releaseResource() - A method used to close connections and free up resources. 
	 * 				
	 * 
	 * @param rs - Resultet
	 * @param stmt - Statment
	 * @param conn - Connection
	 */

	public void releaseResource(ResultSet rs, java.sql.Statement stmt, Connection conn)
	{
		// If the result set is not null,
		if (rs != null)
		{
			// Try to close the resultset.
			try { rs.close(); }
			// If that fails, throw an exception. 
			catch (SQLException e) { /* Ignored */}	
		}
		// If the statment is not null,
		if (stmt != null)
		{
			// Try to close the statement
			try { stmt.close();}
			// If that fails, throw an exception.
			catch (SQLException e) { /* Ignored */}
		}
		// If the connection is not null,
		if (conn != null)
		{
			// Try to close the statement.
			try { conn.close();}
			// If that fails, throw an exception.
			catch (SQLException e) { /* Ignored */}
		}
	} // End of releaseResources()













} // end of PrjSQL Class

