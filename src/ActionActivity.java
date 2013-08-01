package nz.gen.geek_central.infinirule;
/*
    Support for context menu and action bar, where available.

    Copyright 2013 Lawrence D'Oliveiro <ldo@geek-central.gen.nz>.

    This program is free software: you can redistribute it and/or
    modify it under the terms of the GNU General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
    General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

import android.view.Menu;
import android.view.MenuItem;

public abstract class ActionActivity extends android.app.Activity
  {

    protected java.util.Map<MenuItem, Runnable> OptionsMenu;
    protected java.util.Map<MenuItem, Runnable> ContextMenu;
    protected Menu TheOptionsMenu;
    protected android.view.ContextMenu TheContextMenu;

    public static boolean ClassHasMethod
      (
        String ClassName,
        String MethodName,
        Class<?>... ArgTypes
      )
      /* does the named class have a method with the specified argument types. */
      {
        boolean HasIt;
        try
          {
            HasIt =
                    Class.forName(ClassName)
                        .getDeclaredMethod(MethodName, ArgTypes)
                !=
                    null;
          }
        catch (NoSuchMethodException Nope)
          {
            HasIt = false;
          }
        catch (ClassNotFoundException Huh)
          {
            throw new RuntimeException(Huh.toString());
          } /*try*/
        return
            HasIt;
      } /*ClassHasMethod*/

    public static final boolean HasActionBar =
        ClassHasMethod
          (
            /*ClassName =*/ "android.view.MenuItem",
            /*MethodName = */ "setShowAsAction",
            /*ArgTypes =*/ Integer.TYPE
          );

    protected abstract void OnCreateOptionsMenu();
      /* Do actual creation of options menu here. */

    protected void AddOptionsMenuItem
      (
        int StringID,
        int IconID,
        int ActionBarUsage, /* post-Gingerbread only */
        Runnable Action
      )
      /* Call from within OnCreateOptionsMenu to define menu items. */
      {
        final MenuItem TheItem = TheOptionsMenu.add(StringID);
        if (IconID != 0)
          {
            TheItem.setIcon(IconID);
          } /*if*/
        if (HasActionBar)
          {
            TheItem.setShowAsAction(ActionBarUsage);
          } /*if*/
        OptionsMenu.put(TheItem, Action);
      } /*AddOptionsMenuItem*/

    @Override
    public boolean onCreateOptionsMenu
      (
        Menu TheMenu
      )
      /* takes care of calling your OnCreateOptionsMenu. */
      {
        TheOptionsMenu = TheMenu;
        OptionsMenu = new java.util.HashMap<MenuItem, Runnable>();
        OnCreateOptionsMenu();
        return
            true;
      } /*onCreateOptionsMenu*/

    protected void InitContextMenu
      (
        android.view.ContextMenu TheMenu
      )
      /* must be called at start of setup of context menu. */
      {
        TheContextMenu = TheMenu;
        ContextMenu = new java.util.HashMap<MenuItem, Runnable>();
      } /*InitContextMenu*/

    protected void AddContextMenuItem
      (
        String Name,
        Runnable Action
      )
      /* call to add another item to context menu. */
      {
        ContextMenu.put(TheContextMenu.add(Name), Action);
      } /*AddContextMenuItem*/

    protected void AddContextMenuItem
      (
        int StringID,
        Runnable Action
      )
      /* call to add another item to context menu. */
      {
        AddContextMenuItem(getString(StringID), Action);
      } /*AddContextMenuItem*/

    @Override
    public boolean onOptionsItemSelected
      (
        MenuItem TheItem
      )
      {
        boolean Handled = false;
        final Runnable Action = OptionsMenu != null ? OptionsMenu.get(TheItem) : null;
        if (Action != null)
          {
            Action.run();
            Handled = true;
          } /*if*/
        return
            Handled;
      } /*onOptionsItemSelected*/

    @Override
    public boolean onContextItemSelected
      (
        MenuItem TheItem
      )
      {
        boolean Handled = false;
        final Runnable Action = ContextMenu != null ? ContextMenu.get(TheItem) : null;
        if (Action != null)
          {
            Action.run();
            Handled = true;
          } /*if*/
        return
            Handled;
      } /*onContextItemSelected*/

  } /*ActionActivity*/;
