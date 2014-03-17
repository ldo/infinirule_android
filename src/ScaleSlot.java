package nz.gen.geek_central.infinirule;
/*
    Enumeration identifying scale display positions for Infinirule.

    Copyright 2014 Lawrence D'Oliveiro <ldo@geek-central.gen.nz>.

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

import android.graphics.Rect;

public enum ScaleSlot
  {
    TOP(0, "top", true, true, R.string.top)
      {
        public float YOffset()
          {
            final Rect TextBounds = Scales.GetCharacterCellBounds();
            return
                - Scales.HalfLayoutHeight + (Scales.PrimaryMarkerLength - TextBounds.top) * 1.5f;
          } /*YOffset*/
      },

    UPPER(1, "upper", true, false, R.string.upper)
      {
        public float YOffset()
          {
            final Rect TextBounds = Scales.GetCharacterCellBounds();
            return
                - (Scales.PrimaryMarkerLength + TextBounds.bottom) * 1.5f;
          } /*YOffset*/
      },

    LOWER(2, "lower", false, false, R.string.lower)
      {
        public float YOffset()
          {
            final Rect TextBounds = Scales.GetCharacterCellBounds();
            return
                (Scales.PrimaryMarkerLength - TextBounds.top) * 1.5f;
          } /*YOffset*/
      },

    BOTTOM(3, "bottom", false, true, R.string.bottom)
      {
        public float YOffset()
          {
            final Rect TextBounds = Scales.GetCharacterCellBounds();
            return
                Scales.HalfLayoutHeight - (Scales.PrimaryMarkerLength + TextBounds.bottom) * 1.5f;
          } /*YOffset*/
      },

    ;

    public final int Val;
    public final String Name;
    public final boolean Upper, Edge;
    public final int SelectorID; /* string ID for an adjective to describe the specified scale */

    ScaleSlot
      (
        int Val,
        String Name,
        boolean Upper,
        boolean Edge,
        int SelectorID
      )
      {
        this.Val = Val;
        this.Name = Name;
        this.Upper = Upper;
        this.Edge = Edge;
        this.SelectorID = SelectorID;
      } /*ScaleSlot*/

    public static final int NR = values().length;

    public static ScaleSlot WithVal
      (
        int Val
      )
      {
        for (int i = 0;;)
          {
            if (values()[i].Val == Val)
              {
                return
                    values()[i];
              } /*if*/
            ++i;
          } /*for*/
      } /*WithVal*/

    public static ScaleSlot WithFlags
      (
        boolean Upper,
        boolean Edge
      )
      {
        return
            Upper ?
                Edge ? TOP : UPPER
            :
                Edge ? BOTTOM : LOWER;
      } /*WithFlags*/

    public abstract float YOffset();

  } /*ScaleSlot*/;
