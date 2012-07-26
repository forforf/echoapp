package org.younghawk.echoapp;

public class Stat
{

	static double calcStanDev(int n, int[] s) {
		return Math.pow(calcVariance(n, s), 0.5);
	}


	static double calcVariance(int n, int[] s) {
		double total = 0;
		double sTotal = 0;
		double scalar = 1/(double)(n-1);
		for (int i = 0; i < n; i++) {
			total += s[i];
			sTotal += Math.pow(s[i], 2);
		}
		return (scalar*(sTotal - (Math.pow(total, 2)/n)));
	}
}
