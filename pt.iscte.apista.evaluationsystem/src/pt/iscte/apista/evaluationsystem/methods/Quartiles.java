package pt.iscte.apista.evaluationsystem.methods;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;

import pt.iscte.apista.core.SystemConfiguration;
import pt.iscte.apista.evaluationsystem.EvaluationData;

public class Quartiles {

	private static final int N_PERCENTILES = 20;
	
	private double q1;
	private double q2;
	private double q3;
	private double[] percentiles;
	
	public Quartiles(EvaluationData data) {
		setQuartiles(data);
	}

	public Quartiles(double q1, double q2, double q3, double[] percentiles) {
		this.q1 = q1;
		this.q2 = q2;
		this.q3 = q3;
		this.percentiles = percentiles;
	}

	public static Quartiles medianOf(Collection<Quartiles> set) {
		int n = set.size();
		double[] q1list = new double[n];
		double[] q2list = new double[n];
		double[] q3list = new double[n];
		double[][] percentiles = new double[N_PERCENTILES][n];
		
		int i = 0;
		for(Quartiles q : set) {
			q1list[i] = q.getQ1();
			q2list[i] = q.getMedian();
			q3list[i] = q.getQ3();
			for(int j = 0; j < N_PERCENTILES; j++)
				percentiles[j][i] = q.getPercentiles()[j];
			i++;
		}

		Arrays.sort(q1list);
		Arrays.sort(q2list);
		Arrays.sort(q3list);
		
		for(double[] perc : percentiles)
			Arrays.sort(perc);
		
		double[] percMedian = new double[N_PERCENTILES];
		for(int j = 0; j < N_PERCENTILES; j++)
			percMedian[j] = median(percentiles[j]);
		
		return new Quartiles(
				median(q1list), 
				median(q2list), 
				median(q3list),
				percMedian);
				
	}

	public double getQ1() {
		return q1;
	}

	public double getMedian() {
		return q2;
	}

	public double getQ3() {
		return q3;
	}

	public double[] getPercentiles() {
		return percentiles;
	}
	
	public double getPercentile(int n) {
		if(n < 0 || n % 5 != 0 || n > 100)
			throw new IllegalArgumentException("n must be within [0, 100] and multiple of 5");
		
		return percentiles[n / 5];
	}

	private void setQuartiles(EvaluationData data) {
		int[] indexesPlusOne = Arrays.copyOf(data.getIndexes(), data.getIndexes().length + 1);
		indexesPlusOne[indexesPlusOne.length-1] = data.getTotalNotProposed();
		double[] unroll = unroll(indexesPlusOne);
		for (int i = 0 ; i != unroll.length; i++)
			System.out.println(unroll[i]);
		q1 = q1(unroll);
		q2 = median(unroll);
		q3 = q3(unroll);
		percentiles = new double[N_PERCENTILES];
		double p = 0.0;
		for(int i = 0; i < percentiles.length; i++) {
			percentiles[i] = percentile(unroll, p);
			p += 0.05;
		}
	}

	// convert counts to a flat array
	// output is always sorted
	private static double[] unroll(int[] counts) {
		double[] unroll = new double[sum(counts)];
		int x = 0;
		for(int i = 0; i < counts.length; i++) {
			for(int j = 0; j < counts[i]; j++)
				unroll[x++] = i + 1; // adds 1 to all because 0-index has the counts of first in ranking
		}
		return unroll;
	}

	//	private static double[] quartiles(List<EvaluationData> data) {
	//		int[] mergedIndexes = new int[data.get(0).getMaxProposals() + 1];
	//		for (EvaluationData set : data) {
	//			int[] indexes = set.getIndexes();
	//			for(int i = 0; i < mergedIndexes.length; i++) {
	//				mergedIndexes[i] += indexes[i];
	//			}
	//			// last vector position contains the total count of not proposed
	//			mergedIndexes[mergedIndexes.length-1] += set.getTotalNotProposed();
	//		}
	//
	//		int[] unroll = unroll(mergedIndexes);
	//
	//		return new double[] {q1(unroll),median(unroll),q3(unroll)};
	//	}



	private static double q1(double[] v) {
		return median(0, (v.length / 2) - 1, v);
	}

	private static double median(double[] v) {
		return median(0, v.length-1, v);	
	}

	private static double q3(double[] v) {
		int i = v.length % 2 == 0 ? v.length / 2 : (v.length / 2) + 1;
		return median(i, v.length-1, v);
	}

	private static double median(int i, int j, double[] v) {
		int len = j - i + 1;
		int h  = i + len/2;
		return len % 2 == 0 ? (v[h] + v[h-1]) / 2.0 : v[h];
	}
	
	private static double percentile(double[] v, double p) {
		return v[(int) Math.round(v.length*p)];
	}

	private static int sum(int[] v) {
		int total = 0;
		for(int i : v)
			total += i;
		return total;
	}

	public static void main(String[] args) {
		SystemConfiguration configuration = new SystemConfiguration("../pt.iscte.apista.resources/configJackson.properties");
		CrossValidationTokenPrecisionMedian cvtpm = new CrossValidationTokenPrecisionMedian(configuration);
		cvtpm.evaluate();
		cvtpm.reportData();
		
		/*
		int[] counts = {2,3,2,4};
		System.out.println(Arrays.toString(unroll(counts)));
		double[] v1 = {1,2,2,4,5, 6,6,8,9,9};
		System.out.println(q1(v1) + "\t" + median(v1) + "\t" + q3(v1));

		double[] v1b = {1,2,2,3,4,5, 6,6,8,9,9,9};
		System.out.println(q1(v1b) + "\t" + median(v1b) + "\t" + q3(v1b));

		double[] v2 = {1,2,3,4,5, 6, 7,8,9,10,11};
		System.out.println(q1(v2) + "\t" + median(v2) + "\t" + q3(v2));	

		double[] v2b = {1,2,3,3,4,5, 6, 7,8,9,10,10,11};
		System.out.println(q1(v2b) + "\t" + median(v2b) + "\t" + q3(v2b));
		*/
		double[] v3b = {2, 2, 2, 2, 1};
		System.out.println(q1(v3b) + "\t" + median(v3b) + "\t" + q3(v3b));
	}
}
