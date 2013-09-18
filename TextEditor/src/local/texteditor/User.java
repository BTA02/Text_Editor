package local.texteditor;

import java.util.*;
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
  
  
  
  protected static void Undo()
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
      }
      else if (com.operation == User.Operation.DELETE)
      {
    	Add(com.mes);  
      }
      else
      {
    	CursorLocChangeForLocalUser(cursorLoc - com.offset);  
      }
      redoList.push(undoList.pop());   
    } 
    /*
     * if undo list is empty
     */
    else 
    {
      System.out.print("Nothing to undo! "); 
    }
    System.out.println("# of undo/redo left: " + undoList.size() + " / " + redoList.size());
  }
  
  
  
  protected static void Redo()
  {
	isTextSetManually = false;  
	  
    if (!redoList.empty())
    {
      EditCom com = redoList.lastElement();
      
      System.out.println("user manual redo: " + com.operation + com.mes + com.offset);
      
      if (com.operation == User.Operation.ADD)
      {
        Add(com.mes);
      }
      else if (com.operation == User.Operation.DELETE)
      {
    	Delete();  
      }
      else
      {
    	CursorLocChangeForLocalUser(cursorLoc + com.offset);  
      }
      undoList.push(redoList.pop());   
    } 
    else 
    {
      System.out.print("Nothing to redo! "); 
    }
    System.out.println("Num of undo/redo left: " + undoList.size() + " / " + redoList.size());
  }
  
}