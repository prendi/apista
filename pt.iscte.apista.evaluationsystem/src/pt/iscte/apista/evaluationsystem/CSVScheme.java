package pt.iscte.apista.evaluationsystem;

public interface CSVScheme {

		String[] headers();
		String value(int index, int colIndex, EvaluationData data);
	
}
