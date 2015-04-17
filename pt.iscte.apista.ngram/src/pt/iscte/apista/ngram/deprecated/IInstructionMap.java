package pt.iscte.apista.ngram.deprecated;

import java.util.Map;

import pt.iscte.apista.extractor.Analyzer;
import pt.iscte.apista.extractor.Instruction;

public interface IInstructionMap {
	
	Map<String, Map<Instruction, Integer>> buildInstructionMap(Analyzer analyzer);

}
