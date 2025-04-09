// Kaden
// Safe Input
// Completed

import java.util.Scanner;

public class SafeInput {

    public static String getNonZeroLenString(Scanner pipe, String prompt)
    {
        String retString = ""; // Set this to zero length. Loop runs until it isn't
        do
        {
            System.out.print("\n" +prompt + ": "); // show prompt add space
            retString = pipe.nextLine();
        }while(retString.length() == 0);

        return retString;

    }



    public static int getInt(Scanner pipe, String prompt) {
        int value = 0;
        boolean done = false;
        String trash;

        do {
            System.out.print(prompt + ": ");

            if (pipe.hasNextInt()) {
                value = pipe.nextInt();
                done = true;
            } else {
                trash = pipe.next();
                System.out.println("Invalid input. Please enter a valid integer.");
            }
            pipe.nextLine(); // Clear the buffer
        } while (!done);

        return value;
    }




    public static double getDouble(Scanner pipe, String prompt) {
        double value = 0;
        boolean done = false;
        String trash;

        do {
            System.out.print(prompt + ": ");

            if (pipe.hasNextDouble()) {
                value = pipe.nextDouble();
                done = true;
            } else {
                trash = pipe.next();
                System.out.println("Invalid input. Please enter a valid double.");
            }
            pipe.nextLine(); // Clear the buffer
        } while (!done);

        return value;
    }



    public static int getRangedInt(Scanner pipe, String prompt, int low, int high) {
        int result = 0;
        boolean done = false;
        String trash = "";

        do{
            System.out.println(prompt+"[" + low + "-" + high + "]: ");
            if(pipe.hasNextInt()) {
                result = pipe.nextInt();;
                pipe.nextLine();

                if(result >= low && result <= high) {
                    done = true;
                }
                else {
                    System.out.println("You must enter a value in the range [" + low + "-" + high + "]");
                }
            }
            else{
                trash = pipe.nextLine();
                System.out.println("You must enter a value in the range [" + low + "-" + high + "]");
            }
        }
        while (!done);

        return result;
    }



    public static double getRangedDouble(Scanner pipe, String prompt, double low, double high) {
        double result = 0;
        boolean done = false;
        String trash = "";

        do{
            System.out.println(prompt+"[" + low + "-" + high + "]: ");
            if(pipe.hasNextDouble()) {
                result = pipe.nextDouble();;
                pipe.nextLine();

                if(result >= low && result <+ high) {
                    done = true;
                }
                else {
                    System.out.println("You must enter a value in the range [" + low + "-" + high + "]");
                }
            }
            else{
                trash = pipe.nextLine();
                System.out.println("You must enter a value in the range [" + low + "-" + high + "]");
            }
        }
        while (!done);

        return result;
    }



    public static boolean getYNConfirm(Scanner pipe, String prompt) {
        String response;
        boolean done = false;
        boolean result = false;

        do {
            System.out.print(prompt + " [Y/N]: ");
            response = pipe.nextLine();

            if (response.equalsIgnoreCase("Y")) {
                result = true;
                done = true;
            } else if (response.equalsIgnoreCase("N")) {
                result = false;
                done = true;
            } else {
                System.out.println("Invalid input. Please enter 'Y' for Yes or 'N' for No.");
            }
        } while (!done);

        return result;
    }




    public static String getRegExString(Scanner pipe, String prompt, String regExPattern) {
        String value = "";
        boolean done = false;

        do{
            System.out.println(prompt + ":");
            value = pipe.nextLine();

            if(value.matches(regExPattern)){
                done=true;
            }
            else{
                System.out.print("\n Invalid input: " + value);
            }

        }while(!done);

        return value;
    }



    public static void prettyHeader(String msg) {
        // Define the width for header
        int width = 60;

        // Calculate length of message
        int msgLength = msg.length();

        // Calculate padding before and after message to center - also 3 stars at beginning and end
        int padding = (width - 6 - msgLength) / 2;

        // Create top and bottom star rows
        for (int i = 0; i < width; i++) {
            System.out.print("*");
        }
        System.out.println();

        // Print middle row with message
        System.out.print("***");
        for (int i = 0; i < padding; i++) {
            System.out.print(" ");
        }
        System.out.print(msg);
        for (int i = 0; i < padding; i++) {
            System.out.print(" ");
        }
        // for when padding is odd to center
        if ((width - 6 - msgLength) % 2 != 0) {
            System.out.print(" ");
        }
        System.out.println("***");

        // Print bottom row of stars
        for (int i = 0; i < width; i++) {
            System.out.print("*");
        }
        System.out.println();  // Move to the next line
    }



    /* -- SOME COMMENTS TO NOTE
     * @param pipe a Scanner opened to read from System.in
     * @param prompt prompt for the user
     * @return a String response that is not zero length
     */
}