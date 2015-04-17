package pt.iscte.apista.extractor.tests;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class TestCodeSWT {
	public void empty() {

	}
	
	public void irrelevant() {
		//Swing code
		JFrame frame = new JFrame();
		JPanel panel = new JPanel();
		frame.add(panel);
	}
	
	public void simple(){
		Display d = new Display();
		Shell s = new Shell(d);
		s.setLayout(new RowLayout());
		Button a = new Button(s, SWT.PUSH);
	}
	
	public void arg(){
		Display d = new Display();
		Shell s = new Shell(d);
		RowLayout rl = new RowLayout();
		s.setLayout(rl);
	}
	
	public void variableReassignment(){
		Display d = new Display();
		Shell s = new Shell(d);
		
		d = new Display();
		Shell s2 = new Shell(d);
	}

	public void interlace() {
		Display d = new Display();
		Shell s = new Shell(d);
		Shell s2 = new Shell(Display.getDefault());
		s.setLayout(new RowLayout());
		Button a = new Button(s, SWT.PUSH);
		String aText = "A";
		a.setText(aText);
	}
	
	public void doWhile(){
		Display d = new Display();
		Shell s = new Shell(d);
		Button a = new Button(s, SWT.PUSH);
		String aText = "A";
		do {
			a.setText(aText);
		} while (a.getText().isEmpty());
	}
	
	public void doWhile2(){
		Display d = new Display();
		Shell s = new Shell(d);
		Button a = new Button(s, SWT.PUSH);
		String aText = "A";
		do {
			a.setText(aText);
		} while (false);
	}
	
	public void doWhile3(){
		Display d = new Display();
		Shell s = new Shell(d);
		Button a = new Button(s, SWT.PUSH);
		String aText = "A";
		do {
			a.setText(aText);
		} while (a.getText().isEmpty());
		d.close();
	}
	
	public void while1(){
		Display d = new Display();
		Shell s = new Shell(d);
		Button a = new Button(s, SWT.PUSH);
		String aText = "A";
		while(!a.getText().equals(aText))
			a.setText(aText);
	}
	
	public void while2(){
		Display d = new Display();
		Shell s = new Shell(d);
		Button a = new Button(s, SWT.PUSH);
		String aText = "A";
		while(!a.getText().equals(aText))
			a.setText(aText);
		d.close();
	}
	
	public void while3(){
		Display d = new Display();
		Shell s = new Shell(d);
		Button a = new Button(s, SWT.PUSH);
		String aText = "A";
		while(!a.getText().equals(aText)){
			a.setText(aText);
			while(!a.getVisible())
				a.setVisible(true);
			a.getAccessible();
		}
		d.close();
	}
	
	public void whileInterlace(){
		Display d = new Display();
		Shell s = new Shell(d);
		Shell s2 = new Shell(Display.getDefault());
		Button a = new Button(s, SWT.PUSH);
		String aText = "A";
		while(!a.getText().equals(aText)){
			a.setText(aText);
			s2.dispose();
		}
		d.close();
	}
	
	public void for1(){
		Display d = new Display();
		Shell s = new Shell(d);
		for(int i = 0; i < s.getChildren().length; i++){
			s.dispose();
		}
	}
	
	public void for2(){
		Display d = new Display();
		Shell s = new Shell(d);
		for(int i = 0; i < s.getChildren().length; i++){
			s.getImages()[i].dispose();
		}
		d.dispose();
	}
	
	public void for3(){
		Display d = new Display();
		Shell s = new Shell(d);
		for(int i = 0; i < s.getChildren().length; i++){
			s.getChildren()[i].dispose();
		}
		d.dispose();
	}
	
	public void for4(){
		Display d = new Display();
		Shell s = new Shell(d);
		for(int i = 0; i < s.getChildren().length; i++){
			for(int j = 0; j < s.getChildren()[i].getListeners(SWT.ALL).length; j++){
				Object listener = s.getChildren()[i].getListeners(SWT.ALL)[j];
			}
		}
		d.dispose();
	}
	
	public void for5(){
		Display d = new Display();
		Shell s = new Shell(d);
		for(int i = 0; i < s.getChildren().length; i++){
			Control child = s.getChildren()[i];
			for(int j = 0; j < child.getListeners(SWT.ALL).length; j++){
				Object listener = child.getListeners(SWT.ALL)[j];
			}
		}
		d.dispose();
	}
	
	public void forEach1(){
		Display d = new Display();
		Shell s = new Shell(d);
		for(Control c: s.getChildren()) 
			c.dispose();		
	}
	
	public void forEach2(){
		Display d = new Display();
		Shell s = new Shell(d);
		for(Control child: s.getChildren())
			for(Object listener: child.getListeners(SWT.MouseDoubleClick))
				s.addControlListener(null);
		d.dispose();
	}
}