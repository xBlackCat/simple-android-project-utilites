package org.xblackcat.android.ui.list;

/**
 * 27.09.13 10:22
 *
 * @author xBlackCat
 */
public interface IItemGroup<K, E> {
    K getGroupKey();

    E[] getElements();
}
