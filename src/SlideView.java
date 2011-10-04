package nz.gen.geek_central.infinirule;
/*
    Slide-rule display widget for Infinirule.

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

import android.graphics.PointF;
import android.graphics.Paint;
import android.view.MotionEvent;

public class SlideView extends android.view.View
  {
    private Scales.Scale TopScale, UpperScale, LowerScale, BottomScale;
    private double TopScaleOffset, UpperScaleOffset, LowerScaleOffset, BottomScaleOffset; /* (-1.0 .. 0.0] */
    private float CursorX; /* view x-coordinate */
    private int ScaleLength; /* in pixels */

    public void Reset()
      {
        TopScaleOffset = 0.0;
        UpperScaleOffset = 0.0;
        LowerScaleOffset = 0.0;
        BottomScaleOffset = 0.0;
        CursorX = 0.0f;
        if (ScaleLength > 0)
          {
            ScaleLength = getWidth();
          } /*if*/
        invalidate();
      } /*Reset*/

    private void Init()
      /* common code for all constructors */
      {
        TopScale = Scales.DefaultScale(Global.ScaleSelector.TopScale);
        UpperScale = Scales.DefaultScale(Global.ScaleSelector.UpperScale);
        LowerScale = Scales.DefaultScale(Global.ScaleSelector.LowerScale);
        BottomScale = Scales.DefaultScale(Global.ScaleSelector.BottomScale);
        ScaleLength = -1; /* proper value deferred to onLayout */
        Reset();
      } /*Init*/

    public SlideView
      (
        android.content.Context Context
      )
      {
        super(Context);
        Init();
      } /*SlideView*/

    public SlideView
      (
        android.content.Context Context,
        android.util.AttributeSet Attributes
      )
      {
        this(Context, Attributes, 0);
      } /*SlideView*/

    public SlideView
      (
        android.content.Context Context,
        android.util.AttributeSet Attributes,
        int DefaultStyle
      )
      {
        super(Context, Attributes, DefaultStyle);
        Init();
      } /*SlideView*/

    @Override
    protected void onLayout
      (
        boolean Changed,
        int Left,
        int Top,
        int Right,
        int Bottom
      )
      /* just a place to finish initialization after I know what my layout will be */
      {
        super.onLayout(Changed, Left, Top, Right, Bottom);
        if (ScaleLength < 0)
          {
            ScaleLength = getWidth();
          } /*if*/
      } /*onLayout*/

    public void SetScale
      (
        String NewScaleName,
        Global.ScaleSelector WhichScale
      )
      {
        final Scales.Scale NewScale = Scales.KnownScales.get(NewScaleName);
      /* note I need to reset scale offsets to keep scales on the same side in sync */
        switch (WhichScale)
          {
        case TopScale:
            TopScale = NewScale;
            TopScaleOffset = 0.0;
            UpperScaleOffset = 0.0;
        break;
        case UpperScale:
            UpperScale = NewScale;
            TopScaleOffset = 0.0;
            UpperScaleOffset = 0.0;
        break;
        case LowerScale:
            LowerScale = NewScale;
            LowerScaleOffset = 0.0;
            BottomScaleOffset = 0.0;
        break;
        case BottomScale:
            BottomScale = NewScale;
            LowerScaleOffset = 0.0;
            BottomScaleOffset = 0.0;
        break;
          } /*switch*/
        invalidate();
      } /*SetScale*/

    public String GetScaleName
      (
        Global.ScaleSelector WhichScale
      )
      {
        /*final*/ Scales.Scale TheScale
            = null; /*sigh*/
        switch (WhichScale)
          {
        case TopScale:
            TheScale = TopScale;
        break;
        case UpperScale:
            TheScale = UpperScale;
        break;
        case LowerScale:
            TheScale = LowerScale;
        break;
        case BottomScale:
            TheScale = BottomScale;
        break;
          } /*switch*/
        return
            TheScale.Name();
      } /*GetScaleName*/

/*
    Mapping between image coordinates and view coordinates
*/

    public float ScaleToView
      (
        double Pos, /* [0.0 .. 1.0) */
        double Size,
        double Offset
      )
      /* returns a position on a scale, offset by the given amount,
        converted to view coordinates. */
      {
        return
            (float)((Pos + Offset) * Size * ScaleLength);
      } /*ScaleToView*/

    public double ViewToScale
      (
        float Coord,
        double Size,
        double Offset
      )
      /* returns a view coordinate converted to the corresponding
        position on a scale offset by the given amount. */
      {
        return
            Coord / Size / ScaleLength - Offset;
      } /*ViewToScale*/

    public double FindScaleOffset
      (
        float Coord,
        double Size,
        double Pos
      )
      /* finds the offset value such that the specified view coordinate
        maps to the specified position on a scale. */
      {
        final double Offset = Coord / Size / ScaleLength - Pos;
        return
            Offset - Math.ceil(Offset);
      } /*FindScaleOffset*/

/*
    Drawing
*/

    @Override
    public void onDraw
      (
        android.graphics.Canvas g
      )
      {
          {
            final Paint BGHow = new Paint();
            BGHow.setColor(Scales.BackgroundColor);
            BGHow.setStyle(Paint.Style.FILL);
            g.drawRect
              (
                /*left =*/ 0.0f,
                /*top =*/ getHeight() / 2.0f - Scales.HalfLayoutHeight,
                /*right =*/ getWidth(),
                /*bottom =*/ getHeight() / 2.0f + Scales.HalfLayoutHeight,
                /*paint =*/ BGHow
              );
          }
        final android.graphics.Rect TextBounds = Scales.GetCharacterCellBounds();
        Scales.DrawLabel
          (
            /*g =*/ g,
            /*TheScale =*/ TopScale,
            /*Upper =*/ true,
            /*Pos =*/
                new PointF
                  (
                    Scales.PrimaryMarkerLength / 2.0f,
                    getHeight() / 2.0f - Scales.HalfLayoutHeight + (Scales.PrimaryMarkerLength - TextBounds.top) * 1.5f
                  ),
            /*Alignment =*/ Paint.Align.LEFT,
            /*Color =*/ Scales.MainColor
          );
        Scales.DrawLabel
          (
            /*g =*/ g,
            /*TheScale =*/ UpperScale,
            /*Upper =*/ true,
            /*Pos =*/
                new PointF
                  (
                    Scales.PrimaryMarkerLength / 2.0f,
                    getHeight() * 0.5f - (Scales.PrimaryMarkerLength + TextBounds.bottom) * 1.5f
                  ),
            /*Alignment =*/ Paint.Align.LEFT,
            /*Color =*/ Scales.MainColor
          );
        Scales.DrawLabel
          (
            /*g =*/ g,
            /*TheScale =*/ LowerScale,
            /*Upper =*/ false,
            /*Pos =*/
                new PointF
                  (
                    Scales.PrimaryMarkerLength / 2.0f,
                    getHeight() * 0.5f + (Scales.PrimaryMarkerLength - TextBounds.top) * 1.5f
                  ),
            /*Alignment =*/ Paint.Align.LEFT,
            /*Color =*/ Scales.MainColor
          );
        Scales.DrawLabel
          (
            /*g =*/ g,
            /*TheScale =*/ BottomScale,
            /*Upper =*/ false,
            /*Pos =*/
                new PointF
                  (
                    Scales.PrimaryMarkerLength / 2.0f,
                    getHeight() / 2.0f + Scales.HalfLayoutHeight - (Scales.PrimaryMarkerLength + TextBounds.bottom) * 1.5f
                  ),
            /*Alignment =*/ Paint.Align.LEFT,
            /*Color =*/ Scales.MainColor
          );
        final android.graphics.Matrix m_orig = g.getMatrix();
        for (boolean Upper = false;;)
          {
            for (boolean Edge = false;;)
              {
                final android.graphics.Matrix m = new android.graphics.Matrix(m_orig);
                final Scales.Scale TheScale =
                    Upper ?
                        Edge ? TopScale : UpperScale
                    :
                        Edge ? BottomScale : LowerScale;
                final int ScaleRepeat =
                        (getWidth() + (int)(ScaleLength * TheScale.Size() - 1))
                    /
                        (int)(ScaleLength * TheScale.Size());
                m.preTranslate
                  (
                    (float)(
                            (
                                (Upper ?
                                    Edge ? TopScaleOffset : UpperScaleOffset
                                :
                                    Edge ? BottomScaleOffset : LowerScaleOffset
                                )
                            +
                                TheScale.ExtraOffset()
                            )
                        *
                            ScaleLength
                        *
                            TheScale.Size()
                    ),
                        getHeight() / 2.0f
                    +
                        (Edge ?
                            Upper ?
                                - Scales.HalfLayoutHeight
                            :
                                + Scales.HalfLayoutHeight
                        :
                            0.0f
                        )
                  );
                for (int i = -1; i <= ScaleRepeat; ++i)
                  {
                    g.setMatrix(m);
                    TheScale.Draw(g, (float)(ScaleLength * TheScale.Size()), Upper == Edge);
                    m.preTranslate((float)(ScaleLength * TheScale.Size()), 0.0f);
                  } /*for*/
                if (Edge)
                    break;
                Edge = true;
              } /*for*/
            if (Upper)
                break;
            Upper = true;
          } /*for*/
        g.setMatrix(m_orig);
          {
            final float CursorLeft = CursorX - Scales.HalfCursorWidth;
            final float CursorRight = CursorX + Scales.HalfCursorWidth;
            final Paint CursorHow = new Paint();
            g.drawLine(CursorX, 0.0f, CursorX, getHeight(), CursorHow);
            CursorHow.setStyle(Paint.Style.FILL);
            CursorHow.setColor(Scales.CursorFillColor);
            g.drawRect
              (
                /*left =*/ CursorLeft,
                /*top =*/ 0.0f,
                /*right =*/ CursorRight,
                /*bottom =*/ getHeight(),
                /*paint =*/ CursorHow
              );
            CursorHow.setStyle(Paint.Style.STROKE);
            CursorHow.setColor(Scales.CursorEdgeColor);
            g.drawLine(CursorLeft, 0.0f, CursorLeft, getHeight(), CursorHow);
            g.drawLine(CursorRight, 0.0f, CursorRight, getHeight(), CursorHow);
          }
      } /*onDraw*/

/*
    Interaction handling
*/

    private PointF
        LastMouse1 = null,
        LastMouse2 = null;
    private int
        Mouse1ID = -1,
        Mouse2ID = -1;
    private enum MovingState
      {
        MovingNothing,
        MovingCursor,
        MovingBothScales,
        MovingLowerScale,
      } /*MovingState*/
    private MovingState MovingWhat = MovingState.MovingNothing;
    private boolean PrecisionMove = false;
    private final float PrecisionFactor = 10.0f;

    @Override
    public boolean onTouchEvent
      (
        MotionEvent TheEvent
      )
      {
        boolean Handled = false;
        switch (TheEvent.getAction() & (1 << MotionEvent.ACTION_POINTER_ID_SHIFT) - 1)
          {
        case MotionEvent.ACTION_DOWN:
            LastMouse1 = new PointF(TheEvent.getX(), TheEvent.getY());
            Mouse1ID = TheEvent.getPointerId(0);
            if
              (
                    CursorX - Scales.HalfCursorWidth <= LastMouse1.x
                &&
                    LastMouse1.x < CursorX + Scales.HalfCursorWidth
              )
              {
                MovingWhat = MovingState.MovingCursor;
              }
            else if (LastMouse1.y > getHeight() / 2.0f)
              {
                MovingWhat = MovingState.MovingLowerScale;
              }
            else
              {
                MovingWhat = MovingState.MovingBothScales;
              } /*if*/
            PrecisionMove = Math.abs(LastMouse1.y - getHeight() / 2.0f) > Scales.HalfLayoutHeight;
            Handled = true;
        break;
        case MotionEvent.ACTION_POINTER_DOWN:
            if
              (
                    MovingWhat == MovingState.MovingLowerScale
                ||
                    MovingWhat == MovingState.MovingBothScales
              )
              {
                final int PointerIndex =
                        (TheEvent.getAction() & MotionEvent.ACTION_POINTER_ID_MASK)
                    >>
                        MotionEvent.ACTION_POINTER_ID_SHIFT;
                final int MouseID = TheEvent.getPointerId(PointerIndex);
                final PointF MousePos = new PointF
                  (
                    TheEvent.getX(PointerIndex),
                    TheEvent.getY(PointerIndex)
                  );
                if (LastMouse1 == null)
                  {
                    Mouse1ID = MouseID;
                    LastMouse1 = MousePos;
                  }
                else if (LastMouse2 == null)
                  {
                    Mouse2ID = MouseID;
                    LastMouse2 = MousePos;
                  } /*if*/
              } /*if*/
            Handled = true;
        break;
        case MotionEvent.ACTION_MOVE:
            if (LastMouse1 != null)
              {
                final int Mouse1Index = TheEvent.findPointerIndex(Mouse1ID);
                final int Mouse2Index =
                    LastMouse2 != null ?
                        TheEvent.findPointerIndex(Mouse2ID)
                    :
                        -1;
                if (Mouse1Index >= 0 || Mouse2Index >= 0)
                  {
                    final PointF ThisMouse1 =
                        Mouse1Index >= 0 ?
                            new PointF
                              (
                                TheEvent.getX(Mouse1Index),
                                TheEvent.getY(Mouse1Index)
                              )
                        :
                            null;
                    final PointF ThisMouse2 =
                        Mouse2Index >= 0 ?
                            new PointF
                             (
                               TheEvent.getX(Mouse2Index),
                               TheEvent.getY(Mouse2Index)
                             )
                         :
                            null;
                    if (ThisMouse1 != null || ThisMouse2 != null)
                      {
                        if
                          (
                                ThisMouse1 != null
                            &&
                                ThisMouse2 != null
                            &&
                                    ThisMouse1.y < getHeight() / 2.0f
                                !=
                                    ThisMouse2.y < getHeight() / 2.0f
                          )
                          {
                          /* simultaneous scrolling of both scales */
                            PointF
                                ThisMouseUpper, ThisMouseLower, LastMouseUpper, LastMouseLower;
                            if (ThisMouse1.y < getHeight() / 2.0f)
                              {
                                ThisMouseUpper = ThisMouse1;
                                LastMouseUpper = LastMouse1;
                                ThisMouseLower = ThisMouse2;
                                LastMouseLower = LastMouse2;
                              }
                            else
                              {
                                ThisMouseUpper = ThisMouse2;
                                LastMouseUpper = LastMouse2;
                                ThisMouseLower = ThisMouse1;
                                LastMouseLower = LastMouse1;
                              } /*if*/
                            if (PrecisionMove)
                              {
                                ThisMouseUpper = new PointF
                                  (
                                    LastMouseUpper.x + (ThisMouseUpper.x - LastMouseUpper.x) / PrecisionFactor,
                                    ThisMouseUpper.y
                                  );
                                ThisMouseLower = new PointF
                                  (
                                    LastMouseLower.x + (ThisMouseLower.x - LastMouseLower.x) / PrecisionFactor,
                                    ThisMouseLower.y
                                  );
                              } /*if*/
                            TopScaleOffset =
                                FindScaleOffset
                                  (
                                    ThisMouseUpper.x,
                                    TopScale.Size(),
                                    ViewToScale(LastMouseUpper.x, TopScale.Size(), TopScaleOffset)
                                  );
                            UpperScaleOffset =
                                FindScaleOffset
                                  (
                                    ThisMouseUpper.x,
                                    UpperScale.Size(),
                                    ViewToScale(LastMouseUpper.x, UpperScale.Size(), UpperScaleOffset)
                                  );
                            LowerScaleOffset =
                                FindScaleOffset
                                  (
                                    ThisMouseLower.x,
                                    LowerScale.Size(),
                                    ViewToScale(LastMouseLower.x, LowerScale.Size(), LowerScaleOffset)
                                  );
                            BottomScaleOffset =
                                FindScaleOffset
                                  (
                                    ThisMouseLower.x,
                                    BottomScale.Size(),
                                    ViewToScale(LastMouseLower.x, BottomScale.Size(), BottomScaleOffset)
                                  );
                            invalidate();
                          }
                        else
                          {
                            PointF ThisMouse =
                                ThisMouse1 != null ?
                                    ThisMouse2 != null ?
                                        new PointF
                                          (
                                            (ThisMouse1.x + ThisMouse2.x) / 2.0f,
                                            (ThisMouse1.y + ThisMouse2.y) / 2.0f
                                          )
                                    :
                                        ThisMouse1
                                :
                                    ThisMouse2;
                            final PointF LastMouse =
                                ThisMouse1 != null ?
                                    ThisMouse2 != null ?
                                        new PointF
                                          (
                                            (LastMouse1.x + LastMouse2.x) / 2.0f,
                                            (LastMouse1.y + LastMouse2.y) / 2.0f
                                          )
                                    :
                                        LastMouse1
                                :
                                    LastMouse2;
                            if (PrecisionMove)
                              {
                                ThisMouse = new PointF
                                  (
                                    LastMouse.x + (ThisMouse.x - LastMouse.x) / PrecisionFactor,
                                    ThisMouse.y
                                  );
                              } /*if*/
                            switch (MovingWhat)
                              {
                            case MovingCursor:
                                CursorX = Math.max(0.0f, Math.min(CursorX + ThisMouse.x - LastMouse.x, getWidth()));
                                invalidate();
                            break;
                            case MovingBothScales:
                            case MovingLowerScale:
                                for (boolean Upper = false;;)
                                  {
                                    final double Scale1Size =
                                        ((Scales.Scale)(Upper ? TopScale : BottomScale)).Size();
                                    final double Scale2Size =
                                        ((Scales.Scale)(Upper ? UpperScale : LowerScale)).Size();
                                    final double NewOffset1 =
                                        FindScaleOffset
                                          (
                                            ThisMouse.x,
                                            Scale1Size,
                                            ViewToScale
                                              (
                                                LastMouse.x,
                                                Scale1Size,
                                                Upper ? TopScaleOffset : BottomScaleOffset
                                              )
                                          );
                                    final double NewOffset2 =
                                        FindScaleOffset
                                          (
                                            ThisMouse.x,
                                            Scale2Size,
                                            ViewToScale
                                              (
                                                LastMouse.x,
                                                Scale2Size,
                                                Upper ? UpperScaleOffset : LowerScaleOffset
                                              )
                                          );
                                    if (Upper)
                                      {
                                        TopScaleOffset = NewOffset1;
                                        UpperScaleOffset = NewOffset2;
                                      }
                                    else
                                      {
                                        BottomScaleOffset = NewOffset1;
                                        LowerScaleOffset = NewOffset2;
                                      } /*if*/
                                    if (Upper || MovingWhat != MovingState.MovingBothScales)
                                        break;
                                    Upper = true;
                                  } /*for*/
                                invalidate();
                                if
                                  (
                                        ThisMouse1 != null
                                    &&
                                        ThisMouse2 != null
                                    &&
                                            ThisMouse1.y < getHeight() / 2.0f
                                        ==
                                            ThisMouse2.y < getHeight() / 2.0f
                                  )
                                  {
                                  /* pinch to zoom--note no PrecisionMove here */
                                    final float LastDistance = (float)Math.hypot
                                      (
                                        LastMouse1.x - LastMouse2.x,
                                        LastMouse1.y - LastMouse2.y
                                      );
                                    final float ThisDistance = (float)Math.hypot
                                      (
                                        ThisMouse1.x - ThisMouse2.x,
                                        ThisMouse1.y - ThisMouse2.y
                                      );
                                    if
                                      (
                                            LastDistance != 0.0f
                                        &&
                                            ThisDistance != 0.0f
                                      )
                                      {
                                        ScaleLength =
                                            (int)(
                                                ScaleLength * ThisDistance /  LastDistance
                                            );
                                        invalidate();
                                      } /*if*/
                                  } /*if*/
                            break;
                              } /*switch*/
                          } /*if*/
                        LastMouse1 = ThisMouse1;
                        LastMouse2 = ThisMouse2;
                      } /*if*/
                  } /*if*/
              } /*if*/
            Handled = true;
        break;
        case MotionEvent.ACTION_POINTER_UP:
            if (LastMouse2 != null)
              {
                final int PointerIndex =
                        (TheEvent.getAction() & MotionEvent.ACTION_POINTER_ID_MASK)
                    >>
                        MotionEvent.ACTION_POINTER_ID_SHIFT;
                final int PointerID = TheEvent.getPointerId(PointerIndex);
                if (PointerID == Mouse1ID)
                  {
                    Mouse1ID = Mouse2ID;
                    LastMouse1 = LastMouse2;
                    Mouse2ID = -1;
                    LastMouse2 = null;
                  }
                else if (PointerID == Mouse2ID)
                  {
                    Mouse2ID = -1;
                    LastMouse2 = null;
                  } /*if*/
              } /*if*/
            Handled = true;
        break;
        case MotionEvent.ACTION_UP:
            LastMouse1 = null;
            LastMouse2 = null;
            Mouse1ID = -1;
            Mouse2ID = -1;
            MovingWhat = MovingState.MovingNothing;
            Handled = true;
        break;
          } /*switch*/
        return
            Handled;
      } /*onTouchEvent*/

  } /*SlideView*/
