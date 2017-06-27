package connect4;

//process all game data
//haven't add draw case
public class Connect4 {
	private int[][] grid;
	private int goal;
	private int player_turn;
	private GameScreen screen;
	private boolean end;
	//semaphore for AI calculation
	private boolean AI_move = false;
	//can be improved by setting a timer, otherwise the waiting time may be too long
	private int AI_depth = 5;
	
	Connect4(int _row, int _col, int _goal, GameScreen _screen) {
		grid = new int[_row][_col];
		goal = _goal;
		player_turn = 1;
		screen = _screen;
		end = false;
	}
	
	boolean inGrid(int col, int row, int[][] grid) {
		if(col >= 0 && row >= 0 && row < grid.length && col < grid[row].length)
			return true;
		return false;
	}
	
	
	//count consecutive pieces in same row/col/diagonal with specific grid, specific player
	int getPiecesCountByRule(int _row, int _col, int player, String rule, int[][] grid) {
		int cnt = 0, col = _col, row = _row, drow = 0, dcol = 0;
		
		if(rule.equals("row")) dcol = 1;
		else if(rule.equals("col")) drow = 1;
		else if(rule.equals("diagonal++")) {
			dcol = 1;
			drow = 1;
		}
		else if(rule.equals("diagonal+-")) {
			dcol = 1;
			drow = -1;
		}
		else return -1;
		
		while(inGrid(col, row, grid) && grid[row][col] == player) {
			cnt++;
			col -= dcol;
			row -= drow;
		}
		
		col = _col + dcol;
		row = _row + drow;
		while(inGrid(col, row, grid) && grid[row][col] == player) {
			cnt++;
			col += dcol;
			row += drow;
		}
		return cnt;
	}
	
	int getMaxConnPiece(int row, int col, int player, int[][] grid) {
		int cnt = 0;
		cnt = Math.max(cnt, getPiecesCountByRule(row, col, player, "row", grid));
		cnt = Math.max(cnt, getPiecesCountByRule(row, col, player, "col", grid));
		cnt = Math.max(cnt, getPiecesCountByRule(row, col, player, "diagonal++", grid));
		cnt = Math.max(cnt, getPiecesCountByRule(row, col, player, "diagonal+-", grid));
		return cnt;
	}
	
	//check if the player would win after playing this step
	boolean checkWin(int row, int col, int player, int[][] _grid) {
		int goalCnt = getMaxConnPiece(row, col, player, _grid);
		if(goalCnt >= goal && _grid == grid) end = true;
		if(goalCnt >= goal)
			return true;
		return false;
	}
	
	//return the location to place the piece, and update the grid
	//return format: int[] {row, column, which player, whether wins}
	int[] playPiece(int col) {
		assert(col >= 0 && col < grid[0].length);
		if(end || AI_move) return new int[]{-1, -1, -1, -1};
				
		int row = getPlayRowByCol(col, grid);
		if(row >= 0) {
			grid[row][col] = player_turn;
			int win = checkWin(row, col, player_turn, grid) ? 1 : 0;
			if(player_turn == 1) player_turn = 2;
			else player_turn = 1;
			return new int[]{row, col, player_turn, win};
		}
		
		return new int[]{-1, -1, -1, -1};
	}
	
	int getPlayRowByCol(int col, int[][] grid) {
		int row = grid.length-1;
		while(row >= 0 && grid[row][col] != 0)
			row--;
		
		return row;
	}
	
	//determine next move for AI, use min-max algorithm
	//return format is same as playPiece
	int[] playPiece_AI() {
		AI_move = true;
		int score = -999999, best_move = -1;
		
		int[][] simuGrid = new int[grid.length][grid[0].length];
		for(int i = 0; i < grid.length; i++) {
			for(int j = 0; j < grid[i].length; j++) {
				simuGrid[i][j] = grid[i][j];
			}
		}
		
		for(int col = 0; col < grid[0].length; col++) {
			int tempScore = -9999999;
			
			int row = getPlayRowByCol(col, simuGrid);
			if(row >= 0) {
				simuGrid[row][col] = player_turn;
				if(checkWin(row, col, player_turn, simuGrid)) tempScore = evalGrid(simuGrid, player_turn);
				else tempScore = minimaxMove(col, AI_depth, false, flipPlayer(player_turn), simuGrid, player_turn);
				simuGrid[row][col] = 0;
		}
			
			if(tempScore > score) {
				score = tempScore;
				best_move = col;
			}
		}
		AI_move = false;
		return best_move != -1 ? playPiece(best_move) : new int[]{-1, -1, -1, -1};
	}
	
	//return whether there is a piece from "player" near the location(row, col)
	boolean nearPiece(int[][] grid, int row, int col, int player) {
		for(int i = -1; i < 2; i++) {
			for(int j = -1; j < 2; j++) {
				if(i == 0 && j == 0) continue;
				if(inGrid(col+j, row+i, grid) && grid[row+i][col+j] == player)
					return true;
			}
		}
		return false;
	}
	
	//return a heuristic score for player
	//could tuning parameter using AI against another AI
	//simple evaluation function:
	//plus 10000 if win
	//1 available space = 1 score, try to expand as much space as possible
	int evalGrid(int[][] grid, int player) {
		int score = 0;
		//haven't mark visited slot, win score will be added multiple times
		for(int i = 0; i < grid.length; i++) {
			for(int j = 0; j < grid[i].length; j++) {
				if(grid[i][j] == 0 && nearPiece(grid, i, j, player))
					score++;
				else if(grid[i][j] == player) {
					if(getMaxConnPiece(i, j, player, grid) >= goal)
						score += 10000;
				}
				else {
					if(getMaxConnPiece(i, j, flipPlayer(player), grid) >= goal)
						score -= 10000;
				}
					
			}
		}
		return score;
	}
	
	int flipPlayer(int player) {
		if(player == 1) return 2;
		return 1;
	}
	
	//use minimax algorithm to determine AI's next move
	int minimaxMove(int col, int depth, boolean max, int player, int[][] grid, int AI) {
		int score = 0;
		if(depth == 0)
			return evalGrid(grid, AI);
		if(max) {
			score = -999999;
			for(int i = 0; i < grid[0].length; i++) {
				int row = getPlayRowByCol(i, grid);
				if(row >= 0) {
					grid[row][i] = player;
					if(checkWin(row, i, player, grid)) score = Math.max(score, evalGrid(grid, AI));
					else score = Math.max(score, minimaxMove(i, depth-1, !max, flipPlayer(player), grid, AI));
					grid[row][i] = 0;
				}
			}
		}
		else {
			score = 999999;
			for(int i = 0; i < grid[0].length; i++) {
				int row = getPlayRowByCol(i, grid);
				if(row >= 0) {
					grid[row][i] = player;
					if(checkWin(row, i, player, grid)) score = Math.min(score, evalGrid(grid, AI));
					else score = Math.min(score, minimaxMove(i, depth-1, !max, flipPlayer(player), grid, AI));
					grid[row][i] = 0;
			}
			}
		}
		
		return score;
	}
	
	
	
	
}
