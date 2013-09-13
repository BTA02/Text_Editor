package com.example.collabrify_app;

public class EditCom
{
  public String command, previousMes, newMes;
  public int startLoc, length, moveToLoc;
  
  public EditCom(String commandT, int startLocT, int lengthT, int moveToLocT)
  {
    command = commandT;
    startLoc = startLocT;
    length = lengthT;
    moveToLoc = moveToLocT;
  }
}
