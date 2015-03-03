package main;

public interface AbstPoly{
	/**
	 * (x,y)でのセルの有無を返す.
	 * 0<=x<size().x() , 0<=y<size().y() でなければならない.
	 * @param x
	 * @param y
	 * @return
	 */
	boolean get(int x,int y);
	
	/**
	 * (x,y)のセルの有無を反転する.
	 * @param x
	 * @param y
	 */
	void flip(int x,int y);
	
	int getHeight();
	int getWidth();

	/**
	 * 90度回転.方向は問わない.
	 * @return
	 */
	AbstPoly rot90();

	/**
	 * とにかく裏返しにすれば良い
	 * @return
	 */
	AbstPoly flip();

	/**
	 * 周辺の空白を取り除いたpoly を返す.
	 * @return
	 */
	AbstPoly trim();
}
