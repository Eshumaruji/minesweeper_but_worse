import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class MinesweeperGame{
    private final int rows;
    private final int cols;
    private final int mineCount;
    private final Random random = new Random();
    private Cell[][] cells;
    private GameStatus status;
    private boolean minesPlaced;

    public void reset(){
        cells = new Cell[rows][cols];
        for(int r = 0; r < rows; r++)
            for(int c = 0; c < cols; c++) 
                cells[r][c] = new Cell();
        status = GameStatus.READY;
        minesPlaced = false;
    }

    public MinesweeperGame(int rows, int cols, int mineCount){
        if(rows <= 0 || cols <= 0)
            throw new IllegalArgumentException("Rows and columns must be positive.");
        if(mineCount < 0 || mineCount >= rows * cols)
            throw new IllegalArgumentException("Mine count must be between 0 and rows * cols - 1.");
        this.rows = rows; this.cols = cols;
        this.mineCount = mineCount; reset();
    }

    
    public void toggleFlag(int row, int col){
        if(!inBounds(row, col) || status == GameStatus.WON || status == GameStatus.LOST) return;
        Cell cell = cells[row][col];
        if(!cell.isRevealed()) cell.setFlagged(!cell.isFlagged());
    }

    private void checkForWin(){
        for(int r = 0; r < rows; r++){
            for(int c = 0; c < cols; c++){
                Cell cell = cells[r][c];
                if(!cell.hasMine() && !cell.isRevealed()) return;
            }
        } status = GameStatus.WON;
    }

    private boolean inBounds(int row, int col){
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    public Cell getCell(int row, int col){ return cells[row][col]; }
    public int getRows(){ return rows; }
    public int getCols(){ return cols; }
    public int getMineCount(){ return mineCount; }
    public int getFlagCount(){
        int count = 0;
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                if (cells[r][c].isFlagged())
                    count++;
        return count;
    }

    public GameStatus getStatus(){ return status; }
    
    private void placeMines(int safeRow, int safeCol){
        List<int[]> candidates = new ArrayList<>();
        for(int r = 0; r < rows; r++)
            for(int c = 0; c < cols; c++)
                if(Math.abs(r - safeRow) > 1 || Math.abs(c - safeCol) > 1)
                    candidates.add(new int[] {r, c});

        if(candidates.size() < mineCount){
            candidates.clear();
            for(int r = 0; r < rows; r++)
                for(int c = 0; c < cols; c++)
                    if(r != safeRow || c != safeCol)
                        candidates.add(new int[] {r, c});
        }
        Collections.shuffle(candidates, random);
        for(int i = 0; i < mineCount; i++){
            int[] position = candidates.get(i);
            cells[position[0]][position[1]].setHasMine(true);
        }
    }

    private void calculateAdjacentMines(){
        for(int r = 0; r < rows; r++) {
            for(int c = 0; c < cols; c++) {
                if (cells[r][c].hasMine())  continue;
                int count = 0;
                for(int dr = -1; dr <= 1; dr++){
                    for(int dc = -1; dc <= 1; dc++){
                        if(dr == 0 && dc == 0) continue;
                        int nr = r + dr; int nc = c + dc;
                        if(inBounds(nr, nc) && cells[nr][nc].hasMine()) count++;
                    }
                } cells[r][c].setAdjacentMines(count);
            }
        }
    }

    private void floodFill(int startRow, int startCol) {
        Queue<int[]> queue = new ArrayDeque<>();
        Cell start = cells[startRow][startCol];
        start.setRevealed(true);
        queue.add(new int[] {startRow, startCol});
        while(!queue.isEmpty()){
            int[] current = queue.remove();
            int row = current[0], col = current[1];
            Cell currentCell = cells[row][col];
            if(currentCell.getAdjacentMines() != 0) continue;
            for(int dr = -1; dr <= 1; dr++){
                for(int dc = -1; dc <= 1; dc++){
                    if(dr == 0 && dc == 0) continue;
                    int nr = row + dr, nc = col + dc;
                    if(!inBounds(nr, nc)) continue;

                    Cell neighbor = cells[nr][nc];
                    if(!neighbor.isRevealed() && !neighbor.isFlagged() && !neighbor.hasMine()){
                        neighbor.setRevealed(true);
                        if(neighbor.getAdjacentMines() == 0)
                            queue.add(new int[] {nr, nc});
                    }
                }
            }
        }
    }

    private void revealAllMines(){
        for(int r = 0; r < rows; r++)
            for(int c = 0; c < cols; c++)
                if(cells[r][c].hasMine())
                    cells[r][c].setRevealed(true);
    }

    public void reveal(int row, int col){
        if(!inBounds(row, col) || status == GameStatus.WON || status == GameStatus.LOST) return;
        Cell cell = cells[row][col];
        if(cell.isRevealed() || cell.isFlagged()) return;

        if(!minesPlaced){
            placeMines(row, col); 
            calculateAdjacentMines();
            minesPlaced = true;
            status = GameStatus.PLAYING;
        } if(cell.hasMine()){
            cell.setRevealed(true);
            status = GameStatus.LOST;
            revealAllMines();
            return;
        } floodFill(row, col); checkForWin();
    }
}
