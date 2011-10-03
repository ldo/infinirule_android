package nz.gen.geek_central.infinirule;

public class Main extends android.app.Activity
  {
    private java.util.Map<android.view.MenuItem, Runnable> OptionsMenu;

    interface RequestResponseAction /* response to an activity result */
      {
        public void Run
          (
            int ResultCode,
            android.content.Intent Data
          );
      } /*RequestResponseAction*/

    private java.util.Map<Integer, RequestResponseAction> ActivityResultActions;

  /* request codes, all arbitrarily assigned */
    static final int SetUpperScaleRequest = 1;
    static final int SetLowerScaleRequest = 2;

    private SlideView Slide;

    @Override
    public void onCreate
      (
        android.os.Bundle SavedInstanceState
      )
      {
        super.onCreate(SavedInstanceState);
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        BuildActivityResultActions();
        Global.MainMetrics = new android.util.DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(Global.MainMetrics);
        Scales.FontSize = getResources().getDimension(R.dimen.font_size);
        setContentView(R.layout.main);
        Slide = (SlideView)findViewById(R.id.slide_view);
      } /*onCreate*/

    @Override
    public boolean onCreateOptionsMenu
      (
        android.view.Menu TheMenu
      )
      {
        OptionsMenu = new java.util.HashMap<android.view.MenuItem, Runnable>();
        OptionsMenu.put
          (
            TheMenu.add(R.string.set_upper),
            new Runnable()
              {
                public void run()
                  {
                    ScalePicker.Launch(Main.this, true, SetUpperScaleRequest);
                  } /*run*/
              } /*Runnable*/
          );
        OptionsMenu.put
          (
            TheMenu.add(R.string.set_lower),
            new Runnable()
              {
                public void run()
                  {
                    ScalePicker.Launch(Main.this, false, SetLowerScaleRequest);
                  } /*run*/
              } /*Runnable*/
          );
        return
            true;
      } /*onCreateOptionsMenu*/

    void BuildActivityResultActions()
      {
        ActivityResultActions = new java.util.HashMap<Integer, RequestResponseAction>();
        ActivityResultActions.put
          (
            SetUpperScaleRequest,
            new RequestResponseAction()
              {
                public void Run
                  (
                    int ResultCode,
                    android.content.Intent Data
                  )
                  {
                    Slide.SetScale(Data.getStringExtra(ScalePicker.NameID), true);
                  } /*Run*/
              } /*RequestResponseAction*/
          );
        ActivityResultActions.put
          (
            SetLowerScaleRequest,
            new RequestResponseAction()
              {
                public void Run
                  (
                    int ResultCode,
                    android.content.Intent Data
                  )
                  {
                    Slide.SetScale(Data.getStringExtra(ScalePicker.NameID), false);
                  } /*Run*/
              } /*RequestResponseAction*/
          );
      } /*BuildActivityResultActions*/

    @Override
    public boolean onOptionsItemSelected
      (
        android.view.MenuItem TheItem
      )
      {
        boolean Handled = false;
        final Runnable Action = OptionsMenu.get(TheItem);
        if (Action != null)
          {
            Action.run();
            Handled = true;
          } /*if*/
        return
            Handled;
      } /*onOptionsItemSelected*/

    @Override
    public void onActivityResult
      (
        int RequestCode,
        int ResultCode,
        android.content.Intent Data
      )
      {
        ScalePicker.Cleanup();
        if (ResultCode != android.app.Activity.RESULT_CANCELED)
          {
            final RequestResponseAction Action = ActivityResultActions.get(RequestCode);
            if (Action != null)
              {
                Action.Run(ResultCode, Data);
              } /*if*/
          } /*if*/
      } /*onActivityResult*/

  } /*Main*/
