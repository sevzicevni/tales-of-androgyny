package com.majalis.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ObjectMap;
import com.majalis.save.LoadService;
import com.majalis.save.SaveEnum;
import com.majalis.world.GameWorld;
/*
 * The screen that displays the world map.  UI that Handles player input while on the world map - will delegate to other screens depending on the gameWorld state.
 */
public class GameScreen extends AbstractScreen {

	private final AssetManager assetManager;
	private final GameWorld world;
	private final Texture food;
	private final Texture cloud;
	private final int foodAmount;
	public static final ObjectMap<String, Class<?>> resourceRequirements = new ObjectMap<String, Class<?>>();
	static {
		resourceRequirements.put("uiskin.json", Skin.class);
		resourceRequirements.put("node_sound.wav", Sound.class);
		resourceRequirements.put("TinySprite0.png", Texture.class);
		resourceRequirements.put("MountainU.png", Texture.class);
		resourceRequirements.put("ForestU.png", Texture.class);
		resourceRequirements.put("MountainV.png", Texture.class);
		resourceRequirements.put("ForestV.png", Texture.class);
		resourceRequirements.put("Apple.png", Texture.class);
		resourceRequirements.put("Meat.png", Texture.class);
		resourceRequirements.put("Cloud.png", Texture.class);
	}
	public GameScreen(ScreenFactory factory, ScreenElements elements, AssetManager assetManager, LoadService loadService, GameWorld world) {
		super(factory, elements);
		this.assetManager = assetManager;
		int arb = loadService.loadDataValue(SaveEnum.NODE_CODE, Integer.class);
		food = arb % 2 == 0 ? assetManager.get("Apple.png", Texture.class) : assetManager.get("Meat.png", Texture.class);
		foodAmount = loadService.loadDataValue(SaveEnum.FOOD, Integer.class);
		cloud = assetManager.get("Cloud.png", Texture.class);
		Vector3 initialTranslation = loadService.loadDataValue(SaveEnum.CAMERA_POS, Vector3.class);		
		initialTranslation = new Vector3(initialTranslation);
		OrthographicCamera camera = (OrthographicCamera) getCamera();
		initialTranslation.x -= camera.position.x;
		initialTranslation.y -= camera.position.y;
		camera.translate(initialTranslation);
		camera.update();
		this.world = world;
		red = .137f;
		green = .007f;
		blue = .047f;
	}

	@Override
	public void buildStage() {
		for (Actor actor: world.getActors()){
			this.addActor(actor);
		}   
	}
	
	@Override
	public void render(float delta) {
		super.render(delta);
		Vector3 translationVector = new Vector3(0,0,0);

		int speed = 5;
		
		if (Gdx.input.isKeyPressed(Keys.LEFT)){
			translationVector.x -= speed;
		}
		else if (Gdx.input.isKeyPressed(Keys.RIGHT)){
			translationVector.x += speed;
		}
		if (Gdx.input.isKeyPressed(Keys.UP)){
			translationVector.y += speed;
		}
		else if (Gdx.input.isKeyPressed(Keys.DOWN)){
			translationVector.y -= speed;
		}
		
		getCamera().translate(translationVector);
		
		world.gameLoop();
		if (world.gameExit){
			showScreen(ScreenEnum.MAIN_MENU);
		}
		else if (world.encounterSelected){
			showScreen(ScreenEnum.ENCOUNTER);
		}
		else {
			draw();
		}
	}
	
	public void draw(){
		batch.begin();
		
		OrthographicCamera camera = (OrthographicCamera) getCamera();
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.draw(food, camera.position.x+3, camera.position.y+3, 50, 50);
		Matrix4 temp = new Matrix4(batch.getTransformMatrix());
		batch.setTransformMatrix(camera.view);
		batch.setColor(1.0f, 1.0f, 1.0f, .3f);
		batch.draw(cloud, 300, 800, 800, 800);
		batch.draw(cloud, 2200, 600, 800, 800);
		batch.draw(cloud, 1400, 1300, 800, 800);
		batch.setColor(1.0f, 1.0f, 1.0f, 1);
		batch.setTransformMatrix(temp);
		
		font.draw(batch, "X " + foodAmount, camera.position.x+23, camera.position.y+17);
		batch.end(); 
	}
	
	@Override
	public void show() {
		super.show();
	}
	
	@Override
	public void dispose() {
		for(String path: resourceRequirements.keys()){
			if (path.equals("node_sound.wav")) continue;
			assetManager.unload(path);
		}
	}
	
}