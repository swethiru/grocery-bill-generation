import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class bill {

    public static void main(String[] args) {

        try {
            // Register MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Establish database connection
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/grocery", "root", "Swedha@2004");
            Statement stmt = conn.createStatement();

            // Fetch available items from database
            String selectSql = "SELECT ID, Items, Amount, stock FROM items";
            ResultSet rs = stmt.executeQuery(selectSql);

            System.out.println("------------------------------------------------------------");
            System.out.printf("\t\t\tAvailable Items:\n");
            System.out.println("------------------------------------------------------------");
            while (rs.next()) {
                int ID = rs.getInt("ID");
                String itemName = rs.getString("Items");
                int amount = rs.getInt("Amount");
                int stock = rs.getInt("stock");
                System.out.println(ID + "|" + itemName + " | " + amount + "|" + stock);
            }

            rs.close(); // Close ResultSet after use

            // Scanner for user input
            Scanner scanner = new Scanner(System.in);
            int choice = 0;
            double totalAmount = 0.0;

             // Lists to store the selected items, quantities, and their total prices
            List<String> selectedItems = new ArrayList<>();
            List<Integer> selectedQuantities = new ArrayList<>();
            List<Double> itemTotalPrices = new ArrayList<>();

            while (choice != 3) {
                System.out.println("\nSelect an option:");
                System.out.println("1. Enter items");
                System.out.println("2. Calculate total amount");
                System.out.println("3. Exit");

                try {
                    choice = scanner.nextInt();
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input. Please enter a valid option.");
                    scanner.nextLine(); // Consume invalid input
                    continue;
                }

                switch (choice) {
                    case 1:
                        int numItems;
                        while (true) {
                            System.out.println("Enter the number of items to select: ");
                            try {
                                numItems = scanner.nextInt();
                                break;
                            } catch (InputMismatchException e) {
                                System.out.println("Invalid input. Please enter a valid number of items.");
                                scanner.nextLine(); // Consume invalid input
                            }
                        }
                        totalAmount = 0.0;

                        // Loop through items to select
                        for (int i = 0; i < numItems; i++) {
                            System.out.println("Enter item name " + (i + 1) + ": ");
                            scanner.nextLine(); // Consume newline
                            String itemName = scanner.nextLine();
                            int quantity;

                            // Loop until valid quantity is entered
                            while (true) {
                                System.out.println("Enter the quantity for item " + itemName + ": ");
                                try {
                                    quantity = scanner.nextInt();
                                    break;
                                } catch (InputMismatchException e) {
                                    System.out.println("Invalid input. Please enter a valid quantity.");
                                    scanner.nextLine(); // Consume invalid input
                                }
                            }

                            // Query item details
                            String selectItemSql = "SELECT * FROM items WHERE Items = '" + itemName + "'";
                            ResultSet itemResultSet = stmt.executeQuery(selectItemSql);
                            if (itemResultSet.next()) {
                                double price = itemResultSet.getDouble("Amount");
                                int currentStock = itemResultSet.getInt("stock");

                                if (quantity <= currentStock) {
                                    double itemTotalAmount = price * quantity;
                                    totalAmount += itemTotalAmount;

                                    // Store selected item details for bill generation
                                    selectedItems.add(itemName);
                                    selectedQuantities.add(quantity);
                                    itemTotalPrices.add(itemTotalAmount);
                            
                                    System.out.println("Selected item price: " + price);
                                    System.out.println("Quantity: " + quantity);
                                    System.out.println("Total amount for item " + itemName + ": " + itemTotalAmount);

                                    // Update stock and insert into bill
                                    int updatedStock = currentStock - quantity;
                                    String updateStockSql = "UPDATE items SET stock = " + updatedStock + " WHERE Items = '" + itemName + "'";
                                    stmt.executeUpdate(updateStockSql);

                                    String insertBillSql = "INSERT INTO bill (Items, quantity, Amount) VALUES ('" + itemName + "', " + quantity + ", " + itemTotalAmount + ")";
                                    stmt.executeUpdate(insertBillSql);
                                } else {
                                    System.out.println("Insufficient stock for item " + itemName);
                                }
                            } else {
                                System.out.println("Invalid item name.");
                            }
                            itemResultSet.close(); // Close ResultSet for item
                        }

                        // Insert purchase details into grocery_details
                        String selectMaxCustIdSql = "SELECT MAX(cust_id) AS max_cust_id FROM grocery_details";
                        ResultSet maxCustIdRs = stmt.executeQuery(selectMaxCustIdSql);
                        int customerID = 1;
                        if (maxCustIdRs.next()) {
                            customerID = maxCustIdRs.getInt("max_cust_id") + 1;
                        }
                        String insertDetailsSql = "INSERT INTO grocery_details (cust_id, purchased_amt) VALUES (" + customerID + ", " + totalAmount + ")";
                        stmt.executeUpdate(insertDetailsSql);

                        maxCustIdRs.close(); // Close ResultSet for max customer ID
                        
                        // Generate and display the bill
                        System.out.println("\n------------------------------------------------------------");
                        System.out.printf("\t\t\tGrocery Bill\n");
                        System.out.println("------------------------------------------------------------");
                        System.out.println("Item\tQuantity\tPrice\tTotal");
                        for (int i = 0; i < selectedItems.size(); i++) {
                            System.out.printf("%s\t%d\t%.2f\t%.2f\n", selectedItems.get(i), selectedQuantities.get(i),
                                    itemTotalPrices.get(i) / selectedQuantities.get(i), itemTotalPrices.get(i));
                        }
                        System.out.println("------------------------------------------------------------");
                        System.out.println("Total amount: " + totalAmount); 
                        break;

                    case 2:
                        System.out.println("Total amount: " + totalAmount);
                        break;

                    case 3:
                        System.out.println("Thank you!");
                        break;

                    default:
                        System.out.println("Invalid option. Please select a valid option.");
                        break;
                }
            }

            // Close connections after all operations
            stmt.close();
            conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
