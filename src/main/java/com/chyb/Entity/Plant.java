package main.java.com.chyb.Entity;
import main.java.com.chyb.utils.Vector2D;

public class Plant {
    Vector2D position;
    public Plant(Vector2D position){
        this.position = new Vector2D(position.x, position.y);
    }
    public Vector2D getPosition(){
        return position;
    }

}