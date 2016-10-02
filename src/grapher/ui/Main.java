package grapher.ui;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.*;


public class Main extends JFrame {
	Main(String title, String[] expressions) {
		super(title);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		Grapher grapher = new Grapher();
		JList list = new JList(expressions);	
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, list, grapher); 
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(150);

		for(String expression : expressions) {
			grapher.add(expression);
		}
		
		//add(grapher);
		add(splitPane);
		pack();
	}

	public static void main(String[] argv) {
		final String[] expressions = argv;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() { 
				new Main("grapher", expressions).setVisible(true); 
			}
		});
	}
}