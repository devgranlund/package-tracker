package application;

import java.text.SimpleDateFormat;
import java.util.Optional;
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
            // 4.
            else if (input.equals("4")){
                System.out.print("Anna paketin seurantakoodi: " );
                String trackingCode = scanner.nextLine();
                System.out.print("Anna asiakkaan nimi: ");
                String clientName = scanner.nextLine();
                createPackage(db, trackingCode, clientName);
            }
            // 5. 
            else if (input.equals("5")){
                System.out.print("Anna paketin seurantakoodi: ");
                String trackingCode = scanner.nextLine();
                System.out.print("Anna tapahtuman paikka: ");
                String locationName = scanner.nextLine();
                System.out.print("Anna tapahtuman kuvaus: ");
                String description = scanner.nextLine();
                createEvent(db, trackingCode, locationName, description);
            }
            // 12.
            else if (input.equals("12")){
                printLocations(db);
            }
            // 13.
            else if (input.equals("13")){
                printClients(db);
            }
            // 14.
            else if (input.equals("14")){
                printPackages(db);
            }
            // 15.
            else if (input.equals("15")){
                printEvents(db);
            }
        }
        
        db.close();
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
            // Paketit
            s.execute("DROP TABLE IF EXISTS Paketit");
            s.execute("CREATE TABLE Paketit (id INTEGER PRIMARY KEY, koodi STRING UNIQUE NOT NULL, " +
                    "asiakas_id INTEGER REFERENCES Asiakkaat ON DELETE CASCADE)");
            // Tapahtumat
            s.execute("DROP TABLE IF EXISTS Tapahtumat");
            s.execute("CREATE TABLE Tapahtumat (id INTEGER PRIMARY KEY, timestamp TEXT, kuvaus TEXT, " +
                    "paketti_id INTEGER REFERENCES Paketit ON DELETE CASCADE, " +
                    "paikka_id INTEGER REFERENCES Paikat ON DELETE CASCADE)");

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
        Optional<Integer> clientId = getClientIdByName(db, clientName);
        if (clientId.isPresent()){
            try {
                PreparedStatement p = db.prepareStatement("INSERT INTO Paketit(koodi, asiakas_id) VALUES (?,?)");
                p.setString(1, trackingCode);
                p.setInt(2, clientId.get());
                p.executeUpdate();

                System.out.println("Paketti lisätty.\n");
            } catch (SQLException e) {
                System.out.println("VIRHE: Pakettikoodi on jo olemassa. \n" + e.getLocalizedMessage());
            }
        } else {
            System.out.println("VIRHE: Asiakasta ei ole olemassa. \n");
        }
    }
    
    // 5. Create new event by tracking code, location name and event description
    //      tracking code must exist in DB (= package exists)
    //      location name must exist in DB (= location exists)
    public static void createEvent(Connection db, String trackingCode, String locationName, String description){
        Optional<Integer> packageId = getPackageIdByTrackingCode(db, trackingCode);
        if (packageId.isPresent() == false){
            System.out.println("VIRHE: Pakettia ei ole olemassa. \n");
            return;
        }
        Optional<Integer> locationId = getLocationIdByName(db, locationName);
        if (locationId.isPresent() == false){
            System.out.println("VIRHE: Paikkaa ei ole olemassa. \n");
            return;
        }
        try {
            PreparedStatement p = db.prepareStatement("INSERT INTO Tapahtumat(timestamp, kuvaus, paketti_id, paikka_id) VALUES (?,?,?,?)");
            p.setString(1, getCurrentTimeAsString());
            p.setString(2, description);
            p.setInt(3, packageId.get());
            p.setInt(4, locationId.get());
            p.executeUpdate();

            System.out.println("Tapahtuma lisätty.\n");
        } catch (SQLException e) {
            System.out.println("VIRHE: Tapahtuma virheellinen. \n" + e.getLocalizedMessage());
        }
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

    // 14. Print packages (for debug purposes only)
    public static void printPackages(Connection db){
        try {
            Statement s = db.createStatement();
            ResultSet r = s.executeQuery("SELECT * FROM Paketit");
            while (r.next()) {
                System.out.println(r.getInt("id")+" "+r.getString("koodi")+r.getInt("asiakas_id"));
            }
        } catch (SQLException e) {
            System.out.println("VIRHE: Paketteja ei saada tulostettua. \n" + e.getLocalizedMessage());
        }
    }

    // 15. Print events (for debug purposes only)
    public static void printEvents(Connection db){
        try {
            Statement s = db.createStatement();
            ResultSet r = s.executeQuery("SELECT * FROM Tapahtumat");
            while (r.next()) {
                System.out.println(r.getInt("id")+" "+r.getString("timestamp")
                        +" "+r.getString("kuvaus")
                        +" "+r.getInt("paikka_id")
                        +" "+r.getInt("paketti_id"));
            }
        } catch (SQLException e) {
            System.out.println("VIRHE: Tapahtumia ei saada tulostettua. \n" + e.getLocalizedMessage());
        }
    }
    
    private static Optional<Integer> getClientIdByName(Connection db, String clientName){
        try {
            PreparedStatement p = db.prepareStatement("SELECT id FROM Asiakkaat WHERE nimi=?");
            p.setString(1, clientName);
            ResultSet r = p.executeQuery();
            if (r.next()){
                return Optional.of(r.getInt("id"));
            } 
        } catch (SQLException e) {
            System.out.println("VIRHE: " + e.getLocalizedMessage());

        }
        return Optional.empty();
    }
    
    private static Optional<Integer> getLocationIdByName(Connection db, String locationName){
        try {
            PreparedStatement p = db.prepareStatement("SELECT id FROM Paikat WHERE nimi=?");
            p.setString(1, locationName);
            ResultSet r = p.executeQuery();
            if (r.next()){
                return Optional.of(r.getInt("id"));
            }
        } catch (SQLException e) {
            System.out.println("VIRHE: " + e.getLocalizedMessage());
        }
        return Optional.empty();
    }
    
    private static Optional<Integer> getPackageIdByTrackingCode(Connection db, String trackingCode){
        try {
            PreparedStatement p = db.prepareStatement("SELECT id FROM Paketit WHERE koodi=?");
            p.setString(1, trackingCode);
            ResultSet r = p.executeQuery();
            if (r.next()){
                return Optional.of(r.getInt("id"));
            }
        } catch (SQLException e) {
            System.out.println("VIRHE: " + e.getLocalizedMessage());
        }
        return Optional.empty();
    }
    
    private static String getCurrentTimeAsString(){
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        //sdf.setTimeZone(TimeZone.getTimeZone("CET"));
        return sdf.format(date);
    }

}
