package com.xj.zk.lock;

import java.util.Comparator;

/**
 * Author: baichuan - xiajun
 * Date: 2016/12/24 11:39
 */
public class NodeComparator<T> implements Comparator<T> {
    @Override
    public int compare(T c1, T c2) {
        long lc1 = Long.parseLong(c1.toString());
        long lc2 = Long.parseLong(c2.toString());
        return lc1 < lc2 ? -1 : lc1 == lc2 ? 0 : 1;
    }
}
