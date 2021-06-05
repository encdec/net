package net.encdec.eddsk.code;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

//
public class CloseListener implements ActionListener {
	private JFrame toBeClosed;
		public CloseListener(JFrame toBeClosed) {
			this.toBeClosed = toBeClosed;		
		}

	@Override
	public void actionPerformed(ActionEvent e) {
		toBeClosed.setVisible(false);
		toBeClosed.dispose();
		System.exit(0);		
	}

}
