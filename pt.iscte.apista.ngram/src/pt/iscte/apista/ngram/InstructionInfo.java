package pt.iscte.apista.ngram;

import java.io.Serializable;

@SuppressWarnings("serial")
public class InstructionInfo implements Serializable {
	
	private double relativeFrequency;
	private int frequency;
	
	public InstructionInfo(int frequency){
		this.frequency = frequency;
		relativeFrequency = 0;
	}
	
	public InstructionInfo(int frequency, double relativeFrequency){
		this.frequency = frequency;
		this.relativeFrequency = relativeFrequency;
		
	}
	
	public InstructionInfo(double relativeFrequency) {
		this.relativeFrequency = relativeFrequency;
	}
	
	public int getFrequency() {
		return frequency;
	}
	
	
	public void addFrequency(){
		frequency++;
	}
	
	
	public double getRelativeFrequency() {
		return relativeFrequency;
	}
	
	
	public void setRelativeFrequency(double relativeFrequency) {
		this.relativeFrequency = relativeFrequency;
	}
	
	@Override
	public String toString() {
		return frequency + " (" + relativeFrequency + ")"; 
	}

	
}
