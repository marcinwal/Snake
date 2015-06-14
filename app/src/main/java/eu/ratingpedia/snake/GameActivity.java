package eu.ratingpedia.snake;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Random;


public class GameActivity extends Activity {

    Canvas canvas;
    SnakeView snakeView;

    Bitmap headBitmap;
    Bitmap bodyBitmap;
    Bitmap tailBitmap;
    Bitmap appleBitmap;
    Bitmap flowerBitmap;

    private SoundPool soundPool;
    int sample1 = -1;
    int sample2 = -1;
    int sample3 = -1;
    int sample4 = -1;

    int directionOfTravel = 0;

    int screenWidth;
    int screenHeight;
    int topGap;

    long lastFrameTime;
    int fps;
    int score;
    int hi;

    int [] snakeX;
    int [] snakeY;
    int [] snakeH;
    int snakeLength;
    int appleX;
    int appleY;

    int blockSize;
    int numBlocksWide;
    int numBlocksHigh;

    Rect flowerRectToBeDrawn;
    int frameHeight;
    int frameWidth;
    int flowerNumFrames = 2;
    int flowerFrameNumber;
    int flowerAnimTimer = 0;

    Matrix matrix90 = new Matrix();
    Matrix matrix180 = new Matrix();
    Matrix matrix270 = new Matrix();
    Matrix matrixHeadFlip = new Matrix();

    int [] flowersX;
    int [] flowersY;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadSound();
        configureDisplay();
        snakeView = new SnakeView(this);
        setContentView(snakeView);
    }


    private class SnakeView extends SurfaceView implements Runnable {
        Thread ourThread = null;
        SurfaceHolder ourHolder;
        volatile boolean playingSnake;
        Paint paint;

        public SnakeView(Context context) {
            super(context);
            ourHolder = getHolder();
            paint = new Paint();

            snakeX = new int[200];
            snakeY = new int[200];
            snakeH = new int[200];

            plantFlowers();
            getSnake();
            getApple();
        }

        private void plantFlowers() {
            Random random = new Random();
            int x = 0;
            int y = 0;
            flowersX = new int[200];
            flowersY = new int[200];

            for(int i = 0;i < 10; i++){
                x = random.nextInt(numBlocksWide-1)+1;
                y = random.nextInt(numBlocksHigh-1)+1;
                flowersX[i] = x;
                flowersY[i] = y;
            }
        }


        private void getApple() {
            Random random = new Random();
            appleX = random.nextInt(numBlocksWide-1) + 1;
            appleY = random.nextInt(numBlocksHigh-1) + 1;
        }

        private void getSnake() {
            snakeLength = 3;
            //head
            snakeX[0] = numBlocksWide / 2;
            snakeY[0] = numBlocksHigh / 2;
            //body
            snakeX[1] = snakeX[0]-1;
            snakeY[1] = snakeY[0];
            //tail
            snakeX[2] = snakeX[1]-1; //diff
            snakeY[2] = snakeY[0];   //diff

      }

        @Override
        public void run() {
            while(playingSnake){
                updateGame();
                drawGame();
                controlFPS();
            }

        }

        private void controlFPS() {
            long timeThisFrame = (System.currentTimeMillis() - lastFrameTime);
            long timeToSleep = 100 - timeThisFrame;
            if (timeThisFrame > 0){
                fps = (int)(1000 / timeThisFrame);
            }
            if(timeToSleep > 0){
                try{
                    ourThread.sleep(timeToSleep);

                }catch (InterruptedException e){

                }
            }


            //flower animation
            flowerAnimTimer++;
            if(flowerAnimTimer == 6){
                if(flowerFrameNumber == 1){
                    flowerFrameNumber = 0;
                }else{
                    flowerFrameNumber = 1;
                }
                flowerRectToBeDrawn = new Rect((flowerFrameNumber * frameWidth),0,(flowerFrameNumber * frameWidth + frameWidth)-1,frameHeight);
                flowerAnimTimer = 0;
            }

            lastFrameTime = System.currentTimeMillis();
        }

        private void drawGame() {
            if(ourHolder.getSurface().isValid()){
                canvas = ourHolder.lockCanvas();
                canvas.drawColor(Color.argb(255,186,230,177));
                paint.setColor(Color.argb(255, 255, 255, 255));
                paint.setTextSize(topGap / 2);
                canvas.drawText("Score:" + score + " Hi:" + hi,10, topGap - 6, paint);
                paint.setStrokeWidth(3);
                canvas.drawLine(1, topGap, screenWidth - 1, topGap, paint);
                canvas.drawLine(screenWidth-1,topGap,screenWidth-1,topGap+(numBlocksHigh*blockSize),paint);
                canvas.drawLine(screenWidth-1,topGap+(numBlocksHigh*blockSize),1,topGap+(numBlocksHigh*blockSize),paint);
                canvas.drawLine(1, topGap,1, topGap + (numBlocksHigh * blockSize), paint);
                //flowers
                Rect destRect;
                Bitmap rotatedBitmap;
                Bitmap rotatedTailBitmap;

                for(int i = 0;i < 10;i++){
                    destRect = new Rect(flowersX[i]*blockSize,(flowersY[i]*blockSize)+topGap,(flowersX[i]*blockSize)+blockSize,
                                        (flowersY[i]*blockSize)+topGap+blockSize);
                    canvas.drawBitmap(flowerBitmap,flowerRectToBeDrawn,destRect,paint);
                }

                //Draw the snake
                rotatedBitmap = headBitmap;
                switch (snakeH[0]){
                    case 0://up
                        rotatedBitmap = Bitmap.createBitmap(rotatedBitmap , 0, 0, rotatedBitmap .getWidth(), rotatedBitmap .getHeight(), matrix270, true);
                        break;
                    case 1://right
                        //no rotation necessary

                        break;
                    case 2://down
                        rotatedBitmap = Bitmap.createBitmap(rotatedBitmap , 0, 0, rotatedBitmap .getWidth(), rotatedBitmap .getHeight(), matrix90, true);
                        break;

                    case 3://left
                        rotatedBitmap = Bitmap.createBitmap(rotatedBitmap , 0, 0, rotatedBitmap .getWidth(), rotatedBitmap .getHeight(), matrixHeadFlip, true);
                        break;
                }

                //snake
                canvas.drawBitmap(rotatedBitmap,snakeX[0]*blockSize,(snakeY[0]*blockSize)+topGap,paint);


                //Draw the body

                rotatedBitmap = bodyBitmap;
                for(int i = 1; i < snakeLength-1;i++){

                    switch (snakeH[i]){
                        case 0://up
                            rotatedBitmap = Bitmap.createBitmap(bodyBitmap , 0, 0, bodyBitmap .getWidth(), bodyBitmap .getHeight(), matrix270, true);
                            break;
                        case 1://right
                            //no rotation necessary

                            break;
                        case 2://down
                            rotatedBitmap = Bitmap.createBitmap(bodyBitmap , 0, 0, bodyBitmap .getWidth(), bodyBitmap .getHeight(), matrix90, true);
                            break;

                        case 3://left
                            rotatedBitmap = Bitmap.createBitmap(bodyBitmap , 0, 0, bodyBitmap .getWidth(), bodyBitmap .getHeight(), matrix180, true);
                            break;


                    }

                    canvas.drawBitmap(rotatedBitmap, snakeX[i]*blockSize, (snakeY[i]*blockSize)+topGap, paint);
                }
                //tail
                rotatedTailBitmap = Bitmap.createBitmap(tailBitmap, flowerRectToBeDrawn.left, flowerRectToBeDrawn.top, flowerRectToBeDrawn.right - flowerRectToBeDrawn.left, flowerRectToBeDrawn.bottom);

                switch (snakeH[snakeLength-1]){
                    case 0://up
                        rotatedTailBitmap = Bitmap.createBitmap(rotatedTailBitmap , 0, 0, rotatedTailBitmap .getWidth(), rotatedTailBitmap .getHeight(), matrix270, true);
                        break;
                    case 1://right
                        //no rotation necessary

                        break;
                    case 2://down
                        rotatedTailBitmap = Bitmap.createBitmap(rotatedTailBitmap , 0, 0, rotatedTailBitmap .getWidth(), rotatedTailBitmap .getHeight(), matrix90, true);
                        break;

                    case 3://left
                        rotatedTailBitmap = Bitmap.createBitmap(rotatedTailBitmap , 0, 0, rotatedTailBitmap .getWidth(), rotatedTailBitmap .getHeight(), matrix180, true);
                        break;


                }

                canvas.drawBitmap(rotatedTailBitmap, snakeX[snakeLength-1]*blockSize, (snakeY[snakeLength-1]*blockSize)+topGap, paint);
                //apple
                canvas.drawBitmap(appleBitmap,appleX*blockSize,(appleY*blockSize)+topGap,paint);
                ourHolder.unlockCanvasAndPost(canvas);
            }
            
        }

        private void updateGame() {
            if(snakeX[0] == appleX && snakeY[0] == appleY){
                snakeLength++;
                getApple();
                score = score + snakeLength;
                soundPool.play(sample1,1,1,0,0,1);
            }
            //moving body
            for(int i=snakeLength; i>0;i--){
                snakeX[i] = snakeX[i-1];
                snakeY[i] = snakeY[i-1];

                snakeH[i] = snakeH[i-1];
            }
            //moving head
            switch (directionOfTravel){
                case 0://going up
                    snakeY[0]--;
                    snakeH[0] = 0;
                    break;
                case 1:
                    snakeX[0]++;
                    snakeH[0] = 1;
                    break;
                case 2:
                    snakeY[0]++;
                    snakeH[0] = 2;
                    break;
                case 3:
                    snakeX[0]--;
                    snakeH[0] = 3;
                    break;
            }
            boolean dead = false;

            if(snakeX[0] == -1) dead = true;
            if(snakeX[0] >= numBlocksWide) dead = true;
            if(snakeY[0] == -1) dead = true;
            if(snakeY[0] == numBlocksHigh) dead = true;
            //eating ourselves?
            for (int i = snakeLength-1;i > 0;i--){
                if((i > 4) && (snakeX[0] == snakeX[i]) && (snakeY[0] == snakeY[i])){
                    dead = true;
                }
            }

            if (dead){
                soundPool.play(sample4,1,1,0,0,1);
                score = 0;
                getSnake();
            }
        }

        private void pause(){
            playingSnake = false;
            try{
                ourThread.join();
            }catch (InterruptedException e){

            }
        }

        private void resume(){
            playingSnake = true;
            ourThread = new Thread(this);
            ourThread.start();
        }

        @Override
        public boolean onTouchEvent(MotionEvent motionEvent){
            switch (motionEvent.getAction()&MotionEvent.ACTION_MASK){
                case MotionEvent.ACTION_UP:
                    if(motionEvent.getX() >= screenWidth / 2){
                        directionOfTravel++;
                        if(directionOfTravel == 4){
                            directionOfTravel = 0;
                        }
                    }else{
                        //left
                        directionOfTravel--;
                        if(directionOfTravel == -1){
                            directionOfTravel = 3;
                        }
                    }
            }
            return true;
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        snakeView.pause();
    }

    @Override
    protected void onStop(){
        super.onStop();
        while(true){
            snakeView.pause();
            break;
        }
        finish();
    }

    @Override
    protected void onResume(){
        super.onResume();
        snakeView.resume();
    }

    public boolean onKeyDown(int keyCode,KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK){
            snakeView.pause();
            Intent i = new Intent(this,MainActivity.class);
            finish();
            return true;
        }
        return false;
    }

    public void loadSound(){
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC,0);
        try{
            AssetManager assetManager = getAssets();
            AssetFileDescriptor descriptor;

            descriptor = assetManager.openFd("sample1.ogg");
            sample1 = soundPool.load(descriptor,0);
            descriptor = assetManager.openFd("sample2.ogg");
            sample2 = soundPool.load(descriptor,0);
            descriptor = assetManager.openFd("sample3.ogg");
            sample3 = soundPool.load(descriptor,0);
            descriptor = assetManager.openFd("sample4.ogg");
            sample4 = soundPool.load(descriptor,0);
        }catch (IOException e){
            Toast.makeText(getApplicationContext(),"FAILED with MUSIC",Toast.LENGTH_LONG).show();
        }
    }

    public void configureDisplay(){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        topGap = screenHeight / 14;
        blockSize = screenWidth / 30;
        numBlocksWide = 30;
        numBlocksHigh = ((screenHeight - topGap))/blockSize;
        headBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.head);
        bodyBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.body);
        tailBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.tail_sprite_sheet);
        appleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.apple);
        //scaling maps
        headBitmap = Bitmap.createScaledBitmap(headBitmap,blockSize,blockSize,false);
        bodyBitmap = Bitmap.createScaledBitmap(bodyBitmap,blockSize,blockSize,false);
        tailBitmap = Bitmap.createScaledBitmap(tailBitmap,blockSize*flowerNumFrames,blockSize,false);
        appleBitmap = Bitmap.createScaledBitmap(appleBitmap,blockSize,blockSize,false);

        flowerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.flower_sprite_sheet);
        flowerBitmap = Bitmap.createScaledBitmap(flowerBitmap,blockSize*flowerNumFrames,blockSize,false);

        frameWidth = flowerBitmap.getWidth()/flowerNumFrames;
        frameHeight = flowerBitmap.getHeight();

        matrix90.postRotate(90);
        matrix180.postRotate(180);
        matrix270.postRotate(270);

        matrixHeadFlip.setScale(-1,1);
        matrixHeadFlip.postTranslate(headBitmap.getWidth(),0);

        flowerRectToBeDrawn = new Rect((flowerFrameNumber * frameWidth),0,(flowerFrameNumber*frameWidth+frameWidth)-1,frameHeight);


    }
}
