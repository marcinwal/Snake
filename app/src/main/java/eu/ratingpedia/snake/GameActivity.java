package eu.ratingpedia.snake;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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

        private void updateGame() {
        }
    }
}
