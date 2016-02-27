package pt.iscte.apista.evaluationsystem.methods;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import pt.iscte.apista.core.Instruction;
import pt.iscte.apista.core.SystemConfiguration;
import pt.iscte.apista.evaluationsystem.EvaluationData;
import pt.iscte.apista.evaluationsystem.abstracts.ACrossEvaluationMethod;

public class CrossValidationTokenPrecisionMedian extends ACrossEvaluationMethod {

	public CrossValidationTokenPrecisionMedian(SystemConfiguration configuration) {
		super(configuration, TokenPrecision.class);
	}

	static EvaluationData union(int max, Object object, Collection<EvaluationData> data) {
		EvaluationData union = new EvaluationData(max);
		union.setObject(object);
		int totalProposed = 0;
		int totalNotProposed = 0;
		int[] unionIndexes = union.getIndexes();
		for (EvaluationData set : data) {
			int[] indexes = set.getIndexes();
			for(int i = 0; i < unionIndexes.length; i++) {
				unionIndexes[i] += indexes[i];
			}
			totalProposed += set.getTotalProposed();
			totalNotProposed += set.getTotalNotProposed();
		}
		
		union.setTotalProposed(totalProposed);
		union.setTotalNotProposed(totalNotProposed);
		return union;
	}
	
	 static Map<Instruction, EvaluationData> dividePerToken(List<EvaluationData> data, int max) {
		Multimap<Instruction, EvaluationData> multimap = ArrayListMultimap.create();
		
		for(EvaluationData d : data)
			multimap.put((Instruction) d.getObject(), d);
		
		Map<Instruction, EvaluationData> map = new HashMap<>();
		for(Instruction token : multimap.keySet())
			map.put(token, union(max, token, multimap.get(token)));
		
		return map;
	}
	
	static DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
	 
	@Override
	public List<EvaluationData> reportData() {
		String fileName = configuration.getOutputFolder() + "CrossValidation_HitRank.txt";
		
		Map<Instruction, EvaluationData> tokenAggrData = dividePerToken(dataList, configuration.getMaxProposals());

		Map<Instruction, Quartiles> quartilesMap = new HashMap<>();
		for(Entry<Instruction, EvaluationData> e : tokenAggrData.entrySet())
			quartilesMap.put(e.getKey(), new Quartiles(e.getValue()));
		
		Quartiles medQuartiles = Quartiles.medianOf(quartilesMap.values());
		
		try {
			// file append
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));

			out.println("% " + 
					dateFormat.format(new Date()) + " " + 
					configuration.getLibRootPackage() + " " + 
					configuration.getModelParameters().getIntValue("n") + "-gram");
			
			out.println(
				medQuartiles.getPercentile(10) + "\t" +
				medQuartiles.getQ1() + "\t" +
				medQuartiles.getMedian() + "\t" + 
				medQuartiles.getQ3() + "\t" +
				medQuartiles.getPercentile(90));
			
			out.close();
		} catch (IOException e1) {
			System.out.println("Problem creating report file on " + fileName);
		}
		return null;
	}

}
