package nz.gen.geek_central.infinirule;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Scales
  {
    public interface Scale
      {
        public String Name();
          /* the unique, user-visible name of this scale. */

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

    public static class XScale implements Scale
      {
        public String Name()
          {
            return
                "<I>x</I>";
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
            final Paint LineHow = new Paint();
            final Paint TextHow = new Paint();
            TextHow.setTextSize(TextHow.getTextSize() * 2.0f);
            final float Length1 = 20.0f;
            final float Length2 = Length1 / 2.0f;
            for (int i = 1; i < 10; ++i)
              {
                final float Left1 = (float)(PosAt(i) * PixelsWide);
                final float Right1 = (float)(PosAt((i + 1)) * PixelsWide);
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
                    g.drawText(String.format("%d", i), Left1, TopEdge ? Length1 : - Length1, TextHow);
                  /* TBD determine number of graduation levels based on PixelsWide */
                    for (int j = 1; j < 10; ++j)
                      {
                        final float Left2 = (float)(PosAt((10 * i + j) / 10.0) * PixelsWide);
                        g.drawLine(Left2, 0.0f, Left2, TopEdge ? Length2 : - Length2, LineHow);
                      } /*for*/
                  } /*if*/
              } /*for*/
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
