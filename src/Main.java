package nz.gen.geek_central.infinirule;
/*
    Infinirule--the infinitely stretchable and scrollable slide rule, mainline.

    Copyright 2011 Lawrence D'Oliveiro <ldo@geek-central.gen.nz>.

    This program is free software: you can redistribute it and/or
    modify it under the terms of the GNU General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
    General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see
    <http://www.gnu.org/licenses/>.
*/

public class Main extends android.app.Activity
  {

    public static byte[] ReadAll
      (
        java.io.InputStream From
      )
      /* reads all available data from From. */
    throws java.io.IOException
      {
        java.io.ByteArrayOutputStream Result = new java.io.ByteArrayOutputStream();
        final byte[] Buf = new byte[256]; /* just to reduce number of I/O operations */
        for (;;)
          {
            final int BytesRead = From.read(Buf);
            if (BytesRead < 0)
                break;
            Result.write(Buf, 0, BytesRead);
          } /*for*/
        return
            Result.toByteArray();
      } /*ReadAll*/

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
        OptionsMenu.put
          (
            TheMenu.add(R.string.show_help),
            new Runnable()
              {
                public void run()
                  {
                    startActivity
                      (
                        new android.content.Intent
                          (
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.fromParts
                              (
                                "file",
                                "/android_asset/help/index.html",
                                null
                              )
                          ).setClass(Main.this, Help.class)
                      );
                  } /*run*/
              } /*Runnable*/
          );
        OptionsMenu.put
          (
            TheMenu.add(R.string.reset),
            new Runnable()
              {
                public void run()
                  {
                    Slide.Reset();
                  } /*run*/
              } /*Runnable*/
          );
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
        OptionsMenu.put
          (
            TheMenu.add(R.string.about_me),
            new Runnable()
              {
                public void run()
                  {
                    final android.content.Intent ShowAbout =
                        new android.content.Intent(android.content.Intent.ACTION_VIEW);
                    byte[] AboutRaw;
                      {
                        java.io.InputStream ReadAbout;
                        try
                          {
                            ReadAbout = getAssets().open("help/about.html");
                            AboutRaw = ReadAll(ReadAbout);
                          }
                        catch (java.io.IOException Failed)
                          {
                            throw new RuntimeException("can't read about page: " + Failed);
                          } /*try*/
                        try
                          {
                            ReadAbout.close();
                          }
                        catch (java.io.IOException WhoCares)
                          {
                          /* I mean, really? */
                          } /*try*/
                      }
                    String VersionName;
                    try
                      {
                        VersionName =
                            getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                      }
                    catch (android.content.pm.PackageManager.NameNotFoundException CantFindMe)
                      {
                        VersionName = "CANTFINDME"; /*!*/
                      } /*catch*/
                    ShowAbout.putExtra
                      (
                        nz.gen.geek_central.infinirule.Help.ContentID,
                        String.format(Global.StdLocale, new String(AboutRaw), VersionName)
                            .getBytes()
                      );
                    ShowAbout.setClass(Main.this, Help.class);
                    startActivity(ShowAbout);
                  } /*run*/
              } /*Runnable*/
          );
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
