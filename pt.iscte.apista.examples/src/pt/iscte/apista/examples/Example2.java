package pt.iscte.apista.examples;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class Example2 {

	public static void main(String[] args) {
	
		Composite comp = new Composite(null, 0);
		GridLayout rw = new GridLayout();
		comp.setLayout(rw);
		Text t = new Text(null, 0);
		t.setSize(100, 100);
		t.setText("");
	}
	
}
