package com.meditrack;

import com.meditrack.dao.*;
import com.meditrack.model.*;
import com.meditrack.model.User;
import com.meditrack.service.UserService;
import com.meditrack.service.AuditLogService;
import com.meditrack.service.*;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static UserService userService;
    private static MedicineService medicineService;
    private static SaleService saleService;
    private static SupplierService supplierService;
    private static AuditLogService auditLogService;

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("   BOOTING MEDITRACK MEDITRACK ...   ");
        System.out.println("=========================================");
        initializeSystem();

        User loggedInUser = runLoginSystem();

        if (loggedInUser != null) {
            runMainMenu(loggedInUser);
        }

        System.out.println("System shutting down. Goodbye!");
        scanner.close();
    }

    private static void initializeSystem() {
        // Initialize DAOs
        UserDAO userDAO = new UserDAOImpl();
        MedicineDAO medicineDAO = new MedicineDAOImpl();
        CategoryDAO categoryDAO = new CategoryDAOImpl();
        BatchDAO batchDAO = new BatchDAOImpl();
        SupplierDAO supplierDAO = new SupplierDAOImpl();
        MedicineSupplierDAO medicineSupplierDAO = new MedicineSupplierDAOImpl();
        SaleDAO saleDAO = new SaleDAOImpl();
        AuditLogDAO auditLogDAO = new AuditLogDAOImpl();

        // DAOs into Services
        userService = new UserService(userDAO);
        medicineService = new MedicineService(medicineDAO, batchDAO, categoryDAO, medicineSupplierDAO, auditLogService);
        saleService = new SaleService(saleDAO, medicineService, auditLogService, batchDAO);
        supplierService = new SupplierService(supplierDAO, medicineSupplierDAO, medicineService);
        auditLogService = new AuditLogService(auditLogDAO);

        System.out.println("[System] All services initialized successfully.");

        // ==========================================
        //  Create dummy account for first time login
        // ==========================================
        if (userService.getAllUsers().isEmpty()) {
            System.out.println("\n[System Alert] No users found in database. Generating default SuperAdmin...");
            Address adminAddress = new Address("System", "", "Root");
            Credentials dummyCreds = new Credentials(0, 1, "dummy", "127.0.0.1", java.time.LocalDateTime.now(), java.time.LocalDateTime.now());

            User rootAdmin = new User("System", "Admin", "admin@meditrack.com", "0714592278",
                    adminAddress, Role.SuperAdmin, dummyCreds,
                    java.time.LocalDate.of(1990, 1, 1));

            userDAO.saveUser(rootAdmin, "Admin@123");

            System.out.println("---------------------------------------------------");
            System.out.println("  DEFAULT SUPERADMIN CREATED!  ");
            System.out.println("  Email: admin@meditrack.com");
            System.out.println("  Temporary Password: Admin@123"); //Admin@1234
            System.out.println("---------------------------------------------------\n");
        }
    }

    // User Login
    private static User runLoginSystem() {
        User user = null;

        while (user == null) {
            System.out.println("\n---------- SYSTEM LOGIN ----------");
            System.out.println("admin@meditrack.com \n Admin@1234");
            System.out.print("Email: ");
            String email = scanner.nextLine();

            System.out.print("Password: ");
            String password = scanner.nextLine();

            user = userService.login(email, password);

            // If login succeeds, check if they logged first time and force to change password
            if (user != null) {
                if (userService.requiresPasswordChange(user)) {
                    System.out.println("\n[Security Alert] This is your first time logging in.");
                    System.out.println("You MUST change your password before continuing.");

                    boolean changed = false;
                    while (!changed) {
                        System.out.print("Enter NEW Password: ");
                        String newPassword = scanner.nextLine();
                        changed = userService.changePassword(user, password, newPassword);
                    }
                    System.out.println("Please log in again with your new password.\n");
                    user = null;
                }
            } else {
                System.out.println("Please try again.\n");
            }
        }
        return user;
    }

    // Main menu
    private static void runMainMenu(User user) {
        System.out.println("\n=========================================");
        System.out.println("  WELCOME TO MEDITRACK, " + user.getFirstName().toUpperCase());
        System.out.println("  Access Level: " + user.getRole());
        System.out.println("=========================================");

        boolean running = true;
        while (running) {
            System.out.println("\n--- MAIN MENU ---");
            System.out.println("1. Point of Sale (Checkout)");
            System.out.println("2. Inventory Management");
            System.out.println("3. Supplier Directory");
            System.out.println("4. Admin Dashboard (Users & Logs)");
            System.out.println("0. Logout");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.println("\n-> Launching POS Menu...");
                    posMenu(user);
                    break;
                case "2":
                    System.out.println("\n-> Launching Inventory Menu...");
                    inventoryMenu(user);
                    break;
                case "3":
                    System.out.println("\n-> Launching Supplier Menu...");
                    supplierMenu(user);
                    break;
                case "4":
                    if (user.getRole().toString().equals("Admin") || user.getRole().toString().equals("SuperAdmin")) {
                        System.out.println("\n-> Launching Admin Menu...");
                        adminMenu(user);
                    } else {
                        System.out.println("\n[Error] Access Denied. Admin privileges required.");
                    }
                    break;
                case "0":
                    System.out.println("\nLogging out...");
                    running = false;
                    break;
                default:
                    System.out.println("\n[Error] Invalid selection. Try again.");
            }
        }
    }

    // Admin menu
    private static void adminMenu(User loggedInUser) {
        boolean back = false;
        while (!back) {
            System.out.println("\n=== ADMIN DASHBOARD ===");
            System.out.println("1. View All Users");
            System.out.println("2. Register New User");
            System.out.println("3. Delete User");
            System.out.println("4. View System Audit Logs");
            System.out.println("0. Back to Main Menu");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.println("\n--- Registered Users ---");
                    for (User u : userService.getAllUsers()) {
                        System.out.println("ID: " + u.getUserId() + " | Name: " + u.getFirstName() + " " + u.getLastName() + " | Role: " + u.getRole() + " | Email: " + u.getEmail());
                    }
                    break;

                case "2":
                    System.out.println("\n--- Register New User ---");
                    System.out.print("First Name: ");
                    String fName = scanner.nextLine();
                    System.out.print("Last Name: ");
                    String lName = scanner.nextLine();
                    System.out.print("Email: ");
                    String newEmail = scanner.nextLine();
                    System.out.print("Contact Number: ");
                    String contact = scanner.nextLine();

                    System.out.println("- Address Details -");
                    System.out.print("Address Line 1: ");
                    String add1 = scanner.nextLine();
                    System.out.print("Address Line 2 (Optional): ");
                    String add2 = scanner.nextLine();
                    System.out.print("City: ");
                    String city = scanner.nextLine();
                    Address address = new Address(add1, add2.isEmpty() ? null : add2, city);

                    Role role = null;
                    while (role == null) {
                        System.out.println("- Role Selection -");
                        System.out.println("1. Pharmacist");
                        System.out.println("2. Admin");
                        System.out.println("3. SuperAdmin");
                        System.out.print("Select Role (1-3): ");
                        String roleChoice = scanner.nextLine();

                        if (roleChoice.equals("1")) role = Role.Pharmacist;
                        else if (roleChoice.equals("2")) role = Role.Admin;
                        else if (roleChoice.equals("3")) role = Role.SuperAdmin;
                        else System.out.println("[Error] Invalid selection. Please type 1, 2, or 3.");
                    }


                    java.time.LocalDate dob = null;
                    while (dob == null) {
                        System.out.print("Date of Birth (YYYY-MM-DD): ");
                        try {
                            dob = java.time.LocalDate.parse(scanner.nextLine());
                        } catch (java.time.format.DateTimeParseException e) {
                            System.out.println("[Error] Invalid date format. Please try again using exactly YYYY-MM-DD.");
                        }
                    }

                    // Temporary Password
                    String tempPassword = "";
                    while (tempPassword.trim().isEmpty()) {
                        System.out.print("Temporary Password (cannot be empty): ");
                        tempPassword = scanner.nextLine();
                    }

                    try {
                        Credentials dummy = new Credentials(0, 1, "dummy", "127.0.0.1", java.time.LocalDateTime.now(), java.time.LocalDateTime.now());
                        User newUser = new User(fName, lName, newEmail, contact, address, role, dummy, dob);
                        User saved = userService.registerUser(loggedInUser, newUser, tempPassword);

                        if (saved != null) {
                            System.out.println("\n[Success] User " + saved.getFirstName() + " successfully registered!");
                        }
                    } catch (IllegalArgumentException e) {
                        System.out.println("\n[Validation Error] " + e.getMessage());
                        System.out.println("Please try registering the user again.");
                    } catch (Exception e) {
                        System.out.println("\n[Error] Registration failed due to an unexpected system error.");
                    }
                    break;

                case "3":
                    System.out.println("\n--- Delete User ---");
                    System.out.print("Enter the User ID to delete: ");
                    try {
                        int targetId = Integer.parseInt(scanner.nextLine());
                        userService.deleteUser(loggedInUser, targetId);
                    } catch (NumberFormatException e) {
                        System.out.println("[Error] Please enter a valid number.");
                    }
                    break;

                case "4":
                    System.out.println("\n--- Recent System Logs ---");
                    for (AuditLog log : auditLogService.getSystemHistory(1)) {
                        System.out.println("[" + log.getTimestamp() + "] UserID " + log.getUserId() + " performed " + log.getAction() + " on table '" + log.getTableName() + "'");
                    }
                    break;

                case "0":
                    back = true;
                    break;

                default:
                    System.out.println("[Error] Invalid selection.");
            }
        }
    }

    // Inventory Menu
    private static void inventoryMenu(User loggedInUser) {
        boolean back = false;
        while (!back) {
            System.out.println("\n=== INVENTORY MANAGEMENT ===");
            System.out.println("1. View All Inventory");
            System.out.println("2. Add New Medicine");
            System.out.println("3. Receive Stock Batch");
            System.out.println("4. Check Low Stock Alerts");
            System.out.println("0. Back to Main Menu");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.println("\n--- Current Inventory ---");
                    List<Medicine> allMeds = medicineService.listAllInventory();
                    if (allMeds.isEmpty()) {
                        System.out.println("Inventory is currently empty.");
                    } else {
                        for (Medicine m : allMeds) {
                            System.out.println("ID: " + m.getMedicineId() + " | " + m.getName() + " (" + m.getBrand() + ") | Stock: " + m.getStock() + " | Price: LKR " + m.getPrice());
                        }
                    }
                    break;

                case "2":
                    System.out.println("\n--- Add New Medicine ---");
                    try {
                        System.out.print("Type (1 for Tablet, 2 for Liquid): ");
                        String type = scanner.nextLine();

                        System.out.print("Generic Name: ");
                        String name = scanner.nextLine();
                        System.out.print("Brand Name: ");
                        String brand = scanner.nextLine();
                        System.out.print("Category ID (1=Painkiller, 2=Antibiotic, 3=Cold): ");
                        int catId = Integer.parseInt(scanner.nextLine());
                        System.out.print("Selling Price (LKR): ");
                        java.math.BigDecimal price = new java.math.BigDecimal(scanner.nextLine());
                        Category cat = new Category(catId, "System");
                        Medicine newMed = null;

                        if (type.equals("1")) {
                            System.out.print("Dosage (e.g., 500mg): ");
                            String dosage = scanner.nextLine();
                            newMed = new Tablet(0, name, brand, cat, 0, price, dosage, java.time.LocalDateTime.now());
                        } else if (type.equals("2")) {
                            System.out.print("Volume (e.g., 100ml): ");
                            String volume = scanner.nextLine();
                            newMed = new Liquid(0, name, brand, cat, 0, price, volume, java.time.LocalDateTime.now());
                        } else {
                            System.out.println("[Error] Invalid type selected.");
                            break;
                        }

                        medicineService.addMedicine(newMed, loggedInUser.getUserId());

                    } catch (IllegalArgumentException e) {
                        System.out.println("[Validation Error] " + e.getMessage());
                    } catch (Exception e) {
                        System.out.println("[Error] Invalid input. Please check your numbers and try again.");
                    }
                    break;

                case "3":
                    System.out.println("\n--- Receive Stock Batch ---");
                    try {
                        List<Medicine> availableMeds = medicineService.listAllInventory();

                        if (availableMeds.isEmpty()) {
                            System.out.println("[Notice] Your catalog is empty. Please add a New Medicine first.");
                            break;
                        }

                        System.out.println("Available Medicines:");
                        for (Medicine m : availableMeds) {
                            System.out.println("  ID: " + m.getMedicineId() + " | " + m.getName() + " (" + m.getBrand() + ")");
                        }
                        System.out.println("---------------------------");

                        System.out.print("Enter Medicine ID from the list above: ");
                        int medId = Integer.parseInt(scanner.nextLine());

                        System.out.print("Batch Number (e.g., BATCH-001): ");
                        String batchNo = scanner.nextLine();

                        System.out.print("Quantity Received: ");
                        int qty = Integer.parseInt(scanner.nextLine());

                        System.out.print("Expiry Date (YYYY-MM-DD): ");
                        java.time.LocalDate expiry = java.time.LocalDate.parse(scanner.nextLine());

                        Batch newBatch = new Batch(medId, batchNo, qty, expiry);
                        medicineService.receiveNewBatch(newBatch, loggedInUser.getUserId());

                    } catch (IllegalArgumentException e) {
                        System.out.println("[Validation Error] " + e.getMessage());
                    } catch (Exception e) {
                        System.out.println("[Error] Invalid input format. Please check your dates and numbers.");
                    }
                    break;

                case "4":
                    System.out.println("\n--- Low Stock Alerts ---");
                    for (Medicine m : medicineService.getLowStockAlerts(50)) {
                        System.out.println("WARNING: " + m.getName() + " is running low! Only " + m.getStock() + " remaining.");
                    }
                    break;

                case "0":
                    back = true;
                    break;

                default:
                    System.out.println("[Error] Invalid selection.");
            }
        }
    }

    // Checkout Menu
    private static void posMenu(User loggedInUser) {
        System.out.println("\n=== POINT OF SALE (CHECKOUT) ===");
        List<SaleItem> cart = new java.util.ArrayList<>();
        boolean shopping = true;

        while (shopping) {
            System.out.println("\n--- Add Item to Cart ---");
            List<Medicine> available = medicineService.listAllInventory();
            System.out.println("Available Medicines (In Stock):");
            for (Medicine m : available) {
                if(m.getStock() > 0) {
                    System.out.println("  ID: " + m.getMedicineId() + " | " + m.getName() + " | Stock: " + m.getStock() + " | Price: LKR " + m.getPrice());
                }
            }
            System.out.println("---------------------------");

            System.out.print("Enter Medicine ID to add (or 0 to cancel checkout): ");
            try {
                int medId = Integer.parseInt(scanner.nextLine());

                if (medId == 0) {
                    System.out.println("Checkout cancelled.");
                    return;
                }

                Medicine med = medicineService.getMedicineById(medId);
                if (med == null) {
                    System.out.println("[Error] Invalid Medicine ID.");
                    continue;
                }

                System.out.print("Enter Quantity: ");
                int qty = Integer.parseInt(scanner.nextLine());
                if (qty <= 0) {
                    System.out.println("[Error] Quantity must be greater than 0.");
                    continue;
                }
                if (qty > med.getStock()) {
                    System.out.println("[Error] Not enough stock! Only " + med.getStock() + " available.");
                    continue;
                }

                SaleItem item = new SaleItem(medId, qty, med.getPrice());
                cart.add(item);
                System.out.println("[Added] " + qty + "x " + med.getName() + " added to cart. (Subtotal: LKR " + item.getSubtotal() + ")");
                System.out.print("\nAdd another item to this transaction? (Y/N): ");
                String choice = scanner.nextLine();
                if (choice.equalsIgnoreCase("N")) {
                    shopping = false;
                }
            } catch (Exception e) {
                System.out.println("[Error] Invalid input. Please enter numbers only.");
            }
        }

        if (!cart.isEmpty()) {
            Sale currentSale = new Sale(loggedInUser.getUserId(), cart);

            System.out.println("\n===========================");
            System.out.println("      RECEIPT SUMMARY      ");
            System.out.println("===========================");
            for (SaleItem item : currentSale.getItems()) {
                Medicine med = medicineService.getMedicineById(item.getMedicineId());
                System.out.println(med.getName() + " x" + item.getQuantity() + " : LKR " + item.getSubtotal());
            }
            System.out.println("---------------------------");
            System.out.println("TOTAL DUE: LKR " + currentSale.getTotal());
            System.out.println("===========================");

            System.out.print("\nConfirm and Process Transaction? (Y/N): ");
            if (scanner.nextLine().equalsIgnoreCase("Y")) {
                Sale processed = saleService.processTransaction(currentSale);

                if (processed != null) {
                    System.out.println("\n*** THANK YOU FOR YOUR PURCHASE ***\n");
                }
            } else {
                System.out.println("Transaction Cancelled.");
            }
        } else {
            System.out.println("Cart is empty. Returning to Main Menu.");
        }
    }

    // Supplier Menu
    private static void supplierMenu(User loggedInUser) {
        boolean back = false;
        while (!back) {
            System.out.println("\n=== SUPPLIER DIRECTORY ===");
            System.out.println("1. View All Suppliers");
            System.out.println("2. Add New Supplier");
            System.out.println("3. Delete Supplier");
            System.out.println("0. Back to Main Menu");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.println("\n--- Registered Suppliers ---");
                    List<Supplier> suppliers = supplierService.getAllSuppliers();

                    if (suppliers == null || suppliers.isEmpty()) {
                        System.out.println("No suppliers found in the directory.");
                    } else {
                        for (Supplier s : suppliers) {
                            System.out.println("ID: " + s.getSupplierId() + " | Name: " + s.getName());
                            System.out.println("   Contact: " + s.getContact() + " | Email: " + s.getEmail());
                            System.out.println("   Address: " + s.getAddress().getAddressLine1() + ", " + s.getAddress().getCity());
                            System.out.println("-------------------------------------------------");
                        }
                    }
                    break;

                case "2":
                    System.out.println("\n--- Add New Supplier ---");
                    try {
                        System.out.print("Supplier Company Name: ");
                        String name = scanner.nextLine();
                        System.out.print("Contact Number: ");
                        String contact = scanner.nextLine();
                        System.out.print("Email Address: ");
                        String email = scanner.nextLine();

                        System.out.println("- Address Details -");
                        System.out.print("Address Line 1: ");
                        String add1 = scanner.nextLine();
                        System.out.print("Address Line 2 (Optional): ");
                        String add2 = scanner.nextLine();
                        System.out.print("City: ");
                        String city = scanner.nextLine();

                        Address address = new Address(add1, add2.isEmpty() ? null : add2, city);
                        Supplier newSupplier = new Supplier(name, contact, email, address);
                        Supplier saved = supplierService.addSupplier(newSupplier);

                        if (saved != null) {
                            System.out.println("\n[Success] Supplier '" + saved.getName() + "' added successfully!");
                        } else {
                            System.out.println("\n[Error] Could not add supplier. Please check database connection.");
                        }
                    } catch (IllegalArgumentException e) {
                        System.out.println("[Validation Error] " + e.getMessage());
                    } catch (Exception e) {
                        System.out.println("[Error] Invalid input format.");
                    }
                    break;
                case "0":
                    back = true;
                    break;

                default:
                    System.out.println("[Error] Invalid selection.");
            }
        }
    }
}