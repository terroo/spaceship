package com.terminalroot.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import java.util.Iterator;


import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Color;

public class SpaceShip extends ApplicationAdapter {
  SpriteBatch batch;
  Texture img, tNave, tMissile, tEnemy;
  private Sprite nave, missile;
  private float posX, posY, velocity, xMissile, yMissile;
  private boolean attack, gameover;
  private Array<Rectangle> enemies;
  private long lastEnemyTime;
  private int score, power, numEnemies;

  private FreeTypeFontGenerator generator;
  private FreeTypeFontGenerator.FreeTypeFontParameter parameter;
  private BitmapFont bitmap;

  @Override
  public void create () {
    batch = new SpriteBatch();
    img = new Texture("bg.png");
    tNave = new Texture("spaceship.png");
    nave = new Sprite(tNave);
    posX = 0;
    posY = 0;
    velocity = 10;

    tMissile = new Texture("missile.png");
    missile  = new Sprite(tMissile);
    xMissile = posX;
    yMissile = posY;
    attack = false;

    tEnemy = new Texture("enemy.png");
    enemies = new Array<Rectangle>();
    lastEnemyTime = 0;

    score = 0;
    power = 3;
    numEnemies = 799999999;

    generator = new FreeTypeFontGenerator(Gdx.files.internal("font.ttf"));
    parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
    
    parameter.size = 30;
    parameter.borderWidth = 1;
    parameter.borderColor = Color.BLACK;
    parameter.color = Color.WHITE;
    bitmap = generator.generateFont(parameter);

    gameover = false;
  }

  @Override
  public void render () {

    this.moveNave();
    this.moveMissile();
    this.moveEnemies();

    ScreenUtils.clear(1, 0, 0, 1);
    batch.begin();
    batch.draw(img, 0, 0);

    if(!gameover){
      if(attack){
        // ALTERADO
        batch.draw(missile, xMissile, yMissile);
      }
      batch.draw(nave, posX, posY);

      for(Rectangle enemy : enemies ){
        batch.draw(tEnemy, enemy.x, enemy.y);
      }
      bitmap.draw(batch, "Score: " + score, 20, Gdx.graphics.getHeight() - 20);
      bitmap.draw(
          batch, "Power: " + power, 
          Gdx.graphics.getWidth() - 150, 
          Gdx.graphics.getHeight() - 20
          );
    }else{
      bitmap.draw(batch, "Score: " + score, 20, Gdx.graphics.getHeight() - 20);
      bitmap.draw(
          batch, "GAME OVER", 
          Gdx.graphics.getWidth() - 150, 
          Gdx.graphics.getHeight() - 20
          );

      if( Gdx.input.isKeyPressed(Input.Keys.ENTER) ){
        score = 0;
        power = 3;
        posX = 0;
        posY = 0;
        gameover = false;
      }
    }


    batch.end();
  }

  @Override
  public void dispose () {
    batch.dispose();
    img.dispose();
    tNave.dispose();
  }

  private void moveNave(){
    if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
      if( posX < Gdx.graphics.getWidth() - nave.getWidth() ){
        posX += velocity;
      }
    }
    if(Gdx.input.isKeyPressed(Input.Keys.LEFT)){
      if( posX > 0 ){
        posX -= velocity;
      }
    }
    if(Gdx.input.isKeyPressed(Input.Keys.UP)){
      if( posY < Gdx.graphics.getHeight() - nave.getHeight() ){
        posY += velocity;
      }
    }
    if(Gdx.input.isKeyPressed(Input.Keys.DOWN)){
      if( posY > 0 ){
        posY -= velocity;
      }
    }
  }

  private void moveMissile(){
    if( Gdx.input.isKeyPressed(Input.Keys.SPACE) && !attack ){
      attack = true;
      // ALTERADO
      yMissile = posY  + nave.getHeight() / 2 - 12;
    }

    if(attack){
      if( xMissile < Gdx.graphics.getWidth() ){
        xMissile += 40;
      }else{
        // ALTERADO
        xMissile = posX + nave.getWidth() / 2;
        attack = false;
      }
    }else{
      // ALTERADO
      xMissile = posX + nave.getWidth() / 2;
      yMissile = posY  + nave.getHeight() / 2 - 12;
    }
  }

  private void spawnEnemies(){
    Rectangle enemy = new Rectangle( Gdx.graphics.getWidth(), MathUtils.random(0, Gdx.graphics.getHeight() - tEnemy.getHeight()), tEnemy.getWidth(), tEnemy.getHeight());
    enemies.add(enemy);
    lastEnemyTime = TimeUtils.nanoTime();
  }

  private void moveEnemies(){

    if( TimeUtils.nanoTime() - lastEnemyTime > numEnemies ){
      this.spawnEnemies();
    }


    for( Iterator<Rectangle> iter = enemies.iterator(); iter.hasNext(); ){
      Rectangle enemy = iter.next();
      enemy.x -= 400 * Gdx.graphics.getDeltaTime();

      // Colisão com o míssel
      if( collide(enemy.x, enemy.y, enemy.width, enemy.height, xMissile, yMissile, missile.getWidth(), missile.getHeight()) && attack ){
        ++score;
        if( score % 10 == 0 ){
          numEnemies -= 100;
        }
        //System.out.println("Score: " + ++score);
        attack = false;
        iter.remove();
      // Colisão com a nave
      }else if( collide(enemy.x, enemy.y, enemy.width, enemy.height, posX, posY, nave.getWidth(), nave.getHeight()) && !gameover ){
        --power;
        if( power <= 0 ){
          gameover = true;
        }
        iter.remove();
      }

      if(enemy.x + tEnemy.getWidth() < 0){
        iter.remove();
      }
    }
  }

  private boolean collide(float x1, float y1, float w1, float h1, float x2, float y2, float w2, float h2){
    if( x1 + w1 > x2 && x1 < x2 + w2 && y1 + h1 > y2 && y1 < y2 + h2 ){
      return true;
    }
    return false;
  }
}
