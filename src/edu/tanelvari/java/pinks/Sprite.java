package edu.tanelvari.java.pinks;

import javafx.scene.canvas.GraphicsContext;

import java.awt.geom.Rectangle2D;

public class Sprite {

    private double width;
    private double height;

    private double posX;
    private double posY;

    private double velX;
    private double velY;

    public Sprite (double x, double y, double w, double h, double vx, double vy) {
        posX = x;
        posY = y;

        width = w;
        height = h;

        velX = vx;
        velY = vy;
    }

    public void updateSpritePosition(){
        posX += velX;
        posY += velY;
    }

    public void renderSprite(GraphicsContext gc){
        gc.setFill(Constants.MAIN_COLOR);
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

    public Rectangle2D getBounds(){
        return new Rectangle2D.Double(posX, posY, width, height);
    }

    public boolean intersects(Sprite s){
        return s.getBounds().intersects(this.getBounds());
    }
}
