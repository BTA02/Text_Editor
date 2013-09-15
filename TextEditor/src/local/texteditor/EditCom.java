package local.texteditor;

public class EditCom
{
  public String command, previousMes, newMes, charsChanged;
  public int startLoc, length, moveToLoc;
  
  public EditCom(String commandT, int startLocT, int lengthT, int moveToLocT, String nMes, String pMes)
  {
    command = commandT;
    startLoc = startLocT;
    length = lengthT;
    moveToLoc = moveToLocT;
    newMes = nMes; //characters added or deleted
    previousMes = pMes;
  }
}