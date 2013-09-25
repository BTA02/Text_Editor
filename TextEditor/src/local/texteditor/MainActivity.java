package local.texteditor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import local.texteditor.MovesProtos.Move;
import android.R.xml;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.protobuf.InvalidProtocolBufferException;

import edu.umich.imlc.collabrify.client.CollabrifyAdapter;
import edu.umich.imlc.collabrify.client.CollabrifyClient;
import edu.umich.imlc.collabrify.client.CollabrifyListener;
import edu.umich.imlc.collabrify.client.CollabrifySession;
import edu.umich.imlc.collabrify.client.exceptions.CollabrifyException;


public class MainActivity extends Activity 
{
	private final String TAG1 = "adds";
	private final String TAG2 = "dels";
	private EditText to_broadcast;
	private String continuousString = "";
	private int continuousCount = 0;
	private long startTime;
	Cursor myCursor;
	
	Vector<Cursor> cursors = new Vector<Cursor>();

	//for receiving messages handling
	private String serverCopy = ""; //this is the "server" copy of the text
	private int lastLocalMove;
	
	
	private static final Level LOGGING_LEVEL = Level.ALL;	

	private CollabrifyClient myClient;
	private Button createSessionButton;
	private Button joinSessionButton;
	private Button leaveSessionButton;
	private CheckBox withBaseFile;
	private CollabrifyListener collabrifyListener;
	private ArrayList<String> tags = new ArrayList<String>();
	private long sessionId;
	private String sessionName;
	private ByteArrayInputStream baseFileBuffer;
	private ByteArrayOutputStream baseFileReceiveBuffer;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//BUG HERE
		Random userIDgen = new Random();
		myCursor = new Cursor(userIDgen.nextInt(), 0);
		if (myCursor.userID < 0)
		{
			myCursor.userID = myCursor.userID * -1;
		}
		cursors.add(myCursor); //now my cursor is the first one in the vector, excellent.
		User.Id = myCursor.userID;
		Log.i("success", "user info: " + cursors.get(0).userID + " " + cursors.get(0).cursorLoc);
		
		
		/*
		 * get the edittext and link user to edittext
		 */
		to_broadcast = (EditText) findViewById(R.id.to_broadcast);
	    User.to_broadcast = to_broadcast;
	    
	    /*
	     * timer thread
	     */
	 	new Thread () {
	 		public void run() 
	 		{
	 			startTime = System.currentTimeMillis();
	 			while(true) 
	 			{
	 				if (System.currentTimeMillis() - startTime >= 600 
	 					&& continuousCount != 0)
	 					generateInsertDelete(); 
	 			}
	 		}
	 	}.start();

	    
	    
		/*
		 * define undo/redo buttons
		 */
	    Button undoButton = (Button) findViewById(R.id.UndoButton);
	    Button redoButton = (Button) findViewById(R.id.RedoButton);
	    undoButton.setOnClickListener(new OnClickListener()
	    {
	    	@Override
	    	public void onClick(View v) //still need moveID here
	    	{
	    		if (continuousCount != 0)
	    			generateInsertDelete(); 
	    		
	    		EditCom com = User.Undo(); //now broadcast retMove
	    		if (com != null)
	    		{	
	    			Move retmove = com.generateMoveMes(1);
	    			
	    			try 
	    			{	
	    				myClient.broadcast(retmove.toByteArray(), "undo");
	    				Log.i("success", "undo broadcasting success"); //have to change cursors on an undo
	    			} 
	    			catch (CollabrifyException e) 
	    			{
	    				Log.i("failed", "undo broadcasting failed");
	    				e.printStackTrace();
	    			}
	    		}   
	    	}
	    });
	    redoButton.setOnClickListener(new OnClickListener()
	    {
	    	@Override
	    	public void onClick(View v) //still need moveID here
	    	{
	    		if (continuousCount != 0)
	    			generateInsertDelete(); 
	    		
	    		EditCom com = User.Redo(); //now broadcast retMove
	    		if (com != null)
	    		{	
	    			Move retmove = com.generateMoveMes(2);
	    			try 
	    			{	
	    				myClient.broadcast(retmove.toByteArray(), "redo");
	    				Log.i("success", "redo broadcasting success");
	    			} 
	    			catch (CollabrifyException e) 
	    			{
	    				Log.i("failed", "redo broadcasting failed");
	    				e.printStackTrace();
	    			}
	    		}  
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
	    	public void onClick(View v) //for cursor changes
	    	{  
	    		if (continuousCount != 0)
	    			generateInsertDelete(); 
	    		
	    		int cursorNewLoc = to_broadcast.getSelectionEnd();
	    		int offset = cursorNewLoc - User.cursorLoc;
	    		User.CursorChange(User.Id, offset); 
	    		
	    		Random rid = new Random();
				int mid = rid.nextInt();
				
	    		EditCom com = new EditCom(User.Operation.CURSOR, null, offset, mid);
	    		lastLocalMove = mid;
	    		User.undoList.add(com);
	    		Move retMove = com.generateMoveMes(0);
				try 
				{	
					myClient.broadcast(retMove.toByteArray(), "cur");
					Log.i("success", "cursor broadcasting success");
				} 
				catch (CollabrifyException e) 
				{
					Log.i("failed", "cursor broadcasting failed");
					e.printStackTrace();
				}
	    		
	    		User.redoList.clear();
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
					
			    		if (continuousCount > 0)
			    			generateInsertDelete(); 
						
						startTime = System.currentTimeMillis();
						continuousCount--;
						continuousString = s.toString().substring(start, start+count) + continuousString;
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
					{}
					else if (count > before) //this is an add
					{
			    		if (continuousCount < 0)
			    			generateInsertDelete();
						
						startTime = System.currentTimeMillis();
						continuousCount++;
						
						continuousString += s.toString().substring(start, start+count);
						//System.out.println("Let's test here: " + continuousString);
					}
					else //this is a full replace
					{}
				}
				else
				{
					User.isTextSetManually = true;
				}
			}
		});	
		
		
		
		/*
		 * createSession, joinSession, leaveSession, receiveEvent, broadcastEvetn
		 */
	    withBaseFile = (CheckBox) findViewById(R.id.withBaseFileCheckBox);
	    createSessionButton = (Button) findViewById(R.id.CreateButton);
	    joinSessionButton = (Button) findViewById(R.id.JoinButton);
	    leaveSessionButton = (Button) findViewById(R.id.LeaveButton);
	    // enable logging
	    Logger.getLogger("edu.umich.imlc.collabrify.client").setLevel(LOGGING_LEVEL);


	    
	    createSessionButton.setOnClickListener(new OnClickListener()
	    {
	      @Override
	      public void onClick(View v)
	      {
	        try
	        {
	          Random rand = new Random();
	          sessionName = "Test " + rand.nextInt(Integer.MAX_VALUE);

	          if( withBaseFile.isChecked() )
	          {
	            baseFileBuffer = new ByteArrayInputStream(to_broadcast.getText().toString().getBytes());
	            myClient.createSessionWithBase(sessionName, tags, null, 0);
	          }
	          else
	          {
	            myClient.createSession(sessionName, tags, null, 0);
	          }
	          System.out.println("Session name is " + sessionName);
	        }
	        catch( CollabrifyException e )
	        {
	          System.err.println("error " + e);
	        }
	      }
	    });

	    
	    
	    joinSessionButton.setOnClickListener(new OnClickListener() //I think we can hardcode this stuff?
	    {

	      @Override
	      public void onClick(View v)
	      {
	        try
	        {
	          myClient.requestSessionList(tags);
	        }
	        catch( Exception e )
	        {
		      System.err.println("error " + e);
	        }
	      }
	    });

	    
	    
	    leaveSessionButton.setOnClickListener(new OnClickListener()
	    {
	      @Override
	      public void onClick(View v)
	      {
	        try
	        {
	          if( myClient.inSession() )
	            myClient.leaveSession(false);
	        }
	        catch( CollabrifyException e )
	        {
			  System.err.println("error " + e);
	        }
	      }
	    });

	    
	    
	    collabrifyListener = new CollabrifyAdapter()
	    {
	      @Override
	      public void onDisconnect()
	      {
	        System.out.println("disconnected");
	        runOnUiThread(new Runnable()
	        {

	          @Override
	          public void run()
	          {
	            createSessionButton.setText("Create");
	          }
	        });
	      }
	      //HERE2 for the command+f
	      @Override
	      public void onReceiveEvent(final long orderId, int subId,
	          String eventType, final byte[] data)
	      {
	        System.out.println("RECEIVED SUB ID:" + subId);
	        Log.i("success", "received correctly " + eventType.toString());
	        runOnUiThread(new Runnable()
	        {
	        	@Override
				public void run() 
	        	{
					//so what do we want to do when we receieve a move?
	        		//Log.i("success", "received correctly " + eventType.toString());
	        		try 
	        		{
						Move latestMove = Move.parseFrom(data); //gets the data from the move
						int userWhoMadeMove = latestMove.getUserId();
						int indexOfMover = 0; //defaulted as the person who made the move
						String moveData;
						int moveType = latestMove.getMoveType();
						int offsetValue = latestMove.getCursorChange();
						int undoValue = latestMove.getUndo();
						int recMoveId = latestMove.getMoveId();
						int cursStartLoc = 0;
						boolean found = false;
						for (int q = 0; q < cursors.size(); q++) //I wish we had a better way
						{
							if (cursors.get(q).userID == userWhoMadeMove)
							{
								cursStartLoc = cursors.get(q).cursorLoc;
								indexOfMover = q;
								found = true;
							}
						}
						if (found == false) //add new cursor to vector
						{
							Cursor newCursor = new Cursor(userWhoMadeMove, 0);
							indexOfMover = cursors.size()-1;
						}
						Log.i("print", "starting cursor loc: " + cursStartLoc);
						//---add-----
						if (moveType == 1)
						{
							moveData = latestMove.getData();
							Log.i("print", "add");
							Log.i("print", "UserID who made move: " + userWhoMadeMove);
							Log.i("print", "String added/deleted: " + moveData);
							Log.i("print", "offset value: " + offsetValue);
							Log.i("print", "undo value: " + undoValue);
							applyMove(indexOfMover, moveType, moveData, offsetValue, undoValue, recMoveId);
							//cursors.get(indexOfMover).cursorLoc += offsetValue;
							
							
							
						}
						//---delete----
						else if (moveType == 2)
						{
							moveData = latestMove.getData();
							Log.i("print", "delete");
							Log.i("print", "UserID who made move: " + userWhoMadeMove);
							Log.i("print", "String added/deleted: " + moveData);
							Log.i("print", "offset value: " + offsetValue);
							Log.i("print", "undo value: " + undoValue);
							applyMove(indexOfMover, moveType, moveData, offsetValue, undoValue, recMoveId);
						}
						//---cursorChange----
						else //should be moveType 3
						{
							moveData = ""; //because it doesn't matter
							Log.i("print", "cursorChange");
							Log.i("print", "UserID who made move: " + userWhoMadeMove);
							Log.i("print", "offset value: " + offsetValue);
							Log.i("print", "undo value: " + undoValue);
							applyMove(indexOfMover, moveType, moveData, offsetValue, undoValue, recMoveId);
						}
						//DON'T FORGET TO UPDATE CURSORS
					} 
	        		catch (InvalidProtocolBufferException e) 
	        		{
	        			Log.i("failed", "bad parse attempt: " + e);
						e.printStackTrace();
					}
				}

	        });
	      }

	      @Override
	      public void onReceiveSessionList(final List<CollabrifySession> sessionList)
	      {
	        if( sessionList.isEmpty() )
	        {
	          System.out.println("No session available");
	          return;
	        }
	        List<String> sessionNames = new ArrayList<String>();
	        for( CollabrifySession s : sessionList )
	        {
	          sessionNames.add(s.name());
	        }
	        final AlertDialog.Builder builder = new AlertDialog.Builder(
	            MainActivity.this);
	        builder.setTitle("Choose Session").setItems(
	            sessionNames.toArray(new String[sessionList.size()]),
	            new DialogInterface.OnClickListener()
	            {
	              @Override
	              public void onClick(DialogInterface dialog, int which)
	              {
	                try
	                {
	                  sessionId = sessionList.get(which).id();
	                  sessionName = sessionList.get(which).name();
	                  myClient.joinSession(sessionId, null);
	                  //everyone gets a random userId
	                }
	                catch( CollabrifyException e )
	                {
	                  System.err.println("error" + e);
	                }
	              }
	            });

	        runOnUiThread(new Runnable()
	        {

	          @Override
	          public void run()
	          {
	            builder.show();
	          }
	        });
	      }

	      @Override
	      public void onSessionCreated(long id)
	      {
	        System.out.println("Session created, id: " + id);
	        sessionId = id;
	        
	        runOnUiThread(new Runnable()
	        {

	          @Override
	          public void run()
	          {
	            createSessionButton.setText(sessionName);
	          }
	        });
	      }

	      @Override
	      public void onError(CollabrifyException e)
	      {
	       System.err.println("error" + e);
	      }

	      @Override
	      public void onSessionJoined(long maxOrderId, long baseFileSize)
	      {
	        Log.i("success", "joined session");
	        if( baseFileSize > 0 )
	        {
	          //initialize buffer to receive base file
	          baseFileReceiveBuffer = new ByteArrayOutputStream((int) baseFileSize);
	        }
	        runOnUiThread(new Runnable()
	        {

	          @Override
	          public void run()
	          {
	            createSessionButton.setText(sessionName);
	          }
	        });
	      }

	      /*
	       * (non-Javadoc)
	       * 
	       * @see
	       * edu.umich.imlc.collabrify.client.CollabrifyAdapter#onBaseFileChunkRequested
	       * (long)
	       */
	      @Override
	      public byte[] onBaseFileChunkRequested(long currentBaseFileSize)
	      {
	        // read up to max chunk size at a time
	        byte[] temp = new byte[CollabrifyClient.MAX_BASE_FILE_CHUNK_SIZE];
	        int read = 0;
	        try
	        {
	          read = baseFileBuffer.read(temp);
	        }
	        catch( IOException e )
	        {
	          // TODO Auto-generated catch block
	          e.printStackTrace();
	        }
	        if( read == -1 )
	        {
	          return null;
	        }
	        if( read < CollabrifyClient.MAX_BASE_FILE_CHUNK_SIZE )
	        {
	          // Trim garbage data
	          ByteArrayOutputStream bos = new ByteArrayOutputStream();
	          bos.write(temp, 0, read);
	          temp = bos.toByteArray();
	        }
	        return temp;
	      }

	      /*
	       * (non-Javadoc)
	       * 
	       * @see
	       * edu.umich.imlc.collabrify.client.CollabrifyAdapter#onBaseFileChunkReceived
	       * (byte[])
	       */
	      @Override
	      public void onBaseFileChunkReceived(byte[] baseFileChunk)
	      {
	        try
	        {
	          if( baseFileChunk != null )
	          {
	            baseFileReceiveBuffer.write(baseFileChunk);
	          }
	          else
	          {
	            runOnUiThread(new Runnable()
	            {
	              @Override
	              public void run()
	              {
	                to_broadcast.setText(baseFileReceiveBuffer.toString());
	              }
	            });
	            baseFileReceiveBuffer.close();
	          }
	        }
	        catch( IOException e )
	        {
	          // TODO Auto-generated catch block
	          e.printStackTrace();
	        }
	      }

	      /*
	       * (non-Javadoc)
	       * 
	       * @see
	       * edu.umich.imlc.collabrify.client.CollabrifyAdapter#onBaseFileUploadComplete
	       * (long)
	       */
	      @Override
	      public void onBaseFileUploadComplete(long baseFileSize)
	      {
	        runOnUiThread(new Runnable()
	        {

	          @Override
	          public void run()
	          {
	           // to_broadcast.setText(baseFileReceiveBuffer.toString());
	          }
	        });
	        try
	        {
	          baseFileBuffer.close();
	        }
	        catch( IOException e )
	        {
	          // TODO Auto-generated catch block
	          e.printStackTrace();
	        }
	      }
	    };

	    boolean getLatestEvent = false;

	    // Instantiate client object
	    try
	    {
	      myClient = new CollabrifyClient(this, "user email", "user display name",
	          "441fall2013@umich.edu", "XY3721425NoScOpE", getLatestEvent,
	          collabrifyListener);
	    }
	    catch( CollabrifyException e )
	    {
	      e.printStackTrace();
	    }

	    tags.add("sample");
		
	}
	
	
	
	/*
	 * generate action for insert/delete
	 */
	void generateInsertDelete()
	{
		Move retMove;
		if (continuousCount > 0) // add -- decently tested
		{
			int start = User.cursorLoc; //I can't forget to update User.cursorLoc
			Log.i("success", "start: " + start);
			
			myCursor.cursorLoc = User.cursorLoc;
			User.cursorLoc += continuousCount;
			
			
			System.out.println("user manual ADD: " + continuousString + ", after add, cursor @ " + User.cursorLoc);
			Log.i("success", "add: " + continuousString + " starting at: " + User.cursorLoc);
			
			Random rid = new Random();
			int mid = rid.nextInt();
			
			EditCom com = new EditCom(User.Operation.ADD, continuousString, continuousCount, mid);
			lastLocalMove = mid;
			User.undoList.add(com);
			retMove = com.generateMoveMes(0);
			try 
			{	
				myClient.broadcast(retMove.toByteArray(), "add"); //I only want to transfer the exact string I want
				//Log.i("success", "add broadcasting success: " + retMove.getData() );
			} 
			catch (CollabrifyException e) 
			{
				Log.i("failed", "add broadcasting failed");
				e.printStackTrace();
			}
		}
		else // delete
		{
			User.cursorLoc += continuousCount;
			
			//System.out.println("user manual DELETE: " + continuousString + ", after delete, cursor @ " + User.cursorLoc);
			Random rid = new Random();
			int mid = rid.nextInt();
			
			EditCom com = new EditCom(User.Operation.DELETE, continuousString, -continuousCount, mid);
			
			User.undoList.add(com);
			lastLocalMove = mid;
			retMove = com.generateMoveMes(0);
			try 
			{	
				myClient.broadcast(retMove.toByteArray(), "del");
				Log.i("success", "delete broadcasting success");
			} 
			catch (CollabrifyException e) 
			{
				Log.i("failed", "broadcasting failed");
				e.printStackTrace();
			}
		}
		
		continuousCount = 0;
		continuousString = continuousString.substring(0, 0);
		
		User.redoList.clear();
		
		// now broadcast retMove
	}
	
	
	
	  @Override
	  public boolean onCreateOptionsMenu(Menu menu)
	  {
	    getMenuInflater().inflate(R.menu.main, menu);
	    return true;
	  }
	  
	  private void applyMove(int indexOfUser, int mT, String data, int offset, int undo, int mid) 
	  {
		  //add/delete the characters
		  //update cursor locations
		  int startLoc = cursors.get(indexOfUser).cursorLoc;
		  //Log.i("apply", "start at: " + startLoc);
		  //---add-------------------------
		  if (mT == 1)
		  {
			  //Log.i("apply", "add mT: " + mT);
			  //Log.i("apply", "add data: " + data);
			  //Log.i("apply", "add length: " + offset);
			  //Log.i("apply", "add undo: " + undo);
			  //serverCopy = "blah";
			  //Log.i("apply", "final string?: " + serverCopy.substring(0,startLoc)+data+serverCopy.substring(startLoc, serverCopy.length()) );
			  
			  serverCopy = serverCopy.substring(0,startLoc)+data+serverCopy.substring(startLoc, serverCopy.length());
			  //Log.i("apply", serverCopy);
			  //now the server copy is properly updated every time...
			  if (mid == lastLocalMove)
			  {
				  //update the displayed copy when this happens
			  }
			  
			  for(int i = 0; i < cursors.size(); i++) //update all locations (works)
			  {
				  if (cursors.get(i).cursorLoc >= startLoc)
				  {
					  cursors.get(i).cursorLoc += offset;
				  }
			  }
			  //User.cursorLoc = cursors.get(0).cursorLoc; //keep things consistent
			  
		  } //-----end add-------------------
		  
		  else if (mT == 2)//-------delete
		  {
			  //Log.i("apply", "delete mT: " + mT);
			  //Log.i("apply", "delete data: " + data);
			  //Log.i("apply", "delete offset: " + offset);
			  serverCopy = serverCopy.substring(0,startLoc-offset) + serverCopy.substring(startLoc, serverCopy.length());
			  for(int i = 0; i < cursors.size(); i++) //update all locations (works)
			  {
				  if (cursors.get(i).cursorLoc >= startLoc)
				  {
					  cursors.get(i).cursorLoc -= offset;
				  }
				  if (cursors.get(i).cursorLoc < startLoc && cursors.get(i).cursorLoc > startLoc-offset )
				  {
					  cursors.get(i).cursorLoc = startLoc - offset;
				  }
			  }
			  
		  }//--------------end delete-------------
		  else //-------cursor move----------------
		  {
			  //Log.i("apply", "changing cursor location");
			  //Log.i("apply", "who moved: " + cursors.get(indexOfUser).userID);
			  //Log.i("apply", "move it how far?: " + offset); //I hope this is relative, and not indexed
			  cursors.get(indexOfUser).cursorLoc += offset;
			  
			  //for (int i = 0; i < cursors.size(); i++)
			  //{
				  //Log.i("apply", "final location1: " + cursors.get(i).cursorLoc );
				  
			  //}
			  //Log.i("apply", "final location2: " + User.cursorLoc);
			  
			  
		  } //-------end cursor move---------------
		  Log.i("apply", "final server copy: " + serverCopy);
		  for (int i = 0; i < cursors.size(); i++)
		  {
			  Log.i("apply", "final locations of" + i + ": " + cursors.get(i).cursorLoc );
		  }
		  
		  
		  
	  }
	
	
	
}
