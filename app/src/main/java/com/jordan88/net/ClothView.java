package com.jordan88.net;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/**
 * TODO: document your custom view class.
 */
public class ClothView extends View implements View.OnTouchListener{

    private int physicsAccuracy = 4;
    private float mouseInfluence = 20;
    private float mouseCut = 5;
    public final static float gravity = 1200;
    private float clothWidth = 20;
    private float clothHeight = 20;
    private float startX = 20;
    private float startY = 20;
    private float spacing = 7;
    private float tearDistance = 100;
    private long newTime, oldTime;
    private float boundsX = 500;
    private float boundsY = 500;

    public Cloth cloth;
    private Mouse mouse = new Mouse();
    private Paint paint = new Paint();

    public ClothView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    public ClothView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public ClothView(Context context) {
        super(context);
        init();
    }

    private void init() {
        newTime = new Date().getTime();
        oldTime = newTime;
        this.setOnTouchListener(this);


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        this.setMeasuredDimension(parentWidth, parentHeight);
        cloth = new Cloth();
        spacing = parentWidth > parentHeight ? (parentHeight - 2*startY)/clothHeight : (parentWidth - 2*startX)/clothWidth;

    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        cloth.update();
        cloth.draw(canvas);
        this.invalidate();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            mouse.down = true;
        } else if (action == MotionEvent.ACTION_UP) {
            mouse.down = false;
        }
        mouse.px = mouse.x;
        mouse.py = mouse.y;
        mouse.x = event.getX();
        mouse.y = event.getY();
        return true;
    }

    public class Mouse {
        public float x,y,px,py;
        public boolean down;
        public Mouse() {
            down = false;
            x = 0;
            y = 0;
            px = 0;
            py = 0;
        }
    }

    public class Point {
        public float x, y, px, py, vx, vy, pinx, piny;
        public ArrayList<Constraint> constraints;

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
            px = x;
            py = y;
            vx = 0;
            vy = 0;
            pinx = -1;
            piny = -1;
            constraints = new ArrayList<Constraint>();
        }

        public void resolveConstraints() {
            if(pinx != -1 && piny != -1) {
                x = pinx;
                y = piny;
                return;
            }
            Iterator<Constraint> constraintIterator = constraints.iterator();
            while(constraintIterator.hasNext()) {
                constraintIterator.next().resolve();
            }
            constraintIterator = constraints.iterator();
            while(constraintIterator.hasNext()) {
                Constraint c = constraintIterator.next();
                if(c.isDead()) {
                    constraintIterator.remove();
                }
            }
            if(x > getWidth()-1) {
                x = 2 * getWidth()-1 - x;
            }
            else if(1 > x) {
                    x = 2 - x;
            }
            if(y > getHeight()-1) {
                y = 2 * getHeight()-1 - y;
            }
            else if(1 > y) {
                    y = 2 - y;
            }
        }
        public void attach(Point point) {
            constraints.add(new Constraint(this, point));
        }
        public void removeConstraint(Constraint constraint) {
            constraints.remove(constraint);
        }
        public void addForce(float x, float y, float delta) {
            vx += x*delta;
            vy += y*delta;
        }
        public void pin(float pinx, float piny) {
            this.pinx = pinx;
            this.piny = piny;
        }
        public void update(float delta) {
            delta = delta / 1000;
            if(mouse.down) {
                float diffx = x - mouse.x;
                float diffy = y - mouse.y;
                float dist = (float) Math.sqrt(diffx*diffx+diffy*diffy);
                if(dist < mouseInfluence) {
                    px = x - (mouse.x - mouse.px)*1.8f;
                    py = y - (mouse.y - mouse.py)*1.8f;
                }
                else if(dist < mouseCut) constraints.clear();
            }
           // float[] rotations = ((NetActivity)getContext()).getRotations();
            addForce(0, gravity, delta);

            delta *= delta;
            float nx = x + ((x - px) * .99f) + ((vx/2) * delta);
            float ny = y + ((y - py) * .99f) + ((vy/2) * delta);

            px = x;
            py = y;

            x = nx;
            y = ny;

            vx = 0;
            vy = 0;
        }
        public void draw(Canvas canvas) {
            Iterator<Constraint> constraintIterator = constraints.iterator();
            while(constraintIterator.hasNext()) {
                constraintIterator.next().draw(canvas);
            }
        }
    }
    public class Constraint {
        public Point p1, p2;
        public float length;
        private boolean dead = false;

        public Constraint(Point x, Point y) {
            p1 = x;
            p2 = y;
            length = spacing;
        }
        public void resolve() {
            if(!dead) {
                float diffx = p1.x - p2.x;
                float diffy = p1.y - p2.y;
                float dist = (float) Math.sqrt(diffx*diffx + diffy*diffy);
                float diff = (length - dist) / dist;

                if(dist > tearDistance) {
                    dead = true;
                }

                float px = diffx * diff * .5f;
                float py = diffy * diff * .5f;

                p1.x += px;
                p1.y += py;
                p2.x -= px;
                p2.y -= py;
            }
        }
        public void draw(Canvas canvas) {
            if(!dead) {
                paint.setColor(Color.BLACK);
                paint.setStrokeWidth(4);
                paint.setStyle(Paint.Style.STROKE);
                paint.setAlpha(100);
                canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint);
            }
        }
        public boolean isDead() {
            return dead;
        }
    }
    public class Cloth {
        private ArrayList<Point> points = new ArrayList<Point>();
        public Cloth() {
            for(int i = 0; i <= clothHeight; i++) {
                for(int j = 0; j <= clothWidth; j++) {

                    Point p = new Point(startX+j*spacing, startY+i*spacing);

                    if(j != 0) p.attach(points.get(points.size()-1));
                    if(i == 0) p.pin(p.x, p.y);
                    if(i != 0) p.attach(points.get((int)(j + (i-1)*(clothWidth+1))));

                    points.add(p);
                }
            }
        }

        public void update() {

            Iterator<Point> pointIterator;
            for(int i = 0; i < physicsAccuracy; i++) {
                pointIterator = points.iterator();
                while(pointIterator.hasNext()) {
                    pointIterator.next().resolveConstraints();
                }
            }

            oldTime = newTime;
            newTime = new Date().getTime();
            float diff = newTime - oldTime;

            pointIterator = points.iterator();
            while(pointIterator.hasNext()) {
                pointIterator.next().update(diff);
            }
        }
        public void draw(Canvas canvas) {
            Iterator<Point> pointIterator = points.iterator();
            while(pointIterator.hasNext()) {
                pointIterator.next().draw(canvas);
            }
        }
    }
}
