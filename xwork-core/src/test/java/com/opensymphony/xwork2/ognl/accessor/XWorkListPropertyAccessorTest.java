/*
 * Copyright (c) 2005 Opensymphony. All Rights Reserved.
 */
package com.opensymphony.xwork2.ognl.accessor;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.XWorkTestCase;
import com.opensymphony.xwork2.util.ListHolder;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.reflection.ReflectionContextState;
import ognl.PropertyAccessor;

import java.util.ArrayList;
import java.util.List;

/**
 * XWorkListPropertyAccessorTest
 * <p/>
 * Created : Nov 7, 2005 3:54:44 PM
 *
 * @author Jason Carreira <jcarreira@eplus.com>
 */
public class XWorkListPropertyAccessorTest extends XWorkTestCase {

    public void testContains() {
        ValueStack vs = ActionContext.getContext().getValueStack();
        ListHolder listHolder = new ListHolder();
        vs.push(listHolder);

        vs.setValue("longs", new String[] {"1", "2", "3"});

        assertNotNull(listHolder.getLongs());
        assertEquals(3, listHolder.getLongs().size());
        assertEquals(new Long(1), (Long) listHolder.getLongs().get(0));
        assertEquals(new Long(2), (Long) listHolder.getLongs().get(1));
        assertEquals(new Long(3), (Long) listHolder.getLongs().get(2));

        assertTrue(((Boolean) vs.findValue("longs.contains(1)")).booleanValue());
    }

    public void testCanAccessListSizeProperty() {
        ValueStack vs = ActionContext.getContext().getValueStack();
        List myList = new ArrayList();
        myList.add("a");
        myList.add("b");

        ListHolder listHolder = new ListHolder();
        listHolder.setStrings(myList);

        vs.push(listHolder);

        assertEquals(new Integer(myList.size()), vs.findValue("strings.size()"));
        assertEquals(new Integer(myList.size()), vs.findValue("strings.size"));
    }

    public void testAutoGrowthCollectionLimit() {
        PropertyAccessor accessor = container.getInstance(PropertyAccessor.class, ArrayList.class.getName());
        ((XWorkListPropertyAccessor) accessor).setAutoGrowCollectionLimit("2");

        List<String> myList = new ArrayList<String>();
        ListHolder listHolder = new ListHolder();
        listHolder.setStrings(myList);

        ValueStack vs = ActionContext.getContext().getValueStack();
        ReflectionContextState.setCreatingNullObjects(vs.getContext(), true);
        vs.push(listHolder);

        vs.setValue("strings[0]", "a");
        vs.setValue("strings[1]", "b");
        vs.setValue("strings[2]", "c");
        vs.setValue("strings[3]", "d");
        vs.findValue("strings[3]");

        assertEquals(3, vs.findValue("strings.size()"));
    }

    public void testDeprecatedAutoGrowCollectionLimit() {
        PropertyAccessor accessor = container.getInstance(PropertyAccessor.class, ArrayList.class.getName());
        ((XWorkListPropertyAccessor) accessor).setDeprecatedAutoGrowCollectionLimit("2");

        List<String> myList = new ArrayList<String>();
        ListHolder listHolder = new ListHolder();
        listHolder.setStrings(myList);

        ValueStack vs = ActionContext.getContext().getValueStack();
        vs.push(listHolder);

        vs.setValue("strings[0]", "a");
        vs.setValue("strings[1]", "b");
        vs.setValue("strings[2]", "c");
        vs.setValue("strings[3]", "d");

        assertEquals(3, vs.findValue("strings.size()"));
    }

    public void testGetPropertyDoesNotGrowPastLimit() {
        PropertyAccessor accessor = container.getInstance(PropertyAccessor.class, ArrayList.class.getName());
        ((XWorkListPropertyAccessor) accessor).setAutoGrowCollectionLimit("2");

        List<String> myList = new ArrayList<String>();
        ListHolder listHolder = new ListHolder();
        listHolder.setStrings(myList);

        ValueStack vs = ActionContext.getContext().getValueStack();
        ReflectionContextState.setCreatingNullObjects(vs.getContext(), true);
        vs.push(listHolder);

        vs.findValue("strings[3]");

        assertEquals(0, myList.size());
    }
}
