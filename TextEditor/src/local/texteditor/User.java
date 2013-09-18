package local.texteditor;

import java.util.*;

import local.texteditor.MovesProtos.Move;
import android.widget.EditText;


public class User 
{
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
  protected static void Add(Character msg)
  {
	  isTextSetManually = false;
	  
	  to_broadcast.getText().insert(cursorLoc, msg.toString());
	  cursorLoc++;
	  
	  System.out.println("Program Add: " + msg + " @ " + (cursorLoc-1));
  }
 
  
  
  protected static void Delete()
  {
	  isTextSetManually = false;
	  
	  to_broadcast.getText().delete(cursorLoc-1, cursorLoc);
	  cursorLoc--;
	  
	  System.out.println("Program Delete " + "@ " + (cursorLoc+1));
  }
  
  
  
  protected static void CursorLocChangeForLocalUser(int newCursorLoc)
  {
    to_broadcast.setSelection(newCursorLoc);
    cursorLoc = newCursorLoc;
    
    System.out.println("CursurLocChange -> " + newCursorLoc);
  }
  
  
  
  protected static Move Undo()
  {
	isTextSetManually = false;  
	  
	/*
	 * if undoList is not empty  
	 */
    if (!undoList.empty())
    {
      EditCom com = undoList.lastElement();
      
      System.out.println("user manual undo: " + com.operation + com.mes + com.offset);
      
      if (com.operation == User.Operation.ADD)
      {
        Delete();
		Move newMove =
				Move.newBuilder()
				.setUserId(1) //need a user id
				.setMoveType(2)
				.setUndo(1)
				.build();
		redoList.push(undoList.pop());
        return newMove;
      }
      else if (com.operation == User.Operation.DELETE)
      {
    	Add(com.mes);
		Move newMove =
				Move.newBuilder()
				.setUserId(1) //need a user id
				.setMoveType(1)
				.setData( com.mes.toString() )
				.setUndo(1)
				.build();
		redoList.push(undoList.pop());
		return newMove;
      }
      else
      {
    	CursorLocChangeForLocalUser(cursorLoc - com.offset);
		Move newMove =
				Move.newBuilder()
				.setUserId(1) //need a user id
				.setMoveType(3)
				.setUndo(1)
				.build();
		redoList.push(undoList.pop());
		return newMove;
      }
    } 
    /*
     * if undo list is empty
     */
    else 
    {
      System.out.print("Nothing to undo! ");
      Move dummyMove = 
    		  Move.newBuilder()
    		  .setUserId(-1)
    		  .build();
      return dummyMove;
    }
    //System.out.println("# of undo/redo left: " + undoList.size() + " / " + redoList.size());
  }
  
  
  
  protected static Move Redo()
  {
	isTextSetManually = false;  
	  
    if (!redoList.empty())
    {
      EditCom com = redoList.lastElement();
      
      System.out.println("user manual redo: " + com.operation + com.mes + com.offset);
      
      if (com.operation == User.Operation.ADD)
      {
        Add(com.mes);
		Move newMove =
				Move.newBuilder()
				.setUserId(1) //need a user id
				.setMoveType(1)
				.setData( com.mes.toString() )
				.setUndo(2)
				.build();
		redoList.push(undoList.pop());
		undoList.push(redoList.pop());
		return newMove;
      }
      else if (com.operation == User.Operation.DELETE)
      {
    	Delete();
		Move newMove =
				Move.newBuilder()
				.setUserId(1) //need a user id
				.setMoveType(2)
				.setUndo(2)
				.build();
		redoList.push(undoList.pop());
		undoList.push(redoList.pop());
        return newMove;
    	
      }
      else
      {
    	CursorLocChangeForLocalUser(cursorLoc + com.offset);
		Move newMove =
				Move.newBuilder()
				.setUserId(1) //need a user id
				.setMoveType(3)
				.setUndo(1)
				.build();
		redoList.push(undoList.pop());
		undoList.push(redoList.pop());
		return newMove;
		
      }
         
    } 
    else 
    {
    	System.out.print("Nothing to redo! "); 
        Move dummyMove = 
      		  Move.newBuilder()
      		  .setUserId(-1)
      		  .build();
      return dummyMove;
    }
    //System.out.println("Num of undo/redo left: " + undoList.size() + " / " + redoList.size());
  }
  
}