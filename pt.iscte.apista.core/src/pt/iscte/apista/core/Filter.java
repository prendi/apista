package pt.iscte.apista.core;

import java.io.Serializable;

/**
 * Interface that defines the usage of filters on the system. These filters are used to separate the training set and the test set.
 */
public interface Filter {
	
	/**
	 * Determines if the progress provided is accepted by the filter
	 * @param progress of the analysis
	 * @return returns true if the progress is within the boundaries, false otherwise.
	 */
	boolean accept(double progress);

	/**
	 * Returns an array with the inverse filters of the current filter. Used to calculate the inverse of the test filters, i.e the training filters
	 * @return Array containing one or two elements depending on the test filters that were set.
	 */
	public Filter[] getInverseFilters();

	/**
	 * Implementation of the Filter interface.
	 */
	@SuppressWarnings("serial")
	public static class Range implements Filter, Serializable {
		/**
		 * Minimum progress value accepted
		 */
		private final double min;
		/**
		 * Maximum progress value accepted
		 */
		private final double max;

		/**
		 * Default Constructor. Assigns the value of min and max to the attributes
		 * @param min minimum progress value to assign.
		 * @param max maximum progress value to assign.
		 */
		public Range(double min, double max) {
			this.min = min;
			this.max = max;
		}

		@Override
		public Filter[] getInverseFilters() {
			//Temporary array with the inverse filters to be returned
			Filter[] ranges;
			//If the minimum accepted value is 0, there will be only one filter which goes from the maximum accepted value to 1.
			if (min == 0) {
				ranges = new Filter[1];
				ranges[0] = new Range(max, 1);
			}//Like the above case, if the maximum accepted value is 1, there will be only one filter which goes from 0 to the minimum
			//accepted value
			else if (max == 1) {
				ranges = new Filter[1];
				ranges[0] = new Range(0, min);
			}//In this case, the Range is in the middle, so there are two inverse filters, the lower range and the higher range 
			else {
				ranges = new Filter[2];
				ranges[0] = new Range(0, min);
				ranges[1] = new Range(max, 1);
			}

			return ranges;
		}

		@Override
		public boolean accept(double progress) {
			return progress >= min && progress <= max;
		}
		
		@Override
		public String toString() {

			return "F_" + min + "-" + max;
		}
	}
}