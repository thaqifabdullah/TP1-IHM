package grapher.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;
import javax.swing.JPanel;
import javax.swing.event.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import java.awt.Point;

import java.util.*;

import static java.lang.Math.*;

import grapher.fc.*;


public class Grapher extends JPanel implements ListSelectionListener{
	static final int MARGIN = 40;
	static final int STEP = 5;
	
	static final BasicStroke dash = new BasicStroke(1, BasicStroke.CAP_ROUND,
	                                                   BasicStroke.JOIN_ROUND,
	                                                   1.f,
	                                                   new float[] { 4.f, 4.f },
	                                                   0.f);
	                                                   
	protected int W = 400;
	protected int H = 300;
	
	protected double xmin, xmax;
	protected double ymin, ymax;

	protected Vector<Function> functions;

	protected JList list;
	protected DefaultListModel<String> listModel;
	protected JToolBar toolbar;
	protected JOptionPane input;
	protected String[] expressions; 
	protected Action actionAdd, actionAddBut, actionRemove, actionRemoveBut; 

	protected MouseInputAdapter mouse;
	protected boolean drawRectangle = false;
	protected Point p0Rect, sizeRect;
	protected ArrayList<String> listExpGras;


	protected JMenuBar menu_bar ;
	protected JMenu menu_express;
	protected JMenuItem [] menu_item;

	public Grapher(String[] expressions) {
		xmin = -PI/2.; xmax = 3*PI/2;
		ymin = -1.5;   ymax = 1.5;
		
		functions = new Vector<Function>();
		this.expressions = expressions;
		listModel = new DefaultListModel<String>();
		for(String s:expressions){
			listModel.addElement(s);
		}
		actionAdd = new ActionAdd("Add...","Ajouter une fonction", new Integer(KeyEvent.VK_A));
		actionAddBut = new ActionAdd("+", "Ajouter une fonction", new Integer(KeyEvent.VK_A));
		actionRemove = new ActionRemove("Remove", "Supprimer une fonction", new Integer(KeyEvent.VK_S));
		actionRemoveBut = new ActionRemove(" - ", "Supprimer une fonction", new Integer(KeyEvent.VK_S));
		listExpGras = new ArrayList<String>();
		list = new JList(listModel);
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		toolbar = new JToolBar(JToolBar.HORIZONTAL);
		toolbar.setFloatable(false);
		initButtonsJToolBar();
		input = new JOptionPane();
		ajoutMenu();

		initListener();
	}

	protected void ajoutMenu(){

		menu_bar = new JMenuBar();
		menu_item = new JMenuItem[2];
		menu_item[0] = new JMenuItem(actionAdd);
		menu_item[1] = new JMenuItem(actionRemove);

		menu_express = new  JMenu("Expressions");
		for (int i = 0; i<2 ;i++ ) {
			menu_express.add(menu_item[i]);
		}
		menu_bar.add(menu_express);


	}

	protected void initButtonsJToolBar(){
		JButton addBut = new JButton(actionAddBut);
		JButton removeBut = new JButton(actionRemoveBut);
		toolbar.add(addBut);
		toolbar.add(removeBut);
	}

	protected void initListener(){
		mouse = new MouseInputAdapter(){
			Point depart;
			boolean dragged = false;
			Cursor curs, def;
			public void mouseClicked(MouseEvent e){
				if (e.getButton() == MouseEvent.BUTTON1) {
					zoom(e.getPoint(),5);					
				}else if (e.getButton() == MouseEvent.BUTTON3) {
					dezoom(e.getPoint(),5);
				}
			}

			public void mouseDragged(MouseEvent e){
				if(SwingUtilities.isLeftMouseButton(e)){
					curs = new Cursor(Cursor.HAND_CURSOR);
					setCursor(curs);
					if(!dragged){
						dragged = true;
						depart = e.getPoint();
					}else{
						translate(e.getX() - depart.x, e.getY() - depart.y);
						depart = e.getPoint();
					}
					
				}
				if(SwingUtilities.isRightMouseButton(e)){
					if(!dragged){
						dragged = true;
						depart = e.getPoint();
					}else{
						if((e.getX()-depart.x>=0) && (e.getY()-depart.y>=0))
  							p0Rect = depart;
  						else if((e.getX()-depart.x<0) && (e.getY()-depart.y<0))
  							p0Rect = e.getPoint();
  						else if((e.getX()-depart.x<0) && (e.getY()-depart.y>=0))
  							p0Rect = new Point(e.getX(),depart.y);
  						else
  							p0Rect = new Point(depart.x,e.getY());
  						sizeRect = new Point(Math.abs(e.getX()-depart.x),Math.abs(e.getY()-depart.y));
  						drawRectangle = true;
  						repaint();
					}
					
					
				}
			}

			public void mouseReleased(MouseEvent e){
				if(e.getButton() == MouseEvent.BUTTON1){
					dragged = false;
					setCursor(curs.getDefaultCursor());
				}
				if(e.getButton() == MouseEvent.BUTTON3){
					dragged = false;
					drawRectangle = false;
					repaint();
					zoom(depart, e.getPoint());
				}
				
			}

			public void mouseWheelMoved(MouseWheelEvent e){
				zoom(e.getPoint(),-e.getWheelRotation());
			}
		};
		addMouseMotionListener(mouse);
		addMouseListener(mouse);
		addMouseWheelListener(mouse);
		

		list.addListSelectionListener(this);
	}

	public void valueChanged(ListSelectionEvent e){
		int [] tabSelectedIndex = list.getSelectedIndices();
		listExpGras.clear();
		for(int i : tabSelectedIndex){
			listExpGras.add(expressions[i]);
		}
		repaint();
	}
	
	public void add(String expression) {
		add(FunctionFactory.createFunction(expression));
	}
	
	public void add(Function function) {
		functions.add(function);
		repaint();
	}
		
	public Dimension getPreferredSize() { return new Dimension(W, H); }
	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		W = getWidth();
		H = getHeight();

		Graphics2D g2 = (Graphics2D)g;

		// background
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, W, H);
		
		g2.setColor(Color.BLACK);

		// box
		g2.translate(MARGIN, MARGIN);
		W -= 2*MARGIN;
		H -= 2*MARGIN;
		if(W < 0 || H < 0) { 
			return; 
		}
		
		g2.drawRect(0, 0, W, H);
		
		g2.drawString("x", W, H+10);
		g2.drawString("y", -10, 0);
		
	
		// plot
		g2.clipRect(0, 0, W, H);
		g2.translate(-MARGIN, -MARGIN);

		// x values
		final int N = W/STEP + 1;
		final double dx = dx(STEP);
		double xs[] = new double[N];
		int    Xs[] = new int[N];
		for(int i = 0; i < N; i++) {
			double x = xmin + i*dx;
			xs[i] = x;
			Xs[i] = X(x);
		}
		
		for(Function f : functions) {
			// y values
			int Ys[] = new int[N];
			for(int i = 0; i < N; i++) {
				Ys[i] = Y(f.y(xs[i]));
			}
			
			if(!listExpGras.isEmpty()){
				if(listExpGras.contains(f.toString())){
					g2.setStroke(new BasicStroke(2));
					g2.drawPolyline(Xs, Ys, N);
				}else{
					g2.setStroke(new BasicStroke());
					g2.drawPolyline(Xs, Ys, N);
				}
			}else {
				g2.drawPolyline(Xs, Ys, N);
			}
		}
		g2.setStroke(new BasicStroke());
		g2.setClip(null);

		// axes
		drawXTick(g2, 0);
		drawYTick(g2, 0);
		
		double xstep = unit((xmax-xmin)/10);
		double ystep = unit((ymax-ymin)/10);

		g2.setStroke(dash);
		for(double x = xstep; x < xmax; x += xstep)  { drawXTick(g2, x); }
		for(double x = -xstep; x > xmin; x -= xstep) { drawXTick(g2, x); }
		for(double y = ystep; y < ymax; y += ystep)  { drawYTick(g2, y); }
		for(double y = -ystep; y > ymin; y -= ystep) { drawYTick(g2, y); }

		if(drawRectangle){
   			g2.draw(new RoundRectangle2D.Double(p0Rect.x, p0Rect.y, sizeRect.x, sizeRect.y, 10, 10));
   		}
	}
	
	protected double dx(int dX) { return  (double)((xmax-xmin)*dX/W); }
	protected double dy(int dY) { return -(double)((ymax-ymin)*dY/H); }

	protected double x(int X) { return xmin+dx(X-MARGIN); }
	protected double y(int Y) { return ymin+dy((Y-MARGIN)-H); }
	
	protected int X(double x) { 
		int Xs = (int)round((x-xmin)/(xmax-xmin)*W);
		return Xs + MARGIN; 
	}
	protected int Y(double y) { 
		int Ys = (int)round((y-ymin)/(ymax-ymin)*H);
		return (H - Ys) + MARGIN;
	}
		
	protected void drawXTick(Graphics2D g2, double x) {
		if(x > xmin && x < xmax) {
			final int X0 = X(x);
			g2.drawLine(X0, MARGIN, X0, H+MARGIN);
			g2.drawString((new Double(x)).toString(), X0, H+MARGIN+15);
		}
	}
	
	protected void drawYTick(Graphics2D g2, double y) {
		if(y > ymin && y < ymax) {
			final int Y0 = Y(y);
			g2.drawLine(0+MARGIN, Y0, W+MARGIN, Y0);
			g2.drawString((new Double(y)).toString(), 5, Y0);
		}
	}
	
	protected static double unit(double w) {
		double scale = pow(10, floor(log10(w)));
		w /= scale;
		if(w < 2)      { w = 2; } 
		else if(w < 5) { w = 5; }
		else           { w = 10; }
		return w * scale;
	}
	

	protected void translate(int dX, int dY) {
		double dx = dx(dX);
		double dy = dy(dY);
		xmin -= dx; xmax -= dx;
		ymin -= dy; ymax -= dy;
		repaint();	
	}
	
	protected void zoom(Point center, int dz) {
		double x = x(center.x);
		double y = y(center.y);
		double ds = exp(dz*.01);
		xmin = x + (xmin-x)/ds; xmax = x + (xmax-x)/ds;
		ymin = y + (ymin-y)/ds; ymax = y + (ymax-y)/ds;
		repaint();	
	}

	protected void dezoom(Point center, int dz) {
		double x = x(center.x);
		double y = y(center.y);
		double ds = exp(dz*.01);
		xmin = x + (xmin-x)*ds; xmax = x + (xmax-x)*ds;
		ymin = y + (ymin-y)*ds; ymax = y + (ymax-y)*ds;
		repaint();	
	}
	
	protected void zoom(Point p0, Point p1) {
		double x0 = x(p0.x);
		double y0 = y(p0.y);
		double x1 = x(p1.x);
		double y1 = y(p1.y);
		xmin = min(x0, x1); xmax = max(x0, x1);
		ymin = min(y0, y1); ymax = max(y0, y1);
		repaint();	
	}

	public JList getJList(){
		return list;
	}

	public JToolBar getJToolBar(){
		return toolbar;
	}

	public class ActionAdd extends AbstractAction{
		public ActionAdd(String s, String shortDescription, Integer mnemonic){
			super(s);
			putValue(SHORT_DESCRIPTION, shortDescription);
    		putValue(MNEMONIC_KEY, mnemonic);
		}
		public void actionPerformed(ActionEvent e){
			String newExpression = JOptionPane.showInputDialog(null, "Nouvelle Expression");
			if(newExpression != null){
				listModel.addElement(newExpression);
				expressions = new String[listModel.size()];
				for (int i=0; i<listModel.size(); i++) {
					expressions[i] = listModel.getElementAt(i);
				}
				for(String s : expressions) {
					add(s);
				}
			}
		}
	}

	public class ActionRemove extends AbstractAction{
		public ActionRemove(String s, String shortDescription, Integer mnemonic){
			super(s);
			putValue(SHORT_DESCRIPTION, shortDescription);
	    	putValue(MNEMONIC_KEY, mnemonic);
		}
		public void actionPerformed(ActionEvent e){
			if(!list.isSelectionEmpty()){
				int [] tabSelectedIndex = list.getSelectedIndices();
				String [] selectedExpressions = new String[tabSelectedIndex.length];
				for(int i=0; i<tabSelectedIndex.length; i++){
					selectedExpressions[i] = listModel.getElementAt(tabSelectedIndex[i]);
				}
				for(String s:selectedExpressions){
					if(listModel.contains(s)){
						listModel.removeElement(s);
					}
				}
				listExpGras.clear();
				functions.clear();
				expressions = new String[listModel.size()];
				for (int i=0; i<listModel.size(); i++) {
					expressions[i] = listModel.getElementAt(i);
				}
				for(String s : expressions) {
					add(s);
				}
			}
		}
	}
}
