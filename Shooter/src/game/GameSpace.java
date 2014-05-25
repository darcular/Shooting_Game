/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author darcular
 */
public class GameSpace extends SimpleApplication implements ActionListener, ScreenController{
    private BulletAppState bulletAppState;
    //character
    private Character player;
    private Node character_node;
    private Node other_character_node;
//    Vector3f walkDirection = new Vector3f();
    //other character
    private ArrayList<CharacterControl> other_characters;
    private ArrayList<Node> other_character_models;
    //terrain
    private TerrainQuad terrain;
    private Material mat_terrain;
    RigidBodyControl terrainPhysicsNode;
    //bullet
    private Sphere bullet;
    private Material bullet_mat;
    private RigidBodyControl ball_phy;
    private SphereCollisionShape bulletCollisionShape;
    private Node shootables;
    private Geometry mark; //optional
    //animation
//    private AnimControl animationControl;
//    private AnimChannel walkingChannel;
//    private AnimChannel shootingChannel;
    float airTime = 0;
    //camera control
    private boolean left = false, right = false, up = false, down = false;
    private ChaseCamera chaseCam; //optional
    //audio
    private AudioNode audio_gun;
    private AudioNode audio_nature;
    
    private Nifty nifty;
    private ListBox<String> myList;
    private ListBox<String> origList;
    private float time=0;
    private BitmapText playerText;
    private BitmapText targetText;
    private String targetIp;
    
    // for multiple players

    public void simpleInitApp() {
    	NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort);
    	nifty = niftyDisplay.getNifty();
    	nifty.fromXml("scrollpanel.xml","start");

    	guiViewPort.addProcessor(niftyDisplay);
    	myList = nifty.getCurrentScreen().findNiftyControl("list", ListBox.class);
    	myList.changeSelectionMode(ListBox.SelectionMode.Disabled, up);
    	origList = nifty.getCurrentScreen().findNiftyControl("oirginglist", ListBox.class);
    	origList.changeSelectionMode(ListBox.SelectionMode.Disabled, up);
    	Console.peer.setOrigList(origList);

        bulletAppState = new BulletAppState();
        bulletAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
        stateManager.attach(bulletAppState);
//        bulletAppState.getPhysicsSpace().enableDebug(assetManager);        
        shootables = new Node();
        character_node = new Node();
        other_character_node = new Node();
        character_node.attachChild(other_character_node);
        shootables.attachChild(character_node);
        rootNode.attachChild(shootables);       
        cam.setFrustum(1.0f, 1000f, -0.9644643f, 0.9644643f, 0.7233482f, -0.7233482f);
        
        playerText = new BitmapText(guiFont, false);
        playerText.setSize(20);
        playerText.setLocalScale(1.4f);
//        playerText.setLocalScale();
        playerText.setText("Me:"+Console.ip);
        playerText.setLocalTranslation(150, playerText.getLineHeight()+10, 0);
        guiNode.attachChild(playerText);
        
        targetText = new BitmapText(guiFont, false);
        targetText.setSize(20);
        targetText.setLocalScale(1.4f);
        targetText.setColor(ColorRGBA.Red);
        targetText.setText(targetIp);
        targetText.setLocalTranslation(430, playerText.getLineHeight()+10, 0);
        guiNode.attachChild(targetText);
        
        setupKeys();
        initBullet();
        initLight();
        createSky();
        createTerrain();
//        Options.createMap1(assetManager, rootNode, bulletAppState);  // an optional map
        createCharacter();
//        createOtherCharacters();   //optional
//        setupChaseCamera(); //optional
//        setupAnimationController();
        initCrossHairs();
        initAudio();
        initNetwork();
    }
       @Override
    public void simpleUpdate(float tpf) {
    	targetText.setText(targetIp);
        Vector3f camDir = cam.getDirection().clone().multLocal(0.3f);
        Vector3f camLeft = cam.getLeft().clone().multLocal(0.3f);
        camDir.y = 0;  
        camLeft.y = 0;
        
        player.getMovVector().set(0, 0, 0);
        if (left)  { player.getMovVector().addLocal(camLeft); }
        if (right) { player.getMovVector().addLocal(camLeft.negate()); }
        if (up)    { player.getMovVector().addLocal(camDir); }
        if (down)  { player.getMovVector().addLocal(camDir.negate()); }
        player.getControl().setWalkDirection(player.getMovVector());
        player.getControl().setViewDirection(camDir);
        
        //let the cam fellow our character. (Useless for chaseCamera)
        cam.setLocation(player.getControl().getPhysicsLocation().add(new Vector3f(0,3.5f,0)));       
        //let the ear fellow the character
        listener.setLocation(cam.getLocation());
        listener.setRotation(cam.getRotation());
       
        //for animi control
        movAniControl(player,tpf);
        
        // update other players
        time = time + tpf;
        for(Character player : Console.otherPlayers.values()){
            if(!player.isAttached){
                attachPlayer(player);
                other_character_node.attachChild(player.getModel());
                player.isAttached=true;
            }
            if(time>3){
        		if(player.timestamp==player.laststamp){
        			detachPlayer(player);
        			other_character_node.detachChild(player.getModel());
        			Console.otherPlayers.remove(player.getIdentity());	
        		}
        		else{
        			player.laststamp=player.timestamp;
        		}
        	}
            movAniControl(player,tpf);
            //move
            if(!player.getDestination().equals(player.getPhysicsLocation()))
            {
            	Vector3f vector = player.getDestination().add(player.getPhysicsLocation().mult(-1f));
            	vector.y=0;
            	Vector3f movVector = vector.normalize().mult(0.3f);
            	if(vector.length()<0.15 || vector.length()>15){
            		player.getControl().setPhysicsLocation(player.getDestination());
            		movVector = new Vector3f(0,0,0);
            	}
            	else if(vector.length()<= movVector.length())
            		movVector = vector;
            	else
            		movVector = vector.normalize().mult(0.3f);
            	player.setMovVector(movVector);
            	player.getControl().setWalkDirection(player.getMovVector());   
            }
            //shoot
            if(player.getShootEvent()[1]!=null){
            	String ip = player.getIdentity();
            	Vector3f[] parts = player.getShootEvent();
            	shootBullet(parts[0], parts[1],ip);
            	player.setShooEvent(null, null);
            }
        }
        if(time>0.8)
        	pick();
        if(time>3)
        	time=0;
    }
    
    private void setupKeys(){
        inputManager.addMapping("CharLeft", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("CharRight", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("CharUp", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("CharDown", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("CharJump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("CharShoot", new MouseButtonTrigger(0));
        inputManager.addListener(this, "CharLeft");
        inputManager.addListener(this, "CharRight");
        inputManager.addListener(this, "CharUp");
        inputManager.addListener(this, "CharDown");
        inputManager.addListener(this, "CharJump");
        inputManager.addListener(this, "CharShoot");
        
        inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_EXIT);
        inputManager.addMapping("Exit", new KeyTrigger(KeyInput.KEY_ESCAPE));
        inputManager.addListener(this, "Exit");
    }
    
    private void initBullet(){
        bullet = new Sphere(32, 32, 0.8f, true, false);
        bullet.setTextureMode(Sphere.TextureMode.Projected);
        bulletCollisionShape = new SphereCollisionShape(0.8f);
        bullet_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        TextureKey key2 = new TextureKey("Textures/Terrain/Rock/Rock.PNG");
        key2.setGenerateMips(true);
        Texture tex2 = assetManager.loadTexture(key2);
        bullet_mat.setTexture("ColorMap", tex2);
//        bulletAppState.getPhysicsSpace().addCollisionListener(this);      
    }
    
    private void initLight(){
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(1.3f));
        rootNode.addLight(al);
        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
        rootNode.addLight(dl);     
    }
    
    private void createSky(){
        rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/Sky/Bright/BrightSky.dds", false));
    }
    
    private void createTerrain(){
        mat_terrain = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
        mat_terrain.setTexture("Alpha", assetManager.loadTexture("Textures/Terrain/splat/alphamap.png"));
        Texture grass = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
        grass.setWrap(Texture.WrapMode.Repeat);
        mat_terrain.setTexture("Tex1", grass);
        mat_terrain.setFloat("Tex1Scale", 64f);
        Texture dirt = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
        dirt.setWrap(Texture.WrapMode.Repeat);
        mat_terrain.setTexture("Tex2", dirt);
        mat_terrain.setFloat("Tex2Scale", 32f);
        Texture rock = assetManager.loadTexture("Textures/Terrain/splat/road.jpg");
        rock.setWrap(Texture.WrapMode.Repeat);
        mat_terrain.setTexture("Tex3", rock);
        mat_terrain.setFloat("Tex3Scale", 128f);
        AbstractHeightMap heightmap = null;
        Texture heightMapImage = assetManager.loadTexture("Textures/Terrain/splat/mountains512.png");
        heightmap = new ImageBasedHeightMap(heightMapImage.getImage());
        heightmap.load();
        int patchSize = 65;
        terrain = new TerrainQuad("my terrain", patchSize, 513, heightmap.getHeightMap());
        terrain.setMaterial(mat_terrain);
        terrain.setLocalTranslation(0, -100, 0);
        terrain.setLocalScale(2f, 1f, 2f);
        //Level of detail control
        TerrainLodControl control = new TerrainLodControl(terrain, getCamera());
        terrain.addControl(control);
        //setup collision model
        terrainPhysicsNode = new RigidBodyControl(CollisionShapeFactory.createMeshShape(terrain), 0);
        terrain.addControl(terrainPhysicsNode);
        //separate groups for collision
//        terrainPhysicsNode.setCollisionGroup(3);  //3 for terrian
//        terrainPhysicsNode.addCollideWithGroup(2); //2 for bullet
        bulletAppState.getPhysicsSpace().add(terrainPhysicsNode);
        shootables.attachChild(terrain);
    }
    
    public void createCharacter(){
        player = new Character((Node)assetManager.loadModel("Models/Oto/Oto.mesh.xml"));
        attachPlayer(player);
    }
    public void attachPlayer(Character player){
        character_node.attachChild(player.getModel());
        bulletAppState.getPhysicsSpace().add(player.getControl());
        player.isAttached=true;
    }
    
    public void detachPlayer(Character player){
    	character_node.detachChild(player.getModel());
    	bulletAppState.getPhysicsSpace().remove(player.getControl());
    	player.isAttached=false;
    }
    
    public void setupChaseCamera(){
        chaseCam = new ChaseCamera(cam, player.getModel(), inputManager);
        chaseCam.setDragToRotate(false);
    }
    
    
    protected void initCrossHairs() {
//        guiNode.detachAllChildren();
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("+"); // crosshairs
        ch.setColor(ColorRGBA.Black);
        ch.setLocalTranslation( // center
        settings.getWidth() / 2 - guiFont.getCharSet().getRenderedSize() / 3 * 2,
        settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
        guiNode.attachChild(ch);
  }

    public void initAudio(){
        audio_gun = new AudioNode(assetManager, "Sound/Effects/Gun.wav", false);
        audio_gun.setLooping(false);
        audio_gun.setVolume(1.5f);
        rootNode.attachChild(audio_gun);
        
        audio_nature = new AudioNode(assetManager, "Sound/Environment/Nature.ogg", false);
        audio_nature.setLooping(true);  
        audio_nature.setPositional(false);
        audio_nature.setVolume(2.5f);
        rootNode.attachChild(audio_nature);
        audio_nature.play();
    }
    
    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals("CharLeft")) {
            left = isPressed;
        } else if (name.equals("CharRight")) {
            right = isPressed;
        } else if (name.equals("CharUp")) {
            up = isPressed;
        } else if (name.equals("CharDown")) {
            down = isPressed;
        } else if (name.equals("CharJump")) {
            player.getControl().jump();
        }
        if (name.equals("CharShoot") && isPressed) {
//            makeMark();      	
            shootBullet(player.getControl().getPhysicsLocation(), cam.getDirection(), player.getIdentity());
            try {
				Console.caster.boardCastShoot(player, cam);
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        if (name.equals("Exit") && isPressed){
        	Console.isPlaying=false;
        	Console.peer.isThreadWorking=false;
        	Console.peer.closeSocket();      
        	try{
        		Console.movListener.stop();
				Console.movListener.join();
				System.out.println("movListener join");
				Console.caster.stop();
				Console.caster.join();
				System.out.println("caster join");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        	this.stop();
        }
    }

    
    public void shootBullet(Vector3f location, Vector3f direction, String ip){
//        player.getShootingChannel().setAnim("Dodge", 0.1f);
//        player.getShootingChannel().setLoopMode(LoopMode.DontLoop);
        Geometry ball_geo = new Geometry("bullet", bullet);
        ball_geo.setName("b"+ip);
        ball_geo.setMaterial(bullet_mat);  
        ball_geo.setLocalTranslation(location.add(new Vector3f(0,4f,0)).add(direction.mult(5)));
        ball_phy = new BombControl(assetManager, bulletCollisionShape, 1, player);
        ball_geo.addControl(ball_phy);
//        ball_phy.setCollisionGroup(2);
//        ball_phy.removeCollideWithGroup(1);
        bulletAppState.getPhysicsSpace().add(ball_phy);
        shootables.attachChild(ball_geo);
        ball_phy.setGravity(new Vector3f(0,-50,0));
        ball_phy.setLinearVelocity(direction.mult(200));  //1500
        audio_gun.setLocalTranslation(location);
        audio_gun.playInstance();
    }
    
    public void createOtherCharacters(){
        other_characters = new ArrayList<CharacterControl>();
        other_character_models = new ArrayList<Node>();
        
        CapsuleCollisionShape capsule = new CapsuleCollisionShape(3f, 4f);
        CharacterControl other_character = new CharacterControl(capsule, 0.01f);
        Node other_character_model = (Node) assetManager.loadModel("Models/Oto/Oto.mesh.xml");
        //model.setLocalScale(0.5f);
        other_character_model.addControl(other_character);
        other_character.setJumpSpeed(40);
        other_character.setFallSpeed(1000);
        other_character.setGravity(90);
        other_character.setPhysicsLocation(new Vector3f(-20, 500, -20));
//        other_character.setCollisionGroup(3);
//        other_character.setCollideWithGroups(2);
        
        shootables.attachChild(other_character_model);
        bulletAppState.getPhysicsSpace().add(other_character);
        
        other_characters.add(other_character);
        other_character_models.add(other_character_model);
    }
    
    public void createOtherCharacters(Vector3f location, Vector3f direction){
        other_characters = new ArrayList<CharacterControl>();
        other_character_models = new ArrayList<Node>();
        
        CapsuleCollisionShape capsule = new CapsuleCollisionShape(3f, 4f);
        CharacterControl other_character = new CharacterControl(capsule, 0.01f);
        Node other_character_model = (Node) assetManager.loadModel("Models/Oto/Oto.mesh.xml");
        //model.setLocalScale(0.5f);
        other_character_model.addControl(other_character);
        other_character.setJumpSpeed(40);
        other_character.setFallSpeed(1000);
        other_character.setGravity(90);
        other_character.setPhysicsLocation(location);
        other_character.setViewDirection(direction);
//        other_character.setCollisionGroup(3);
//        other_character.setCollideWithGroups(2);
        
        shootables.attachChild(other_character_model);
        bulletAppState.getPhysicsSpace().add(other_character);
        
        other_characters.add(other_character);
        other_character_models.add(other_character_model);
    }
    
    public void initNetwork(){
        Console.movListener = new MovingListener(assetManager, myList);
        Console.movListener.start();
        Console.caster = new Caster(player);
        Console.caster.start();
    }
    
    public void movAniControl(Character player,float tpf){
        AnimChannel walkingChannel = player.getWalkingChannel();
//        if (!player.getControl().onGround()) {
//            airTime = airTime + tpf;
//        } else {
//            airTime = 0;
//        }
        if (player.getMovVector().length() == 0 && !player.isWalking) {
            if (!"stand".equals(walkingChannel.getAnimationName())) {
                walkingChannel.setAnim("stand", 1f);
            }
        } else {
            if (airTime > 0.3f) {
                if (!"stand".equals(walkingChannel.getAnimationName())) {
                    walkingChannel.setAnim("stand");
                }
            } else if (!"Walk".equals(walkingChannel.getAnimationName())) {
                walkingChannel.setAnim("Walk", 0.7f);
                walkingChannel.setSpeed(2);
            }
        }    
    }
    public void pick(){
    	CollisionResults results = new CollisionResults();
        Ray ray = new Ray(cam.getLocation(), cam.getDirection());
        other_character_node.collideWith(ray, results);
        if(results.size()!=0)
        	targetIp="Target:"+results.getCollision(0).getGeometry().getParent().getName();
        else
        	targetIp=null;
    }
	@Override
	public void bind(Nifty arg0, Screen arg1) {	
	}
	@Override
	public void onEndScreen() {	
	}
	@Override
	public void onStartScreen() {	
	}
}
