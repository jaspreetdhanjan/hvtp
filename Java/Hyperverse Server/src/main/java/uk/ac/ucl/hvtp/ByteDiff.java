package uk.ac.ucl.hvtp;

import java.util.Arrays;

public class ByteDiff {
	public static void main(String[] args) {
		byte[] a = {1, 0, 0, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1};
		byte[] b = {1, 0, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1};

		// 01101010001
		// 01100101010
		// XOR
		// 00001111010

		printDiff(a, b);
	}

	public static void printDiff(byte[] a, byte[] b) {
/*		int max = Math.max(a.length, b.length);

		int i = 0;

		while (i < max) {


			i++;
		}*/


		System.out.println("A:\t" + Arrays.toString(a));
		System.out.println("B:\t" + Arrays.toString(b));
		System.out.println("DF:\t" + Arrays.toString(flaggedBits(a, b)));
	}

	private static byte[] flaggedBits(byte[] a, byte[] b) {
		byte[] flagged = new byte[a.length];

		for (int i = 0; i < a.length; i++) {
			flagged[i] = (byte) (a[i] ^ b[i]);
		}

		return flagged;
	}
}