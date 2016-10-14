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
		splitPane.setOneTouchExpandable(false);
		splitPane.setDividerLocation(100);

		for(String expression : expressions) {
			grapher.add(expression);
		}
		
		//add(grapher);
		//add(splitPane);
		JPanel add_menu = new JPanel();
		GridLayout gl = new GridLayout(1,1);
		add_menu.setLayout(gl);
		add_menu.add(grapher.menu_bar);
		add(add_menu,BorderLayout.NORTH);
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
