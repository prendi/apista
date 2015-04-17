package pt.iscte.apista.ngram;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import pt.iscte.apista.core.Instruction;

import com.google.common.collect.ForwardingList;

public class NGramSentence extends ForwardingList<Instruction> {
	private final int n;
	private final List<Instruction> list;
	private final UnigramTable unigrams;
	
	public NGramSentence(int n, List<Instruction> list, UnigramTable unigrams) {
		this.n = n;
		this.list = list;
		this.unigrams = unigrams;
	}
	
	
	@Override
	public int size() {
		return list.size() + n;
	}

	public Instruction get(int index) {
		if(index < n - 1)
			return Instruction.START;
		else if(index ==  size() - 1)
			return Instruction.END;
		else {
			Instruction i = list.get(index-(n-1));
			if(unigrams.isRare(i))
				return Instruction.UNK;
			else
				return i;
		}
	}

	@Override
	public Iterator<Instruction> iterator() {
		throw new UnsupportedOperationException();
	}

	public List<Instruction> tail() {
		List<Instruction> tail = new ArrayList<Instruction>(n-1);
		for(int i = size() - n; i < size() - 1; i++)
			tail.add(get(i));
		
		assert tail.size() == n-1;
		return tail;
	}
	
	@Override
	protected List<Instruction> delegate() {
		return list;
	}
	
	@Override
	public String toString() {
		String text = "(";
		for(int i = 0; i < size(); i++) {
			if(i != 0)
				text += ", ";
			text += get(i).toString();
		}
		return text + ")";
	}
}