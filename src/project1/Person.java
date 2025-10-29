package project1;


import java.io.*;
import java.util.*;


interface BloodOperations {
 void addDonor();
 void searchDonor(String bloodGroup);
 void displayAllDonors();
}


abstract class Person {
 protected String name;
 protected int age;
 protected String gender;

 Person(String name, int age, String gender) {
     this.name = name;
     this.age = age;
     this.gender = gender;
 }

 abstract void displayInfo();
}


class Donor extends Person {
 private String bloodGroup;
 private String contact;

 Donor(String name, int age, String gender, String bloodGroup, String contact) {
     super(name, age, gender);
     this.bloodGroup = bloodGroup;
     this.contact = contact;
 }

 public String getBloodGroup() {
     return bloodGroup;
 }

 @Override
 void displayInfo() {
     System.out.println("Donor: " + name + ", Age: " + age + ", Gender: " + gender +
             ", Blood Group: " + bloodGroup + ", Contact: " + contact);
 }
}


class BloodUnit {
 private String bloodGroup;
 private int quantity;

 BloodUnit(String bloodGroup, int quantity) {
     this.bloodGroup = bloodGroup;
     this.quantity = quantity;
 }

 public String getBloodGroup() {
     return bloodGroup;
 }

 public int getQuantity() {
     return quantity;
 }

 public void addQuantity(int q) {
     quantity += q;
 }

 public void reduceQuantity(int q) throws Exception {
     if (q > quantity) throw new Exception("Insufficient blood stock!");
     quantity -= q;
 }

 public void display() {
     System.out.println("Blood Group: " + bloodGroup + ", Quantity: " + quantity + " units");
 }
}


class Recipient extends Person {
 private String requiredBloodGroup;

 Recipient(String name, int age, String gender, String requiredBloodGroup) {
     super(name, age, gender);
     this.requiredBloodGroup = requiredBloodGroup;
 }

 public String getRequiredBloodGroup() {
     return requiredBloodGroup;
 }

 @Override
 void displayInfo() {
     System.out.println("Recipient: " + name + ", Required Blood: " + requiredBloodGroup);
 }
}


class BloodBank implements BloodOperations {
 private ArrayList<Donor> donors = new ArrayList<>();
 private ArrayList<BloodUnit> stock = new ArrayList<>();

 @Override
 public void addDonor() {
     Scanner sc = new Scanner(System.in);
     System.out.print("Enter Name: ");
     String name = sc.nextLine();
     System.out.print("Enter Age: ");
     int age = sc.nextInt();
     sc.nextLine();
     System.out.print("Enter Gender: ");
     String gender = sc.nextLine();
     System.out.print("Enter Blood Group: ");
     String bg = sc.nextLine();
     System.out.print("Enter Contact: ");
     String contact = sc.nextLine();

     Donor d = new Donor(name, age, gender, bg, contact);
     donors.add(d);
     addBlood(bg, 1);
     saveDonorToFile(d);
     System.out.println("✅ Donor added successfully!");
 }

 private void saveDonorToFile(Donor d) {
     try (FileWriter fw = new FileWriter("donors.txt", true)) {
         fw.write(d.toString() + "\n");
     } catch (IOException e) {
         System.out.println("Error saving donor data: " + e.getMessage());
     }
 }

 private void addBlood(String bg, int quantity) {
     for (BloodUnit b : stock) {
         if (b.getBloodGroup().equalsIgnoreCase(bg)) {
             b.addQuantity(quantity);
             return;
         }
     }
     stock.add(new BloodUnit(bg, quantity));
 }

 @Override
 public void searchDonor(String bloodGroup) {
     boolean found = false;
     for (Donor d : donors) {
         if (d.getBloodGroup().equalsIgnoreCase(bloodGroup)) {
             d.displayInfo();
             found = true;
         }
     }
     if (!found) System.out.println("❌ No donor found with blood group " + bloodGroup);
 }

 @Override
 public void displayAllDonors() {
     for (Donor d : donors)
         d.displayInfo();
 }

 public void displayStock() {
     for (BloodUnit b : stock)
         b.display();
 }

 public void processRequest(Recipient r) {
     for (BloodUnit b : stock) {
         if (b.getBloodGroup().equalsIgnoreCase(r.getRequiredBloodGroup())) {
             try {
                 b.reduceQuantity(1);
                 System.out.println("✅ Blood issued successfully to " + r.name);
                 return;
             } catch (Exception e) {
                 System.out.println(e.getMessage());
             }
         }
     }
     System.out.println("❌ Requested blood group not available.");
 }
}


class StockMonitor extends Thread {
 private BloodBank bank;

 StockMonitor(BloodBank bank) {
     this.bank = bank;
 }

 public void run() {
     try {
         while (true) {
             System.out.println("\n[Monitor] Checking blood stock...");
             bank.displayStock();
             Thread.sleep(10000); // every 10 seconds
         }
     } catch (InterruptedException e) {
         System.out.println("Stock monitor interrupted.");
     }
 }
}


 class BloodBankSystem {
 public static void main(String[] args) {
     BloodBank bank = new BloodBank();
     StockMonitor monitor = new StockMonitor(bank);
     monitor.start();

     Scanner sc = new Scanner(System.in);
     while (true) {
         System.out.println("\n=== BLOOD BANK SYSTEM ===");
         System.out.println("1. Add Donor");
         System.out.println("2. Search Donor");
         System.out.println("3. Display All Donors");
         System.out.println("4. Show Blood Stock");
         System.out.println("5. Process Blood Request");
         System.out.println("6. Exit");
         System.out.print("Choose: ");

         int choice = sc.nextInt();
         sc.nextLine();

         switch (choice) {
             case 1 -> bank.addDonor();
             case 2 -> {
                 System.out.print("Enter blood group: ");
                 String bg = sc.nextLine();
                 bank.searchDonor(bg);
             }
             case 3 -> bank.displayAllDonors();
             case 4 -> bank.displayStock();
             case 5 -> {
                 System.out.print("Enter Recipient Name: ");
                 String name = sc.nextLine();
                 System.out.print("Enter Age: ");
                 int age = sc.nextInt();
                 sc.nextLine();
                 System.out.print("Enter Gender: ");
                 String gender = sc.nextLine();
                 System.out.print("Enter Required Blood Group: ");
                 String bg = sc.nextLine();
                 Recipient r = new Recipient(name, age, gender, bg);
                 bank.processRequest(r);
             }
             case 6 -> {
                 System.out.println("Exiting System...");
                 monitor.interrupt();
                 System.exit(0);
             }
             default -> System.out.println("Invalid choice!");
         }
     }
 }
}

