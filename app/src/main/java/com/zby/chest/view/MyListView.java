package com.zby.chest.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

public class MyListView extends ListView {


	private GestureDetector mGestureDetector;
     View.OnTouchListener mGestureListener;
     
     public MyListView(Context context) {
    	 super(context);
    	 // TODO Auto-generated constructor stub
     }
     public MyListView(Context context, AttributeSet attrs) {
    	 super(context, attrs);
    	 // TODO Auto-generated constructor stub
    	 mGestureDetector =new GestureDetector(new YScrollDetector());
    	 setFadingEdgeLength(0);
     }
     
     public MyListView(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
     }

     @Override
     public boolean onInterceptTouchEvent(MotionEvent ev) {
         super.onInterceptTouchEvent(ev);
         return mGestureDetector.onTouchEvent(ev);
     }
     
     
     class YScrollDetector extends SimpleOnGestureListener {
    	 @Override
    	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
    			float distanceY) {
    		// TODO Auto-generated method stub
    		 if(Math.abs(distanceY) >= Math.abs(distanceX) && Math.abs(distanceY)>10) {
                return true;
            }
            return false;
    	}
     }
 }