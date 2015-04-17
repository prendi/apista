package pt.iscte.apista.evaluationsystem.methods;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import pt.iscte.apista.core.Filter;
import pt.iscte.apista.core.SystemConfiguration;
import pt.iscte.apista.evaluationsystem.EvaluationData;

public class CumulativeContextPrecision extends ContextPrecision{

	
	public CumulativeContextPrecision(SystemConfiguration configuration,
			Filter[] filters) {
		super(configuration, filters);
	}

	@Override
	public List<EvaluationData> reportData() {
		
		File dataFile = new File(getFilename("CumulativeContextPrecision_")+ ".csv");
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(dataFile));
			bw.write("Context Size, Comulative Total Proposed, Comulative Total Not Proposed \n");
			int comulativeTotalProposed = 0, comulativeNotProposed = 0;
			
			for (Integer size : contextSizeData.keySet()) {
				
				bw.write("" + size);
				bw.write(",");
				comulativeTotalProposed += contextSizeData.get(size).getTotalProposed();
				bw.write("" + comulativeTotalProposed);
				bw.write(",");
				comulativeNotProposed += contextSizeData.get(size).getTotalNotProposed();
				bw.write("" + comulativeNotProposed);
				bw.write("\n");
			}
			
			bw.flush();
			bw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return null;
	}
	
}
