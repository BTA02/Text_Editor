package local.texteditor;

import java.util.Stack;

import android.widget.EditText;


public class User 
{
  protected static EditText to_broadcast;
  
  private static int cursorLoc = 0;
  private static Stack<EditCom> 
    undoList = new Stack<EditCom> (), 
    redoList = new Stack<EditCom> ();
  private int userIdInt; //first person is user 0, and on and on.....
  //vector[0] = where my cursor is
  
  protected static void MoveChars(int startLoc, int length, int newLoc) 
  {


    undoList.add(new EditCom("mov", startLoc, length, newLoc));
    
    System.out.println("MoveChars ->" + undoList.lastElement());
  }
  
  protected static void CursorLocChange(int newCursorLoc)
  {
    to_broadcast.setSelection(newCursorLoc);
    cursorLoc = newCursorLoc;
    System.out.println("CursurLocChange ->" + newCursorLoc);
  }
  
  protected static void Undo()
  {
    if (!undoList.empty())
    {
      EditCom com = undoList.lastElement();
      System.out.println("Undo ->" + com);
      if (com.command == "mov")
      {
        MoveChars(com.moveToLoc, com.length, com.startLoc);
      }
      redoList.push(undoList.pop());   
    } else {
      System.out.println("Nothing to undo"); 
    }
  }
  
  protected static void Redo()
  {
    if (!redoList.empty())
    {
      EditCom com = redoList.lastElement();
      System.out.println("Redo ->" + com);
      if (com.command == "mov")
      {
        MoveChars(com.startLoc, com.length, com.moveToLoc);
      }
      undoList.push(redoList.pop());   
    } else {
      System.out.println("Nothing to redo"); 
    }
  }
  
  protected static void Add()
  {
	  
  }
  
  protected static void Del()
  {
	  
  }
  
}