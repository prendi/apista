package pt.iscte.apista.evaluationsystem.methods;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import pt.iscte.apista.core.Filter;
import pt.iscte.apista.core.Instruction;
import pt.iscte.apista.core.Sentence;
import pt.iscte.apista.core.SystemConfiguration;
import pt.iscte.apista.evaluationsystem.EvaluationData;
import pt.iscte.apista.evaluationsystem.abstracts.AEvaluationMethod;

public class TrainTestEvaluation extends AEvaluationMethod {


	public TrainTestEvaluation(SystemConfiguration configuration, Filter...filters) {
		super(configuration,filters);
	}

	private String getFiltersDescription(){
		String s = "";
		for (Filter f : filters) {
			s += f.toString();
		}
		return s;
	}
	
	@Override
	public void evaluate() {
		
		File dataFile = new File("InstructionsNotFound_" + getFiltersDescription() + ".csv");
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(dataFile));
			bw.write("Instruction Not Found, Sentence");

			for (Sentence sentence : configuration.getAnalyzer().getSentences()) {

				List<Instruction> context = new ArrayList<Instruction>();
				List<Instruction> proposals;

				if (sentence.getInstructions().size() > 1) {

					for (int i = 0; i != sentence.getInstructions().size(); i++) {
						context.add(sentence.getInstructions().get(i));
						proposals = configuration.getModel().query(context,
								configuration.getMaxProposals());

						if (i + 1 < sentence.getInstructions().size()) {
							Instruction nextInstruction = sentence
									.getInstructions().get(i + 1);
							
							if (!evaluateInstruction(proposals, nextInstruction)) {
								bw.write(nextInstruction.toString());
								bw.write(",");
								bw.write(sentence.toString());
								bw.write("\n");
							}
						}
					}
				}
			}
		} catch (IOException e) {
			System.err.println("Error evaluating on class TrainTestEvaluation");
			e.printStackTrace();
		}

	}

	private boolean evaluateInstruction(List<Instruction> proposals,
			Instruction instruction) {
		int index = proposals.indexOf(instruction);
//		
//		for (int i = 0; i != proposals.size()
//				&& i != data.getMaxProposals(); i++) {
//			if (proposals.get(i).equals(instruction)) {
//				index = i;
//			}
//
//		}
		
		if (index >= 0 && index <= configuration.getMaxProposals()) {
			data.addProposedToIndex(index);
			return true;
		} else {
			data.addTotalNotProposed();
			return false;
		}
	}

	@Override
	public List<EvaluationData> reportData() {
		
		DecimalFormat df = new DecimalFormat("##.##");
		File dataFile = new File(getFilename("TrainTestEvaluation_") + ".csv");
		BufferedWriter bw;
		double comulativePercentage = 0;
		
		try {
			bw = new BufferedWriter(new FileWriter(dataFile));
			bw.write("sep=,"+ "\n");
			bw.write("Index, Number Of Proposals, Cover Percentage, Cumulative Percentage \n");

			for (int i = 0; i != data.getMaxProposals(); i++) {
				bw.write("" + (i + 1));
				bw.write(",");
				bw.write("" + data.getValueFromIndex(i));

				double percentage = (double) data
						.getValueFromIndex(i)
						/ (double) (data.getTotalProposed() + data
								.getTotalNotProposed());
				bw.write(",");
				bw.write("" + percentage);
				comulativePercentage += percentage;
				bw.write(",");
				bw.write("" + comulativePercentage);
				bw.write("\n");

				System.out.println("N: " + (i + 1) + " NUMBER: "
						+ data.getValueFromIndex(i) + " COVERS: "
						+ df.format(percentage * 100) + " COMULATIVE: "
						+ df.format(comulativePercentage * 100));
			}

			double percentage = (double) data.getTotalProposed()
					/ (double) (data.getTotalProposed() + data
							.getTotalNotProposed());
			bw.write("Total Proposed, " + data.getTotalProposed()
					+ "\n");
			System.out.println("Total Proposed, "
					+ data.getTotalProposed() + " COVERS: "
					+ df.format(percentage * 100));

			percentage = (double) data.getTotalNotProposed()
					/ (double) (data.getTotalProposed() + data
							.getTotalNotProposed());
			bw.write("Total Not Proposed, "
					+ data.getTotalNotProposed());
			System.out.println("Total Not Proposed, "
					+ data.getTotalNotProposed() + " COVERS: "
					+ df.format(percentage * 100));
			System.out.println("TOTAL: "
					+ (data.getTotalProposed() + data
							.getTotalNotProposed()));

			bw.flush();
			bw.close();

		} catch (IOException e1) {
			System.out
					.println("Problem creating report file on TrainTestEvaluationMethod.class - reportData(String filename)");
		}
		
		return getListFromEvaluationData(data);
	}

}
