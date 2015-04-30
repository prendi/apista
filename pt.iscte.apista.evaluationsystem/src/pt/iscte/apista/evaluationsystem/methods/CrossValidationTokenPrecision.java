package pt.iscte.apista.evaluationsystem.methods;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.iscte.apista.core.Instruction;
import pt.iscte.apista.core.SystemConfiguration;
import pt.iscte.apista.evaluationsystem.EvaluationData;
import pt.iscte.apista.evaluationsystem.abstracts.ACrossEvaluationMethod;

public class CrossValidationTokenPrecision extends ACrossEvaluationMethod{

	public CrossValidationTokenPrecision(SystemConfiguration configuration,
			int numberOfValidations) {
		super(configuration, numberOfValidations, TokenPrecision.class);
	}


	@Override
	public List<EvaluationData> reportData() {
		
		
		File dataFile = new File(getFilename() + ".csv");
		BufferedWriter bw;
		Map<Instruction, EvaluationData> dataMap = new HashMap<>();
		Map<Instruction, Integer> countMap = new HashMap<>();
		Map<Instruction, Integer> countIndexMap = new HashMap<>();
		try {

			bw = new BufferedWriter(new FileWriter(dataFile));
			bw.write("Token, Average Proposal Index, Total Times Proposed, Total Times not Proposed \n");
			
			for (EvaluationData data : dataList) {
				Instruction token = (Instruction) data.getObject();
				EvaluationData tokenData = null;
				
				if(countMap.containsKey(token)){
					int count = countMap.get(token);
					count++;
					countMap.put(token, count);
				}else{
					countMap.put(token, 1);
				}
				
				if(dataMap.containsKey(token)){
					tokenData = dataMap.get(token);
				}else{
					tokenData = new EvaluationData(configuration.getMaxProposals());
					dataMap.put(token, tokenData);
				}
				
				double averageIndex = data.computeAverageIndex();
				
				if(averageIndex != -1){
					tokenData.setAverageIndex(tokenData.getAverageIndex() + averageIndex);
					if(countIndexMap.containsKey(token)){
						int count = countIndexMap.get(token);
						count++;
						countIndexMap.put(token, count);
					}else{
						countIndexMap.put(token, 1);
					}
				}
				
				for (int i = 0; i < data.getIndexes().length; i++) {
					tokenData.getIndexes()[i] += data.getIndexes()[i];
				}
				
				tokenData.setTotalProposed(tokenData.getTotalProposed() + data.getTotalProposed());
				tokenData.setTotalNotProposed(tokenData.getTotalNotProposed() + data.getTotalNotProposed());
			}
			
			for (Instruction token : dataMap.keySet()) {
				
				bw.write(token.getWord() + ",");
				
				if(countIndexMap.containsKey(token)){
					bw.write("" + dataMap.get(token).getAverageIndex() / countIndexMap.get(token)+ ",");
				}else{
					bw.write("0" + ",");
				}
				
				bw.write("" + (double) dataMap.get(token).getTotalProposed() / countMap.get(token) + ",");
				bw.write("" + (double) dataMap.get(token).getTotalNotProposed() / countMap.get(token));
				
				bw.write("\n");
			}

			bw.flush();
			bw.close();

		} catch (IOException e1) {
			System.out
					.println("Problem creating report file on CrossValidation.class - reportData(String filename)");
		}
		return null;
	}

}
