package pacman.controllers.examples;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;

import com.sun.corba.se.impl.orbutil.closure.Constant;
import com.sun.org.apache.bcel.internal.Constants;
import com.sun.org.apache.bcel.internal.generic.MULTIANEWARRAY;
import com.sun.org.apache.xpath.internal.operations.Mod;

import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.GameView;
import sun.invoke.empty.Empty;

public class FinalPacman extends Controller<MOVE> {

	private static final int SAFE_DISTANCE      = 17;
	private static final int SAFE_EDIBLE_TIME   = 3;
	private static final int SAFE_LAIR_TIME     = 5;
	private static final int PILL_POINTS        = 10;
	private static final int POWER_PILL_POINTS  = 50;
	private static final int GHOSTS_POINTS      = 1000;
	private static final int AGRESSIVE_DISTANCE = 60;
	private static final int MAX_TRYES          = 150;
	private static final int RADAR_PILLS	    = 10;
	private static final int INITIAL_LEVEL      = 30;
	private boolean isBreadthSearch = true; 
	private MOVE lastMove;
	private int MAX_NIVEL_NODO = 30;
	private int pacmanLives = 3;
	public int [] pills;
	public int [] powerPills;
	public ArrayList<GHOST> ghosts;
	public ArrayList<GHOST> ghostsEdible;
	public int myPosition;
	public Game myGame;
	public ArrayList<Nodo> nodoList;
	public ArrayList<Nodo> nodoListSomething;
	
	@Override
	public MOVE getMove(Game game, long timeDue) {
		myGame = game;
		this.lastMove = game.getPacmanLastMoveMade();
		if (pacmanLives != game.getPacmanNumberOfLivesRemaining()) {
			pacmanLives = game.getPacmanNumberOfLivesRemaining();
			this.MAX_NIVEL_NODO = this.INITIAL_LEVEL;
		}
		myPosition = game.getPacmanCurrentNodeIndex();
		pills = game.getActivePillsIndices();
		powerPills = game.getActivePowerPillsIndices();
		nodoList = new ArrayList<FinalPacman.Nodo>();
		nodoListSomething = new ArrayList<FinalPacman.Nodo>();
		ghostsEdible = new ArrayList<GHOST>();
		ghosts = new ArrayList<GHOST>();
		for (GHOST value : GHOST.values()) {
			if (game.isGhostEdible(value)) {
				ghostsEdible.add(value);
			} else {
				ghosts.add(value);
			}
		}
		
		return this.criaArvore(game);
	}
	
	public void orderNearstGhostEdibleTree(Game game) {
		ArrayList<GHOST> ghostsEdibleNew = new ArrayList();
		GHOST [] vGhost = new GHOST[ghostsEdible.size()];
		System.out.println("Ghost Edible N " + ghostsEdible.size());

		for (int i = 0; i < ghostsEdible.size(); i++) {
			GHOST aux = ghostsEdible.get(i);
			boolean nearst = true;
			for (GHOST ghost : ghostsEdible) {
				if (game.getDistance(this.myPosition, game.getGhostCurrentNodeIndex(ghost), DM.PATH) < game.getDistance(this.myPosition, game.getGhostCurrentNodeIndex(aux), DM.PATH)) {
					
					aux = ghost;
				}
			}
			if (nearst) {
				ghostsEdibleNew.add(aux);
				nearst = false;
				System.out.println("Ghost EdibleNEW N " + ghostsEdibleNew.size());
			}
		
			System.out.println();
		}
		if (this.ghostsEdible.size() == ghostsEdibleNew.size()) {
			this.ghostsEdible = ghostsEdibleNew;
			System.out.println("Ordenou GHOST");
		}
		
	}
	
	public boolean isAnyGhostEdible(Game game){
		for(GHOST ghost : GHOST.values()) {
			if(game.isGhostEdible(ghost)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean haveGhostInLair(Game game) {
		for(GHOST ghost : this.ghosts) {
			if(game.getGhostLairTime(ghost) != 0) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isPathSafe(int [] path, Game game) {
		for (int j : path) {
			for(GHOST ghost : this.ghosts) {
				if(game.getGhostCurrentNodeIndex(ghost) == j || game.getDistance(j, game.getGhostCurrentNodeIndex(ghost), game.getPacmanLastMoveMade(), DM.EUCLID) < this.SAFE_DISTANCE) {
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean isAllNodoMaxLevel() {		
		for (Nodo n : this.nodoList) {
			if (n.getNivel() < MAX_NIVEL_NODO) {
				return false;
			}
		}
		
		return true;
	}
	
	public Nodo getMaxPointsNodoList(ArrayList<Nodo> list, Game game) {
		Nodo oNodo = null;
		float points = -1;
		for (Nodo n : list) {
			if (n.getPoints() > points) {
				points = n.getPoints();
				oNodo = n;
			}
			GameView.addPoints(game, Color.green, game.getShortestPath(myPosition, n.getIndex(), game.getPacmanLastMoveMade()));
		}
		System.out.println("FirstNodo and is LArgura:" + this.isBreadthSearch + " points " + points);
		return oNodo;
	}
	
	public Nodo getMaxPointsLastNodoList(ArrayList<Nodo> list) {
		Nodo oNodo = null;
		float points = 0;
		for (int i = list.size() - 1; i >= 0; i--) {
			Nodo n = list.get(i);
			if (n.getPoints() > points) {
				points = n.getPoints();
				oNodo = n;
			}
		}
		System.out.println("LastNodo");
		return oNodo;
	}
	
	private boolean isIndexInFatherTree(int index, Nodo nodo) {
		Nodo aux = nodo;
		int i = 0;
		while (aux.getPai() != null && i < 1) {
			aux = aux.getPai();
			if (aux.getIndex() == index) {
				return true;
			}			
			i++;
		}
		System.out.println("FirstNodo");
		return false;
	}
	
	private Nodo getFirstNodoFromNodoListNivel() {
		Nodo aux = null;
		for (int i = 0; i < this.nodoList.size(); i++) {
			if (this.nodoList.get(i).getNivel() != this.MAX_NIVEL_NODO) {
				aux = this.nodoList.remove(i);
				return aux;
			}
		}
		System.out.println("GET FIRST NODO");
		return aux;
	}
	
	private Nodo getLastNodoFromNodoListNivel() {
		Nodo aux = null;
		for (int i = this.nodoList.size() - 1; i >= 0 ; i--) {
			if (this.nodoList.get(i).getNivel() != this.MAX_NIVEL_NODO) {
				aux = this.nodoList.remove(i);
				return aux;
			}
		}
		System.out.println("GET LAST NODO");
		return aux;
	}
	
	private void setPaiPointsZero(Nodo oNodo) {
		Nodo aux = oNodo;
		int i = oNodo.getNivel() / 6;
		while(aux.getPai() != null && i != 0) {
			aux.setPoints(0);
			aux = aux.getPai();
			i--;
		}
	}
	
	public MOVE criaArvore(Game game) {
		Nodo pacmanNodo = new Nodo(this.myPosition, 0, 0);
		this.nodoList.add(pacmanNodo);
		this.orderNearstGhostEdibleTree(game);
		while(!this.isAllNodoMaxLevel()) {
			Nodo aux = null;
			if (isBreadthSearch) {
				aux = this.getFirstNodoFromNodoListNivel();
			} else {
				aux = this.getLastNodoFromNodoListNivel();
			}
			
			int [] neighbors = null; 	

			neighbors = game.getNeighbouringNodes(aux.getIndex()); 
			
			for (int i : neighbors) {
				boolean isSafe = true;
				float nodoPoints = 0;
				if (!this.isIndexInFatherTree(i, aux)) {
					 
					for (int p : pills) {
						if (i == p) {
							nodoPoints += this.PILL_POINTS;
							for (GHOST g : ghosts) {
								int ghostIndex = game.getGhostCurrentNodeIndex(g);
								if (game.getGhostLairTime(g) <= 0) {
									if (i == ghostIndex ||  game.getDistance(i, ghostIndex, this.lastMove, DM.PATH) < FinalPacman.SAFE_DISTANCE) {
										nodoPoints = 0;
										isSafe = false;
									}
								} 
							}
						}
					}
					for (int pp : powerPills) {
						if (i == pp && !this.haveGhostInLair(game) && this.ghostsEdible.size() == 0) {
							nodoPoints += FinalPacman.POWER_PILL_POINTS;
						} else if (i == pp && this.haveGhostInLair(game) || i == pp && this.ghostsEdible.size() > 0) {
							nodoPoints += 1;
						}
					}
					
					for (GHOST g : ghosts) {
						int ghostIndex = game.getGhostCurrentNodeIndex(g);
						if (game.getGhostLairTime(g) <= 0) {
							if (i == ghostIndex ||  game.getDistance(i, ghostIndex, game.getMoveToMakeToReachDirectNeighbour(i, ghostIndex), DM.EUCLID) < FinalPacman.SAFE_DISTANCE) {
//								System.out.println("Distance entre index vizinho -> ghost: " + game.getDistance(i, ghostIndex, DM.PATH));
//								System.out.println("Distance pacman -> ghost: " + game.getDistance(this.myPosition, ghostIndex, DM.PATH));
								nodoPoints = 0;
								System.out.println("Ghost is too close " + g.toString());
								isSafe = false;
							}
						}
					}
					for (GHOST ge : ghostsEdible) {
						int ghostIndex = game.getGhostCurrentNodeIndex(ge);
						if (game.getDistance(i, ghostIndex, DM.EUCLID) < FinalPacman.AGRESSIVE_DISTANCE) {
							boolean safe = true;
							int [] pathGhost = game.getShortestPath(this.myPosition, ghostIndex);
							for (GHOST g : ghosts) {
								if (game.getGhostLairTime(g) <= 0) {
									for (int j : pathGhost) {
										if (j == ghostIndex) {
											safe = false;
											isSafe = false;
											System.out.println("Is not SAFE:" + g.toString());
											nodoPoints = 0;
										}
									}
								}
//								if (game.getDistance(ghostIndex, game.getGhostInitialNodeIndex(), DM.PATH) < FinalPacman.SAFE_DISTANCE || game.getDistance(i, game.getGhostCurrentNodeIndex(g), DM.PATH) < FinalPacman.SAFE_DISTANCE) {
//									
//								}
							}
							if (safe) {
								if (game.getGhostEdibleTime(ge) > this.SAFE_EDIBLE_TIME) {
									this.nodoList.add(new Nodo(ghostIndex, null, (nodoPoints + FinalPacman.GHOSTS_POINTS), this.MAX_NIVEL_NODO));
								} else {
									nodoPoints = 0;
									isSafe = false;
								}		
							} else {
								isSafe = false;
								nodoPoints = 0;
							}
						}
					}
					
					nodoPoints += 0.1; // pontos por movimentar
					
					
					Nodo newNodo = new Nodo(i, aux, nodoPoints + aux.getPoints(), aux.getNivel() + 1);
					
					if (!isSafe) {
//						newNodo.setNivel(this.MAX_NIVEL_NODO);
//						newNodo.setPai(null);
						this.setPaiPointsZero(newNodo);
					} 
					this.nodoList.add(newNodo);
				}
			}
		}
		
		Nodo oNodo = null;
		if (isBreadthSearch) {
			oNodo = this.getMaxPointsNodoList(this.nodoList, game);
		} 
		if (oNodo == null) {
			oNodo = this.getMaxPointsLastNodoList(this.nodoList);
		}
		
		
		System.out.println("Quantidade de itens na arvore: " + this.nodoList.size());
		if (oNodo != null) {
			System.out.println("Nivel NODO" + oNodo.getNivel());
			GameView.addPoints(game, Color.BLUE, game.getShortestPath(myPosition, oNodo.getIndex(), game.getPacmanLastMoveMade()));
			return game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), oNodo.getIndex(), game.getPacmanLastMoveMade(), DM.EUCLID);
		} else {

			System.out.println("NODO é null");
		}
		
		if (this.MAX_NIVEL_NODO > 0) {
			this.MAX_NIVEL_NODO -= 1;
			this.nodoList = new ArrayList();
			this.isBreadthSearch = !this.isBreadthSearch;
			this.criaArvore(game);
		}
		
		// vai atras da pílula por um caminho seguro
		if (!this.haveGhostInLair(game)) {
			for (int pp : powerPills) {
				int [] path = game.getShortestPath(myPosition, pp);
				if(isPathSafe(path, game)) {
					GameView.addPoints(game, Color.MAGENTA, game.getShortestPath(myPosition, path[path.length - 1], game.getPacmanLastMoveMade()));
					return game.getNextMoveTowardsTarget(myPosition, path[path.length - 1], DM.PATH);	
				}
			}
		}
		for (int p : pills) {
			int [] path = game.getShortestPath(myPosition, p);
			if(isPathSafe(path, game)) {
				GameView.addPoints(game, Color.CYAN, game.getShortestPath(myPosition, path[path.length - 1], game.getPacmanLastMoveMade()));
				return game.getNextMoveTowardsTarget(myPosition, path[path.length - 1], DM.PATH);	
			}
		}
		
		this.MAX_NIVEL_NODO = this.INITIAL_LEVEL;
		
		//vai atras da pilula mais proxima
		int[] targetNodeIndices=new int[pills.length+powerPills.length];
		
		for(int i=0;i<pills.length;i++)
			targetNodeIndices[i]=pills[i];
		
		for(int i=0;i<powerPills.length;i++)
			targetNodeIndices[pills.length+i]=powerPills[i];
		
		GameView.addPoints(game, Color.YELLOW, game.getShortestPath(myPosition, game.getClosestNodeIndexFromNodeIndex(myPosition,targetNodeIndices,DM.PATH), game.getPacmanLastMoveMade()));
		return game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(),game.getClosestNodeIndexFromNodeIndex(myPosition,targetNodeIndices,DM.PATH),DM.PATH);
	}
	
	
	class Nodo {
		
		private int index;
		private Nodo pai;
		private float points; 
		private int nivel;
		
		public Nodo(int index, float points, int nivel) {
			this.index = index;
			this.points = points;
			this.nivel = nivel;
		}
		
		public Nodo(int index, Nodo pai, float points, int nivel) {
			this.index = index;
			this.pai = pai;
			this.points = points;
			this.nivel = nivel;
		}
		
		public int getNivel() {
			return nivel;
		}

		public void setNivel(int nivel) {
			this.nivel = nivel;
		}

		public float getPoints() {
			return points;
		}

		public void setPoints(float points) {
			this.points = points;
		}


		int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		public Nodo getPai() {
			return pai;
		}

		public void setPai(Nodo pai) {
			this.pai = pai;
		}
		
	}
	
}