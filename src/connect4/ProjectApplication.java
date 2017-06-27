package connect4;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.GL20;



public class ProjectApplication implements ApplicationListener {

	private Screen screen;
	private Screen game;
	private Screen display;

	public static void main(String[] args) {
		new LwjglApplication(new ProjectApplication(), "Connect Four", 1280, 720, true);
	}

	@Override
	public void create() {

		
		screen = new MainScreen(this);
		game = new GameScreen(this);

		display = screen;
		display.show();
	}

	@Override
	public void dispose() {

		screen.hide();
		screen.dispose();
		game.hide();
		game.dispose();
	}

	@Override
	public void pause() {
		screen.pause();
		game.pause();
	}

	@Override
	public void render() {
		clearWhite();
		display.render(Gdx.graphics.getDeltaTime());
	}

	/** Clears the screen with a white color */
	private void clearWhite() {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	}

	@Override
	public void resize(int width, int height) {
		screen.resize(width, height);
	}

	@Override
	public void resume() {
		screen.resume();
	}

	public void switchScreen() {
		// TODO Auto-generated method stub
		display.hide();
		if(display == game) {
			display = screen;
			game.dispose();
			//new game
			game = new GameScreen(this);
		}
		else
			display = game;
		display.show();
		render();
	}



}
