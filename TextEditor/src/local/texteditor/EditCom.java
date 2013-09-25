package local.texteditor;

import local.texteditor.MovesProtos.Move;


public class EditCom
{
  User.Operation operation;
  public int offset;
  public String mes;
  public int moveId;
  
  public EditCom(User.Operation operationT, String mesT, int offsetT, int mid)
  {
    operation = operationT;
    mes = mesT;
    offset = offsetT;  
    moveId = mid;
  }
  
  public Move generateMoveMes(int undo)
  {
		Move move;
		if (this.operation == User.Operation.ADD)
			move = Move.newBuilder()
					.setUserId(User.Id) //need a user id
					.setMoveType(1)
					.setData(this.mes)
					.setCursorChange(this.offset)
					.setUndo(undo)
					.setMoveId(this.moveId)
					.build();
	
		else if(this.operation == User.Operation.DELETE)
			move = Move.newBuilder()
					.setUserId(User.Id)//need a user id
					.setMoveType(2)
					.setCursorChange(this.offset)
					.setUndo(undo)
					.setMoveId(this.moveId)
					.build();
		else
			move = Move.newBuilder()
					.setUserId(User.Id) //need a user id
					.setMoveType(3)
					.setCursorChange(this.offset)
					.setUndo(undo)
					.setMoveId(this.moveId)
					.build();	
		return move;  
  }
 }