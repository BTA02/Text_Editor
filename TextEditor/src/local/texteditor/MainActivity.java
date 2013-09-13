package local.texteditor;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.DragEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.widget.Button;
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
	    Button undoButton = (Button) findViewById(R.id.UndoButton);
	    Button redoButton = (Button) findViewById(R.id.RedoButton);
	    User.to_broadcast = to_broadcast;
	    
	    undoButton.setOnClickListener(new OnClickListener()
	    {
	      @Override
	      public void onClick(View v)
	      {
	         User.Undo();
	      }
	    });
	    
	    redoButton.setOnClickListener(new OnClickListener()
	    {
	      @Override
	      public void onClick(View v)
	      {
	         User.Redo();
	      }
	    });
	    to_broadcast.setSingleLine(false);   
	    to_broadcast.setHorizontallyScrolling(false);
	    to_broadcast.setOnClickListener(new View.OnClickListener() {
	      @Override
	      public void onClick(View v) 
	      {
	        User.CursorLocChange(to_broadcast.getSelectionEnd());
	        
	      }
	    });
	    
	    to_broadcast.setOnDragListener(new OnDragListener(){

	      @Override
	      public boolean onDrag(View v, DragEvent event)
	      {
	        // TODO Auto-generated method stub
	        return false;
	      }
	      
	    });

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
				//add the text changed to our queue
				if (count < before) //this is a delete
				{
					User.Del();
					//any cursor whose location is > start, moves left (before-count)
				}
				else if (count > before) //this is an add
				{
					User.Add();
				}
				else //this is a full replace
				{
					
				}
			}
		});
		
	}

}