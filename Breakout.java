
/*
 * File: Breakout.java
 * -------------------
 * Name:
 * Section Leader:
 * 
 * This file will eventually implement the game of Breakout.
 */

import acm.graphics.*;
import acm.program.*;
import acm.util.*;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class Breakout extends GraphicsProgram {

	/** Width and height of application window in pixels */
	public static final int APPLICATION_WIDTH = 400;
	public static final int APPLICATION_HEIGHT = 600;

	/** Dimensions of game board (usually the same) */
	private static final int WIDTH = APPLICATION_WIDTH;
	private static final int HEIGHT = APPLICATION_HEIGHT;

	/** Dimensions of the paddle */
	private static final int PADDLE_WIDTH = 60;
	private static final int PADDLE_HEIGHT = 10;

	/** Offset of the paddle up from the bottom */
	private static final int PADDLE_Y_OFFSET = 30;

	/** Number of bricks per row */
	private static final int NBRICKS_PER_ROW = 10;

	/** Number of rows of bricks */
	private static final int NBRICK_ROWS = 10;

	/** Separation between bricks */
	private static final int BRICK_SEP = 4;

	/** Width of a brick */
	private static final int BRICK_WIDTH = (WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

	/** Height of a brick */
	private static final int BRICK_HEIGHT = 8;

	/** Radius of the ball in pixels */
	private static final int BALL_RADIUS = 10;

	/** Diameter will be twice as much as radius */
	private static final int BALL_DIAMETER = 2 * BALL_RADIUS;

	/** Offset of the top brick row from the top */
	private static final int BRICK_Y_OFFSET = 70;

	/** Number of turns */
	private static final int NTURNS = 3;

	/** time in milliseconds required to make game visible for users */
	private static final int DELAY = 8;

	/** Total Number of bricks, which can change but won't be created again */
	private static int numOfBricks = NBRICKS_PER_ROW * NBRICK_ROWS;

	/** Total Number of turns, which can change but won't be created again */
	private static int totalAttempts = NTURNS;

	private RandomGenerator rgen = RandomGenerator.getInstance();

	// variable instances
	private GRect paddle;
	private GOval ball;
	private double vx, vy;

	// used for game set up
	public void init() {
		initializeGame();
	}

	public void run() {
		gameInProcess();
	}

	private void initializeGame() {
		buildBricks();
		buildPaddle();
	}

	private void buildBricks() {
		for (int i = 0; i < NBRICK_ROWS; i++) {
			for (int a = 0; a < NBRICKS_PER_ROW; a++) {
				GRect brick = new GRect(BRICK_WIDTH, BRICK_HEIGHT);
				// initialX is offset from left and right walls
				double initialX = (WIDTH - (NBRICKS_PER_ROW * BRICK_WIDTH + (NBRICKS_PER_ROW - 1) * BRICK_SEP)) / 2;
				double x = initialX + a * (BRICK_WIDTH + BRICK_SEP);
				double y = BRICK_Y_OFFSET + (BRICK_SEP + BRICK_HEIGHT) * i;
				brick.setFilled(true);
				brick.setColor(color(i));
				add(brick, x, y);
			}
		}

	}

	private Color color(int i) {
		if (i == 0 || i == 1) {
			return Color.RED;
		} else if (i == 2 || i == 3) {
			return Color.ORANGE;
		} else if (i == 4 || i == 5) {
			return Color.YELLOW;
		} else if (i == 6 || i == 7) {
			return Color.GREEN;
		} else { // i==8 || i==9 (in this case)
			return Color.CYAN;
		}
	}

	private void buildPaddle() {
		paddle = new GRect(PADDLE_WIDTH, PADDLE_HEIGHT);
		double x = (WIDTH - PADDLE_WIDTH) / 2;
		double y = HEIGHT - PADDLE_HEIGHT - PADDLE_Y_OFFSET;
		paddle.setFilled(true);
		add(paddle, x, y);
		addMouseListeners();
	}

	public void mouseMoved(MouseEvent mouse) {
		double mouseX = mouse.getX();
		double paddleX = mouseX - PADDLE_WIDTH / 2;
		double paddleY = paddle.getY(); // doesn't change
		// making sure paddle does't go over walls
		if (paddleX < 0) {
			paddleX = 0; // doesn't let paddle go through left wall
		} else if (paddleX > WIDTH - PADDLE_WIDTH) {
			paddleX = WIDTH - PADDLE_WIDTH; // doesn't let paddle go through
											// right wall
		}
		paddle.setLocation(paddleX, paddleY);
	}

	private void gameInProcess() {
		// makes sure ball does not drop when player isn't ready
		tellUserToStart(); 
		for (int i = 0; i < NTURNS; i++) {
			if (!gameOver()) { // if player didn't win or lose already
				initializeBall();
				moveBall();
			} else {
				break; // get out of for loop
			}
		}
	}

	private void tellUserToStart() {
		GLabel start = labels("Click to Start the Game", Color.BLUE, 30);
		waitForClick();
		remove(start);
	}

	private boolean gameOver() {
		return numOfBricks == 0 || totalAttempts == 0;
	}

	private void initializeBall() {
		ball = new GOval(BALL_DIAMETER, BALL_DIAMETER);
		double x = (WIDTH - BALL_DIAMETER) / 2;
		double y = (HEIGHT - BALL_DIAMETER) / 2;
		ball.setFilled(true);
		add(ball, x, y);
	}

	private void moveBall() {
		generateSpeed();
		while (!gameOver()) {
			ball.move(vx, vy);
			bounceFromWalls();
			if (ball.getY() >= HEIGHT - BALL_DIAMETER) {
				totalAttempts--;
				lose();
				return;
			}
			operateCollision();
			pause(DELAY); // makes game visible for players
		}
	}

	private void generateSpeed() {
		vx = rgen.nextDouble(1.0, 3.0);
		if (rgen.nextBoolean(0.5)) {
			vx = -vx;
		}
		vy = 3.0;
	}

	private void bounceFromWalls() {
		if (ball.getX() >= WIDTH - BALL_DIAMETER || ball.getX() <= 0) {
			vx = -vx; // bounce from right and left walls
		}
		if (ball.getY() <= 0) {
			vy = -vy; // bounce from top wall
		}
	}

	private void lose() {
		if (totalAttempts == 0) {
			removeAll();
			labels("YOU LOST", Color.RED, 40);
		} else { // attempts are still left
			remove(ball);
			GLabel lostBallLabel = labels("YOU LOST A BALL", Color.RED, 20);
			pause(1500);
			remove(lostBallLabel);
		}
	}

	private void operateCollision() {
		GObject collider = getCollidingObject();
		if (collider == paddle) {
			vy = -Math.abs(vy);
		} else if (collider != null) { // collider == bricks
			vy = -vy;
			numOfBricks--;
			remove(collider);
			if (numOfBricks == 0) {
				removeAll();
				labels("YOU WON", Color.GREEN, 40);
			}
		}
	}

	/*
	 * This method returns objects which collided with ball or returns
	 * null, if nothing touched ball. we have 4 points so we check them
	 * all not just one to track every hit
	 */
	private GObject getCollidingObject() {
		double x = ball.getX();
		double y = ball.getY();
		GObject topLeft = getElementAt(x, y);
		if (topLeft != null) {
			return topLeft;
		}
		GObject topRight = getElementAt(x + BALL_DIAMETER, y);
		if (topRight != null) {
			return topRight;
		}
		GObject downRight = getElementAt(x + BALL_DIAMETER, y + BALL_DIAMETER);
		if (downRight != null) {
			return downRight;
		}
		GObject downLeft = getElementAt(x, y + BALL_DIAMETER);
		if (downLeft != null) {
			return downLeft;
		}
		return null; // if ball did not touch anything
	}

	private GLabel labels(String string, Color color, int num) {
		GLabel label = new GLabel(string);
		Font font = new Font("Serif", Font.BOLD, num);
		label.setFont(font);
		label.setColor(color);
		double x = (WIDTH - label.getWidth()) / 2;
		double y = (HEIGHT - label.getAscent()) / 2;
		add(label, x, y);
		return label;
	}

}
