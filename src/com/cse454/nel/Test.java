package com.cse454.nel;

import java.util.Scanner;

public class Test {

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);

		do {
			System.out.println("hello natalie");
			System.out.print("play again");
		} while (scanner.next().equalsIgnoreCase("y"));
		System.out.println("bye");
	}

}
