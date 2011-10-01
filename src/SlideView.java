package nz.gen.geek_central.infinirule;
/*
    Slide-rule display widget
*/

import android.graphics.PointF;
import android.view.MotionEvent;

public class SlideView extends android.view.View
  {
    private Scales.Scale Scale1, Scale2;
    private double Offset1, Offset2;
    private int ScaleLength; /* in pixels */

    private void Init()
      /* common code for all constructors */
      {
        Scale1 = new Scales.XScale();
        Scale2 = Scale1;
        Offset1 = 0.0;
        Offset2 = 0.0;
        ScaleLength = -1; /* proper value deferred to onLayout */
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

/*
    Mapping between image coordinates and view coordinates
*/

    public float ScaleToView
      (
        double Pos, /* [0.0 .. 1.0) */
        double Offset
      )
      /* returns a position on a scale, offset by the given amount,
        converted to view coordinates. */
      {
        return
            (float)((Pos + Offset) * ScaleLength);
      } /*ScaleToView*/

    public double ViewToScale
      (
        float Coord,
        double Offset
      )
      /* returns a view coordinate converted to the corresponding
        position on a scale offset by the given amount. */
      {
        return
            Coord / ScaleLength - Offset;
      } /*ViewToScale*/

    public double FindScaleOffset
      (
        float Coord,
        double Pos
      )
      /* finds the offset value such that the specified view coordinate
        maps to the specified position on a scale. */
      {
        return
            Coord / ScaleLength - Pos;
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
      /* scale names TBD */
      /* scale wraparound TBD */
        g.drawColor(0xfffffada);
        g.save(android.graphics.Canvas.MATRIX_SAVE_FLAG);
        final android.graphics.Matrix m1 = g.getMatrix();
        final android.graphics.Matrix m2 = g.getMatrix();
        m1.preTranslate((float)(Offset1 * ScaleLength), getHeight() / 2.0f);
        m2.preTranslate((float)(Offset2 * ScaleLength), getHeight() / 2.0f);
        g.setMatrix(m1);
        Scale1.Draw(g, ScaleLength, false);
        g.setMatrix(m2);
        Scale2.Draw(g, ScaleLength, true);
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
            Handled = true;
        break;
        case MotionEvent.ACTION_POINTER_DOWN:
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
              }
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
                        final PointF ThisMouse =
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
                        final boolean UpperScale = ThisMouse.y < getHeight() / 2.0f;
                        final double NewOffset =
                            FindScaleOffset
                              (
                                ThisMouse.x,
                                ViewToScale(LastMouse.x, UpperScale ? Offset1 : Offset2)
                              );
                        if (UpperScale)
                          {
                            Offset1 = NewOffset;
                          }
                        else
                          {
                            Offset2 = NewOffset;
                          } /*if*/
                        invalidate();
                        if (ThisMouse1 != null && ThisMouse2 != null)
                          {
                          /* pinch to zoom */
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
            Handled = true;
        break;
          } /*switch*/
        return
            Handled;
      } /*onTouchEvent*/

  } /*SlideView*/
