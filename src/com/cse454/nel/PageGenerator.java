package com.cse454.nel;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Scanner;

import com.cse454.nel.mysql.WikiConnect;

public class PageGenerator {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception  {
		WikiConnect wiki = new WikiConnect();
		Scanner scanner = new Scanner(System.in);
		
		while(true) {
			System.out.print("Page Title: ");
			String page = scanner.nextLine();
			if (wiki.doesWikiPageExist(page)) {
				System.out.print("Page already exists. Overwrite?");
				
				if (!continueHelper(scanner)) {
					break;
				}
				// overwrite
			}
			
			FileInputStream fstream = new FileInputStream("doc.txt");
			System.out.println("Loading text: ");
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			StringBuilder strLine = new StringBuilder();
			//Read File Line By Line
			String line;
			while ((line = br.readLine()) != null) {
				strLine.append(line+"\n");
			}
			in.close();
			
			System.out.println(strLine.substring(0, 200));
			
			System.out.print("Correct?");
			if (!continueHelper(scanner)) {
				break;
			}
			
			wiki.AddPage(page, strLine.toString());
			System.out.println();
		}
	}
	
	private static boolean continueHelper(Scanner scanner) {
		String overwrite = "a";
		while(!overwrite.startsWith("n") && !overwrite.startsWith("y")) {
			System.out.print("(Y/N): ");
			overwrite = scanner.nextLine().toLowerCase();
		}
		
		return !overwrite.startsWith("n");
	}

}
