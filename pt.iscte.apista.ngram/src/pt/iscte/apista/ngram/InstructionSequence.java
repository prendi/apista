package pt.iscte.apista.ngram;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IType;

import pt.iscte.apista.core.ITypeCache;
import pt.iscte.apista.core.Instruction;

public class InstructionSequence extends Instruction {

	private List<Instruction> instructionSequence;
	
	public InstructionSequence() {
		super("");
		instructionSequence = new ArrayList<Instruction>();
	}
	public InstructionSequence(Instruction instruction){
		this();
		instructionSequence.add(instruction);
	}
	
	public InstructionSequence(List<Instruction> instructions, int min, int max) {
		this();
		for (int i = min; i <= max; i++)
			instructionSequence.add(instructions.get(i));
	}

	@Override
	public String getWord() {
		String word = "";
	
		for (Instruction i : instructionSequence) {
			if(!word.isEmpty())
				word += "/";
			
			word += i.getWord();
		}
		return word;
	}

	
	public boolean startsWith(List<Instruction> list , int min, int max){
		
		for (int i = 0; i != instructionSequence.size() && i != max - min + 1 ; i++) {
			
			if(!instructionSequence.get(i).equals(list.get(min + i))){
				return false;
			}
		}
		
		return true;
	}
	
	public Instruction getLast(){
		return instructionSequence.get(instructionSequence.size()-1);
		
	}
	
	public List<Instruction> getInstructionSequence() {
		return Collections.unmodifiableList(instructionSequence);
	}
	
	public void add(Instruction i) {
		instructionSequence.add(i);
	}

	@Override
	public String resolveInstruction(IType type, Map<String, IType> vars,
			ITypeCache typeCache) {
		throw new UnsupportedOperationException();
	}


}
