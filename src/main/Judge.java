package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ui.AbstProgressMonitor;

public class Judge{
	/**
	 * coveredがcoverで,重ならずに覆えるかどうかを判定する.
	 * 覆えない場合は,null を返す.
	 * 覆える場合は,覆った状態を表すint[][] を返す.
	 * 0は空白,1~ がcoverのある位置を表し, -1 ~ が,coveredとcoverが重なっていることを表す.
	 * numOfCoverは,使用するcoverの最大枚数を示す.
	 * validCellDepthは,有効なセルのバウンディングボックスからの距離を示す.例えば,1なら,周辺のセルしかみない.
	 * また,その内側にはブロックがおいてあるかのように,入れない.
	 * 
	 * @param covered
	 * @param cover
	 * @param numCover
	 * @param validCellDepth
	 * @throws NoCellException
	 * @return
	 */
	public static int[][] judge(AbstPoly covered,AbstPoly cover,int numCover,int validCellDepth,AbstProgressMonitor monitor) throws NoCellException{
		if(cover==null)throw new NoCellException();
		
		if(monitor!=null){
			monitor.setValue(0);
		}
		
		int numValidCellOfCover=numValidCellOfCover(cover,validCellDepth);
		if(numValidCellOfCover==0)throw new NoCellException();
		
		cover=cover.trim();
		HashSet<AbstPoly> coversSet=new HashSet<AbstPoly>();
		for(int i=0;i<2;i++){
			for(int j=0;j<4;j++){
				coversSet.add(cover);
				cover=cover.rot90();
			}
			cover=cover.flip();
		}
		AbstPoly[] covers=coversSet.toArray(new AbstPoly[0]);
		
		int numCovers=covers.length;
		
		Cell[][] validCellsOfCovers=validCellsOfCovers(validCellDepth,covers,numCovers,numValidCellOfCover);
		//左上隅が,(0,0).
		
		int numCellOfCovered=0;
		for(int i=0;i<covered.getHeight();i++)
			for(int j=0;j<covered.getWidth();j++)
				if(covered.get(i,j)) numCellOfCovered++;
		Cell[] cellsOfCovered=new Cell[numCellOfCovered];
		for(int i=0,k=0;i<covered.getHeight();i++)
			for(int j=0;j<covered.getWidth();j++)
				if(covered.get(i,j)) cellsOfCovered[k++]=new Cell(i,j);
		
		Map<State,Integer> stateToNumCovered=new HashMap<State,Integer>();
		
		int all1 = numCovers * numValidCellOfCover * numCellOfCovered;
		for(int i=0,progress=0;i<numCovers;i++)
			for(int j=0;j<numValidCellOfCover;j++)
				for(int k=0;k<numCellOfCovered;k++){
					progress++;
					if(monitor!=null)monitor.setValue(progress * 10 / all1);
					// cellsOfCovered[k] に,validCellsOfCovers[i][j]
					// を重ねた場合を考えている.
					// dは,coverを,どれだけ動かせば,coveredに重なるかを表す.
					Cell d=cellsOfCovered[k].sub(validCellsOfCovers[i][j]);
					State state=new State(i,d);
					if(!stateToNumCovered.containsKey(state)) stateToNumCovered.put(state,1);
					else stateToNumCovered.put(state,stateToNumCovered.get(state)+1);
				}
		State[] states=stateToNumCovered.keySet().toArray(new State[0]);
		for(State state:states)
			state.num=stateToNumCovered.get(state);
		int numStates=states.length;
		for(int i=0;i<numStates;i++)
			states[i].es=new ArrayList<State>();
		
		int all2 = numStates*(numStates-1)/2;
		for(int i=0,progress=0;i<numStates;i++){
			Set<Cell> set=new HashSet<Cell>();
			for(int j=0;j<numValidCellOfCover;j++){
				set.add(validCellsOfCovers[states[i].id][j].add(states[i].d));
			}
			for(int j=i+1;j<numStates;j++){
				progress++;
				if(monitor!=null)monitor.setValue(10 + progress * 40 / all2);
				
				Cell d=states[i].d.sub(states[j].d);
				
				int h = Math.min(covers[states[i].id].getHeight(),covers[states[j].id].getHeight());
				int w = Math.min(covers[states[i].id].getWidth(),covers[states[j].id].getWidth());
				if(Math.abs(d.x)<h-validCellDepth&&Math.abs(d.y)<w-validCellDepth)
					continue;

				boolean ok=true;
				for(int k=0;k<numValidCellOfCover;k++){
					if(set.contains(validCellsOfCovers[states[j].id][k].add(states[j].d))){
						ok=false;
						break;
					}
				}
				if(ok){
					states[i].es.add(states[j]);
				}
			}
		}
		
		List<State> ans=null;
		numCover = Math.min(numCover,numCellOfCovered);
		
		int all3 = numCover * numStates;
		
		loop:for(int i=0,progress=0;i<numCover;i++){
			Set<State> visited=new HashSet<State>();
			for(State state:states){

				progress++;
				if(monitor!=null)monitor.setValue(50 + progress * 50 / all3);
				
				List<State> init=new ArrayList<State>();
				init.add(state);
				visited.add(state);
				ans=dfs(init,states,0,state.num,numCellOfCovered,
					i,0);
				if(ans!=null){
					break loop;
				}
			}
		}
		
		if(monitor!=null)monitor.setValue(0);
		
		if(ans==null) return null;
		
		int minX=Integer.MAX_VALUE,maxX=Integer.MIN_VALUE;
		int minY=Integer.MAX_VALUE,maxY=Integer.MIN_VALUE;
		for(State state:ans){
			for(int i=0;i<numValidCellOfCover;i++){
				Cell cell=validCellsOfCovers[state.id][i].add(state.d);
				minX=Math.min(minX,cell.x);
				maxX=Math.max(maxX,cell.x);
				minY=Math.min(minY,cell.y);
				maxY=Math.max(maxY,cell.y);
			}
		}
		int[][] res=new int[maxX-minX+1][maxY-minY+1];
		for(int i=0;i<ans.size();i++){
			State state=ans.get(i);
			for(int j=0;j<numValidCellOfCover;j++){
				Cell cell=validCellsOfCovers[state.id][j].add(state.d);
				assert res[cell.x-minX][cell.y-minY]==0;
				res[cell.x-minX][cell.y-minY]=i+1;
			}
		}
		
		for(int i=0;i<numCellOfCovered;i++){
			Cell cell=cellsOfCovered[i];
			assert res[cell.x-minX][cell.y-minY]>0;
			res[cell.x-minX][cell.y-minY]=-res[cell.x-minX][cell.y-minY];
		}
		return res;
	}
	
	private static Cell[][] validCellsOfCovers(int validCellDepth,AbstPoly[] covers,int numCovers,int numCellOfCover){
		Cell[][] cellsOfCovers=new Cell[numCovers][numCellOfCover];
		for(int i=0;i<numCovers;i++){
			for(int j=0,l=0;j<covers[i].getHeight();j++)
				for(int k=0;k<covers[i].getWidth();k++)
					if(isValidCoverCell(covers[i],j,k,validCellDepth)){
						cellsOfCovers[i][l++]=new Cell(j,k);
					}
		}
		return cellsOfCovers;
	}
	
	private static int numValidCellOfCover(AbstPoly cover,int validCellDepth){
		int numCellOfCover=0;
		for(int i=0;i<cover.getHeight();i++)
			for(int j=0;j<cover.getWidth();j++)
				if(isValidCoverCell(cover,i,j,validCellDepth)) numCellOfCover++;
		return numCellOfCover;
	}
	
	private static boolean isValidCoverCell(AbstPoly cover,int x,int y,int validCellDepth){
		int h=cover.getHeight(),w=cover.getWidth();
		if(!cover.get(x,y)) return false;
		if(0+validCellDepth<=x&&x<h-validCellDepth&&0+validCellDepth<=y&&y<w-validCellDepth) return false;
		return true;
	}
	
	static List<State> dfs(List<State> befStates,State[] states,int befId,int sum,int obj,
//		final Set<State> visited,
		int maxDepth,int depth){
		if(sum==obj) return befStates;
		if(depth>=maxDepth) return null;
		assert sum<obj;
		for(int i=befId;i<befStates.get(0).es.size();i++){
			State nxtState=befStates.get(0).es.get(i);
//			if(visited.contains(nxtState)) continue;
			boolean ok=true;
			for(int j=1;j<befStates.size();j++){
				if(!befStates.get(j).es.contains(nxtState)){
					ok=false;
					break;
				}
			}
			if(ok){
				befStates.add(nxtState);
				List<State> tmp=dfs(befStates,states,i,sum+nxtState.num,obj,
					maxDepth,depth+1);
				if(tmp!=null) return tmp;
				befStates.remove(befStates.size()-1);
			}
		}
		return null;
	}
	
	private static class State{
		int id;
		Cell d;
		int num;
		List<State> es;
		
		public State(int id,Cell d){
			super();
			this.id=id;
			this.d=d;
		}
		public int hashCode(){
			final int prime=31;
			int result=1;
			result=prime*result+d.x;
			result=prime*result+d.y;
			result=prime*result+id;
			return result;
		}
		public boolean equals(Object obj){
			if(this==obj) return true;
			if(obj==null) return false;
			if(getClass()!=obj.getClass()) return false;
			State other=(State)obj;
			if(d.x!=other.d.x) return false;
			if(d.y!=other.d.y) return false;
			if(id!=other.id) return false;
			return true;
		}
		public String toString(){
			return "State [d="+d+", es.size()="+es.size()+", id="+id+", num="+num+"]";
		}
	}
	
	/**
	 * ポリオミノが連結であるかどうかを返す.
	 * 
	 * @param poly
	 * @return
	 */
	public static boolean isConnected(AbstPoly poly){
		int h=poly.getHeight(),w=poly.getWidth();
		boolean[][] cell=new boolean[h+2][w+2];
		for(int i=0;i<h;i++)
			for(int j=0;j<w;j++)
				cell[i+1][j+1]=poly.get(i,j);
		return connected(cell);
	}
	private static boolean connected(boolean[][] cell){
		int h=cell.length,w=cell[0].length;
		loop:for(int i=0;i<h;i++)
			for(int j=0;j<w;j++)
				if(cell[i][j]){
					dfs(cell,i,j);
					break loop;
				}
		for(int i=0;i<h;i++)
			for(int j=0;j<w;j++)
				if(cell[i][j]) return false;
		return true;
	}
	private static final int[] dx={1,0,-1,0};
	private static final int[] dy={0,1,0,-1};
	private static void dfs(boolean[][] cell,int x,int y){
		int h=cell.length,w=cell[0].length;
		assert cell[x][y];
		cell[x][y]=false;
		for(int d=0;d<4;d++){
			int nx=x+dx[d],ny=y+dy[d];
			if(0<=nx&&nx<h&&0<=ny&&ny<w&&cell[nx][ny]){
				dfs(cell,nx,ny);
			}
		}
	}
	
	/**
	 * ポリオミノが穴開きでなければ,trueを返す.
	 * 
	 * @param poly
	 * @return
	 */
	public static boolean noHole(AbstPoly poly){
		int h=poly.getHeight(),w=poly.getWidth();
		boolean[][] cell=new boolean[h+2][w+2];
		for(int i=0;i<h+2;i++)
			for(int j=0;j<w+2;j++)
				cell[i][j]=true;
		for(int i=0;i<h;i++)
			for(int j=0;j<w;j++)
				cell[i+1][j+1]=!poly.get(i,j);
		return connected(cell);
	}
	
	public static int[][] judge(PolyArray covered,PolyArray cover) throws NoCellException{
		return judge(covered,cover,Integer.MAX_VALUE/2,Integer.MAX_VALUE/2);
	}

	public static int[][] judge(PolyArray covered,PolyArray cover,int numCover,int validCellDepth) throws NoCellException{
		return judge(covered,cover,numCover,validCellDepth,null);
	}
}
