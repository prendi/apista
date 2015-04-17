package pt.iscte.apista.examples;


import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class Example1 {
	
	public static void main(String[] args) {
		Composite comp = new Composite(null, 0);
		RowLayout rl = new RowLayout();
		comp.setLayout(rl);
		Label l = new Label(null,0);
		l.setText("");
		
		
	}

}
