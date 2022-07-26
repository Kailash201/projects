package com.example.tetris;

import androidx.annotation.RequiresApi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Matrix;
import android.animation.ValueAnimator;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static android.widget.RelativeLayout.ALIGN_PARENT_LEFT;
import static android.widget.RelativeLayout.BELOW;
import static android.widget.RelativeLayout.RIGHT_OF;

public class MainActivity extends AppCompatActivity {

    RelativeLayout rL; //ViewGroup that holds Shapes
    LinearLayout oLinearL; //Board
    Button left,right,rotate;
    BgShape bgShape;
    ImageView stop;
    ViewGroup relativeLayout; //Access Shape inGame
    int[][] fPoints; //Array for board
    View[][] shapeHolder; //Array to hold each shapes
    List<ViewGroup> relativeLayoutHolder = new ArrayList<>(); //List of shapes
    int x = 0; //Counter for each shape
    int rotationCounter=0; //Indicates rotation
    int ShapeNo=0; //Indicates the type of shape
    int value;  //AnimatedValue
    boolean offset; //Adjust Y coords after rotation
    int score=0;
    int speed=10000;
    int tempScore=0;
    int level=0;
    int xGrid = 0;
    int yGrid = 0;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        Board();
        right = (Button)findViewById(R.id.right);
        left = (Button)findViewById(R.id.left);
        rotate = (Button)findViewById(R.id.rotate);
        stop = (ImageView)findViewById(R.id.image);

        shapeSpawner();
        left.setOnClickListener(moveLeft);
        right.setOnClickListener(moveRight);
        rotate.setOnClickListener(moveRotate);
        stop.setOnClickListener(stopActivity);
    }

    /////////////GETTERS&SETTERSTOADJUSTYCOORDS//////////////
    public void setValue(int value){
        this.value=value;
    }
    public int getValue(){
        if(offset&&(rotationCounter==2||rotationCounter==3)) {
            return this.value + 2;
        }
        return this.value;
    }
    public void setSpeed(int speed){
        this.speed=speed;
    }
    public int getSpeed(){
        return this.speed;
    }

    ////////////////GAMELOGIC////////////////////////////////////
    public void movingDown1(final ViewGroup shape){
        final ValueAnimator va = ValueAnimator.ofInt(-2,35);
        va.setInterpolator(new LinearInterpolator());
        va.setDuration(getSpeed());
        va.start();
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int Yvalue = (int) animation.getAnimatedValue();
                setValue(Yvalue);
                System.out.println(Yvalue);
                shape.setY(getValue()*50);
                Log.d("TAG", "rotation: y"+relativeLayout.getY());
                if(collisionDetector(shape,rotationCounter)){
                    va.pause();
                    if(va.isPaused()){
                        newUpdate(shape,rotationCounter);
                        Log.d("TAG", "rotation: x"+relativeLayout.getX());
                        Log.d("TAG", "rotation: yyy "+rotationCounter);
                        shapeSpawner();
                        lineDetector();
                        rotationCounter=0;
                        offset=false;
                    }
                }
            }
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public boolean collisionDetector(ViewGroup shape, int rotation){
        if(rotation==0) {
            int xS = (int) shape.getX() / 50;
            int xY = (int) (shape.getY() / 50);
            if (xY > -1) {
                int childCount = shape.getChildCount();
                View[] eachChild = new View[childCount];
                for (int i = 0; i < childCount; i++) {
                    eachChild[i] = shape.getChildAt(i);
                }
                for (int i = 0; i < childCount; i++) {
                    int x = (int) eachChild[i].getX() / 50 + xS;
                    int y = (int) (eachChild[i].getY() / 50) + xY + 1;
                    if (y > -1 && y < yGrid) {
                        if (fPoints[y][x] == 1) {
                            return true;
                        }
                    }
                }
            }
        }
        if(rotation==1) {
            int updateX=0;
            if(shape.getWidth()==150)
                updateX=1;
            if(shape.getWidth()==100)
                updateX=2;
            if(shape.getWidth()==50)
                updateX=3;
            Log.d("TAG", "getwidth: " + shape.getWidth() + shape.getX());Log.d("TAG", "getheight: " + shape.getHeight());
            float[] rotatedCoords = rotation();
            int xS = (int) shape.getX() / 50 - (shape.getHeight()/50); // Due to rotation, the x point will shift back by the shape's height
            int xY = (int) (shape.getY() / 50) + 1;
            if (xY > -1) {
                int counter=0;
                for(int i =0;i<4;i++){
                    int arrayX = (int)rotatedCoords[counter]/50 + xS + updateX;
                    counter++;
                    int arrayY = (int)rotatedCoords[counter]/50 + xY;
                    counter++;
                    if (arrayY > -1 && arrayY < yGrid) {
                        if (fPoints[arrayY][arrayX] == 1) {
                            return true;
                        }
                    }
                }
            }
        }
        if(rotation==2) {
            float[] rotatedCoords = rotation();
            int xS = (int) (shape.getX() / 50)-1;
            int xY = (int) (shape.getY() / 50) +1 ;
            if (xY > -1) {
                int counter=0;
                for(int i =0;i<4;i++){
                    int arrayX = (int)(rotatedCoords[counter]/50) + xS ;
                    counter++;
                    int arrayY = (int)(rotatedCoords[counter]/50) + xY;
                    counter++;
                    if (arrayY > -1 && arrayY < yGrid) {
                        if (fPoints[arrayY][arrayX] == 1) {
                            Log.d("TAG", "arrayx0 " + arrayX);
                            Log.d("TAG", "arrayY0 " + arrayY);
                            return true;
                        }
                    }
                }
            }
        }
        if(rotation==3) {
            float[] rotatedCoords = rotation();
            int xS = (int) (shape.getX() / 50);
            int xY = (int) (shape.getY() / 50) +1 ;
            if (xY > -1) {
                int counter=0;
                for(int i =0;i<4;i++){
                    int arrayX = (int)(rotatedCoords[counter]/50) + xS;
                    counter++;
                    int arrayY = (int)(rotatedCoords[counter]/50) + xY;
                    counter++;
                    if (arrayY > -1 && arrayY < yGrid) {
                        if (fPoints[arrayY][arrayX] == 1) {
                            Log.d("TAG", "arrayx0 " + arrayX);
                            Log.d("TAG", "arrayY0 " + arrayY);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /////////////////UPDATEARRAY///////////////
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void newUpdate(ViewGroup shape, int rotation){
        if(rotation==0) {
            int sY = (int) shape.getY() / 50;
            int sX = (int) shape.getX() / 50;
            Log.d("TAG", "ShapeY: " + shape.getY());
            Log.d("TAG", "ShapeX: " + shape.getX());
            int childCount = shape.getChildCount();
            Log.d("TAG", "count: " + childCount);
            View[] eachChild = new View[childCount];
            for (int i = 0; i < childCount; i++) {
                if (shape.getChildAt(i) != null)
                    eachChild[i] = shape.getChildAt(i);
            }
            rL.removeView(shape);
            View[] newChild = new View[4];
            for(int j =0;j<4;j++){
                MyShapes newShapes = new MyShapes(this,null);
                newChild[j]=newShapes;
            }
            for (int i = 0; i < childCount; i++) {
                int xx = (int) (eachChild[i].getX() / 50) + sX;
                int y =  (int) (eachChild[i].getY() / 50) + sY;
                Log.d("TAG", "ChildYX: " + y + xx);
                if (y > -1 && y < yGrid) {
                    fPoints[y][xx] = 1;
                    Log.d("TAG", "arrayx1 " + xx);
                    Log.d("TAG", "arrayY1 " + y);
                    newChild[i].setX(xx*50);
                    newChild[i].setY((y-1)*50);
                    rL.addView(newChild[i]);
                    shapeHolder[y][xx]=newChild[i];
                }
            }
        }
        if(rotation==1){
            int updateX=0;
            if(shape.getWidth()==150)
                updateX=1;
            if(shape.getWidth()==100)
                updateX=2;
            if(shape.getWidth()==50)
                updateX=3;
            int childCount = shape.getChildCount();
            Log.d("TAG", "count: " + childCount);
            rL.removeView(shape);
            View[] newChild = new View[4];
            for(int j =0;j<4;j++){
                MyShapes newShapes = new MyShapes(this,null);
                newChild[j]=newShapes;
            }
            int sY = (int) shape.getY() / 50; //getY will give 1700
            int sX = (int) (shape.getX() / 50)-(shape.getHeight()/50);
            Log.d("TAG", "height " + shape.getHeight());
            Log.d("TAG", "ShapeX: " + shape.getX());
            float[] newCoords = rotation();
            int counter=0;
            for(int i=0;i<4;i++){
                int arrayX = (int)(newCoords[counter]/50) + sX+updateX;
                Log.d("TAG", "rotation xx" + newCoords[counter]);
                counter++;
                int arrayY = (int)(newCoords[counter]/50) + sY;
                Log.d("TAG", "rotation yy " + newCoords[counter]);
                counter++;
                if (arrayY > -1 && arrayY < yGrid) {
                    fPoints[arrayY][arrayX] = 1;
                    Log.d("TAG", "arrayx " + arrayX);
                    Log.d("TAG", "arrayY " + arrayY);
                    newChild[i].setX(arrayX*50);
                    newChild[i].setY((arrayY-1)*50);
                    rL.addView(newChild[i]);
                    shapeHolder[arrayY][arrayX]=newChild[i];
                }
            }
        }
        if(rotation==2){
            int sY = (int) shape.getY() / 50;
            int sX = (int) (shape.getX() / 50) -1;
            Log.d("TAG", "height " + sY);
            Log.d("TAG", "ShapeX: " +sX );
            int childCount = shape.getChildCount();
            Log.d("TAG", "count: " + childCount);
            rL.removeView(shape);
            View[] newChild = new View[4];
            for(int j =0;j<4;j++){
                MyShapes newShapes = new MyShapes(this,null);
                newChild[j]=newShapes;
            }
            float[] newCoords = rotation();
            int counter=0;
            for(int i=0;i<4;i++){
                int arrayX = (int)(newCoords[counter]/50) + sX;
                Log.d("TAG", "rotation xx" + newCoords[counter]);
                counter++;
                int arrayY = (int)(sY+(newCoords[counter]/50));
                Log.d("TAG", "rotation yy " + newCoords[counter]);
                counter++;
                if (arrayY > -1 && arrayY < yGrid) {
                    fPoints[arrayY][arrayX] = 1;
                    Log.d("TAG", "arrayx2 " + arrayX);
                    Log.d("TAG", "arrayY2 " + arrayY);
                    newChild[i].setX(arrayX*50);
                    newChild[i].setY((arrayY-1)*50);
                    rL.addView(newChild[i]);
                    shapeHolder[arrayY][arrayX]=newChild[i];

                }
            }
        }
        if(rotation==3){
            int sY = (int) shape.getY() / 50;
            int sX = (int) (shape.getX() / 50) ;
            Log.d("TAG", "height " + sY);
            Log.d("TAG", "ShapeX: " +sX );
            int childCount = shape.getChildCount();
            Log.d("TAG", "count: " + childCount);
            rL.removeView(shape);
            View[] newChild = new View[4];
            for(int j =0;j<4;j++){
                MyShapes newShapes = new MyShapes(this,null);
                newChild[j]=newShapes;
            }
            float[] newCoords = rotation();
            int counter=0;
            for(int i=0;i<4;i++){
                int arrayX = (int)(newCoords[counter]/50) + sX;
                Log.d("TAG", "rotation xx" + newCoords[counter]);
                counter++;
                int arrayY = (int)(sY+(newCoords[counter]/50));
                Log.d("TAG", "rotation yy " + newCoords[counter]);
                counter++;
                if (arrayY > -1 && arrayY < yGrid) {
                    fPoints[arrayY][arrayX] = 1;
                    Log.d("TAG", "arrayx2 " + arrayX);
                    Log.d("TAG", "arrayY2 " + arrayY);
                    newChild[i].setX(arrayX*50);
                    newChild[i].setY((arrayY-1)*50);
                    rL.addView(newChild[i]);
                    shapeHolder[arrayY][arrayX]=newChild[i];
                }
            }
        }
    }

    ////////////////REMOVINGLINES//////////////
    public void deleteLine(int column){
        for(int i=0;i<xGrid;i++){
            View shape = shapeHolder[column][i];
            rL.removeView(shape);
            fPoints[column][i]=0;
        }
        for(int i = column-1;i>=0;i--){
            for(int a=0;a<xGrid;a++){
                if(shapeHolder[i][a]!=null){
                    View tempShape = shapeHolder[i][a];
                    shapeHolder[i][a].setY(shapeHolder[i][a].getY()+50);
                    shapeHolder[i][a] = null;
                    shapeHolder[i+1][a]=tempShape;

                    fPoints[i+1][a] =1;
                    fPoints[i][a] =0;
                }
            }
        }
        lineDetector();
    }

    public void lineDetector(){
        int counter=0;
        for(int i =yGrid-2;i>=0;i--){
            for(int a=0;a<xGrid;a++){
                if(fPoints[i][a]==1){
                    counter++;
                    if(counter==xGrid) {
                        deleteLine(i);
                        pointsSystem();
                    }
                }
            }
            counter=0;
        }
    }

    ///////////////POINTS/////////////////////
    public void pointsSystem(){
        TextView points = (TextView)findViewById(R.id.points);
        score+=1000;
        tempScore+=1000;
        points.setText(String.valueOf(score));
        points.setTextSize(20);
        Log.d("TAG", "pointsSystem: "+tempScore);
        if(tempScore==5000){
            setSpeed(getSpeed()-1000);
            TextView whatLevel = (TextView)findViewById(R.id.whatlevel);
            level+=1;
            whatLevel.setText(String.valueOf(level));
            tempScore=0;
        }
    }

////////////////ASSETMAKER/////////////////

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void shapeSpawner(){
        Random random = new Random();
        int number = random.nextInt(7);
        //int number = 6;
        btrShapeMaker(number);
        this.ShapeNo = number;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void btrShapeMaker(int selector){
        RelativeLayout relativeLayout = new RelativeLayout(this);

        MyShapes btrShapes = new MyShapes(this,null);
        btrShapes.setId(View.generateViewId());
        RelativeLayout.LayoutParams bS = new RelativeLayout.LayoutParams((ViewGroup.LayoutParams.WRAP_CONTENT), ViewGroup.LayoutParams.WRAP_CONTENT);
        relativeLayout.addView(btrShapes,bS);

        MyShapes btrShapes1 = new MyShapes(this,null);
        btrShapes1.setId(View.generateViewId());
        RelativeLayout.LayoutParams bS1 = new RelativeLayout.LayoutParams((ViewGroup.LayoutParams.WRAP_CONTENT), ViewGroup.LayoutParams.WRAP_CONTENT);
        bS1.addRule(RIGHT_OF,btrShapes.getId());
        relativeLayout.addView(btrShapes1,bS1);

        MyShapes btrShapes2 = new MyShapes(this,null);
        btrShapes2.setId(View.generateViewId());
        RelativeLayout.LayoutParams bS2 = new RelativeLayout.LayoutParams((ViewGroup.LayoutParams.WRAP_CONTENT), ViewGroup.LayoutParams.WRAP_CONTENT);
        bS2.addRule(RelativeLayout.BELOW,btrShapes.getId());
        relativeLayout.addView(btrShapes2,bS2);

        MyShapes btrShapes3 = new MyShapes(this,null);
        btrShapes3.setId(View.generateViewId());
        RelativeLayout.LayoutParams bS3 = new RelativeLayout.LayoutParams((ViewGroup.LayoutParams.WRAP_CONTENT), ViewGroup.LayoutParams.WRAP_CONTENT);
        bS3.addRule(BELOW,btrShapes1.getId());
        bS3.addRule(RIGHT_OF,btrShapes2.getId());
        relativeLayout.addView(btrShapes3,bS3);

        //////////////////LSHAPED///////////////////////////////////
        if(selector==0) {
            RelativeLayout r13= new RelativeLayout(this);
            MyShapes a = new MyShapes(this,null);
            a.setId(View.generateViewId());
            RelativeLayout.LayoutParams aa = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            r13.addView(a,aa);

            MyShapes b = new MyShapes(this,null);
            b.setId(View.generateViewId());
            RelativeLayout.LayoutParams bb = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            bb.addRule(BELOW,a.getId());
            r13.addView(b,bb);
            MyShapes c = new MyShapes(this,null);
            c.setId(View.generateViewId());
            RelativeLayout.LayoutParams cc = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            cc.addRule(BELOW,b.getId());
            r13.addView(c,cc);

            MyShapes d = new MyShapes(this,null);
            RelativeLayout.LayoutParams dd = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dd.addRule(BELOW,b.getId());
            dd.addRule(RIGHT_OF,c.getId());
            r13.addView(d,dd);

            relativeLayoutHolder.add(r13);
            rL.addView(r13);
            r13.setY(-300);
            r13.setX(Math.floorDiv(xGrid, 2)*50);
            x++;
            movingDown1(r13);
            this.relativeLayout=r13;

        }
        /////////////////SQUARESHAPED//////////////////////////////
        if(selector==1) {
            relativeLayoutHolder.add(relativeLayout);
            rL.addView(relativeLayout);
            relativeLayout.setY(-300);
            relativeLayout.setX(Math.floorDiv(xGrid, 2)*50);
            x++;
            movingDown1(relativeLayout);

            this.relativeLayout = relativeLayout;
        }
        /////////////////TSHAPED2by3//////////////////////////////////
        if(selector==2) {
            RelativeLayout r13= new RelativeLayout(this);
            MyShapes a = new MyShapes(this,null);
            a.setId(View.generateViewId());
            RelativeLayout.LayoutParams aa = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            aa.addRule(ALIGN_PARENT_LEFT);
            r13.addView(a,aa);

            MyShapes b = new MyShapes(this,null);
            b.setId(View.generateViewId());
            RelativeLayout.LayoutParams bb = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            bb.addRule(RIGHT_OF,a.getId());
            r13.addView(b,bb);

            MyShapes c = new MyShapes(this,null);
            c.setId(View.generateViewId());
            RelativeLayout.LayoutParams cc = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            cc.addRule(RIGHT_OF,b.getId());

            r13.addView(c,cc);

            MyShapes d = new MyShapes(this,null);
            RelativeLayout.LayoutParams dd = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dd.addRule(BELOW,b.getId());
            dd.addRule(RIGHT_OF,a.getId());
            r13.addView(d,dd);

            relativeLayoutHolder.add(r13);
            rL.addView(r13);
            r13.setY(-300);
            r13.setX(Math.floorDiv(xGrid, 2)*50);
            x++;
            movingDown1(r13);
            this.relativeLayout=r13;

        }
        /////////////////ZSHAPED2by3/////////////////////////////
        if(selector==3) {
            RelativeLayout r13 = new RelativeLayout(this);
            MyShapes a = new MyShapes(this, null);
            a.setId(View.generateViewId());
            RelativeLayout.LayoutParams aa = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            r13.addView(a, aa);

            MyShapes b = new MyShapes(this, null);
            b.setId(View.generateViewId());
            RelativeLayout.LayoutParams bb = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            bb.addRule(RIGHT_OF, a.getId());
            r13.addView(b, bb);

            MyShapes c = new MyShapes(this, null);
            c.setId(View.generateViewId());
            RelativeLayout.LayoutParams cc = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            cc.addRule(BELOW, b.getId());
            cc.addRule(RIGHT_OF, a.getId());
            r13.addView(c, cc);

            MyShapes d = new MyShapes(this, null);
            RelativeLayout.LayoutParams dd = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dd.addRule(BELOW, a.getId());
            dd.addRule(RIGHT_OF, c.getId());
            r13.addView(d, dd);

            relativeLayoutHolder.add(r13);
            rL.addView(r13);
            r13.setY(-300);
            r13.setX(Math.floorDiv(xGrid, 2)*50);
            x++;
            movingDown1(r13);
            this.relativeLayout = r13;
        }
        /////////////////SSHAPED3by2//////////////////////////////
        if(selector==4) {
            RelativeLayout r13 = new RelativeLayout(this);
            MyShapes a = new MyShapes(this, null);
            a.setId(View.generateViewId());
            RelativeLayout.LayoutParams aa = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            r13.addView(a, aa);

            MyShapes b = new MyShapes(this, null);
            b.setId(View.generateViewId());
            RelativeLayout.LayoutParams bb = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            bb.addRule(BELOW, a.getId());
            r13.addView(b, bb);

            MyShapes c = new MyShapes(this, null);
            c.setId(View.generateViewId());
            RelativeLayout.LayoutParams cc = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            cc.addRule(BELOW, a.getId());
            cc.addRule(RIGHT_OF, b.getId());
            r13.addView(c, cc);

            MyShapes d = new MyShapes(this, null);
            RelativeLayout.LayoutParams dd = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dd.addRule(BELOW, c.getId());
            dd.addRule(RIGHT_OF, b.getId());
            r13.addView(d, dd);

            relativeLayoutHolder.add(r13);
            rL.addView(r13);
            r13.setY(-300);
            r13.setX(Math.floorDiv(xGrid, 2)*50);
            x++;
            movingDown1(r13);
            this.relativeLayout = r13;
        }
        /////////////////INVERTLSHAPED3by2////////////////////////
        if(selector==5) {
            RelativeLayout r13 = new RelativeLayout(this);
            MyShapes a = new MyShapes(this, null);
            a.setId(View.generateViewId());
            RelativeLayout.LayoutParams aa = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            r13.addView(a, aa);


            MyShapes b = new MyShapes(this, null);
            b.setId(View.generateViewId());
            RelativeLayout.LayoutParams bb = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            bb.addRule(BELOW, a.getId());
            r13.addView(b, bb);

            MyShapes c = new MyShapes(this, null);
            c.setId(View.generateViewId());
            RelativeLayout.LayoutParams cc = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            cc.addRule(BELOW, b.getId());
            r13.addView(c, cc);

            MyShapes d = new MyShapes(this, null);
            RelativeLayout.LayoutParams dd = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dd.addRule(RIGHT_OF, a.getId());
            r13.addView(d, dd);

            relativeLayoutHolder.add(r13);
            rL.addView(r13);
            r13.setY(-300);
            r13.setX(Math.floorDiv(xGrid, 2)*50);
            x++;
            movingDown1(r13);
            this.relativeLayout = r13;
        }
        ////////////////////STRAIGHTLINE//////////////////////////
        if(selector==6) {
            RelativeLayout r13 = new RelativeLayout(this);
            MyShapes a = new MyShapes(this, null);
            a.setId(View.generateViewId());
            RelativeLayout.LayoutParams aa = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            r13.addView(a, aa);


            MyShapes b = new MyShapes(this, null);
            b.setId(View.generateViewId());
            RelativeLayout.LayoutParams bb = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            bb.addRule(BELOW, a.getId());
            r13.addView(b, bb);

            MyShapes c = new MyShapes(this, null);
            c.setId(View.generateViewId());
            RelativeLayout.LayoutParams cc = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            cc.addRule(BELOW, b.getId());
            r13.addView(c, cc);

            MyShapes d = new MyShapes(this, null);
            RelativeLayout.LayoutParams dd = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dd.addRule(BELOW, c.getId());
            r13.addView(d, dd);

            relativeLayoutHolder.add(r13);
            rL.addView(r13);
            r13.setY(-300);
            r13.setX(Math.floorDiv(xGrid, 2)*50);
            x++;
            movingDown1(r13);
            this.relativeLayout = r13;
        }

    }

    ////////////////INPUT///////////////////////
    View.OnClickListener stopActivity = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };


    View.OnClickListener moveLeft = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(relativeLayout!=null&&ShapeNo!=6) {
                if ((rotationCounter == 0||rotationCounter==3) && relativeLayout.getX() > 0) {
                    relativeLayout.setX(relativeLayout.getX() - 50);
                }
                if (rotationCounter == 1 && relativeLayout.getHeight() == 100 && relativeLayout.getX() > 100
                        || rotationCounter == 1 && relativeLayout.getHeight() == 150 && relativeLayout.getX() > 150) {
                    relativeLayout.setX(relativeLayout.getX() - 50);
                }
                if (rotationCounter == 2 && relativeLayout.getHeight() == 100 && relativeLayout.getX() > 150
                        || rotationCounter == 2 && relativeLayout.getHeight() == 150 && relativeLayout.getX() > 100) {
                    relativeLayout.setX(relativeLayout.getX() - 50);
                }
            }
            if(ShapeNo==6) {
                if ((rotationCounter == 0||rotationCounter==3 )&& relativeLayout.getX() > 0) {
                    relativeLayout.setX(relativeLayout.getX() - 50);
                }
                if (rotationCounter == 2&& relativeLayout.getX() > 50) {
                    relativeLayout.setX(relativeLayout.getX() - 50);
                }
                if (rotationCounter == 1&& relativeLayout.getX() > 200) {
                    relativeLayout.setX(relativeLayout.getX() - 50);
                }
            }
        }
    };
    View.OnClickListener moveRight = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(relativeLayout!=null&&ShapeNo!=6) {
                if (rotationCounter == 0&& relativeLayout.getX()+relativeLayout.getWidth() < xGrid * 50 ) {
                    relativeLayout.setX(relativeLayout.getX() + 50);
                }
                if (rotationCounter == 1 && relativeLayout.getX() < xGrid * 50) {
                    relativeLayout.setX(relativeLayout.getX() + 50);
                }
                if (rotationCounter == 2 && relativeLayout.getX() < xGrid * 50) {
                    relativeLayout.setX(relativeLayout.getX() + 50);
                }
                if (rotationCounter == 3 && relativeLayout.getHeight() == 100 && relativeLayout.getX() < (xGrid * 50 - 100)
                        || rotationCounter == 3 && relativeLayout.getHeight() == 150 && relativeLayout.getX() < (xGrid * 50 - 150)) {
                    relativeLayout.setX(relativeLayout.getX() + 50);
                }

            }
            if(ShapeNo==6) {
                if (rotationCounter == 0 && relativeLayout.getX()+50 < xGrid * 50) {
                    relativeLayout.setX(relativeLayout.getX() + 50);
                }
                if (rotationCounter != 0 && relativeLayout.getX() < xGrid * 50) {
                    relativeLayout.setX(relativeLayout.getX() + 50);
                }

            }
        }
    };

    View.OnClickListener moveRotate = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public void onClick(View v) {
            float height = relativeLayout.getHeight();
            if(ShapeNo!=1) {
                rotationCounter++;
                if (rotationCounter == 4)
                    rotationCounter = 0;
                rotation();
                if (rotationCounter == 1)
                    relativeLayout.setX(relativeLayout.getX() + height);
                if (rotationCounter == 2 && relativeLayout.getWidth() == 150) {
                    relativeLayout.setX(relativeLayout.getX() + 50);
                    offset = true;
                }
                if (rotationCounter == 2 && relativeLayout.getWidth() == 100) {
                    relativeLayout.setX(relativeLayout.getX() - 50);
                    offset = true;
                }
                if (rotationCounter == 3) {
                    relativeLayout.setX(relativeLayout.getX() - 100);
                    offset = true;
                }

                if (rotationCounter == 0)
                    relativeLayout.setX(relativeLayout.getX() - 50);
                if (rotationCounter == 0 && relativeLayout.getWidth() == 100)
                    relativeLayout.setX(relativeLayout.getX() + 50);

                if (ShapeNo == 6) {
                    if (rotationCounter == 1)
                        relativeLayout.setX(relativeLayout.getX()-100);
                    if (rotationCounter == 2 ) {
                        relativeLayout.setX(relativeLayout.getX() - 50);
                        offset = true;
                    }
                    if (rotationCounter == 3) {
                        relativeLayout.setX(relativeLayout.getX());
                        offset = true;
                    }
                    if (rotationCounter == 0)
                        relativeLayout.setX(relativeLayout.getX()+100);
                }
            }
        }
    };
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public float[] rotation(){
        if(rotationCounter==1){
            float[] coords = new float[8];
            int childCount = relativeLayout.getChildCount();
            int counter=0;
            for(int i=0;i<childCount;i++){
                coords[counter]= relativeLayout.getChildAt(i).getX();
                counter++;
                coords[counter]= relativeLayout.getChildAt(i).getY();
                counter++;
                Log.d("TAG", "rotatttion: x"+relativeLayout.getChildAt(i).getX());
                Log.d("TAG", "rotatttion: y"+relativeLayout.getChildAt(i).getY());

            }
            Matrix matrix = new Matrix();
            matrix.setRotate(90,coords[0],coords[1]);
            matrix.mapPoints(coords);
            relativeLayout.setPivotX(0);
            relativeLayout.setPivotY(0);
            Log.d("TAG", "rotation: x"+relativeLayout.getPivotX());
            Log.d("TAG", "rotation: y"+relativeLayout.getPivotY());
            relativeLayout.setRotation(90);

            for(int i=0;i<childCount;i++){
                Log.d("TAG", "rotatttion: x"+relativeLayout.getChildAt(i).getX());
                Log.d("TAG", "rotatttion: y"+relativeLayout.getChildAt(i).getY());

            }


            Log.d("TAG", "rotxxxxxx"+relativeLayout.getX());
            return coords;
        }
        if(rotationCounter==2){
            float[] coords = new float[8];
            int childCount = relativeLayout.getChildCount();
            int counter=0;
            for(int i=0;i<childCount;i++){
                coords[counter]= relativeLayout.getChildAt(i).getX();
                counter++;
                coords[counter]= relativeLayout.getChildAt(i).getY();
                counter++;
            }
            Matrix matrix = new Matrix();
            matrix.setRotate(180,coords[0],coords[1]);
            matrix.mapPoints(coords);
            relativeLayout.setPivotX(0);
            relativeLayout.setPivotY(0);
            Log.d("TAG", "rotation: x1"+coords[0]);
            Log.d("TAG", "rotation: yonw"+coords[1]);
            relativeLayout.setRotation(180);

            Log.d("TAG", "rotxxxxxx"+relativeLayout.getX());
            return coords;
        }
        if(rotationCounter==3){
            float[] coords = new float[8];
            int childCount = relativeLayout.getChildCount();
            int counter=0;
            for(int i=0;i<childCount;i++){
                coords[counter]= relativeLayout.getChildAt(i).getX();
                counter++;
                coords[counter]= relativeLayout.getChildAt(i).getY();
                counter++;
            }
            Matrix matrix = new Matrix();
            matrix.setRotate(270,coords[0],coords[1]);
            matrix.mapPoints(coords);
            relativeLayout.setPivotX(0);
            relativeLayout.setPivotY(0);
            Log.d("TAG", "rotation: x1"+coords[0]);
            Log.d("TAG", "rotation: yonw"+coords[1]);
            relativeLayout.setRotation(270);

            Log.d("TAG", "rotxxxxxx"+relativeLayout.getX());
            return coords;
        }
        if(rotationCounter==0){
            relativeLayout.setPivotX(0);
            relativeLayout.setPivotY(0);
            relativeLayout.setRotation(0);

        }


        return null;
    }

    ////////////////DISPLAY//////////////////////
    public void Board() {
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        double width = size.x;
        double height = size.y;
        yGrid = (int) ((height*4/5)/50);
        xGrid = (int) (width*7/10)/50;
        //xGrid = 8;
        fPoints = new int[yGrid][xGrid];
        shapeHolder = new View[yGrid][xGrid];
        System.out.println(yGrid + xGrid);

        rL = (RelativeLayout) findViewById(R.id.Relative);
        oLinearL = new LinearLayout(this);
        LinearLayout.LayoutParams lL1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        oLinearL.setOrientation(LinearLayout.VERTICAL);
        oLinearL.setLayoutParams(lL1);
        LinearLayout hLinearLayout;

        for (int ii = 0; ii < yGrid-2; ii++) {
            hLinearLayout = new LinearLayout(this);
            LinearLayout.LayoutParams lL = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            hLinearLayout.setLayoutParams(lL);
            hLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            for (int i = 0; i < xGrid; i++) {
                bgShape = new BgShape(this, null);
                hLinearLayout.addView(bgShape);
            }
            oLinearL.addView(hLinearLayout);
        }
        rL.addView(oLinearL);
        for (int i = 0; i < yGrid; i++) {
            for (int a = 0; a < xGrid; a++) {
                fPoints[i][a] = 0;
                if (i == yGrid-1)
                    fPoints[i][a] = 1;
            }

        }
    }

}
