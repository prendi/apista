package pt.iscte.apista.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Sentence implements Iterable<Instruction>, Serializable {

	private final String filePath;
	private final int blockLine;
	private final List<Instruction> list;

	public Sentence(String filePath, int blockLine) {
		this.filePath = filePath;
		this.blockLine = blockLine;
		list = new ArrayList<>();
	}

	
	public void addInstruction(Instruction instruction) {
		list.add(instruction);
	}

	public List<Instruction> getInstructions(){
		return Collections.unmodifiableList(list);
	}

	public String getFilePath() {
		return filePath;	
	}
	
	public int getBlockLine() {
		return blockLine;
	}
	
	public String toString(){
		return Arrays.toString(list.toArray());
	}
	
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public Iterator<Instruction> iterator() {
		return Collections.unmodifiableList(list).iterator();
	}
	
//	public Sentence copy() {
//		List<Instruction> copy = new ArrayList<>(list.size());
//		Collections.copy(copy, list);
//		return new Sentence(copy);
//	}
	
	public Instruction getLastInstruction(){
		return list.isEmpty() ? null : list.get(list.size()-1);
	}

	public boolean containsInstruction(String word) {
		for(Instruction i : list)
			if(i.getWord().equals(word))
				return true;
		
		return false;
	}
	
	public boolean equalTo(Sentence s) {
		if(list.size() != s.list.size())
			return false;
		
		for(int i = 0; i < list.size(); i++)
			if(!list.get(i).equals(s.list.get(i)))
				return false;
		
		return true;
	}
	
}
