package local.texteditor;


public class EditCom
{
  User.Operation operation;
  public int offset;
  public Character mes;
  
  public EditCom(User.Operation operationT, Character mesT, int offsetT)
  {
    operation = operationT;
    mes = mesT;
    offset = offsetT;  
  }
}