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
	
	static String jdbcURL = "jdbc:postgresql://localhost:5432/DMV";
	static String username = "postgres";
	static String password = "boobsandfeet";
	static Scanner scanner = new Scanner(System.in);
	static String loggedUsername = "bradfordy";
	
	static void scheduleDrivingLesson() {
		System.out.println("Please enter the class of license (A, B, C, or E) that you would like to take lessons for:");
		char licenseClass = '\0';
		int numOfChoices = 1;
		String licenseString = scanner.nextLine();
		licenseClass = licenseString.charAt(0);
		Vector<String> selectableInstructors = new Vector(0);
		if (Character.isLowerCase(licenseClass)) {
			System.out.println("toUpper");
			licenseClass = Character.toUpperCase(licenseClass);
			System.out.println(licenseClass);
		}
		if (licenseClass != '\0') {
			if (!(licenseClass != 'A' || licenseClass != 'B' || licenseClass == 'C' || licenseClass == 'E')) {
				System.out.println("Please Enter A, B, C, or E");
				scheduleDrivingLesson();
				return;
			}
		}
		else {
			System.out.println("Please make a selection.");
			scheduleDrivingLesson();
			return;
		}
		try {
			Connection connection = DriverManager.getConnection(jdbcURL, username, password);
			licenseClass = Character.toLowerCase(licenseClass);
			String query = "SELECT * FROM \"drivinglesson\" WHERE class= '" + Character.toUpperCase(licenseClass) + "' AND motorist='" + loggedUsername + "';";
			System.out.println(query);
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next()) {
				;
			}
			if (rs.next()) {
				System.out.println("You cannot schedule more than one test for a particular license class.");
				return;
			}
			connection.close();
		}
		
		catch(SQLException e) {
			System.out.println("Error in connecting to PostgreSQL server");
			e.printStackTrace();
		}
		try {
			Connection connection = DriverManager.getConnection(jdbcURL, username, password);
			String query = "SELECT * FROM \"" + licenseClass + "instructors\";";
			//System.out.println(query);
			Statement stmt = connection.createStatement();
			ResultSet instructors = stmt.executeQuery(query);
			while (instructors.next()) {
				//System.out.println(instructors);
				String instructorFirstName = instructors.getString(1);
				String instructorLastName = instructors.getString(2);
				String instructorCredentials = instructors.getString(3);
				System.out.println("[" + numOfChoices + "] " + instructorFirstName + " " + instructorLastName + " " + instructorCredentials);
				String entry = instructorFirstName + " " + instructorLastName + " " + instructorCredentials;
				System.out.println(entry);
				selectableInstructors.add(entry);
				numOfChoices++;
			}
			connection.close();
		}
		catch (SQLException e) {
			System.out.println("Error in connecting to PostgreSQL server");
			e.printStackTrace();
		}
		System.out.println(selectableInstructors);
		System.out.println("Please select an instructor: ");
		String buffer = scanner.nextLine();
		int bufferInt = 0;
		try{
			bufferInt = Integer.parseInt(buffer);
	    }
	    catch (NumberFormatException ex){
	       System.out.println("Please Enter an Integer.");
	       scheduleDrivingLesson();
	       return;
	    }
		if (bufferInt < 1 || bufferInt > numOfChoices) {
			System.out.println("Instructor Selected.");
		}
		int choice = bufferInt;
		String selection = selectableInstructors.get(choice - 1);
		String[] selectionArray = selection.split(" ", 0);
		System.out.println(selectionArray[0]);
		System.out.println("Below are all of this instructor's taken appointment times");
		//I'm going to do my query to show the user all that instructors scheduled appointments
		try {
			Connection connection = DriverManager.getConnection(jdbcURL, username, password);
			String query = "SELECT * FROM bookedAppointments WHERE instructor = '" + selectionArray[0] + "';";
			System.out.println(query);
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				String appointmentTime = rs.getString(1);
				Date appointmentDate = rs.getDate(2);
				String appointment = appointmentTime + " " + appointmentDate;
				System.out.println(appointment);
			}
			connection.close();
		}
		catch (SQLException e) {
			System.out.println("Error in connecting to PostgreSQL server");
			e.printStackTrace();
		}
		System.out.println("Rules for scheduling lessons and tests: ");
		System.out.println("Can only schedule one 2-hour test on Monday-Friday during the following times: ");
		System.out.println("9AM, 11AM, 1PM, 3PM");
		System.out.println("Please enter the date that you wish to take the test (format: YYYY-MM-DD)");
		buffer = scanner.nextLine();
		LocalDate testDate;
		try {
			testDate = LocalDate.parse(buffer);
		}
		catch(DateTimeParseException e) {
			System.out.println("Please enter a valid date");
			scheduleDrivingLesson();
			return;
		}
		LocalDate currDate = LocalDate.now();
		long daysBetween = ChronoUnit.DAYS.between(currDate, testDate);
		if (daysBetween > 21) {
			System.out.println("Please enter a date that is only three weeks from today.");
			scheduleDrivingLesson();
			return;
		}
		if (daysBetween < 1) {
			System.out.println("Please don't enter today's date or a date that has already passed.");
			scheduleDrivingLesson();
			return;
		}
		DayOfWeek dayOfWeek = DayOfWeek.from(testDate);
		if(dayOfWeek.name() == "SATURDAY" || dayOfWeek.name() == "SUNDAY") {
			System.out.println("Please enter a date that doesn't occur on Saturday or Sunday.");
			scheduleDrivingLesson();
			return;
		}
		System.out.println("Please enter the time in which you want to take the test on that day. (9, 11, 1, or 3)");
		buffer = scanner.nextLine();
		String selectedTime;
		if (buffer.contains("9")) {
			selectedTime = "9AM";
		}
		else if (buffer.contains("11")) {
			selectedTime = "11AM";
		}
		else if (buffer.contains("1")) {
			selectedTime = "1PM";
		}
		else if (buffer.contains("3")) {
			selectedTime = "3PM";
		}
		else {
			System.out.println("Please enter a valid time");
			scheduleDrivingLesson();
			return;
		}
		try {
			Connection connection = DriverManager.getConnection(jdbcURL, username, password);
			String query = "SELECT * FROM bookedAppointments WHERE instructor = '" + testDate + "'  AND time = '" + selectedTime + "';";
			System.out.println(query);
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next()) {
				System.out.println("Please book a time and appointment at a time and date that are not taken.");
				scheduleDrivingLesson();
				return;
			}
			connection.close();
		}
		catch (SQLException e) {
			System.out.println("Error in connecting to PostgreSQL server");
			e.printStackTrace();
		}
		try {
			Connection connection = DriverManager.getConnection(jdbcURL, username, password);
			String query = "INSERT INTO drivinglesson (testID, class, time, date, instructor, motorist)"
			+ "VALUES (uuid_generate_v4(), '" + Character.toUpperCase(licenseClass) + "','" + selectedTime + "', '" + testDate + "', '" + 
			selectionArray[0] + "', '" + loggedUsername + "');";
			Statement stmt = connection.createStatement();
			int rows = stmt.executeUpdate(query);
			if (rows > 0) {
				System.out.println("Test Scheduled!");
			}
		}
		catch (SQLException e) {
			System.out.println("Error in connecting to PostgreSQL server");
			e.printStackTrace();
		}
	}
	
	static void scheduleDrivingTest() { //Assume each Driving Test is two hour and testing is from 9-5
		//First thing we must do is determine what class of license the user is trying to get
		System.out.println("Please enter the class of license (A, B, C, or E) that you would like to be tested on:");
		char licenseClass = '\0';
		int numOfChoices = 1;
		String licenseString = scanner.nextLine();
		licenseClass = licenseString.charAt(0);
		Vector<String> selectableInstructors = new Vector(0);
		if (Character.isLowerCase(licenseClass)) {
			System.out.println("toUpper");
			licenseClass = Character.toUpperCase(licenseClass);
			System.out.println(licenseClass);
		}
		if (licenseClass != '\0') {
			if (!(licenseClass != 'A' || licenseClass != 'B' || licenseClass == 'C' || licenseClass == 'E')) {
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
			licenseClass = Character.toLowerCase(licenseClass);
			String query = "SELECT * FROM \"drivingtest\" WHERE class= '" + Character.toUpperCase(licenseClass) + "' AND motorist='" + loggedUsername + "';";
			System.out.println(query);
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next()) {
				System.out.println("You cannot schedule more than one test for a particular license class.");
				return;
			}
			connection.close();
		}
		
		catch(SQLException e) {
			System.out.println("Error in connecting to PostgreSQL server");
			e.printStackTrace();
		}
		try {
			Connection connection = DriverManager.getConnection(jdbcURL, username, password);
			String query = "SELECT * FROM \"" + licenseClass + "instructors\";";
			//System.out.println(query);
			Statement stmt = connection.createStatement();
			ResultSet instructors = stmt.executeQuery(query);
			while (instructors.next()) {
				//System.out.println(instructors);
				String instructorFirstName = instructors.getString(1);
				String instructorLastName = instructors.getString(2);
				String instructorCredentials = instructors.getString(3);
				System.out.println("[" + numOfChoices + "] " + instructorFirstName + " " + instructorLastName + " " + instructorCredentials);
				String entry = instructorFirstName + " " + instructorLastName + " " + instructorCredentials;
				System.out.println(entry);
				selectableInstructors.add(entry);
				numOfChoices++;
			}
			connection.close();
		}
		catch (SQLException e) {
			System.out.println("Error in connecting to PostgreSQL server");
			e.printStackTrace();
		}
		//We now know what class of license the motorist is testing for
		System.out.println(selectableInstructors);
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
			System.out.println("Instructor Selected.");
		}
		int choice = bufferInt;
		String selection = selectableInstructors.get(choice - 1);
		String[] selectionArray = selection.split(" ", 0);
		System.out.println(selectionArray[0]);
		System.out.println("Below are all of this instructor's taken appointment times");
		//I'm going to do my query to show the user all that instructors scheduled appointments
		try {
			Connection connection = DriverManager.getConnection(jdbcURL, username, password);
			String query = "SELECT * FROM bookedAppointments WHERE instructor = '" + selectionArray[0] + "';";
			System.out.println(query);
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				String appointmentTime = rs.getString(1);
				Date appointmentDate = rs.getDate(2);
				String appointment = appointmentTime + " " + appointmentDate;
				System.out.println(appointment);
			}
			connection.close();
		}
		catch (SQLException e) {
			System.out.println("Error in connecting to PostgreSQL server");
			e.printStackTrace();
		}
		System.out.println("Rules for scheduling lessons and tests: ");
		System.out.println("Can only schedule one 2-hour test on Monday-Friday during the following times: ");
		System.out.println("9AM, 11AM, 1PM, 3PM");
		System.out.println("Please enter the date that you wish to take the test (format: YYYY-MM-DD)");
		buffer = scanner.nextLine();
		LocalDate testDate;
		try {
			testDate = LocalDate.parse(buffer);
		}
		catch(DateTimeParseException e) {
			System.out.println("Please enter a valid date");
			scheduleDrivingTest();
			return;
		}
		LocalDate currDate = LocalDate.now();
		long daysBetween = ChronoUnit.DAYS.between(currDate, testDate);
		if (daysBetween > 21) {
			System.out.println("Please enter a date that is only three weeks from today.");
			scheduleDrivingTest();
			return;
		}
		if (daysBetween < 1) {
			System.out.println("Please don't enter today's date or a date that has already passed.");
			scheduleDrivingTest();
			return;
		}
		DayOfWeek dayOfWeek = DayOfWeek.from(testDate);
		if(dayOfWeek.name() == "SATURDAY" || dayOfWeek.name() == "SUNDAY") {
			System.out.println("Please enter a date that doesn't occur on Saturday or Sunday.");
			scheduleDrivingTest();
			return;
		}
		System.out.println("Please enter the time in which you want to take the test on that day. (9, 11, 1, or 3)");
		buffer = scanner.nextLine();
		String selectedTime;
		if (buffer.contains("9")) {
			selectedTime = "9AM";
		}
		else if (buffer.contains("11")) {
			selectedTime = "11AM";
		}
		else if (buffer.contains("1")) {
			selectedTime = "1PM";
		}
		else if (buffer.contains("3")) {
			selectedTime = "3PM";
		}
		else {
			System.out.println("Please enter a valid time");
			scheduleDrivingTest();
			return;
		}
		try {
			Connection connection = DriverManager.getConnection(jdbcURL, username, password);
			String query = "SELECT * FROM bookedAppointments WHERE instructor = '" + testDate + "'  AND time = '" + selectedTime + "';";
			System.out.println(query);
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next()) {
				System.out.println("Please book a time and appointment at a time and date that are not taken.");
				scheduleDrivingTest();
				return;
			}
			connection.close();
		}
		catch (SQLException e) {
			System.out.println("Error in connecting to PostgreSQL server");
			e.printStackTrace();
		}
		try {
			Connection connection = DriverManager.getConnection(jdbcURL, username, password);
			String query = "INSERT INTO drivingtest (testID, class, time, date, instructor, motorist)"
			+ "VALUES (uuid_generate_v4(), '" + Character.toUpperCase(licenseClass) + "','" + selectedTime + "', '" + testDate + "', '" + 
			selectionArray[0] + "', '" + loggedUsername + "');";
			Statement stmt = connection.createStatement();
			int rows = stmt.executeUpdate(query);
			if (rows > 0) {
				System.out.println("Test Scheduled!");
			}
		}
		catch (SQLException e) {
			System.out.println("Error in connecting to PostgreSQL server");
			e.printStackTrace();
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
		if (bufferInt > 6 || bufferInt < 1) {
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
		if (bufferInt == 6) {
			System.out.println("[6] View your scheduled tests and lessons and tests you've previously taken.");
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
			connection.close();
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
		String jdbcURL = "jdbc:postgresql://localhost:5432/DMV";
		String username = "postgres";
		String password = "boobsandfeet";
		try {
			Connection connection = DriverManager.getConnection(jdbcURL, username, password);
			System.out.println("Connected to PostgreSQL server");
			connection.close();
		}
		catch (SQLException e) {
			System.out.println("Error in connecting to PostgreSQL server");
			e.printStackTrace();
		}
		scheduleDrivingTest();
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
