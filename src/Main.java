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
    private static final int SetTopScaleRequest = 1;
    private static final int SetUpperScaleRequest = 2;
    private static final int SetLowerScaleRequest = 3;
    private static final int SetBottomScaleRequest = 4;

    private static class SetScaleEntry
      {
        public final int ResID;
        public final int RequestCode;
        public final Global.ScaleSelector WhichScale;

        public SetScaleEntry
          (
            int ResID,
            int RequestCode,
            Global.ScaleSelector WhichScale
          )
          {
            this.ResID = ResID;
            this.RequestCode = RequestCode;
            this.WhichScale = WhichScale;
          } /*SetScaleEntry*/
      } /*SetScaleEntry*/
    private static final SetScaleEntry[] WhichScales =
        new SetScaleEntry[]
            {
                new SetScaleEntry(R.string.set_top, SetTopScaleRequest, Global.ScaleSelector.TopScale),
                new SetScaleEntry(R.string.set_upper, SetUpperScaleRequest, Global.ScaleSelector.UpperScale),
                new SetScaleEntry(R.string.set_lower, SetLowerScaleRequest, Global.ScaleSelector.LowerScale),
                new SetScaleEntry(R.string.set_bottom, SetBottomScaleRequest, Global.ScaleSelector.BottomScale),
            };

    private SlideView Slide;

    @Override
    public void onCreate
      (
        android.os.Bundle SavedInstanceState
      )
      {
        super.onCreate(SavedInstanceState);
        BuildActivityResultActions();
        Global.MainMetrics = new android.util.DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(Global.MainMetrics);
          {
            final android.content.res.Resources Resources = getResources();
            Scales.BackgroundColor = Resources.getColor(R.color.background);
            Scales.MainColor = Resources.getColor(R.color.main);
            Scales.AltColor = Resources.getColor(R.color.alt);
            Scales.CursorFillColor = Resources.getColor(R.color.cursor_fill);
            Scales.CursorEdgeColor = Resources.getColor(R.color.cursor_edge);
            Scales.PrimaryMarkerLength = Resources.getDimension(R.dimen.primary_marker_length);
            Scales.FontSize = Resources.getDimension(R.dimen.font_size);
            Scales.HalfLayoutHeight = Resources.getDimension(R.dimen.half_layout_height);
            Scales.HalfCursorWidth = Resources.getDimension(R.dimen.half_cursor_width);
          }
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
        for (final SetScaleEntry s : WhichScales)
          {
            OptionsMenu.put
              (
                TheMenu.add(s.ResID),
                new Runnable()
                  {
                    public void run()
                      {
                        ScalePicker.Launch
                          (
                            /*Caller =*/ Main.this,
                            /*WhichScale =*/ s.WhichScale,
                            /*RequestCode =*/s.RequestCode,
                            /*CurScaleName =*/ Slide.GetScaleName(s.WhichScale)
                          );
                      } /*run*/
                  } /*Runnable*/
              );
          } /*for*/
        return
            true;
      } /*onCreateOptionsMenu*/

    void BuildActivityResultActions()
      {
        ActivityResultActions = new java.util.HashMap<Integer, RequestResponseAction>();
        for (final SetScaleEntry s : WhichScales)
          {
            ActivityResultActions.put
              (
                s.RequestCode,
                new RequestResponseAction()
                  {
                    public void Run
                      (
                        int ResultCode,
                        android.content.Intent Data
                      )
                      {
                        Slide.SetScale(Data.getStringExtra(ScalePicker.NameID), s.WhichScale);
                      } /*Run*/
                  } /*RequestResponseAction*/
              );
          } /*for*/
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
