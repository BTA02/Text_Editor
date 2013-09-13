package local.texteditor;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class MainActivity extends Activity 
{
	public final static String EXTRA_MESSAGE = "local.myfirstapp.message";
	private EditText to_broadcast;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		to_broadcast = (EditText) findViewById(R.id.to_broadcast);
		to_broadcast.addTextChangedListener(new TextWatcher() 
		{
			@Override
			public void afterTextChanged(Editable s) 
			{
				// TODO Auto-generated method stub
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) 
			{
				// TODO Auto-generated method stub
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) 
			{
				if (count < before) //this is a delete
				{
					//any cursor whose location is > start, moves left (before-count)
				}
				else if (count > before) //this is an add
				{
					
				}
				else //this is a full replace
				{
					
				}
			}
		});
		
		
		
		
	}

}
