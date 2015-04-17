package pt.iscte.apista.extractor.tests;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class TestCodeClassSWT {
	
	public void local(){
		class LocalClass{
			public void simple(){
				Display d = new Display();
				Shell s = new Shell(d);
				s.setLayout(new RowLayout());
				Button a = new Button(s, SWT.PUSH);
			}
		}
	}
	
	public void anonymous1(){
		Button a = new Button(new Shell(Display.getDefault()), SWT.PUSH);
		a.addControlListener(new ControlListener() {
			
			@Override
			public void controlResized(ControlEvent arg0) {
				Display d = new Display();
				Shell s = new Shell(d);
				if(s.isEnabled()){
					Button a = new Button(s, SWT.PUSH);
				}else {
					Button b = new Button(s, SWT.PUSH);
				}
			}
			
			@Override
			public void controlMoved(ControlEvent arg0) {
				
			}
		});
	}
	
	
	
	

}
