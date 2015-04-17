package pt.iscte.apista.evaluationsystem.methods;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pt.iscte.apista.core.Filter;
import pt.iscte.apista.core.Instruction;
import pt.iscte.apista.core.Sentence;
import pt.iscte.apista.core.SystemConfiguration;
import pt.iscte.apista.evaluationsystem.EvaluationData;
import pt.iscte.apista.evaluationsystem.abstracts.AEvaluationMethod;

public class CrossEntropy extends AEvaluationMethod {


	public CrossEntropy(SystemConfiguration configuration, Filter[] filters) {
		super(configuration, filters);
	}

	@Override
	public void evaluate() {
		int n = 0;
		double entropy = 0;
		for (Sentence sentence : configuration.getAnalyzer().getSentences()) {

			List<Instruction> context = new ArrayList<Instruction>();

			if (sentence.getInstructions().size() > 1) {
				for (int i = 0; i != sentence.getInstructions().size(); i++) {
					context.add(sentence.getInstructions().get(i));
					if (i + 1 < sentence.getInstructions().size()) {
						Instruction nextInstruction = sentence
								.getInstructions().get(i + 1);
						entropy += Math.log10(configuration.getModel().probability(nextInstruction, context));
						n++;
					}
				}

			}
		}
		entropy = - (entropy / n);
		data.setObject(entropy);
	}

	@Override
	public List<EvaluationData> reportData() {
		File dataFile = new File(getFilename("CrossEntropy_") + ".txt");
		BufferedWriter bw;
		
		try {
			bw = new BufferedWriter(new FileWriter(dataFile));
			bw.write(""+(Double)data.getObject());
			System.out.println(""+(Double)data.getObject());
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return getListFromEvaluationData(data);
	}

}
