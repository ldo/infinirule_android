package nz.gen.geek_central.infinirule;
/*
    Let the user choose a new upper or lower scale
*/

public class ScalePicker extends android.app.Activity
  {
    public static final String NameID = "nz.gen.geek_central.infinirule.name";
    public static final String UpperID = "nz.gen.geek_central.infinirule.upper";

    private static boolean Reentered = false; /* sanity check */
    private static ScalePicker Current = null;

    private static boolean Upper;

    private android.widget.ListView PickerListView;
    private SelectedItemAdapter PickerList;

    public static class PickerItem
      {
        String Name;
        boolean Selected;

        public PickerItem
          (
            String Name
          )
          {
            this.Name = Name;
            this.Selected = false;
          } /*PickerItem*/

      } /*PickerItem*/

    class SelectedItemAdapter extends android.widget.ArrayAdapter<PickerItem>
      {
        final int ResID;
        final android.view.LayoutInflater TemplateInflater;
        PickerItem CurSelected;
        android.widget.RadioButton LastChecked;

        class OnSetCheck implements android.view.View.OnClickListener
          {
            final PickerItem MyItem;

            public OnSetCheck
              (
                PickerItem TheItem
              )
              {
                MyItem = TheItem;
              } /*OnSetCheck*/

            public void onClick
              (
                android.view.View TheView
              )
              {
                if (MyItem != CurSelected)
                  {
                  /* only allow one item to be selected at a time */
                    if (CurSelected != null)
                      {
                        CurSelected.Selected = false;
                        LastChecked.setChecked(false);
                      } /*if*/
                    LastChecked =
                        TheView instanceof android.widget.RadioButton ?
                            (android.widget.RadioButton)TheView
                        :
                            (android.widget.RadioButton)
                            ((android.view.ViewGroup)TheView).findViewById(R.id.item_checked);
                    CurSelected = MyItem;
                    MyItem.Selected = true;
                    LastChecked.setChecked(true);
                  } /*if*/
              } /*onClick*/
          } /*OnSetCheck*/

        SelectedItemAdapter
          (
            android.content.Context TheContext,
            int ResID,
            android.view.LayoutInflater TemplateInflater
          )
          {
            super(TheContext, ResID);
            this.ResID = ResID;
            this.TemplateInflater = TemplateInflater;
            CurSelected = null;
            LastChecked = null;
          } /*SelectedItemAdapter*/

        @Override
        public android.view.View getView
          (
            int Position,
            android.view.View ReuseView,
            android.view.ViewGroup Parent
          )
          {
            android.view.View TheView = ReuseView;
            if (TheView == null)
              {
                TheView = TemplateInflater.inflate(ResID, null);
              } /*if*/
            final PickerItem ThisItem = (PickerItem)this.getItem(Position);
            final android.widget.ImageView ItemDisplay =
                (android.widget.ImageView)TheView.findViewById(R.id.select_name);
              {
                final android.graphics.Paint LabelHow = new android.graphics.Paint();
                LabelHow.setTypeface(Scales.NormalStyle);
                LabelHow.setTextSize(Scales.FontSize);
                final android.graphics.Rect TextBounds = new android.graphics.Rect();
                LabelHow.getTextBounds("W", 0, 1, TextBounds);
                final android.graphics.Bitmap ItemBits = android.graphics.Bitmap.createBitmap
                  (
                    /*width =*/
                        Math.max
                          (
                            (int)(
                                Scales.DrawLabel
                                  (
                                    /*g =*/ null,
                                    /*Scale =*/ Scales.KnownScales.get(ThisItem.Name),
                                    /*Upper =*/ Upper,
                                    /*Pos =*/ null,
                                    /*Alignment =*/ android.graphics.Paint.Align.LEFT,
                                    /*Color =*/ 0
                                  )
                              *
                                1.2
                              *
                                Global.PixelDensity()
                            ),
                            100
                          ),
                    /*height =*/
                        Math.max
                          (
                            (int)(
                                (TextBounds.bottom - TextBounds.top)
                            *
                                2.0
                            *
                                Global.PixelDensity()
                            ),
                            64
                          ),
                    /*config =*/ android.graphics.Bitmap.Config.ARGB_8888
                  );
                ItemBits.setDensity(Global.MainMetrics.densityDpi);
                final android.graphics.Canvas ItemDraw = new android.graphics.Canvas(ItemBits);
                ItemDraw.drawColor(0xff000000);
                Scales.DrawLabel
                  (
                    /*g =*/ ItemDraw,
                    /*Scale =*/ Scales.KnownScales.get(ThisItem.Name),
                    /*Upper =*/ Upper,
                    /*Pos =*/
                        new android.graphics.PointF
                          (
                            TextBounds.right - TextBounds.left,
                            ItemBits.getHeight() / 2 - (TextBounds.bottom + TextBounds.top) / 2.0f
                          ),
                    /*Alignment =*/ android.graphics.Paint.Align.LEFT,
                    /*Color =*/ 0xffffffff
                  );
                ItemBits.prepareToDraw();
                ItemDisplay.setImageBitmap(ItemBits); /* should I bother recycling ItemBits? */
              }
            android.widget.RadioButton ThisChecked =
                (android.widget.RadioButton)TheView.findViewById(R.id.item_checked);
            ThisChecked.setChecked(ThisItem.Selected);
            final OnSetCheck ThisSetCheck = new OnSetCheck(ThisItem);
            ThisChecked.setOnClickListener(ThisSetCheck);
              /* otherwise radio button can get checked but I don't notice */
            TheView.setOnClickListener(ThisSetCheck);
            return
                TheView;
          } /*getView*/

      } /*SelectedItemAdapter*/

    @Override
    public void onCreate
      (
        android.os.Bundle SavedInstanceState
      )
      {
        super.onCreate(SavedInstanceState);
        Current = this;
        setContentView(R.layout.scale_picker);
        ((android.widget.TextView)findViewById(R.id.prompt)).setText
          (
            String.format
              (
                Global.StdLocale,
                getString(R.string.picker_prompt),
                getString(Upper ? R.string.upper : R.string.lower)
              )
          );
        PickerList = new SelectedItemAdapter(this, R.layout.scale_picker_item, getLayoutInflater());
        PickerList.clear();
          {
            final java.util.TreeSet<PickerItem> ResultTemp =
                new java.util.TreeSet<PickerItem>
                  (
                    new java.util.Comparator<PickerItem>()
                      {
                        @Override
                        public int compare
                          (
                            PickerItem Item1,
                            PickerItem Item2
                          )
                          {
                            return
                                Item1.Name.compareTo(Item2.Name);
                          } /*compare*/
                      } /*Comparator*/
                  );
            for
              (
                java.util.Map.Entry<String, Scales.Scale> ThisScale :
                    Scales.KnownScales.entrySet()
              )
              {
                ResultTemp.add(new PickerItem(ThisScale.getValue().Name()));
              } /*for*/
            for (PickerItem ThisItem : ResultTemp)
              {
                PickerList.add(ThisItem);
              } /*for*/
          }
        PickerList.notifyDataSetChanged(); /* is this necessary? */
        PickerListView = (android.widget.ListView)findViewById(R.id.list);
        PickerListView.setAdapter(PickerList);
      } /*onCreate*/

    @Override
    public boolean dispatchKeyEvent
      (
        android.view.KeyEvent TheEvent
      )
      {
        boolean Handled = false;
        if
          (
                TheEvent.getAction() == android.view.KeyEvent.ACTION_UP
            &&
                TheEvent.getKeyCode() == android.view.KeyEvent.KEYCODE_BACK
          )
          {
            if (PickerList.CurSelected != null)
              {
                setResult
                  (
                    android.app.Activity.RESULT_OK,
                    new android.content.Intent()
                        .putExtra(NameID, PickerList.CurSelected.Name)
                        .putExtra(UpperID, Upper)
                  );
              } /*if*/
            finish();
            Handled = true;
          } /*if*/
        if (!Handled)
          {
            Handled = super.dispatchKeyEvent(TheEvent);
          } /*if*/
        return
            Handled;
      } /*dispatchKeyEvent*/

    @Override
    public void onDestroy()
      {
        Current = null;
        super.onDestroy();
      } /*onDestroy*/

    public static void Launch
      (
        android.app.Activity Caller,
        boolean Upper,
        int RequestCode
      )
      {
        if (!Reentered)
          {
            Reentered = true; /* until Picker activity terminates */
            ScalePicker.Upper = Upper;
            Caller.startActivityForResult
              (
                new android.content.Intent(android.content.Intent.ACTION_PICK)
                    .setClass(Caller, ScalePicker.class),
                RequestCode
              );
          }
        else
          {
          /* can happen if user gets impatient and selects from menu twice, just ignore */
          } /*if*/
      } /*Launch*/

    public static void Cleanup()
      /* Client must call this to do explicit cleanup; I tried doing it in
        onDestroy, but of course that gets called when user rotates screen,
        which means picker context is lost. */
      {
        Reentered = false;
      } /*Cleanup*/

  } /*ScalePicker*/
