package pt.iscte.apista.extractor.tests;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class TestCodeIfSWT {
	public void if1(){
		Display d = new Display();
		Shell s = new Shell(d);
		Button a = new Button(s, SWT.PUSH);
		String aText = "A";
		if(!a.getText().equals(aText))
			a.setText(aText);
	}
	
	public void if2(){
		if(true)
			if(true)
				if(true){
					Display d = new Display();
				}
	}
	
	public void if3(){
		Display d = new Display();
		if(true)
			if(true)
				if(true){
					Shell s = new Shell(d);
				}
	}
	
	public void if4(){
		Display d = new Display();
		if(true){
			Shell s = new Shell(d);
			Button a = new Button(s, SWT.PUSH);	
		}
	}
	
	public void if5(){
		Display d = new Display();
		Shell s = new Shell(d);
		Shell s2 = new Shell(Display.getDefault());
		if(true){
			Button a = new Button(s, SWT.PUSH);
			a.setText("Button A");
		}
		if(false){
			Button b = new Button(s2, SWT.PUSH);
			b.setText("Button B");
		}
	}
	
	public void if6(){
		Display d = new Display();
		Shell s = new Shell(d);
		Shell s2 = new Shell(Display.getDefault());
		if(true){
			Button a = new Button(s, SWT.PUSH);
			a.setText("Button A");
			Button b = new Button(s2, SWT.PUSH);
			b.setText("Button B");
		}
	}
	
	public void ifElse(){
		Display d = new Display();
		Shell s = new Shell(d);
		if(s.isEnabled()){
			Button a = new Button(s, SWT.PUSH);
		}else {
			Button b = new Button(s, SWT.PUSH);
		}
	}
	
	public void ifElse2(){
		Display d = new Display();
		Shell s = new Shell(d);
		Button a = new Button(s, SWT.PUSH);
		if(s.isEnabled()){
			a.setText("");
		}else {
			Button b = new Button(s, SWT.PUSH);
		}
	}
	
	public void ifElse3(){
		Display d = new Display();
		Shell s = new Shell(d);
		Button a = new Button(s, SWT.PUSH);
		if(s.isEnabled()){
			a.setText("");
		}else {
			Button b = new Button(s, SWT.PUSH);
		}
		d.dispose();
	}
	
	public void ifElse4(){
		Display d = new Display();
		Shell s = new Shell(d);
		Button a = new Button(s, SWT.PUSH);
		if(s.isEnabled()){
			a.setText("");
		}else {
			Button b = new Button(s, SWT.PUSH);
		}
		if(s.forceFocus())
			s.addKeyListener(null);
	}
	
	public void ifElse5(){
		Display d = new Display();
		Shell s = new Shell(d);
		Button a = new Button(s, SWT.PUSH);
		if(s.isEnabled()){
			a.setText("");
		}else {
			Button b = new Button(s, SWT.PUSH);
		}
		if(s.forceFocus())
			s.addKeyListener(null);
		else
			s.addControlListener(null);
	}
	
	public void ifElse6(){
		Display d = new Display();
		Shell s = new Shell(d);
		Button a = new Button(s, SWT.PUSH);
		if(s.isEnabled()){
			if(s.forceFocus())
				s.addKeyListener(null);
			else
				s.addControlListener(null);
		}else {
			Button b = new Button(s, SWT.PUSH);
		}
	}
	
	public void ifElse7(){
		Display d = new Display();
		Shell s = new Shell(d);
		Button a = new Button(s, SWT.PUSH);
		if(s.isEnabled()){
			a.setText("");
		}else {
			if(s.forceFocus())
				s.addKeyListener(null);
			else
				s.addControlListener(null);
		}
	}
	
	public void else1(){
		Display d = new Display();
		Shell s = new Shell(d);
		Button a = new Button(s, SWT.PUSH);
		if(s.isEnabled())
			;
		else
			a.setText("");
	}
	
	public void ifJoin(){
		Button b = new Button(null, SWT.PUSH);
		Color c = new Color(null,255,255,255);
		if(true){
			b.setBackground(c);
		}
	}
	
	public void elseJoin(){
		Button b = new Button(null, SWT.PUSH);
		Color c = new Color(null,255,255,255);
		if(false){
		}else{
			b.setBackground(c);
		}
	}
}