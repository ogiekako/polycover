package ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import main.Cell;

public class PolyPanel extends JPanel implements AbstView{
	public void update(){
		getParent().getParent().repaint();
	}
	
	private static final int margin = 5;
	private static final Color cellColor = Color.cyan;
	
	private final Model poly;
	PolyPanel(Model _poly,final Cont cont){
		cont.addView(this);
		poly = _poly;
		addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				if(cont.isRunning())return;
				if(poly.isNull())return;
				int mx = e.getX() , my = e.getY();
				Cell c = getCell(mx,my);
				if(c!=null) {
					cont.flip(poly,c.x,c.y);
				}
			}
		});
	}
	
	private Cell getCell(int mx,int my) {
		calcParams();

		int cx = (my-y1)/cL;
		int cy = (mx-x1)/cL;
		if(0<=cx && cx<ph && 0<=cy && cy<pw)return new Cell(cx,cy);
		return null;
	}
	
	int ph,pw;
	int cL;
	int x1,y1;
	private void calcParams() {
		ph = poly.getHeight();
		pw = poly.getWidth();
		
		int H = getHeight(), W = getWidth();
		cL = Math.min((H-2*margin)/ph,(W-2*margin)/pw);
		
//		assert cL > 0;
		
		y1 = (H-cL*ph)/2;
		x1 = (W-cL*pw)/2;
		
	}
	
	protected void paintComponent(Graphics g){
		if(poly.isNull())return;
		
		calcParams();
		drawCell(g);
		drawGrid(g);
	}
	private void drawGrid(Graphics g) {
		g.setColor(Color.black);
		
		int y2 = y1 + cL * ph;
		int x2 = x1 + cL * pw;
		for(int i=0;i<=ph;i++) {
			int y = y1 + cL * i;
			g.drawLine(x1,y,x2,y);
		}
		for(int i=0;i<=pw;i++) {
			int x = x1 + cL * i;
			g.drawLine(x,y1,x,y2);
		}
	}
	private void drawCell(Graphics g) {
		for(int i=0;i<ph;i++) {
			for(int j=0;j<pw;j++) {
				if(poly.get(i,j))g.setColor(cellColor);
				else g.setColor(Color.white);
				
				int y = y1 + cL * i;
				int x = x1 + cL * j;
				g.fillRect(x+1,y+1,cL-1,cL-1);
			}
		}
	}
}
