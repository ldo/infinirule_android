package nz.gen.geek_central.infinirule;

import android.graphics.PointF;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

public class Scales
  {
    public static final char VarEscape = '\u1e8b'; /* indicates where to substitute variable name */
    public static final String UpperVarName = "x";
    public static final String LowerVarName = "y";

    public static float FontSize;
    public static final Typeface NormalStyle = Typeface.defaultFromStyle(Typeface.NORMAL);
    public static final Typeface ItalicStyle = Typeface.defaultFromStyle(Typeface.ITALIC);

    public interface Scale /* implemented by all slide-rule scales */
      {
        public String Name();
          /* the unique, user-visible name of this scale. Instances of VarEscape
            will be replaced with a variable name. */

        public double Size();
          /* returns a relative measure of size, e.g. 1.0 for C & D scales,
            2.0 for square root, 0.5 for A & B (squares). */

        public double ValueAt
          (
            double Pos /* [0.0 .. 1.0) */
          );

        public double PosAt
          (
            double Value /* whatever range is returned by ValueAt */
          );

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

    private static void DrawSubGraduations
      (
        Canvas g,
        float ScaleLength,
        boolean TopEdge,
        Scale TheScale,
        int NrPrimarySteps, /* negative to go backwards */
        float ParentMarkerLength,
        double BaseOffset,
        double Division,
        Paint LineHow
      )
      {
        final float MarkerLength = ParentMarkerLength * 0.65f;
        final float MidMarkerLength = ParentMarkerLength * 0.82f;
        float PrevMarkerX = 0.0f;
        for (int j = 0; j <= 10; ++j)
          {
            final double ThisBaseOffset =
                    BaseOffset
                +
                    j / Division / Math.abs((double)NrPrimarySteps);
            final float MarkerX = (float)(TheScale.PosAt(ThisBaseOffset) * ScaleLength);
            if (j != 0)
              {
                if (Math.abs(MarkerX - PrevMarkerX) >= 30.0f)
                  {
                    DrawSubGraduations
                      (
                        /*g =*/ g,
                        /*ScaleLength =*/ ScaleLength,
                        /*TopEdge =*/ TopEdge,
                        /*TheScale =*/ TheScale,
                        /*NrPrimarySteps =*/ NrPrimarySteps,
                        /*ParentMarkerLength =*/ MarkerLength,
                        /*BaseOffset =*/ ThisBaseOffset,
                        /*Division =*/ Division * 10.0,
                        /*LineHow =*/ LineHow
                      );
                  } /*if*/
                if (j != 10)
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
            PrevMarkerX = MarkerX;
          } /*for*/
      } /*DrawSubGraduations*/

    public static void DrawGraduations
      (
        Canvas g,
        float ScaleLength,
        boolean TopEdge,
        Scale TheScale,
        int NrPrimarySteps, /* negative to go backwards */
        boolean IncludeZero
      )
      /* common routine for drawing scale graduations. */
      {
        final Paint LineHow = new Paint();
        final Paint TextHow = new Paint();
        TextHow.setTextSize(FontSize);
        final float MarkerLength = 20.0f;
        for (int i = NrPrimarySteps > 0 ? 0 : - NrPrimarySteps;;)
          {
            if (NrPrimarySteps > 0 && i == NrPrimarySteps)
                break;
            final float Left1 = (float)(TheScale.PosAt(i / Math.abs((double)NrPrimarySteps)) * ScaleLength);
            final float Right1 = (float)(TheScale.PosAt((i + (NrPrimarySteps > 0 ? +1 : -1)) / Math.abs((double)NrPrimarySteps)) * ScaleLength);
            if
              (
                !g.quickReject
                  (
                    /*left =*/ Left1,
                    /*top =*/ TopEdge ? 0.0f : - MarkerLength,
                    /*right =*/ Right1,
                    /*bottom =*/ TopEdge ? MarkerLength : 0.0f,
                    /*type =*/ Canvas.EdgeType.AA
                  )
              )
              {
                if (i != (NrPrimarySteps > 0 ? 0 : - NrPrimarySteps))
                  {
                    g.drawLine(Left1, 0.0f, Left1, TopEdge ? MarkerLength : - MarkerLength, LineHow);
                  } /*if*/
                if (IncludeZero || i != (NrPrimarySteps > 0 ? 0 : - NrPrimarySteps))
                  {
                    DrawCenteredText
                      (
                        /*Draw =*/ g,
                        /*TheText =*/ String.format(Global.StdLocale, "%d", i),
                        /*x =*/ Left1,
                        /*y =*/ TopEdge ? MarkerLength : - MarkerLength,
                        /*UsePaint =*/ TextHow
                      );
                  } /*if*/
                DrawSubGraduations
                  (
                    /*g =*/ g,
                    /*ScaleLength =*/ ScaleLength,
                    /*TopEdge =*/ TopEdge,
                    /*TheScale =*/ TheScale,
                    /*NrPrimarySteps =*/ NrPrimarySteps,
                    /*ParentMarkerLength =*/ MarkerLength,
                    /*BaseOffset =*/
                            (i - (NrPrimarySteps < 0 ? 1 : 0))
                        /
                            Math.abs((double)NrPrimarySteps),
                    /*Division =*/ 10.0,
                    /*LineHow =*/ LineHow
                  );
              } /*if*/
            if (NrPrimarySteps < 0 && i == 0)
                break;
            i += NrPrimarySteps > 0 ? +1 : -1;
          } /*for*/
      } /*DrawGraduations*/

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
        LabelHow.setTextSize(FontSize);
        final PointF LabelPos = g != null ? new PointF(Pos.x, Pos.y) : null;
        final String Template = TheScale.Name();
        float TotalLength = 0.0f;
        for (boolean Render = g != null && Alignment == Paint.Align.LEFT;;)
          {
          /* first pass: determine total length; second pass: actually draw */
          /* first pass unneeded if alignment is LEFT */
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

        public XNScale
          (
            String ScaleName,
            int Power
          )
          {
            this.ScaleName = ScaleName;
            this.Power = Power;
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
            DrawGraduations
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
            DrawGraduations
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
                        new XNScale("\u1e8b", 1),
                        new XNScale("1/\u1e8b", -1),
                        new XNScale("\u1e8b²", 2),
                        new XNScale("\u1e8b³", 3),
                        new XNScale("1/\u1e8b²", -2),
                        new XNScale("1/\u1e8b³", -3),
                        new LogXScale(),
                      /* more TBD */
                    }
          )
          {
            KnownScales.put(s.Name(), s);
          } /*for*/
      } /*static*/

    public static Scale DefaultScale()
      {
        return
            KnownScales.get("\u1e8b");
      } /*DefaultScale*/

  } /*Scales*/
