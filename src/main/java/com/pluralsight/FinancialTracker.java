package com.pluralsight;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

public class FinancialTracker {

    private static ArrayList<Transaction> transactions = new ArrayList<>();
    private static final String FILE_NAME = "transactions.csv";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String TIME_FORMAT = "HH:mm:ss";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_FORMAT);

    public static void main(String[] args) {
        loadTransactions(FILE_NAME);
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("Welcome to TransactionApp");
            System.out.println("Choose an option:");
            System.out.println("D) Add Deposit");
            System.out.println("P) Make Payment (Debit)");
            System.out.println("L) Ledger");
            System.out.println("X) Exit");

            String input = scanner.nextLine().trim();

            switch (input.toUpperCase()) {
                case "D":
                    addDeposit(scanner);
                    break;
                case "P":
                    addPayment(scanner);
                    break;
                case "L":
                    ledgerMenu(scanner);
                    break;
                case "X":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option");
                    break;
            }
        }

        scanner.close();
    }

    public static void loadTransactions(String fileName) {
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
                return;
            }

            Scanner fileScanner = new Scanner(file);
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                String[] parts = line.split("\\|");

                if (parts.length == 5) {
                    String date = parts[0];
                    String time = parts[1];
                    String description = parts[2];
                    String vendor = parts[3];
                    double amount = Double.parseDouble(parts[4]);

                    Transaction transaction = new Transaction(date, time, description, vendor, amount);
                    transactions.add(transaction);
                }
            }
            fileScanner.close();
        } catch (Exception e) {
            System.out.println("Error loading transactions: " + e.getMessage());
        }
    }
    public static void saveTransaction(Transaction t, String fileName) {
        try {
            FileWriter writer = new FileWriter(fileName, true); // true = append mode
            writer.write(t.toCSV() + "\n");
            writer.close();
        } catch (Exception e) {
            System.out.println("Error saving transaction: " + e.getMessage());
        }
    }

    public static void addDeposit(Scanner scanner) {
        System.out.print("Enter description: ");
        String description = scanner.nextLine();

        System.out.print("Enter vendor: ");
        String vendor = scanner.nextLine();

        System.out.print("Enter amount: ");
        double amount = Double.parseDouble(scanner.nextLine());

        LocalDateTime now = LocalDateTime.now();
        String date = now.format(DATE_FORMATTER);
        String time = now.format(TIME_FORMATTER);

        Transaction deposit = new Transaction(date, time, description, vendor, amount);
        transactions.add(deposit);
        saveTransaction(deposit, FILE_NAME);

        System.out.println("Deposit added successfully!");
    }

    public static void addPayment(Scanner scanner) {
        System.out.print("Enter description: ");
        String description = scanner.nextLine();

        System.out.print("Enter vendor: ");
        String vendor = scanner.nextLine();

        System.out.print("Enter amount: ");
        double amount = Double.parseDouble(scanner.nextLine());

        amount = -Math.abs(amount);

        LocalDateTime now = LocalDateTime.now();
        String date = now.format(DATE_FORMATTER);
        String time = now.format(TIME_FORMATTER);

        Transaction payment = new Transaction(date, time, description, vendor, amount);
        transactions.add(payment);
        saveTransaction(payment, FILE_NAME);

        System.out.println("Payment added successfully!");
    }

    private static void ledgerMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("Ledger");
            System.out.println("Choose an option:");
            System.out.println("A) All");
            System.out.println("D) Deposits");
            System.out.println("P) Payments");
            System.out.println("R) Reports");
            System.out.println("H) Home");

            String input = scanner.nextLine().trim();

            switch (input.toUpperCase()) {
                case "A":
                    displayLedger();
                    break;
                case "D":
                    displayDeposits();
                    break;
                case "P":
                    displayPayments();
                    break;
                case "R":
                    reportsMenu(scanner);
                    break;
                case "H":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option");
                    break;
            }
        }
    }

    public static void displayLedger() {
        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
        } else {
            for (Transaction t : transactions) {
                System.out.println(t);
            }
        }
    }

    public static void displayDeposits() {
        boolean found = false;
        for (Transaction t : transactions) {
            if (t.getAmount() > 0) {
                System.out.println(t);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No deposits found.");
        }
    }

    public static void displayPayments() {
        boolean found = false;
        for (Transaction t : transactions) {
            if (t.getAmount() < 0) {
                System.out.println(t);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No payments found.");
        }
    }

    public static void reportsMenu(Scanner scanner) {
        boolean stayInReports = true;
        while (stayInReports) {
            System.out.println("\nReports Menu:");
            System.out.println("1) Month To Date");
            System.out.println("2) Previous Month");
            System.out.println("3) Year To Date");
            System.out.println("4) Previous Year");
            System.out.println("5) Search by Vendor");
            System.out.println("0) Back to Ledger Menu");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    reportMonthToDate();
                    break;
                case "2":
                    reportPreviousMonth();
                    break;
                case "3":
                    reportYearToDate();
                    break;
                case "4":
                    reportPreviousYear();
                    break;
                case "5":
                    searchByVendor(scanner);
                    break;
                case "0":
                    stayInReports = false;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    public static void reportMonthToDate() {
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);
        boolean found = false;
        for (Transaction t : transactions) {
            LocalDate date = LocalDate.parse(t.getDate());
            if (YearMonth.from(date).equals(currentMonth)) {
                System.out.println(t);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No transactions found for this month.");
        }
    }

    public static void reportPreviousMonth() {
        LocalDate today = LocalDate.now();
        YearMonth previousMonth = YearMonth.from(today).minusMonths(1);
        boolean found = false;
        for (Transaction t : transactions) {
            LocalDate date = LocalDate.parse(t.getDate());
            if (YearMonth.from(date).equals(previousMonth)) {
                System.out.println(t);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No transactions found for the previous month.");
        }
    }

    public static void reportYearToDate() {
        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        boolean found = false;
        for (Transaction t : transactions) {
            LocalDate date = LocalDate.parse(t.getDate());
            if (date.getYear() == currentYear) {
                System.out.println(t);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No transactions found for the current year.");
        }
    }

    public static void reportPreviousYear() {
        LocalDate today = LocalDate.now();
        int previousYear = today.getYear() - 1;
        boolean found = false;
        for (Transaction t : transactions) {
            LocalDate date = LocalDate.parse(t.getDate());
            if (date.getYear() == previousYear) {
                System.out.println(t);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No transactions found for the previous year.");
        }
    }

    public static void searchByVendor(Scanner scanner) {
        System.out.print("Enter vendor name to search: ");
        String vendorSearch = scanner.nextLine().toLowerCase();
        boolean found = false;
        for (Transaction t : transactions) {
            if (t.getVendor().toLowerCase().contains(vendorSearch)) {
                System.out.println(t);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No transactions found for that vendor.");
        }
    }

    public static void customSearch(Scanner scanner) {
        System.out.print("Start date (yyyy-MM-dd) or leave blank: ");
        String startInput = scanner.nextLine();
        LocalDate startDate = startInput.isEmpty() ? null : LocalDate.parse(startInput);

        System.out.print("End date (yyyy-MM-dd) or leave blank: ");
        String endInput = scanner.nextLine();
        LocalDate endDate = endInput.isEmpty() ? null : LocalDate.parse(endInput);

        System.out.print("Description keyword (optional): ");
        String descKeyword = scanner.nextLine().toLowerCase();

        System.out.print("Vendor keyword (optional): ");
        String vendorKeyword = scanner.nextLine().toLowerCase();

        System.out.print("Minimum amount (optional): ");
        String minInput = scanner.nextLine();
        Double minAmount = minInput.isEmpty() ? null : Double.parseDouble(minInput);

        System.out.print("Maximum amount (optional): ");
        String maxInput = scanner.nextLine();
        Double maxAmount = maxInput.isEmpty() ? null : Double.parseDouble(maxInput);

        boolean found = false;
        for (Transaction t : transactions) {
            LocalDate date = LocalDate.parse(t.getDate());
            boolean matches =
                    (startDate == null || !date.isBefore(startDate)) &&
                            (endDate == null || !date.isAfter(endDate)) &&
                            (descKeyword.isEmpty() || t.getDescription().toLowerCase().contains(descKeyword)) &&
                            (vendorKeyword.isEmpty() || t.getVendor().toLowerCase().contains(vendorKeyword)) &&
                            (minAmount == null || t.getAmount() >= minAmount) &&
                            (maxAmount == null || t.getAmount() <= maxAmount);

            if (matches) {
                System.out.println(t);
                found = true;
            }
        }

        if (!found) {
            System.out.println("No transactions found matching all filters.");
        }
    }
}