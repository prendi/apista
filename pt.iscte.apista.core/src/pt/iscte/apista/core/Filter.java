package pt.iscte.apista.core;

import java.io.Serializable;


public interface Filter {
	boolean accept(double progress);

	public Filter[] getInverseFilters();

	@SuppressWarnings("serial")
	public static class Range implements Filter, Serializable {
		private final double min;
		private final double max;

		public Range(double min, double max) {
			this.min = min;
			this.max = max;
		}

		@Override
		public Filter[] getInverseFilters() {

			Filter[] ranges;
			if (min == 0) {
				ranges = new Filter[1];
				ranges[0] = new Range(max, 1);

			} else if (max == 1) {
				ranges = new Filter[1];
				ranges[0] = new Range(0, min);
			} else {
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