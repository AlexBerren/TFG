package com.loszorros.quienjuega.RegistroLoginActivitys.Complementario;

import android.view.MotionEvent;

import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewDisableScrollTouchListener implements RecyclerView.OnItemTouchListener {
    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        return true; // Indicamos que vamos a interceptar el evento táctil
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        // No hacemos nada aquí
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        // No hacemos nada aquí
    }
}