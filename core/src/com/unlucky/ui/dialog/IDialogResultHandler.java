package com.unlucky.ui.dialog;
/**
 * @author hundun
 * Created on 2021/10/22
 * @param <T>
 */
public interface IDialogResultHandler<T> {
    void handleDialogResult(T event);
}
