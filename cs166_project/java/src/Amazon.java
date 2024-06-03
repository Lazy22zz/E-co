import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;


/**
* This class defines a simple embedded SQL utility class that is designed to
* work with PostgreSQL JDBC drivers.
*
*/
public class Amazon {

 // reference to physical database connection.
 private Connection _connection = null;

 // handling the keyboard inputs through a BufferedReader
 // This variable can be global for convenience.
 static BufferedReader in = new BufferedReader(
                              new InputStreamReader(System.in));

 /**
  * Creates a new instance of Amazon store
  *
  * @param hostname the MySQL or PostgreSQL server hostname
  * @param database the name of the database
  * @param username the user name used to login to the database
  * @param password the user login password
  * @throws java.sql.SQLException when failed to make a connection.
  */


 public Amazon(String dbname, String dbport, String user, String passwd) throws SQLException {

    System.out.print("Connecting to database...");
    try{
       // constructs the connection URL
       String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
       System.out.println ("Connection URL: " + url + "\n");

       // obtain a physical connection
       this._connection = DriverManager.getConnection(url, user, passwd);
       System.out.println("Done");
    }catch (Exception e){
       System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
       System.out.println("Make sure you started postgres on this machine");
       System.exit(-1);
    }//end catch
 }//end Amazon

 // Method to calculate euclidean distance between two latitude, longitude pairs. 
 public double calculateDistance (double lat1, double long1, double lat2, double long2){
    double t1 = (lat1 - lat2) * (lat1 - lat2);
    double t2 = (long1 - long2) * (long1 - long2);
    return Math.sqrt(t1 + t2); 
 }
 /**
  * Method to execute an update SQL statement.  Update SQL instructions
  * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
  *
  * @param sql the input SQL string
  * @throws java.sql.SQLException when update failed
  */
 public void executeUpdate (String sql) throws SQLException {
    // creates a statement object
    Statement stmt = this._connection.createStatement ();

    // issues the update instruction
    stmt.executeUpdate (sql);

    // close the instruction
    stmt.close ();
 }//end executeUpdate

 /**
  * Method to execute an input query SQL instruction (i.e. SELECT).  This
  * method issues the query to the DBMS and outputs the results to
  * standard out.
  *
  * @param query the input query string
  * @return the number of rows returned
  * @throws java.sql.SQLException when failed to execute the query
  */
 public int executeQueryAndPrintResult (String query) throws SQLException {
    // creates a statement object
    Statement stmt = this._connection.createStatement ();

    // issues the query instruction
    ResultSet rs = stmt.executeQuery (query);

    /*
     ** obtains the metadata object for the returned result set.  The metadata
     ** contains row and column info.
     */
    ResultSetMetaData rsmd = rs.getMetaData ();
    int numCol = rsmd.getColumnCount ();
    int rowCount = 0;

    // iterates through the result set and output them to standard out.
    boolean outputHeader = true;
    while (rs.next()){
     if(outputHeader){
       for(int i = 1; i <= numCol; i++){
       System.out.print(rsmd.getColumnName(i) + "\t");
       }
       System.out.println();
       outputHeader = false;
     }
       for (int i=1; i<=numCol; ++i)
          System.out.print (rs.getString (i) + "\t");
       System.out.println ();
       ++rowCount;
    }//end while
    stmt.close ();
    return rowCount;
 }//end executeQuery

 /**
  * Method to execute an input query SQL instruction (i.e. SELECT).  This
  * method issues the query to the DBMS and returns the results as
  * a list of records. Each record in turn is a list of attribute values
  *
  * @param query the input query string
  * @return the query result as a list of records
  * @throws java.sql.SQLException when failed to execute the query
  */
 public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
    // creates a statement object
    Statement stmt = this._connection.createStatement ();

    // issues the query instruction
    ResultSet rs = stmt.executeQuery (query);

    /*
     ** obtains the metadata object for the returned result set.  The metadata
     ** contains row and column info.
     */
    ResultSetMetaData rsmd = rs.getMetaData ();
    int numCol = rsmd.getColumnCount ();

    // iterates through the result set and saves the data returned by the query.
    List<List<String>> result  = new ArrayList<List<String>>();
    while (rs.next()){
      List<String> record = new ArrayList<String>();
    for (int i=1; i<=numCol; ++i)
       record.add(rs.getString (i));
      result.add(record);
    }//end while
    stmt.close ();
    return result;
 }//end executeQueryAndReturnResult

 /**
  * Method to execute an input query SQL instruction (i.e. SELECT).  This
  * method issues the query to the DBMS and returns the number of results
  *
  * @param query the input query string
  * @return the number of rows returned
  * @throws java.sql.SQLException when failed to execute the query
  */
 public int executeQuery (String query) throws SQLException {
     // creates a statement object
     Statement stmt = this._connection.createStatement ();

     // issues the query instruction
     ResultSet rs = stmt.executeQuery (query);

     int rowCount = 0;

     // iterates through the result set and count nuber of results.
     while (rs.next()){
        rowCount++;
     }//end while
     stmt.close ();
     return rowCount;
 }

 /**
  * Method to fetch the last value from sequence. This
  * method issues the query to the DBMS and returns the current
  * value of sequence used for autogenerated keys
  *
  * @param sequence name of the DB sequence
  * @return current value of a sequence
  * @throws java.sql.SQLException when failed to execute the query
  */
 public int getCurrSeqVal(String sequence) throws SQLException {
 Statement stmt = this._connection.createStatement ();

 ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
 if (rs.next())
    return rs.getInt(1);
 return -1;
 }

 /**
  * Method to close the physical connection if it is open.
  */
 public void cleanup(){
    try{
       if (this._connection != null){
          this._connection.close ();
       }//end if
    }catch (SQLException e){
       // ignored.
    }//end try
 }//end cleanup

 /**
  * The main execution method
  *
  * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
  */
public static void main(String[] args) throws InterruptedException{
    if (args.length != 3) {
        System.err.println(
            "Usage: " +
            "java [-classpath <classpath>] " +
            Amazon.class.getName() +
            " <dbname> <port> <user>");
        return;
    }

    Greeting();
    Amazon esql = null;
    try {
        // use postgres JDBC driver.
        Class.forName("org.postgresql.Driver").newInstance();
        // instantiate the Amazon object and create a physical connection.
        String dbname = args[0];
        String dbport = args[1];
        String user = args[2];
        esql = new Amazon(dbname, dbport, user, "");
        
        boolean quit = false;
        while (!quit) {
            String authorisedUser = null;
            boolean loggedIn = false;

            // Check if there is a logged-in user
            String query = "SELECT name FROM LoggedInUser WHERE login = TRUE";
            List<List<String>> result = esql.executeQueryAndReturnResult(query);

            if (!result.isEmpty()) {
                authorisedUser = result.get(0).get(0);
                loggedIn = true;
            
                // ASCII art smiley face
                // ASCII art smiley face
                String smileyFace = "\n" +
        "   .-\"\"\"\"\"-.  \n" +
        "  /  .   .  \\ \n" +
        " |    __    | \n" +
        "  \\  \\__/  /  \n" +
        "   '-.____.-' \n";
                // Print the welcome message with the smiley face
                System.out.println("Welcome back, " + authorisedUser + "!" + smileyFace);

            }

            while (!loggedIn) {
                if (authorisedUser != null){
                    //user is already logged in, skip the login process
                    break;
                }
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. Create user");
                System.out.println("2. Log in");
                System.out.println("9. < EXIT");

                switch (readChoice()) {
                    case 1:
                        CreateUser(esql);
                        break;
                    case 2:
                        authorisedUser = LogIn(esql);
                        if (authorisedUser != null) {
                            loggedIn = true;
                        }
                        break;
                    case 9:
                        quit = true;
                        loggedIn = true;
                        break;
                    default:
                        System.out.println("Unrecognized choice!");
                        break;
                }
            }

            if (quit) {
                break;
            }

            boolean usermenu = true;
            while (usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. View Stores within 30 miles");
                System.out.println("2. View Product List");
                System.out.println("3. Place a Order");
                System.out.println("4. View 5 recent orders");
                System.out.println("5. Update Product");
                System.out.println("6. View 5 recent Product Updates Info");
                System.out.println("7. View 5 Popular Items");
                System.out.println("8. View 5 Popular Customers");
                System.out.println("9. Place Product Supply Request to Warehouse");
                System.out.println("10. Admin System.");
                System.out.println(".........................");
                System.out.println("20. Log out");
                System.out.println("21. Exist the system");

                switch (readChoice()) {
                    case 1:
                        viewStores(esql);
                        break;
                    case 2:
                        viewProducts(esql);
                        break;
                    case 3:
                        placeOrder(esql);
                        break;
                    case 4:
                        viewRecentOrders(esql);
                        break;
                    case 5:
                        updateProduct(esql);
                        break;
                    case 6:
                        viewRecentUpdates(esql);
                        break;
                    case 7:
                        viewPopularProducts(esql);
                        break;
                    case 8:
                        viewPopularCustomers(esql);
                        break;
                    case 9:
                        placeProductSupplyRequests(esql);
                        break;
                    case 10:
                        Admin(esql);
                        break;
                    case 20:
                        // Set the login attribute to FALSE when the user logs out
                        query = String.format("UPDATE LoggedInUser SET login = FALSE WHERE name = '%s'", authorisedUser);
                        esql.executeUpdate(query);
                        usermenu = false;
                        loggedIn = false;
                        break;
                    case 21:
                        quit = true;
                        usermenu = false;
                        loggedIn = true;
                        break;
                    default:
                        System.out.println("Unrecognized choice!");
                        break;
                }
            }
        }
    } catch (Exception e) {
        System.err.println(e.getMessage());
    } finally {
        try {
            if (esql != null) {
                System.out.print("Disconnecting from database...");
                esql.cleanup();
                System.out.println("Done\n\nBye!");
            }
        } catch (Exception e) {
            // ignored.
        }
    }
}

 public static void Greeting(){
    System.out.println(
       "\n\n*******************************************************\n" +
       "              User Interface      	               \n" +
       "*******************************************************\n");
 }//end Greeting

 /*
  * Reads the users choice given from the keyboard
  * @int
  **/
 public static int readChoice() {
    int input;
    // returns only if a correct value is given.
    do {
       System.out.print("Please make your choice: ");
       try { // read the integer, parse it and break.
          input = Integer.parseInt(in.readLine());
          break;
       }catch (Exception e) {
          System.out.println("Your input is invalid!");
          continue;
       }//end try
    }while (true);
    return input;
 }//end readChoice

 /*
  * Creates a new user
  **/
  public static void CreateUser(Amazon esql) {
    try {
        System.out.print("\tEnter name: ");
        String name = in.readLine();
        System.out.print("\tEnter password: ");
        String password = in.readLine();
        System.out.print("\tEnter latitude: ");
        String latitude = in.readLine(); // enter lat value between [0.0, 100.0]
        System.out.print("\tEnter longitude: "); // enter long value between [0.0, 100.0]
        String longitude = in.readLine();

        String type = "Customer";

        String query = String.format("INSERT INTO USERS (name, password, latitude, longitude, type) VALUES ('%s','%s', %s, %s,'%s')", name, password, latitude, longitude, type);
        esql.executeUpdate(query);

        // Insert the new user's information into the LoggedInUser table
        query = String.format("INSERT INTO LoggedInUser (userID, name, login) VALUES ((SELECT userID FROM Users WHERE name = '%s'), '%s', TRUE)", name, name);
        esql.executeUpdate(query);

        System.out.println("User successfully created!");
    } catch (Exception e) {
        System.err.println(e.getMessage());
    }
}


 /*
  * Check log in credentials for an existing user
  * @return User login or null is the user does not exist
  **/
 public static String LogIn(Amazon esql){
    try{
       System.out.print("\tEnter name: ");
       String name = in.readLine();
       System.out.print("\tEnter password: ");
       String password = in.readLine();

       String query = String.format("SELECT * FROM USERS WHERE name = '%s' AND password = '%s'", name, password);
        int userNum = esql.executeQuery(query);
        if (userNum > 0) {
            query = String.format("UPDATE LoggedInUser SET login = TRUE WHERE name = '%s'", name);
            esql.executeUpdate(query);
            return name;
        }else{
            System.out.println("\tUserName or password is wrong!\t");
            System.out.print("");
        }
        return null;
    }catch(Exception e){
       System.err.println (e.getMessage ());
       return null;
    }
 }//end

// Rest of the functions definition go in here

public static void viewStores(Amazon esql) {
  try {
      // Define the radius (in miles)
      double radius = 30.0;
      
      // ask for userLatitude, userLongitude
      System.out.print("Enter your latitude: ");
       double userLatitude = Double.parseDouble(in.readLine());
       System.out.print("Enter your longitude: ");
       double userLongitude = Double.parseDouble(in.readLine()); 

      // Construct the SQL query to retrieve all stores
      String query = "SELECT s.storeID, s.latitude, s.longitude, s.dateEstablished " +
                     "FROM Store s";

      // Execute the query
      List<List<String>> results = esql.executeQueryAndReturnResult(query);

      // Display the stores within the radius
      System.out.println("Stores within a " + radius + "-mile radius:");
      boolean found = false;
      for (List<String> row : results) {
          double storeLatitude = Double.parseDouble(row.get(1));
          double storeLongitude = Double.parseDouble(row.get(2));

          // Calculate the distance between the user and the store
          double distance = esql.calculateDistance(userLatitude, userLongitude, storeLatitude, storeLongitude);

          // Check if the store is within the radius
          if (distance <= radius) {
              System.out.println("StoreID: " + row.get(0) + ", Latitude: " + row.get(1)
                      + ", Longitude: " + row.get(2) + ", Date Established: " + row.get(3));
              found = true;
          }
      }

      if (!found) {
          System.out.println("No stores found within a " + radius + "-mile radius.");
      }
  } catch (Exception e) {
      System.out.println("Error: " + e.getMessage());
  }
}
public static void viewProducts(Amazon esql) {
  try {
      // Prompt the user to enter the storeID
      System.out.print("Enter the storeID: ");
      int storeID = Integer.parseInt(in.readLine());

      // Construct the SQL query to retrieve product information for the specified store
      String query = "SELECT p.productName, p.numberOfUnits, p.pricePerUnit " +
              "FROM Product p " +
              "WHERE p.storeID = " + storeID;

      // Execute the query
      List<List<String>> results = esql.executeQueryAndReturnResult(query);

      // Display the product information
      System.out.println("Products for Store " + storeID + ":");
      System.out.println("Product Name\tNumber of Units\tPrice per Unit");
      for (List<String> row : results) {
          String productName = row.get(0);
          int numberOfUnits = Integer.parseInt(row.get(1));
          double pricePerUnit = Double.parseDouble(row.get(2));
          System.out.printf("%-20s\t%d\t\t$%.2f\n", productName, numberOfUnits, pricePerUnit);
      }
  } catch (Exception e) {
      System.out.println("Error: " + e.getMessage());
  }
}
// user place order
public static void placeOrder(Amazon esql) {
  try {
      // Retrieve the user's latitude and longitude
      System.out.print("Enter your latitude: ");
      double userLatitude = Double.parseDouble(in.readLine());
      System.out.print("Enter your longitude: ");
      double userLongitude = Double.parseDouble(in.readLine());

      // Prompt the user to enter their name
      System.out.print("Enter your name: ");
      String userName = in.readLine();

      // Retrieve the userID based on the provided name
      String query = "SELECT userID FROM Users WHERE name = '" + userName + "'";
      List<List<String>> userResult = esql.executeQueryAndReturnResult(query);

      if (userResult.isEmpty()) {
          System.out.println("User not found.");
          return;
      }

      int userID = Integer.parseInt(userResult.get(0).get(0));

      // Prompt the user to enter the order details
      System.out.print("Enter the storeID: ");
      int storeID = Integer.parseInt(in.readLine());
      System.out.print("Enter the product name: ");
      String productName = in.readLine();
      System.out.print("Enter the number of units: ");
      int numberOfUnits = Integer.parseInt(in.readLine());

      // Check if the store is within a 30-mile radius
      query = "SELECT latitude, longitude FROM Store WHERE storeID = " + storeID;
      List<List<String>> results = esql.executeQueryAndReturnResult(query);
      if (results.isEmpty()) {
          System.out.println("Invalid storeID.");
          return;
      }
      double storeLatitude = Double.parseDouble(results.get(0).get(0));
      double storeLongitude = Double.parseDouble(results.get(0).get(1));
      double distance = esql.calculateDistance(userLatitude, userLongitude, storeLatitude, storeLongitude);
      if (distance > 30.0) {
          System.out.println("Store is not within a 30-mile radius.");
          return;
      }

      // Check if the store has sufficient stock
      query = "SELECT numberOfUnits FROM Product WHERE storeID = " + storeID + " AND productName = '" + productName + "'";
      results = esql.executeQueryAndReturnResult(query);
      if (results.isEmpty()) {
          System.out.println("Product not found in the store.");
          return;
      }
      int availableUnits = Integer.parseInt(results.get(0).get(0));
      if (numberOfUnits > availableUnits) {
          System.out.println("Insufficient stock.");
          return;
      }

      // Insert the order information into the Orders table
      query = "INSERT INTO Orders (customerID, storeID, productName, unitsOrdered, orderTime) " +
              "VALUES (" + userID + ", " + storeID + ", '" + productName + "', " + numberOfUnits + ", CURRENT_TIMESTAMP)";
      esql.executeUpdate(query);

      // Update the Product table
      query = "UPDATE Product SET numberOfUnits = numberOfUnits - " + numberOfUnits + " WHERE storeID = " + storeID + " AND productName = '" + productName + "'";
      esql.executeUpdate(query);

      System.out.println("Order placed successfully!");
  } catch (Exception e) {
      System.out.println("Error: " + e.getMessage());
  }
}
// user view their orders
public static void viewRecentOrders(Amazon esql) {
  try {
      // Prompt the user to enter their name
      System.out.print("Enter your name: ");
      String userName = in.readLine();

      // Retrieve the userID based on the provided name
      String query = "SELECT userID FROM Users WHERE name = '" + userName + "'";
      List<List<String>> userResult = esql.executeQueryAndReturnResult(query);

      if (userResult.isEmpty()) {
          System.out.println("User not found.");
          return;
      }

      int userID = Integer.parseInt(userResult.get(0).get(0));

      // Retrieve the last 5 recent orders for the user
      query = "SELECT o.orderNumber, s.storeID, o.productName, o.unitsOrdered, o.orderTime " +
              "FROM Orders o " +
              "JOIN Product p ON o.storeID = p.storeID AND o.productName = p.productName " +
              "JOIN Store s ON p.storeID = s.storeID " +
              "WHERE o.customerID = " + userID + " " +
              "ORDER BY o.orderTime DESC LIMIT 5";

      List<List<String>> orders = esql.executeQueryAndReturnResult(query);

      if (orders.isEmpty()) {
          System.out.println("You have no recent orders.");
          return;
      }

      System.out.println("Your Recent Orders:");
      System.out.println("Order Number\tStore ID\tProduct Name\tUnits Ordered\tOrder Time");

      // Display the order information
      for (List<String> order : orders) {
          String orderNumber = order.get(0);
          String storeID = order.get(1);
          String productName = order.get(2);
          String unitsOrdered = order.get(3);
          String orderTime = order.get(4);
          System.out.printf("%-15s%-15s%-20s%-15s%s%n", orderNumber, storeID, productName, unitsOrdered, orderTime);
      }
  } catch (Exception e) {
      System.out.println("Error: " + e.getMessage());
  }
}

// manager updae product
public static void updateProduct(Amazon esql) {
  try {
      // Prompt the manager to enter their ManagerID
      System.out.print("Enter your ManagerID: ");
      int managerID = Integer.parseInt(in.readLine());

      // Retrieve the storeIDs managed by the manager
      String query = "SELECT storeID FROM Store WHERE managerID = " + managerID;
      List<List<String>> results = esql.executeQueryAndReturnResult(query);

      if (results.isEmpty()) {
          System.out.println("You do not manage any stores.");
          return;
      }

      // Prompt the manager to enter the update details
      System.out.print("Enter the storeID: ");
      int storeID = Integer.parseInt(in.readLine());
      System.out.print("Enter the product name: ");
      String productName = in.readLine();
      System.out.print("Enter the new number of units: ");
      int numberOfUnits = Integer.parseInt(in.readLine());
      System.out.print("Enter the new price per unit: ");
      double pricePerUnit = Double.parseDouble(in.readLine());

      // Check if the manager manages the specified store
      boolean managesStore = false;
      for (List<String> row : results) {
          if (Integer.parseInt(row.get(0)) == storeID) {
              managesStore = true;
              break;
          }
      }

      if (!managesStore) {
          System.out.println("You do not have permission to update products in this store.");
          return;
      }

      // Check if the product exists in the specified store
      query = "SELECT * FROM Product WHERE storeID = " + storeID + " AND productName = '" + productName + "'";
      results = esql.executeQueryAndReturnResult(query);

      if (results.isEmpty()) {
          System.out.println("Product not found in the specified store.");
          return;
      }

      // Update the Product table
      query = "UPDATE Product SET numberOfUnits = " + numberOfUnits + ", pricePerUnit = " + pricePerUnit +
              " WHERE storeID = " + storeID + " AND productName = '" + productName + "'";
      esql.executeUpdate(query);

      // Insert a new record into the ProductUpdates table
      query = "INSERT INTO ProductUpdates (managerID, storeID, productName, updatedOn) " +
              "VALUES (" + managerID + ", " + storeID + ", '" + productName + "', CURRENT_TIMESTAMP)";
      esql.executeUpdate(query);

      System.out.println("Product information updated successfully!");
  } catch (Exception e) {
      System.out.println("Error: " + e.getMessage());
  }
}

// manager see recent product updated
public static void viewRecentUpdates(Amazon esql) {
  try {
      // Prompt the manager to enter their ManagerID
      System.out.print("Enter your ManagerID: ");
      int managerID = Integer.parseInt(in.readLine());

      // Retrieve the storeIDs managed by the manager
      String query = "SELECT storeID FROM Store WHERE managerID = " + managerID;
      List<List<String>> storeResults = esql.executeQueryAndReturnResult(query);

      if (storeResults.isEmpty()) {
          System.out.println("You do not manage any stores.");
          return;
      }

      // Build the storeID list
      StringBuilder storeIDList = new StringBuilder();
      for (List<String> row : storeResults) {
          storeIDList.append(row.get(0)).append(",");
      }
      storeIDList.deleteCharAt(storeIDList.length() - 1); // Remove the last comma

      // Retrieve the last 5 recent updates for the manager's stores
      query = "SELECT s.storeID, u.productName, p.numberOfUnits AS newUnits, p.pricePerUnit AS newPrice, u.updatedOn " +
              "FROM ProductUpdates u " +
              "JOIN Store s ON u.storeID = s.storeID " +
              "JOIN Product p ON u.storeID = p.storeID AND u.productName = p.productName " +
              "WHERE s.storeID IN (" + storeIDList + ") AND u.managerID = " + managerID + " " +
              "ORDER BY u.updatedOn DESC LIMIT 5";

      List<List<String>> updates = esql.executeQueryAndReturnResult(query);

      if (updates.isEmpty()) {
          System.out.println("No recent updates found for your stores.");
          return;
      }

      System.out.println("+----------+---------------+----------+----------+---------------------+");
      System.out.println("| Store ID | Product Name  | New Units| New Price| Update Time         |");
      System.out.println("+----------+---------------+----------+----------+---------------------+");

      // Display the update information
      for (List<String> update : updates) {
          String storeID = update.get(0);
          String productName = update.get(1);
          String newUnits = update.get(2);
          String newPrice = update.get(3);
          String updatedOn = update.get(4);

          System.out.printf("| %-9s| %-14s| %-9s| %-9s| %-20s|%n", storeID, productName, newUnits, newPrice, updatedOn);
      }

      System.out.println("+----------+---------------+----------+----------+---------------------+");
  } catch (Exception e) {
      System.out.println("Error: " + e.getMessage());
  }
}

// manager see their store top 5 hot 
public static void viewPopularProducts(Amazon esql) {
  try {
      // Prompt the manager to enter their ManagerID
      System.out.print("Enter your ManagerID: ");
      int managerID = Integer.parseInt(in.readLine());

      // Retrieve the storeIDs managed by the manager
      String query = "SELECT storeID FROM Store WHERE managerID = " + managerID;
      List<List<String>> storeResults = esql.executeQueryAndReturnResult(query);

      if (storeResults.isEmpty()) {
          System.out.println("You do not manage any stores.");
          return;
      }

      // Build the storeID list
      StringBuilder storeIDList = new StringBuilder();
      for (List<String> row : storeResults) {
          storeIDList.append(row.get(0)).append(",");
      }
      storeIDList.deleteCharAt(storeIDList.length() - 1); // Remove the last comma

      // Retrieve the top 5 popular products for the manager's stores
      query = "SELECT p.productName, COUNT(o.orderNumber) AS orderCount " +
              "FROM Product p " +
              "JOIN Orders o ON p.storeID = o.storeID AND p.productName = o.productName " +
              "WHERE p.storeID IN (" + storeIDList + ") " +
              "GROUP BY p.productName " +
              "ORDER BY orderCount DESC LIMIT 5";

      List<List<String>> popularProducts = esql.executeQueryAndReturnResult(query);

      if (popularProducts.isEmpty()) {
          System.out.println("No orders found for your stores.");
          return;
      }

      System.out.println("Top 5 Popular Products in Your Stores:");
      System.out.println("Product Name\tOrder Count");

      // Display the popular products
      for (List<String> product : popularProducts) {
          String productName = product.get(0);
          String orderCount = product.get(1);

          System.out.printf("%-20s\t%s%n", productName, orderCount);
      }
  } catch (Exception e) {
      System.out.println("Error: " + e.getMessage());
  }
}

// manager see which customer placed more orders
public static void viewPopularCustomers(Amazon esql) {
  try {
      // Prompt the manager to enter their ManagerID
      System.out.print("Enter your ManagerID: ");
      int managerID = Integer.parseInt(in.readLine());

      // Retrieve the storeIDs managed by the manager
      String query = "SELECT storeID FROM Store WHERE managerID = " + managerID;
      List<List<String>> storeResults = esql.executeQueryAndReturnResult(query);

      if (storeResults.isEmpty()) {
          System.out.println("You do not manage any stores.");
          return;
      }

      // Build the storeID list
      StringBuilder storeIDList = new StringBuilder();
      for (List<String> row : storeResults) {
          storeIDList.append(row.get(0)).append(",");
      }
      storeIDList.deleteCharAt(storeIDList.length() - 1); // Remove the last comma

      // Retrieve the top 5 customers who placed the most orders in the manager's stores
      query = "SELECT u.name, u.latitude, u.longitude, u.type, COUNT(o.orderNumber) AS orderCount " +
              "FROM Users u " +
              "JOIN Orders o ON u.userID = o.customerID " +
              "JOIN Product p ON o.storeID = p.storeID AND o.productName = p.productName " +
              "WHERE p.storeID IN (" + storeIDList + ") " +
              "GROUP BY u.userID " +
              "ORDER BY orderCount DESC LIMIT 5";

      List<List<String>> popularCustomers = esql.executeQueryAndReturnResult(query);

      if (popularCustomers.isEmpty()) {
          System.out.println("No orders found for your stores.");
          return;
      }

      System.out.println("Top 5 Customers Who Placed the Most Orders in Your Stores:");
      System.out.println("Name\tLatitude\tLongitude\tType\tOrder Count");

      // Display the popular customers
      for (List<String> customer : popularCustomers) {
          String name = customer.get(0);
          String latitude = customer.get(1);
          String longitude = customer.get(2);
          String type = customer.get(3);
          String orderCount = customer.get(4);

          System.out.printf("%-20s\t%-10s\t%-10s\t%-10s\t%s%n", name, latitude, longitude, type, orderCount);
      }
  } catch (Exception e) {
      System.out.println("Error: " + e.getMessage());
  }
}

// manager request order
public static void placeProductSupplyRequests(Amazon esql) {
  try {
      // Prompt the manager to enter their ManagerID
      System.out.print("Enter your ManagerID: ");
      int managerID = Integer.parseInt(in.readLine());

      // Retrieve the storeIDs managed by the manager
      String query = "SELECT storeID FROM Store WHERE managerID = " + managerID;
      List<List<String>> storeResults = esql.executeQueryAndReturnResult(query);

      if (storeResults.isEmpty()) {
          System.out.println("You do not manage any stores.");
          return;
      }

      // Prompt the manager to enter the supply request details
      System.out.print("Enter the storeID: ");
      int storeID = Integer.parseInt(in.readLine());
      System.out.print("Enter the product name: ");
      String productName = in.readLine();
      System.out.print("Enter the number of units needed: ");
      int unitsRequested = Integer.parseInt(in.readLine());
      System.out.print("Enter the warehouseID: ");
      int warehouseID = Integer.parseInt(in.readLine());

      // Check if the manager manages the specified store
      boolean managesStore = false;
      for (List<String> row : storeResults) {
          if (Integer.parseInt(row.get(0)) == storeID) {
              managesStore = true;
              break;
          }
      }

      if (!managesStore) {
          System.out.println("You do not have permission to place supply requests for this store.");
          return;
      }

      // Check if the product exists in the specified store
      query = "SELECT * FROM Product WHERE storeID = " + storeID + " AND productName = '" + productName + "'";
      List<List<String>> productResult = esql.executeQueryAndReturnResult(query);

      if (productResult.isEmpty()) {
          System.out.println("Product not found in the specified store.");
          return;
      }

      // Insert a new record into the ProductSupplyRequests table
      query = "INSERT INTO ProductSupplyRequests (managerID, warehouseID, storeID, productName, unitsRequested) " +
              "VALUES (" + managerID + ", " + warehouseID + ", " + storeID + ", '" + productName + "', " + unitsRequested + ")";
      esql.executeUpdate(query);

      // Update the Product table
      query = "UPDATE Product SET numberOfUnits = numberOfUnits + " + unitsRequested +
              " WHERE storeID = " + storeID + " AND productName = '" + productName + "'";
      esql.executeUpdate(query);

      System.out.println("Product supply request placed successfully!");
  } catch (Exception e) {
      System.out.println("Error: " + e.getMessage());
  }
}

//for admin
public static void Admin(Amazon esql){
  try {
     // Prompt the admin to enter their AdminID
     System.out.print("Enter your AdminID: ");
     String userIDInput = in.readLine();

     // Verify if the user is an admin
     String query = "SELECT * FROM Users WHERE name = '" + userIDInput + "' AND type = 'admin'";
     List<List<String>> results = esql.executeQueryAndReturnResult(query);

     if (results.isEmpty()) {
         System.out.println("You do not have admin privileges.");
         return;
     }

     boolean quit = false;
     while (!quit) {
         System.out.println("Admin Menu:");
         System.out.println("1. View all users");
         System.out.println("2. Update user information");
         System.out.println("3. View all products");
         System.out.println("4. Update product information");
         System.out.println("5. Quit");
         System.out.print("Enter your choice: ");
         int choice = Integer.parseInt(in.readLine());

         switch (choice) {
             case 1:
                 // View all users
                 query = "SELECT * FROM Users";
                 results = esql.executeQueryAndReturnResult(query);
                 System.out.println("User Information:");
                 for (List<String> row : results) {
                     System.out.println("UserID: " + row.get(0) + ", Name: " + row.get(1) +
                             ", Password: " + row.get(2) + ", Latitude: " + row.get(3) +
                             ", Longitude: " + row.get(4) + ", Type: " + row.get(5));
                 }
                 break;
             case 2:
                 // Update user information
                 System.out.print("Enter the userID to update: ");
                 int userID = Integer.parseInt(in.readLine());
                 System.out.print("Enter the new name: ");
                 String name = in.readLine();
                 System.out.print("Enter the new password: ");
                 String password = in.readLine();
                 System.out.print("Enter the new latitude: ");
                 double latitude = Double.parseDouble(in.readLine());
                 System.out.print("Enter the new longitude: ");
                 double longitude = Double.parseDouble(in.readLine());
                 System.out.print("Enter the new user type: ");
                 String type = in.readLine();
                 query = "UPDATE Users SET name = '" + name + "', password = '" + password +
                         "', latitude = " + latitude + ", longitude = " + longitude +
                         ", type = '" + type + "' WHERE userID = " + userID;
                 esql.executeUpdate(query);
                 System.out.println("User information updated successfully!");
                 break;
             case 3:
                 // View all products
                 query = "SELECT * FROM Product";
                 results = esql.executeQueryAndReturnResult(query);
                 System.out.println("Product Information:");
                 for (List<String> row : results) {
                     System.out.println("StoreID: " + row.get(0) + ", ProductName: " + row.get(1) +
                             ", NumberOfUnits: " + row.get(2) + ", PricePerUnit: " + row.get(3));
                 }
                 break;
             case 4:
                 // Update product information
                 System.out.print("Enter the storeID: ");
                 int storeID = Integer.parseInt(in.readLine());
                 System.out.print("Enter the product name: ");
                 String productName = in.readLine();
                 System.out.print("Enter the new number of units: ");
                 int numberOfUnits = Integer.parseInt(in.readLine());
                 System.out.print("Enter the new price per unit: ");
                 double pricePerUnit = Double.parseDouble(in.readLine());
                 query = "UPDATE Product SET numberOfUnits = " + numberOfUnits +
                         ", pricePerUnit = " + pricePerUnit +
                         " WHERE storeID = " + storeID + " AND productName = '" + productName + "'";
                 esql.executeUpdate(query);
                 System.out.println("Product information updated successfully!");
                 break;
             case 5:
                 quit = true;
                 break;
             default:
                 System.out.println("Invalid choice. Please try again.");
         }
         System.out.println();
     }
 } catch (Exception e) {
     System.out.println("Error: " + e.getMessage());
 }
 }

}//end Amazon
