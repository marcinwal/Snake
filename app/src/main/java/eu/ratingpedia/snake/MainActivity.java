package eu.ratingpedia.snake;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;



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

        


    }


}
