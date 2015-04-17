package pt.iscte.apista.ngram;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import pt.iscte.apista.core.Instruction;

import com.google.common.collect.Table;

public class Reporter {

	public static void reportFrequencyTable(
			Table<Instruction, Instruction, InstructionInfo> table) {

		File dataFile = new File("Data.csv");
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(dataFile));

			for (Instruction row : table.rowKeySet()) {
				bw.write(row.getWord() + ", ");

				for (Instruction column : table.row(row).keySet()) {
					bw.write(column.getWord()
							+ "("
							+ table.get(row, column)
									.getRelativeFrequency() + ") + ( " + table.get(row, column).getFrequency() + " ),");
//							.getFrequency() + "), ");
				}
				bw.write("\n");
			}
			bw.flush();
			bw.close();

		} catch (IOException e1) {
			System.out
					.println("Problem creating report file on Report.class - reportFrequencyTable(Table<R,C,V) table)");
		}
	}
}
