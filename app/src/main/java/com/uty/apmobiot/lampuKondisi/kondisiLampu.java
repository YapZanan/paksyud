package com.uty.apmobiot.lampuKondisi;

public class kondisiLampu {
    private Boolean kondisi;
    public kondisiLampu(){}

    public kondisiLampu(Boolean kondisi) {
        this.kondisi = kondisi;
    }

    public Boolean getKondisi() {
        return kondisi;
    }

    public void setKondisi(Boolean kondisi) {
        this.kondisi = kondisi;
    }
}
