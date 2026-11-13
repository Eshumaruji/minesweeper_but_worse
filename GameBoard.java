import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** Hexagonal Minesweeper rendered as isometric Minecraft-style blocks. */
public class GameBoard extends JPanel{
    private static final double MAX_RADIUS = 26;
    private static final int MARGIN = 10;
    private static final Color OUTLINE = new Color(30, 30, 30);
    private static final Color INK = new Color(26, 26, 26);

    private static final Color[] NUMBER_COLORS = {
        null,
        new Color(25, 70, 200),   // 1
        new Color(20, 130, 40),   // 2
        new Color(215, 35, 35),   // 3
        new Color(110, 40, 170),  // 4
        new Color(140, 60, 20),   // 5
        new Color(0, 135, 140)    // 6
    };

    private final MinesweeperGame game;
    private final HexGridPanel hexPanel;
    private final JLabel statusLabel = new JLabel("Click a square to start", SwingConstants.CENTER);
    private final JLabel mineLabel = new JLabel();

    public GameBoard(int rows, int cols, int mineCount){
        super(new BorderLayout(8, 8));
        game = new MinesweeperGame(rows, cols, mineCount);
        hexPanel = new HexGridPanel(chooseRadius(rows, cols));

        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(createHeader(), BorderLayout.NORTH);
        add(hexPanel, BorderLayout.CENTER);
        refreshBoard();
    }

    /** Picks the hex radius (max 26) so the whole board fits on the screen. */
    private static double chooseRadius(int rows, int cols){
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        double availW = screen.width * 0.92 - 2 * MARGIN - 20;
        double availH = screen.height * 0.80 - 2 * MARGIN - 80;
        double rW = availW / (Math.sqrt(3) * (cols + 0.5));
        double rH = availH / (1.5 * (rows - 1) + 2);
        double r = Math.floor(Math.min(MAX_RADIUS, Math.min(rW, rH)));
        return Math.max(10, r);
    }

    private JPanel createHeader(){
        JPanel header = new JPanel(new BorderLayout(8, 0));
        JButton restartButton = new JButton("New Game");
        restartButton.setFocusable(false);
        restartButton.addActionListener(e -> { game.reset(); refreshBoard(); });

        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 14f));
        mineLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        header.add(statusLabel, BorderLayout.CENTER);
        header.add(mineLabel, BorderLayout.EAST);
        header.add(restartButton, BorderLayout.WEST);
        return header;
    }

    private void refreshBoard(){
        switch(game.getStatus()){
            case READY:   statusLabel.setText("Click a square to start"); break;
            case PLAYING: statusLabel.setText("Game in progress"); break;
            case WON:     statusLabel.setText("You won! Well done."); break;
            case LOST:    statusLabel.setText("Boom! You hit a mine."); break;
            default:      statusLabel.setText("");
        }
        mineLabel.setText("Mines: " + game.getMineCount() + "   Flags: " + game.getFlagCount());
        hexPanel.repaint();
    }

    /**
     * A parallelogram face described by an origin and two edge vectors:
     * point(s, t) = origin + s * a + t * b, with s and t in [0, 1].
     */
    private static final class Face{
        final double ox, oy, ax, ay, bx, by;

        Face(double ox, double oy, double ax, double ay, double bx, double by){
            this.ox = ox; this.oy = oy;
            this.ax = ax; this.ay = ay;
            this.bx = bx; this.by = by;
        }

        private void add(Polygon p, double s, double t){
            p.addPoint((int) Math.round(ox + s * ax + t * bx),
                       (int) Math.round(oy + s * ay + t * by));
        }

        /** Sub-parallelogram covering s in [s0, s1] and t in [t0, t1]. */
        Polygon quad(double s0, double s1, double t0, double t1){
            Polygon p = new Polygon();
            add(p, s0, t0); add(p, s1, t0); add(p, s1, t1); add(p, s0, t1);
            return p;
        }

        Line2D.Double line(double s0, double t0, double s1, double t1){
            return new Line2D.Double(ox + s0 * ax + t0 * bx, oy + s0 * ay + t0 * by,
                                     ox + s1 * ax + t1 * bx, oy + s1 * ay + t1 * by);
        }

        /** Maps the unit square (s, t) onto this face. */
        AffineTransform toFace(){
            return new AffineTransform(ax, ay, bx, by, ox, oy);
        }
    }

    /** Custom canvas that paints and hit-tests the hexagonal block grid. */
    private final class HexGridPanel extends JPanel{
        private final double rad;
        private final double hexW;
        private final double rowH;
        private final int rows;
        private final int cols;
        private final double[][] centerX;
        private final double[][] centerY;
        private final Polygon[][] hexes;

        HexGridPanel(double radius){
            this.rad = radius;
            this.rows = game.getRows();
            this.cols = game.getCols();
            this.hexW = Math.sqrt(3) * radius;
            this.rowH = 1.5 * radius;
            centerX = new double[rows][cols];
            centerY = new double[rows][cols];
            hexes = new Polygon[rows][cols];

            for(int r = 0; r < rows; r++){
                for(int c = 0; c < cols; c++){
                    double cx = MARGIN + c * hexW + (r % 2 != 0 ? hexW / 2.0 : 0) + hexW / 2.0;
                    double cy = MARGIN + r * rowH + rad;
                    centerX[r][c] = cx;
                    centerY[r][c] = cy;
                    Polygon hex = new Polygon();
                    for(int k = 0; k < 6; k++){
                        double angle = Math.toRadians(-90 + 60 * k);
                        hex.addPoint((int) Math.round(cx + rad * Math.cos(angle)),
                                     (int) Math.round(cy + rad * Math.sin(angle)));
                    }
                    hexes[r][c] = hex;
                }
            }

            setBackground(new Color(35, 38, 45));
            setOpaque(true);
            int width = (int) Math.ceil(cols * hexW + hexW / 2.0 + 2 * MARGIN);
            int height = (int) Math.ceil((rows - 1) * rowH + 2 * rad + 2 * MARGIN);
            setPreferredSize(new Dimension(width, height));

            addMouseListener(new MouseAdapter(){
                @Override
                public void mousePressed(MouseEvent e){
                    for(int r = 0; r < rows; r++){
                        for(int c = 0; c < cols; c++){
                            if(hexes[r][c].contains(e.getX(), e.getY())){
                                if(e.getButton() == MouseEvent.BUTTON1) game.reveal(r, c);
                                else if(e.getButton() == MouseEvent.BUTTON3) game.toggleFlag(r, c);
                                refreshBoard();
                                repaint();
                                return;
                            }
                        }
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            for(int r = 0; r < rows; r++)
                for(int c = 0; c < cols; c++)
                    drawCell(g2, r, c);
            g2.dispose();
        }

        private void drawCell(Graphics2D g2, int r, int c){
            Cell cell = game.getCell(r, c);
            double x = centerX[r][c], y = centerY[r][c];
            double hw = Math.sqrt(3) / 2.0 * rad;

            // C = (x, y), V5 = (x-hw, y-R/2), V1 = (x+hw, y-R/2), V3 = (x, y+R)
            Face top   = new Face(x, y,           -hw, -rad / 2.0,  hw, -rad / 2.0); // V5 V0 V1 C
            Face left  = new Face(x - hw, y - rad / 2.0, hw, rad / 2.0, 0, rad);     // V5 C V3 V4
            Face right = new Face(x, y,            hw, -rad / 2.0,  0,   rad);       // C V1 V2 V3
            int seed = r * 131 + c * 17;

            if(cell.isRevealed()){
                if(cell.hasMine()) drawTnt(g2, top, left, right);
                else drawStone(g2, top, left, right, seed);
            } else if(cell.isFlagged()){
                drawWool(g2, top, left, right);
            } else {
                drawGrass(g2, top, left, right, seed);
            }

            // Outer hex edges and inner Y-seam
            g2.setColor(OUTLINE);
            g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(hexes[r][c]);
            g2.draw(new Line2D.Double(x, y, x - hw, y - rad / 2.0)); // C-V5
            g2.draw(new Line2D.Double(x, y, x + hw, y - rad / 2.0)); // C-V1
            g2.draw(new Line2D.Double(x, y, x, y + rad));            // C-V3

            if(cell.isRevealed() && !cell.hasMine() && cell.getAdjacentMines() > 0)
                drawNumber(g2, x, y, cell.getAdjacentMines());
        }

        private void fill(Graphics2D g2, Polygon p, Color color){
            g2.setColor(color);
            g2.fill(p);
        }

        /** Deterministic light/dark pixel noise to give blocks a pixel-art feel. */
        private void speckle(Graphics2D g2, Face f, int seed, double t0, int n){
            for(int i = 0; i < n; i++){
                for(int j = 0; j < n; j++){
                    int h = (seed * 31 + i * 17 + j * 7 + i * j * 13) & 7;
                    if(h > 1) continue;
                    g2.setColor(h == 0 ? new Color(0, 0, 0, 30) : new Color(255, 255, 255, 26));
                    double ta = t0 + (1 - t0) * j / n, tb = t0 + (1 - t0) * (j + 1) / n;
                    g2.fill(f.quad((double) i / n, (double) (i + 1) / n, ta, tb));
                }
            }
        }

        private void drawGrass(Graphics2D g2, Face top, Face left, Face right, int seed){
            fill(g2, top.quad(0, 1, 0, 1), new Color(0x69B030));
            fill(g2, left.quad(0, 1, 0, 1), new Color(0x8B5A2B));
            fill(g2, right.quad(0, 1, 0, 1), new Color(0x6E4720));
            speckle(g2, top, seed, 0, 4);
            speckle(g2, left, seed + 1, 0.2, 4);
            speckle(g2, right, seed + 2, 0.2, 4);
            // Grass overhang strips (top 20% of each side face)
            fill(g2, left.quad(0, 1, 0, 0.2), new Color(0x589826));
            fill(g2, right.quad(0, 1, 0, 0.2), new Color(0x467A1E));
        }

        private void drawWool(Graphics2D g2, Face top, Face left, Face right){
            Face[] faces = {top, left, right};
            Color[] colors = {new Color(0xE32626), new Color(0xC21B1B), new Color(0x991414)};
            g2.setStroke(new BasicStroke(1f));
            for(int i = 0; i < 3; i++){
                Face f = faces[i];
                fill(g2, f.quad(0, 1, 0, 1), colors[i]);
                // cross-hatch wool texture
                g2.setColor(new Color(0, 0, 0, 45));
                for(double k = 0.25; k < 1; k += 0.25){
                    g2.draw(f.line(k, 0, k, 1));
                    g2.draw(f.line(0, k, 1, k));
                }
                // inner bevel
                g2.setColor(new Color(255, 255, 255, 70));
                g2.draw(f.quad(0.08, 0.92, 0.08, 0.92));
            }
        }

        private void drawTnt(Graphics2D g2, Face top, Face left, Face right){
            g2.setStroke(new BasicStroke(1f));
            Color darkRed = new Color(110, 20, 12, 150);

            // Top face with dynamite-stick lines and a fuse square
            fill(g2, top.quad(0, 1, 0, 1), new Color(0xDB3321));
            g2.setColor(darkRed);
            for(double k = 0.25; k < 1; k += 0.25) g2.draw(top.line(k, 0, k, 1));
            fill(g2, top.quad(0.30, 0.70, 0.30, 0.70), new Color(0x555555));
            fill(g2, top.quad(0.38, 0.62, 0.38, 0.62), new Color(0x1A1A1A));

            // Side faces
            fill(g2, left.quad(0, 1, 0, 1), new Color(0xC72A1C));
            fill(g2, right.quad(0, 1, 0, 1), new Color(0x9E2015));
            g2.setColor(darkRed);
            for(double k = 0.25; k < 1; k += 0.25){
                g2.draw(left.line(k, 0, k, 1));
                g2.draw(right.line(k, 0, k, 1));
            }
            // White strips across the middle third (35%..65%)
            fill(g2, left.quad(0, 1, 0.35, 0.65), new Color(0xE8E8E8));
            fill(g2, right.quad(0, 1, 0.35, 0.65), new Color(0xCCCCCC));
            drawTntLetters(g2, left);
            drawTntLetters(g2, right);
        }

        /** Draws "TNT" as pixel bars in the face's skewed (s, t) coordinate space. */
        private void drawTntLetters(Graphics2D g2, Face f){
            Graphics2D gg = (Graphics2D) g2.create();
            gg.transform(f.toFace());
            gg.setColor(INK);
            double t0 = 0.39, t1 = 0.61, h = t1 - t0;
            double w = 0.22, bar = 0.07;
            double[] starts = {0.11, 0.39, 0.67};
            for(int i = 0; i < 3; i++){
                double s0 = starts[i];
                if(i == 1){ // N
                    gg.fill(new Rectangle2D.Double(s0, t0, bar, h));
                    gg.fill(new Rectangle2D.Double(s0 + w - bar, t0, bar, h));
                    Path2D.Double diag = new Path2D.Double();
                    diag.moveTo(s0, t0);
                    diag.lineTo(s0 + bar, t0);
                    diag.lineTo(s0 + w, t1);
                    diag.lineTo(s0 + w - bar, t1);
                    diag.closePath();
                    gg.fill(diag);
                } else {    // T
                    gg.fill(new Rectangle2D.Double(s0, t0, w, bar));
                    gg.fill(new Rectangle2D.Double(s0 + w / 2.0 - bar / 2.0, t0, bar, h));
                }
            }
            gg.dispose();
        }

        private void drawStone(Graphics2D g2, Face top, Face left, Face right, int seed){
            fill(g2, top.quad(0, 1, 0, 1), new Color(0xD0D0D0));
            fill(g2, left.quad(0, 1, 0, 1), new Color(0xB0B0B0));
            fill(g2, right.quad(0, 1, 0, 1), new Color(0x959595));
            speckle(g2, top, seed, 0, 4);
            speckle(g2, left, seed + 1, 0, 4);
            speckle(g2, right, seed + 2, 0, 4);
        }

        private void drawNumber(Graphics2D g2, double x, double y, int number){
            String text = Integer.toString(number);
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, (float) (rad * 0.85)));
            FontMetrics fm = g2.getFontMetrics();
            float tx = (float) (x - fm.stringWidth(text) / 2.0);
            float ty = (float) (y + (fm.getAscent() - fm.getDescent()) / 2.0);
            g2.setColor(new Color(0, 0, 0, 170));
            g2.drawString(text, tx + 1f, ty + 1f);
            g2.setColor(NUMBER_COLORS[Math.min(number, 6)]);
            g2.drawString(text, tx, ty);
        }
    }
}
