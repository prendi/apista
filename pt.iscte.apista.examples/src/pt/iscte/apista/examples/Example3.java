package pt.iscte.apista.examples;

import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class Example3 {

	public static void main(String[] args) {
		
		Composite c = new Composite(null, 0);
		RowLayout r = new RowLayout();
		c.setLayout(r);
		c.getBackground();
		Text t = new Text(null,0);
		t.setText("");
		
	}
	
}
