package com.example.collabrify_app;

import android.os.Bundle;
import android.app.Activity;
import android.text.Selection;
import android.text.Spannable;
import android.view.DragEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity
{
  
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    
    final EditText textBody = (EditText) findViewById(R.id.TextBody);
    Button undoButton = (Button) findViewById(R.id.UndoButton);
    Button redoButton = (Button) findViewById(R.id.RedoButton);
    User.textBody = textBody;
    
    undoButton.setOnClickListener(new OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
         User.Undo();
      }
    });
    redoButton.setOnClickListener(new OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
         User.Redo();
      }
    });
    textBody.setSingleLine(false);   
    textBody.setHorizontallyScrolling(false);
    textBody.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) 
      {
        User.CursorLocChange(textBody.getSelectionEnd());
        
      }
    });
    
    textBody.setOnDragListener(new OnDragListener(){

      @Override
      public boolean onDrag(View v, DragEvent event)
      {
        // TODO Auto-generated method stub
        return false;
      }
      
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

}
