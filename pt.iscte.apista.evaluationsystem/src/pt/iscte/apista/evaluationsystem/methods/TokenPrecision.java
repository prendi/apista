package pt.iscte.apista.evaluationsystem.methods;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.iscte.apista.core.Filter;
import pt.iscte.apista.core.Instruction;
import pt.iscte.apista.core.Sentence;
import pt.iscte.apista.core.SystemConfiguration;
import pt.iscte.apista.evaluationsystem.EvaluationData;
import pt.iscte.apista.evaluationsystem.abstracts.AEvaluationMethod;

public class TokenPrecision extends AEvaluationMethod{

	public TokenPrecision(SystemConfiguration configuration, Filter[] filters) {
		super(configuration, filters);
	}

	private Map<Instruction, EvaluationData> tokenDataMap = new HashMap<>();
	
	@Override
	public void evaluate() {
		for (Sentence sentence : configuration.getAnalyzer().getSentences()) {

			List<Instruction> context = new ArrayList<Instruction>();
			List<Instruction> proposals;

			if (sentence.getInstructions().size() > 1) {

				for (int i = 0; i != sentence.getInstructions().size(); i++) {

					context.add(sentence.getInstructions().get(i));
					proposals = configuration.getModel().query(context,configuration.getMaxProposals());

					if (i + 1 < sentence.getInstructions().size()) {
						Instruction nextInstruction = sentence
								.getInstructions().get(i + 1);
						
						evaluateInstruction(proposals, nextInstruction);
					}
				}
			}
		}
	}
	
	private void evaluateInstruction(List<Instruction> proposals,
			Instruction instruction) {
		int index = -1;
	
		EvaluationData tokenData = null;
		
		if(tokenDataMap.containsKey(instruction)){
			tokenData = tokenDataMap.get(instruction);
		}else{
			tokenData = new EvaluationData(configuration.getMaxProposals());
			tokenData.setObject(instruction);
			tokenDataMap.put(instruction, tokenData);
		}
		
		for (int i = 0; i != proposals.size()
				&& i != configuration.getMaxProposals(); i++) {
			if (proposals.get(i).equals(instruction)) {
				index = i;
			}
		}
		if (index >= 0) {
				tokenData.addProposedToIndex(index);
		} else {
				tokenData.addTotalNotProposed();
		}
	}
	

	@Override
	public List<EvaluationData> reportData() {

		File dataFile = new File(getFilename("TokenPrecision_") + ".csv");
		BufferedWriter bw;
		
		try {
			bw = new BufferedWriter(new FileWriter(dataFile));
			bw.write("Token, Average Proposal Index, Total Times Proposed, Percentage Proposed, Total Times not Proposed, Percentage not Proposed \n");//,Proposed Coverage \n");
			int totalTokensProposed = 0 , totalTokensNotProposed = 0;
			for(EvaluationData data : tokenDataMap.values()){
				totalTokensProposed += data.getTotalProposed();
				totalTokensNotProposed += data.getTotalNotProposed();
			}
			
			for(Instruction instruction : tokenDataMap.keySet()){
				
				bw.write(instruction.getWord() + ",");
				bw.write(tokenDataMap.get(instruction).computeAverageIndex() + ",");
				
				int totalTimesProposed = tokenDataMap.get(instruction).getTotalProposed();
				bw.write(totalTimesProposed + ",");
				bw.write(((double) totalTimesProposed / (double)totalTokensProposed ) + ",");
				
				int totalTimesNotProposed = tokenDataMap.get(instruction).getTotalNotProposed();
				bw.write(totalTimesNotProposed + ",");
				bw.write(((double) totalTimesNotProposed / (double)totalTokensNotProposed ) + "\n");
			}
			
			bw.flush();
			bw.close();
			
		} catch (IOException e) {
			System.err.println("Error reporting data on class TokenPrecision");
			e.printStackTrace();
		}
		
		List<EvaluationData> list = new ArrayList<EvaluationData>();
		list.addAll(tokenDataMap.values());
		return list;
		
	}

}
