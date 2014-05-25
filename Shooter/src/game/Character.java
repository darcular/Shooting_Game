package game;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.input.controls.ActionListener;

public class Character implements AnimEventListener{
    private CharacterControl characterControl;
    private Node character_model;
    private CapsuleCollisionShape capsule;
    private Vector3f movVector = new Vector3f();
    private String identity;
    private Vector3f destination = new Vector3f();
    private AnimControl animationControl;
    private AnimChannel walkingChannel;
    private AnimChannel shootingChannel;
    private Vector3f[] shootEvent = new Vector3f[2];
    private int hitCount =0;
    public boolean isAttached = false;
    public volatile boolean isWalking=false;
    public long timestamp;
    public long laststamp=0;
    

    
    public Character(Node character_model){
        capsule = new CapsuleCollisionShape(3f, 4f);
        characterControl = new CharacterControl(capsule, 0.01f);
        this.character_model = character_model;
        character_model.addControl(characterControl);
        characterControl.setJumpSpeed(40);
        characterControl.setFallSpeed(1000);
        characterControl.setGravity(90);
        characterControl.setPhysicsLocation(new Vector3f(-160, 50, -11));
        destination = new Vector3f(0, 200, 0);
        this.character_model.setName(Console.ip);
        initAnim();      
        identity = Console.ip;
        character_model.setName(identity);
    }
    
    public Character(Node character_model, String ip, Vector3f location, Vector3f direction){
        capsule = new CapsuleCollisionShape(3f, 4f);
        characterControl = new CharacterControl(capsule, 0.01f);
        this.character_model = character_model;
        character_model.addControl(characterControl);
        characterControl.setJumpSpeed(40);
        characterControl.setFallSpeed(1000);
        characterControl.setGravity(90);
        characterControl.setPhysicsLocation(location);
        characterControl.setViewDirection(direction);
        destination = location;
        initAnim();
        identity = ip;
        timestamp = System.currentTimeMillis();
        character_model.setName(identity);
    }
    
    public final void initAnim(){
        animationControl = character_model.getControl(AnimControl.class);
//        animationControl.addListener(this);
        walkingChannel = animationControl.createChannel();
        shootingChannel = animationControl.createChannel();
        shootingChannel.addBone(animationControl.getSkeleton().getBone("uparm.right"));
        shootingChannel.addBone(animationControl.getSkeleton().getBone("arm.right"));
        shootingChannel.addBone(animationControl.getSkeleton().getBone("hand.right"));
    }
    
    
    public void setCharacterModel(Node character_model){
        this.character_model = (Node) character_model;
    }
    public void setPhysicsLocation(Vector3f location){
        characterControl.setPhysicsLocation(location);
    }
    public void setViewDirection(Vector3f direction){
        characterControl.setViewDirection(direction);
    }
    public void setWalkDirection(Vector3f walkDirection){
        characterControl.setWalkDirection(walkDirection);
    }
    public void setMovVector(Vector3f movVector){
        this.movVector = movVector;
    }
    public void updateMov(Vector3f location, Vector3f direction){
    	destination = location;
        setViewDirection(direction);
        timestamp = System.currentTimeMillis();
    }
    public void setDestination(Vector3f location){
    	destination=location;
    }
    public void setShooEvent(Vector3f location, Vector3f direction){
    	shootEvent[0] = location;
    	shootEvent[1] = direction;
    }
    public void setHitCount(int count){
    	hitCount=count;
    }
       
    
    public CharacterControl getControl(){
        return this.characterControl;
    }
    public Node getModel(){
        return this.character_model;
    }
    public Vector3f getWalkDirection(){
        return characterControl.getWalkDirection();
    }
    public Vector3f getMovVector(){
        return movVector;
    }
    public Vector3f getPhysicsLocation(){
        return characterControl.getPhysicsLocation();
    }
    public Vector3f getDestination(){
        return destination;
    }
    public Vector3f getViewDirection(){
        return characterControl.getViewDirection();
    }
    public AnimControl getAnimControl(){
        return this.animationControl;
    }
    public AnimChannel getWalkingChannel(){
        return this.walkingChannel;
    }
    public AnimChannel getShootingChannel(){
        return this.shootingChannel;
    }
    public Vector3f[] getShootEvent(){
    	return shootEvent;
    }
    public String getIdentity(){
    	return identity;
    }
    public int getHitCount(){
    	return hitCount;
    }
    
    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
       if (channel == shootingChannel) {
            channel.setAnim("stand");
        }
    }

    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
       //override do nothing here
    }
}
