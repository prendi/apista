package pt.iscte.apista.evaluationsystem;

import java.io.File;

/**
 * 
 * Class used to gather results from the evaluation
 *
 */
public class EvaluationData {
	
		/**
		 * Number of max proposals evaluated
		 */
		private int maxProposals;
		
		/**
		 * Total number of instructions proposed and not proposed
		 */
		private int totalProposed = 0, totalNotProposed = 0;
		
		/**
		 * Distribution of proposals among the different ranks
		 */
		private int[] indexes;
		
		/**
		 * Average hit index
		 */
		private double averageIndex= 0;
		
		/**
		 * Optional object related to this evaluation
		 */
		private Object object;

		public EvaluationData(int maxProposals) {
			this.maxProposals = maxProposals;
			indexes = new int[maxProposals];
		}

		public void setObject(Object object) {
			this.object = object;
		}

		public Object getObject() {
			return object;
		}

		public void addTotalNotProposed() {
			totalNotProposed++;
		}
		
		public void setTotalNotProposed(int totalNotProposed) {
			this.totalNotProposed = totalNotProposed;
		}
		
		public void setTotalProposed(int totalProposed) {
			this.totalProposed = totalProposed;
		}
		
		public void addProposedToIndex(int index) {
			indexes[index]++;
			totalProposed++;
		}

		public int getTotalNotProposed() {
			return totalNotProposed;
		}

		public int getTotalProposed() {
			return totalProposed;
		}

		public int getMaxProposals() {
			return maxProposals;
		}

		public int getValueFromIndex(int index) {
			return indexes[index];

		}

		public int[] getIndexes() {
			return indexes;
		}

		public void setAverageIndex(double averageIndex) {
			this.averageIndex = averageIndex;
		}
		
		public double getAverageIndex() {
			return averageIndex;
		}

		/**
		 * 
		 * @return returns average hit index
		 */
		public double computeAverageIndex() {
			if(totalProposed == 0 )
				return -1;
				
			double index = 0;
			for (int i = 0; i < indexes.length; i++) {
				index += ((double) indexes[i] / (double) totalProposed)
						* (i + 1);
			}
			return index;
		}

		/**
		 * 
		 * @param index to be calculated
		 * @return returns the index individual percentage
		 */
		
		public double getIndexCoverPercentage(int index) {
			return (double) indexes[index]
					/ (double) (totalProposed + totalNotProposed);
		}

		/**
		 * 
		 * @param index to be calculated
		 * @return returns the cumulative cover percentage for that index
		 */
		
		public double getIndexCumulativePercentage(int index) {
			double cumulativePercentage = 0;
			for (int i = 0; i != index + 1; i++) {
				cumulativePercentage += getIndexCoverPercentage(i);
			}
			return cumulativePercentage;
		}

		/**
		 * 
		 * @return Cover percentage for the total number of instructions proposed
		 */
		public double getTotalProposedPercentage() {
			return (double) totalProposed
					/ (double) (totalProposed + totalNotProposed);

		}

		/**
		 * 
		 * @return Cover percentage for the total number of instructions that were not proposed
		 */
		public double getTotalNotProposedPercentage() {
			return (double) totalNotProposed
					/ (double) (totalProposed + totalNotProposed);

		}

		/**
		 * Exports this evaluation data to csv format
		 * @param f
		 * @param scheme
		 */
		public void toCSV(File f, CSVScheme scheme) {
			//TODO
			throw new UnsupportedOperationException();
		}
	
}
