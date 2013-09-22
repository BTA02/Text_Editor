package local.texteditor;

import java.util.*;
import android.widget.EditText;


public class User 
{
	protected static int Id = 0;	
	protected static EditText to_broadcast;
	protected static boolean isTextSetManually = true;
	protected static int cursorLoc = 0;
	protected static Stack<EditCom> 
    	undoList = new Stack<EditCom> (), 
    	redoList = new Stack<EditCom> ();
  
	// first person is user 0, and on and on.....
	//private int userIdInt;
	
	//private static Vector cursorOfUsers = new Vector (1);
	//vector[0] = where my cursor is
	public enum Operation
	{
		ADD, DELETE, CURSOR, INIT
	}

	
  
	/*
	 * implementation of add, delete, cursor location change, and undo/redo
	 */
	protected static void Add(int userId, int count, String msg)
	{
		isTextSetManually = false;
	
		if (userId == Id) //called for local undo/redo
		{
			to_broadcast.getText().insert(cursorLoc, msg);
			cursorLoc += count;
		} 
		else // called for other users' add
		{ 
		}
	  
		System.out.println("Program ADD from user " + userId + ": " + msg + " @ " + (cursorLoc-count));
	}
 
  
  
	protected static void Delete(int userId, int count) 
	{
		isTextSetManually = false;
	
		if (userId == Id) //called for local undo/redo
		{
			to_broadcast.getText().delete(cursorLoc-count, cursorLoc);
			cursorLoc -= count;
		}
		else // called for other users' delete
		{
		}
	  
		System.out.println("Program DELETE from user " + userId + "of length " + count + " @ " + (cursorLoc+count));
	}
  
  
  
	protected static void CursorChange(int userId, int offset)
	{
		if (userId == Id) //called for local cursor change
		{
			to_broadcast.setSelection(cursorLoc + offset);
			cursorLoc += offset;	
			System.out.println("LOCAL CURSOR CHANGE from " + (cursorLoc-offset) + " to " + cursorLoc);
		}
		else // called for other users' cursor change
		{		
		}
	}
  
  
  
	protected static EditCom Undo()
  	{
	  	isTextSetManually = false;  

	  	if (!undoList.empty()) // if undoList is not empty
	  	{
	  		EditCom com = undoList.lastElement();
      
	  		System.out.println("user manual UNDO: " + com.operation + com.mes + com.offset);
      
	  		if (com.operation == User.Operation.ADD)
	  			Delete(Id, com.offset);
	  		else if (com.operation == User.Operation.DELETE)
	  			Add(Id, com.offset, com.mes);
	  		else
	  			CursorChange(Id, -com.offset);
  			redoList.push(undoList.pop());
	  		
	  		System.out.println("# of undo/redo left: " + undoList.size() + " / " + redoList.size());
	  		
	  		return com;
	  	} 
	  	else // if undo list is empty
	  	{
	  		System.out.print("Nothing to undo! ");
	  		System.out.println("# of undo/redo left: " + undoList.size() + " / " + redoList.size());
	  		
	  		return null;
	  	}
  	}
  
  
  
	protected static EditCom Redo()
	{
		isTextSetManually = false;  
	  
		if (!redoList.empty()) // redoList is not empty
		{
			EditCom com = redoList.lastElement();
      
			System.out.println("user manual REDO: " + com.operation + com.mes + com.offset);
      
			if (com.operation == User.Operation.ADD)
				Add(Id, com.offset, com.mes);
			else if (com.operation == User.Operation.DELETE)
				Delete(Id, com.offset);
			else
				CursorChange(Id, com.offset);
			undoList.push(redoList.pop());
			
			System.out.println("Num of undo/redo left: " + undoList.size() + " / " + redoList.size());
			
			return com;
		} 
		else // redo list is empty
		{
			System.out.print("Nothing to redo! "); 
			System.out.println("Num of undo/redo left: " + undoList.size() + " / " + redoList.size());

			return null;
		}
	}
  
}