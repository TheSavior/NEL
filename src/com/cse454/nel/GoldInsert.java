package com.cse454.nel;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.HashSet;

public class GoldInsert {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws NumberFormatException 
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws NumberFormatException, IOException, SQLException {
		DocumentConnect docs = new DocumentConnect();
		
		FileInputStream fstream = new FileInputStream("manual_gold.txt");
		// Get the object of DataInputStream
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		//Read File Line By Line
		while ((strLine = br.readLine()) != null) {
			// Print the content on the console
			strLine = strLine.trim();
			String[] parts = strLine.split("\t");

			docs.SetGoldData(Integer.parseInt(parts[0]), parts[1]);
		}
		in.close();
	}
}
