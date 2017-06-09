package es.pfctonimartos.tpms;

import java.io.Serializable;

/**
 * Created by toni on 9/6/17.
 */

public class Tyre implements Serializable {
    private static final long serialVersionUID = 1;
    private int dis;
    private int dl;
    private int id;
    private int ir;
    private int tw;
    private int ty;

    public int getIr() {
        return this.ir;
    }

    public void setIr(int ir) {
        this.ir = ir;
    }

    public int getTy() {
        return this.ty;
    }

    public void setTy(int ty) {
        this.ty = ty;
    }

    public int getTw() {
        return this.tw;
    }

    public void setTw(int tw) {
        this.tw = tw;
    }

    public int getDl() {
        return this.dl;
    }

    public void setDl(int dl) {
        this.dl = dl;
    }

    public int getDis() {
        return this.dis;
    }

    public void setDis(int dis) {
        this.dis = dis;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
