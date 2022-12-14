package Main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class GameEngine extends JPanel implements ActionListener {

	final int WIDTH = 1000;
	final int HEIGHT = 600;

	ArrayList<ArrayList<Chunk>> allChunks = new ArrayList<ArrayList<Chunk>>();

	boolean gameOver = false;

	Character[] characters = new Character[2];

	Timer timer;
	Image background;

	GameEngine() {
		addKeyListener(new KeyP());
		setFocusable(true);
		requestFocusInWindow();
		setPreferredSize(new Dimension(WIDTH, HEIGHT));

		characters[0] = new PossessedMonk(500, 200);
		characters[1] = new PossessedMonk(50, 200);

		characters[0].setDirection(-1);
		characters[1].setDirection(1);

		allChunks.add(new ArrayList<Chunk>());
		allChunks.add(new ArrayList<Chunk>());

		background = new ImageIcon(getClass().getResource("/Images/background.jpeg")).getImage();
		timer = new Timer(250, this);
		timer.start();
	}

	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2D = (Graphics2D) g;

		g2D.drawImage(background, 0, 0, null);
		g2D.setFont(new Font("Ink free", Font.BOLD, 20));
		g2D.setColor(Color.red);
		g2D.drawString("▊▊▊▊▊▊▊▊▊▊", 5, 20);

		for (int i = 0; i < characters.length; i++) {
			if (characters[i].getDirection() == 1) {
				g2D.drawImage(characters[i].currentImage, characters[i].getLocation()[0],
						characters[i].getLocation()[1], null);
			} else {
				try {
					int width = characters[i].currentImage.getWidth(getFocusCycleRootAncestor());
					int height = characters[i].currentImage.getHeight(getFocusCycleRootAncestor());
					g2D.drawImage(characters[i].currentImage, characters[i].getLocation()[0] + width,
							characters[i].getLocation()[1], -width, height, null);
				} catch (Exception e) {
					System.out.println("Not loaded");
				}
			}
			if (characters[i].getAttackHitbox() != null) {
				paintHitBox(g2D, characters[i].getAttackHitbox());
			}
			if (characters[i].getBodyHitbox() != null) {
				paintHitBox(g2D, characters[i].getBodyHitbox());
			}
		}
	}

	public void paintHitBox(Graphics2D g2D, Rectangle bounds) {
		g2D.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		executeNextChunks();
		repaint();
	}

	public void executeNextChunks() {

		for (int i = 0; i < 2; i++) {
			if (allChunks.get(i).size() == 0) {
				this.addChunks(i, characters[i].idle());
			}

			Chunk currentChunk = allChunks.get(i).get(0);

			characters[i].setCurrentImage(currentChunk.getImage());
			if (currentChunk.getMovement() != null) {
				Rectangle bodyHitBox = characters[i].getBodyHitbox();
				if (bodyHitBox.x + bodyHitBox.width <= WIDTH && bodyHitBox.x >= 0
						&& bodyHitBox.y + bodyHitBox.height <= HEIGHT && bodyHitBox.x >= 0) {
					characters[i]
							.setLocation(new int[] { characters[i].getLocation()[0] + currentChunk.getMovement()[0],
									characters[i].getLocation()[1] + currentChunk.getMovement()[1] });
					if (currentChunk.getBodyHitbox() != null) {
						characters[i].setBodyHitbox(currentChunk.getBodyHitbox());
					}
				}
			}

			if (currentChunk.getAttackHitbox() != null) {
				System.out.println("eentra");
				characters[i].setAttackHitbox(currentChunk.getAttackHitbox());
			}
			if (currentChunk.getSound() != null) {
				sound(currentChunk.getSound());
			} else {
				characters[i].setAttackHitbox(null);
			}
		}

		for (int i = 0; i < 2; i++) {
			Chunk currentChunk = allChunks.get(i).get(0);
			Chunk otherChunk = allChunks.get((i + 1) % 2).get(0);
			if (currentChunk.getDamage() != 0) {
				if (currentChunk.getAttackHitbox().intersects(otherChunk.getBodyHitbox())) {
					characters[i + 1 % 2].setHp(characters[i + 1 % 2].getHp() - currentChunk.getDamage());
				}
			}
		}

		allChunks.get(0).remove(0);
		allChunks.get(1).remove(0);

		if (characters[0].getHp() <= 0) {
			this.gameOver = true;
		}

		if (characters[1].getHp() <= 0) {
			this.gameOver = true;
		}
	}

	public void addChunks(int index, Chunk[] chunks) {
		for (int i = 0; i < chunks.length; i++) {
			allChunks.get(index).add(chunks[i]);
		}
	}

	public void gameOver() {
		gameOver = true;
		timer.stop();
		repaint();
	}

	public void sound(String path) {
		try {
			AudioInputStream audioInputStream = AudioSystem
					.getAudioInputStream(this.getClass().getResource("/Sounds" + path));
			Clip clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			clip.start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public class KeyP extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			super.keyPressed(e);

			switch (e.getKeyCode()) {
			case KeyEvent.VK_UP:
				addChunks(0, characters[0].jump());
				break;
			case KeyEvent.VK_LEFT:
				addChunks(0, characters[0].move(Directions.Left));
				break;
			case KeyEvent.VK_RIGHT:
				addChunks(0, characters[0].move(Directions.Right));
				break;
			case KeyEvent.VK_DOWN:
				addChunks(0, characters[0].down());
				break;
			case KeyEvent.VK_MINUS:
				addChunks(0, characters[0].ability1());
				break;
			case KeyEvent.VK_PLUS:
				addChunks(0, characters[0].ability2());
				break;
			case KeyEvent.VK_ENTER:
				addChunks(0, characters[0].ability3());
				break;
			case KeyEvent.VK_A:
				addChunks(1, characters[1].move(Directions.Left));
				break;
			case KeyEvent.VK_D:
				addChunks(1, characters[1].move(Directions.Right));
				break;
			case KeyEvent.VK_W:
				addChunks(1, characters[1].jump());
				break;
			case KeyEvent.VK_S:
				addChunks(1, characters[1].down());
				break;
			case KeyEvent.VK_SPACE:
				addChunks(1, characters[1].ability1());
				break;
			case KeyEvent.VK_E:
				addChunks(1, characters[1].ability2());
				break;
			case KeyEvent.VK_SHIFT:
				addChunks(1, characters[1].ability3());
				break;
			}
		}
	}
}
