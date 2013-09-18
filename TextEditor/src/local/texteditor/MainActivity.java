package local.texteditor;

import java.util.Vector;

import local.texteditor.MovesProtos.Move;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity 
{
	public final static String EXTRA_MESSAGE = "local.myfirstapp.message";
	private EditText to_broadcast;
	private final String TAG1 = "adds";
	private final String TAG2 = "dels";
	Vector cursors = new Vector();
	Vector moves = new Vector();

	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		/*
		 * get the edittext and link user to edittext
		 */
		to_broadcast = (EditText) findViewById(R.id.to_broadcast);
	    User.to_broadcast = to_broadcast;
	    
		/*
		 * define undo/redo buttons
		 */
	    Button undoButton = (Button) findViewById(R.id.UndoButton);
	    Button redoButton = (Button) findViewById(R.id.RedoButton);
	    undoButton.setOnClickListener(new OnClickListener()
	    {
	      @Override
	      public void onClick(View v)
	      {
	         Move retMove = User.Undo(); //now broadcast retMove
	         
	      }
	    });
	    redoButton.setOnClickListener(new OnClickListener()
	    {
	      @Override
	      public void onClick(View v)
	      {
	         Move retMove = User.Redo(); //now broadcast retMove
	      }
	    });
	    
	    /*
	     * define basic operation listener
	     */
	    to_broadcast.setSingleLine(false);   
	    to_broadcast.setHorizontallyScrolling(false); 
	    to_broadcast.setLongClickable(false);
	    to_broadcast.setOnClickListener(new View.OnClickListener() 
	    {
	      @Override
	      public void onClick(View v) 
	      {  
	    	int cursorNewLoc = to_broadcast.getSelectionEnd();
	    	int offset = cursorNewLoc - User.cursorLoc;
			User.CursorLocChangeForLocalUser(cursorNewLoc);   
			User.undoList.add(new EditCom(User.Operation.CURSOR, null, offset));
			Move newMove =
					Move.newBuilder()
					.setUserId(1) //need a user id
					.setMoveType(3)
					.setCursorChange(offset)
					.setUndo(0)
					.build();
	      }
	    });
	   
		to_broadcast.addTextChangedListener(new TextWatcher() 
		{
			@Override
			public void afterTextChanged(Editable s) 
			{}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) 
			{
				if (User.isTextSetManually)
				{
					if (count > after) //delete
					{
						Log.i(TAG2, "sequence: " + s);
						Log.i(TAG2, "start: " + start);
						Log.i(TAG2, "count: " + count);
						Log.i(TAG2, "after: " + after);
						Log.i(TAG2, "characters deleted: " + s.toString().substring(start, start+count) );			
					
						User.cursorLoc --;
						System.out.println("user manual delete: after delete, cursor @ " + User.cursorLoc);
						User.undoList.add(new EditCom(User.Operation.DELETE, s.toString().charAt(start), 0));
						Move newMove =
								Move.newBuilder()
								.setUserId(1)//need a user id
								.setMoveType(2)
								.setUndo(0)
								.build();						
					}
				}

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) 
			{
				if (User.isTextSetManually)
				{
					if (count < before) //this is a delete, deal with adding it to the queue elsewhere
					{
						//dealt with in beforeTextChanged
					}
					else if (count > before) //this is an add
					{
						Log.i(TAG1, "sequence: " + s);
						Log.i(TAG1, "start: " + start);
						Log.i(TAG1, "before: " + before);
						Log.i(TAG1, "count: " + count);
						Log.i(TAG1, "characters added: " + s.toString().substring(start, (start+count)) );
					
						User.cursorLoc ++;
						System.out.println("user manual add: after add, cursor @ " + User.cursorLoc);
						User.undoList.add(new EditCom(User.Operation.ADD, s.toString().charAt(start), 0));
						Move newMove =
								Move.newBuilder()
								.setUserId(1) //need a user id
								.setMoveType(1)
								.setData(s.toString().substring(start, (start+count)) )
								.setUndo(0)
								.build();
					}
					else //this is a full replace
					{
						//no longer needed
					}
				}
				else
				{
					User.isTextSetManually = true;
				}
			}
		});
		
	}
}
