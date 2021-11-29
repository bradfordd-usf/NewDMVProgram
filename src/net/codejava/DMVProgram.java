package net.codejava;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.*;
import java.time.*;
import java.time.DayOfWeek;

public class DMVProgram {
	
	static String jdbcURL = "jdbc:postgresql://localhost:5433/DMV";
	static String username = "postgres";
	static String password = "Morgana1!";
	static Scanner scanner = new Scanner(System.in);
	static String loggedUsername = "";
	
	static void scheduleTest(char licenseClass) { 
		try {
			Connection connection = DriverManager.getConnection(jdbcURL, username, password);
			String query = "SELECT * FROM \"drivinglesson\" WHERE class= '" + licenseClass + "';";
			System.out.println(query);
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
		}
		
		catch(SQLException e) {
			System.out.println("Error in connecting to PostgreSQL server");
			e.printStackTrace();
		}
		System.out.println("Rules for scheduling lessons and tests: ");
		System.out.println("Can only schedule one test for a specific license at a time");
		System.out.println("May schedule up to two lessons in advance.");
		System.out.println("Can only schedule one 2-hour test on Monday-Friday during the following times: ");
		System.out.println("9AM, 11AM, 1PM, 3PM");
		System.out.println("Please enter the date that you wish to take the test (format: YYYY-MM-DD)");
		String buffer = scanner.nextLine();
		LocalDate testDate;
		try {
			testDate = LocalDate.parse(buffer);
		}
		catch(DateTimeParseException e) {
			System.out.println("Please enter a valid date");
			scheduleTest(licenseClass);
			return;
		}
		
		LocalDate currDate = LocalDate.now();
		long daysBetween = ChronoUnit.DAYS.between(currDate, testDate);
		if (daysBetween > 21) {
			System.out.println("Please enter a date that is only three weeks from today.");
			scheduleTest(licenseClass);
			return;
		}
		if (daysBetween < 1) {
			System.out.println("Please don't enter today's date or a date that has already passed.");
			scheduleTest(licenseClass);
			return;
		}
		DayOfWeek dayOfWeek = DayOfWeek.from(testDate);
		if(dayOfWeek.name() == "SATURDAY" || dayOfWeek.name() == "SUNDAY") {
			System.out.println("Please enter a date that doesn't occur on Saturday or Sunday.");
			scheduleTest(licenseClass);
			return;
		}
		
		
	}
	static void scheduleDrivingTest() { //Assume each Driving Test is one hour and testing is from 9-5
		//First thing we must do is determine what class of license the user is trying to get
		System.out.println("Please enter the class of license (A, B, C, or E) that you would like to be tested on:");
		char licenseClass = '\0';
		int numOfChoices = 1;
		String licenseString = scanner.nextLine();
		licenseClass = licenseString.charAt(0);
		Vector selectableInstructors = new Vector(0);
		if (Character.isLowerCase(licenseClass)) {
			System.out.println("toUpper");
			licenseClass = Character.toUpperCase(licenseClass);
			System.out.println(licenseClass);
		}
		if (licenseClass != '\0') {
			if (licenseClass != 'A' && licenseClass != 'B' && licenseClass == 'C' && licenseClass == 'E') {
				System.out.println("Please Enter A, B, C, or E");
				scheduleDrivingTest();
				return;
			}
		}
		else {
			System.out.println("Please make a selection.");
			scheduleDrivingTest();
			return;
		}
		try {
			Connection connection = DriverManager.getConnection(jdbcURL, username, password);
			String query = "SELECT * FROM \"" + licenseClass + "instructors\";";
			System.out.println(query);
			Statement stmt = connection.createStatement();
			ResultSet instructors = stmt.executeQuery(query);
			while (instructors.next()) {
				String instructorFirstName = instructors.getString(1);
				String instructorLastName = instructors.getString(2);
				String instructorCredentials = instructors.getString(3);
				System.out.println("[" + numOfChoices + "] " + instructorFirstName + " " + instructorLastName + " " + instructorCredentials);
				String entry = instructorFirstName + " " + instructorLastName + " " + instructorCredentials;
				selectableInstructors.add(entry);
				numOfChoices++;
			}
		}
		catch (SQLException e) {
			System.out.println("Error in connecting to PostgreSQL server");
			e.printStackTrace();
		}
		System.out.println("Please select an instructor: ");
		String buffer = scanner.nextLine();
		int bufferInt = 0;
		try{
			bufferInt = Integer.parseInt(buffer);
	    }
	    catch (NumberFormatException ex){
	       System.out.println("Please Enter an Integer.");
	       scheduleDrivingTest();
	       return;
	    }
		if (bufferInt < 1 || bufferInt > numOfChoices) {
			System.out.println("Please Enter an Integer within range.");
			scheduleDrivingTest();
			return;
		}
		//schedule meeting with instructor
	}
	
	static boolean checkForSQLInjection(String input) 
	{
		if (input.contains(";") || input.contains("\"") || input.contains(":") || input.contains("\'"))
		{
			System.out.println("Input cannot contain special characters ';', ', '\"', or ':' ");
			return false;
		}
		return true;
	}
	
	
	//Issues License to motorists who have completed the driving test with a passing grade
	static void issueLicense(String currentUser)
	{
		
		int numChoices = 1;
		Vector<String> selections = new Vector(0);
		try {
			Connection connection = DriverManager.getConnection(jdbcURL, username, password);
			String query = "SELECT * FROM completedtest WHERE pass = 'P';";
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next())
			{
				String motorist = rs.getString(6);
				String licenceClass = rs.getString(2);
				System.out.println("[" + numChoices + "] Motorist: " +  motorist + " Class: " + licenceClass);
				numChoices++;
				String entry = motorist + " " + licenceClass;
				selections.add(entry);
				
			}
			connection.close();
		}
		catch(SQLException e)
		{
			System.out.println("Error in connecting to PostgreSQL server");
			e.printStackTrace();
		}
		
		//choose motorist
		System.out.println("Which motorist would you like to issue license too:");
		String buffer = scanner.nextLine();
		int bufferInt = 0;
		try {
			bufferInt = Integer.parseInt(buffer);
		}
		catch(NumberFormatException ex)
		{
			System.out.println("Please Enter an Integer.");
			issueLicense(username);
			return;
		}
		if(!(bufferInt < 1 || bufferInt > numChoices))
		{
			System.out.println("Motorist Selected.");
		}
		String selection = selections.get(bufferInt - 1);
		String[] selectionArray = selection.split(" ", 0);
		
		
		//chose license number
		String licenseNum = "";
		
		char choice0 = '\0';
		boolean completed0 = false;
		do {
			System.out.println("Would you like to specify a Licence Plate Number for this license? (Y/N)");
			buffer = scanner.nextLine();
			if (buffer.isEmpty()) {
				System.out.println("Please make a selection");
				continue;
			}
			choice0 = buffer.charAt(0);
			Character.toUpperCase(choice0);
			if (choice0 != 'Y' && choice0 != 'N') {
				System.out.println("Please make a selection");
				continue;
			}
			completed0 = true;
		} while (!completed0);
		completed0 = false;
		
		if (choice0 == 'N') {
			try {
				Connection connection = DriverManager.getConnection(jdbcURL, username, password);
				String query = "SELECT uuid_generate_v4()::text;";
				Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(query);
				while(rs.next())
				{
					licenseNum = rs.getString(1);
				}
				
				connection.close();
			}
			catch(SQLException e)
			{
				System.out.println("Error in connecting to PostgreSQL server");
				e.printStackTrace();
			}
			
		} 
		if (choice0 == 'Y') {
			do {
				System.out.println("Please specify the License Number you would like to have for this vehicle.");
				buffer = scanner.nextLine();
				if (buffer.isEmpty()) {
					System.out.println("Please enter the License Number");
					continue;
				}
				if (!checkForSQLInjection(buffer)) {
					boolean repeat = false;
					try {
						Connection connection = DriverManager.getConnection(jdbcURL, username, password);
						String query = "SELECT * FROM license WHERE licensenum = '" + buffer + "';";
						Statement stmt = connection.createStatement();
						ResultSet rs = stmt.executeQuery(query);
						if (rs.next())
						{
							System.out.println("License Number already exists in database.");
							repeat = true;
						}
						
						connection.close();
					}
					catch(SQLException e)
					{
						System.out.println("Error in connecting to PostgreSQL server");
						e.printStackTrace();
					}
					if (repeat) {
						continue;
					}
				}
				else {
					continue;
				}
				licenseNum = buffer;
				completed0 = true;
			} while (!completed0);
			completed0 = false;
		}
		
		
		//choose restrictions
		String restrictions = "";
		char choice1 = '\0';
		boolean completed1 = false;
		do {
			System.out.println("Would you like to specify restrictions for this license? (Y/N)");
			buffer = scanner.nextLine();
			if (buffer.isEmpty()) {
				System.out.println("Please make a selection");
				continue;
			}
			choice1 = buffer.charAt(0);
			Character.toUpperCase(choice1);
			if (choice1 != 'Y' && choice1 != 'N') {
				System.out.println("Please make a selection");
				continue;
			}
			completed1 = true;
		} while (!completed1);
		completed1 = false;
		
		if (choice1 == 'N') {
			restrictions = "NONE";
			
		} 
		if (choice1 == 'Y') {
			do {
				System.out.println("Please specify the restrictions you would like to give this license:");
				buffer = scanner.nextLine();
				if (buffer.isEmpty()) {
					System.out.println("Please enter the restrictions:");
					continue;
				}
				restrictions = buffer;
				completed1 = true;
			} while (!completed1);
			completed1 = false;
		}
		
		
		
		//get date issued
		LocalDate issueDate = LocalDate.now();
		
		//expiration date
		LocalDate exDate = addTenYearsToDate(issueDate);
		
		//class
		String cl = selectionArray[1];
		
		//motorist to issue license
		String user = selectionArray[0];
		
		//create insert query
		try {
			Connection connection = DriverManager.getConnection(jdbcURL, username, password);
			String query = "INSERT INTO License (licensenum,issuedate,expirationdate,restrictions,class,motorist,technician) VALUES (";
			query += "'" + licenseNum + "',";
			query += "'" + issueDate + "',";
			query += "'" + exDate + "',";
			query += "'" + restrictions + "',";
			query += "'" + cl + "',";
			query += "'" + user + "',";
			query += "'" + currentUser + "');";
			System.out.println(query);
			Statement stmt = connection.createStatement();
			int rows = stmt.executeUpdate(query);
			if (rows > 0) {
				System.out.println("Licence Created \n\n");
			}
			
			connection.close();
		}
		catch(SQLException e)
		{
			System.out.println("Error in connecting to PostgreSQL server");
			e.printStackTrace();
		}
		
		technicianView(currentUser);
		
		
	}
	
	//Used to generate expiration date
	static LocalDate addTenYearsToDate(LocalDate currDate) {
        return currDate.plusYears(10);
    }

	
	static void registerVehicles(String currentUser)
	{
		int numChoices = 1;
		Vector<String> selections = new Vector(0);
		try {
			Connection connection = DriverManager.getConnection(jdbcURL, username, password);
			String query = "SELECT * FROM motorist;";
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next())
			{
				String motorist = rs.getString(1);
				System.out.println("[" + numChoices + "]" +  motorist);
				numChoices++;
				String entry = motorist;
				selections.add(entry);
				
			}
			connection.close();
		}
		catch(SQLException e)
		{
			System.out.println("Error in connecting to PostgreSQL server");
			e.printStackTrace();
		}
		
		System.out.println("Which motorist would you like to register a vehicle to:");
		String buffer = scanner.nextLine();
		int bufferInt = 0;
		try {
			bufferInt = Integer.parseInt(buffer);
		}
		catch(NumberFormatException ex)
		{
			System.out.println("Please Enter an Integer.");
			issueLicense(username);
			return;
		}
		if(!(bufferInt < 1 || bufferInt > numChoices))
		{
			System.out.println("Motorist Selected.");
		}
		
		String vid = "";
		String user = selections.get(bufferInt - 1);
		String manu = "";
		int oduo = -1;
		String model = "";
		int year = -1;
		
		
		char choice = '\0';
		boolean completed = false;
		do {
			System.out.println("Would you like to specify a VID for this vehicle? (Y/N)");
			buffer = scanner.nextLine();
			if (buffer.isEmpty()) {
				System.out.println("Please make a selection");
				continue;
			}
			choice = buffer.charAt(0);
			Character.toUpperCase(choice);
			if (choice != 'Y' && choice != 'N') {
				System.out.println("Please make a selection");
				continue;
			}
			completed = true;
		} while (!completed);
		completed = false;
		
		if (choice == 'N') {
			try {
				Connection connection = DriverManager.getConnection(jdbcURL, username, password);
				String query = "SELECT uuid_generate_v4()::text;";
				Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(query);
				while(rs.next())
				{
					vid = rs.getString(1);
				}
				
				connection.close();
			}
			catch(SQLException e)
			{
				System.out.println("Error in connecting to PostgreSQL server");
				e.printStackTrace();
			}
			
		} 
		if (choice == 'Y') {
			do {
				System.out.println("Please specify the VID you would like to have for this vehicle.");
				buffer = scanner.nextLine();
				if (buffer.isEmpty()) {
					System.out.println("Please enter the VID");
					continue;
				}
				if (!checkForSQLInjection(buffer)) {
					boolean repeat = false;
					try {
						Connection connection = DriverManager.getConnection(jdbcURL, username, password);
						String query = "SELECT * FROM vehicle WHERE VID = '" + buffer + "';";
						Statement stmt = connection.createStatement();
						ResultSet rs = stmt.executeQuery(query);
						if (rs.next())
						{
							System.out.println("VID already exists in database.");
							repeat = true;
						}
						
						connection.close();
					}
					catch(SQLException e)
					{
						System.out.println("Error in connecting to PostgreSQL server");
						e.printStackTrace();
					}
					if (repeat) {
						continue;
					}
				}
				else {
					continue;
				}
				vid = buffer;
				completed = true;
			} while (!completed);
			completed = false;
		}
		do {
			System.out.println("please enter the manufacturer");
			buffer = scanner.nextLine();
			if (buffer.isEmpty()) {
				continue;
			}
			if (checkForSQLInjection(buffer)) { 
				continue;
			} 
			manu = buffer;
			completed = true;
		} while (!completed); 
		completed = false;
		do {
			System.out.println("please enter the model");
			buffer = scanner.nextLine();
			if (buffer.isEmpty()) {
				continue;
			}
			if (checkForSQLInjection(buffer)) { 
				continue;
			} 
			model = buffer;
			completed = true;
		} while (!completed); 
		completed = false;
		do {
			System.out.println("please enter the year");
			buffer = scanner.nextLine();
			bufferInt = 0;
			if (buffer.isEmpty()) {
				continue;
			}
			if (checkForSQLInjection(buffer)) { 
				continue;
			} 
			try {
				bufferInt = Integer.parseInt(buffer);
			}
			catch(NumberFormatException ex)
			{
				System.out.println("Please Enter an Integer.");
				continue;
			}
			if (bufferInt >= 2030 || bufferInt < 1900) {
				System.out.println("Please Enter a valid year.");
				continue;
			}
			year = bufferInt;
			completed = true;
		} while (!completed); 
		completed = false;
		do {
			System.out.println("please enter the reading on the odometer.");
			buffer = scanner.nextLine();
			bufferInt = 0;
			if (buffer.isEmpty()) {
				continue;
			}
			if (checkForSQLInjection(buffer)) { 
				continue;
			} 
			try {
				bufferInt = Integer.parseInt(buffer);
			}
			catch(NumberFormatException ex)
			{
				System.out.println("Please Enter an Integer.");
				continue;
			}
			if (bufferInt < 0) {
				System.out.println("Please Enter a valid reading.");
				continue;
			}
			oduo = bufferInt;
			completed = true;
		} while (!completed); 
		completed = false;
		try {
			Connection connection = DriverManager.getConnection(jdbcURL, username, password);
			String query = "INSERT INTO vehicle (VID,owner,technician,manufacturer,oduometer,model,year) VALUES (";
			query += "'" + vid + "',";
			query += "'" + user + "',";
			query += "'" + currentUser + "',";
			query += "'" + manu + "',";
			query += oduo + ",";
			query += "'" + model + "',";
			query += year + ");";
			System.out.println(query);
			Statement stmt = connection.createStatement();
			int rows = stmt.executeUpdate(query);
			if (rows > 0) {
				System.out.println("Licence Created \n\n");
			}
			
			connection.close();
		}
		catch(SQLException e)
		{
			System.out.println("Error in connecting to PostgreSQL server");
			e.printStackTrace();
		}
		
		technicianView(currentUser);
		
	}
	
	static void addTechnician(String currentUser)
	{
		System.out.println("Please enter the username you would like the technician to have for the account: ");
		String newUsername = "";
		newUsername = scanner.nextLine();
		if (!checkUsernameAndPasswordValidity(newUsername)) {
			addTechnician(currentUser);
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
		        	addTechnician(currentUser);
		        	return;
		        }
		    }
		}
		catch (SQLException e) {
			System.out.println("Error in connecting to PostgreSQL server");
			e.printStackTrace();
		}
		System.out.println("Please enter your gender(M/F/N/P (Prefer not to answer)): ");
		char gender = '\0';
		String genderString = scanner.nextLine();
		gender = genderString.charAt(0);
		if (gender != '\0') {
			if (gender == '\"' || gender == ';' || gender == ':' || gender == '\'') {
				System.out.println("Gender cannot be a special character (i.e. :, ', or ;.");
				addTechnician(currentUser);
				return;
			}
		}
		else {
			System.out.println("Please Enter a gender.");
			addTechnician(currentUser);
			return;
		}
		System.out.println("Please enter your firstName: ");
		String firstName = "";
		firstName = scanner.nextLine();
		if (firstName.isEmpty()) {
			System.out.println("please enter your first name");
			addTechnician(currentUser);
			return;
		}
		else {
			if (firstName.contains(";") || firstName.contains("\"") || firstName.contains(":") || firstName.contains("\'")) {
				System.out.println("first name cannot contain special characters ';', ', '\"', or ':' ");
				addTechnician(currentUser);
				return;
			}
		}
		System.out.println("Please enter your lastName: ");
		String lastName = "";
		lastName = scanner.nextLine();
		if (lastName.isEmpty()) {
			System.out.println("please enter your last name");
			addTechnician(currentUser);
			return;
		}
		else {
			if (lastName.contains(";") || lastName.contains("\"") || lastName.contains(":") || lastName.contains("\'")) {
				System.out.println("last name cannot contain special characters ';', ', '\"', or ':' ");
				addTechnician(currentUser);
				return;
			}
		}
		System.out.println("Please initialize the accounts password: ");
		String accountPassword = "";
		accountPassword = scanner.nextLine();
		if (!checkUsernameAndPasswordValidity(accountPassword)) {
			addTechnician(currentUser);
			return;
		}
		System.out.println("Please enter the technician's address: ");
		String address = "";
		address = scanner.nextLine();
		if (address.isEmpty()) {
			System.out.println("please enter the technician's address");
			addTechnician(currentUser);
			return;
		}
		else {
			if (address.contains(";") || address.contains("\"") || address.contains(":") || address.contains("\'")) {
				System.out.println("last name cannot contain special characters ';', ', '\"', or ':' ");
				addTechnician(currentUser);
				return;
			}
		}
		System.out.println("Please enter the year the technician was born: ");
		String buffer = scanner.nextLine();
		int bufferInt = -1;
		try {
			bufferInt = Integer.parseInt(buffer);
		}
		catch (NumberFormatException ex){
          System.out.println("Please Enter an Integer.");
          addTechnician(currentUser);
          return;
        }
		if (bufferInt > 3000 || bufferInt < 1000) {
			System.out.println("Please Enter a year that the technician was born.");
			addTechnician(currentUser);
		}
		int year = bufferInt;

		System.out.println("Please enter the month of the year the technician was born as a digit (i.e. 1 as January and 12 as December: ");
		buffer = scanner.nextLine();
		try{
			bufferInt = Integer.parseInt(buffer);
	    }
	    catch (NumberFormatException ex){
	       System.out.println("Please Enter an Integer.");
	       return;
	    }
		int month = bufferInt;
		if (month > 12 || month < 1) {
			System.out.println("Please enter a valid month.");
		}
		System.out.println("Please enter the day of the year the technician was born: ");
		buffer = scanner.nextLine();
		try{
			bufferInt = Integer.parseInt(buffer);
	    }
	    catch (NumberFormatException ex){
	       System.out.println("Please Enter an Integer.");
	       return;
	    }
		int day = bufferInt;
		if (day > 31 || day < 1) {
			System.out.println("Please enter a valid day of the month.");
		}
		String date = formatDate(year, month, day);
		
		System.out.println("Please enter your the technician's shift type: ");
		String shift = "";
		shift = scanner.nextLine();
		if (shift.isEmpty()) {
			System.out.println("please enter your first name");
			addTechnician(currentUser);
			return;
		}
		else {
			if (shift.contains(";") || shift.contains("\"") || shift.contains(":") || shift.contains("\'")) {
				System.out.println("Shift type cannot contain special characters ';', ', '\"', or ':' ");
				addTechnician(currentUser);
				return;
			}
		}
		
		
		
		try {
			Connection connection = DriverManager.getConnection(jdbcURL, username, password);
			String query = "INSERT INTO \"users\" (\"gender\", \"firstname\", \"lastname\", \"username\", \"password\", \"address\", \"dob\") "
					+ "VALUES ('" + gender + "', '" + firstName + "', '" + lastName + "', '" +  newUsername + "', '" + accountPassword + "', '" 
					+ address + "', '" + date + "');";      
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
		try {
			Connection connection = DriverManager.getConnection(jdbcURL, username, password);
			String query = "INSERT INTO \"technician\" (\"gender\", \"firstname\", \"lastname\", \"username\", \"password\", \"address\", \"dob\", \"shifttype\") "
					+ "VALUES ('" + gender + "', '" + firstName + "', '" + lastName + "', '" +  newUsername + "', '" + accountPassword + "', '" 
					+ address + "', '" + date + "', '" + shift + "');";      
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
		
		technicianView(currentUser);
	}
	static void addInstructor()
	{
		System.out.println("Please enter the username you would like the instructor to have for the account: ");
		String newUsername = "";
		newUsername = scanner.nextLine();
		if (!checkUsernameAndPasswordValidity(newUsername)) {
			addInstructor();
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
		        	addInstructor();
		        	return;
		        }
		    }
		}
		catch (SQLException e) {
			System.out.println("Error in connecting to PostgreSQL server");
			e.printStackTrace();
		}
		System.out.println("Please enter your gender(M/F/N/P (Prefer not to answer)): ");
		char gender = '\0';
		String genderString = scanner.nextLine();
		gender = genderString.charAt(0);
		if (gender != '\0') {
			if (gender == '\"' || gender == ';' || gender == ':' || gender == '\'') {
				System.out.println("Gender cannot be a special character (i.e. :, ', or ;.");
				addInstructor();
				return;
			}
		}
		else {
			System.out.println("Please Enter a gender.");
			addInstructor();
			return;
		}
		System.out.println("Please enter the firstName: ");
		String firstName = "";
		firstName = scanner.nextLine();
		if (firstName.isEmpty()) {
			System.out.println("please enter the first name");
			addInstructor();
			return;
		}
		else {
			if (firstName.contains(";") || firstName.contains("\"") || firstName.contains(":") || firstName.contains("\'")) {
				System.out.println("first name cannot contain special characters ';', ', '\"', or ':' ");
				addInstructor();
				return;
			}
		}
		System.out.println("Please enter the last name: ");
		String lastName = "";
		lastName = scanner.nextLine();
		if (lastName.isEmpty()) {
			System.out.println("please enter the last name");
			addInstructor();
			return;
		}
		else {
			if (lastName.contains(";") || lastName.contains("\"") || lastName.contains(":") || lastName.contains("\'")) {
				System.out.println("last name cannot contain special characters ';', ', '\"', or ':' ");
				addInstructor();
				return;
			}
		}
		System.out.println("Please initialize the accounts password: ");
		String accountPassword = "";
		accountPassword = scanner.nextLine();
		if (!checkUsernameAndPasswordValidity(accountPassword)) {
			addInstructor();
			return;
		}
		System.out.println("Please enter the address: ");
		String address = "";
		address = scanner.nextLine();
		if (address.isEmpty()) {
			System.out.println("please enter the address");
			addInstructor();
			return;
		}
		else {
			if (address.contains(";") || address.contains("\"") || address.contains(":") || address.contains("\'")) {
				System.out.println("address cannot contain special characters ';', ', '\"', or ':' ");
				addInstructor();
				return;
			}
		}
		System.out.println("Please enter the year the instructor was born: ");
		String buffer = scanner.nextLine();
		int bufferInt = -1;
		try {
			bufferInt = Integer.parseInt(buffer);
		}
		catch (NumberFormatException ex){
          System.out.println("Please Enter an Integer.");
          addInstructor();
          return;
        }
		if (bufferInt > 3000 || bufferInt < 1000) {
			System.out.println("Please Enter a year the instructor was born:.");
			addInstructor();
		}
		int year = bufferInt;

		System.out.println("Please enter the month of the year the instructor was born: as a digit (i.e. 1 as January and 12 as December: ");
		buffer = scanner.nextLine();
		try{
			bufferInt = Integer.parseInt(buffer);
	    }
	    catch (NumberFormatException ex){
	       System.out.println("Please Enter an Integer.");
	       return;
	    }
		int month = bufferInt;
		if (month > 12 || month < 1) {
			System.out.println("Please enter a valid month.");
		}
		System.out.println("Please enter the day of the year the instructor was born:: ");
		buffer = scanner.nextLine();
		try{
			bufferInt = Integer.parseInt(buffer);
	    }
	    catch (NumberFormatException ex){
	       System.out.println("Please Enter an Integer.");
	       return;
	    }
		int day = bufferInt;
		if (day > 31 || day < 1) {
			System.out.println("Please enter a valid day of the month.");
		}
		String date = formatDate(year, month, day);
		
		System.out.println("Please enter your the Instructors Credentials: ");
		String credentials = "";
		credentials = scanner.nextLine();
		if (credentials.isEmpty()) {
			System.out.println("please enter credentials:");
			addInstructor();
			return;
		}
		else {
			if (credentials.contains(";") || credentials.contains("\"") || credentials.contains(":") || credentials.contains("\'")) {
				System.out.println("Credentials cannot contain special characters ';', ', '\"', or ':' ");
				addInstructor();
				return;
			}
		}
		
		
		
		try {
			Connection connection = DriverManager.getConnection(jdbcURL, username, password);
			String query = "INSERT INTO \"users\" (\"gender\", \"firstname\", \"lastname\", \"username\", \"password\", \"address\", \"dob\") "
					+ "VALUES ('" + gender + "', '" + firstName + "', '" + lastName + "', '" +  newUsername + "', '" + accountPassword + "', '" 
					+ address + "', '" + date + "');";      
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
		try {
			Connection connection = DriverManager.getConnection(jdbcURL, username, password);
			String query = "INSERT INTO \"instructor\" (\"gender\", \"firstname\", \"lastname\", \"username\", \"password\", \"address\", \"dob\", \"credentials\") "
					+ "VALUES ('" + gender + "', '" + firstName + "', '" + lastName + "', '" +  newUsername + "', '" + accountPassword + "', '" 
					+ address + "', '" + date + "', '" + credentials + "');";      
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
		
		//instructView();
	}
	
	static void technicianView(String currentUser)
	{
		System.out.println("[1] Issue licenses");
		System.out.println("[2] Register Vehicles");
		System.out.println("[3] Add New Instructors to the database");
		String buffer = scanner.nextLine();
		if (buffer.isEmpty()) {
			System.out.println("Please make a selection.");
			technicianView(currentUser);
			return;
		}
		int bufferInt = -1;
		try {
			bufferInt = Integer.parseInt(buffer);
		}
		catch (NumberFormatException ex){
	          System.out.println("Please Enter an Integer.");
	          technicianView(currentUser);
	          return;
	    }
		if (bufferInt > 3 || bufferInt < 1) {
			System.out.println("Please Enter an Integer within range.");
			technicianView(currentUser);
			return;
		}
		if(bufferInt==1)
		{
			issueLicense(currentUser);
		}
		if (bufferInt == 2) 
		{
			registerVehicles( currentUser);
		}
		if (bufferInt == 3)
		{
			addTechnician(currentUser);
		}
		
	}
	
	static void motoristView() {
		System.out.println("[1] View Licenses that you currently possess");
		System.out.println("[2] Schedule Driving Tests");
		System.out.println("[3] Schedule a Driving Lesson");
		System.out.println("[4] View Vehicles that you currently have registered");
		System.out.println("[5] View Licenses that you currently possess");
		System.out.println("Please enter your selection: ");
		String buffer = scanner.nextLine();
		if (buffer.isEmpty()) {
			System.out.println("Please make a selection.");
			motoristView();
			return;
		}
		int bufferInt = -1;
		try {
			bufferInt = Integer.parseInt(buffer);
		}
		catch (NumberFormatException ex){
	          System.out.println("Please Enter an Integer.");
	          motoristView();
	          return;
	    }
		if (bufferInt > 5 || bufferInt < 1) {
			System.out.println("Please Enter an Integer within range.");
			motoristView();
			return;
		}
		if (bufferInt == 1) {
			System.out.println("[1] View Licenses that you currently possess");
		}
		if (bufferInt == 2) {
			System.out.println("[2] Schedule Driving Tests");
			//scheduleDrivingTest
		}
		if (bufferInt == 3) {
			System.out.println("[3] Schedule a Driving Lesson");
		}
		if (bufferInt == 4) {
			System.out.println("[4] View Vehicles that you currently have registered");
		}
		if (bufferInt == 5) {
			System.out.println("[5] View Licenses that you currently possess");
		}
		
	}
	static boolean checkUsernameAndPasswordValidity(String input) {
		if ( input.length() < 8 ) {
			System.out.println("Username cannot be shorter than 8 characters. ");
			return false;
		}
		if (input.contains(";") || input.contains("\"") || input.contains(":") || input.contains("\'"))
		{
			System.out.println("Username cannot contain special characters ';', ', '\"', or ':' ");
			return false;
		}
		return true;
	}
	
	static String formatDate(int year, int month, int day) {
		String formattedDate = "";
		formattedDate = String.valueOf(year);
		if (month < 10) {
			formattedDate = formattedDate + "-0" + String.valueOf(month);
		}
		else {
			formattedDate = formattedDate + "-" + String.valueOf(month);
		}
		if (day < 10) {
			formattedDate = formattedDate + "-0" + String.valueOf(day);
		}
		else {
			formattedDate = formattedDate + "-" + String.valueOf(day);
		}
		return formattedDate;
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
		        	Register();
		        	return;
		        }
		    }
		}
		catch (SQLException e) {
			System.out.println("Error in connecting to PostgreSQL server");
			e.printStackTrace();
		}
		System.out.println("Please enter your gender(M/F/N/P (Prefer not to answer)): ");
		char gender = '\0';
		String genderString = scanner.nextLine();
		gender = genderString.charAt(0);
		if (gender != '\0') {
			if (gender == '\"' || gender == ';' || gender == ':' || gender == '\'') {
				System.out.println("Gender cannot be a special character (i.e. :, ', or ;.");
				Register();
				return;
			}
		}
		else {
			System.out.println("Please Enter a gender.");
			Register();
			return;
		}
		System.out.println("Please enter your firstName: ");
		String firstName = "";
		firstName = scanner.nextLine();
		if (firstName.isEmpty()) {
			System.out.println("please enter your first name");
			Register();
			return;
		}
		else {
			if (firstName.contains(";") || firstName.contains("\"") || firstName.contains(":") || firstName.contains("\'")) {
				System.out.println("first name cannot contain special characters ';', ', '\"', or ':' ");
				Register();
				return;
			}
		}
		System.out.println("Please enter your lastName: ");
		String lastName = "";
		lastName = scanner.nextLine();
		if (lastName.isEmpty()) {
			System.out.println("please enter your last name");
			Register();
			return;
		}
		else {
			if (lastName.contains(";") || lastName.contains("\"") || lastName.contains(":") || lastName.contains("\'")) {
				System.out.println("last name cannot contain special characters ';', ', '\"', or ':' ");
				Register();
				return;
			}
		}
		System.out.println("Please initialize your accounts password: ");
		String accountPassword = "";
		accountPassword = scanner.nextLine();
		if (!checkUsernameAndPasswordValidity(accountPassword)) {
			Register();
			return;
		}
		System.out.println("Please enter your address: ");
		String address = "";
		address = scanner.nextLine();
		if (address.isEmpty()) {
			System.out.println("please enter your address");
			Register();
			return;
		}
		else {
			if (address.contains(";") || address.contains("\"") || address.contains(":") || address.contains("\'")) {
				System.out.println("last name cannot contain special characters ';', ', '\"', or ':' ");
				Register();
				return;
			}
		}
		System.out.println("Please enter the year you were born: ");
		String buffer = scanner.nextLine();
		int bufferInt = -1;
		try {
			bufferInt = Integer.parseInt(buffer);
		}
		catch (NumberFormatException ex){
          System.out.println("Please Enter an Integer.");
          Register();
          return;
        }
		if (bufferInt > 3000 || bufferInt < 1000) {
			System.out.println("Please Enter a year that you were born.");
			Register();
		}
		int year = bufferInt;

		System.out.println("Please enter the month of the year you were born as a digit (i.e. 1 as January and 12 as December: ");
		buffer = scanner.nextLine();
		try{
			bufferInt = Integer.parseInt(buffer);
	    }
	    catch (NumberFormatException ex){
	       System.out.println("Please Enter an Integer.");
	       return;
	    }
		int month = bufferInt;
		if (month > 12 || month < 1) {
			System.out.println("Please enter a valid month.");
		}
		System.out.println("Please enter the day of the year you were born: ");
		buffer = scanner.nextLine();
		try{
			bufferInt = Integer.parseInt(buffer);
	    }
	    catch (NumberFormatException ex){
	       System.out.println("Please Enter an Integer.");
	       return;
	    }
		int day = bufferInt;
		if (day > 31 || day < 1) {
			System.out.println("Please enter a valid day of the month.");
		}
		String date = formatDate(year, month, day);
		try {
			Connection connection = DriverManager.getConnection(jdbcURL, username, password);
			String query = "INSERT INTO \"users\" (\"gender\", \"firstname\", \"lastname\", \"username\", \"password\", \"address\", \"dob\") "
					+ "VALUES ('" + gender + "', '" + firstName + "', '" + lastName + "', '" +  newUsername + "', '" + accountPassword + "', '" 
					+ address + "', '" + date + "');";      
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
		try {
			Connection connection = DriverManager.getConnection(jdbcURL, username, password);
			String query = "INSERT INTO \"motorist\" (\"gender\", \"firstname\", \"lastname\", \"username\", \"password\", \"address\", \"dob\") "
					+ "VALUES ('" + gender + "', '" + firstName + "', '" + lastName + "', '" +  newUsername + "', '" + accountPassword + "', '" 
					+ address + "', '" + date + "');";      
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
	static void determineView(String acceptedUsername) { //determines what role the user is and selects their view
		boolean foundRole = false;
		try {
				Connection connection = DriverManager.getConnection(jdbcURL, username, password);
				String query = "SELECT username FROM \"motorist\" WHERE username = '" + acceptedUsername + "' ;";
				Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(query);
				if (rs.next() == true) { 
					foundRole = true; 
				}
				connection.close();
			}
			catch (SQLException e) {
				System.out.println("Error connecting to database: determineView");
				e.printStackTrace();
			}
		if (foundRole == true) {
			loggedUsername = acceptedUsername;
			motoristView();
			return;
		}
		foundRole = false;
		try {
			Connection connection = DriverManager.getConnection(jdbcURL, username, password);
			String query = "SELECT username FROM \"instructor\" WHERE username = '" + acceptedUsername + "' ;";
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next() == true) { 
				foundRole = true;
			}
			connection.close();
		}
		catch (SQLException e) {
			System.out.println("Error connecting to database: determineView");
			e.printStackTrace();
		}
		if (foundRole == true) {
			loggedUsername = acceptedUsername;
			System.out.println("instructor view");
			return;
		}
		foundRole = false;
		try {
			Connection connection = DriverManager.getConnection(jdbcURL, username, password);
			String query = "SELECT username FROM \"technician\" WHERE username = '" + acceptedUsername + "' ;";
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next() == true) { 
				foundRole = true;
			}
			connection.close();
		}
		catch (SQLException e) {
			System.out.println("Error connecting to database: determineView");
			e.printStackTrace();
		}
		if (foundRole == true) {
			loggedUsername = acceptedUsername;
			System.out.println("going to view: technician");
			technicianView(acceptedUsername);
			return;
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
		Boolean match = false;
		try { 
			Connection connection = DriverManager.getConnection(jdbcURL, username, password);
			Statement stmt = connection.createStatement();
			String query = "SELECT username from Users;";
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				String indexedUsername = rs.getString(1);
		        if (indexedUsername.equals(loginUsername)) {
		        	match = true;
		        }
		    }
			connection.close();
		}
		catch (SQLException e) {
			System.out.println("Error in connecting to PostgreSQL server");
			e.printStackTrace();
		}
		if (match == false) {
			System.out.println("Username not found in database.");
			Login();
			return;
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
		determineView(loginUsername);
	}
	
	public static void main(String[] args) {
		try {
			Connection connection = DriverManager.getConnection(jdbcURL, username, password);
			System.out.println("Connected to PostgreSQL server");
			connection.close();
		}
		catch (SQLException e) {
			System.out.println("Error in connecting to PostgreSQL server");
			e.printStackTrace();
		}
		//scheduleTest('A');
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
		}
		else {
			System.out.println("Please enter a value within range.");
		}
	}
}
