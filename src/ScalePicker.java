package nz.gen.geek_central.infinirule;
/*
    Infinirule--let the user choose which scales to show.

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

public class ScalePicker extends android.app.Activity
  {
    public static final String NameID = "nz.gen.geek_central.infinirule.name";

    private static boolean Reentered = false; /* sanity check */
    private static ScalePicker Current = null;

    private static int /*SCALE.**/ WhichScale;
    private static String CurScaleName;

    private android.widget.ListView PickerListView;
    private SelectedItemAdapter PickerList;

    public static class PickerItem
      {
        String Name;
        boolean Selected;

        public PickerItem
          (
            String Name,
            boolean Selected
          )
          {
            this.Name = Name;
            this.Selected = Selected;
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
            final PickerItem ThisItem = this.getItem(Position);
            final android.widget.ImageView ItemDisplay =
                (android.widget.ImageView)TheView.findViewById(R.id.select_name);
              {
                final android.graphics.Rect TextBounds = Scales.GetCharacterCellBounds();
                final android.graphics.Bitmap ItemBits = android.graphics.Bitmap.createBitmap
                  (
                    /*width =*/
                        Math.max
                          (
                                (int)(
                                    Scales.DrawScaleName
                                      (
                                        /*g =*/ null,
                                        /*Scale =*/ Scales.KnownScales.get(ThisItem.Name),
                                        /*Upper =*/
                                                WhichScale == SCALE.TOP
                                            ||
                                                WhichScale == SCALE.UPPER,
                                        /*Pos =*/ null,
                                        /*Alignment =*/ android.graphics.Paint.Align.LEFT,
                                        /*Color =*/ 0
                                      )
                                  *
                                    1.2
                                  *
                                    Global.PixelDensity()
                                )
                            +
                                (TextBounds.right - TextBounds.left),
                                  /* extra to allow for italic slant */
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
              /* ItemBits.setDensity(Global.MainMetrics.densityDpi); */ /* no need? */
                final android.graphics.Canvas ItemDraw = new android.graphics.Canvas(ItemBits);
                ItemDraw.drawColor(0x00000000);
                Scales.DrawScaleName
                  (
                    /*g =*/ ItemDraw,
                    /*Scale =*/ Scales.KnownScales.get(ThisItem.Name),
                    /*Upper =*/
                            WhichScale == SCALE.TOP
                        ||
                            WhichScale == SCALE.UPPER,
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
            if (ThisItem.Selected)
              {
                CurSelected = ThisItem;
                LastChecked = ThisChecked;
              } /*if*/
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
                getString(Global.ScaleNameID(WhichScale))
              )
          );
        int ScrollToItem = 0;
        PickerList = new SelectedItemAdapter(this, R.layout.scale_picker_item, getLayoutInflater());
        PickerList.setNotifyOnChange(false);
        PickerList.clear();
          {
            final java.util.TreeSet<PickerItem> ResultTemp = /* this is how you sort things in Java */
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
                ResultTemp.add
                  (
                    new PickerItem
                      (
                        ThisScale.getValue().Name(),
                        ThisScale.getValue().Name().equals(CurScaleName)
                      )
                  );
              } /*for*/
            for (PickerItem ThisItem : ResultTemp)
              {
                if (ThisItem.Selected)
                  {
                    ScrollToItem = PickerList.getCount();
                  } /*if*/
                PickerList.add(ThisItem);
              } /*for*/
          }
        PickerList.notifyDataSetChanged();
        PickerListView = (android.widget.ListView)findViewById(R.id.list);
        PickerListView.setAdapter(PickerList);
          {
            final int ScrollTo = ScrollToItem;
            PickerListView.post
              (
                new Runnable()
                  {
                    public void run()
                      {
                        PickerListView.setSelection(ScrollTo);
                      } /*run*/
                  } /*Runnable*/
              );
          }
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
        int /*SCALE.**/ WhichScale,
        int RequestCode,
        String CurScaleName
      )
      {
        if (!Reentered)
          {
            Reentered = true; /* until Picker activity terminates */
            ScalePicker.WhichScale = WhichScale;
            ScalePicker.CurScaleName = CurScaleName;
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
