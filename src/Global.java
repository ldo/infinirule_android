package nz.gen.geek_central.infinirule;
/*
    Global data for Infinirule
*/

public class Global
  {
    public static final java.util.Locale StdLocale = java.util.Locale.US;
      /* for all those places I don't want formatting to be locale-specific */

    public static android.util.DisplayMetrics MainMetrics;

    public static float PixelDensity()
      /* returns scale factor to convert device-independent pixels to device-dependent. */
      {
        return
                MainMetrics.densityDpi
            /
                android.util.DisplayMetrics.DENSITY_DEFAULT;
      } /*PixelDensity*/

    public static enum ScaleSelector
      {
        TopScale,
        UpperScale,
        LowerScale,
        BottomScale,
      } /*ScaleSelector*/

  } /*Global*/
