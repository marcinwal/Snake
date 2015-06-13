package eu.ratingpedia.snake;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class MainActivity extends Activity {

    Canvas canvas;
    SnakeAnimView snakeAnimView;

    Bitmap headAnimBitmap;

    Rect rectToBeDrawn;
    int frameHeight = 64;
    int frameWidth = 64;
    int numFrames = 6;
    int frameNumber;

    int screenWidth;
    int screenHeight;

    long lastFrameTime;
    int fps;
    int hi;

    Intent i;//for starting game from onTouchEvent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        headAnimBitmap = BitmapFactory.decodeResource(getResources(),
                         R.drawable.head_sprite_sheet);

        snakeAnimView = new SnakeAnimView(this);
        setContentView(snakeAnimView);

        i = new Intent(this,GameActivity.class);

        class SnakeAnimView extends SurfaceView implements Runnable{

            Thread ourThread = null;
            SurfaceHolder ourHolder;
            volatile boolean playingSnake;
            Paint paint;

            public SnakeAnimView(Context context){
                super(context);
                ourHolder = getHolder();
                paint = new Paint();
                frameWidth = headAnimBitmap.getWidth()/numFrames;
                frameHeight = headAnimBitmap.getHeight();
            }

            @Override
            public void run() {
                while(playingSnake){
                    update();
                    draw();
                    controlFPS();
                }
            }

            private void update() {
                rectToBeDrawn = new Rect((frameNumber * frameWidth)-1),
                                0,(frameNumber * frameWidth + frameWidth)-1,
                                frameHeight);

                frameNumber++;
                if(frameNumber == numFrames){
                    frameNumber = 0;
                }
            }
        }






    }


}
