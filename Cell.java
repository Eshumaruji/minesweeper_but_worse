/** Model for one Minesweeper square. */
public class Cell {
    private boolean hasMine;
    private boolean isRevealed;
    private boolean isFlagged;
    private int adjacentMines;
    
    public boolean hasMine() {return hasMine;}
    public void setHasMine(boolean hasMine) {this.hasMine = hasMine;}
    public boolean isRevealed() {return isRevealed;}
    public void setRevealed(boolean revealed) {this.isRevealed = revealed;}
    public boolean isFlagged() {return isFlagged;}
    public void setFlagged(boolean flagged) {this.isFlagged = flagged;}
    public int getAdjacentMines() {return adjacentMines;}
    public void setAdjacentMines(int adjacentMines) {this.adjacentMines = adjacentMines;}

    public void reset(){
        hasMine = false;
        isRevealed = false;
        isFlagged = false;
        adjacentMines = 0;
    }
}
