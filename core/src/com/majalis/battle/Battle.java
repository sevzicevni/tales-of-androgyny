package com.majalis.battle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.majalis.battle.BattleFactory.EnemyEnum;
import com.majalis.character.AbstractCharacter;
import com.majalis.character.AbstractCharacter.Stance;
import com.majalis.character.PlayerCharacter.Stat;
import com.majalis.character.EnemyCharacter;
import com.majalis.character.PlayerCharacter;
import com.majalis.character.Technique;
import com.majalis.save.SaveEnum;
import com.majalis.save.SaveService;
/*
 * Represents the logic for the flow of a battle.  Currently only supports 1 on 1.
 */
public class Battle extends Group{

	private static int[] POSSIBLE_KEYS = new int[]{Keys.A, Keys.S, Keys.D, Keys.F, Keys.G, Keys.H, Keys.J};
	private static char[] POSSIBLE_KEYS_CHAR = new char[]{'A','S','D','F','G','H','J','K','L','M'};
	
	private final PlayerCharacter character;
	private final EnemyCharacter enemy;
	private final SaveService saveService;
	private final AssetManager assetManager;
	private final BitmapFont font;
	private final int victoryScene;
	private final int defeatScene;
	private final Table table;
	private final Skin skin;
	private final Sound buttonSound;
	private String console;
	private Array<Technique> options;
	private Technique selectedTechnique;
	public boolean battleOver;
	public boolean victory;
	public boolean gameExit;
	public int struggle;
	public boolean inRear;
	public int battleEndCount;
	public int playerLust;
	
	public Battle(SaveService saveService, AssetManager assetManager, BitmapFont font, PlayerCharacter character, EnemyCharacter enemy,  int victoryScene, int defeatScene) {
		this.saveService = saveService;
		this.assetManager = assetManager;
		this.font = font;
		this.character = character;
		this.enemy = enemy;
		this.victoryScene = victoryScene;
		this.defeatScene = defeatScene;
		console = "";
		battleOver = false;
		gameExit = false;	
		this.addActor(character);
		this.addActor(enemy);
		skin = assetManager.get("uiskin.json", Skin.class);
		buttonSound = assetManager.get("sound.wav", Sound.class);
		table = new Table();
		this.addActor(table);
		displayTechniqueOptions();
		struggle = 0;
		inRear = false;
		battleEndCount = 0;
		playerLust = 0;
	}
	
	private void displayTechniqueOptions(){
		table.clear();
		options = character.getPossibleTechniques();
		for (int ii = 0; ii < options.size; ii++){
			TextButton button;
			Technique option = options.get(ii);
			button = new TextButton(option.getTechniqueName() + (ii > POSSIBLE_KEYS_CHAR.length ? "" : " ("+POSSIBLE_KEYS_CHAR[ii]+")"), skin);
			button.addListener(getListener(option, buttonSound));
			table.add(button).width(220).height(40).row();
		}
        table.setFillParent(true);
        table.addAction(Actions.moveTo(1077, 150));
        
	}

	public void battleLoop() {
		if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)){
			gameExit = true;
		}
		else {
			Technique playerTechnique = getTechnique();
			if (playerTechnique != null){		
				// possibly construct a separate class for this
				resolveTechniques(character, playerTechnique, enemy, enemy.getTechnique(character));
				displayTechniqueOptions();
				
				saveService.saveDataValue(SaveEnum.PLAYER, character);
				saveService.saveDataValue(SaveEnum.ENEMY, enemy);
			}
		}
		
		if (character.getCurrentHealth() <= 0){
			victory = false;
			battleOver = true;
		}
		if (enemy.getCurrentHealth() <= 0){
			victory = true;
			battleOver = true;
		}
		if (battleOver){
			character.refresh();
			saveService.saveDataValue(SaveEnum.ENEMY, null);
		}
	}
	
	private Technique getTechnique() {
		if (selectedTechnique != null){
			Technique temp = selectedTechnique;
			selectedTechnique = null;
			return temp;
		}
		int ii = 0;
		for (int possibleKey : POSSIBLE_KEYS){
			if (Gdx.input.isKeyJustPressed(possibleKey)){
				if (ii < options.size){
					return options.get(ii);
				}
			}
			ii++;
		}
		return null;
	}

	// should probably use String builder to build a string to display in the console - needs to properly be getting information from the interactions - may need to be broken up into its own class
	private void resolveTechniques(AbstractCharacter firstCharacter, Technique firstTechnique, AbstractCharacter secondCharacter, Technique secondTechnique) {
		int rand = (int) Math.floor(Math.random() * 100);
		
		Stance currentNaughtyStance = firstCharacter.getStance();
		
		// this needs to be refactored to reduce code duplication and possibly to condense the whole algorithm down
		// this should probably display the attack you attempted to use, and then display that you used Fall Down / Trip instead.
		// can return extracted costs later for printing
		// will cause a character to fall over / lose endurance of its own volition
		firstTechnique = firstCharacter.extractCosts(firstTechnique);
		secondTechnique = secondCharacter.extractCosts(secondTechnique);
		
		double firstBlockMod = firstTechnique.getBlock() > rand * 2 ? 0 : (firstTechnique.getBlock() > rand ? .5 : 1);
		double secondBlockMod = secondTechnique.getBlock() > rand * 2 ? 0 : (secondTechnique.getBlock() > rand ? .5 : 1);
		
		// these attacks should be generated with all the information from the opposing technique that's relevant, then passed to the character, which will determine the results and return the result string
		Attack attackForFirst = new Attack((int)Math.floor(secondTechnique.getDamage() * firstBlockMod), secondTechnique.getForceStance());
		Attack attackForSecond = new Attack((int)Math.floor(firstTechnique.getDamage() * secondBlockMod), firstTechnique.getForceStance());
				
		console = "";
		Stance firstStance = firstTechnique.getStance();
		Stance secondStance = secondTechnique.getStance();
		// this should only display a message if stance has actually changed - current stance of player and enemy should be visible in UI
		console += getStanceString(firstCharacter, firstStance);
		console += getStanceString(secondCharacter, secondStance);
		firstCharacter.setStance(firstStance);
		secondCharacter.setStance(secondStance);
		
		Stance forcedStance = attackForFirst.getForceStance();
		if (forcedStance != null){
			if ( (firstCharacter.stance == Stance.KNEELING || firstCharacter.stance == Stance.PRONE || firstCharacter.stance == Stance.SUPINE) && secondTechnique.getTechniqueName().equals("Divebomb")){
				console += "The divebomb missed!  The harpy crashed!\n";
				secondCharacter.setStance(Stance.PRONE);
			}
			else {
				firstCharacter.setStance(forcedStance);
			}
		}
		
		Stance forcedStanceSecond = attackForSecond.getForceStance();
		if (forcedStanceSecond != null){
			struggle++;
			if (struggle > 2){
				
				console += "You broke free!\n";
				if (currentNaughtyStance == Stance.FELLATIO){
					console += "It slips out of your mouth and you get to your feet!\n";
				}
				else {
					console += "It pops out of your ass and you get to your feet!\n";
				}
				secondCharacter.setStance(forcedStanceSecond);
				firstCharacter.setStance(Stance.BALANCED);
				struggle = 0;
			}
			else {
				console += "You can't break free!\n";
			}
		}
		
		if (firstTechnique.getTechniqueName().equals("Taunt")){
			console += "You taunt the enemy!  They become aroused!\n";
			secondCharacter.modLust(((PlayerCharacter)firstCharacter).getStat(Stat.CHARISMA));
		}
		
		if (firstTechnique.getTechniqueName().equals("Combat Heal")){
			firstCharacter.heal(firstTechnique);
			console += "You heal for " + firstTechnique.getDamage()  + "!\n";
		}
		else {
			console += getResultString(firstCharacter, secondCharacter, firstTechnique.getTechniqueName(), attackForSecond, secondBlockMod != 1);
		}
		if (secondTechnique.getTechniqueName().equals("Erupt")){
			
			struggle = 0;
			if (currentNaughtyStance == Stance.FELLATIO){
				if (secondCharacter.enemyType == EnemyEnum.HARPY){
					console += "A harpy semen bomb explodes in your mouth!  It tastes awful!\n"
							+ "You are going to vomit!\n"
							+ "You spew up harpy cum!  The harpy preens her feathers.\n";
				}
				else {
					console += "Her cock erupts in your mouth!\n"
							+ "You swallow all of her semen!\n";
				}
			}
			else {
				console += "The " + secondCharacter.getLabel() + " spews hot, thick semen into your bowels!\n";
			}
			console += playerLustIncrement(currentNaughtyStance);
		}
		else if (firstCharacter.getStance() == Stance.DOGGY){
			inRear = true;
			// need some way to get info from sex techniques to display here.  For now, some random fun text
			console += "You are being anally violated!\n"
					+ "Your hole is stretched by her fat dick!\n"
					+ "Your hole feels like it's on fire!\n"
					+ "Her cock glides smoothly through your irritated anal mucosa!\n"
					+ "Her rhythmic thrusting in and out of your asshole is emasculating!\n"
					+ "You are red-faced and embarassed because of her butt-stuffing!\n"
					+ "Your cock is ignored!\n";
			console += playerLustIncrement(Stance.DOGGY);
		}
		else if (firstCharacter.getStance() == Stance.KNOTTED){
			if (battleEndCount == 0){
				console += "Her powerful hips try to force something big inside!\n"
						+ "You struggle... but can't escape!\n"
						+ "Her grapefruit-sized knot slips into your rectum!  You take 4 damage!\n"
						+ "You learned about Anatomy(Wereslut)! You are being bred!\n"
						+ "Your anus is permanently stretched!\n";
			}
			else if (battleEndCount < 3){
				console += "Her tremendous knot is still lodged in your rectum!\n"
						+ "You can't dislodge it; it's too large!\n"
						+ "You're drooling!\n"
						+ "Her fat thing is plugging your shithole!\n";						
			}
			else {
				console += "The battle is over, but your ordeal has just begun!\n"
				+ "You are about to be bred like a bitch!\n"
				+ "She's going to ejaculate her runny dog cum in your bowels!\n";
			}
			if (secondTechnique.forceBattleOver()){	
				// player character should also be able to force battle over
				if (battleEndCount >= 4){
					battleOver = true;
					victory = false;
				}
			}
			battleEndCount++;
			console += playerLustIncrement(Stance.KNOTTED);
		}
		else if (firstCharacter.getStance() == Stance.FELLATIO){
			playerLust++;
			if (secondCharacter.enemyType == EnemyEnum.HARPY){
				console += "She tastes horrible! Harpies are highly unhygenic!\n"
						+ "You learned Anatomy (Harpy)!\n"
						+ "You learned Behavior (Harpy)!\n"
						+ "There is a phallus in your mouth!\n"
						+ "It blew past your lips!\n"
						+ "The harpy is holding your head in place with\n"
						+ "her talons and balancing herself with her wings!\n"
						+ "She flaps violently while humping your face!  Her cock tastes awful!\n";
			}
			else {
				console += "She forces her cock into your mouth!\n";
			}
			if (inRear){
				console += "BLEUGH! That was in your ass!\n"
						+  "Achievement unlocked: Ass to Mouth.\n";
				inRear = false;
			}
			console += playerLustIncrement(Stance.FELLATIO);
		}
		else {
			console += getResultString(secondCharacter, firstCharacter, secondTechnique.getTechniqueName(), attackForFirst, firstBlockMod != 1);
		}		
	}
	
	private String playerLustIncrement(Stance stance){
		String spurt = "";
		playerLust++;
		if (playerLust > 10){
			playerLust = 0;
			switch (stance){
				case KNOTTED:
				case DOGGY: 
					spurt = "Awoo! Semen spurts out your untouched cock as your hole is violated!\n"
						+	"You feel it with your ass, like a girl! Your face is red with shame!\n"
						+	"Got a little too comfortable, eh?\n";
				break;
				case FELLATIO:
					spurt = "You spew while sucking!\n"
						+	"Your worthless cum spurts into the dirt!\n"
						+	"They don't even notice!\n";
				break;
				default: spurt = "You spew your semen onto the ground!\n"; 
			}
			spurt += "You're now flaccid!\n";
		}
		return spurt;
	}

	private String getStanceString(AbstractCharacter character, Stance stance) {
		return character.getLabel() + (character.getSecondPerson() ? " adopt " : " adopts ") + " a(n) " + stance.toString() + " stance!\n";
	}
	// this should be the result based on nested methods - iff a response is needed from the defender, call 
	private String getResultString(AbstractCharacter firstCharacter, AbstractCharacter secondCharacter, String technique, Attack attackForSecond, boolean blocked){
		return firstCharacter.doAttack(technique, secondCharacter, attackForSecond);
	}
	
	@Override
    public void draw(Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
		font.draw(batch, "Health: " + String.valueOf(character.getCurrentHealth()) + "\nStamina: " + String.valueOf(character.getCurrentStamina()) + (character.getStat(Stat.MAGIC) > 1 ? "\nMana: " + String.valueOf(character.getCurrentMana()) : "") + "\nBalance: " + String.valueOf(character.getStability()) + "\nStance: " + character.getStance().toString(), 70, 695);		
		batch.draw(getStanceImage(character.stance), 330, 540, 100, 115);
		batch.draw(getLustImage(playerLust, PhallusType.SMALL), 60, 450, 100, 115);
		font.draw(batch, "Health: " + String.valueOf(enemy.getCurrentHealth()) + "\nStance: " + enemy.getStance().toString(), 1100, 650);		
		batch.draw(getStanceImage(enemy.stance), 920, 540, 100, 115);
		batch.draw(getLustImage(enemy.lust, enemy.enemyType == EnemyEnum.BRIGAND ? PhallusType.NORMAL : PhallusType.MONSTER), 1150, 450, 100, 115);
		font.draw(batch, console, 80, 270);
    }
	
	private enum PhallusType {
		SMALL("Trap"),
		NORMAL("Human"),
		MONSTER("Monster");
		private final String label;

		PhallusType(String label) {
		    this.label = label;
		 }
	}
	
	private Texture getLustImage(int lust, PhallusType type){
		int lustLevel = lust > 7 ? 2 : lust > 3 ? 1 : 0;
		return assetManager.get("arousal/"+ type.label + lustLevel + ".png", Texture.class);
	}
	
	private Texture getStanceImage(Stance stance){
		switch(stance){
			case BALANCED:
				return assetManager.get("stances/Balanced.png", Texture.class);
			case DEFENSIVE:
				return assetManager.get("stances/Defensive.png", Texture.class);
			case DOGGY:
				return assetManager.get("stances/Doggy.png", Texture.class);
			case ERUPT:
				return assetManager.get("stances/Erupt.png", Texture.class);
			case FELLATIO:
				return assetManager.get("stances/Fellatio.png", Texture.class);
			case KNEELING:
				return assetManager.get("stances/Kneeling.png", Texture.class);
			case OFFENSIVE:
				return assetManager.get("stances/Offensive.png", Texture.class);
			case PRONE:
				return assetManager.get("stances/Prone.png", Texture.class);
			case SUPINE:
				return assetManager.get("stances/Supine.png", Texture.class);
			case AIRBORNE:
				return assetManager.get("stances/Airborne.png", Texture.class);
			case CASTING:
				return assetManager.get("stances/Casting.png", Texture.class);
			case KNOTTED:
				return assetManager.get("stances/Knotted.png", Texture.class);
			default:
				return assetManager.get("stances/Balanced.png", Texture.class);
			}
	}
	
	public int getVictoryScene(){
		return victoryScene;
	}
	
	public int getDefeatScene(){
		return defeatScene;
	}
	
	public void dispose(){
		assetManager.unload("uiskin.json");
		assetManager.unload("sound.wav");
	}
	
	// this should pass in a technique that will be used if this button is pressed
	private ClickListener getListener(final Technique technique, final Sound buttonSound){
		return new ClickListener(){
	        @Override
	        public void clicked(InputEvent event, float x, float y) {
	        	buttonSound.play(.5f);
	        	selectedTechnique = technique;
	        }
	    };
	}
}
