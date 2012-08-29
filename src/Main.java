package nz.gen.geek_central.infinirule;
/*
    Infinirule--the infinitely stretchable and scrollable slide rule, mainline.

    Copyright 2011, 2012 Lawrence D'Oliveiro <ldo@geek-central.gen.nz>.

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

public class Main
    extends android.app.Activity
    implements SlideView.ContextMenuAction
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

    public void ShowHelp
      (
        String Path,
        String[] FormatArgs
      )
      /* launches the Help activity, displaying the page in my resources with
        the specified Path. */
      {
        final android.content.Intent LaunchHelp =
            new android.content.Intent(android.content.Intent.ACTION_VIEW);
      /* must always load the page contents, can no longer pass a file:///android_asset/
        URL with Android 4.0. */
        byte[] HelpRaw;
          {
            java.io.InputStream ReadHelp;
            try
              {
                ReadHelp = getAssets().open(Path);
                HelpRaw = ReadAll(ReadHelp);
              }
            catch (java.io.IOException Failed)
              {
                throw new RuntimeException("can't read help page: " + Failed);
              } /*try*/
            try
              {
                ReadHelp.close();
              }
            catch (java.io.IOException WhoCares)
              {
              /* I mean, really? */
              } /*try*/
          }
        LaunchHelp.putExtra
          (
            nz.gen.geek_central.infinirule.Help.ContentID,
            FormatArgs != null ?
                String.format(Global.StdLocale, new String(HelpRaw), FormatArgs)
                    .getBytes()
            :
                HelpRaw
          );
        LaunchHelp.setClass(this, Help.class);
        startActivity(LaunchHelp);
      } /*ShowHelp*/

    private java.util.Map<android.view.MenuItem, Runnable> OptionsMenu;
    private java.util.Map<android.view.MenuItem, Runnable> ContextMenu;

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
        public final int RequestCode;
        public final Global.ScaleSelector WhichScale;

        public SetScaleEntry
          (
            int RequestCode,
            Global.ScaleSelector WhichScale
          )
          {
            this.RequestCode = RequestCode;
            this.WhichScale = WhichScale;
          } /*SetScaleEntry*/
      } /*SetScaleEntry*/
    private static final SetScaleEntry[] WhichScales =
        new SetScaleEntry[]
            {
                new SetScaleEntry(SetTopScaleRequest, Global.ScaleSelector.TopScale),
                new SetScaleEntry(SetUpperScaleRequest, Global.ScaleSelector.UpperScale),
                new SetScaleEntry(SetLowerScaleRequest, Global.ScaleSelector.LowerScale),
                new SetScaleEntry(SetBottomScaleRequest, Global.ScaleSelector.BottomScale),
            };

    private SlideView Slide;
    private android.text.ClipboardManager Clipboard;

    @Override
    public void onCreate
      (
        android.os.Bundle SavedInstanceState
      )
      {
        super.onCreate(SavedInstanceState);
        getWindow().requestFeature(android.view.Window.FEATURE_CUSTOM_TITLE);
        Clipboard = (android.text.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        BuildActivityResultActions();
        Global.MainMetrics = new android.util.DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(Global.MainMetrics);
          {
            final android.content.res.Resources Resources = getResources();
            Scales.BackgroundColor = Resources.getColor(R.color.background);
            Scales.MainColor = Resources.getColor(R.color.main);
            Scales.AltColor = Resources.getColor(R.color.alt);
            Scales.SpecialMarkerColor = Resources.getColor(R.color.special_marker);
            Scales.CursorFillColor = Resources.getColor(R.color.cursor_fill);
            Scales.CursorEdgeColor = Resources.getColor(R.color.cursor_edge);
            Scales.PrimaryMarkerLength = Resources.getDimension(R.dimen.primary_marker_length);
            Scales.FontSize = Resources.getDimension(R.dimen.font_size);
            Scales.HalfLayoutHeight = Resources.getDimension(R.dimen.half_layout_height);
            Scales.HalfCursorWidth = Resources.getDimension(R.dimen.half_cursor_width);
          }
        setContentView(R.layout.main);
        Slide = (SlideView)findViewById(R.id.slide_view);
        Slide.SetContextMenuAction(this);
      } /*onCreate*/

    @Override
    public void onPostCreate
      (
        android.os.Bundle SavedInstanceState
      )
      {
        super.onPostCreate(SavedInstanceState);
        getWindow().setFeatureInt
          (
            android.view.Window.FEATURE_CUSTOM_TITLE,
            R.layout.title_bar
          );
        ((android.widget.Button)findViewById(R.id.action_help)).setOnClickListener
          (
            new android.view.View.OnClickListener()
              {
                public void onClick
                  (
                    android.view.View ButtonView
                  )
                  {
                    ShowHelp("help/index.html", null);
                  } /*onClick*/
              } /*OnClickListener*/
          );
      } /*onPostCreate*/

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
                    ShowHelp("help/index.html", null);
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
                    Slide.Reset(true);
                  } /*run*/
              } /*Runnable*/
          );
        OptionsMenu.put
          (
            TheMenu.add(R.string.about_me),
            new Runnable()
              {
                public void run()
                  {
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
                    ShowHelp("help/about.html", new String[] {VersionName});
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

    public void CreateContextMenu
      (
        android.view.ContextMenu TheMenu,
        SlideView.ContextMenuTypes MenuType
      )
      {
        ContextMenu = new java.util.HashMap<android.view.MenuItem, Runnable>();
        switch (MenuType)
          {
        case Cursor:
            for (boolean Pasting = false;;)
              {
                for (final SetScaleEntry s : WhichScales)
                  {
                    ContextMenu.put
                      (
                        TheMenu.add
                          (
                            String.format
                              (
                                getString(Pasting ? R.string.paste_prompt : R.string.copy_prompt),
                                getString(Global.ScaleNameID(s.WhichScale))
                              )
                          ),
                        Pasting ?
                            new Runnable()
                              {
                                public void run()
                                  {
                                    final CharSequence NumString = Clipboard.getText();
                                    if (NumString != null)
                                      {
                                        final Scales.Scale TheScale = Slide.GetScale(s.WhichScale);
                                        try
                                          {
                                            final double Value = Double.parseDouble(NumString.toString());
                                            double ScalePos = TheScale.PosAt(Value);
                                            if
                                              (
                                                    TheScale.Wrap()
                                                ||
                                                    ScalePos >= 0.0 && ScalePos < 1.0
                                              )
                                              {
                                                Slide.SetCursorPos
                                                  (
                                                    /*ByScale =*/ s.WhichScale,
                                                    /*NewPos =*/ ScalePos - Math.floor(ScalePos),
                                                    /*Animate =*/ true
                                                  );
                                              }
                                            else
                                              {
                                                android.widget.Toast.makeText
                                                  (
                                                    /*context =*/ Main.this,
                                                    /*text =*/
                                                        String.format
                                                          (
                                                            getString(R.string.paste_range),
                                                            Global.FormatNumber(Value),
                                                            getString(Global.ScaleNameID(s.WhichScale)),
                                                            Global.FormatNumber(TheScale.ValueAt(0.0)),
                                                            Global.FormatNumber(TheScale.ValueAt(1.0))
                                                          ),
                                                    /*duration =*/ android.widget.Toast.LENGTH_SHORT
                                                  ).show();
                                              } /*if*/
                                          }
                                        catch (NumberFormatException BadNum)
                                          {
                                            android.widget.Toast.makeText
                                              (
                                                /*context =*/ Main.this,
                                                /*text =*/ getString(R.string.paste_nan),
                                                /*duration =*/ android.widget.Toast.LENGTH_SHORT
                                              ).show();
                                          } /*try*/
                                      } /*if*/
                                  } /*run*/
                              } /*Runnable*/
                        :
                            new Runnable()
                              {
                                public void run()
                                  {
                                    Clipboard.setText
                                      (
                                        Global.FormatNumber
                                          (
                                            Slide.GetScale(s.WhichScale)
                                                .ValueAt(Slide.GetCursorPos(s.WhichScale))
                                          )
                                      );
                                  } /*run*/
                              } /*Runnable*/
                      );
                  } /*for*/
                if (Pasting)
                    break;
                Pasting = true;
              } /*for*/
        break;
        case Scales:
            for (final SetScaleEntry s : WhichScales)
              {
                ContextMenu.put
                  (
                    TheMenu.add
                      (
                        String.format
                          (
                            getString(R.string.set_scale),
                            getString(Global.ScaleNameID(s.WhichScale))
                          )
                      ),
                    new Runnable()
                      {
                        public void run()
                          {
                            ScalePicker.Launch
                              (
                                /*Caller =*/ Main.this,
                                /*WhichScale =*/ s.WhichScale,
                                /*RequestCode =*/s.RequestCode,
                                /*CurScaleName =*/ Slide.GetScale(s.WhichScale).Name()
                              );
                          } /*run*/
                      } /*Runnable*/
                  );
              } /*for*/
        break;
          } /*switch*/
      } /*CreateContextMenu*/

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
    public boolean onContextItemSelected
      (
        android.view.MenuItem TheItem
      )
      {
        boolean Handled = false;
        final Runnable Action = ContextMenu.get(TheItem);
        if (Action != null)
          {
            Action.run();
            Handled = true;
          } /*if*/
        return
            Handled;
      } /*onContextItemSelected*/

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

    @Override
    public void onSaveInstanceState
      (
        android.os.Bundle ToSaveInstanceState
      )
      {
        super.onSaveInstanceState(ToSaveInstanceState);
        ToSaveInstanceState.putAll(Slide.SaveState());
      } /*onPause*/

    @Override
    public void onRestoreInstanceState
      (
        android.os.Bundle SavedInstanceState
      )
      {
        Slide.RestoreState(SavedInstanceState);
        super.onRestoreInstanceState(SavedInstanceState);
      } /*onResume*/

  } /*Main*/
