package pt.iscte.apista.extractor.tests;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class TestCodePaper {

	public void test1() {
		Composite composite1 = new Composite (null,0);
		composite1.setLayout(new RowLayout());
		Composite composite2 = new Composite (null,0);
		composite2.setLayout(new FillLayout());
		Button button = new Button (composite1, 0 );
		Text text = new Text(composite2 , 0); 
		button.setText ("");
		text.setText("");
	}
	
	public void ifElse3(){
		Composite c = new Composite(null, 0);
		if(c.isVisible()){
			c.setFocus();
		}
		else {
			c.setVisible(true);
			c.redraw();
		}
		c.layout();
	}
	
	
//	public void test2() {
//		new Label(null,0).getParent().getParent();
//		Label label = new Label(null, 0);
//		Composite parent = label.getParent();
//		Composite parent2 = parent.getParent();
//	}
	
	public void interlace() {
		Shell shell1 = new Shell(Display.getDefault());
		Shell shell2 = new Shell(Display.getDefault());
		Button a = new Button(shell1, SWT.PUSH);
		a.setText("");
		Text b = new Text(shell2, SWT.PUSH);
		b.setText("");
	}
}
