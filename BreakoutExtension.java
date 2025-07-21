
/*
 * File: GraphicsHierarchy.java
 * ----------------------------
 * This program is a stub for the GraphicsHierarchy problem, which
 * draws a partial diagram of the acm.graphics hierarchy.
 */

import acm.graphics.*;
import acm.program.*;
import acm.util.*;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class BreakoutExtension extends GraphicsProgram {

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

	/** time in millisecond required to make game visible for users */
	private static final int DELAY = 8;

	/** Y offset from mid point */
	private static final int SCORE_Y_OFFSET = 100;

	/** offset from win-lose label */
	private static final int SCORE_LABEL_OFFSET = 270;

	/** Surprise needs to fit in brick perfectly */
	private static final int SURPRISE_DIAMETER = PADDLE_HEIGHT;

	/** Diameter of bullet, which is launched from paddle */
	private static final int BULLET_DIAMETER = 10;

	/** Total Number of bricks, which can change but won't be created again */
	private static int numOfBricks = NBRICKS_PER_ROW * NBRICK_ROWS;

	/** Total Number of turns, which can change but won't be created again */
	private static int totalAttempts = NTURNS;

	/** score of user, which can change but won't be created again */
	private static int score = 0;

	/** width of paddle. surprise sometimes changes it */
	private static int newWidthPaddle = PADDLE_WIDTH;

	/** game starts with default WHITE color, but it may change */
	private static Color currentBackgroundColor = Color.WHITE;

	/** tracks extraPoints label so it is removed after specific time */
	private static long lastTimeExtraPointsShown = 0;

	/** tracks time when the last ball was launched */
	private static long lastTimeBulletsShown = 0;

	/** tracks noBulletsLabel so it is removed after specific time */
	private static long lastTimenoBulletsLabelShown = 0;

	/** tracks addBulletsShown so it is removed after specific time */
	private static long lastTimeaddBulletsShown = 0;

	/** Player has total of 3 bullets in game */
	private static int totalBullets = 3;

	private RandomGenerator rgen = RandomGenerator.getInstance();

	// variable instances
	private GRect paddle;
	private GOval ball, surprise, bullet;
	private double vx, vy; // speed of ball
	private int check; // monitors surprise-related choices
	// surprise-related labels
	private GLabel extraPointsLabel, scoreLabel, noBulletsLabel, addBulletsLabel;

	public void run() {
		setUpGame();
		gameInProcess();
	}

	public void setUpGame() {
		welcomeUsers();
		initializeGame();
		explainRules();
		addKeyListeners();
	}

	private void welcomeUsers() {
		introAudio();
		Color welcomeColor = new Color(200, 230, 250);
		setBackground(welcomeColor);
		GLabel welcome = labels("WELCOME TO BREAKOUT", Color.BLUE, 30, WIDTH, HEIGHT);
		pause(2000);
		remove(welcome);
		setBackground(Color.WHITE);
	}

	private void introAudio() {
		AudioClip introClip = MediaTools.loadAudioClip("intro.au");
		introClip.play();
	}

	private void explainRules() {
		String[] introLabel = { "HERE IS THE BOARD", "YOU HAVE 3 LIVES", "CYAN BRICKS ARE 1 POINT",
				"GREEN BRICKS ARE 2 POINTS", "YELLOW BRICKS ARE 4 POINTS", "ORANGE BRICKS ARE 8 POINTS",
				"RED BRICKS ARE 16 POINTS", "BRICKS MAY HAVE SURPRISES", "YOU ALSO HAVE 3 BULLETS",
				"LAUNCH THEM MY SPACE BAR", "ARE YOU READY ?" };
		for (int i = 0; i < introLabel.length; i++) {
			depictLabel(introLabel[i]);
		}
	}

	private void depictLabel(String label) {
		GLabel l = labels(label, Color.BLUE, 25, WIDTH, HEIGHT);
		pause(1500);
		remove(l);
	}

	private void initializeGame() {
		buildBricks();
		buildPaddle();
	}

	private void buildBricks() {
		for (int i = 0; i < NBRICK_ROWS; i++) {
			for (int a = 0; a < NBRICKS_PER_ROW; a++) {
				GRect brick = new GRect(BRICK_WIDTH, BRICK_HEIGHT);
				double initialX = (WIDTH - (NBRICKS_PER_ROW * BRICK_WIDTH + (NBRICKS_PER_ROW - 1) * BRICK_SEP)) / 2;
				double x = initialX + a * (BRICK_WIDTH + BRICK_SEP);
				double y = BRICK_Y_OFFSET + (BRICK_SEP + BRICK_HEIGHT) * i;
				brick.setFilled(true);
				brick.setColor(color(i));
				add(brick, x, y);
			}
		}

	}

	/*
	 * method ensures that if number of rows change, set up will be still same,
	 * as same color sequence will be repeated, reminders are always gonna be
	 * between 0-9
	 */
	private Color color(int i) {
		if (i % 10 == 0 || i % 10 == 1) {
			return Color.RED;
		} else if (i % 10 == 2 || i % 10 == 3) {
			return Color.ORANGE;
		} else if (i % 10 == 4 || i % 10 == 5) {
			return Color.YELLOW;
		} else if (i % 10 == 6 || i % 10 == 7) {
			return Color.GREEN;
		} else { // i%10==8 || i%10==9 (in this case)
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

	/*
	 * when paddle width changes because of surprise, it should be treated
	 * differently. So size will adjust and ensure that new paddle still stays
	 * between boarders ( that's why newWidthPaddle is used)
	 */
	public void mouseMoved(MouseEvent mouse) {
		double mouseX = mouse.getX();
		double paddleX = mouseX - newWidthPaddle / 2;
		double paddleY = paddle.getY(); // doesn't change
		// making sure paddle does't go over walls
		if (paddleX < 0) {
			paddleX = 0; // doesn't let paddle go through left wall
		} else if (paddleX > WIDTH - newWidthPaddle) {
			paddleX = WIDTH - newWidthPaddle; // doesn't let paddle go through
												// right wall
		}
		paddle.setLocation(paddleX, paddleY);
	}

	private void gameInProcess() {
		// makes sure ball does not drop when player isn't ready
		tellUserToStart(); 
		for (int i = 0; i < NTURNS; i++) {
			if (!gameOver()) { // if player did not win or lost already
				showTotalAttempts();
				initializeBall();
				moveObjects(); // ball, surprise and bullet(in this case)
			} else {
				break;
			}
		}
	}

	private void tellUserToStart() {
		GLabel start = labels("Click to Start the Game", Color.BLUE, 30, WIDTH, HEIGHT);
		waitForClick();
		remove(start);
	}

	private boolean gameOver() {
		return numOfBricks == 0 || totalAttempts == 0;
	}

	private void showTotalAttempts() {
		GLabel turns = labels("Attemps left : " + totalAttempts, Color.RED, 25, WIDTH, BRICK_Y_OFFSET);
		pause(1000);
		remove(turns);
	}

	private void initializeBall() {
		ball = new GOval(BALL_DIAMETER, BALL_DIAMETER);
		double x = (WIDTH - BALL_DIAMETER) / 2;
		double y = (HEIGHT - BALL_DIAMETER) / 2;
		ball.setFilled(true);
		add(ball, x, y);
	}

	private void moveObjects() {
		generateSpeed();
		while (!gameOver()) {
			ball.move(vx, vy);
			bounceFromWalls();
			checkForLabelRemoval(noBulletsLabel,lastTimenoBulletsLabelShown, 1500);
			checkForLabelRemoval(addBulletsLabel,lastTimeaddBulletsShown, 1000);
			if (ball.getY() >= HEIGHT - BALL_DIAMETER) {
				totalAttempts--;
				lose();
				return;
			}
			depictScore();
			launchBullet();
			operateCollision();
			dropSurprise();
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
			handleSticking();
		}
		if (ball.getY() <= 0) {
			vy = -vy; // bounce from top wall
		}
	}
	
	/*
	 * this works until game is not over 
	 */
	public void keyPressed(KeyEvent e) {
		//if pressed "space bar"
		if (e.getKeyCode() == KeyEvent.VK_SPACE && !gameOver()) {
			// makes sure bullets are not launched in a row.
			// user can launch bullets after some specific time
			if (System.currentTimeMillis() - lastTimeBulletsShown >= 2000 && totalBullets != 0) {
				initializeBullet();
				totalBullets--;
			} else if (totalBullets == 0 && noBulletsLabel == null) {
				noBulletsLabel = labels("No bullets left", Color.ORANGE, 20, WIDTH, getHeight());
				lastTimenoBulletsLabelShown = System.currentTimeMillis();
			}
		}
	}

	private void initializeBullet() {
		lastTimeBulletsShown = System.currentTimeMillis();
		bullet = new GOval(BULLET_DIAMETER, BULLET_DIAMETER);
		bullet.setFilled(true);
		bullet.setColor(Color.ORANGE);
		double x=(paddle.getX() + newWidthPaddle / 2) - BULLET_DIAMETER / 2;
		double y=paddle.getY() - BULLET_DIAMETER;
		add(bullet, x, y);
	}

	/*
	 * Bullets in breakout might have great essentiality, as sometimes it gets
	 * hard to break specific brick, if it is located on the edge. Bullets
	 * enable user to launch them by pressing "space bar", which will demolish
	 * brick if aimed correctly.(makes game more fun)
	 */
	private void moveBullet() {
		if (bullet != null) {
			shootAudio();
			bullet.move(0, -5);
			GObject object = getElementAt(bullet.getX(), bullet.getY());
			// demolish just bricks
			if (object != null && object instanceof GRect) { 
				remove(object);
				BounceAudio();
				numOfBricks--;
				operateScore(object);
				remove(bullet);
				bullet = null;
				checkForWin();
				createSurprise(object);
			} else if (bullet.getY() + BULLET_DIAMETER < 0) {
				// make sure our program does not overload
				// with unnecessary stuff
				remove(bullet);
				bullet = null;
			}
		}
	}

	private void shootAudio() {
		AudioClip lostBallClip = MediaTools.loadAudioClip("shoot.au");
		lostBallClip.play();
	}

	public void launchBullet() {
		if (bullet != null) {
			moveBullet();
		}
	}

	public void depictScore() {
		if (scoreLabel != null) {
			remove(scoreLabel);
		}
		scoreLabel = labels("SCORE: " + score, Color.BLUE, 20, WIDTH + SCORE_LABEL_OFFSET, SCORE_LABEL_OFFSET / 2);
	}

	/*
	 * This method drops surprises from bricks with random speed, if user
	 * catches them with paddle they will receive gift, if they don't, they will
	 * go away
	 */
	private void dropSurprise() {
		if (surprise != null) { // it may not have been created
			double surpriseVy = rgen.nextDouble(2, 3);
			surprise.move(0, surpriseVy);
			if (paddle.contains(surprise.getX(), surprise.getY())) {
				remove(surprise);
				generateGift();
				surprise = null;
			} else if (surprise.getY() + SURPRISE_DIAMETER >= getHeight()) {
				remove(surprise);
				surprise = null;
			}
		}
	}

	private void generateGift() {
		int choice = rgen.nextInt(1, 5);
		choice = checkChoice(choice);
		checkBackground();
		if (choice == 1) {
			paddleBecomesBig();
		} else if (choice == 2) {
			paddleBecomesSmall();
		} else if (choice == 3) {
			backgroundGetsDark();
		} else if (choice == 4) {
			receivedExtraPoints();
		} else if (choice == 5) {
			receivedExtraBullet();
		}
	}
	
	/*
	 * If choice is still the same, it will change to ensure the game does
	 * not stay static
	 */
	private int checkChoice(int choice) {
		while (true) { // makes sure same choice is not repeated
			if (choice == check) {
				choice = rgen.nextInt(1, 5);
			} else {
				break;
			}
		}
		check = choice;
		return choice;
	}
	
	private void checkBackground() {
		if (currentBackgroundColor != Color.WHITE) {
			setBackground(Color.WHITE);
			currentBackgroundColor = Color.WHITE;
		}
	}
	
	/*
	 * being big is not always so good, as ball might move faster,
 	 * which makes game more fun
	 */
	private void paddleBecomesBig(){
		paddle.setSize(2 * PADDLE_WIDTH, PADDLE_HEIGHT);
		newWidthPaddle = 2 * PADDLE_WIDTH;
	}
	
	private void paddleBecomesSmall(){
		paddle.setSize(PADDLE_WIDTH / 2, PADDLE_HEIGHT);
		newWidthPaddle = PADDLE_WIDTH / 2;
	}
	
	private void backgroundGetsDark(){
		Color hardlyVisible = new Color(27, 27, 27);
		currentBackgroundColor = hardlyVisible;
		setBackground(hardlyVisible);
	}
	
	private void receivedExtraPoints(){
		score += 10;
		extraPointsLabel = labels("+ 10 points", Color.GREEN, 30, WIDTH, getHeight() + SCORE_Y_OFFSET);
		lastTimeExtraPointsShown = System.currentTimeMillis();
	}
	
	private void receivedExtraBullet(){
		if (totalBullets < 3) { // makes sure bullets don't exceed 3
			totalBullets++;
			lastTimeaddBulletsShown = System.currentTimeMillis();
			addBulletsLabel = labels("+ 1 bullet", Color.GREEN, 30, WIDTH, getHeight() + SCORE_Y_OFFSET);
		}
	}

	/*
	 * Sometimes, when ball bounced from the edge of the paddle, ball was
	 * receiving huge speed, and as right and left walls were close, ball could
	 * stick to them. So this method handles that.
	 */
	private void handleSticking() {
		if (ball.getX() >= WIDTH - BALL_DIAMETER) {
			ball.move(-1, 0); // Move left if stuck on the right wall
		} else if (ball.getX() <= 0) {
			ball.move(1, 0); // Move right if stuck on the left wall
		}
	}

	private void lose() {
		if (totalAttempts == 0) {
			removeAll();
			setBackground(Color.RED);
			labels("YOU LOST", Color.PINK, 40, WIDTH, HEIGHT);
			lostAudio();
			pause(1000);
			labels("score : " + score, Color.PINK, 20, WIDTH, HEIGHT + SCORE_Y_OFFSET);
		} else {
			remove(ball);
			removeExtraObjects();
			lostBallAudio();
			setBackToNormal();
			GLabel lostBallLabel = labels("YOU LOST A BALL", Color.RED, 20, WIDTH, HEIGHT);
			pause(1500);
			remove(lostBallLabel);
		}
	}

	/*
	 * All audios were used from free Website called Freesound in order to avoid
	 * plagiarism
	 */
	private void lostAudio() {
		AudioClip lostClip = MediaTools.loadAudioClip("lost.au");
		lostClip.play();
	}

	private void lostBallAudio() {
		AudioClip lostBallClip = MediaTools.loadAudioClip("lostBall.au");
		lostBallClip.play();
	}

	private void removeExtraObjects() {
		if (surprise != null) {
			remove(surprise);
		}
		if (noBulletsLabel != null) {
			remove(noBulletsLabel);
			noBulletsLabel = null;
		}
		if (bullet != null) {
			remove(bullet);
			bullet = null;
		}
	}

	/*
	 * when user loses ball, almost everything should be renewed
	 */
	private void setBackToNormal() {
		paddle.setSize(PADDLE_WIDTH, PADDLE_HEIGHT);
		newWidthPaddle = PADDLE_WIDTH;
		setBackground(Color.WHITE);
	}

	private void operateCollision() {
		checkForLabelRemoval(extraPointsLabel,lastTimeExtraPointsShown, 1000);
		GObject collider = getCollidingObject();
		if (collider == paddle) {
			bounceFromPaddle();
		} else if (collider != null && collider instanceof GRect) {
			vy = -vy;
			numOfBricks--;
			operateScore(collider);
			BounceAudio();
			remove(collider);
			createSurprise(collider);
			checkForWin();
		}
	}
	
	private void checkForWin(){
		if (numOfBricks == 0) {
			removeAll();
			winAnimations();
		}
	}
	
	/*
	 * This method checks time of labels appearance,
	 * which ensures they do not stay on screen for long time
	 */
	private void checkForLabelRemoval(GLabel label,long lastTimeAppearance, int time){
		if (label != null) {
			long currentTime = System.currentTimeMillis();
			if (currentTime - lastTimeAppearance >= time) {
				remove(label);
				makeLabelNull(label);
			}
		}
	}
	
	/*
	 * label becoming null in checkForLabelRemoval would not affect
	 * instance variables, so separate method needed to be created
	 * to make instance variables null
	 */
	private void makeLabelNull(GLabel label){
		if (label == noBulletsLabel) {
            noBulletsLabel = null; 
        } else if (label == addBulletsLabel){
        	addBulletsLabel = null; 
        }  else if (label == extraPointsLabel){
        	extraPointsLabel = null;
        }
	}
	
	/*
	 * This method decides with the probability of 0.1 if surprise should be
	 * created or not ( surprises should be random, as game is one too).
	 */
	private void createSurprise(GObject collider) {
		// if surprise has not finished moving, other
		// surprise should not be created
		if (surprise == null && rgen.nextBoolean(0.1)) {
			surprise = new GOval(SURPRISE_DIAMETER, SURPRISE_DIAMETER);
			surprise.setFilled(true);
			surprise.setColor(Color.MAGENTA);
			double x = collider.getX() + BRICK_WIDTH / 2 - SURPRISE_DIAMETER / 2;
			double y = collider.getY();
			add(surprise, x, y);
		}
	}

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

	/*
	 * This method ensures that game becomes more fun Vy will change a direction
	 * as expected BUT Vx(direction and speed) will change too depending how you
	 * position a paddle when ball is about to hit it
	 */
	private void bounceFromPaddle() {
		vy = -Math.abs(vy);
		double middlePointOfPaddle = paddle.getX() + PADDLE_WIDTH / 2;
		double rightPointOfBall = ball.getX() + BALL_DIAMETER;
		double difference = rightPointOfBall - middlePointOfPaddle;
		// DELAY makes vx a bit slower, since difference might be huge
		vx = (difference) / DELAY;
	}

	private void BounceAudio() {
		AudioClip bounceClip = MediaTools.loadAudioClip("brick.au");
		bounceClip.play();
	}

	/*
	 * Calculates score depending on brick color. It checks bricks of all colors
	 * because ball might hit couple of bricks at the same time
	 */
	private void operateScore(GObject collider) {
		Color colorOfBricks[] = { Color.CYAN, Color.GREEN, Color.YELLOW, Color.ORANGE, Color.RED };
		int numOfPoints[] = { 1, 2, 4, 8, 16 };
		for (int i = 0; i < colorOfBricks.length; i++) {
			scoreCount(collider, colorOfBricks[i], numOfPoints[i]);
		}
	}

	private void scoreCount(GObject collider, Color color, int point) {
		if (collider.getColor() == color) {
			score += point;
		}
	}

	private void winAnimations() {
		Color lightGreen = new Color(144, 238, 144);
		setBackground(lightGreen);
		victoryAudio();
		long timeOfColorChange = System.currentTimeMillis();
		while (true) {
			Color color = rgen.nextColor();
			GLabel won = labels("YOU WON", color, 40, WIDTH, HEIGHT);
			pause(20);
			GLabel labelForscore = labels("score : " + score, color, 20, WIDTH, HEIGHT + SCORE_Y_OFFSET);
			// changing color animations will end after 4 seconds
			if (System.currentTimeMillis()- timeOfColorChange > 4000) {
				won.setColor(Color.WHITE);
				labelForscore.setColor(Color.WHITE);
				return;
			}

		}
	}

	private void victoryAudio() {
		AudioClip winClip = MediaTools.loadAudioClip("win.au");
		winClip.play();
	}

	private GLabel labels(String string, Color color, int num, int a, int b) {
		GLabel label = new GLabel(string);
		Font font = new Font("Serif", Font.BOLD, num);
		label.setFont(font);
		label.setColor(color);
		double x = (a - label.getWidth()) / 2;
		double y = (b - label.getAscent()) / 2;
		add(label, x, y);
		return label;
	}

}