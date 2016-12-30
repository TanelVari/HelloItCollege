package edu.tanelvari.java.pinks;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

public class Sprite {

    private double width;
    private double height;

    private double posX;
    private double posY;

    private double velX;
    private double velY;

    private static final Color fillColor = Color.rgb(250, 250, 250);

    public Sprite (double x, double y, double w, double h) {
        posX = x;
        posY = y;

        width = w;
        height = h;

        velX = 10;
        velY = 10;
    }

    public void updateSpritePosition(){
        posX += velX;
        posY += velY;
    }

    public void renderSprite(GraphicsContext gc){
        gc.setFill(fillColor);
        gc.fillRect(posX, posY, width, height);
    }

    public void setVelX(double x) {
        velX = x;
    }

    public void setVelY(double y) {
        velY = y;
    }

    public double getVelX() {
        return velX;
    }

    public double getVelY() {
        return velY;
    }

    public void setX(double x) {
        posX = x;
    }

    public void setY(double y) {
        posY = y;
    }

    public double getX() {
        return posX;
    }

    public double getY() {
        return posY;
    }
}
