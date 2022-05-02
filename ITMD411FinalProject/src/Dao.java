
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Date;

public class Dao {
	// instance fields
	static Connection connect = null;
	Statement statement = null;

	// constructor
	public Dao() {
	  
	}

	public Connection getConnection() {
		// Setup the connection with the DB
		try {
			connect = DriverManager
					.getConnection("jdbc:mysql://www.papademas.net:3307/tickets?autoReconnect=true&useSSL=false"
							+ "&user=fp411&password=411");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return connect;
	}

	// CRUD implementation

	public void createTables() {
		// variables for SQL Query table creations
		final String createTicketsTable = "CREATE TABLE mfortelnyy_tickets1 (ticket_id INT AUTO_INCREMENT PRIMARY KEY, ticket_issuer VARCHAR(30), ticket_description VARCHAR(200), ticket_time VARCHAR(50))";
		final String createUsersTable = "CREATE TABLE mfortelnyy_users(uid INT AUTO_INCREMENT PRIMARY KEY, uname VARCHAR(30), upass VARCHAR(30), admin int)";

		try {

			// execute queries to create tables

			statement = getConnection().createStatement();

			statement.executeUpdate(createTicketsTable);
			statement.executeUpdate(createUsersTable);
			System.out.println("Created tables in given database...");

			// end create table
			// close connection/statement object
			statement.close();
			connect.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		// add users to user table
		addUsers();
	}

	public void addUsers() {
		// add list of users from userlist.csv file to users table

		// variables for SQL Query inserts
		String sql;

		Statement statement;
		BufferedReader br;
		List<List<String>> array = new ArrayList<>(); // list to hold (rows & cols)

		// read data from file
		try {
			br = new BufferedReader(new FileReader(new File("./userlist.csv")));

			String line;
			while ((line = br.readLine()) != null) {
				array.add(Arrays.asList(line.split(",")));
			}
		} catch (Exception e) {
			System.out.println("There was a problem loading the file");
		}

		try {

			// Setup the connection with the DB

			statement = getConnection().createStatement();

			// create loop to grab each array index containing a list of values
			// and PASS (insert) that data into your User table
			for (List<String> rowData : array) {

				sql = "insert into mfortelnyy_users (uname,upass,admin) " + "values('" + rowData.get(0) + "'," + " '"
						+ rowData.get(1) + "','" + rowData.get(2) + "');";
				statement.executeUpdate(sql);
			}
			System.out.println("Inserts completed in the given database...");

			// close statement object
			statement.close();

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public int insertRecords(String ticketName, String ticketDesc) {
		int id = 0;
		String time = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());

		try {

			statement = getConnection().createStatement();
			statement.executeUpdate("Insert into mfortelnyy_tickets1" + "(ticket_issuer, ticket_description, ticket_time) values(" + " '"
					+ ticketName + "','" + ticketDesc + "','" +time+ "')", Statement.RETURN_GENERATED_KEYS);

			// retrieve ticket id number newly auto generated upon record insertion
			ResultSet resultSet = null;
			resultSet = statement.getGeneratedKeys();
			if (resultSet.next()) {
				// retrieve first field in table
				id = resultSet.getInt(1);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		return id;

	}

	//admin will see al the tickets
	public ResultSet readRecordsAdmin() {

		ResultSet results = null;
		try {
			String query = "SELECT * FROM mfortelnyy_tickets1;";
			PreparedStatement statement = connect.prepareStatement(query);
			results = statement.executeQuery();
			//connect.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return results;
	}
	
	//user will see tickets added under the username
	public ResultSet readRecordsUser(String un) {

		ResultSet results = null;
		try {
			String query = "SELECT * FROM mfortelnyy_tickets1 WHERE ticket_issuer = ?";
			PreparedStatement stmt = connect.prepareStatement(query);
			stmt.setString(1, un);
			results = stmt.executeQuery();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return results;
	}
	
	//updateRecords implementation
	public int updateRecords(String id, String tname, String tdesc) {
		try {		
			 //perform update w/ prepared statement 
			 
			String query = "UPDATE mfortelnyy_tickets1 SET ticket_issuer=?, ticket_description=? WHERE ticket_id=?" ;
			PreparedStatement statement = connect.prepareStatement(query);
			//Where clause -> only one row will be updated specified by the id(key)
			statement.setInt(3, Integer.parseInt(id));
			//2nd value to be updated
			statement.setString(2, tdesc);
			//1st value to be updated
			statement.setString(1, tname);
			//execute the statement and store the return value to integer to check for success
			statement.executeUpdate();
			statement.close();
			return 1;			
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			return 0;	
		}
	}
	

	//deleteRecords
	public int deleteRecords(String id) {
		
		try {
			//perform deletion w/ prepared statement 
			String query = "DELETE FROM mfortelnyy_tickets1 WHERE ticket_id=?" ;
			PreparedStatement statement = connect.prepareStatement(query);
			//Where clause -> only one row will be deleted specified by the id(key)
			statement.setString(1, id);	
		    statement.executeUpdate();			
		    statement.close();
		    return 1;
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			return 0;
		}
	}
}
