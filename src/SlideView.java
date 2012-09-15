package nz.gen.geek_central.infinirule;
/*
    Slide-rule display widget for Infinirule.

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

import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Paint;
import android.view.MotionEvent;

public class SlideView extends android.view.View
  {
    public enum ContextMenuTypes
      {
        Cursor,
        Scales,
      } /*ContextMenuTypes*/
    public interface ContextMenuAction
      {
        public void CreateContextMenu
          (
            android.view.ContextMenu TheMenu,
            ContextMenuTypes MenuType
          );
      } /*ContextMenuAction*/

  /* fixme: perhaps replace Global.ScaleSelector enum with this */
    private static final int SCALE_TOP = 0;
    private static final int SCALE_UPPER = 1;
    private static final int SCALE_LOWER = 2;
    private static final int SCALE_BOTTOM = 3;
    private static final int NR_SCALES = 4;

    private boolean LayoutDone = false;
    private Matrix Orient, InverseOrient;
      /* rotate entire display so rule is drawn in landscape orientation,
        but context menus pop up in orientation of activity, which is portrait */
    private Scales.Scale[] CurScale = new Scales.Scale[NR_SCALES]; /* (-1.0 .. 0.0] */
    private double[] CurScaleOffset = new double[NR_SCALES];
    private float CursorX; /* view x-coordinate */
    private int ScaleLength; /* in pixels */
    private static final float MaxZoom = 10000.0f; /* something reasonable so rendering doesn't get too slow */

    private android.os.Vibrator Vibrate;

    private PointF GetPoint
      (
        PointF InCoord
      )
      /* returns InCoord transformed through InverseOrient. */
      {
        return
            GetPoint(InCoord.x, InCoord.y);
      } /*GetPoint*/

    private PointF GetPoint
      (
        float X,
        float Y
      )
      /* returns point (X, Y) transformed through InverseOrient. */
      {
        final float[] Coords = new float[] {X, Y};
        InverseOrient.mapPoints(Coords);
        return
            new PointF(Coords[0], Coords[1]);
      } /*GetPoint*/

    private PointF GetViewDimensions()
      /* returns the view dimensions rotated 90°. */
      {
        return
            new PointF(getHeight(), getWidth());
      } /*GetViewDimensions*/

    public void Reset
      (
        boolean Animate
      )
      {
        if (Animate)
          {
            final double Now = System.currentTimeMillis() / 1000.0;
            final double SlideDuration = 1.0f; /* maybe make this depend on offset amounts in future */
            new SlideAnimator
              (
                /*AnimFunction =*/ new android.view.animation.AccelerateDecelerateInterpolator(),
                /*StartTime =*/ Now,
                /*EndTime =*/ Now + SlideDuration,
                /*StartScaleOffset =*/ CurScaleOffset,
                /*EndScaleOffset =*/ new double[] {0.0, 0.0, 0.0, 0.0},
                /*StartCursorX =*/ CursorX,
                /*EndCursorX =*/ 0.0f,
                /*StartScaleLength =*/ ScaleLength,
                /*EndScaleLength =*/ (int)GetViewDimensions().x
              );
          }
        else
          {
            for (int i = 0; i < NR_SCALES; ++i)
              {
                CurScaleOffset[i] = 0.0;
              } /*for*/
            CursorX = 0.0f;
            if (ScaleLength > 0)
              {
                ScaleLength = (int)GetViewDimensions().x;
              } /*if*/
            invalidate();
          } /*if*/
      } /*Reset*/

    private void Init
      (
        android.content.Context Context
      )
      /* common code for all constructors */
      {
        Orient = new Matrix();
        InverseOrient = new Matrix();
        CurScale[SCALE_TOP] = Scales.DefaultScale(Global.ScaleSelector.TopScale);
        CurScale[SCALE_UPPER] = Scales.DefaultScale(Global.ScaleSelector.UpperScale);
        CurScale[SCALE_LOWER] = Scales.DefaultScale(Global.ScaleSelector.LowerScale);
        CurScale[SCALE_BOTTOM] = Scales.DefaultScale(Global.ScaleSelector.BottomScale);
        ScaleLength = -1; /* proper value deferred to onLayout */
        Vibrate =
            (android.os.Vibrator)Context.getSystemService(android.content.Context.VIBRATOR_SERVICE);
        Reset(false);
      } /*Init*/

    public SlideView
      (
        android.content.Context Context
      )
      {
        super(Context);
        Init(Context);
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
        Init(Context);
      } /*SlideView*/

    public void SetScale
      (
        String NewScaleName,
        Global.ScaleSelector WhichScale
      )
      {
        final Scales.Scale NewScale = Scales.KnownScales.get(NewScaleName);
      /* note I need to reset scale offsets to keep scales on the same side in sync */
        int i, j;
        switch (WhichScale)
          {
        case TopScale:
            i = SCALE_TOP;
            j = SCALE_UPPER;
        break;
        case UpperScale:
        default: /*sigh*/
            i = SCALE_UPPER;
            j = SCALE_TOP;
        break;
        case LowerScale:
            i = SCALE_LOWER;
            j = SCALE_BOTTOM;
        break;
        case BottomScale:
            i = SCALE_BOTTOM;
            j = SCALE_LOWER;
        break;
          } /*switch*/
        CurScale[i] = NewScale;
        CurScaleOffset[i] = 0.0;
        CurScaleOffset[j] = 0.0;
        invalidate();
      } /*SetScale*/

    public Scales.Scale GetScale
      (
        Global.ScaleSelector WhichScale
      )
      {
        int i;
        switch (WhichScale)
          {
        case TopScale:
            i = SCALE_TOP;
        break;
        default: /*sigh*/
        case UpperScale:
            i = SCALE_UPPER;
        break;
        case LowerScale:
            i = SCALE_LOWER;
        break;
        case BottomScale:
            i = SCALE_BOTTOM;
        break;
          } /*switch*/
        return
            CurScale[i];
      } /*GetScale*/

    public double GetScaleOffset
      (
        Global.ScaleSelector TheScale
      )
      {
        int i;
        switch (TheScale)
          {
        case TopScale:
            i = SCALE_TOP;
        break;
        default: /*sigh*/
        case UpperScale:
            i = SCALE_UPPER;
        break;
        case LowerScale:
            i = SCALE_LOWER;
        break;
        case BottomScale:
            i = SCALE_BOTTOM;
        break;
          } /*switch*/
        return
            CurScaleOffset[i];
      } /*GetScaleOffset*/

    public double GetCursorPos
      (
        Global.ScaleSelector ByScale
      )
      /* returns the cursor position relative to the specified scale. */
      {
        final double Pos = ViewToScale(CursorX, GetScale(ByScale).Size(), GetScaleOffset(ByScale));
        return
            Pos - Math.floor(Pos);
      } /*GetCursorPos*/

    public void SetCursorPos
      (
        Global.ScaleSelector ByScale,
        double NewPos,
        boolean Animate
      )
      /* sets a new cursor position relative to the specified scale. */
      {
        final double[] NewScaleOffset = new double[NR_SCALES];
        for (int i = 0; i < NR_SCALES; ++i)
          {
            NewScaleOffset[i] = CurScaleOffset[i]; /* to begin with */
          } /*for*/
        final float ViewWidth = GetViewDimensions().x;
        final Scales.Scale TheScale = GetScale(ByScale);
        float NewCursorX = ScaleToView(NewPos, TheScale.Size(), GetScaleOffset(ByScale));
        if (TheScale.Wrap() && (NewCursorX < 0.0f || NewCursorX >= ViewWidth))
          {
            final float Left = ScaleToView(0.0, TheScale.Size(), GetScaleOffset(ByScale));
            final float Right = ScaleToView(1.0, TheScale.Size(), GetScaleOffset(ByScale));
            final float Length = Right - Left;
            final float NewX = NewCursorX - (float)Math.floor((NewCursorX - Left) / Length) * Length;
            if (NewX >= 0 && NewX < ViewWidth)
              {
                NewCursorX = NewX;
              } /*if*/
          } /*if*/
        if (NewCursorX < 0.0f || NewCursorX >= ViewWidth)
          {
          /* adjust offsets of all scales as necessary to bring cursor position into view */
            final float NewX = ViewWidth * 0.5f; /* new cursor position will be in middle */
            for (int i = 0; i < NR_SCALES; ++i)
              {
                NewScaleOffset[i] = FindScaleOffset
                  (
                    NewX,
                    CurScale[i].Size(),
                    ViewToScale(NewCursorX, CurScale[i].Size(), CurScaleOffset[i])
                  );
              } /*for*/
            NewCursorX = NewX;
          } /*if*/
        if (Animate)
          {
            final double Now = System.currentTimeMillis() / 1000.0;
            final double SlideDuration = 1.0f; /* maybe make this depend on offset amounts in future */
            new SlideAnimator
              (
                /*AnimFunction =*/ new android.view.animation.AccelerateDecelerateInterpolator(),
                /*StartTime =*/ Now,
                /*EndTime =*/ Now + SlideDuration,
                /*StartScaleOffset =*/ CurScaleOffset,
                /*EndScaleOffset =*/ NewScaleOffset,
                /*StartCursorX =*/ CursorX,
                /*EndCursorX =*/ NewCursorX,
                /*StartScaleLength =*/ ScaleLength,
                /*EndScaleLength =*/ ScaleLength
              );
          }
        else
          {
            for (int i = 0; i < NR_SCALES; ++i)
              {
                CurScaleOffset[i] = NewScaleOffset[i];
              } /*for*/
            CursorX = NewCursorX;
            invalidate();
          } /*if*/
      } /*SetCursorPos*/

    public void SetContextMenuAction
      (
        ContextMenuAction TheAction
      )
      {
        DoContextMenu = TheAction;
      } /*SetContextMenuAction*/

/*
    Mapping between image coordinates and view coordinates
*/

    public float ScaleToView
      (
        double Pos, /* [0.0 .. 1.0] */
        double Size,
        double Offset
      )
      /* returns a position on a scale, offset by the given amount,
        converted to view coordinates. */
      {
        return
            Scales.ScaleToView(Pos, Size, Offset, ScaleLength);
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
            Scales.ViewToScale(Coord, Size, Offset, ScaleLength);
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
        return
            Scales.FindScaleOffset(Coord, Size, Pos, ScaleLength);
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
        final PointF ViewDimensions = GetViewDimensions();
        g.save();
        g.concat(Orient);
          {
            final Paint BGHow = new Paint();
            BGHow.setColor(Scales.BackgroundColor);
            BGHow.setStyle(Paint.Style.FILL);
            g.drawRect
              (
                /*left =*/ 0.0f,
                /*top =*/ ViewDimensions.y / 2.0f - Scales.HalfLayoutHeight,
                /*right =*/ ViewDimensions.x,
                /*bottom =*/ ViewDimensions.y / 2.0f + Scales.HalfLayoutHeight,
                /*paint =*/ BGHow
              );
          }
        final android.graphics.Rect TextBounds = Scales.GetCharacterCellBounds();
        Scales.DrawScaleName
          (
            /*g =*/ g,
            /*TheScale =*/ CurScale[SCALE_TOP],
            /*Upper =*/ true,
            /*Pos =*/
                new PointF
                  (
                    Scales.PrimaryMarkerLength / 2.0f,
                    ViewDimensions.y / 2.0f - Scales.HalfLayoutHeight + (Scales.PrimaryMarkerLength - TextBounds.top) * 1.5f
                  ),
            /*Alignment =*/ Paint.Align.LEFT,
            /*Color =*/ Scales.MainColor
          );
        Scales.DrawScaleName
          (
            /*g =*/ g,
            /*TheScale =*/ CurScale[SCALE_UPPER],
            /*Upper =*/ true,
            /*Pos =*/
                new PointF
                  (
                    Scales.PrimaryMarkerLength / 2.0f,
                    ViewDimensions.y * 0.5f - (Scales.PrimaryMarkerLength + TextBounds.bottom) * 1.5f
                  ),
            /*Alignment =*/ Paint.Align.LEFT,
            /*Color =*/ Scales.MainColor
          );
        Scales.DrawScaleName
          (
            /*g =*/ g,
            /*TheScale =*/ CurScale[SCALE_LOWER],
            /*Upper =*/ false,
            /*Pos =*/
                new PointF
                  (
                    Scales.PrimaryMarkerLength / 2.0f,
                    ViewDimensions.y * 0.5f + (Scales.PrimaryMarkerLength - TextBounds.top) * 1.5f
                  ),
            /*Alignment =*/ Paint.Align.LEFT,
            /*Color =*/ Scales.MainColor
          );
        Scales.DrawScaleName
          (
            /*g =*/ g,
            /*TheScale =*/ CurScale[SCALE_BOTTOM],
            /*Upper =*/ false,
            /*Pos =*/
                new PointF
                  (
                    Scales.PrimaryMarkerLength / 2.0f,
                    ViewDimensions.y / 2.0f + Scales.HalfLayoutHeight - (Scales.PrimaryMarkerLength + TextBounds.bottom) * 1.5f
                  ),
            /*Alignment =*/ Paint.Align.LEFT,
            /*Color =*/ Scales.MainColor
          );
        final android.graphics.Matrix m_orig = g.getMatrix();
        for (boolean Upper = false;;)
          {
            for (boolean Edge = false;;)
              {
                final int ScaleIndex =
                    Upper ?
                        Edge ? SCALE_TOP : SCALE_UPPER
                    :
                        Edge ? SCALE_BOTTOM : SCALE_LOWER;
                final Scales.Scale TheScale = CurScale[ScaleIndex];
                final int ScaleRepeat =
                        (int)(ViewDimensions.x + ScaleLength * TheScale.Size() - 1)
                    /
                        (int)(ScaleLength * TheScale.Size());
                double Offset = CurScaleOffset[ScaleIndex] + TheScale.ExtraOffset();
                  {
                    final android.graphics.Matrix m = new android.graphics.Matrix();
                    m.preTranslate
                      (
                        0.0f,
                            ViewDimensions.y / 2.0f
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
                    g.setMatrix(m_orig);
                    g.concat(m);
                  }
                for (int i = -1; i <= ScaleRepeat; ++i)
                  {
                    TheScale.Draw
                      (
                        /*g =*/ g,
                        /*Offset =*/ Offset,
                        /*ScaleLength =*/ ScaleLength,
                        /*ViewWidth =*/ (int)ViewDimensions.x,
                        /*TopEdge =*/ Upper == Edge
                      );
                    Offset += 1.0;
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
            g.drawLine(CursorX, 0.0f, CursorX, ViewDimensions.y, CursorHow);
            CursorHow.setStyle(Paint.Style.FILL);
            CursorHow.setColor(Scales.CursorFillColor);
            g.drawRect
              (
                /*left =*/ CursorLeft,
                /*top =*/ 0.0f,
                /*right =*/ CursorRight,
                /*bottom =*/ ViewDimensions.y,
                /*paint =*/ CursorHow
              );
            CursorHow.setStyle(Paint.Style.STROKE);
            CursorHow.setColor(Scales.CursorEdgeColor);
            g.drawLine(CursorLeft, 0.0f, CursorLeft, ViewDimensions.y, CursorHow);
            g.drawLine(CursorRight, 0.0f, CursorRight, ViewDimensions.y, CursorHow);
          }
        g.restore();
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
    private ContextMenuAction DoContextMenu = null;
    private boolean MouseMoved = false;
    private MovingState MovingWhat = MovingState.MovingNothing;
    private boolean PrecisionMove = false;
    private final float PrecisionFactor = 10.0f;

    private final Runnable LongClicker =
      /* do my own long-click handling, because setOnLongClickListener doesn't seem to work */
        new Runnable()
          {
            public void run()
              {
                showContextMenu();
              /* stop handling cursor/scale movements */
                LastMouse1 = null;
                LastMouse2 = null;
                Mouse1ID = -1;
                Mouse2ID = -1;
                MovingWhat = MovingState.MovingNothing;
              } /*run*/
          } /*Runnable*/;

    private class SlideAnimator implements Runnable
      {
        final android.view.animation.Interpolator AnimFunction;
        final double StartTime, EndTime;
        final double[]
            StartScaleOffset = new double[NR_SCALES],
            EndScaleOffset = new double[NR_SCALES];
        final float
            StartCursorX, EndCursorX;
        final int
            StartScaleLength, EndScaleLength;

        public SlideAnimator
          (
            android.view.animation.Interpolator AnimFunction,
            double StartTime,
            double EndTime,
            double[/*NR_SCALES*/] StartScaleOffset,
            double[/*NR_SCALES*/] EndScaleOffset,
            float StartCursorX,
            float EndCursorX,
            int StartScaleLength,
            int EndScaleLength
          )
          {
            this.AnimFunction = AnimFunction;
            this.StartTime = StartTime;
            this.EndTime = EndTime;
            for (int i = 0; i < NR_SCALES; ++i)
              {
                this.StartScaleOffset[i] = StartScaleOffset[i];
                this.EndScaleOffset[i] = EndScaleOffset[i];
              } /*for*/
            this.StartCursorX = StartCursorX;
            this.EndCursorX = EndCursorX;
            this.StartScaleLength = StartScaleLength;
            this.EndScaleLength = EndScaleLength;
            CurrentAnim = this;
            getHandler().post(this);
          } /*SlideAnimator*/

        public void run()
          {
            if (CurrentAnim == this)
              {
                final double CurrentTime = System.currentTimeMillis() / 1000.0;
                final float AnimAmt =
                    AnimFunction.getInterpolation
                      (
                        (float)(
                                (Math.min(CurrentTime, EndTime) - StartTime)
                                  /* clamp to avoid random rebounding effects */
                            /
                                (EndTime - StartTime)
                        )
                      );
                for (int i = 0; i < NR_SCALES; ++i)
                  {
                    CurScaleOffset[i] =
                            StartScaleOffset[i]
                        +
                            (EndScaleOffset[i] - StartScaleOffset[i]) * AnimAmt;
                  } /*for*/
                CursorX = StartCursorX + (float)((EndCursorX - StartCursorX) * AnimAmt);
                ScaleLength = StartScaleLength + (int)((EndScaleLength - StartScaleLength) * AnimAmt);
                invalidate();
                final android.os.Handler MyHandler = getHandler();
                  /* can be null if activity is being destroyed */
                if (MyHandler != null && CurrentTime < EndTime)
                  {
                    MyHandler.post(this);
                  }
                else
                  {
                    CurrentAnim = null;
                  } /*if*/
              } /*if*/
          } /*run*/
      } /*SlideAnimator*/

    private SlideAnimator CurrentAnim = null;

    @Override
    public boolean onTouchEvent
      (
        MotionEvent TheEvent
      )
      {
        CurrentAnim = null; /* cancel any animation in progress */
        final PointF ViewDimensions = GetViewDimensions();
        boolean Handled = false;
        switch (TheEvent.getAction() & (1 << MotionEvent.ACTION_POINTER_ID_SHIFT) - 1)
          {
        case MotionEvent.ACTION_DOWN:
            LastMouse1 = GetPoint(TheEvent.getX(), TheEvent.getY());
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
            else if (LastMouse1.y > ViewDimensions.y / 2.0f)
              {
                MovingWhat = MovingState.MovingLowerScale;
              }
            else
              {
                MovingWhat = MovingState.MovingBothScales;
              } /*if*/
            PrecisionMove = Math.abs(LastMouse1.y - ViewDimensions.y / 2.0f) > Scales.HalfLayoutHeight;
            MouseMoved = false;
            Handled = true;
            getHandler().postDelayed(LongClicker, android.view.ViewConfiguration.getLongPressTimeout());
        break;
        case MotionEvent.ACTION_POINTER_DOWN:
            if
              (
                    MovingWhat == MovingState.MovingLowerScale
                ||
                    MovingWhat == MovingState.MovingBothScales
              )
              {
                if (!MouseMoved)
                  {
                    getHandler().removeCallbacks(LongClicker);
                    MouseMoved = true;
                  } /*if*/
                final int PointerIndex =
                        (TheEvent.getAction() & MotionEvent.ACTION_POINTER_ID_MASK)
                    >>
                        MotionEvent.ACTION_POINTER_ID_SHIFT;
                final int MouseID = TheEvent.getPointerId(PointerIndex);
                final PointF MousePos = GetPoint
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
            if(LastMouse1 != null)
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
                            GetPoint
                              (
                                TheEvent.getX(Mouse1Index),
                                TheEvent.getY(Mouse1Index)
                              )
                        :
                            null;
                    final PointF ThisMouse2 =
                        Mouse2Index >= 0 ?
                            GetPoint
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
                                    ThisMouse1.y < ViewDimensions.y / 2.0f
                                !=
                                    ThisMouse2.y < ViewDimensions.y / 2.0f
                          )
                          {
                          /* simultaneous scrolling of both scales */
                            PointF
                                ThisMouseUpper, ThisMouseLower, LastMouseUpper, LastMouseLower;
                            if (ThisMouse1.y < ViewDimensions.y / 2.0f)
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
                            for (int i = 0; i < NR_SCALES; ++i)
                              {
                                CurScaleOffset[i] = FindScaleOffset
                                  (
                                    ThisMouseUpper.x,
                                    CurScale[i].Size(),
                                    ViewToScale
                                      (
                                        i == SCALE_TOP || i == SCALE_UPPER ?
                                            LastMouseUpper.x
                                        :
                                            LastMouseLower.x,
                                        CurScale[i].Size(),
                                        CurScaleOffset[i]
                                      )
                                  );
                              } /*for*/
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
                            if
                              (
                                    MouseMoved
                                ||
                                        Math.hypot(ThisMouse.x - LastMouse.x, ThisMouse.y - LastMouse.y)
                                    >
                                        Math.sqrt(android.view.ViewConfiguration.get(getContext()).getScaledTouchSlop())
                                          /* avoid accidentally moving anything during long-tap */
                              )
                              {
                                if (!MouseMoved)
                                  {
                                    getHandler().removeCallbacks(LongClicker);
                                    MouseMoved = true;
                                  } /*if*/
                                if (PrecisionMove)
                                  {
                                    ThisMouse = new PointF
                                      (
                                        LastMouse.x + (ThisMouse.x - LastMouse.x) / PrecisionFactor,
                                        ThisMouse.y
                                      );
                                  } /*if*/
                                int NewScaleLength = ScaleLength;
                                if
                                  (
                                        MovingWhat != MovingState.MovingCursor
                                    &&
                                        ThisMouse1 != null
                                    &&
                                        ThisMouse2 != null
                                    &&
                                            ThisMouse1.y < ViewDimensions.y / 2.0f
                                        ==
                                            ThisMouse2.y < ViewDimensions.y / 2.0f
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
                                        NewScaleLength =
                                            Math.min
                                              (
                                                Math.max
                                                  (
                                                    (int)(
                                                        ScaleLength * ThisDistance /  LastDistance
                                                    ),
                                                    (int)ViewDimensions.x
                                                  ),
                                                MaxZoom > 0.0f ?
                                                    (int)(ViewDimensions.x * MaxZoom)
                                                :
                                                    Integer.MAX_VALUE
                                              );
                                      } /*if*/
                                  } /*if*/
                                switch (MovingWhat)
                                  {
                                case MovingCursor:
                                case MovingBothScales:
                                    CursorX =
                                        Math.max
                                          (
                                            0.0f,
                                            Math.min
                                              (
                                                        (CursorX - LastMouse.x) / ScaleLength
                                                    *
                                                        NewScaleLength
                                                +
                                                    ThisMouse.x,
                                                ViewDimensions.x
                                              )
                                          );
                                break;
                                  } /*switch*/
                                switch (MovingWhat)
                                  {
                                case MovingBothScales:
                                case MovingLowerScale:
                                    for (boolean Upper = false;;)
                                      {
                                        final int i = Upper ? SCALE_TOP : SCALE_BOTTOM;
                                        final int j = Upper ? SCALE_UPPER : SCALE_LOWER;
                                        final double Scale1Size = CurScale[i].Size();
                                        final double Scale2Size = CurScale[j].Size();
                                        CurScaleOffset[i] =
                                            Scales.FindScaleOffset
                                              (
                                                ThisMouse.x,
                                                Scale1Size,
                                                Scales.ViewToScale
                                                  (
                                                    LastMouse.x,
                                                    Scale1Size,
                                                    CurScaleOffset[i],
                                                    ScaleLength
                                                  ),
                                                NewScaleLength
                                              );
                                        CurScaleOffset[j] =
                                            Scales.FindScaleOffset
                                              (
                                                ThisMouse.x,
                                                Scale2Size,
                                                Scales.ViewToScale
                                                  (
                                                    LastMouse.x,
                                                    Scale2Size,
                                                    CurScaleOffset[j],
                                                    ScaleLength
                                                  ),
                                                NewScaleLength
                                              );
                                        if (Upper || MovingWhat != MovingState.MovingBothScales)
                                            break;
                                        Upper = true;
                                      } /*for*/
                                break;
                                  } /*switch*/
                                ScaleLength = NewScaleLength;
                              } /*if*/
                          } /*if*/
                        invalidate();
                        LastMouse1 = ThisMouse1;
                        LastMouse2 = ThisMouse2;
                      } /*if*/
                  } /*if*/
                Handled = true;
              } /*if*/
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
            getHandler().removeCallbacks(LongClicker);
            LastMouse1 = null;
            LastMouse2 = null;
            Mouse1ID = -1;
            Mouse2ID = -1;
            MovingWhat = MovingState.MovingNothing;
            Handled = MouseMoved;
        break;
          } /*switch*/
        return
            Handled;
      } /*onTouchEvent*/

    @Override
    public void onCreateContextMenu
      (
        android.view.ContextMenu TheMenu
      )
      {
        if (DoContextMenu != null)
          {
            Vibrate.vibrate(20);
            DoContextMenu.CreateContextMenu
              (
                TheMenu,
                MovingWhat == MovingState.MovingCursor ?
                    ContextMenuTypes.Cursor
                :
                    ContextMenuTypes.Scales
              );
          } /*if*/
      } /*onCreateContextMenu*/

/*
    Save/restore state
*/

    static final String[] SaveItemName =
        new String[/*NR_SCALES*/] {"topscale", "upperscale", "lowerscale", "bottomscale"};
    static final String SaveItemOffsetSuffix = "_offset";

    public android.os.Bundle SaveState()
      /* returns a snapshot of the current slide rule state. */
      {
        final android.os.Bundle SavedState = new android.os.Bundle();
        for (int i = 0; i < NR_SCALES; ++i)
          {
            SavedState.putString(SaveItemName[i], CurScale[i].Name());
            SavedState.putDouble(SaveItemName[i] + SaveItemOffsetSuffix, CurScaleOffset[i]);
          } /*for*/
        SavedState.putFloat("cursor_x", CursorX);
        SavedState.putFloat("scalezoom", ScaleLength / GetViewDimensions().x);
        return
            SavedState;
      } /*SaveState*/

    private android.os.Bundle PendingRestoreState = null;

    public void RestoreState
      (
        android.os.Bundle SavedState
      )
      /* restores the slide rule state from a previous snapshot. */
      {
        if (LayoutDone)
          {
            for (int i = 0; i < NR_SCALES; ++i)
              {
                Scales.Scale TheScale = Scales.KnownScales.get(SavedState.getString(SaveItemName[i]));
                if (TheScale == null)
                  {
                    final Global.ScaleSelector j;
                    switch (i)
                      {
                    case SCALE_TOP:
                        j = Global.ScaleSelector.TopScale;
                    break;
                    case SCALE_UPPER:
                    default: /* sigh */
                        j = Global.ScaleSelector.UpperScale;
                    break;
                    case SCALE_LOWER:
                        j = Global.ScaleSelector.LowerScale;
                    break;
                    case SCALE_BOTTOM:
                        j = Global.ScaleSelector.BottomScale;
                    break;
                      } /*switch*/
                    TheScale = Scales.DefaultScale(j);
                  } /*if*/
                CurScale[i] = TheScale;
                CurScaleOffset[i] = SavedState.getDouble(SaveItemName[i] + SaveItemOffsetSuffix, 0.0);
              } /*for*/
            CursorX = Math.max(0.0f, Math.min(SavedState.getFloat("cursor_x", 0.0f), GetViewDimensions().x));
            ScaleLength = (int)(SavedState.getFloat("scalezoom", 1.0f) * GetViewDimensions().x);
            invalidate();
          }
        else
          {
          /* defer to next onLayout call */
            PendingRestoreState = SavedState;
          } /*if*/
      } /*RestoreState*/

    @Override
    protected void onLayout
      (
        boolean Changed,
        int Left,
        int Top,
        int Right,
        int Bottom
      )
      /* just a place to finish initialization/state restoriation after I
        know what my layout will be */
      {
        super.onLayout(Changed, Left, Top, Right, Bottom);
        LayoutDone = true;
        Orient.reset();
        Orient.postTranslate(0, - getWidth());
        Orient.postRotate(90f);
        Orient.invert(InverseOrient);
        if (PendingRestoreState != null)
          {
            RestoreState(PendingRestoreState);
            PendingRestoreState = null;
          } /*if*/
        if (ScaleLength < 0)
          {
            ScaleLength = (int)GetViewDimensions().x;
          } /*if*/
      } /*onLayout*/

  } /*SlideView*/
