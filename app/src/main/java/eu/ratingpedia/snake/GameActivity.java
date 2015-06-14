package eu.ratingpedia.snake;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.net.ContentHandler;
import java.util.Random;


public class GameActivity extends Activity {

    Canvas canvas;
    SnakeView snakeView;

    Bitmap headBitmap;
    Bitmap bodyBitmap;
    Bitmap tailBitmap;
    Bitmap appleBitmap;

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
    int snakeLength;
    int appleX;
    int appleY;

    int blockSize;
    int numBlocksWide;
    int numBlocksHigh;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadSound();
        configureDisplay();
        snakeView = new SnakeView(this);
        setContentView(snakeView);
    }


    private class SnakeView extends SurfaceView implements Runnable {
        Thread outThread = null;
        SurfaceHolder ourHolder;
        volatile boolean playingSnake;
        Paint paint;

        public SnakeView(Context context) {
            super(context);
            ourHolder = getHolder();
            paint = new Paint();

            snakeX = new int[200];
            snakeY = new int[200];

            getSnake();
            getApple();
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
            snakeX[2] = snakeX[1]-1;
            snakeY[2] = snakeY[0];

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

        }

        private void drawGame() {
            if(ourHolder.getSurface().isValid()){
                canvas = ourHolder.lockCanvas();
                canvas.drawColor(Color.BLACK);
                paint.setColor(Color.argb(255, 255, 255, 255));
                paint.setTextSize(topGap / 2);
                canvas.drawText("Score:" + score + " Hi:" + hi,
                        10, topGap - 6, paint);
                paint.setStrokeWidth(3);
                canvas.drawLine(1, topGap, screenWidth - 1, topGap, paint);
                canvas.drawLine(screenWidth-1,topGap,screenWidth-1,topGap+
                        (numBlocksHigh*blockSize),paint);

                canvas.drawLine(screenWidth-
                        1,topGap+(numBlocksHigh*blockSize),1,topGap+
                        (numBlocksHigh*blockSize),paint);
                canvas.drawLine(1,topGap,
                        1,topGap+(numBlocksHigh*blockSize), paint);
                //snake
                canvas.drawBitmap(headBitmap,snakeX[0]*blockSize,
                        (snakeY[0]*blockSize)+topGap,paint);
                for(int i = 1;i < snakeLength-1;i++){
                    canvas.drawBitmap(bodyBitmap,snakeX[i]*blockSize,
                            (snakeY[i]*blockSize)+topGap,paint);
                }
                //tail
                canvas.drawBitmap(tailBitmap,snakeX[snakeLength-1]*blockSize,
                                  (snakeY[snakeLength-1]*blockSize)+topGap,paint);
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
            }
            //moving head
            switch (directionOfTravel){
                case 0://going up
                    snakeY[0]--;
                    break;
                case 1:
                    snakeX[0]++;
                    break;
                case 2:
                    snakeY[0]++;
                    break;
                case 3:
                    snakeX[0]--;
                    break;
            }
            boolean dead = false;

            if(snakeX[0] == -1) dead = true;
            if(snakeX[0] >= numBlocksWide) dead = true;
            if(snakeY[0] == -1) dead = true;
            if(snakeY[0] == numBlocksHigh) dead = true;
            //eating ourselves?
            for (int i = snakeLength-1;i > 0;i--){
                if((i > 4) && snakeX[0] == snakeX[i]) &&
                (snakeY[0] == snakeY[i]){
                    dead = true;
                }
            }

            if (dead){
                soundPool.play(sample4,1,1,0,0,1);
                score = 0;
                getSnake();
            }
        }
    }
}
