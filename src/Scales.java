package nz.gen.geek_central.infinirule;
/*
    Individual slide-rule scale definition and rendering for Infinirule.

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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

public class Scales
  {
    public static final char VarEscape = '\u1e8b'; /* indicates where to substitute variable name */
    public static final String UpperVarName = "x";
    public static final String LowerVarName = "y";

    public static int BackgroundColor, MainColor, AltColor, CursorFillColor, CursorEdgeColor;
    public static float FontSize, PrimaryMarkerLength, HalfLayoutHeight, HalfCursorWidth;
    public static final Typeface NormalStyle = Typeface.defaultFromStyle(Typeface.NORMAL);
    public static final Typeface ItalicStyle = Typeface.defaultFromStyle(Typeface.ITALIC);

    public interface Scale /* implemented by all slide-rule scales */
      {
        public String Name();
          /* the unique, user-visible name of this scale. Instances of VarEscape
            will be replaced with a variable name. */

        public double Size();
          /* returns a measure of relative scale length, e.g. 1.0 for C & D scales,
            2.0 for square root, 0.5 for A & B (squares). */

        public double ExtraOffset();
          /* nonzero for folded versions of scales */

        public double ValueAt
          (
            double Pos /* [0.0 .. 1.0) */
          );
          /* returns scale reading at specified position. */

        public double PosAt
          (
            double Value /* whatever range is returned by ValueAt */
          );
          /* returns position corresponding to specified scale reading. */

        public void Draw
          (
            Canvas g, /* draw it starting at (0, 0) here */
            float ScaleLength, /* total width */
            boolean TopEdge /* false for bottom edge */
          );
      } /*Scale*/

/*
    Common useful stuff
*/

    public static void DrawCenteredText
      (
        Canvas Draw,
        String TheText,
        float x,
        float y,
        Paint UsePaint
      )
      /* draws text at position x, vertically centred around y. */
      {
        final android.graphics.Rect TextBounds = new android.graphics.Rect();
        UsePaint.getTextBounds(TheText, 0, TheText.length(), TextBounds);
        Draw.drawText
          (
            TheText,
            x, /* depend on UsePaint to align horizontally */
            y - (TextBounds.bottom + TextBounds.top) / 2.0f,
            UsePaint
          );
      } /*DrawCenteredText*/

    public static android.graphics.Rect GetCharacterCellBounds()
      /* returns the bounds of the character “W” in the label font. */
      {
        final Paint LabelHow = new Paint();
        LabelHow.setTypeface(NormalStyle);
        LabelHow.setTextSize(FontSize);
        final android.graphics.Rect TextBounds = new android.graphics.Rect();
        LabelHow.getTextBounds("W", 0, 1, TextBounds);
        return
            TextBounds;
      } /*GetCharacterCellBounds*/

    private static void DrawSubGraduations
      (
        Canvas g,
        float ScaleLength,
        boolean TopEdge,
        Scale TheScale,
        float ParentMarkerLength,
        double LeftArg,
        double RightArg,
        double Leftmost,
        double Rightmost,
        Paint LineHow
      )
      {
        final float MarkerLength = ParentMarkerLength * 0.65f;
        final float MidMarkerLength = ParentMarkerLength * 0.82f;
        float PrevMarkerX = 0.0f;
        double PrevArg = 0.0;
        for (int j = 0; j <= 10; ++j)
          {
            final double ThisArg = LeftArg + j / 10.0 * (RightArg - LeftArg);
            final float MarkerX = (float)(TheScale.PosAt(ThisArg) * ScaleLength);
            if (j != 0)
              {
                if
                  (
                        (LeftArg < RightArg ?
                                ThisArg >= Leftmost && ThisArg <= Rightmost
                            ||
                                PrevArg >= Leftmost && PrevArg <= Rightmost
                        :
                                ThisArg >= Rightmost && ThisArg <= Leftmost
                            ||
                                PrevArg >= Rightmost && PrevArg <= Leftmost
                        )
                        /* at least some part of interval is within scale */
                    &&
                        MarkerX - PrevMarkerX >= 30.0f
                          /* worth subdividing further */
                  )
                  {
                    DrawSubGraduations
                      (
                        /*g =*/ g,
                        /*ScaleLength =*/ ScaleLength,
                        /*TopEdge =*/ TopEdge,
                        /*TheScale =*/ TheScale,
                        /*ParentMarkerLength =*/ MarkerLength,
                        /*LeftArg =*/ PrevArg,
                        /*RightArg =*/ ThisArg,
                        /*Leftmost =*/ Leftmost,
                        /*Rightmost =*/ Rightmost,
                        /*LineHow =*/ LineHow
                      );
                  } /*if*/
                if
                  (
                        (LeftArg < RightArg ?
                            ThisArg >= Leftmost && ThisArg <= Rightmost
                        :
                            ThisArg >= Rightmost && ThisArg <= Leftmost
                        )
                        /* marker is within scale */
                    &&
                        j != 10
                  )
                  {
                    g.drawLine
                      (
                        MarkerX,
                        0.0f,
                        MarkerX,
                        (j == 5 ? MidMarkerLength : MarkerLength) * (TopEdge ? +1 : -1),
                        LineHow
                      );
                  } /*if*/
              } /*if*/
            PrevArg = ThisArg;
            PrevMarkerX = MarkerX;
          } /*for*/
      } /*DrawSubGraduations*/

    public static void DrawGraduations
      (
        Canvas g,
        float ScaleLength,
        boolean TopEdge,
        Scale TheScale,
        double[] PrimaryGraduations, /* in order of increasing X-coordinate, length must be at least 2 */
        double Leftmost, /* at or after PrimaryGraduations[0] */
        double Rightmost, /* at or before PrimaryGraduations[PrimaryGraduations.length - 1] */
        int NrDecimals,
        int Multiplier
      )
      /* common routine for drawing general scale graduations. */
      {
        final boolean Decreasing = PrimaryGraduations[1] < PrimaryGraduations[0];
        final Paint LineHow = new Paint();
        final Paint TextHow = new Paint();
      /* No anti-aliasing for LineHow, doesn't look good */
        TextHow.setAntiAlias(true);
        TextHow.setTextSize(FontSize);
        if (Decreasing)
          {
            TextHow.setColor(AltColor);
          } /*if*/
        for (int i = 0; i < PrimaryGraduations.length - 1; ++i)
          {
            final float LeftPos = (float)(TheScale.PosAt(PrimaryGraduations[i]) * ScaleLength);
            final float RightPos = (float)(TheScale.PosAt(PrimaryGraduations[i + 1]) * ScaleLength);
            if
              (
                !g.quickReject
                  (
                    /*left =*/ LeftPos,
                    /*top =*/ TopEdge ? 0.0f : - PrimaryMarkerLength,
                    /*right =*/ RightPos,
                    /*bottom =*/ TopEdge ? PrimaryMarkerLength : 0.0f,
                    /*type =*/ Canvas.EdgeType.AA
                  )
              )
              {
                if (i != 0 || Leftmost == PrimaryGraduations[0])
                  {
                    g.drawLine
                      (
                        LeftPos, 0.0f,
                        LeftPos, TopEdge ? PrimaryMarkerLength : - PrimaryMarkerLength,
                        LineHow
                      );
                    DrawCenteredText
                      (
                        /*Draw =*/ g,
                        /*TheText =*/
                            String.format
                              (
                                Global.StdLocale,
                                String.format(Global.StdLocale, "%%.%df", NrDecimals),
                                PrimaryGraduations[i] * Multiplier
                              ),
                        /*x =*/ LeftPos,
                        /*y =*/ TopEdge ? PrimaryMarkerLength : - PrimaryMarkerLength,
                        /*UsePaint =*/ TextHow
                      );
                  } /*if*/
                DrawSubGraduations
                  (
                    /*g =*/ g,
                    /*ScaleLength =*/ ScaleLength,
                    /*TopEdge =*/ TopEdge,
                    /*TheScale =*/ TheScale,
                    /*ParentMarkerLength =*/ PrimaryMarkerLength,
                    /*LeftArg =*/ PrimaryGraduations[i],
                    /*RightArg =*/ PrimaryGraduations[i + 1],
                    /*Leftmost =*/ Leftmost,
                    /*Rightmost =*/ Rightmost,
                    /*LineHow =*/ LineHow
                  );
              } /*if*/
          } /*for*/
        if (Leftmost != PrimaryGraduations[0])
          {
          /* draw alternate-colour marker indicating scale does not wraparound */
            LineHow.setColor(AltColor);
            g.drawLine
              (
                0.0f, 0.0f,
                0.0f, TopEdge ? PrimaryMarkerLength : - PrimaryMarkerLength,
                LineHow
              );
          } /*if*/
      } /*DrawGraduations*/

    public static void DrawSimpleGraduations
      (
        Canvas g,
        float ScaleLength,
        boolean TopEdge,
        Scale TheScale,
        int NrPrimarySteps, /* negative to go backwards */
        boolean IncludeZero
      )
      /* common routine for drawing wrappable scale graduations. */
      {
        final int NrGraduations = Math.abs(NrPrimarySteps) + (IncludeZero ? 1 : 0);
        final double[] Graduations = new double[NrGraduations];
        for (int i = 0; i < NrGraduations; ++i)
          {
            Graduations[NrPrimarySteps > 0 ? i : NrGraduations - 1 - i] =
                (i + (IncludeZero ? 0 : 1)) / Math.abs((double)NrPrimarySteps);
          } /*for*/
        DrawGraduations
          (
            /*g =*/ g,
            /*ScaleLength =*/ ScaleLength,
            /*TopEdge =*/ TopEdge,
            /*TheScale =*/ TheScale,
            /*PrimaryGraduations =*/ Graduations,
            /*Leftmost =*/ Graduations[0],
            /*Rightmost =*/ Graduations[Graduations.length - 1],
            /*NrDecimals =*/ 0,
            /*Multiplier =*/ 10
          );
      } /*DrawSimpleGraduations*/

    public static float DrawLabel
      (
        Canvas g, /* null to only determine string width */
        Scale TheScale,
        boolean Upper,
        PointF Pos, /* position for rendering, ignored if g is null */
        Paint.Align Alignment, /* alignment for rendering, ignored if g is null */
        int Color
      )
      /* draws/measures the label for the specified scale, doing appropriate variable
        substitution depending on Upper. */
      {
        final Paint LabelHow = new Paint();
        LabelHow.setAntiAlias(true);
        LabelHow.setTextSize(FontSize);
        final PointF LabelPos = g != null ? new PointF(Pos.x, Pos.y) : null;
        final String Template = TheScale.Name();
        float TotalLength = 0.0f;
        for (boolean Render = g != null && Alignment == Paint.Align.LEFT;;)
          {
          /* first pass: determine total length; second pass: actually draw */
          /* first pass unneeded if drawing with alignment = LEFT */
          /* second pass unneeded if only measuring length and not drawing */
            int CharPos = 0;
            StringBuilder CurSeg = null;
            for (;;)
              {
                if
                  (
                        CharPos == Template.length()
                    ||
                        Template.charAt(CharPos) == Scales.VarEscape
                  )
                  {
                    if (CurSeg != null)
                      {
                        final String SegStr = CurSeg.toString();
                        LabelHow.setTypeface(NormalStyle);
                        LabelHow.setTextSkewX(0.0f);
                        if (Render)
                          {
                            LabelHow.setColor(Color);
                            g.drawText(SegStr, LabelPos.x, LabelPos.y, LabelHow);
                            LabelPos.x += LabelHow.measureText(SegStr);
                          }
                        else
                          {
                            TotalLength += LabelHow.measureText(SegStr);
                          } /*if*/
                        CurSeg = null;
                      } /*if*/
                    if (CharPos == Template.length())
                        break;
                     {
                     /* found another occurrence of VarEscape, substitute
                        with appropriate variable name */
                        final String VarStr = Upper ? UpperVarName : LowerVarName;
                        LabelHow.setTypeface(ItalicStyle);
                        if (!ItalicStyle.isItalic())
                          {
                          /* fake it */
                            LabelHow.setTextSkewX(-0.25f); /* as per docs recommendation */
                          } /*if*/
                        if (Render)
                          {
                            LabelHow.setColor(Color);
                            g.drawText(VarStr, LabelPos.x, LabelPos.y, LabelHow);
                            LabelPos.x += LabelHow.measureText(VarStr);
                          }
                        else
                          {
                            TotalLength += LabelHow.measureText(VarStr);
                          } /*if*/
                     }
                    if (CharPos + 1 == Template.length())
                        break;
                  }
                else
                  {
                    if (CurSeg == null)
                      {
                        CurSeg = new StringBuilder();
                      } /*if*/
                    CurSeg.append(Template.charAt(CharPos));
                  } /*if*/
                ++CharPos;
              } /*for*/
            if (Render || g == null)
                break;
            LabelPos.x -= TotalLength / (Alignment == Paint.Align.CENTER ? 2.0f : 1.0f);
            Render = true;
          } /*for*/
        return
            TotalLength;
      } /*DrawLabel*/

/*
    The actual scales
*/

    public static class XNScale implements Scale
      /* produces a scale x**n for any integer n */
      {
        public final String ScaleName;
        public final int Power;
        public final double Offset;

        public XNScale
          (
            String ScaleName,
            int Power,
            double Offset
          )
          {
            this.ScaleName = ScaleName;
            this.Power = Power;
            this.Offset = Offset;
          } /*XNScale*/

        public String Name()
          {
            return
                ScaleName;
          } /*Name*/

        public double Size()
          {
            return
                1.0 / Math.abs(Power);
          } /*Size*/

        public double ExtraOffset()
          {
            return
                Offset;
          } /*ExtraOffset*/

        public double ValueAt
          (
            double Pos
          )
          {
            return
                Math.pow(10.0, Power > 0 ? Pos : 1.0 - Pos) / 10.0;
          } /*ValueAt*/

        public double PosAt
          (
            double Value
          )
          {
            return
                Power > 0 ?
                    Math.log10(Value * 10.0)
                :
                    1.0 - Math.log10(Value * 10.0);
          } /*PosAt*/

        public void Draw
          (
            Canvas g,
            float ScaleLength,
            boolean TopEdge
          )
          {
            DrawSimpleGraduations
              (
                /*g =*/ g,
                /*ScaleLength =*/ ScaleLength,
                /*TopEdge =*/ TopEdge,
                /*TheScale =*/ this,
                /*NrPrimarySteps =*/ Power > 0 ? 10 : -10,
                /*IncludeZero =*/ false
              );
          } /*Draw*/
      } /*XNScale*/

    public static class LogXScale implements Scale
      {
        public String Name()
          {
            return
                "log10 \u1e8b";
          } /*Name*/

        public double Size()
          {
            return
                1.0;
          } /*Size*/

        public double ExtraOffset()
          {
            return
                0.0;
          } /*ExtraOffset*/

        public double ValueAt
          (
            double Pos
          )
          {
            return
                Pos;
          } /*ValueAt*/

        public double PosAt
          (
            double Value
          )
          {
            return
                Value;
          } /*PosAt*/

        public void Draw
          (
            Canvas g,
            float ScaleLength,
            boolean TopEdge
          )
          {
            DrawSimpleGraduations
              (
                /*g =*/ g,
                /*ScaleLength =*/ ScaleLength,
                /*TopEdge =*/ TopEdge,
                /*TheScale =*/ this,
                /*NrPrimarySteps =*/ 10,
                /*IncludeZero =*/ true
              );
          } /*Draw*/
      } /*LogXScale*/

    public static java.util.Map<String, Scale> KnownScales =
        new java.util.HashMap<String, Scale>();
    static
      {
        for
          (
            Scale s :
                new Scale[]
                    {
                        new XNScale("\u1e8b", 1, 0.0),
                        new XNScale("1/\u1e8b", -1, 0.0),
                        new XNScale("\u1e8b²", 2, 0.0),
                        new XNScale("\u1e8b³", 3, 0.0),
                        new XNScale("1/\u1e8b²", -2, 0.0),
                        new XNScale("1/\u1e8b³", -3, 0.0),
                        new LogXScale(),
                        new XNScale("\u03c0\u1e8b", 1, - Math.log10(Math.PI)),
                      /* more TBD */
                    }
          )
          {
            KnownScales.put(s.Name(), s);
          } /*for*/
      } /*static*/

    public static Scale DefaultScale
      (
        Global.ScaleSelector WhichScale
      )
      {
        /*final*/ Scale Result
            = null; /*sigh*/
        switch (WhichScale)
          {
        case UpperScale:
        case LowerScale:
            Result = KnownScales.get("\u1e8b");
        break;
        case TopScale:
        case BottomScale:
            Result = KnownScales.get("\u1e8b²");
        break;
          } /*switch*/
        return
            Result;
      } /*DefaultScale*/

  } /*Scales*/
