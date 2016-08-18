package com.majalis.traprpg;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;

public abstract class AbstractScreen extends Stage implements Screen {
	
	private static final int winWidth = 640;
	private static final int winHeight = 800;
	protected SpriteBatch batch;
	protected BitmapFont font;
	
    protected AbstractScreen() {
        super( new FitViewport(winWidth, winHeight, new OrthographicCamera()) );
    }
 
    // Subclasses must load actors in this method
    public abstract void buildStage();
 
    @Override
    public void render(float delta) {
        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 
        // Calling to Stage methods
        super.act(delta);
        super.draw();
    }
 
    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
        batch = new SpriteBatch();
        font = new BitmapFont();
    }
 
    @Override
    public void resize(int width, int height) {
        getViewport().update(width, height, true);
    }
 
    @Override public void hide() {
    	batch.dispose();
    	font.dispose();
    }
    @Override public void pause() {}
    @Override public void resume() {}
}