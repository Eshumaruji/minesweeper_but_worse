import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class GameBoard extends JPanel{
    private static final int ICON_SIZE = 25;
    private final ImageIcon mineIcon = loadImage("images/mine.png");
    private final ImageIcon flagIcon = loadImage("images/flag.png");

    private final MinesweeperGame game;
    private final JButton[][] cellButtons;
    private final JLabel statusLabel = new JLabel("Click a square to start", SwingConstants.CENTER);
    private final JLabel mineLabel = new JLabel();

    private final Color HIDDEN_COLOR = new Color(170, 190, 210);
    private final Color REVEALED_COLOR = new Color(230, 230, 230);
    private final Color FLAG_COLOR = new Color(255, 220, 120);

    public GameBoard(int rows, int cols, int mineCount){
        super(new BorderLayout(8, 8));
        game = new MinesweeperGame(rows, cols, mineCount);
        cellButtons = new JButton[rows][cols];

        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(createHeader(), BorderLayout.NORTH);
        add(createGrid(rows, cols), BorderLayout.CENTER);
        refreshBoard();
    }

    private JPanel createHeader(){
        JPanel header = new JPanel(new BorderLayout(8, 0));
        JButton restartButton = new JButton("New Game");
        restartButton.addActionListener(e ->{ game.reset(); refreshBoard(); });

        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 14f));
        mineLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        header.add(statusLabel, BorderLayout.CENTER);
        header.add(mineLabel, BorderLayout.EAST);
        header.add(restartButton, BorderLayout.WEST);
        return header;
    }

    private JPanel createGrid(int rows, int cols){
        JPanel grid = new JPanel(new GridLayout(rows, cols, 2, 2));
        grid.setBackground(Color.DARK_GRAY);
        for(int r = 0; r < rows; r++){
            for(int c = 0; c < cols; c++){
                final int row = r;
                final int col = c;
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
                button.setMargin(new java.awt.Insets(0, 0, 0, 0));
                button.setFont(button.getFont().deriveFont(Font.BOLD, 16f));
                button.setFocusable(false);
                button.setBackground(HIDDEN_COLOR);
                button.setOpaque(true);
                button.setBorderPainted(true);
                button.setFocusPainted(false);
                button.addMouseListener(new MouseAdapter(){
                    @Override
                    public void mousePressed(MouseEvent event){
                        if(event.getButton() == MouseEvent.BUTTON1){
                            game.reveal(row, col);
                            button.setBackground(REVEALED_COLOR);
                        } else if(event.getButton() == MouseEvent.BUTTON3){
                            game.toggleFlag(row, col);
                            button.setBackground(FLAG_COLOR);
                        } refreshBoard();
                    }
                });
                cellButtons[r][c] = button;
                grid.add(button);
            }
        } return grid;
    }
    private ImageIcon loadImage(String path){
        ImageIcon original = new ImageIcon(path);
        if(original.getIconWidth() <= 0){
            System.out.println("Image not found: " + path);
            return null;
        }
        Image scaled = original.getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH); 
        return new ImageIcon(scaled);
    }

    private void refreshBoard(){
        GameStatus status = game.getStatus();
        for(int r = 0; r < game.getRows(); r++){
            for(int c = 0; c < game.getCols(); c++){
                Cell cell = game.getCell(r, c);
                JButton button = cellButtons[r][c];
                button.setText("");
                button.setIcon(null);
                button.setDisabledIcon(null);
                button.setHorizontalAlignment(SwingConstants.CENTER);
                button.setBackground(new Color(170, 190, 210));
                button.setEnabled(
                    status != GameStatus.WON &&
                    status != GameStatus.LOST &&
                    !cell.isRevealed()
                );
                if(cell.isRevealed()){
                    button.setBackground(new Color(235, 235, 235));
                    if(cell.hasMine()){
                        if(mineIcon != null){
                            button.setIcon(mineIcon);
                            button.setDisabledIcon(mineIcon);
                        } else button.setText("*");
                        button.setBackground(new Color(255, 120, 120));
                    } else if(cell.getAdjacentMines() > 0){
                        button.setText(Integer.toString(cell.getAdjacentMines()));
                        button.setFont(button.getFont().deriveFont(Font.BOLD, 18f));
                        button.setForeground(numberColor(cell.getAdjacentMines()));
                    }
                } else if(cell.isFlagged()){
                    if(flagIcon != null) button.setIcon(flagIcon);
                    else button.setText("F");
                    button.setBackground(new Color(255, 225, 140));
                }
            }
        }
        switch(status){
            case READY:   statusLabel.setText("Click a square to start"); break;
            case PLAYING: statusLabel.setText("Game in progress"); break;
            case WON:     statusLabel.setText("You won! Well done."); break;
            case LOST:    statusLabel.setText("Boom! You hit a mine."); break;
            default:      statusLabel.setText("");
        }
        mineLabel.setText("Mines: " + game.getMineCount() + "   Flags: " + game.getFlagCount());
        revalidate(); repaint();
    }

    private Color numberColor(int number){
        switch(number){
            case 1: return new Color(35, 75, 190);
            case 2: return new Color(30, 125, 55);
            case 3: return new Color(200, 45, 45);
            case 4: return new Color(90, 45, 145);
            case 5: return new Color(130, 55, 35);
            case 6: return new Color(35, 130, 135);
            case 7: return Color.BLACK;
            default: return Color.GRAY;
        }
    }
}
