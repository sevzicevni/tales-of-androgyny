package com.majalis.traprpg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;

public class GameScreen extends AbstractScreen {

	private boolean paused;
	private GameWorld world;
	private Skin skin = new Skin(Gdx.files.internal("uiskin.json"), new TextureAtlas(Gdx.files.internal("uiskin.atlas")));
	private Sound buttonSound;
	private String classSelection;
	
	public GameScreen(boolean loadGame) {
		world = new GameWorld(loadGame);
		paused = false;
		buttonSound = Gdx.audio.newSound(Gdx.files.internal("sound.wav"));	
		if (loadGame){
			SaveManager save = new SaveManager(false);
			String retrievedValue = save.loadDataValue("Class", String.class);
			// this is to avoid null pointer errors - this should later either create a blank save file and just begin, or the button should be disabled/hidden
			classSelection = retrievedValue != null ? retrievedValue : ""; 
			if (classSelection == null) classSelection = "";
		}
		// create a new blank save file
		else {
			classSelection = "";
			save("");
		}
	}
	
	@Override
	public void buildStage() {
		Table table = new Table();
		Array<String> classes = new Array<String>();
		classes.add("Warrior");
		classes.add("Paladin");
		classes.add("Rogue");
		classes.add("Ranger");
		classes.add("Mage");
		classes.add("Enchanter");
		for (String jobClass: classes){
			TextButton button = new TextButton(jobClass, skin);
			button.addListener(getListener(jobClass));
			table.add(button).row();
		}
        table.setFillParent(true);
        this.addActor(table);	
	}
	
	
	private ClickListener getListener(final String selection){
		return new ClickListener(){
	        @Override
	        public void clicked(InputEvent event, float x, float y) {
	        	buttonSound.play();
	        	classSelection = selection;
	        	save(selection);
	        }
	    };
	}

	private void save(String data){
		SaveManager save = new SaveManager(false);
    	save.saveDataValue("Class", data);
	}
	
	@Override
	public void render(float delta) {
		super.render(delta);
		world.gameLoop();
		if (paused!=world.paused){
			
		}
		paused = world.paused;
		if (world.gameExit){
			//game.saveManager.saveDataValue();
			ScreenManager.getInstance().showScreen(ScreenEnum.MAIN_MENU);
		}
		else if (world.gameOver){
			ScreenManager.getInstance().showScreen(ScreenEnum.GAME_OVER);
		}
		draw();
	}
	
	public void draw(){
		batch.begin();
		OrthographicCamera camera = (OrthographicCamera) getCamera();
        batch.setTransformMatrix(camera.view);
		batch.setProjectionMatrix(camera.combined);
		camera.update();
		// need to make these relative to viewport
		font.draw(batch, "Choose a class:", 1230, 900);
		font.draw(batch, classSelection, 1260, 850);
		
		if (world.displayHUD){
			font.draw(batch, "FPS: " + MathUtils.ceil(1/Gdx.graphics.getDeltaTime()), camera.position.x-200+(400), camera.position.y+220);
		}
		batch.end();
	}
	
	@Override
	public void show() {
		super.show();
	}

	@Override
	public void dispose() {
		super.dispose();
	}
}