package nz.gen.geek_central.infinirule;
/*
    Individual slide-rule scale definition and rendering for Infinirule.

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

import android.graphics.PointF;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

public class Scales
  {
    public static final char VarEscape = '\u1e8b'; /* indicates where to substitute variable name */
    public static final String UpperVarName = "x";
    public static final String LowerVarName = "y";

    public static int
        BackgroundColor, MainColor, AltColor, SpecialMarkerColor, CursorFillColor, CursorEdgeColor;
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

        public boolean Wrap();
          /* true if scale seamlessly wraps around, so decimal point is somewhat arbitrary. */

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

        public SpecialMarker[] SpecialMarkers();
          /* returns positions at which to draw special markers, or null if none. */

        public void Draw
          (
            Canvas g, /* draw it starting at (0, 0) here */
            float ScaleLength, /* total width */
            boolean TopEdge /* false for bottom edge */
          );
      } /*Scale*/

    public static class SpecialMarker
      {
        public final String Name;
        public final double Value;

        public SpecialMarker
          (
            String Name,
            double Value
          )
          {
            this.Name = Name;
            this.Value = Value;
          } /*SpecialMarker*/

      } /*SpecialMarker*/

/*
    Common useful stuff
*/

    public static void DrawCenteredText
      (
        Canvas Draw,
        String TheText,
        float x,
        float y,
        Paint UsePaint,
        float MaxWidth
      )
      /* draws text at position x, vertically centred around y. */
      {
        final android.graphics.Rect TextBounds = new android.graphics.Rect();
        UsePaint.getTextBounds(TheText, 0, TheText.length(), TextBounds);
        final float PrevScaleX = UsePaint.getTextScaleX();
        if (MaxWidth > 0.0f && TextBounds.right - TextBounds.left > MaxWidth)
          {
            UsePaint.setTextScaleX(MaxWidth / (TextBounds.right - TextBounds.left));
          } /*if*/
        Draw.drawText
          (
            TheText,
            x, /* depend on UsePaint to align horizontally */
            y - (TextBounds.bottom + TextBounds.top) / 2.0f,
            UsePaint
          );
        if (MaxWidth > 0.0f)
          {
            UsePaint.setTextScaleX(PrevScaleX);
          } /*if*/
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

    public static class Graduation
      /* represents a text label for a scale graduation. */
      {
        public final double Mantissa, NextMantissa;
        public final int NrDecimals, MinDecimals, Multiplier, Exponent;
        public final boolean ExponentialStep;

        public Graduation
          (
            double Mantissa,
            double NextMantissa, /* might be more or less than Mantissa */
            int NrDecimals,
            int MinDecimals,
            int Multiplier, /* to apply to Mantissa before formatting as label */
            int Exponent,
            boolean ExponentialStep
          )
          {
            this.Mantissa = Mantissa;
            this.NextMantissa = NextMantissa;
            this.NrDecimals = NrDecimals;
            this.MinDecimals = MinDecimals;
            this.Multiplier = Multiplier;
            this.Exponent = Exponent;
            this.ExponentialStep = ExponentialStep;
          } /*Graduation*/

        public Graduation
          (
            double Value,
            double NextValue,
            int NrDecimals,
            int MinDecimals,
            int Multiplier /* to apply to Value before formatting as label */
          )
          {
            this
              (
                Value,
                NextValue,
                NrDecimals,
                MinDecimals,
                Multiplier,
                0,
                false
              );
          } /*Graduation*/

        public Graduation
          (
            int Exponent,
            boolean Increasing
          )
          {
            this
              (
                1.0,
                Increasing ? 10.0 : 0.1,
                0,
                0,
                1,
                Exponent,
                true
              );
          } /*Graduation*/

        public double GetValue()
          {
            return
                Mantissa * Math.pow(10.0, Exponent);
          } /*GetValue*/

        public void DrawCentered
          (
            Canvas Draw,
            float x,
            float y,
            Paint UsePaint,
            float MaxWidth
          )
          {
            final float BaseTextSize = UsePaint.getTextSize();
            final float ExpTextSize = BaseTextSize * 0.5f;
            final float PrevScaleX = UsePaint.getTextScaleX();
            String Mantissa = "";
            if (Exponent == 0 || this.Mantissa != 1.0)
              {
                Mantissa = String.format
                  (
                    Global.StdLocale,
                    String.format(Global.StdLocale, "%%.%df", NrDecimals),
                    this.Mantissa * Multiplier
                  );
                if (NrDecimals > 0 && MinDecimals < NrDecimals)
                  {
                    final int DecPos = Mantissa.indexOf('.');
                    if (DecPos >= 0)
                      {
                        int EndPos = Mantissa.length();
                        for (;;)
                          {
                            if (EndPos - DecPos <= MinDecimals)
                                break;
                            --EndPos;
                            if (Mantissa.charAt(EndPos) != '0')
                              {
                                if (Mantissa.charAt(EndPos) != '.')
                                  {
                                    ++EndPos;
                                  } /*if*/
                                break;
                              } /*if*/
                          } /*for*/
                        if (EndPos < Mantissa.length())
                          {
                            Mantissa = Mantissa.substring(0, EndPos);
                          } /*if*/
                      } /*if*/
                  } /*if*/
                if (Exponent != 0)
                  {
                    Mantissa += "×";
                  } /*if*/
              } /*if*/
            if (Exponent != 0)
              {
                Mantissa += "10";
              } /*if*/
            final android.graphics.Rect MantissaTextBounds = new android.graphics.Rect();
            UsePaint.getTextBounds(Mantissa, 0, Mantissa.length(), MantissaTextBounds);
            final String ExponentStr =
                Exponent != 0 ?
                    String.format(Global.StdLocale, "%d", Exponent)
                :
                    "";
            if (Exponent != 0)
              {
                UsePaint.setTextSize(ExpTextSize);
              } /*if*/
            final android.graphics.Rect ExpTextBounds = new android.graphics.Rect();
            UsePaint.getTextBounds(ExponentStr, 0, ExponentStr.length(), ExpTextBounds);
            if (MaxWidth > 0.0f)
              {
                final float TotalWidth =
                        MantissaTextBounds.right - MantissaTextBounds.left
                    +
                        ExpTextBounds.right - ExpTextBounds.left;
                if (TotalWidth > MaxWidth)
                  {
                    UsePaint.setTextScaleX(MaxWidth / TotalWidth);
                  } /*if*/
              } /*if*/
            final float BaseY = y - (MantissaTextBounds.bottom + MantissaTextBounds.top) / 2.0f;
            final Paint.Align TextAlign = UsePaint.getTextAlign();
            if (Exponent != 0)
              {
                final float ExpY = BaseY + (MantissaTextBounds.bottom + MantissaTextBounds.top) * 0.7f;
                Draw.drawText
                  (
                    ExponentStr,
                        x
                    +
                            (MantissaTextBounds.right - MantissaTextBounds.left)
                        *
                            (TextAlign == Paint.Align.LEFT ?
                                1.0f
                            : TextAlign == Paint.Align.CENTER ?
                                0.5f
                            : /*TextAlign == Paint.Align.RIGHT ?*/
                                0.0f
                            )
                    +
                            (ExpTextBounds.right - ExpTextBounds.left)
                         *
                            (TextAlign == Paint.Align.CENTER ?
                                0.5f
                            :
                                0.0f
                            ),
                    ExpY,
                    UsePaint
                  );
                UsePaint.setTextSize(BaseTextSize);
              } /*if*/
            Draw.drawText
              (
                Mantissa,
                    x
                +
                        (ExpTextBounds.right - ExpTextBounds.left)
                     *
                        (TextAlign == Paint.Align.RIGHT ?
                            - 1.0f
                        :
                            0.0f
                        ),
                BaseY,
                UsePaint
              );
            if (MaxWidth > 0.0f)
              {
                UsePaint.setTextScaleX(PrevScaleX);
              } /*if*/
          /* UsePaint.setTextSize(BaseTextSize); */ /* already restored */
          } /*DrawCentered*/

        public Graduation[] Subdivide
          (
            int NrSteps
          )
          /* returns a series of subdivided graduations. */
          {
            final Graduation[] Result = new Graduation[NrSteps];
            final double UseMantissa;
            double MantissaStep;
            final int UseExponent;
            if (ExponentialStep && Math.abs(NextMantissa) < Math.abs(Mantissa))
              {
                UseMantissa = Mantissa * 10.0;
                MantissaStep = NextMantissa * 10.0;
                UseExponent = Exponent - 1;
              }
            else
              {
                UseMantissa = Mantissa;
                MantissaStep = NextMantissa;
                UseExponent = Exponent;
              } /*if*/
            MantissaStep = (MantissaStep - UseMantissa) / NrSteps;
            for (int i = 0; i < NrSteps; ++i)
              {
                Result[i] = new Graduation
                  (
                    /*Mantissa =*/ UseMantissa + i * MantissaStep,
                    /*NextMantissa =*/ UseMantissa + (i + 1) * MantissaStep,
                    /*NrDecimals =*/ NrDecimals + 1, /* assumes NrSteps is around 10 */
                    /*MinDecimals =*/ MinDecimals,
                    /*Multiplier =*/ Multiplier,
                    /*Exponent =*/ UseExponent,
                    /*ExponentialStep =*/ false
                  );
              } /*for*/
            return
                Result;
          } /*Subdivide*/

      } /*Graduation*/

    private static Graduation[] MakeGraduations
      (
        double[] Values,
          /* might be in monotonically-increasing or monotonically-decreasing order,
            but placement on scale is always left-to-right */
        int NrDecimals,
        int MinDecimals,
        int Multiplier,
        boolean PlusExponents, /* false => following args ignored */
        int FromExponent,
        int ToExponent
      )
      /* generates a set of Graduation objects covering the specified values,
        and optionally also the specified range of exponents. */
      {
        final Graduation[] Result = new Graduation
            [
                Values.length + (PlusExponents ? Math.abs(ToExponent - FromExponent) + 1 : 0)
            ];
        for (int i = 0; i < Values.length; ++i)
          {
            Result[i] = new Graduation
              (
                Values[i],
                i < Values.length - 1 ?
                    Values[i + 1]
                : PlusExponents ?
                    Math.pow(10.0, FromExponent) / Multiplier
                :
                    Values[i],
                NrDecimals,
                MinDecimals,
                Multiplier
              );
          } /*for*/
        if (PlusExponents)
          {
            final int Step = ToExponent >= FromExponent ? +1 : -1;
            int e = FromExponent;
            int i = Values.length;
            for (;;)
              {
                Result[i] = new Graduation(e, Step > 0);
                if (e == ToExponent)
                    break;
                e += Step;
                i += 1;
              } /*for*/
          } /*if*/
        return
            Result;
      } /*MakeGraduations*/

    private static class SubGraduations
      {
        final Canvas g;
        final float ScaleLength;
        final boolean TopEdge;
        final Scale TheScale;
        final double Leftmost; /* lower limit of reading of entire scale */
        final double Rightmost; /* upper limit of reading of entire scale */
        final Paint TextHow;
        final Paint LineHow;
        final SpecialMarker[] SpecialMarkers;
        final Paint SpecialMarkerTextHow; /* for special markers */
        final Paint SpecialMarkerLineHow; /* for special markers */
        final float TopMarkerLength; /* so special markers line up with top-level graduation labels */

        public SubGraduations
          (
            Canvas g,
            float ScaleLength,
            boolean TopEdge,
            Scale TheScale,
            double Leftmost,
            double Rightmost,
            Paint TextHow,
            Paint LineHow,
            SpecialMarker[] SpecialMarkers,
            Paint SpecialMarkerTextHow,
            Paint SpecialMarkerLineHow,
            float TopMarkerLength
          )
          {
            this.g = g;
            this.ScaleLength = ScaleLength;
            this.TopEdge = TopEdge;
            this.TheScale = TheScale;
            this.Leftmost = Leftmost;
            this.Rightmost = Rightmost;
            this.TextHow = TextHow;
            this.LineHow = LineHow;
            this.SpecialMarkers = SpecialMarkers;
            this.SpecialMarkerTextHow = SpecialMarkerTextHow;
            this.SpecialMarkerLineHow = SpecialMarkerLineHow;
            this.TopMarkerLength = TopMarkerLength;
          } /*SubGraduations*/

        public void Draw
          (
            float ParentMarkerLength,
            double LeftArg,
            double RightArg,
            int NrSteps,
            Graduation ToSubdivide /* if non-null, graduation corresponding to LeftArg */
          )
          /* draws another level of sub-graduations within the specified interval,
            going recursive if zoom is large enough. */
          {
            final boolean Increasing = LeftArg < RightArg;
            final boolean DoGraduations;
              {
                final float Leftmost = (float)(TheScale.PosAt(LeftArg) * ScaleLength);
                final float Rightmost = (float)(TheScale.PosAt(RightArg) * ScaleLength);
                DoGraduations =
                        ToSubdivide != null
                    &&
                        g.quickReject
                          (
                            /*left =*/ Leftmost - 1.0f,
                            /*top =*/ TopEdge ? 0.0f : - PrimaryMarkerLength,
                            /*right =*/ Leftmost + 1.0f,
                            /*bottom =*/ TopEdge ? PrimaryMarkerLength : 0.0f,
                            /*type =*/ Canvas.EdgeType.AA
                          )
                    &&
                        g.quickReject
                          (
                            /*left =*/ Rightmost - 1.0f,
                            /*top =*/ TopEdge ? 0.0f : - PrimaryMarkerLength,
                            /*right =*/ Rightmost + 1.0f,
                            /*bottom =*/ TopEdge ? PrimaryMarkerLength : 0.0f,
                            /*type =*/ Canvas.EdgeType.AA
                          );
              }
            final Graduation[] Subdivisions = DoGraduations ? ToSubdivide.Subdivide(NrSteps) : null;
            final float MarkerLength = ParentMarkerLength * 0.65f;
            final float MidMarkerLength = ParentMarkerLength * 0.82f;
            float PrevGradX = 0.0f;
            double PrevArg = 0.0;
            for (int j = 0; j <= NrSteps; ++j)
              {
                final double ThisArg = LeftArg + (double)j / NrSteps * (RightArg - LeftArg);
                final float GradX = (float)(TheScale.PosAt(ThisArg) * ScaleLength);
                if (j != 0)
                  {
                    final boolean Subdivide =
                            !g.quickReject
                              (
                                /*left =*/ PrevGradX,
                                /*top =*/ TopEdge ? 0.0f : - PrimaryMarkerLength,
                                /*right =*/ GradX,
                                /*bottom =*/ TopEdge ? PrimaryMarkerLength : 0.0f,
                                /*type =*/ Canvas.EdgeType.AA
                              )
                        &&
                            (Increasing ?
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
                            GradX - PrevGradX >= 30.0f;
                              /* worth subdividing further */
                    if (Subdivide)
                      {
                        Draw
                          (
                            /*ParentMarkerLength =*/ MarkerLength,
                            /*LeftArg =*/ PrevArg,
                            /*RightArg =*/ ThisArg,
                            /*NrSteps =*/
                                NrSteps == 1 && ToSubdivide != null && ToSubdivide.ExponentialStep ?
                                    9
                                :
                                    10,
                            /*ToSubdivide =*/ Subdivisions != null ? Subdivisions[j - 1] : null
                          );
                      } /*if*/
                    if
                      (
                            (Increasing ?
                                ThisArg >= Leftmost && ThisArg <= Rightmost
                            :
                                ThisArg >= Rightmost && ThisArg <= Leftmost
                            )
                            /* marker is within scale */
                        &&
                            j != NrSteps
                      )
                      {
                        final float UseMarkerLength =
                            j % NrSteps == 0 ?
                                ParentMarkerLength
                            : (NrSteps == 9 || NrSteps == 10) && j % NrSteps == (Increasing ? NrSteps - 5 : 5) ?
                                MidMarkerLength
                            :
                                MarkerLength;
                        g.drawLine
                          (
                            GradX,
                            0.0f,
                            GradX,
                            TopEdge ? UseMarkerLength : - UseMarkerLength,
                            LineHow
                          );
                        if (Subdivisions != null && j < NrSteps)
                          {
                            Subdivisions[j].DrawCentered
                              (
                                /*Draw =*/ g,
                                /*x =*/ GradX,
                                /*y =*/ TopEdge ? PrimaryMarkerLength : - PrimaryMarkerLength,
                                /*UsePaint =*/ TextHow,
                                /*MaxWidth =*/ 0.9f * Math.abs(GradX - PrevGradX) /* roughly */
                              );
                          } /*if*/
                        if (!Subdivide && SpecialMarkers != null)
                          {
                          /* special markers not done at lower level, do them at this level */
                            for (SpecialMarker ThisMarker : SpecialMarkers)
                              {
                                if
                                  (
                                    Increasing ?
                                        ThisMarker.Value > PrevArg && ThisArg >= ThisMarker.Value
                                    :
                                        ThisMarker.Value < PrevArg && ThisArg <= ThisMarker.Value
                                  )
                                  {
                                    final float MarkerX = (float)(TheScale.PosAt(ThisMarker.Value) * ScaleLength);
                                    g.drawLine
                                      (
                                        MarkerX,
                                        0.0f,
                                        MarkerX,
                                        MidMarkerLength * (TopEdge ? +1 : -1),
                                        SpecialMarkerLineHow
                                      );
                                    DrawCenteredText
                                      (
                                        /*Draw =*/ g,
                                        /*TheText =*/ ThisMarker.Name,
                                        /*x =*/ MarkerX,
                                        /*y =*/ TopMarkerLength * (TopEdge ? +1 : -1),
                                        /*UsePaint =*/ SpecialMarkerTextHow,
                                        /*MaxWidth =*/ -1.0f
                                      );
                                    /* fixme: should check label text does not overlap graduation labels */
                                  } /*if*/
                              } /*for*/
                          } /*if*/
                      } /*if*/
                  } /*if*/
                PrevArg = ThisArg;
                PrevGradX = GradX;
              } /*for*/
          } /*Draw*/

      } /*SubGraduations*/;

    public static void DrawGraduations
      (
        Canvas g,
        float ScaleLength, /* total length of scale */
        boolean TopEdge, /* true if markers descend from edge, false if they ascend from edge */
        Scale TheScale, /* for mapping readings to X positions and showing markers */
        Graduation[] PrimaryGraduations,
          /* scale readings at which to draw graduations at this level, in order of increasing
            X-coordinate, might extend slightly outside scale limits, length must be at least 2 */
        int[] NrDivisions,
          /* divisions between graduations at this level, length must equal
            PrimaryGraduations.length - 1 */
        double Leftmost, /* lower limit of scale reading, at or after PrimaryGraduations[0] */
        double Rightmost
          /* upper limit of scale reading, at or before
            PrimaryGraduations[PrimaryGraduations.length - 1] */
      )
      /* common routine for drawing general scale graduations. */
      {
        final boolean Decreasing =
            PrimaryGraduations[1].GetValue() < PrimaryGraduations[0].GetValue();
        final Paint LineHow = new Paint();
        final Paint TextHow = new Paint();
        LineHow.setColor(MainColor);
      /* No anti-aliasing for LineHow or SpecialMarkerLineHow, looks best without */
        TextHow.setAntiAlias(true);
        TextHow.setTextSize(FontSize);
        if (Decreasing)
          {
            TextHow.setColor(AltColor);
            TextHow.setTextAlign(Paint.Align.RIGHT);
          }
        else
          {
            TextHow.setColor(MainColor);
          } /*if*/
        final SpecialMarker[] SpecialMarkers = TheScale.SpecialMarkers();
          /* faster to do it here and pass to SubGraduations.Draw calls */
        final Paint SpecialMarkerTextHow = SpecialMarkers != null ? new Paint() : null;
        final Paint SpecialMarkerLineHow = SpecialMarkers != null ? new Paint() : null;
        if (SpecialMarkers != null)
          {
            SpecialMarkerTextHow.setAntiAlias(true);
            SpecialMarkerTextHow.setTextSize(FontSize);
            SpecialMarkerTextHow.setTextAlign(Paint.Align.CENTER);
            SpecialMarkerTextHow.setColor(SpecialMarkerColor);
            SpecialMarkerLineHow.setColor(SpecialMarkerColor);
          } /*if*/
        for (int i = 0; i < PrimaryGraduations.length - 1; ++i)
          {
            final float LeftPos = (float)(TheScale.PosAt(PrimaryGraduations[i].GetValue()) * ScaleLength);
            final float RightPos = (float)(TheScale.PosAt(PrimaryGraduations[i + 1].GetValue()) * ScaleLength);
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
                if (i != 0 || Leftmost == PrimaryGraduations[0].GetValue())
                  {
                    g.drawLine
                      (
                        LeftPos, 0.0f,
                        LeftPos, TopEdge ? PrimaryMarkerLength : - PrimaryMarkerLength,
                        LineHow
                      );
                    PrimaryGraduations[i].DrawCentered
                      (
                        /*Draw =*/ g,
                        /*x =*/ LeftPos,
                        /*y =*/ TopEdge ? PrimaryMarkerLength : - PrimaryMarkerLength,
                        /*UsePaint =*/ TextHow,
                        /*MaxWidth =*/ 0.9f * Math.abs(RightPos - LeftPos)
                      );
                  } /*if*/
                new SubGraduations
                  (
                    /*g =*/ g,
                    /*ScaleLength =*/ ScaleLength,
                    /*TopEdge =*/ TopEdge,
                    /*TheScale =*/ TheScale,
                    /*Leftmost =*/ Leftmost,
                    /*Rightmost =*/ Rightmost,
                    /*TextHow =*/ TextHow,
                    /*LineHow =*/ LineHow,
                    /*SpecialMarkers =*/ SpecialMarkers,
                    /*SpecialMarkerTextHow =*/ SpecialMarkerTextHow,
                    /*SpecialMarkerLineHow =*/ SpecialMarkerLineHow,
                    /*TopMarkerLength =*/ PrimaryMarkerLength
                  )
                .Draw
                  (
                    /*ParentMarkerLength =*/ PrimaryMarkerLength,
                    /*LeftArg =*/ PrimaryGraduations[i].GetValue(),
                    /*RightArg =*/ PrimaryGraduations[i + 1].GetValue(),
                    /*NrSteps =*/ NrDivisions[i],
                    /*ToSubdivide =*/ PrimaryGraduations[i]
                  );
              } /*if*/
          } /*for*/
        if (!TheScale.Wrap())
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
        final int[] NrDivisions = new int[NrGraduations - 1];
        for (int i = 0; i < NrDivisions.length; ++i)
          {
            NrDivisions[i] = 10;
          } /*for*/
        DrawGraduations
          (
            /*g =*/ g,
            /*ScaleLength =*/ ScaleLength,
            /*TopEdge =*/ TopEdge,
            /*TheScale =*/ TheScale,
            /*PrimaryGraduations =*/
                MakeGraduations
                  (
                    /*Values =*/ Graduations,
                    /*NrDecimals =*/ 0,
                    /*MinDecimals =*/ 0,
                    /*Multiplier =*/ 10,
                    /*PlusExponents =*/ false,
                    /*FromExponent =*/ 0,
                    /*ToExponent =*/ 0
                  ),
            /*NrDivisions =*/ NrDivisions,
            /*Leftmost =*/ Graduations[0],
            /*Rightmost =*/ Graduations[Graduations.length - 1]
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
              /* scale goes from 10 ** (Power - 1) to 10 ** Power for positive Power,
                10 ** Power to 10 ** (Power + 1) for negative Power */
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

        public boolean Wrap()
          {
            return
                true;
          } /*Wrap*/

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

        public SpecialMarker[] SpecialMarkers()
          {
            final SpecialMarker StdMarker = new SpecialMarker("π", Math.PI);
            final int Norm = (int)Math.ceil(Math.log10(StdMarker.Value));
            return
                new SpecialMarker[]
                    {
                        new SpecialMarker(StdMarker.Name, StdMarker.Value * Math.pow(10.0, - Norm)),
                    };
          } /*SpecialMarkers*/

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

        public boolean Wrap()
          {
            return
                false;
          } /*Wrap*/

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

        public SpecialMarker[] SpecialMarkers()
          {
            return
                null;
          } /*SpecialMarker*/

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

    public static class LnXScale implements Scale
      {
        public final static double Ln10 = Math.log(10.0);

        public String Name()
          {
            return
                "ln \u1e8b";
          } /*Name*/

        public double Size()
          {
            return
                1.0;
          } /*Size*/

        public boolean Wrap()
          {
            return
                true;
          } /*Wrap*/

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
                Pos * Ln10;
          } /*ValueAt*/

        public double PosAt
          (
            double Value
          )
          {
            return
                Value / Ln10;
          } /*PosAt*/

        public SpecialMarker[] SpecialMarkers()
          {
            return
                null;
          } /*SpecialMarker*/

        public void Draw
          (
            Canvas g,
            float ScaleLength,
            boolean TopEdge
          )
          {
            final int NrGraduations = 25;
            final double[] Graduations = new double[NrGraduations];
            for (int i = 0; i < NrGraduations; ++i)
              {
                Graduations[i] = i / 10.0;
              } /*for*/
            final int[] NrDivisions = new int[NrGraduations - 1];
            for (int i = 0; i < NrDivisions.length; ++i)
              {
                NrDivisions[i] = 10;
              } /*for*/
            DrawGraduations
              (
                /*g =*/ g,
                /*ScaleLength =*/ ScaleLength,
                /*TopEdge =*/ TopEdge,
                /*TheScale =*/ this,
                /*PrimaryGraduations =*/
                    MakeGraduations
                      (
                        /*Values =*/ Graduations,
                        /*NrDecimals =*/ 1,
                        /*MinDecimals =*/ 1,
                        /*Multiplier =*/ 1,
                        /*PlusExponents =*/ false,
                        /*FromExponent =*/ 0,
                        /*ToExponent =*/ 0
                      ),
                /*NrDivisions =*/ NrDivisions,
                /*Leftmost =*/ Graduations[0],
                /*Rightmost =*/ ValueAt(1.0)
              );
          } /*Draw*/
      } /*LnXScale*/

    public static class ASinATanXScale implements Scale
      /* 0.57° < asin/atan X in degrees ≤ 5.7° */
      {
        public String Name()
          {
            return
                "0.57° < asin°|atan° \u1e8b ≤ 5.7°";
          } /*Name*/

        public double Size()
          {
            return
                1.0;
          } /*Size*/

        public boolean Wrap()
          {
            return
                false;
          } /*Wrap*/

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
                Math.pow(10.0, Pos) * 18.0 / Math.PI;
          } /*ValueAt*/

        public double PosAt
          (
            double Value
          )
          {
            return
                Math.log10(Value * Math.PI / 18.0);
          } /*PosAt*/

        public SpecialMarker[] SpecialMarkers()
          {
            return
                null;
          } /*SpecialMarker*/

        public void Draw
          (
            Canvas g,
            float ScaleLength,
            boolean TopEdge
          )
          {
            final double[] Graduations = new double[]
                {
                    0.0,
                    1.0,
                    1.5,
                    2.0,
                    2.5,
                    3.0,
                    4.0,
                    5.0,
                    6.0,
                };
            final int[] NrDivisions = new int[]
                {
                    10,
                    5,
                    5,
                    5,
                    10,
                    10,
                    10,
                    10,
                };
            DrawGraduations
              (
                /*g =*/ g,
                /*ScaleLength =*/ ScaleLength,
                /*TopEdge =*/ TopEdge,
                /*TheScale =*/ this,
                /*PrimaryGraduations =*/
                    MakeGraduations
                      (
                        /*Values =*/ Graduations,
                        /*NrDecimals =*/ 1,
                        /*MinDecimals =*/ 1,
                        /*Multiplier =*/ 1,
                        /*PlusExponents =*/ false,
                        /*FromExponent =*/ 0,
                        /*ToExponent =*/ 0
                      ),
                /*NrDivisions =*/ NrDivisions,
                /*Leftmost =*/ 1.8 / Math.PI,
                /*Rightmost =*/ 18.0 / Math.PI
              );
          } /*Draw*/
      } /*ASinATanXScale*/

    public static class ASinACosXScale implements Scale
      /* asin/acos X in degrees, > 5.7° */
      {
        public final String ScaleName;
        public final boolean CosScale;

        public ASinACosXScale
          (
            boolean CosScale
          )
          {
            this.CosScale = CosScale;
            ScaleName = String.format
              (
                Global.StdLocale,
                "a%s° \u1e8b > 5.7°",
                CosScale ? "cos" : "sin"
              );
          } /*ASinACosXScale*/

        public String Name()
          {
            return
                ScaleName;
          } /*Name*/

        public double Size()
          {
            return
                1.0;
          } /*Size*/

        public boolean Wrap()
          {
            return
                false;
          } /*Wrap*/

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
            final double T = Math.pow(10.0, Pos - 1.0);
            return
                Math.toDegrees
                  (
                    CosScale ?
                        Math.acos(T)
                    :
                        Math.asin(T)
                  );
          } /*ValueAt*/

        public double PosAt
          (
            double Value
          )
          {
            final double T = Math.toRadians(Value);
            return
                    Math.log10
                      (
                        CosScale ?
                            Math.cos(T)
                        :
                            Math.sin(T)
                      )
                +
                    1.0;
          } /*PosAt*/

        public SpecialMarker[] SpecialMarkers()
          {
            return
                null;
          } /*SpecialMarker*/

        public void Draw
          (
            Canvas g,
            float ScaleLength,
            boolean TopEdge
          )
          {
            final double[] Graduations = new double[]
                {
                    4.0,
                    6.0,
                    8.0,
                    10.0,
                    15.0,
                    20.0,
                    30.0,
                    40.0,
                    70.0,
                    90.0,
                };
            final int[] NrDivisions = new int[]
                {
                    20,
                    20,
                    20,
                    25,
                    25,
                    10,
                    10,
                    30,
                    4,
                };
            double Leftmost = 18.0 / Math.PI;
            double Rightmost = 90.0;
            if (CosScale)
              {
                for (int i = 0; i < Graduations.length; ++i)
                  {
                    Graduations[i] = 90.0 - Graduations[i];
                  } /*for*/
                Leftmost = 90.0 - Leftmost;
                Rightmost = 90.0 - Rightmost;
              } /*if*/
            DrawGraduations
              (
                /*g =*/ g,
                /*ScaleLength =*/ ScaleLength,
                /*TopEdge =*/ TopEdge,
                /*TheScale =*/ this,
                /*PrimaryGraduations =*/
                    MakeGraduations
                      (
                        /*Values =*/ Graduations,
                        /*NrDecimals =*/ 1,
                        /*MinDecimals =*/ 1,
                        /*Multiplier =*/ 1,
                        /*PlusExponents =*/ false,
                        /*FromExponent =*/ 0,
                        /*ToExponent =*/ 0
                      ),
                /*NrDivisions =*/ NrDivisions,
                /*Leftmost =*/ Leftmost,
                /*Rightmost =*/ Rightmost
              );
          } /*Draw*/
      } /*ASinACosXScale*/

    public static class ATanXScale implements Scale
      /* atan X in degrees, > 5.7° */
      {
        public String Name()
          {
            return
                "atan° \u1e8b > 5.7°";
          } /*Name*/

        public double Size()
          {
            return
                1.0;
          } /*Size*/

        public boolean Wrap()
          {
            return
                false;
          } /*Wrap*/

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
                Math.toDegrees(Math.atan(Math.pow(10.0, Pos - 1.0)));
          } /*ValueAt*/

        public double PosAt
          (
            double Value
          )
          {
            return
                    Math.log10(Math.tan(Math.toRadians(Value)))
                +
                    1.0;
          } /*PosAt*/

        public SpecialMarker[] SpecialMarkers()
          {
            return
                null;
          } /*SpecialMarker*/

        public void Draw
          (
            Canvas g,
            float ScaleLength,
            boolean TopEdge
          )
          {
            final double[] Graduations = new double[]
                {
                    4.0,
                    6.0,
                    8.0,
                    10.0,
                    15.0,
                    20.0,
                    30.0,
                    40.0,
                    45.0,
                };
            final int[] NrDivisions = new int[]
                {
                    20,
                    20,
                    20,
                    25,
                    25,
                    10,
                    10,
                    5,
                };
            DrawGraduations
              (
                /*g =*/ g,
                /*ScaleLength =*/ ScaleLength,
                /*TopEdge =*/ TopEdge,
                /*TheScale =*/ this,
                /*PrimaryGraduations =*/
                    MakeGraduations
                      (
                        /*Values =*/ Graduations,
                        /*NrDecimals =*/ 1,
                        /*MinDecimals =*/ 1,
                        /*Multiplier =*/ 1,
                        /*PlusExponents =*/ false,
                        /*FromExponent =*/ 0,
                        /*ToExponent =*/ 0
                      ),
                /*NrDivisions =*/ NrDivisions,
                /*Leftmost =*/ 18.0 / Math.PI,
                /*Rightmost =*/ 45.0
              );
          } /*Draw*/
      } /*ATanXScale*/

    public static class ExpXScale implements Scale
      {
        public final String ScaleName;
        public final int Level;
        public final boolean Base10;
        public final double Factor, Base;

        public ExpXScale
          (
            String ScaleName,
            int Level,
            boolean Base10
          )
          {
            this.ScaleName = ScaleName;
            this.Level = Level;
            Factor = Math.pow(10.0, 1 - Math.abs(Level)) * Math.signum(Level);
            this.Base10 = Base10;
            Base = Base10 ? Math.log(10.0) : 1.0;
          } /*ExpXScale*/

        public String Name()
          {
            return
                ScaleName;
          } /*Name*/

        public double Size()
          {
            return
                1.0;
          } /*Size*/

        public boolean Wrap()
          {
            return
                false;
          } /*Wrap*/

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
                Math.exp(Math.pow(10.0, Pos) * Factor * Base);
          } /*ValueAt*/

        public double PosAt
          (
            double Value
          )
          {
            return
                Math.log10(Math.log(Value) / Factor / Base);
          } /*PosAt*/

        public SpecialMarker[] SpecialMarkers()
          {
            SpecialMarker[] Result = null;
            if (Base10 && Level == 2)
              {
                Result = new SpecialMarker[]
                    {
                        new SpecialMarker("e", Math.E),
                    };
              } /*if*/
            return
                Result;
          } /*SpecialMarker*/

        public void Draw
          (
            Canvas g,
            float ScaleLength,
            boolean TopEdge
          )
          {
            double[] Graduations;
            double Leftmost, Rightmost;
            int[] NrDivisions;
            int NrDecimals, MinDecimals = 99;
            boolean PlusExponents = false; /* to begin with */
            int FromExponent = 0, ToExponent = 0;
            if (Base10)
              {
                switch(Level)
                  {
                default: /*sigh*/
                case 1:
                    Graduations = new double[]
                        {
                            10.0,
                            15.0,
                            20.0,
                            50.0,
                            100.0,
                            500.0,
                        };
                    PlusExponents = true;
                    FromExponent = 3;
                    ToExponent = 10;
                    NrDivisions = new int[]
                        {
                            5,
                            5,
                            3,
                            5,
                            4,
                            5,
                            1,
                            1,
                            1,
                            1,
                            1,
                            1,
                            1,
                        };
                    Leftmost = 10.0;
                    Rightmost = Math.pow(10.0, 10.0);
                    NrDecimals = 0;
                break;
                case 2:
                    Graduations = new double[]
                        {
                            1.25,
                            1.3,
                            1.35,
                            1.4,
                            1.5,
                            1.6,
                            1.7,
                            1.8,
                            1.9,
                            2.0,
                            2.5,
                            3.0,
                            4.0,
                            5.0,
                            6.0,
                            7.0,
                            8.0,
                            9.0,
                            10.0,
                        };
                    NrDivisions = new int[]
                        {
                            5,
                            5,
                            5,
                            10,
                            10,
                            10,
                            10,
                            10,
                            10,
                            5,
                            5,
                            10,
                            10,
                            10,
                            10,
                            10,
                            10,
                            10,
                        };
                    Leftmost = Math.pow(10.0, 0.1);
                    Rightmost = 10.0;
                    NrDecimals = 2;
                break;
                case 3:
                    Graduations = new double[]
                        {
                            1.02,
                            1.03,
                            1.04,
                            1.05,
                            1.06,
                            1.07,
                            1.08,
                            1.09,
                            1.10,
                            1.15,
                            1.20,
                            1.25,
                            1.30,
                        };
                    NrDivisions = new int[]
                        {
                            10,
                            10,
                            10,
                            10,
                            10,
                            10,
                            10,
                            10,
                            5,
                            5,
                            5,
                            5,
                        };
                    Leftmost = Math.pow(10.0, 0.01);
                    Rightmost = Math.pow(10.0, 0.1);
                    NrDecimals = 2;
                break;
                case 4:
                    Graduations = new double[]
                        {
                            1.002,
                            1.003,
                            1.004,
                            1.005,
                            1.006,
                            1.007,
                            1.008,
                            1.009,
                            1.01,
                            1.015,
                            1.02,
                            1.025,
                        };
                    NrDivisions = new int[]
                        {
                            10,
                            10,
                            10,
                            10,
                            10,
                            10,
                            10,
                            10,
                            5,
                            5,
                            5,
                        };
                    Leftmost = Math.pow(10.0, 0.001);
                    Rightmost = Math.pow(10.0, 0.01);
                    NrDecimals = 3;
                break;
                case -1:
                    Graduations = new double[]
                        {
                            0.1,
                            0.05,
                            0.01,
                            0.005,
                        };
                    PlusExponents = true;
                    FromExponent = -3;
                    ToExponent = -10;
                    NrDivisions = new int[]
                        {
                            5,
                            4,
                            5,
                            4,
                            1,
                            1,
                            1,
                            1,
                            1,
                            1,
                            1,
                        };
                    Leftmost = Math.pow(10.0, -1);
                    Rightmost = Math.pow(10.0, -10);
                    NrDecimals = 9;
                    MinDecimals = 1;
                break;
                case -2:
                    Graduations = new double[]
                        {
                            0.80,
                            0.75,
                            0.70,
                            0.65,
                            0.60,
                            0.55,
                            0.50,
                            0.45,
                            0.40,
                            0.35,
                            0.30,
                            0.25,
                            0.20,
                            0.15,
                            0.10,
                        };
                    NrDivisions = new int[]
                        {
                            5,
                            5,
                            5,
                            5,
                            5,
                            5,
                            5,
                            5,
                            5,
                            5,
                            5,
                            5,
                            5,
                            5,
                        };
                    Leftmost = Math.pow(10.0, -0.1);
                    Rightmost = Math.pow(10.0, -1);
                    NrDecimals = 2;
                break;
                case -3:
                    Graduations = new double[]
                        {
                            0.98,
                            0.97,
                            0.96,
                            0.95,
                            0.94,
                            0.93,
                            0.92,
                            0.91,
                            0.90,
                            0.85,
                            0.80,
                            0.75,
                        };
                    NrDivisions = new int[]
                        {
                            1,
                            1,
                            1,
                            1,
                            1,
                            1,
                            1,
                            1,
                            5,
                            5,
                            5,
                        };
                    Leftmost = Math.pow(10.0, -0.01);
                    Rightmost = Math.pow(10.0, -0.1);
                    NrDecimals = 2;
                break;
                case -4:
                    Graduations = new double[]
                        {
                            0.998,
                            0.997,
                            0.996,
                            0.995,
                            0.990,
                            0.98,
                            0.97,
                        };
                    NrDivisions = new int[]
                        {
                            1,
                            1,
                            1,
                            5,
                            1,
                            1,
                        };
                    Leftmost = Math.pow(10.0, -0.001);
                    Rightmost = Math.pow(10.0, -0.01);
                    NrDecimals = 3;
                break;
                  } /*switch*/
              }
            else /* base-e */
              {
                switch(Level)
                  {
                default: /*sigh*/
                case 1:
                    Graduations = new double[]
                        {
                            2.0,
                            3.0,
                            4.0,
                            5.0,
                            10.0,
                            20.0,
                            30.0,
                            50.0,
                            100.0,
                            300.0,
                            1000.0,
                            5000.0,
                            10000.0,
                            20000.0,
                        };
                    NrDivisions = new int[]
                        {
                            10,
                            10,
                            10,
                            5,
                            10,
                            10,
                            20,
                            5,
                            20,
                            7,
                            4,
                            5,
                            2,
                        };
                    Leftmost = Math.exp(1.0);
                    Rightmost = Math.exp(10.0);
                    NrDecimals = 0;
                break;
                case 2:
                    Graduations = new double[]
                        {
                            1.10,
                            1.11,
                            1.15,
                            1.2,
                            1.3,
                            1.4,
                            1.5,
                            1.6,
                            1.7,
                            1.8,
                            1.9,
                            2.0,
                            3.0,
                        };
                    NrDivisions = new int[]
                        {
                            1,
                            4,
                            5,
                            1,
                            1,
                            1,
                            1,
                            1,
                            1,
                            1,
                            1,
                            10,
                        };
                    Leftmost = Math.exp(0.1);
                    Rightmost = Math.exp(1.0);
                    NrDecimals = 2;
                break;
                case 3:
                    Graduations = new double[]
                        {
                            1.00,
                            1.01,
                            1.02,
                            1.03,
                            1.04,
                            1.05,
                            1.06,
                            1.07,
                            1.08,
                            1.09,
                            1.10,
                            1.11,
                        };
                    NrDivisions = new int[]
                        {
                            10,
                            10,
                            10,
                            10,
                            10,
                            10,
                            10,
                            10,
                            10,
                            10,
                            10,
                        };
                    Leftmost = Math.exp(0.01);
                    Rightmost = Math.exp(0.1);
                    NrDecimals = 3;
                break;
                case 4:
                    Graduations = new double[]
                        {
                            1.000,
                            1.001,
                            1.002,
                            1.003,
                            1.004,
                            1.005,
                            1.006,
                            1.007,
                            1.008,
                            1.009,
                            1.100,
                            1.101,
                        };
                    NrDivisions = new int[]
                        {
                            10,
                            10,
                            10,
                            10,
                            10,
                            10,
                            10,
                            10,
                            10,
                            10,
                            10,
                        };
                    Leftmost = Math.exp(0.001);
                    Rightmost = Math.exp(0.01);
                    NrDecimals = 4;
                break;
                case -1:
                    Graduations = new double[]
                        {
                            0.36,
                            0.3,
                            0.1,
                            0.01,
                            0.001,
                            0.0001,
                            0.00001,
                        };
                    NrDivisions = new int[]
                        {
                            6,
                            2,
                            9,
                            9,
                            9,
                            9,
                        };
                    Leftmost = Math.exp(-1.0);
                    Rightmost = Math.exp(-10.0);
                    NrDecimals = 4;
                    MinDecimals = 1;
                break;
                case -2:
                    Graduations = new double[]
                        {
                            0.95,
                            0.90,
                            0.85,
                            0.80,
                            0.70,
                            0.60,
                            0.50,
                            0.40,
                            0.30,
                        };
                    NrDivisions = new int[]
                        {
                            5,
                            5,
                            5,
                            1,
                            1,
                            1,
                            1,
                            1,
                        };
                    Leftmost = Math.exp(-0.1);
                    Rightmost = Math.exp(-1.0);
                    NrDecimals = 2;
                break;
                case -3:
                    Graduations = new double[]
                        {
                            1.00,
                            0.99,
                            0.98,
                            0.97,
                            0.96,
                            0.95,
                            0.94,
                            0.93,
                            0.92,
                            0.91,
                            0.90,
                        };
                    NrDivisions = new int[]
                        {
                            10,
                            10,
                            10,
                            10,
                            10,
                            10,
                            10,
                            10,
                            10,
                            10,
                        };
                    Leftmost = Math.exp(-0.01);
                    Rightmost = Math.exp(-0.1);
                    NrDecimals = 2;
                break;
                case -4:
                    Graduations = new double[]
                        {
                            1.00,
                            0.999,
                            0.998,
                            0.997,
                            0.996,
                            0.995,
                            0.994,
                            0.993,
                            0.992,
                            0.991,
                            0.990,
                        };
                    NrDivisions = new int[]
                        {
                            10,
                            10,
                            10,
                            10,
                            10,
                            10,
                            10,
                            10,
                            10,
                            10,
                        };
                    Leftmost = Math.exp(-0.001);
                    Rightmost = Math.exp(-0.01);
                    NrDecimals = 3;
                break;
                  } /*switch*/
              } /*if*/
            DrawGraduations
              (
                /*g =*/ g,
                /*ScaleLength =*/ ScaleLength,
                /*TopEdge =*/ TopEdge,
                /*TheScale =*/ this,
                /*PrimaryGraduations =*/
                    MakeGraduations
                      (
                        /*Values =*/ Graduations,
                        /*NrDecimals =*/ NrDecimals,
                        /*MinDecimals =*/ MinDecimals,
                        /*Multiplier =*/ 1,
                        /*PlusExponents =*/ PlusExponents,
                        /*FromExponent =*/ FromExponent,
                        /*ToExponent =*/ ToExponent
                      ),
                /*NrDivisions =*/ NrDivisions,
                /*Leftmost =*/ Leftmost,
                /*Rightmost =*/ Rightmost
              );
          } /*Draw*/
      } /*ExpXScale*/

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
                        new LnXScale(),
                        new XNScale("\u03c0\u1e8b", 1, - Math.log10(Math.PI)),
                        new ASinATanXScale(),
                        new ASinACosXScale(false),
                        new ASinACosXScale(true),
                        new ATanXScale(),
                        new ExpXScale("exp(\u1e8b)", 1, false),
                        new ExpXScale("exp(0.1\u1e8b)", 2, false),
                        new ExpXScale("exp(0.01\u1e8b)", 3, false),
                        new ExpXScale("exp(0.001\u1e8b)", 4, false),
                        new ExpXScale("exp(-\u1e8b)", -1, false),
                        new ExpXScale("exp(-0.1\u1e8b)", -2, false),
                        new ExpXScale("exp(-0.01\u1e8b)", -3, false),
                        new ExpXScale("exp(-0.001\u1e8b)", -4, false),
                        new ExpXScale("10**\u1e8b", 1, true),
                        new ExpXScale("10**(0.1\u1e8b)", 2, true),
                        new ExpXScale("10**(0.01\u1e8b)", 3, true),
                        new ExpXScale("10**(0.001\u1e8b)", 4, true),
                        new ExpXScale("10**(-\u1e8b)", -1, true),
                        new ExpXScale("10**(-0.1\u1e8b)", -2, true),
                        new ExpXScale("10**(-0.01\u1e8b)", -3, true),
                        new ExpXScale("10**(-0.001\u1e8b)", -4, true),
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
        final Scale Result;
        switch (WhichScale)
          {
        default: /*sigh*/
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
