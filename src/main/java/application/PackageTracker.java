package application;

import java.util.Scanner;
import java.sql.*;

public class PackageTracker {

    public static void main(final String[] args) throws SQLException {
        System.out.println("\n");
        runUi();
        System.out.println("\nMooi!");
        System.exit(0);
    }
    
    public static void runUi() throws SQLException {
        System.out.println("" +
                "******************\n" +
                "Ohjelman toiminnot:\n" +
                "" +
                "1. Tyhjennä ja luo tietokanta.\n" +
                "2. Lisää uusi paikka.\n" +
                "3. Lisää uusi asiakas.\n" +
                "4. Lisää uusi paketti.\n" +
                "5. Lisää uusi tapahtuma tietokantaan.\n" +
                "6. Hae kaikki paketin tapahtumat.\n" +
                "7. Hae kaikki asiakkaan paketit.\n" +
                "8. Hae paikan tapahtumien määrä.\n" + 
                "9. Suorita tietokannan tehokkuustesti.\n" +
                "10. Lopeta.\n" +
                "12. -> Debug.\n" +
                "******************");
        
        Scanner scanner = new Scanner(System.in);
        String input = "";
        Connection db = DriverManager.getConnection("jdbc:sqlite:package-tracker.db");

        while (input.equals("10") == false){
            System.out.print("Valitse toiminto (1-9): ");
            input = scanner.nextLine();
            
            // 1.
            if (input.equals("1")){
                createDb(db);
            } 
            // 2. 
            else if (input.equals("2")){
                System.out.print("Anna paikan nimi: ");
                String locationName = scanner.nextLine();
                createLocation(db, locationName);
            }
            // 3.
            else if (input.equals("3")){
                System.out.print("Anna asiakkaan nimi: ");
                String clientName = scanner.nextLine();
                createClient(db, clientName);
            }
            // 12.
            else if (input.equals("12")){
                printLocations(db);
            }
            // 13.
            else if (input.equals("13")){
                printClients(db);
            }
        }
        
    }
    
    // 1. Crete Database
    public static void createDb(Connection db){
        try {
            Statement s = db.createStatement();
            // Paikat
            s.execute("DROP TABLE IF EXISTS Paikat");
            s.execute("CREATE TABLE Paikat (id INTEGER PRIMARY KEY, nimi TEXT NOT NULL UNIQUE)");
            // Asiakkaat
            s.execute("DROP TABLE IF EXISTS Asiakkaat");
            s.execute("CREATE TABLE Asiakkaat (id INTEGER PRIMARY KEY, nimi TEXT NOT NULL UNIQUE)");

            // TODO create rest of the tables
            
            System.out.println("Tietokanta luotu.\n");
            
        } catch (SQLException e) {
            System.out.println("VIRHE: Tietokannan luominen ei onnistu! \n" + e.getLocalizedMessage());
        }
    }
    
    // 2. Create new location by location name
    //      locationName is unique
    public static void createLocation(Connection db, String locationName){
        PreparedStatement p = null;
        try {
            p = db.prepareStatement("INSERT INTO Paikat(nimi) VALUES (?)");
            p.setString(1, locationName);
            p.executeUpdate();

            System.out.println("Paikka luotu.\n");
            
        } catch (SQLException e) {
            System.out.println("VIRHE: Paikka on jo olemassa. \n" + e.getLocalizedMessage());
        }
    }
    
    // 3. Create new client by client name
    //      clientName is unique
    public static void createClient(Connection db, String clientName){
        PreparedStatement p = null;
        try {
            p = db.prepareStatement("INSERT INTO Asiakkaat(nimi) VALUES (?)");
            p.setString(1, clientName);
            p.executeUpdate();

            System.out.println("Asiakas luotu.\n");

        } catch (SQLException e) {
            System.out.println("VIRHE: Asiakas on jo olemassa. \n" + e.getLocalizedMessage());
        }
    }
    
    // 4. Create new package by tracking code and client name
    //      tracking code is unique
    //      client must exist in DB
    public static void createPackage(Connection db, String trackingCode, String clientName){
        // TODO implementation, handle db constraint violations
    }
    
    // 5. Create new event by tracking code, location name and event description
    //      tracking code must exist in DB (= package exists)
    //      location name must exist in DB (= location exists)
    public static void createEvent(Connection db, String trackingCode, String locationName, String description){
        // TODO implementation, handle db constraint violations
    }
    
    // 6. Print events by tracking code
    public static void printEventsByTrackingCode(Connection db, String trackingCode){
        // TODO implementation
    }
    
    // 7. Print packages by client name
    public static void printPackagesByClientName(Connection db, String clientName){
        // TODO implementation
    }
    
    // 8. Print event number by location and date
    //      TODO: check date format!
    public static void printEventNumberByLocationNameAndDate(Connection db, String locationName, String date){
        // TODO implementation
    }
    
    // 9. Efficiency test
    public static void doEfficiencyTest(Connection db){
        // TODO implementation
    }
    
    // 12. Print locations (for debug purposes only)
    public static void printLocations(Connection db){
        try {
            Statement s = db.createStatement();
            ResultSet r = s.executeQuery("SELECT * FROM Paikat");
            while (r.next()) {
                System.out.println(r.getInt("id")+" "+r.getString("nimi"));
            }
        } catch (SQLException e) {
            System.out.println("VIRHE: Paikkoja ei saada tulostettua. \n" + e.getLocalizedMessage());
        }
    }
    
    // 13. Print clients (for debug purposes only)
    public static void printClients(Connection db){
        try {
            Statement s = db.createStatement();
            ResultSet r = s.executeQuery("SELECT * FROM Asiakkaat");
            while (r.next()) {
                System.out.println(r.getInt("id")+" "+r.getString("nimi"));
            }
        } catch (SQLException e) {
            System.out.println("VIRHE: Asiakkaita ei saada tulostettua. \n" + e.getLocalizedMessage());
        }
    }

}
