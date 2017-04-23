package com.pennypop.project;

import java.util.ArrayList;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entries;
import com.badlogic.gdx.utils.OrderedMap;

/**
 * This is where you screen code will go, any UI should be in here
 * 
 * @author Richard Taylor
 */
public class MainScreen implements Screen {
	
	private final Stage stage;
	private final SpriteBatch spriteBatch;
	private ProjectApplication app;
	
	private SpriteBatch batch;
    private BitmapFont font;
    
    private ArrayList<Text> textList;
	
	public MainScreen(ProjectApplication _app) {
		spriteBatch = new SpriteBatch();
		
		app = _app;
		stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false, spriteBatch);
		
		batch = new SpriteBatch();    
        font = new BitmapFont(Gdx.files.local("assets/font.fnt"), false);

        
        
        Image sfx = GameScreen.createImg("assets/sfxButton.png");
        sfx.setPosition(300, Gdx.graphics.getHeight()/2-50);
        sfx.addListener(new ClickListener() {
        	public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
		    {
	        	Gdx.audio.newSound(Gdx.files.internal("assets/button_click.wav")).play();
	        	return true;
		    }
        });
        stage.addActor(sfx);
        
        Image apiBtn = GameScreen.createImg("assets/apiButton.png");
        apiBtn.setPosition(420, Gdx.graphics.getHeight()/2-50);
        apiBtn.addListener(new ClickListener() {
        	public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
		    {
        		String url = "http://api.openweathermap.org/data/2.5/weather?q=San%20Francisco,US&appid=2e32d2b4b825464ec8c677a49531e9ae";
	        	sendRequest(url);
        		return true;
		    }
        });
        stage.addActor(apiBtn);
        
        Image startGame = GameScreen.createImg("assets/gameButton.png");
        startGame.setPosition(540, Gdx.graphics.getHeight()/2-50);
        startGame.addListener(new ClickListener() {
        	public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
		    {
        		app.switchScreen();
        		return true;
		    }
        });
        stage.addActor(startGame);

        //Text title = ;
        stage.addActor(new Text(font, 400, 500, "PennyPop", Color.RED, 1));
	}

	@Override
	public void dispose() {
		batch.dispose();
        font.dispose();
        
		spriteBatch.dispose();
		stage.dispose();
	}

	@Override
	public void render(float delta) {
    
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
	
	void sendRequest(String url) {
		HttpRequest httpGet = new HttpRequest(HttpMethods.GET);
		httpGet.setUrl(url);
		Gdx.net.sendHttpRequest (httpGet, new HttpResponseListener() {
	        public void handleHttpResponse(HttpResponse httpResponse) {
                //not remove old actors for new data! repeatly click the button could
	        	//cause a mess
	        	
	        	
                //parse the response JSON
	            JsonReader json = new JsonReader();
	            OrderedMap res = (OrderedMap) json.parse(httpResponse.getResultAsString());
	            String city = (String) res.get("name");

	            Array<Object> info = (Array<Object>) res.get("weather");
	            String weather = (String) ((ObjectMap) info.items[0]).get("description");
	            
	            Float windSpd = (Float) ((ObjectMap) res.get("wind")).get("speed");
	            Float windDeg = (Float) ((ObjectMap) res.get("wind")).get("deg");
	            
	            int height = Gdx.graphics.getHeight()-250;
	            stage.addActor(new Text(font, 700, height, "Current weather", new Color(0.5f, 0, 0, 1), 1));
	            stage.addActor(new Text(font, 700, height-50, city, Color.BLUE, 1));
	            stage.addActor(new Text(font, 700, height-120, weather, Color.RED, 1));
	            stage.addActor(new Text(font, 870, height-170, windSpd.toString()+"mph Wind", Color.RED, 0.6f));
	            stage.addActor(new Text(font, 700, height-170, windDeg.toString()+" degrees,", Color.RED, 0.6f));
	            
	        }
	 
	        public void failed(Throwable t) {
	            System.out.println("request fail");
	        }
	 });
	}

}
