package pt.iscte.apista.evaluationsystem.methods;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import pt.iscte.apista.core.SystemConfiguration;
import pt.iscte.apista.evaluationsystem.EvaluationData;
import pt.iscte.apista.evaluationsystem.abstracts.ACrossEvaluationMethod;

public class CrossValidationTrainTestEvaluation extends ACrossEvaluationMethod {

	
	public CrossValidationTrainTestEvaluation(SystemConfiguration configuration) {
		super(configuration, TrainTestEvaluation.class);
	}

	@Override
	public List<EvaluationData> reportData() {
		int maxProposals = configuration.getMaxProposals();

		File dataFile = new File(getFilename() + ".csv");
		BufferedWriter bw;
		double[] indexes = new double[maxProposals];
		double[] indexPercentage = new double[maxProposals];
		double[] indexComulativePercentage = new double[maxProposals];
		double totalProposed = 0, totalNotProposed = 0, totalProposedPercentage = 0, totalNotProposedPercentage = 0;

		try {

			bw = new BufferedWriter(new FileWriter(dataFile));
			/*bw.write("sep=,"+ "\n");
			bw.write("Index, Average Number Of Proposals, Average Cover Percentage, Average Cumulative Percentage Cover \n");
			*/
			
			for (EvaluationData data : dataList) {
				
				for (int i = 0; i != maxProposals; i++) {

					indexes[i] += data.getValueFromIndex(i);
					indexPercentage[i] += data.getIndexCoverPercentage(i);
					indexComulativePercentage[i] += data
							.getIndexCumulativePercentage(i);

				}
				totalProposed += data.getTotalProposed();
				totalNotProposed += data.getTotalNotProposed();
				totalProposedPercentage += data.getTotalProposedPercentage();
				totalNotProposedPercentage += data
						.getTotalNotProposedPercentage();
			}

			for (int i = 0; i != maxProposals; i++) {
				bw.write("" + (i + 1));
//				bw.write(",");
				//bw.write("" + (indexes[i] / (double) dataList.size()));
				//bw.write(",");
				//bw.write("" + (indexPercentage[i] / (double) dataList.size()));
				//bw.write(",");
				bw.write(" "
						+ new DecimalFormat("#.00").format((indexComulativePercentage[i] / (double) dataList
								.size()) * 100));
				bw.write("\n");
			}
			/*bw.write("Average of Total Proposed, " + totalProposed
					/ (double) dataList.size() + " \n");
			bw.write("Average of Total Not Proposed, " + totalNotProposed
					/ (double) dataList.size() + "\n");
			bw.write("Average Percentage of Total Proposed, "
					+ totalProposedPercentage / (double) dataList.size() + "\n");
			bw.write("Average Percentage of Total Not Proposed, "
					+ totalNotProposedPercentage / (double) dataList.size()
					+ "\n");
			*/
			bw.flush();
			bw.close();

		} catch (IOException e1) {
			System.err
					.println("Problem creating report file on CrossValidation.class - reportData()");
		}

		/*File chartCoordinateFile = new File(getFilename() + ".txt");
		BufferedWriter bwChartCoordinates;
		try {

			bwChartCoordinates = new BufferedWriter(new FileWriter(chartCoordinateFile));
			
			for (int i = 0; i != maxProposals; i++) {
				bwChartCoordinates.write("(" + (i + 1));
				bwChartCoordinates.write(",");
				bwChartCoordinates.write(""
						+ (indexComulativePercentage[i] / (double) dataList
								.size()) + ")");
				bwChartCoordinates.write("\n");
			}
			
			
			bwChartCoordinates.flush();
			bwChartCoordinates.close();
		
		}catch (IOException e1) {
				System.err
						.println("Problem creating report file on CrossValidation.class - reportData()");
			}
		*/
		return null;
	}

}
