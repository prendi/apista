package pt.iscte.apista.ngram;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import pt.iscte.apista.core.Instruction;

@SuppressWarnings("serial")
public class InstructionWrapper implements Serializable, Comparable<InstructionWrapper>{

	private double rFrequency;
	private Instruction instruction;
	
	public InstructionWrapper(Instruction instruction,
			double rFrequency) {
		this.instruction = instruction;
		this.rFrequency = rFrequency;
	}

	public Instruction getInstruction() {
		return instruction;
	}

	public double getrFrequency() {
		return rFrequency;
	}
	
	public void setrFrequency(double rFrequency) {
		this.rFrequency = rFrequency;
	}

	@Override
	public int compareTo(InstructionWrapper o) {
		return rFrequency == o.getrFrequency() ? 
				0 : rFrequency > o.getrFrequency() ? -1 : 1;
	}
	
	@Override
	public String toString() {
		return instruction.getWord() + " - " + rFrequency;
	}

	public static List<Instruction> convertFromWrapperToList(List<InstructionWrapper> list) {
		List<Instruction> returnList = new ArrayList<>();

		for (InstructionWrapper instruction : list)
			returnList.add(instruction.getInstruction());

		return returnList;
	}
}