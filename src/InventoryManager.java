// InventoryManager.java
// Manages multiple character inventories, saved as separate files.
// MODIFIED: Added item descriptions for specific categories.

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.io.*; // Required for file operations
import java.time.LocalDateTime; // For timestamp in save file
import java.util.Arrays; // For sorting files
import java.util.Comparator; // For sorting files
import java.util.regex.Pattern; // For filename validation

public class InventoryManager {

    // --- Directory where all character inventory files are stored ---
    private static final String INVENTORY_DIR_PATH = "C:\\Users\\Public\\DnD_Information"; // [cite: 56]
    // Alternatively:
    // private static final String INVENTORY_DIR_PATH = "C:/Users/Public/DnD_Information"; // [cite: 57]

    // *** MODIFIED: Data Structure ***
    // Holds the *current* character's inventory: Category -> ItemName -> ItemDetails
    private static Map<String, Map<String, ItemDetails>> inventory = new HashMap<>(); // [cite: 58] // MODIFIED

    // --- Holds the full path to the currently loaded character's file ---
    private static String currentActiveInventoryFile = null; // [cite: 59]
    private static String currentCharacterName = null; // Store the name for display // [cite: 59]

    private static Scanner scanner = new Scanner(System.in); // [cite: 60] // Shared scanner

    // Define Categories (can be expanded - these apply to all characters)
    private static final String CAT_WEAPONS = "Weapons"; // [cite: 61]
    private static final String CAT_IMPORTANT = "Important Items"; // [cite: 61]
    private static final String CAT_GENERAL = "General Items"; // [cite: 62]
    private static final String CAT_MONEY = "Money"; // [cite: 62]
    private static final List<String> categories = new ArrayList<>(List.of(CAT_WEAPONS, CAT_IMPORTANT, CAT_GENERAL, CAT_MONEY)); // [cite: 63]

    // *** NEW: Categories that support descriptions ***
    private static final List<String> descriptionCategories = List.of(CAT_WEAPONS, CAT_IMPORTANT, CAT_GENERAL);

    // *** NEW: Inner class to hold item details ***
    private static class ItemDetails {
        int quantity;
        String description;

        ItemDetails(int quantity, String description) {
            this.quantity = quantity;
            this.description = description != null ? description : ""; // Ensure description is never null
        }
    }

    // --- Main Program Flow ---
    public static void main(String[] args) {

        // --- Phase 1: Character Selection/Creation ---
        if (!selectOrCreateCharacter()) {
            System.err.println("Failed to select or create a character inventory. Exiting."); // [cite: 64]
            scanner.close();
            return; // Exit if character selection fails
        }
        // Now currentActiveInventoryFile and currentCharacterName are set

        // --- Phase 2: Inventory Management Loop ---
        String menuChoice; // [cite: 65]
        boolean quit = false; // [cite: 65]

        do {
            displayMenu(); // [cite: 66] // Menu now shows character name
            menuChoice = SafeInput.getRegExString(scanner, "Enter your choice", "^[AaDdPpSsQq]$").toUpperCase(); // [cite: 67]
            switch (menuChoice) {
                case "A":
                    addItem(); // [cite: 68]
                    break;
                case "D":
                    deleteItem(); // [cite: 69]
                    break;
                case "P":
                    printInventory(); // [cite: 70]
                    break;
                case "S":
                    saveInventory(); // [cite: 71] // Saves the *current* character's inventory
                    break; // [cite: 72]
                case "Q":
                    boolean confirmSave = SafeInput.getYNConfirm(scanner, "Save " + currentCharacterName + "'s inventory before quitting?"); // [cite: 73]
                    if (confirmSave) {
                        saveInventory(); // [cite: 74]
                    }
                    quit = true; // [cite: 75]
                    System.out.println("Exiting Inventory Manager for " + currentCharacterName + "."); // [cite: 75]
                    break; // [cite: 76]
                default:
                    System.out.println("Invalid choice. Please try again."); // [cite: 77]
                    break;
            }
            System.out.println(); // [cite: 78] // Add a newline for better readability after actions

        } while (!quit);

        scanner.close(); // [cite: 79] // Close the scanner when done
    }

    // --- Character Selection Logic (Unchanged logic, only file path validation) ---
    private static boolean selectOrCreateCharacter() {
        File inventoryDir = new File(INVENTORY_DIR_PATH); // [cite: 80]
        // 1. Ensure inventory directory exists (Logic from)
        if (!inventoryDir.exists()) {
            System.out.println("Inventory directory not found. Attempting to create: " + INVENTORY_DIR_PATH); // [cite: 81]
            if (inventoryDir.mkdirs()) {
                System.out.println("Directory created successfully."); // [cite: 82]
            } else {
                System.err.println("Error: Failed to create inventory directory."); // [cite: 83]
                System.err.println("Please ensure the path is correct and you have permissions."); // [cite: 83]
                return false; // [cite: 84] // Cannot proceed
            }
        } else if (!inventoryDir.isDirectory()) {
            System.err.println("Error: The specified path exists but is not a directory: " + INVENTORY_DIR_PATH); // [cite: 85]
            return false; // Cannot proceed
        } else if (!inventoryDir.canRead() || !inventoryDir.canWrite()) {
            System.err.println("Error: Insufficient permissions (read/write) for directory: " + INVENTORY_DIR_PATH); // [cite: 86]
            return false; // Cannot proceed
        }


        // 2. Find existing character files (.txt extension) (Logic from)
        File[] inventoryFiles = inventoryDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt")); // [cite: 87]
        List<String> characterNames = new ArrayList<>();
        if (inventoryFiles != null) {
            Arrays.sort(inventoryFiles, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER)); // [cite: 88]
            for (File f : inventoryFiles) {
                String fileName = f.getName(); // [cite: 89]
                characterNames.add(fileName.substring(0, fileName.length() - 4)); // [cite: 89]
            }
        }

        // 3. Display Selection Menu (Logic from)
        SafeInput.prettyHeader("Select Character Inventory"); // [cite: 90]
        System.out.println("0. Create New Character Inventory");
        if (characterNames.isEmpty()) {
            System.out.println("(No existing character inventories found)"); // [cite: 91]
        } else {
            for (int i = 0; i < characterNames.size(); i++) {
                System.out.println((i + 1) + ". " + characterNames.get(i)); // [cite: 92]
            }
        }
        System.out.println("------------------------------------"); // [cite: 93]

        // 4. Get User Choice (Logic from [cite: 94])
        int choice = SafeInput.getRangedInt(scanner, "Enter selection", 0, characterNames.size()); // [cite: 94]

        // 5. Process Choice (Logic from)
        if (choice == 0) {
            // Create New Character
            boolean nameValid = false; // [cite: 95]
            String newName = "";
            while (!nameValid) {
                newName = SafeInput.getNonZeroLenString(scanner, "Enter new character name"); // [cite: 96]
                // Basic filename validation
                if (newName.matches(".*[<>:\"/\\\\|?*].*")) { // [cite: 96]
                    System.out.println("Error: Character name contains invalid characters ( <>:\"/\\|?* ). Please try again."); // [cite: 96]
                } else {
                    boolean nameExists = false; // [cite: 97]
                    for(String existingName : characterNames){ // [cite: 97]
                        if(existingName.equalsIgnoreCase(newName)){ // [cite: 97]
                            nameExists = true; // [cite: 98]
                            break; // [cite: 98]
                        }
                    }
                    if(nameExists){ // [cite: 98]
                        System.out.println("Error: A character with this name already exists. Please choose a different name."); // [cite: 99]
                    } else {
                        nameValid = true; // [cite: 99]
                    }
                }
            }

            currentCharacterName = newName; // [cite: 100] // Use the exact case provided
            currentActiveInventoryFile = INVENTORY_DIR_PATH + File.separator + currentCharacterName + ".txt"; // [cite: 101]
            System.out.println("Creating new inventory for " + currentCharacterName + "."); // [cite: 101]
            initializeEmptyInventory(); // [cite: 102] // Start with a fresh map
            // Optionally perform an initial save: saveInventory(); [cite: 103]
            return true; // [cite: 103]

        } else {
            // Load Existing Character
            currentCharacterName = characterNames.get(choice - 1); // [cite: 104]
            currentActiveInventoryFile = INVENTORY_DIR_PATH + File.separator + currentCharacterName + ".txt"; // [cite: 104]
            System.out.println("Loading inventory for " + currentCharacterName + "..."); // [cite: 104]
            loadInventory(); // [cite: 105] // Load data from the selected file
            return true; // [cite: 106]
        }
    }

    // Helper to set up a fresh inventory map
    private static void initializeEmptyInventory() {
        inventory.clear(); // [cite: 107]
        for (String cat : categories) {
            inventory.put(cat, new HashMap<>()); // [cite: 108] // MODIFIED: Value is Map<String, ItemDetails>
        }
    }

    // --- Category Management ---
    private static String selectCategory() {
        System.out.println("\nSelect a Category:"); // [cite: 109]
        for (int i = 0; i < categories.size(); i++) {
            System.out.println((i + 1) + ". " + categories.get(i)); // [cite: 110]
        }
        int choice = SafeInput.getRangedInt(scanner, "Enter category number", 1, categories.size()); // [cite: 111]
        return categories.get(choice - 1);
    }

    // --- Menu and Display (Updated Header) ---
    private static void displayMenu() {
        SafeInput.prettyHeader("Inventory Manager: " + currentCharacterName); // [cite: 112]
        System.out.println("A - Add/Update an item"); // [cite: 112]
        System.out.println("D - Delete an item stack"); // [cite: 112]
        System.out.println("P - Print inventory (All or by Category)"); // [cite: 113]
        System.out.println("S - Save inventory to file"); // [cite: 113]
        System.out.println("Q - Quit the program"); // [cite: 113]
        System.out.println("\nCurrent Inventory Summary:"); // [cite: 114]
        boolean inventoryHasItems = inventory.values().stream().anyMatch(m -> m != null && !m.isEmpty()); // [cite: 115] // MODIFIED Check
        if (!inventoryHasItems) {
            System.out.println("  Inventory is currently empty."); // [cite: 116]
        } else {
            for (String category : categories) { // Iterate in defined order // [cite: 117]
                Map<String, ItemDetails> items = inventory.get(category); // MODIFIED
                if (items != null && !items.isEmpty()) { // Only show categories with items // [cite: 117]
                    System.out.println("  Category [" + category + "]: " + items.size() + " item types"); // [cite: 118] // MODIFIED
                }
            }
        }
        System.out.println("------------------------------------"); // [cite: 119]
    }

    // --- Core Inventory Actions ---

    // *** MODIFIED: addItem to handle descriptions ***
    private static void addItem() {
        String category = selectCategory(); // [cite: 120]
        String itemName = SafeInput.getNonZeroLenString(scanner, "Enter the name of the item"); // [cite: 121]
        int quantityToAdd = SafeInput.getInt(scanner, "Enter the quantity to add (can be negative to subtract)"); // [cite: 122]

        Map<String, ItemDetails> categoryItems = inventory.computeIfAbsent(category, k -> new HashMap<>()); // MODIFIED
        ItemDetails currentDetails = categoryItems.get(itemName);
        int currentQuantity = (currentDetails != null) ? currentDetails.quantity : 0;
        String currentDescription = (currentDetails != null) ? currentDetails.description : "";

        int newQuantity = currentQuantity + quantityToAdd; // [cite: 123]

        if (newQuantity <= 0) {
            System.out.println("Resulting quantity for '" + itemName + "' is " + newQuantity + "."); // [cite: 124]
            boolean removeItem = SafeInput.getYNConfirm(scanner, "Remove this item completely?"); // [cite: 124]
            if (removeItem) {
                categoryItems.remove(itemName); // [cite: 125]
                System.out.println("Item '" + itemName + "' removed from " + category + "."); // [cite: 126]
            } else {
                // Keep item with 0 quantity, maybe update description
                String finalDescription = currentDescription;
                if (descriptionCategories.contains(category)) {
                    boolean updateDesc = SafeInput.getYNConfirm(scanner, "Quantity is 0. Update description for '" + itemName + "'? (Current: \"" + finalDescription + "\")");
                    if (updateDesc) {
                        finalDescription = SafeInput.getNonZeroLenString(scanner, "Enter new description (or leave blank)");
                    }
                }
                categoryItems.put(itemName, new ItemDetails(0, finalDescription)); // MODIFIED
                System.out.println("Item '" + itemName + "' quantity set to 0 in " + category + "."); // [cite: 128] // MODIFIED
            }
        } else {
            // Add/Update item with positive quantity
            String finalDescription = currentDescription;
            if (descriptionCategories.contains(category)) {
                if (currentDetails == null) { // It's a new item
                    finalDescription = SafeInput.getNonZeroLenString(scanner, "Enter description for new item '" + itemName + "'");
                } else { // It's an existing item
                    boolean updateDesc = SafeInput.getYNConfirm(scanner, "Update description for '" + itemName + "'? (Current: \"" + finalDescription + "\")");
                    if (updateDesc) {
                        finalDescription = SafeInput.getNonZeroLenString(scanner, "Enter new description");
                    }
                }
            }
            categoryItems.put(itemName, new ItemDetails(newQuantity, finalDescription)); // MODIFIED
            System.out.println("Updated '" + itemName + "' in " + category + ". New quantity: " + newQuantity); // [cite: 130] // MODIFIED
            if (descriptionCategories.contains(category)) {
                System.out.println("  Description: \"" + finalDescription + "\"");
            }
        }
    }

    // *** MODIFIED: deleteItem to handle ItemDetails and potentially show description ***
    private static void deleteItem() {
        boolean inventoryHasItems = inventory.values().stream().anyMatch(m -> m != null && !m.isEmpty()); // [cite: 131] // MODIFIED Check
        if (!inventoryHasItems) {
            System.out.println("Inventory is empty. Nothing to delete."); // [cite: 132]
            return; // [cite: 132]
        }

        String category = selectCategory(); // [cite: 133]
        Map<String, ItemDetails> categoryItems = inventory.get(category); // MODIFIED
        if (categoryItems == null || categoryItems.isEmpty()) { // [cite: 133]
            System.out.println("Category '" + category + "' is empty or does not exist."); // [cite: 134]
            return; // [cite: 134]
        }

        System.out.println("\nItems in category '" + category + "':"); // [cite: 135]
        List<String> itemList = new ArrayList<>(categoryItems.keySet()); // [cite: 136]
        if (itemList.isEmpty()) {
            System.out.println("  No items to delete in this category."); // [cite: 136]
            return;
        }
        itemList.sort(String.CASE_INSENSITIVE_ORDER); // [cite: 137]
        boolean showDesc = descriptionCategories.contains(category); // Check if category supports descriptions

        for (int i = 0; i < itemList.size(); i++) {
            String currentItemName = itemList.get(i); // [cite: 138]
            ItemDetails details = categoryItems.get(currentItemName); // MODIFIED
            System.out.print("  " + (i + 1) + ". " + currentItemName + " (" + details.quantity + ")"); // [cite: 139] // MODIFIED
            if (showDesc && !details.description.isEmpty()) {
                System.out.print(" - \"" + details.description + "\""); // Optionally show description
            }
            System.out.println(); // Newline
        }

        int itemIndex = SafeInput.getRangedInt(scanner, "Enter the number of the item stack to delete", 1, itemList.size()); // [cite: 140]
        String itemToDelete = itemList.get(itemIndex - 1); // [cite: 140]
        ItemDetails detailsToDelete = categoryItems.get(itemToDelete); // Get details for confirmation message

        boolean confirmDelete = SafeInput.getYNConfirm(scanner, "Are you sure you want to delete all '" + itemToDelete + "' (" + detailsToDelete.quantity + ") from " + category + "?"); // [cite: 141] // MODIFIED Message
        if (confirmDelete) {
            ItemDetails removedValue = categoryItems.remove(itemToDelete); // [cite: 142] // MODIFIED
            if (removedValue != null) {
                System.out.println("Item '" + itemToDelete + "' removed from " + category + "."); // [cite: 143]
            } else {
                System.out.println("Item '" + itemToDelete + "' could not be found for removal (unexpected error)."); // [cite: 144]
            }
        } else {
            System.out.println("Deletion cancelled."); // [cite: 145]
        }
    }

    // *** MODIFIED: printInventory to handle descriptions when printing specific categories ***
    private static void printInventory() {
        boolean inventoryHasItems = inventory.values().stream().anyMatch(m -> m != null && !m.isEmpty()); // [cite: 146] // MODIFIED Check
        if (!inventoryHasItems) {
            System.out.println("\nInventory is currently empty."); // [cite: 146]
            return; // [cite: 147]
        }

        System.out.println("\nPrint Options:"); // [cite: 148]
        System.out.println("1. Print All Categories"); // [cite: 148]
        System.out.println("2. Print Specific Category"); // [cite: 148]
        int printChoice = SafeInput.getRangedInt(scanner, "Enter your print choice", 1, 2); // [cite: 148]

        System.out.println("\n--- INVENTORY REPORT for " + currentCharacterName + " ---"); // [cite: 149]
        if (printChoice == 1) {
            // Print All - Descriptions are NOT shown here as per original simple request
            boolean itemsPrinted = false; // [cite: 150]
            for (String category : categories) { // [cite: 150]
                Map<String, ItemDetails> items = inventory.get(category); // [cite: 151] // MODIFIED
                if (items != null && !items.isEmpty()) { // [cite: 151]
                    System.out.println("\nCategory: " + category); // [cite: 152]
                    System.out.println("--------------------"); // [cite: 152]
                    List<String> sortedItemNames = new ArrayList<>(items.keySet()); // [cite: 153]
                    sortedItemNames.sort(String.CASE_INSENSITIVE_ORDER); // [cite: 153]
                    for (String itemName : sortedItemNames) {
                        System.out.printf("  - %-25s : %d\n", itemName, items.get(itemName).quantity); // [cite: 153] // MODIFIED - Increased spacing slightly
                    }
                    itemsPrinted = true; // [cite: 154]
                }
            }
            if (!itemsPrinted) {
                System.out.println("Inventory contains no items."); // [cite: 155]
            }
        } else {
            // Print Specific Category - Descriptions ARE shown for relevant categories
            String categoryToPrint = selectCategory(); // [cite: 156]
            Map<String, ItemDetails> items = inventory.get(categoryToPrint); // [cite: 156] // MODIFIED

            System.out.println("\nCategory: " + categoryToPrint); // [cite: 157]
            System.out.println("--------------------"); // [cite: 157]
            if (items == null || items.isEmpty()) { // [cite: 157]
                System.out.println("  No items in this category."); // [cite: 158]
            } else {
                List<String> sortedItemNames = new ArrayList<>(items.keySet()); // [cite: 159]
                sortedItemNames.sort(String.CASE_INSENSITIVE_ORDER); // [cite: 159]
                boolean showDesc = descriptionCategories.contains(categoryToPrint); // Check if descriptions apply

                for (String itemName : sortedItemNames) {
                    ItemDetails details = items.get(itemName); // MODIFIED
                    // Print differently based on whether descriptions are supported and present
                    if (showDesc && !details.description.isEmpty()) {
                        System.out.printf("  - %-25s (%d): %s\n", itemName, details.quantity, details.description); // MODIFIED - Show desc
                    } else {
                        System.out.printf("  - %-25s : %d\n", itemName, details.quantity); // MODIFIED - No desc (or Money category)
                    }
                }
            }
        }
        System.out.println("--- END OF REPORT ---"); // [cite: 161]
    }

    // --- File Persistence ---

    // *** MODIFIED: loadInventory to handle descriptions and new format ***
    private static void loadInventory() {
        initializeEmptyInventory(); // [cite: 162] // Clear out any previous character's data

        if (currentActiveInventoryFile == null) {
            System.err.println("Error: No character inventory file selected for loading."); // [cite: 163]
            return; // Should not happen if selectOrCreateCharacter worked
        }

        File file = new File(currentActiveInventoryFile); // [cite: 164]
        if (!file.exists()) {
            System.out.println("Inventory file (" + file.getName() + ") not found. Starting with empty inventory for " + currentCharacterName + "."); // [cite: 165]
            return;
        }
        if (!file.canRead()) {
            System.err.println("Error: Cannot read inventory file (check permissions): " + currentActiveInventoryFile); // [cite: 166]
            System.out.println("Starting with empty inventory due to permissions issue."); // [cite: 166]
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) { // [cite: 167]
            String line;
            int lineNumber = 0; // [cite: 168]
            while ((line = reader.readLine()) != null) { // [cite: 168]
                lineNumber++;
                line = line.trim(); // [cite: 168]
                if (line.isEmpty() || line.startsWith("#")) continue; // [cite: 168]

                // Use split with a limit to handle descriptions containing semicolons
                String[] parts = line.split(";", 4); // MODIFIED Limit to 4 parts

                if (parts.length >= 3) { // Need at least Category, Item, Quantity
                    String category = parts[0]; // [cite: 170]
                    String itemName = parts[1]; // [cite: 170]
                    try {
                        int quantity = Integer.parseInt(parts[2]); // [cite: 171]
                        String description = ""; // Default empty description
                        if (parts.length == 4 && descriptionCategories.contains(category)) {
                            // Only load description if it exists AND category supports it
                            description = parts[3];
                        }

                        // Ensure category exists in map, add dynamically if needed (as per original code)
                        Map<String, ItemDetails> categoryItems = inventory.computeIfAbsent(category, k -> { // [cite: 171] // MODIFIED
                            System.out.println("Warning: Category '" + k + "' found in file but not pre-defined. Adding it."); // [cite: 172]
                            if (!categories.contains(k)) { // [cite: 172]
                                categories.add(k); // [cite: 173]
                            }
                            return new HashMap<>(); // [cite: 174]
                        });

                        categoryItems.put(itemName, new ItemDetails(quantity, description)); // [cite: 174] // MODIFIED

                    } catch (NumberFormatException e) {
                        System.out.println("Warning: Skipping line " + lineNumber + " with invalid quantity in file: " + line); // [cite: 175]
                    }
                } else {
                    System.out.println("Warning: Skipping malformed line " + lineNumber + " in file: " + line); // [cite: 176]
                }
            }
            System.out.println("Inventory for " + currentCharacterName + " loaded successfully."); // [cite: 177]
        } catch (IOException e) {
            System.err.println("Error loading inventory from file '" + currentActiveInventoryFile + "': " + e.getMessage()); // [cite: 178]
            initializeEmptyInventory(); // Reset to empty state on error [cite: 179]
            System.out.println("Starting with empty inventory due to loading error."); // [cite: 179]
        }
    }


    // *** MODIFIED: saveInventory to handle descriptions and new format ***
    private static void saveInventory() {
        if (currentActiveInventoryFile == null) {
            System.err.println("Error: No character inventory file selected for saving."); // [cite: 180]
            return; // Should not happen
        }

        File inventoryFile = new File(currentActiveInventoryFile); // [cite: 181]
        File parentDir = inventoryFile.getParentFile(); // [cite: 181]

        if (parentDir == null || !parentDir.canWrite()) { // [cite: 182]
            System.err.println("Error: Cannot write to inventory directory (check permissions): " + (parentDir != null ? parentDir.getAbsolutePath() : "Invalid Path")); // [cite: 182]
            System.err.println("Inventory NOT saved."); // [cite: 182]
            return;
        }

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(inventoryFile)))) { // [cite: 182]
            writer.println("# Inventory Data for: " + currentCharacterName); // [cite: 183]
            writer.println("# Saved on: " + LocalDateTime.now()); // [cite: 183]
            writer.println("# Format: Category;ItemName;Quantity[;Description]"); // NEW: Header explaining format
            for (String category : categories) { // [cite: 184]
                Map<String, ItemDetails> items = inventory.get(category); // [cite: 184] // MODIFIED
                if (items != null && !items.isEmpty()) { // [cite: 184]
                    List<String> sortedItemNames = new ArrayList<>(items.keySet()); // [cite: 185]
                    sortedItemNames.sort(String.CASE_INSENSITIVE_ORDER); // [cite: 185]
                    for (String itemName : sortedItemNames) {
                        ItemDetails details = items.get(itemName); // MODIFIED
                        writer.print(category + ";" + itemName + ";" + details.quantity); // [cite: 186] // MODIFIED - Print base part
                        // Append description only if category supports it and description is not empty
                        if (descriptionCategories.contains(category) && !details.description.isEmpty()) {
                            writer.print(";" + details.description); // MODIFIED - Append description
                        }
                        writer.println(); // Newline after each item
                    }
                }
            }
            System.out.println(currentCharacterName + "'s inventory successfully saved."); // [cite: 187]
        } catch (IOException e) {
            System.err.println("Error saving inventory to file '" + currentActiveInventoryFile + "': " + e.getMessage()); // [cite: 188]
            System.err.println("Inventory NOT saved."); // [cite: 188]
        } catch (SecurityException se) {
            System.err.println("Error saving inventory due to security restrictions: " + se.getMessage()); // [cite: 189]
            System.err.println("Inventory NOT saved."); // [cite: 189]
        }
    }
}