package nz.gen.geek_central.infinirule;

public class Main extends android.app.Activity
  {

    @Override
    public void onCreate
      (
        android.os.Bundle SavedInstanceState
      )
      {
        super.onCreate(SavedInstanceState);
        setContentView(R.layout.main);
        ((SlideView)findViewById(R.id.slide_view)).SetLabelViews
          (
            /*UpperLabel =*/ (android.widget.TextView)findViewById(R.id.label_upper),
            /*LowerLabel =*/ (android.widget.TextView)findViewById(R.id.label_lower)
          );
      /* more TBD */
      } /*onCreate*/

  } /*Main*/
