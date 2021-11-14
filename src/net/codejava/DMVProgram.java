package net.codejava;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;
import java.sql.Statement;
import java.sql.ResultSet;


public class DMVProgram {
	
	static String jdbcURL = "jdbc:postgresql://localhost:5432/DMV";
	static String username = "postgres";
	static String password = "SeaBiscuit1738#$!";
	static Scanner scanner = new Scanner(System.in);
	
	static boolean checkUsernameAndPasswordValidity(String input) {
		if ( input.length() < 8 ) {
			System.out.println("Username cannot be shorter than 8 characters. ");
			return false;
		}
		if (input.contains(";") || input.contains("\"") || input.contains(":") || input.contains("\'"))
		{
			System.out.println("Username cannot contain special characters ';', ', '\', or ':' ");
			return false;
		}
		return true;
	}
	
	static void Register() {
		System.out.println("Please enter the username you would like to have for your account: ");
		String newUsername = "";
		newUsername = scanner.nextLine();
		if (!checkUsernameAndPasswordValidity(newUsername)) {
			Register();
			return;
		}
		try {
			Connection connection = DriverManager.getConnection(jdbcURL, username, password);
			String query = "SELECT username FROM \"users\"";
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				String indexedUsername = rs.getString(1);
		        if (indexedUsername.equals(newUsername)) {
		        	System.out.println("A User with this username already exists");
		        	return;
		        }
		    }
		}
		catch (SQLException e) {
			System.out.println("Error in connecting to PostgreSQL server");
			e.printStackTrace();
		}
		System.out.println("Please enter your gender(M/F/N/P (Prefer not to answer)): ");
		boolean enteredInput = false;
		char gender = '\0';
		while(enteredInput == false) {
			gender = scanner.next().charAt(0);
			if (gender != '\0') {
				enteredInput = true;
				System.out.println(gender);
			}
		}
		System.out.println("Please enter your firstName: ");
		enteredInput = false;
		String firstName = "";
		while(enteredInput == false) {
			firstName = scanner.nextLine();
			if (!firstName.isEmpty()) {
				enteredInput = true;
			}
		}
		System.out.println("Please enter your last name: ");
		enteredInput = false;
		String lastName = "";
		while(enteredInput == false) {
			lastName = scanner.nextLine();
			if (!lastName.isEmpty()) {
				enteredInput = true;
			}
		}
		System.out.println("Please initialize your accounts password: ");
		enteredInput = false;
		String accountPassword = "";
		while(enteredInput == false) {
			accountPassword = scanner.nextLine();
			if (!accountPassword.isEmpty()) {
				enteredInput = true;
			}
		}
		System.out.println("Please enter your address: ");
		enteredInput = false;
		String address = "";
		while(enteredInput == false) {
			address = scanner.nextLine();
			if (!address.isEmpty()) {
				enteredInput = true;
			}
		}
		System.out.println("Please enter the year you were born: ");
		enteredInput = false;
		int year = 0;
		while(enteredInput == false) {
			year = scanner.nextInt();
			if (year != 0) {
				enteredInput = true;
			}
		}
		System.out.println("Please enter the month of the year you were born as a digit (i.e. 1 as January and 12 as December: ");
		enteredInput = false;
		int month = 0;
		while(enteredInput == false) {
			month = scanner.nextInt();
			if (month != 0) {
				enteredInput = true;
			}
		}
		System.out.println("Please enter the day of the year you were born: ");
		enteredInput = false;
		int day = 0;
		while(enteredInput == false) {
			day = scanner.nextInt();
			if (day != 0) {
				enteredInput = true;
			}
		}
		try {
			Connection connection = DriverManager.getConnection(jdbcURL, username, password);
			String query = "INSERT INTO \"users\" (\"gender\", \"fname\", \"lname\", \"username\", \"password\", \"address\", \"DOB\") "
					+ "VALUES ('" + gender + "', '" + firstName + "', '" + lastName + "', '" +  newUsername + "', '" + accountPassword + "', '" 
					+ address + "', '" + year + "-0" + month + "-" + day + "');";      
			System.out.println(query);
			Statement stmt = connection.createStatement();
			int rows = stmt.executeUpdate(query);
			if (rows > 0) {
				System.out.println("Account Successfully created!");
			}
			
			connection.close();
		}
		catch (SQLException e) {
			System.out.println("Error in connecting to PostgreSQL server");
			e.printStackTrace();
		}
	}
	
	static void Login() {
		System.out.println("Please enter your username: ");
		String loginUsername = "";
		loginUsername = scanner.nextLine();
		if (loginUsername.isEmpty()) {
			System.out.println("Please enter a username:");
			Login();
			return;
		}
		try { 
			Connection connection = DriverManager.getConnection(jdbcURL, username, password);
			String query = "SELECT username FROM \"users\"";
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				String indexedUsername = rs.getString(1);
		        if (!indexedUsername.equals(loginUsername)) {
		        	System.out.println("Username not found in database");
		        	Login();
		        	return;
		        }
		    }
			connection.close();
		}
		catch (SQLException e) {
			System.out.println("Error in connecting to PostgreSQL server");
			e.printStackTrace();
		}
		System.out.println("Please enter your password: ");
		String loginPassword = "";
		loginPassword = scanner.nextLine();
		if (loginPassword.isEmpty()) {
			System.out.println("Please enter a username:");
			Login();
			return;
		}
		try {
			Connection connection = DriverManager.getConnection(jdbcURL, username, password);
			String query = "SELECT password FROM \"users\" WHERE username = '" + loginUsername + "';";
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				String indexedpassword = rs.getString(1);
		        if (indexedpassword.equals(loginPassword)) {
		        	System.out.println("You have sucessfully logged in!");
		        }
		    }
			connection.close();
		}
		catch (SQLException e) {
			System.out.println("Error in connecting to PostgreSQL server");
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		String jdbcURL = "jdbc:postgresql://localhost:5432/DMV";
		String username = "postgres";
		String password = "SeaBiscuit1738#$!";
		
		try {
			Connection connection = DriverManager.getConnection(jdbcURL, username, password);
			System.out.println("Connected to PostgreSQL server");
			connection.close();
		}
		catch (SQLException e) {
			System.out.println("Error in connecting to PostgreSQL server");
			e.printStackTrace();
		}
		System.out.println("Welcome to the DMV Website, please select from the options below what you would like to do:");
		System.out.println("[1] Login");
		System.out.println("[2] Create An Account");
		System.out.print("Make a selection: ");
		String selection = scanner.nextLine();
		int selectionInt = -1;
        try{
            selectionInt = Integer.parseInt(selection);
        }
        catch (NumberFormatException ex){
            System.out.println("Please Enter an Integer.");
            return;
        }
		if (selectionInt == 1) { //Login Selected
			Login();
		}
		else if(selectionInt == 2) {
			Register();
//			System.out.println("Please enter the username you would like to have for your account: ");
//			String newUsername = "";
//			while(newUsername.isEmpty()) {
//				newUsername = scanner.nextLine();
//			}
//			try {
//				Connection connection = DriverManager.getConnection(jdbcURL, username, password);
//				String query = "SELECT username FROM \"Users\"";
//				Statement stmt = connection.createStatement();
//				ResultSet rs = stmt.executeQuery(query);
//				while (rs.next()) {
//					String indexedUsername = rs.getString(1);
//			        if (indexedUsername.equals(newUsername)) {
//			        	System.out.println("A User with this username already exists");
//			        	return;
//			        }
//			    }
//				
//				connection.close();
//			}
//			catch (SQLException e) {
//				System.out.println("Error in connecting to PostgreSQL server");
//				e.printStackTrace();
//			}
//			System.out.println("Please enter your gender(M/F/N/P (Prefer not to answer)): ");
//			boolean enteredInput = false;
//			char gender = '\0';
//			while(enteredInput == false) {
//				gender = scanner.next().charAt(0);
//				if (gender != '\0') {
//					enteredInput = true;
//					System.out.println(gender);
//				}
//			}
//			System.out.println("Please enter your firstName: ");
//			enteredInput = false;
//			String firstName = "";
//			while(enteredInput == false) {
//				firstName = scanner.nextLine();
//				if (!firstName.isEmpty()) {
//					enteredInput = true;
//				}
//			}
//			System.out.println("Please enter your last name: ");
//			enteredInput = false;
//			String lastName = "";
//			while(enteredInput == false) {
//				lastName = scanner.nextLine();
//				if (!lastName.isEmpty()) {
//					enteredInput = true;
//				}
//			}
//			System.out.println("Please initialize your accounts password: ");
//			enteredInput = false;
//			String accountPassword = "";
//			while(enteredInput == false) {
//				accountPassword = scanner.nextLine();
//				if (!accountPassword.isEmpty()) {
//					enteredInput = true;
//				}
//			}
//			System.out.println("Please enter your address: ");
//			enteredInput = false;
//			String address = "";
//			while(enteredInput == false) {
//				address = scanner.nextLine();
//				if (!address.isEmpty()) {
//					enteredInput = true;
//				}
//			}
//			System.out.println("Please enter the year you were born: ");
//			enteredInput = false;
//			int year = 0;
//			while(enteredInput == false) {
//				year = scanner.nextInt();
//				if (year != 0) {
//					enteredInput = true;
//				}
//			}
//			System.out.println("Please enter the month of the year you were born as a digit (i.e. 1 as January and 12 as December: ");
//			enteredInput = false;
//			int month = 0;
//			while(enteredInput == false) {
//				month = scanner.nextInt();
//				if (month != 0) {
//					enteredInput = true;
//				}
//			}
//			System.out.println("Please enter the day of the year you were born: ");
//			enteredInput = false;
//			int day = 0;
//			while(enteredInput == false) {
//				day = scanner.nextInt();
//				if (day != 0) {
//					enteredInput = true;
//				}
//			}
//			try {
//				Connection connection = DriverManager.getConnection(jdbcURL, username, password);
//				String query = "INSERT INTO \"users\" (\"gender\", \"fname\", \"lname\", \"username\", \"password\", \"address\", \"DOB\") "
//						+ "VALUES ('" + gender + "', '" + firstName + "', '" + lastName + "', '" +  newUsername + "', '" + accountPassword + "', '" 
//						+ address + "', '" + year + "-0" + month + "-" + day + "');";      
//				System.out.println(query);
//				Statement stmt = connection.createStatement();
//				int rows = stmt.executeUpdate(query);
//				if (rows > 0) {
//					System.out.println("Account Successfully created!");
//				}
//				
//				connection.close();
//			}
//			catch (SQLException e) {
//				System.out.println("Error in connecting to PostgreSQL server");
//				e.printStackTrace();
//			}
		}
		else {
			System.out.println("Please enter a value within range.");
		}
	}
}
