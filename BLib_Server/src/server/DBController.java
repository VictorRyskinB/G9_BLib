package server;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executors;

import common.BorrowRecord;
import common.DestRecord;


public class DBController {
	private static final DBController instance = new DBController();
	private Connection con;
	//intFields is the list of all fields that have int values in the database
	//private static Set<String> intFields = new HashSet<>(Arrays.asList());
	private static Set<String> intFields;
	private static Set<String> dateFields;
	private static Set<String> boolFields;
	public static DBController getInstance () {
		return instance;
	}
	
	private DBController() {
		intFields=new HashSet<>(Arrays.asList());
		dateFields=new HashSet<>(Arrays.asList());
		boolFields=new HashSet<>(Arrays.asList());
		connectToDB();
		initializeFields();
	}
	public Connection getConnection() {
	    return con;
	}

	private boolean connectToDB() {
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.out.print("the class Driver is not found");
			return false ;
		}
		
		try {
			con = DriverManager.getConnection("jdbc:mysql://localhost/blib?serverTimezone=Asia/Jerusalem","root","eden1234");
		} catch (SQLException e) {
			System.out.print("cannot connect to the DB : " + e.getMessage());
			return false;
		}
		
		return true;
	}
//
	
	
	private void initializeFields() {

	    // Table: users
	    intFields.add("user_id");


	    // Table: librarian
	    intFields.add("librarian_id");

	    // Table: subscriber
	    intFields.add("subscriber_id");


	    // Table: user_status_registry
	    dateFields.add("status_set_date");
	    dateFields.add("status_end_date");
	    boolFields.add("status_is_current");

	    // Table: book

	    boolFields.add("book_available");
	    dateFields.add("return_date");

	    // Table: borrow
	    intFields.add("borrow_number");
	    intFields.add("lending_librarian");
	    dateFields.add("borrow_date");
	    dateFields.add("actual_returned_date");

	    // Table: extensions
	    dateFields.add("day_of_extension");
	    dateFields.add("new_return_date");

	    // Table: order_book
	    dateFields.add("nearest_book_return");
	    intFields.add("subscriber_id");
	    
	    
	    // Table : librarian_messages
	    boolFields.add("is_new");
	    
	    

	  
	}
	
	
	
	public ResultSet retrieveRowsWithConditions(String tablename, String[] fields, String[] values) {

	    StringBuilder query = new StringBuilder("SELECT * FROM ");
	    query.append(tablename);
	    
	    int fieldLen=0,valuesLen=0;
	    
	    if (fields!=null)
	    {
	    	query.append(" WHERE ");
	    	fieldLen=fields.length;
	    }
	    if (values!=null)
	    	valuesLen=values.length;
	    
	    for (int i = 0; i < fieldLen; i++) {
	        query.append(fields[i]);
	        if (values[i]==null)
	        	query.append(" IS ?");
	        else
	        	query.append(" LIKE ?");
	        
	        if (i < fieldLen - 1) {
	            query.append(" AND ");
	        }
	    }
	    
	    try {
	        PreparedStatement stmt = con.prepareStatement(query.toString());
	        for (int i = 0; i < valuesLen; i++) {
	            //stmt.setString(i + 1, "%" + values[i] + "%");
	            setStmt(stmt,i+1,fields[i],"%" + values[i] + "%");
	        }
	        return stmt.executeQuery();
	    } catch (SQLException e) {
	        e.printStackTrace();
	        System.out.println("SQLException in retrieveRowsWithConditions" + e.getMessage());

	        return null;
	    }
	    
	}
	
	//consideration for changes:
	//create a string array for keyfields and keys and concat a statement that can have as many ANDS as i want
	//for example, i want a user, i can have an array of 1 with sub_id and keynum
	//and if i want a book that has "harry" in the name and "fantasy" in the topic,
	//the array can be of length 2 with fields [name,topic] and [harry,fantasy] as fields.
	//a for loop would then create a statement that looks like this:
	//"SELECT * FROM " + table + "WHERE keyfield[0] = ? AND keyfield[1] = ?"
	

	private void setStmt(PreparedStatement stmt, int index,String field ,String value) throws SQLException
	{

		
		if (intFields.contains(field))
		{
			int intValue=-1;
			try {
				intValue = Integer.parseInt(value);
			}catch(NumberFormatException e)
			{
				//do nothing.
			}
			
			stmt.setInt(index,intValue);
		}	

	    // Handling date fields
		else if (dateFields.contains(field)) {
		    if (value == null) {
		        stmt.setNull(index, java.sql.Types.DATE);
		    } else if (value.contains("null")) {
		        stmt.setNull(index, java.sql.Types.DATE);
		    } else {
		        try {
		            java.sql.Date sqlDate;
	                String cleanValue = value.replaceAll("%", "").trim();
		            if (cleanValue.matches("\\d{4}-\\d{2}-\\d{2}")) {

		                // Handle yyyy-MM-dd format
		                sqlDate = java.sql.Date.valueOf(cleanValue);

		            } else {
		                // Handle Date.toString() format (e.g., "Wed Dec 31 00:00:00 PST 1969")
		                SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
		                java.util.Date parsed = format.parse(cleanValue);
		                sqlDate = new java.sql.Date(parsed.getTime());
		            }
		            stmt.setDate(index, sqlDate);
		        } catch (IllegalArgumentException | ParseException e) {

		            throw new SQLException("Invalid date format for field: " + field + " with value: " + value);
		        }
		    }
		}
		else if (boolFields.contains(field))
			stmt.setBoolean(index, value.contains("true"));//if value = "true" will set true.
		else
			stmt.setString(index,value);	
	}
	
	public ResultSet retrieveRow(String table, String field, String val)
	{
		
		try
		{
			//stmt = con.createStatement();
			PreparedStatement stmt;
			if (val==null)
				 stmt = con.prepareStatement("SELECT * FROM " +table +" WHERE " + field + " IS ?");
			else
				 stmt = con.prepareStatement("SELECT * FROM " +table +" WHERE " + field + " = ?");
			
			
			setStmt(stmt,1,field,val);
			
			ResultSet rs = stmt.executeQuery();

			return rs;

		}
		catch(SQLException e) {
			System.out.println("SQL Exception error : " + e.getMessage());
			e.printStackTrace();
			return null;
		}
			
	}
		
	public void editRow(String tableName, String keyName, String keyVal, String col, String data) {
		
		
		PreparedStatement stmt = null;
		int rows=0;
		
		//integer value handling

		
		try {
			stmt = con.prepareStatement("UPDATE " + tableName + " SET " + col +" = ? WHERE " + keyName + " =  ?;");
			//handling strings and integers differently
	
			//stmt.setString(1, data);
			//stmt.setString(2, keyVal);
			setStmt(stmt,1,col,data);
			setStmt(stmt,2,keyName,keyVal);
			
			rows = stmt.executeUpdate();
			stmt.close();
		}
		catch(SQLException e) {
			System.out.println("SQL Exception at savetoDB error: " + e.getMessage());
		}
		
	}
	
	public void insertRow(String table, String[] fields, String[] vals) {
		int rows;
		PreparedStatement stmt = null;
		StringBuilder query = new StringBuilder ("INSERT INTO ");
		query.append(table);
		//query.append(" ");
		if (fields!=null)
		{
			query.append(" (");
			for (int i = 0; i < fields.length-1; i++) {
				query.append(fields[i]);
				query.append(", ");
			}
			query.append(fields[fields.length-1]);
			query.append(")");
			
		}
		
		
		query.append(" VALUES (");
	    for (int i = 0; i < vals.length-1; i++) {
	        query.append("?, ");
	    }
	    	query.append("?)");
	    	

	    
	    	try {
				stmt = con.prepareStatement(query.toString());
				for (int i = 0; i < vals.length; i++) {
					setStmt(stmt,i+1,fields[i],vals[i]);
				}
				rows=stmt.executeUpdate();
			} catch (SQLException e) {
				System.out.println("DBController.insertRows failed");
				e.printStackTrace();
			}
	}


	
	//function counts the amount of rows that satisfy the conditions of the fields=values. 
	//fields can be null to retrieve the table size
	public int countRows(String tableName, String[] fields, String[] values) {
        String newID;
        boolean keyExists;
        int rows=-1;
        
        StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM ");
	    query.append(tableName);
        
        if (fields!=null)
        {
        	
        	query.append(" WHERE ");
        	
        	for (int i = 0; i < fields.length; i++) {
    	        query.append(fields[i]);
    	        query.append(" LIKE ?");
    	        if (i < fields.length - 1) {
    	            query.append(" AND ");
    	        }
    	    }
        	
        }
        
        PreparedStatement stmt;
		try {
			stmt = con.prepareStatement(query.toString());
		    // Set the value for the placeholder (e.g., "%active%" for LIKE query)
		    stmt.setString(1, "%" + values[0] + "%");  // Assuming you're looking for the first value in the array (values[0])
			ResultSet rs = stmt.executeQuery();
	        rs.next(); // Move to the first and only result
	        rows = rs.getInt(1); 
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       

        return rows;
    
	}
	
	
	public int tableCount(String tableName)
	{
		
        int rows=-1;
        
        String query = "SELECT COUNT(*) FROM " + tableName;
        
       
        PreparedStatement stmt;
		try {
			stmt = con.prepareStatement(query.toString());
			ResultSet rs = stmt.executeQuery();
	        rs.next(); // Move to the first and only result
	        rows = rs.getInt(1); 
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       

        return rows;
	}
	
	
	// method creates a list of borrow records fetched from table
	public List<BorrowRecord> getBorrowRecords() {
	    List<BorrowRecord> records = new ArrayList<>();
	    String query = "SELECT borrow_date, return_date, actual_returned_date FROM borrow";

	    try (PreparedStatement stmt = con.prepareStatement(query); 
	         ResultSet rs = stmt.executeQuery()) {

	        while (rs.next()) {
	            // Handling borrow_date
	            LocalDate borrowDate = rs.getDate("borrow_date") != null
	                ? rs.getDate("borrow_date").toLocalDate()
	                : null;
	                
	            // Handling return_date
	            LocalDate returnDate = rs.getDate("return_date") != null
	                ? rs.getDate("return_date").toLocalDate()
	                : null;
	                
	            // Handling actual_return_date (can be NULL)
	            LocalDate actualReturnDate = rs.getDate("actual_returned_date") != null
	                ? rs.getDate("actual_returned_date").toLocalDate()
	                : null;

	            // Add the borrow record to the list
	            records.add(new BorrowRecord(borrowDate, returnDate, actualReturnDate));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();  // Log the exception or handle it appropriately
	        // Optionally, rethrow or return an empty list to ensure the application continues
	    }

	    return records;
	}
	
	// Method creates a list of destroyed book records fetched from the table
	public List<DestRecord> getDestRecords() {
	    List<DestRecord> records = new ArrayList<>();
	    String query = "SELECT book_barcode, borrower_id FROM destroyed_books";

	    try (PreparedStatement stmt = con.prepareStatement(query);
	         ResultSet rs = stmt.executeQuery()) {

	        while (rs.next()) {
	            // Fetch the book barcode and borrower ID from the result set
	            String bookBarcode = rs.getString("book_barcode");
	            String borrowerId = rs.getString("borrower_id");

	            // Add the destroyed book record to the list
	            records.add(new DestRecord(bookBarcode, borrowerId));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();  // Log the exception or handle it appropriately
	        // Optionally, rethrow or return an empty list to ensure the application continues
	    }

	    return records;
	}
	
	
	
	public void deleteRow(String table, String primaryKeyColumn, String primaryKeyValue) {
	    int rows;
	    PreparedStatement stmt = null;
	    StringBuilder query = new StringBuilder("DELETE FROM ");
	    query.append(table);
	    query.append(" WHERE ");
	    query.append(primaryKeyColumn);
	    query.append(" = ?");

	    try {
	        stmt = con.prepareStatement(query.toString());
	        stmt.setString(1, primaryKeyValue); // Assuming the primary key value is a string, modify this if it's a different data type
	        rows = stmt.executeUpdate();
	        System.out.println("Rows deleted: " + rows);
	    } catch (SQLException e) {
	        System.out.println("DBController.deleteRow failed");
	        e.printStackTrace();
	    }
	}




	public void closeConnection() {
		try {
	            //con.close();
	        
		} catch (Exception e) {
			
			//System.out.print("cannot close connection with the DB ");
		}
	}
	
	
	

}
