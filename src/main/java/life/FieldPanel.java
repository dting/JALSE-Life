package life;

import jalse.DefaultJALSE;
import jalse.JALSE;
import life.actions.Update;
import life.entities.Cell;
import life.entities.Cell.DeadCell;
import life.entities.Cell.LiveCell;
import life.entities.Field;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class FieldPanel extends JPanel implements ActionListener {

  public static final int HEIGHT = 500;
  public static final int WIDTH = 700;

  public static final int NUM_ROWS = 50;
  public static final int NUM_COLS = 70;

  private final JALSE jalse;
  private final Random random;

  private static void drawElement(final Graphics g, final Cell cell) {
    final Point position = cell.getPosition();
    final int size = CellProperties.SIZE;

    g.setColor(cell.getColor());
    g.fillRect(position.x, position.y, size, size);
  }

  public FieldPanel() {
    random = new Random();
    jalse = new DefaultJALSE.Builder().setManualEngine().build();
    createEntities();
    setPreferredSize(getField().getSize());
    new Timer(1000 / 30, this).start();
  }

  private void createEntities() {
    final Field field = jalse.newEntity(Field.ID, Field.class);
    final Cell[][] cells = new Cell[NUM_ROWS][NUM_COLS];
    field.setSize(new Dimension(WIDTH, HEIGHT));

    for (int row = 0; row < NUM_ROWS; row++) {
      for (int col = 0; col < NUM_COLS; col++) {
        final Cell cell = field.newEntity(UUID.randomUUID(), Cell.class);
        final Point position = new Point(CellProperties.SIZE * col, CellProperties.SIZE * row);
        cell.setRow(row);
        cell.setCol(col);
        cell.setPosition(position);
        cells[row][col] = cell;
      }
    }
    field.streamCells().forEach(c -> initialize(c, cells));
    field.scheduleForActor(new Update(), 0, 1000, TimeUnit.MILLISECONDS);
  }

  private void initialize(Cell cell, Cell[][] cells) {
    Set<Cell> neighbors = new LinkedHashSet<>();

    final int row = cell.getRow();
    final int col = cell.getCol();

    if (row > 0) neighbors.add(cells[row-1][col]);
    if (row < NUM_ROWS-1) neighbors.add(cells[row+1][col]);
    if (col > 0) neighbors.add(cells[row][col-1]);
    if (col < NUM_COLS-1) neighbors.add(cells[row][col+1]);

    if (row > 0 && col > 0) neighbors.add(cells[row-1][col-1]);
    if (row > 0 && col < NUM_COLS-1) neighbors.add(cells[row-1][col+1]);
    if (row < NUM_ROWS-1 && col > 0) neighbors.add(cells[row+1][col-1]);
    if (row < NUM_ROWS-1 && col < NUM_COLS -1) neighbors.add(cells[row+1][col+1]);

    cell.setNeighbors(neighbors);
    if (random.nextFloat() < 0.15) {
      cell.markAsType(LiveCell.class);
      cell.setColor(CellProperties.Live.COLOR);
    } else {
      cell.markAsType(DeadCell.class);
      cell.setColor(CellProperties.Dead.COLOR);
    }
  }

  private Field getField() {
    return jalse.getEntityAsType(Field.ID, Field.class);
  }

  @Override
  protected void paintComponent(final Graphics g) {
    super.paintComponent(g);

    final Field field = getField();
    field.streamCells().forEach(c -> drawElement(g, c));
    Toolkit.getDefaultToolkit().sync();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    jalse.resume();
    repaint();
  }
}
