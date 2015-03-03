package ui;

import main.AbstPoly;
import main.PolyArray;

public class Model implements AbstModel,AbstPoly{
	AbstPoly poly;
	public void flip(int x,int y){
		poly.flip(x,y);
	}
	public boolean get(int x,int y){
		return poly.get(x,y);
	}
	public int getHeight(){
		return poly.getHeight();
	};
	public int getWidth(){
		return poly.getWidth();
	}
	public AbstPoly flip() {
		throw new UnsupportedOperationException();
	}
	public AbstPoly rot90() {
		throw new UnsupportedOperationException();
	}
	public AbstPoly trim(){
		throw new UnsupportedOperationException();
	}
	
	Model(AbstPoly poly){
		this.poly=poly;
	}
	public void setPoly(PolyArray poly){
		this.poly = poly;
	}
	public boolean isNull(){
		return poly==null;
	}
}
