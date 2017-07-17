package com.zby.chest.activity;

import com.zby.chest.R;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

public class DialogInputActivity extends Activity {
	
	private LinearLayout layout_cancel, layout_edit;
	private EditText et_1,et_2, et_3, et_4, et_5, et_6;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_input2);
	}
	
	private void initViews() {
		layout_cancel = (LinearLayout) findViewById(R.id.layout_cancel);
		layout_edit = (LinearLayout) findViewById(R.id.layout_edit);
		et_1 = (EditText) findViewById(R.id.editText_input1);
		et_2 = (EditText) findViewById(R.id.editText_input2);
		et_3 = (EditText) findViewById(R.id.editText_input3);
		et_4 = (EditText) findViewById(R.id.editText_input4);
		et_5 = (EditText) findViewById(R.id.editText_input5);
		et_6 = (EditText) findViewById(R.id.editText_input6);
		
	}
	
	 public boolean dispatchKeyEvent(KeyEvent event) {  
		 	Log.d("tag", "event " + event.getKeyCode());
	        if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER){  
	            /*隐藏软键盘*/  
	            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);  
	            if(inputMethodManager.isActive()){  
	                inputMethodManager.hideSoftInputFromWindow(DialogInputActivity.this.getCurrentFocus().getWindowToken(), 0);  
	            }  
	              
	            return true;  
	        }  
	        return super.dispatchKeyEvent(event);  
	    }  
}
