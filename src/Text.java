package com.pennypop.project;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Text extends Actor {

    BitmapFont font;
    int posX;
    int posY;
    String text;
    Color color;
    float size;
    
    public Text(BitmapFont _font, int x, int y, String _text, Color _color, float _size){
        font = _font;
        posX = x;
        posY = y;
        text = _text;
        color = _color;
        size = _size;
    }


    @Override
    public void draw(SpriteBatch batch, float parentAlpha) {
    	 font.setColor(color);
    	 font.setScale(size);
         font.draw(batch, text, posX, posY);
    }


}