package com.zby.chest.activity;

import com.zby.chest.R;
import com.zby.chest.utils.ScreenUtils;
import com.zby.chest.view.MyTextView2;
import com.zby.chest.view.WeDroidAlignTextView;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

public class HelpLawInfoActivity extends BaseActivity {
	
	private TextView tv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help_lawinfo);
		initBaseViews(this);
		tv = (TextView) findViewById(R.id.textView);
		layout_back.setVisibility(View.VISIBLE);
		tv.setText(R.string.law_info_content);
	}

	@Override
	void initHandler() {
		// TODO Auto-generated method stub
		
	}
	
	public static String ToDBC(String input) {          
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {              
        if (c[i] == 12288) {                 
        c[i] = (char) 32;                  
        continue;
         }
         if (c[i] > 65280 && c[i] < 65375)
            c[i] = (char) (c[i] - 65248);
        }
        return new String(c);
    }  

}
