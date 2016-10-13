package grapher.ui;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.*;
import java.awt.*;


public class Main extends JFrame {
	Main(String title, String[] expressions) {
		super(title);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		Grapher grapher = new Grapher(expressions);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, grapher.getJList(), grapher); 
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(150);

		for(String expression : expressions) {
			grapher.add(expression);
		}
		
		//add(grapher);
		//add(splitPane);
		add(grapher.getJToolBar(), BorderLayout.PAGE_END);
		add(splitPane, BorderLayout.CENTER);
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
