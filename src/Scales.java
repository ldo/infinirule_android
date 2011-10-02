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

    public static final java.util.Locale StdLocale = java.util.Locale.US;
      /* for all those places I don't want formatting to be locale-specific */
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

    public static void DrawGraduations
      (
        Canvas g,
        float ScaleLength,
        boolean TopEdge,
        Scale TheScale,
        int NrPrimarySteps
      )
      /* common routine for drawing scale graduations. */
      {
        final Paint LineHow = new Paint();
        final Paint TextHow = new Paint();
        TextHow.setTextSize(FontSize);
        final float Length1 = 20.0f;
        final float Length2 = Length1 / 2.0f;
        for (int i = 1; i < NrPrimarySteps; ++i)
          {
            final float Left1 = (float)(TheScale.PosAt(i) * ScaleLength);
            final float Right1 = (float)(TheScale.PosAt((i + 1)) * ScaleLength);
            if
              (
                !g.quickReject
                  (
                    /*left =*/ Left1,
                    /*top =*/ TopEdge ? 0.0f : - Length1,
                    /*right =*/ Right1,
                    /*bottom =*/ TopEdge ? Length1 : 0.0f,
                    /*type =*/ Canvas.EdgeType.AA
                  )
              )
              {
                g.drawLine(Left1, 0.0f, Left1, TopEdge ? Length1 : - Length1, LineHow);
                DrawCenteredText
                  (
                    /*Draw =*/ g,
                    /*TheText =*/ String.format(StdLocale, "%d", i),
                    /*x =*/ Left1,
                    /*y =*/ TopEdge ? Length1 : - Length1,
                    /*UsePaint =*/ TextHow
                  );
              /* TBD determine number of graduation levels based on ScaleLength */
                for (int j = 1; j < 10; ++j)
                  {
                    final float Left2 = (float)(TheScale.PosAt((10 * i + j) / 10.0) * ScaleLength);
                    g.drawLine(Left2, 0.0f, Left2, TopEdge ? Length2 : - Length2, LineHow);
                  } /*for*/
              } /*if*/
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

    public static class XScale implements Scale
      {
        public String Name()
          {
            return
                "\u1e8b";
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
                Math.pow(10.0, Pos);
          } /*ValueAt*/

        public double PosAt
          (
            double Value
          )
          {
            return
                Math.log10(Value);
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
                /*NrPrimarySteps =*/ 10
              );
          } /*Draw*/
      } /*XScale*/

    public static class X2Scale extends XScale
      {
        public String Name()
          {
            return
                "\u1e8b²";
          } /*Name*/

        public double Size()
          {
            return
                0.5;
          } /*Size*/
      } /*X2Scale*/

    public static java.util.Map<String, Scale> KnownScales =
        new java.util.HashMap<String, Scale>();
    static
      {
        for
          (
            Scale s :
                new Scale[]
                    {
                        new XScale(),
                        new X2Scale(),
                      /* more TBD */
                    }
          )
          {
            KnownScales.put(s.Name(), s);
          } /*for*/
      } /*static*/
  } /*Scales*/
