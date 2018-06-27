package cs.inhatc.sensor.smart_sensor_project;


import android.app.Activity;
import android.view.View;

import net.daum.mf.map.api.MapView;

/* http://ondestroy.tistory.com/entry/%EB%A7%B5%EB%B7%B0-%ED%98%84%EC%9E%AC%EC%9C%84%EC%B9%98-%ED%91%9C%EC%8B%9C-%EB%A7%B5%EB%B7%B0-%ED%84%B0%EC%B9%98%EC%8B%9C-%EB%A7%88%EC%BB%A4%ED%91%9C%EC%8B%9C-%EC%98%88%EC%A0%9C */
public class MapLocation extends MapView{

    MapView Map;

    public MapView getMap() {
        return Map;
    }

    public void setMap(View map) {
        Map = (MapView)map;
    }

    public MapLocation(Activity activity){
        super(activity);
    }

}
