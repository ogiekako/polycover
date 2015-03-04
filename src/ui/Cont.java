package ui;

import java.awt.Component;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import main.Cell;
import main.Judge;
import main.NoCellException;
import main.PolyArray;

public class Cont implements AbstCont,AbstProgressMonitor{
	final Model cover;
	final Model covered;
	final WAModel wa;
	boolean rotSym=false;
	boolean revRotSym=false;
	boolean realTime=false;
	boolean useBlock=false;
	int numOfCover=Integer.MAX_VALUE;
	int validCellDepth=Integer.MAX_VALUE;
	
	private boolean running;
	
	Cont(){
		cover=new Model(null);
		covered=new Model(new PolyArray(new boolean[20][20]));
		wa=new WAModel();
	}
	void setCover(PolyArray _cover){
		this.cover.setPoly(_cover);
		updateView();
	}
	void setCovered(PolyArray _covered){
		this.covered.setPoly(_covered);
		updateView();
	}
	private final List<AbstView> viewList=new ArrayList<AbstView>();
	private final List<AbstProgressMonitor> monitorList=new ArrayList<AbstProgressMonitor>();
	
	@Override
	public void addView(AbstView view){
		viewList.add(view);
	}
	
	@Override
	public void updateView(){
		for(AbstView view:viewList){
			view.update();
		}
	}
	
	public void flip(Model model,int x,int y){
		if(model==cover){
			flipCover(x,y);
		}else if(model==covered){
			model.flip(x,y);
		}else{
			assert false;
		}
		updateView();
	}
	private Cell rot90(Cell sz,Cell c){
		return new Cell(c.y,sz.y-1-c.x);
	}
	private Cell rev(Cell sz,Cell c){
		return new Cell(c.x,sz.y-1-c.y);
	}
	
	private void flipCover(int x,int y){
		Cell sz=new Cell(cover.getHeight(),cover.getWidth());
		Cell c=new Cell(x,y);
		Set<Cell> visited=new HashSet<Cell>();
		if(rotSym){
			assert cover.getHeight()==cover.getWidth();
			assert !revRotSym;
			for(int i=0;i<4;i++){
				if(!visited.contains(c)){
					cover.flip(c.x,c.y);
					visited.add(c);
				}
				c=rot90(sz,c);
			}
		}else if(revRotSym){
			assert cover.getHeight()==cover.getWidth();
			assert !rotSym;
			for(int i=0;i<2;i++){
				for(int j=0;j<4;j++){
					if(!visited.contains(c)){
						cover.flip(c.x,c.y);
						visited.add(c);
					}
					c=rot90(sz,c);
				}
				c=rev(sz,c);
			}
		}else{
			cover.flip(c.x,c.y);
		}
		updateView();
		
		if(realTime) run(null);
	}
	
	public void save(Component parent){
		JFileChooser chooser=new JFileChooser();
		chooser.showSaveDialog(parent);
		File file=chooser.getSelectedFile();
		
		PrintWriter pw;
		try{
			pw=new PrintWriter(file);
		}catch(Exception e){
			error(parent);
			return;
		}
		pw.println("covered:");
		pw.println(covered.getHeight()+" "+covered.getWidth());
		for(int i=0;i<covered.getHeight();i++){
			String s="";
			for(int j=0;j<covered.getWidth();j++){
				s+=covered.get(i,j)?"#":".";
			}
			pw.println(s);
		}
		
		pw.println("cover:");
		pw.println(cover.getHeight()+" "+cover.getWidth());
		for(int i=0;i<cover.getHeight();i++){
			String s="";
			for(int j=0;j<cover.getWidth();j++){
				s+=cover.get(i,j)?"#":".";
			}
			pw.println(s);
		}
		pw.flush();
	}
	
	private void error(Component parent){
		JOptionPane.showMessageDialog(parent,"something is wrong.");
	}
	
	public void load(Component parent){
		JFileChooser chooser=new JFileChooser();
		chooser.showOpenDialog(parent);
		File file=chooser.getSelectedFile();
		if(file==null) return;
		
		Scanner sc;
		try{
			sc=new Scanner(file);
		}catch(Exception e){
			error(parent);
			return;
		}
		try{
			if(!sc.next().equals("covered:")){
				error(parent);
				return;
			}
		}catch(Exception e){
			error(parent);
			return;
		}
		
		PolyArray _covered;
		try{
			_covered=PolyArray.load(sc);
		}catch(Exception e){
			error(parent);
			return;
		}
		try{
			if(!sc.next().equals("cover:")){
				error(parent);
				return;
			}
		}catch(Exception e){
			error(parent);
			return;
		}
		PolyArray _cover;
		try{
			_cover=PolyArray.load(sc);
		}catch(Exception e){
			error(parent);
			return;
		}
		this.cover.setPoly(_cover);
		this.covered.setPoly(_covered);
		updateView();
	}
	
	private void runInBackGround(JFrame parent){
		int[][] res;
		try{
			res=Judge.judge(covered.poly,cover.poly,numOfCover,validCellDepth,this);
		}catch(NoCellException ex){
			JOptionPane.showMessageDialog(parent,"board is empty.");
			return;
		}
		if(res==null){
			if(!Judge.isConnected(cover.poly)){
				JOptionPane.showMessageDialog(parent,"OK. but not connected.");
			}else if(!Judge.noHole(cover.poly)){
				JOptionPane.showMessageDialog(parent,"OK. but contains hole(s).");
			}else{
				JOptionPane.showMessageDialog(parent,"OK.");
			}
		}else{
			wa.setArray(res);
			updateView();
		}
	}
	public void run(final JFrame parent){
		if(running) return;
		SwingWorker<Integer,Integer> sw=new SwingWorker<Integer,Integer>(){
			protected Integer doInBackground() throws Exception{
				running=true;
				runInBackGround(parent);
				running=false;
				return 0;
			}
		};
		sw.execute();
	}
	
	public void setRotSym(boolean b){
		rotSym=b;
		updateView();
	}
	public void setRevRotSym(boolean b){
		revRotSym=b;
		updateView();
	}
	public void expand(){
		int h=cover.getHeight(),w=cover.getWidth();
		boolean[][] nxt=new boolean[h+2][w+2];
		for(int i=0;i<h;i++)
			for(int j=0;j<w;j++)
				nxt[i+1][j+1]=cover.get(i,j);
		cover.setPoly(new PolyArray(nxt));
		updateView();
	}
	public void setRealTimeMode(boolean b){
		realTime=b;
		updateView();
	}
	public void enableBlock(boolean b){
		useBlock=b;
		updateView();
	}
	public void setNumCoer(int i){
		numOfCover=i;
		updateView();
	}
	public void setValidCellDepth(int i){
		validCellDepth=i;
		updateView();
	}
	/**
	 * monitorには,0~100のあいだの数値を与える.
	 * 
	 * @param monitor
	 */
	public void addProgressMonitor(AbstProgressMonitor monitor){
		monitorList.add(monitor);
	}
	
	private int befProgress=0;
	public void setValue(int n){
		if(befProgress==n) return;
		befProgress=n;
		for(AbstProgressMonitor monitor:monitorList){
			monitor.setValue(n);
		}
	}
	public boolean isRunning(){
		return running;
	}
}
