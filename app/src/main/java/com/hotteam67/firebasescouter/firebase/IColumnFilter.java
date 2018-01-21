package com.hotteam67.firebasescouter.firebase;

import java.util.concurrent.Callable;

/**
 * Created by Jakob on 1/17/2018.
 */

public interface IColumnFilter {
    boolean Filter(String value);
}
