package com.pennypop.project;

import java.util.Timer;
import java.util.TimerTask;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * This is where you screen code will go, any UI should be in here
 * 
 * @author Richard Taylor
 */
public class GameScreen implements Screen {
	
	private final Stage stage;
	private final SpriteBatch spriteBatch;
	
	private int row = 6;
	private int col = 7;
	private int goal = 4;
	
	//for display images
	private Table grids;
	//for store images
	private Image[][] imgs;
	private Pixmap pixmap;
	private Skin skin;
	private BitmapFont font;
	private ProjectApplication app;
	
	public GameScreen(ProjectApplication _app) {
		spriteBatch = new SpriteBatch();
		stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false, spriteBatch);
		
		app = _app;
		final Connect4 game = new Connect4(row, col, goal, this);
		imgs = new Image[row][col];
		skin = new Skin();
		font = new BitmapFont();
		
		pixmap = new Pixmap( 1, 1, Format.RGBA8888 );
		pixmap.setColor( Color.GRAY );
		pixmap.fill();
		skin.add("light_gray", new Texture(pixmap));
		
		WindowStyle windowStyle = new WindowStyle(font, Color.GREEN, skin.getDrawable( "light_gray" ) );
		LabelStyle labelStyle = new LabelStyle(font, Color.RED);
		ButtonStyle buttonStyle = new ButtonStyle(skin.getDrawable("light_gray"), skin.getDrawable("light_gray"), null );
		 skin.add( "default", buttonStyle );
		 skin.add( "default", windowStyle );
		 skin.add( "default", labelStyle );

		

		Gdx.input.setInputProcessor(stage);
		
		grids = new Table();
		grids.debug();
		grids.setFillParent(true);
		
		for(int i = 0; i < row; i++) {
			for(int j = 0; j < col; j++) {
				Image img = createImg("");
				imgs[i][j] = img;
				
				img.addListener(new ClickListener() {
				    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
				    {
				    	//since the game border is small,
				    	//search the matrix to find the index of the image
				    	
				    	
				    	int col = getImageIdx(event.getListenerActor())[1];
				    	int[] location = game.playPiece(col);
				    	
				    	displayMove(location);
				    	
						if(location[3] == 0) {
							location = game.playPiece_AI();
							displayMove(location);
						}
						
				        return true;
				    }
				});
				
				grids.add(img).width(100).height(100);
			}
			grids.row();
		}
		
		
		
		Image img = createImg("");
		img.setPosition(50, 400);
		
		Image backMain = GameScreen.createImg("assets/back.png");
		backMain.setPosition(0, Gdx.graphics.getHeight()-100);
		backMain.addListener(new ClickListener() {
        	public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
		    {
        		app.switchScreen();
        		return true;
		    }
        });
        
        stage.addActor(backMain);
		
		stage.addActor(grids);
		stage.addActor(img);
		
	}
	
	public void displayMove(int[] location) {
		if(location[2] == 1)
    		changeImage(imgs[location[0]][location[1]], "assets/red.png");
    	else if(location[2] == 2)
    		changeImage(imgs[location[0]][location[1]], "assets/yellow.png");
		if(location[3] == 1)
			displayResult(location[2]);
	}
	
	
	public int[] getImageIdx(Actor img) {
		for(int i = 0; i < imgs.length; i++){
			for(int j = 0; j < imgs[i].length; j++) {
				if(imgs[i][j] == img)
					return new int[]{i, j};
			}
		}
		return new int[]{-1, -1};
	}
	
	
	public void changeImage(Image img, String file) {
		Texture texture = new Texture(Gdx.files.internal(file));
		img.setDrawable(new TextureRegionDrawable(new TextureRegion(texture)));
		img.setSize(100, 100);
		stage.draw();
	}
	
	public void displayResult(int winner) {
		String player = winner == 1 ? "Red" : "Yellow";
		final Dialog dialog = new Dialog( "Result", skin, "default" );
		dialog.text("\n"+player+" player wins!");
		dialog.show(stage);
		int time = 1500;
		if(winner == 1) {
			Gdx.audio.newSound(Gdx.files.internal("assets/fail.wav")).play();
			time = 2500;
		}
		else
			Gdx.audio.newSound(Gdx.files.internal("assets/tada.wav")).play();
		Timer timer = new Timer();
		timer.schedule(new TimerTask(){
		    @Override
		    public void run() {
		        dialog.hide();
		    }
		}, time);
	}
	
	public static Image createImg(String file) {
		
		Image itemImage = new Image();
		if(Gdx.files.local(file).exists()) {
			Texture texture = new Texture(Gdx.files.internal(file));
			itemImage.setDrawable(new TextureRegionDrawable(new TextureRegion(texture)));
		}
		itemImage.setSize(100, 100);
		return itemImage;
	}

	@Override
	public void dispose() {
		spriteBatch.dispose();
		stage.dispose();
	}

	@Override
	public void render(float delta) {
		Table.drawDebug(stage);
		stage.act(delta);
		stage.draw();
		
	}

	@Override
	public void resize(int width, int height) {
		stage.setViewport(width, height, false);
	}

	@Override
	public void hide() {
		Gdx.input.setInputProcessor(null);
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void pause() {
		// Irrelevant on desktop, ignore this
	}

	@Override
	public void resume() {
		// Irrelevant on desktop, ignore this
	}

}
