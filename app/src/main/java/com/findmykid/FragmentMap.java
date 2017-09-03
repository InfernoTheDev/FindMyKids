package com.findmykid;

import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;
import org.reactivestreams.Subscription;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentMap extends Fragment implements OnMapReadyCallback {

    MapView mapView;
    String TAG = "FragmentMap";
    RequestQueue requestQueue;
    GoogleMap map;

    String search_type = "";
    String value = "";

    DefaultRetryPolicy defaultRetryPolicy;

    Observable requestLocationInterval;

    public FragmentMap() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = (MapView) v.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        MapsInitializer.initialize(this.getActivity());

        Bundle bundle = this.getArguments();

        search_type = bundle.getString("search_type", "");
        value = bundle.getString("value", "");

        mapView.getMapAsync(this);

        defaultRetryPolicy =
                new DefaultRetryPolicy( 10000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        requestLocationInterval = interval();


        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {

        mapView.onResume();

        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;

        Log.d(TAG, "onMapReady: " + search_type + ", Value: " + value);

        if (search_type.equalsIgnoreCase("ip")) {
            requestLocationInterval.subscribe();
        } else {

            try {

                JSONObject parseJOSN = new JSONObject(value.toString());

                setMarker(parseJOSN);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

    public Observable interval(){
        return Observable.interval(5, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .map(new Function<Long, Long>() {
                    @Override
                    public Long apply(Long aLong) throws Exception {
                        requestLocation(value)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Observer<JSONObject>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {
                                        Log.d(TAG, "onSubscribe: " + d.toString());
                                    }

                                    @Override
                                    public void onNext(JSONObject value) {
                                        Log.d(TAG, "onNext: " + value.toString());
                                        setMarker(value);
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Log.d(TAG, "onError: " + e.getMessage());
                                        Toast.makeText(getActivity(), "Location error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    }

                                    @Override
                                    public void onComplete() {
                                        Log.d(TAG, "onComplete: ");
                                    }
                                });
                        return null;
                    }
                });
    }

    public Observable requestLocation(String ip) {
        final String url = "http://" + ip + "/getlocation";
        Log.d(TAG, "requestLocation: " + url);
        Toast.makeText(getActivity(), "requestLocation URL: " + url, Toast.LENGTH_LONG).show();
        return Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(final ObservableEmitter<Object> e) throws Exception {
                JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        e.onNext(response);
                        e.onComplete();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Error: " + error.getMessage());
                        // hide the progress dialog
                        e.onError(error);
                    }
                });
                if (requestQueue == null) {
                    requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
                }
                jor.setRetryPolicy(defaultRetryPolicy);
                requestQueue.getCache().clear();
                requestQueue.add(jor);
            }
        });
    }

    public void setMarker(JSONObject latlng) {
        try {
            Double latValue = 0.0;
            Double lngValue = 0.0;

            String lat = latlng.getString("lat");
            String lng = latlng.getString("lng");

            latValue = Double.parseDouble(lat);
            lngValue = Double.parseDouble(lng);

            LatLng latLng = new LatLng(latValue, lngValue);

            MarkerOptions markerOptions =
                    new MarkerOptions()
                    .position(latLng)
                    .title("Marker");

            map.addMarker(markerOptions);

            // Updates the location and zoom of the MapView
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
            map.animateCamera(cameraUpdate);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
