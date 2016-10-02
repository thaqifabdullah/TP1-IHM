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

import java.util.Vector;

import static java.lang.Math.*;

import grapher.fc.*;


public class Grapher extends JPanel{
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

	MouseInputAdapter mouse;
	boolean drawRectangle = false;
	Point p0Rect, sizeRect;
	final static float dash1[] = { 10.0f };
	final static BasicStroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);

	public Grapher() {
		xmin = -PI/2.; xmax = 3*PI/2;
		ymin = -1.5;   ymax = 1.5;
		
		functions = new Vector<Function>();
		initListener();
	}

	public void initListener(){
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
			
			g2.drawPolyline(Xs, Ys, N);
		}

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
			g2.setStroke(dashed);
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
}
