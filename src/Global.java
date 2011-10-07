package nz.gen.geek_central.infinirule;
/*
    Global data for Infinirule.

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

    public static int ScaleNameID
      (
        ScaleSelector WhichScale
      )
      /* returns the string ID for an adjective to describe the specified scale. */
      {
        final int SelectorID;
        switch (WhichScale)
          {
        case TopScale:
            SelectorID = R.string.top;
        break;
        default: /*sigh*/
        case UpperScale:
            SelectorID = R.string.upper;
        break;
        case LowerScale:
            SelectorID = R.string.lower;
        break;
        case BottomScale:
            SelectorID = R.string.bottom;
        break;
          } /*switch*/
        return
            SelectorID;
      } /*ScaleNameID*/

  } /*Global*/
