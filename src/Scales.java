package nz.gen.geek_central.infinirule;

import android.graphics.PointF;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

public class Scales
  {
    public static final char VarEscape = '\u1e8b'; /* indicates where to substitute variable name */

    public interface Scale /* implemented by all slide-rule scales */
      {
        public String Name();
          /* the unique, user-visible name of this scale. Instances of VarEscape
            will be replaced with a variable name. */

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
            int PixelsWide, /* total width */
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
        int PixelsWide,
        boolean TopEdge,
        Scale TheScale,
        int NrPrimarySteps
      )
      /* common routine for drawing scale graduations. */
      {
        final Paint LineHow = new Paint();
        final Paint TextHow = new Paint();
        TextHow.setTextSize(TextHow.getTextSize() * 2.0f); /* TBD fudge */
        final float Length1 = 20.0f;
        final float Length2 = Length1 / 2.0f;
        for (int i = 1; i < NrPrimarySteps; ++i)
          {
            final float Left1 = (float)(TheScale.PosAt(i) * PixelsWide);
            final float Right1 = (float)(TheScale.PosAt((i + 1)) * PixelsWide);
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
                    /*TheText =*/ String.format("%d", i),
                    /*x =*/ Left1,
                    /*y =*/ TopEdge ? Length1 : - Length1,
                    /*UsePaint =*/ TextHow
                  );
              /* TBD determine number of graduation levels based on PixelsWide */
                for (int j = 1; j < 10; ++j)
                  {
                    final float Left2 = (float)(TheScale.PosAt((10 * i + j) / 10.0) * PixelsWide);
                    g.drawLine(Left2, 0.0f, Left2, TopEdge ? Length2 : - Length2, LineHow);
                  } /*for*/
              } /*if*/
          } /*for*/
      } /*DrawGraduations*/

    public static void DrawLabel
      (
        Canvas g,
        Scale TheScale,
        boolean Upper,
        PointF Pos,
        Paint.Align Alignment
      )
      /* draws the label for the specified scale, doing appropriate variable
        substitution depending on Upper. */
      {
        final Paint LabelHow = new Paint();
        final Typeface NormalStyle = Typeface.defaultFromStyle(Typeface.NORMAL);
        final Typeface ItalicStyle = Typeface.defaultFromStyle(Typeface.ITALIC);
        LabelHow.setTextSize(LabelHow.getTextSize() * 2.0f); /* TBD fudge */
        final PointF LabelPos = new PointF(Pos.x, Pos.y);
        final String Template = TheScale.Name();
        float TotalLength = 0.0f;
        for (boolean Render = Alignment == Paint.Align.LEFT;;)
          {
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
                        final String VarStr = Upper ? "x" : "y";
                        LabelHow.setTypeface(ItalicStyle);
                        if (!ItalicStyle.isItalic())
                          {
                            LabelHow.setTextSkewX(-0.25f); /* as per docs recommendation */
                          } /*if*/
                        if (Render)
                          {
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
            if (Render)
                break;
            LabelPos.x -= TotalLength / (Alignment == Paint.Align.CENTER ? 2.0f : 1.0f);
            Render = true;
          } /*for*/
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
            int PixelsWide,
            boolean TopEdge
          )
          {
            DrawGraduations
              (
                /*g =*/ g,
                /*PixelsWide =*/ PixelsWide,
                /*TopEdge =*/ TopEdge,
                /*TheScale =*/ this,
                /*NrPrimarySteps =*/ 10
              );
          } /*Draw*/
      } /*XScale*/

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
                      /* more TBD */
                    }
          )
          {
            KnownScales.put(s.Name(), s);
          } /*for*/
      } /*static*/
  } /*Scales*/
