import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.JOptionPane;
public class Main{
    public static void main(String[] args){
        EventQueue.invokeLater(() ->{
            try{ 
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            String[] modes = {"Beginner", "Intermediate", "Expert"};
            String selected = (String) JOptionPane.showInputDialog(
                null,"Choose your difficulty level:",
                "Minesweeper",JOptionPane.QUESTION_MESSAGE,
                null,modes,modes[0]
            );

            if(selected == null) selected = "Beginner";
            int rows, cols, mines;
            switch(selected){
                case "Intermediate":
                    rows = 16;
                    cols = 16;
                    mines = 40;
                    break;
                case "Expert":
                    rows = 16;
                    cols = 30;
                    mines = 99;
                    break;
                default:
                    rows = 9;
                    cols = 9;
                    mines = 10;
                    break;
            }
            JFrame frame = new JFrame("Minesweeper - " + selected);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new GameBoard(rows, cols, mines));
            frame.pack();
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}