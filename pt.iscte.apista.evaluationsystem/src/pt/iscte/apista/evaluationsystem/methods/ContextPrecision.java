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

public class ContextPrecision extends AEvaluationMethod {

	protected Map<Integer,EvaluationData> contextSizeData = new HashMap<Integer,EvaluationData>();
	
	public ContextPrecision(SystemConfiguration configuration, Filter[] filters) {
		super(configuration, filters);
	}

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
						
						evaluateInstruction(proposals, nextInstruction, context.size());
					}
				}
			}
		}

	}
	
	private void evaluateInstruction(List<Instruction> proposals,
			Instruction instruction, int contextSize) {
		int index = -1;
	
		EvaluationData data = null;
		
		if(contextSizeData.containsKey(contextSize)){
			data = contextSizeData.get(contextSize);
		}else{
			data = new EvaluationData(configuration.getMaxProposals());
			contextSizeData.put(contextSize, data);
		}
		
		for (int i = 0; i != proposals.size()
				&& i != configuration.getMaxProposals(); i++) {
			if (proposals.get(i).equals(instruction)) {
				index = i;
			}
		}
		if (index >= 0) {
				data.addProposedToIndex(index);
		} else {
				data.addTotalNotProposed();
		}
	}

	@Override
	public List<EvaluationData> reportData() {
		

		File dataFile = new File(getFilename("ContextPrecision_") + ".csv");
		BufferedWriter bw;
		
		try {
			bw = new BufferedWriter(new FileWriter(dataFile));
			bw.write("Context Size, Total Proposed, Total Not Proposed \n");
			for(Integer size : contextSizeData.keySet()){
				
				EvaluationData data = contextSizeData.get(size);
				bw.write("" + size);
				bw.write(",");
				bw.write("" + data.getTotalProposed());
				bw.write(",");
				bw.write("" + data.getTotalNotProposed());
				bw.write("\n");
			}
			
			bw.flush();
			bw.close();
			
		} catch (IOException e) {
			System.err.println("Problem writing to file on ContextPrecision");
			e.printStackTrace();
		}
		
		List<EvaluationData> list = new ArrayList<EvaluationData>();
		list.addAll(contextSizeData.values());
		return list;
	}

}
