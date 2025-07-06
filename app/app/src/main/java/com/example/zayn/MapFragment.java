package com.example.zayn;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.graphics.Color;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapFragment extends Fragment {

    private MapView mapView;
    private static final String ARG_CHAT_ID = "chat_id";
    private String currentChatId = null;
    private final OkHttpClient httpClient = new OkHttpClient();
    private GeoPoint myLocation = null;
    private FusedLocationProviderClient fusedLocationClient;

    private final Map<String, GeoPoint> knownPoints = new HashMap<String, GeoPoint>() {{
        put("Yerevan", new GeoPoint(40.1792, 44.4991));
        put("Vanadzor", new GeoPoint(40.8140, 44.4939));
        put("Gyumri", new GeoPoint(40.7894, 43.8475));
        put("Dilijan", new GeoPoint(40.7400, 44.8638));
        put("Sevan", new GeoPoint(40.5556, 44.9536));
        put("Kapan", new GeoPoint(39.2076, 46.4154));
        put("Goris", new GeoPoint(39.5111, 46.3383));
        put("Jermuk", new GeoPoint(39.8417, 45.6736));
        put("Stepanakert", new GeoPoint(39.8177, 46.7528));
        put("Tatev Monastery", new GeoPoint(39.4021, 46.2530));
        put("Noravank", new GeoPoint(39.6847, 45.2328));
        put("Geghard", new GeoPoint(40.1406, 44.8186));
        put("Garni Temple", new GeoPoint(40.1190, 44.7251));
        put("Sevanavank", new GeoPoint(40.5615, 44.9362));
        put("Haghpat Monastery", new GeoPoint(41.0956, 44.7106));
        put("Zvartnots Cathedral", new GeoPoint(40.1539, 44.3219));
        put("Lavash Restaurant (Yerevan)", new GeoPoint(40.1811, 44.5146));
        put("Dolmama Restaurant (Yerevan)", new GeoPoint(40.1836, 44.5147));
    }};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        Context ctx = requireContext().getApplicationContext();
        Configuration.getInstance().load(ctx, androidx.preference.PreferenceManager.getDefaultSharedPreferences(ctx));

        mapView = view.findViewById(R.id.map);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(7.5);
        mapView.getController().setCenter(new GeoPoint(40.1792, 44.4991));
        mapView.setMinZoomLevel(3.0);
        mapView.setMaxZoomLevel(18.0);

        CompassOverlay compassOverlay = new CompassOverlay(ctx, mapView);
        compassOverlay.enableCompass();
        mapView.getOverlays().add(compassOverlay);

        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(mapView);
        scaleBarOverlay.setAlignBottom(true);
        mapView.getOverlays().add(scaleBarOverlay);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        requestLocation();

        if (getArguments() != null && getArguments().containsKey(ARG_CHAT_ID)) {
            currentChatId = getArguments().getString(ARG_CHAT_ID);
            loadRouteIfExists(currentChatId);
        }

        addCityMarkers();

        return view;
    }

//    private void loadRouteIfExists(String chatId) {
//        if (chatId == null) return;
//        DatabaseReference ref = FirebaseDatabase.getInstance("https://zayn-e9f9c-default-rtdb.europe-west1.firebasedatabase.app")
//                .getReference("chats").child(chatId).child("route");
//        ref.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (!snapshot.exists()) return;
//
//                List<String> placeNames = new ArrayList<>();
//                for (DataSnapshot snap : snapshot.getChildren()) {
//                    String name = snap.getValue(String.class);
//                    if (name != null) placeNames.add(name);
//                }
//
//                List<GeoPoint> geoPoints = new ArrayList<>();
//                for (String name : placeNames) {
//                    GeoPoint pt = knownPoints.get(name);
//                    if (pt != null) geoPoints.add(pt);
//                }
//
//                if (geoPoints.size() < 2) return;
//
//                mapView.getOverlays().removeIf(o -> o instanceof Polyline || o instanceof Marker);
//
//                for (int i = 0; i < geoPoints.size(); i++) {
//                    Marker marker = new Marker(mapView);
//                    marker.setPosition(geoPoints.get(i));
//                    marker.setTitle(placeNames.get(i));
//                    mapView.getOverlays().add(marker);
//                }
//
//                requestRouteFromOSRM(geoPoints);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(getContext(), "Failed to load route", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }


    private void loadRouteIfExists(String chatId) {
        if (chatId == null) {
            Log.d("MapDebug", "❌ chatId is null");
            return;
        }

        Log.d("MapDebug", "🔄 Загружаем маршрут для chatId: " + chatId);

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("chats").child(chatId).child("route");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("MapDebug", "📊 Firebase snapshot exists: " + snapshot.exists());

                if (!snapshot.exists()) {
                    Log.d("MapDebug", "❌ Маршрут в Firebase не найден");
                    // Добавляем маркеры городов, если маршрута нет
                    addCityMarkers();
                    return;
                }

                List<String> placeNames = new ArrayList<>();

                // Проверяем, какой тип данных в snapshot
                if (snapshot.hasChildren()) {
                    // Если это массив
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        String name = snap.getValue(String.class);
                        if (name != null && !name.isEmpty()) {
                            placeNames.add(name);
                            Log.d("MapDebug", "📍 Добавлена точка: " + name);
                        }
                    }
                } else {
                    // Если это строка (возможно, JSON)
                    String routeData = snapshot.getValue(String.class);
                    if (routeData != null) {
                        Log.d("MapDebug", "📝 Данные маршрута: " + routeData);
                        // Парсим строку как JSON или CSV
                        try {
                            // Удаляем лишние символы
                            routeData = routeData.replace("[", "").replace("]", "")
                                    .replace("'", "").replace("\"", "");
                            String[] parts = routeData.split(",");
                            for (String part : parts) {
                                String name = part.trim();
                                if (!name.isEmpty()) {
                                    placeNames.add(name);
                                    Log.d("MapDebug", "📍 Добавлена точка из строки: " + name);
                                }
                            }
                        } catch (Exception e) {
                            Log.e("MapDebug", "❌ Ошибка парсинга строки маршрута: " + e.getMessage());
                        }
                    }
                }

                if (placeNames.isEmpty()) {
                    Log.d("MapDebug", "❌ Список точек маршрута пуст");
                    addCityMarkers();
                    return;
                }

                // Преобразуем названия в координаты
                List<GeoPoint> geoPoints = new ArrayList<>();
                for (String name : placeNames) {
                    GeoPoint pt = knownPoints.get(name);
                    if (pt != null) {
                        geoPoints.add(pt);
                        Log.d("MapDebug", "✅ Найдена точка: " + name + " -> " + pt.getLatitude() + "," + pt.getLongitude());
                    } else {
                        Log.e("MapDebug", "❌ Точка не найдена в knownPoints: " + name);
                    }
                }

                if (geoPoints.size() < 2) {
                    Log.d("MapDebug", "❌ Недостаточно точек для маршрута: " + geoPoints.size());
                    addCityMarkers();
                    return;
                }

                Log.d("MapDebug", "🗺️ Строим маршрут с " + geoPoints.size() + " точками");

                // Очищаем карту от старых маркеров и линий
                mapView.getOverlays().removeIf(overlay ->
                        overlay instanceof Polyline || (overlay instanceof Marker &&
                                !((Marker) overlay).getTitle().equals("Ваше местоположение")));

                // Добавляем маркеры маршрута
                for (int i = 0; i < geoPoints.size(); i++) {
                    Marker marker = new Marker(mapView);
                    marker.setPosition(geoPoints.get(i));
                    marker.setTitle(placeNames.get(i));
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    mapView.getOverlays().add(marker);
                }

                // Добавляем остальные городские маркеры
                addCityMarkers();

                // Строим маршрут через OSRM
                requestRouteFromOSRM(geoPoints);

                // Центрируем карту на первой точке маршрута
                if (!geoPoints.isEmpty()) {
                    mapView.getController().setCenter(geoPoints.get(0));
                    mapView.getController().setZoom(8.0);
                }

                mapView.invalidate();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MapDebug", "❌ Firebase error: " + error.getMessage());
                Toast.makeText(getContext(), "Ошибка загрузки маршрута: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                // В случае ошибки показываем обычные маркеры
                addCityMarkers();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    public void onLocationPermissionGranted() {
        // This method is called when location permission is granted from MainActivity
        requestLocation();
    }

    // Also add the missing newInstance method if it's not already there
    public static MapFragment newInstance(String chatId) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CHAT_ID, chatId);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("MissingPermission")
    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                myLocation = new GeoPoint(location.getLatitude(), location.getLongitude());

                Marker myMarker = new Marker(mapView);
                myMarker.setPosition(myLocation);
                myMarker.setTitle("Ваше местоположение");
                mapView.getOverlays().add(myMarker);

                mapView.getController().setCenter(myLocation);
                mapView.invalidate();
            } else {
                Toast.makeText(getContext(), "Не удалось определить lastLocation", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Обработка результата разрешения
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocation();
            } else {
                Toast.makeText(getContext(), "Разрешение на геолокацию не выдано", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addCityMarkers() {
        for (Map.Entry<String, GeoPoint> entry : knownPoints.entrySet()) {
            String city = entry.getKey();
            GeoPoint point = entry.getValue();

            Marker marker = new Marker(mapView);
            marker.setPosition(point);
            marker.setTitle(city);

            marker.setOnMarkerClickListener((m, mapView) -> {
                if (myLocation != null) {
                    // Показываем текущее местоположение
                    String currentLocationText = String.format("Ваше местоположение: %.4f, %.4f",
                            myLocation.getLatitude(), myLocation.getLongitude());

                    // Сначала показываем прямое расстояние
                    double directDistanceKm = myLocation.distanceToAsDouble(point) / 1000.0;
                    String initialInfo = String.format("%s\nПрямое расстояние: %.1f км\nРасчет маршрута...",
                            currentLocationText, directDistanceKm);
                    m.setSnippet(initialInfo);
                    m.showInfoWindow();

                    // Запрашиваем точный маршрут через OSRM
                    calculateRouteDistance(myLocation, point, (roadDistance, duration) -> {
                        requireActivity().runOnUiThread(() -> {
                            String detailedInfo = String.format(
                                    "%s\nПрямое расстояние: %.1f км\nПо дорогам: %.1f км\nВремя в пути: %.0f мин",
                                    currentLocationText, directDistanceKm, roadDistance, duration);
                            m.setSnippet(detailedInfo);
                            m.showInfoWindow();
                        });
                    });
                } else {
                    Toast.makeText(getContext(), "Геолокация не определена", Toast.LENGTH_SHORT).show();
                }
                return true;
            });

            mapView.getOverlays().add(marker);
        }
    }

    private void calculateRouteDistance(GeoPoint from, GeoPoint to, RouteCallback callback) {
        String url = String.format("http://router.project-osrm.org/route/v1/driving/%f,%f;%f,%f?overview=false",
                from.getLongitude(), from.getLatitude(), to.getLongitude(), to.getLatitude());

        Request request = new Request.Builder().url(url).build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("OSRM", "Ошибка расчета маршрута: " + e.getMessage());
                // В случае ошибки возвращаем примерное значение
                double estimatedDistance = from.distanceToAsDouble(to) / 1000.0 * 1.3; // +30% к прямому расстоянию
                double estimatedTime = (estimatedDistance / 60.0) * 60.0; // ~60 км/ч
                callback.onRouteCalculated(estimatedDistance, estimatedTime);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("OSRM", "Сервер OSRM вернул ошибку");
                    onFailure(call, new IOException("HTTP " + response.code()));
                    return;
                }

                try {
                    JSONObject json = new JSONObject(response.body().string());
                    JSONArray routes = json.getJSONArray("routes");

                    if (routes.length() > 0) {
                        JSONObject route = routes.getJSONObject(0);
                        double distance = route.getDouble("distance") / 1000.0; // в км
                        double duration = route.getDouble("duration") / 60.0;    // в минутах

                        callback.onRouteCalculated(distance, duration);
                    } else {
                        onFailure(call, new IOException("Нет доступных маршрутов"));
                    }
                } catch (Exception e) {
                    Log.e("OSRM", "Ошибка парсинга ответа: " + e.getMessage());
                    onFailure(call, new IOException("Ошибка парсинга"));
                }
            }
        });
    }

    // Интерфейс для обратного вызова
    private interface RouteCallback {
        void onRouteCalculated(double distanceKm, double durationMin);
    }

//    private void addCityMarkers() {
//        for (Map.Entry<String, GeoPoint> entry : knownPoints.entrySet()) {
//            String city = entry.getKey();
//            GeoPoint point = entry.getValue();
//
//            Marker marker = new Marker(mapView);
//            marker.setPosition(point);
//            marker.setTitle(city);
//
//            marker.setOnMarkerClickListener((m, mapView) -> {
//                if (myLocation != null) {
//                    double distanceKm = myLocation.distanceToAsDouble(point) / 1000.0;
//                    double estimatedTime = (distanceKm / 60.0) * 60.0; // Приблизительно 60 км/ч
//
//                    String info = String.format("Расстояние: %.1f км\nВремя в пути: %.1f мин", distanceKm, estimatedTime);
//                    m.setSnippet(info);
//                    m.showInfoWindow();
//                } else {
//                    Toast.makeText(getContext(), "Геолокация не определена", Toast.LENGTH_SHORT).show();
//                }
//                return true;
//            });
//
//            mapView.getOverlays().add(marker);
//        }
//    }

    public void drawRouteFromAiResponse(String aiResponse, String chatId) {
        if (mapView == null) return;
        currentChatId = chatId;

        String routeLine = null;
        for (String line : aiResponse.split("\n")) {
            if (line.trim().startsWith("1. Travel Route:") || line.trim().startsWith("Travel Route:")) {
                routeLine = line.substring(line.indexOf(":") + 1).trim();
                break;
            }
        }

        if (routeLine == null || routeLine.isEmpty()) {
            Log.e("MapDebug", "❌ Не найден Travel Route");
            return;
        }

        routeLine = routeLine.replace("[", "").replace("]", "").replace("'", "").replace("\"", "");
        String[] parts = routeLine.split(",");
        List<String> placeNames = new ArrayList<>();
        for (String part : parts) {
            String name = part.trim();
            if (!name.isEmpty()) placeNames.add(name);
        }

        List<GeoPoint> geoPoints = new ArrayList<>();
        for (String name : placeNames) {
            GeoPoint pt = knownPoints.get(name);
            if (pt != null) geoPoints.add(pt);
        }

        if (geoPoints.size() < 2) {
            Log.e("MapDebug", "❌ Недостаточно точек для маршрута");
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("chats").child(chatId).child("route");
        ref.setValue(placeNames);

        mapView.getOverlays().removeIf(o -> o instanceof Polyline || o instanceof Marker);

        for (int i = 0; i < geoPoints.size(); i++) {
            Marker marker = new Marker(mapView);
            marker.setPosition(geoPoints.get(i));
            marker.setTitle(placeNames.get(i));
            mapView.getOverlays().add(marker);
        }

        requestRouteFromOSRM(geoPoints);
    }

    private void requestRouteFromOSRM(List<GeoPoint> geoPoints) {
        StringBuilder coordsBuilder = new StringBuilder();
        for (GeoPoint pt : geoPoints) {
            coordsBuilder.append(pt.getLongitude()).append(",").append(pt.getLatitude()).append(";");
        }
        if (coordsBuilder.length() > 0) coordsBuilder.setLength(coordsBuilder.length() - 1);

        String url = "http://router.project-osrm.org/route/v1/driving/" + coordsBuilder + "?overview=full&geometries=geojson";

        Request request = new Request.Builder().url(url).build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("OSRM", "❌ Запрос не удался: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("OSRM", "❌ Сервер OSRM вернул ошибку");
                    return;
                }

                try {
                    JSONObject json = new JSONObject(response.body().string());
                    JSONObject route = json.getJSONArray("routes").getJSONObject(0);
                    JSONArray coords = route.getJSONObject("geometry").getJSONArray("coordinates");

                    List<GeoPoint> routePoints = new ArrayList<>();
                    for (int i = 0; i < coords.length(); i++) {
                        JSONArray coord = coords.getJSONArray(i);
                        double lon = coord.getDouble(0);
                        double lat = coord.getDouble(1);
                        routePoints.add(new GeoPoint(lat, lon));
                    }

                    requireActivity().runOnUiThread(() -> {
                        Polyline routeLine = new Polyline();
                        routeLine.setColor(Color.BLUE);
                        routeLine.setWidth(8f);
                        routeLine.setPoints(routePoints);
                        mapView.getOverlays().add(routeLine);

                        mapView.invalidate();
                        Log.d("OSRM", "✅ Маршрут по дорогам построен без промежуточных точек");
                    });

                } catch (Exception e) {
                    Log.e("OSRM", "❌ Ошибка парсинга: " + e.getMessage());
                }
            }
        });
    }
}


//import android.Manifest;
//import android.annotation.SuppressLint;
//import android.content.pm.PackageManager;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.core.app.ActivityCompat;
//import androidx.fragment.app.Fragment;
//import org.osmdroid.config.Configuration;
//import org.osmdroid.util.GeoPoint;
//import org.osmdroid.views.MapView;
//import org.osmdroid.views.overlay.ScaleBarOverlay;
//import org.osmdroid.views.overlay.compass.CompassOverlay;
//import android.content.Context;
//import org.osmdroid.views.overlay.Polyline;
//import java.util.*;
//import android.graphics.Color;
//import android.util.Log;
//import android.widget.Toast;
//
//import com.google.android.gms.location.LocationServices;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//import org.osmdroid.views.overlay.Marker;
//import com.google.android.gms.location.FusedLocationProviderClient;
//
//import okhttp3.Call;
//import okhttp3.Callback;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.Response;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//import java.io.IOException;
//
//public class MapFragment extends Fragment {
//    private MapView mapView;
//    private static final String ARG_CHAT_ID = "chat_id";
//    private String currentChatId = null;
//    private final OkHttpClient httpClient = new OkHttpClient();
//    private GeoPoint myLocation = null;
//    private FusedLocationProviderClient fusedLocationClient;
//
//    private final Map<String, GeoPoint> knownPoints = new HashMap<String, GeoPoint>() {{
//        put("Yerevan", new GeoPoint(40.1792, 44.4991));
//        put("Vanadzor", new GeoPoint(40.8140, 44.4939));
//        put("Gyumri", new GeoPoint(40.7894, 43.8475));
//        put("Dilijan", new GeoPoint(40.7400, 44.8638));
//        put("Sevan", new GeoPoint(40.5556, 44.9536));
//        put("Kapan", new GeoPoint(39.2076, 46.4154));
//        put("Goris", new GeoPoint(39.5111, 46.3383));
//        put("Jermuk", new GeoPoint(39.8417, 45.6736));
//        put("Stepanakert", new GeoPoint(39.8177, 46.7528));
//        put("Tatev Monastery", new GeoPoint(39.4021, 46.2530));
//        put("Noravank", new GeoPoint(39.6847, 45.2328));
//        put("Geghard", new GeoPoint(40.1406, 44.8186));
//        put("Garni Temple", new GeoPoint(40.1190, 44.7251));
//        put("Sevanavank", new GeoPoint(40.5615, 44.9362));
//        put("Haghpat Monastery", new GeoPoint(41.0956, 44.7106));
//        put("Zvartnots Cathedral", new GeoPoint(40.1539, 44.3219));
//        put("Lavash Restaurant (Yerevan)", new GeoPoint(40.1811, 44.5146));
//        put("Dolmama Restaurant (Yerevan)", new GeoPoint(40.1836, 44.5147));
//    }};
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_map, container, false);
//
//        Context ctx = requireContext().getApplicationContext();
//        Configuration.getInstance().load(ctx, androidx.preference.PreferenceManager.getDefaultSharedPreferences(ctx));
//
//        mapView = view.findViewById(R.id.map);
//        mapView.setMultiTouchControls(true);
//        mapView.getController().setZoom(7.5);
//        mapView.getController().setCenter(new GeoPoint(40.1792, 44.4991));
//        mapView.setMinZoomLevel(3.0);
//        mapView.setMaxZoomLevel(18.0);
//
//        CompassOverlay compassOverlay = new CompassOverlay(ctx, mapView);
//        compassOverlay.enableCompass();
//        mapView.getOverlays().add(compassOverlay);
//
//        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(mapView);
//        scaleBarOverlay.setAlignBottom(true);
//        mapView.getOverlays().add(scaleBarOverlay);
//
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
//        requestLocation();
//
//        if (getArguments() != null && getArguments().containsKey(ARG_CHAT_ID)) {
//            currentChatId = getArguments().getString(ARG_CHAT_ID);
//            loadRouteIfExists(currentChatId);
//        }
//
//        addCityMarkers();
//
//        return view;
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        if (mapView != null) mapView.onResume();
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        if (mapView != null) mapView.onPause();
//    }
//
//    @SuppressLint("MissingPermission")
//    private void requestLocation() {
//        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
//            return;
//        }
//
//        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
//            if (location != null) {
//                myLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
//
//                Marker myMarker = new Marker(mapView);
//                myMarker.setPosition(myLocation);
//                myMarker.setTitle("Ваше местоположение");
//                mapView.getOverlays().add(myMarker);
//
//                mapView.getController().setCenter(myLocation);
//                mapView.invalidate();
//            } else {
//                Toast.makeText(getContext(), "Не удалось получить lastLocation, запрашиваем обновления...", Toast.LENGTH_SHORT).show();
//
//                fusedLocationClient.requestLocationUpdates(
//                        new com.google.android.gms.location.LocationRequest.Builder(10000).build(),
//                        locationResult -> {
//                            if (!locationResult.getLocations().isEmpty()) {
//                                android.location.Location loc = locationResult.getLastLocation();
//                                myLocation = new GeoPoint(loc.getLatitude(), loc.getLongitude());
//
//                                Marker myMarker = new Marker(mapView);
//                                myMarker.setPosition(myLocation);
//                                myMarker.setTitle("Ваше местоположение");
//                                mapView.getOverlays().add(myMarker);
//
//                                mapView.getController().setCenter(myLocation);
//                                mapView.invalidate();
//                            }
//                        },
//                        null
//                );
//            }
//        });
//    }
//
//
//
//    private void addCityMarkers() {
//        for (Map.Entry<String, GeoPoint> entry : knownPoints.entrySet()) {
//            String city = entry.getKey();
//            GeoPoint point = entry.getValue();
//
//            Marker marker = new Marker(mapView);
//            marker.setPosition(point);
//            marker.setTitle(city);
//
//            marker.setOnMarkerClickListener((m, mapView) -> {
//                if (myLocation != null) {
//                    double distanceKm = myLocation.distanceToAsDouble(point) / 1000.0;
//                    double estimatedTime = (distanceKm / 60.0) * 60.0; // ~60 км/ч
//
//                    String info = String.format("Расстояние: %.1f км\nВремя в пути: %.1f мин", distanceKm, estimatedTime);
//                    m.setSnippet(info);
//                    m.showInfoWindow();
//                } else {
//                    Toast.makeText(getContext(), "Геолокация не определена", Toast.LENGTH_SHORT).show();
//                }
//                return true;
//            });
//
//            mapView.getOverlays().add(marker);
//        }
//    }
//
//
//    public void drawRouteFromAiResponse(String aiResponse, String chatId) {
//        if (mapView == null) return;
//        currentChatId = chatId;
//
//        String routeLine = null;
//        for (String line : aiResponse.split("\n")) {
//            if (line.trim().startsWith("1. Travel Route:") || line.trim().startsWith("Travel Route:")) {
//                routeLine = line.substring(line.indexOf(":") + 1).trim();
//                break;
//            }
//        }
//
//        if (routeLine == null || routeLine.isEmpty()) {
//            Log.e("MapDebug", "❌ Не найден Travel Route");
//            return;
//        }
//
//        routeLine = routeLine.replace("[", "").replace("]", "").replace("'", "").replace("\"", "");
//        String[] parts = routeLine.split(",");
//        List<String> placeNames = new ArrayList<>();
//        for (String part : parts) {
//            String name = part.trim();
//            if (!name.isEmpty()) placeNames.add(name);
//        }
//
//        List<GeoPoint> geoPoints = new ArrayList<>();
//        for (String name : placeNames) {
//            GeoPoint pt = knownPoints.get(name);
//            if (pt != null) geoPoints.add(pt);
//        }
//
//        if (geoPoints.size() < 2) {
//            Log.e("MapDebug", "❌ Недостаточно точек для маршрута");
//            return;
//        }
//
//        DatabaseReference ref = FirebaseDatabase.getInstance("https://zayn-e9f9c-default-rtdb.europe-west1.firebasedatabase.app")
//                .getReference("chats").child(chatId).child("route");
//        ref.setValue(placeNames);
//
//        mapView.getOverlays().removeIf(o -> o instanceof Polyline || o instanceof Marker);
//
//        for (int i = 0; i < geoPoints.size(); i++) {
//            Marker marker = new Marker(mapView);
//            marker.setPosition(geoPoints.get(i));
//            marker.setTitle(placeNames.get(i));
//            mapView.getOverlays().add(marker);
//        }
//
//        requestRouteFromOSRM(geoPoints);
//    }
//
//    private void requestRouteFromOSRM(List<GeoPoint> geoPoints) {
//        StringBuilder coordsBuilder = new StringBuilder();
//        for (GeoPoint pt : geoPoints) {
//            coordsBuilder.append(pt.getLongitude()).append(",").append(pt.getLatitude()).append(";");
//        }
//        if (coordsBuilder.length() > 0) coordsBuilder.setLength(coordsBuilder.length() - 1);
//
//        String url = "http://router.project-osrm.org/route/v1/driving/" + coordsBuilder + "?overview=full&geometries=geojson";
//
//        Request request = new Request.Builder().url(url).build();
//        httpClient.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                Log.e("OSRM", "❌ Запрос не удался: " + e.getMessage());
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if (!response.isSuccessful()) {
//                    Log.e("OSRM", "❌ Сервер OSRM вернул ошибку");
//                    return;
//                }
//
//                try {
//                    JSONObject json = new JSONObject(response.body().string());
//                    JSONObject route = json.getJSONArray("routes").getJSONObject(0);
//                    JSONArray coords = route.getJSONObject("geometry").getJSONArray("coordinates");
//
//                    List<GeoPoint> routePoints = new ArrayList<>();
//                    for (int i = 0; i < coords.length(); i++) {
//                        JSONArray coord = coords.getJSONArray(i);
//                        double lon = coord.getDouble(0);
//                        double lat = coord.getDouble(1);
//                        routePoints.add(new GeoPoint(lat, lon));
//                    }
//
//                    requireActivity().runOnUiThread(() -> {
//                        Polyline routeLine = new Polyline();
//                        routeLine.setColor(Color.BLUE);
//                        routeLine.setWidth(8f);
//                        routeLine.setPoints(routePoints);
//                        mapView.getOverlays().add(routeLine);
//
//                        mapView.invalidate();
//                        Log.d("OSRM", "✅ Маршрут по дорогам построен без промежуточных меток");
//                    });
//
//                } catch (Exception e) {
//                    Log.e("OSRM", "❌ Ошибка парсинга: " + e.getMessage());
//                }
//            }
//        });
//    }
//
//
//
//
//
//    private void requestRouteFromOSRM(List<GeoPoint> geoPoints) {
//        StringBuilder coordsBuilder = new StringBuilder();
//        for (GeoPoint pt : geoPoints) {
//            coordsBuilder.append(pt.getLongitude()).append(",").append(pt.getLatitude()).append(";");
//        }
//        if (coordsBuilder.length() > 0) coordsBuilder.setLength(coordsBuilder.length() - 1);
//
//        String url = "http://router.project-osrm.org/route/v1/driving/" + coordsBuilder + "?overview=full&geometries=geojson";
//
//        Request request = new Request.Builder().url(url).build();
//        httpClient.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                Log.e("OSRM", "❌ Запрос не удался: " + e.getMessage());
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if (!response.isSuccessful()) {
//                    Log.e("OSRM", "❌ Сервер OSRM вернул ошибку");
//                    return;
//                }
//
//                try {
//                    JSONObject json = new JSONObject(response.body().string());
//                    JSONObject route = json.getJSONArray("routes").getJSONObject(0);
//                    JSONArray coords = route.getJSONObject("geometry").getJSONArray("coordinates");
//                    JSONArray legs = route.getJSONArray("legs");
//
//                    List<GeoPoint> routePoints = new ArrayList<>();
//                    for (int i = 0; i < coords.length(); i++) {
//                        JSONArray coord = coords.getJSONArray(i);
//                        double lon = coord.getDouble(0);
//                        double lat = coord.getDouble(1);
//                        routePoints.add(new GeoPoint(lat, lon));
//                    }
//
//                    requireActivity().runOnUiThread(() -> {
//                        Polyline routeLine = new Polyline();
//                        routeLine.setColor(Color.BLUE);
//                        routeLine.setWidth(8f);
//                        routeLine.setPoints(routePoints);
//                        mapView.getOverlays().add(routeLine);
//
//                        // Добавляем промежуточные расстояния
//                        try {
//                            for (int i = 0; i < legs.length(); i++) {
//                                JSONObject leg = legs.getJSONObject(i);
//                                double distance = leg.getDouble("distance") / 1000.0; // в км
//                                double duration = leg.getDouble("duration") / 60.0;  // в минутах
//
//                                GeoPoint from = geoPoints.get(i);
//                                GeoPoint to = geoPoints.get(i + 1);
//
//                                double midLat = (from.getLatitude() + to.getLatitude()) / 2.0;
//                                double midLon = (from.getLongitude() + to.getLongitude()) / 2.0;
//
//                                Marker info = new Marker(mapView);
//                                info.setPosition(new GeoPoint(midLat, midLon));
//                                info.setTitle(String.format("~ %.1f km, ~ %.1f min", distance, duration));
//                                mapView.getOverlays().add(info);
//                            }
//                        } catch (Exception e) {
//                            Log.e("OSRM", "❌ Ошибка при разборе legs: " + e.getMessage());
//                        }
//
//                        mapView.invalidate();
//                        Log.d("OSRM", "✅ Маршрут с расстояниями и временем построен");
//                    });
//
//                } catch (Exception e) {
//                    Log.e("OSRM", "❌ Ошибка парсинга: " + e.getMessage());
//                }
//            }
//        });
//    }

//    private void loadRouteIfExists(String chatId) {
//        if (chatId == null) return;
//        DatabaseReference ref = FirebaseDatabase.getInstance("https://zayn-e9f9c-default-rtdb.europe-west1.firebasedatabase.app")
//                .getReference("chats").child(chatId).child("route");
//        ref.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (!snapshot.exists()) return;
//
//                List<String> placeNames = new ArrayList<>();
//                for (DataSnapshot snap : snapshot.getChildren()) {
//                    String name = snap.getValue(String.class);
//                    if (name != null) placeNames.add(name);
//                }
//
//                List<GeoPoint> geoPoints = new ArrayList<>();
//                for (String name : placeNames) {
//                    GeoPoint pt = knownPoints.get(name);
//                    if (pt != null) geoPoints.add(pt);
//                }
//
//                if (geoPoints.size() < 2) return;
//
//                mapView.getOverlays().removeIf(o -> o instanceof Polyline || o instanceof Marker);
//
//                for (int i = 0; i < geoPoints.size(); i++) {
//                    Marker marker = new Marker(mapView);
//                    marker.setPosition(geoPoints.get(i));
//                    marker.setTitle(placeNames.get(i));
//                    mapView.getOverlays().add(marker);
//                }
//
//                requestRouteFromOSRM(geoPoints);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(getContext(), "Failed to load route", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    public static MapFragment newInstance(String chatId) {
//        MapFragment fragment = new MapFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_CHAT_ID, chatId);
//        fragment.setArguments(args);
//        return fragment;
//    }
//}

//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import org.osmdroid.config.Configuration;
//import org.osmdroid.util.GeoPoint;
//import org.osmdroid.views.MapView;
//import org.osmdroid.views.overlay.ScaleBarOverlay;
//import org.osmdroid.views.overlay.compass.CompassOverlay;
//import android.content.Context;
//import org.osmdroid.views.overlay.Polyline;
//import java.util.*;
//import android.graphics.Color;
//import android.util.Log;
//import android.widget.Toast;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//import org.osmdroid.views.overlay.Marker;
//
//import okhttp3.Call;
//import okhttp3.Callback;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.Response;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//import java.io.IOException;
//
//public class MapFragment extends Fragment {
//    private MapView mapView;
//    private static final String ARG_CHAT_ID = "chat_id";
//    private String currentChatId = null;
//    private final OkHttpClient httpClient = new OkHttpClient();
//
//    private final Map<String, GeoPoint> knownPoints = new HashMap<String, GeoPoint>() {{
//        put("Yerevan", new GeoPoint(40.1792, 44.4991));
//        put("Vanadzor", new GeoPoint(40.8140, 44.4939));
//        put("Gyumri", new GeoPoint(40.7894, 43.8475));
//        put("Dilijan", new GeoPoint(40.7400, 44.8638));
//        put("Sevan", new GeoPoint(40.5556, 44.9536));
//        put("Kapan", new GeoPoint(39.2076, 46.4154));
//        put("Goris", new GeoPoint(39.5111, 46.3383));
//        put("Jermuk", new GeoPoint(39.8417, 45.6736));
//        put("Stepanakert", new GeoPoint(39.8177, 46.7528));
//        put("Tatev Monastery", new GeoPoint(39.4021, 46.2530));
//        put("Noravank", new GeoPoint(39.6847, 45.2328));
//        put("Geghard", new GeoPoint(40.1406, 44.8186));
//        put("Garni Temple", new GeoPoint(40.1190, 44.7251));
//        put("Sevanavank", new GeoPoint(40.5615, 44.9362));
//        put("Haghpat Monastery", new GeoPoint(41.0956, 44.7106));
//        put("Zvartnots Cathedral", new GeoPoint(40.1539, 44.3219));
//        put("Lavash Restaurant (Yerevan)", new GeoPoint(40.1811, 44.5146));
//        put("Dolmama Restaurant (Yerevan)", new GeoPoint(40.1836, 44.5147));
//    }};
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_map, container, false);
//        Context ctx = requireContext().getApplicationContext();
//        Configuration.getInstance().load(ctx, androidx.preference.PreferenceManager.getDefaultSharedPreferences(ctx));
//        mapView = view.findViewById(R.id.map);
//        mapView.setMultiTouchControls(true);
//        mapView.getController().setZoom(7.5);
//        mapView.getController().setCenter(new GeoPoint(40.1792, 44.4991));
//        mapView.setMinZoomLevel(3.0);
//        mapView.setMaxZoomLevel(18.0);
//
//        CompassOverlay compassOverlay = new CompassOverlay(ctx, mapView);
//        compassOverlay.enableCompass();
//        mapView.getOverlays().add(compassOverlay);
//
//        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(mapView);
//        scaleBarOverlay.setAlignBottom(true);
//        mapView.getOverlays().add(scaleBarOverlay);
//
//        if (getArguments() != null && getArguments().containsKey(ARG_CHAT_ID)) {
//            currentChatId = getArguments().getString(ARG_CHAT_ID);
//            loadRouteIfExists(currentChatId);
//        }
//
//        return view;
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        if (mapView != null) mapView.onResume();
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        if (mapView != null) mapView.onPause();
//    }
//
//    public void drawRouteFromAiResponse(String aiResponse, String chatId) {
//        if (mapView == null) return;
//        currentChatId = chatId;
//
//        String routeLine = null;
//        for (String line : aiResponse.split("\n")) {
//            if (line.trim().startsWith("1. Travel Route:") || line.trim().startsWith("Travel Route:")) {
//                routeLine = line.substring(line.indexOf(":") + 1).trim();
//                break;
//            }
//        }
//
//        if (routeLine == null || routeLine.isEmpty()) {
//            Log.e("MapDebug", "❌ Не найден Travel Route");
//            return;
//        }
//
//        routeLine = routeLine.replace("[", "").replace("]", "").replace("'", "").replace("\"", "");
//        String[] parts = routeLine.split(",");
//        List<String> placeNames = new ArrayList<>();
//        for (String part : parts) {
//            String name = part.trim();
//            if (!name.isEmpty()) placeNames.add(name);
//        }
//
//        List<GeoPoint> geoPoints = new ArrayList<>();
//        for (String name : placeNames) {
//            GeoPoint pt = knownPoints.get(name);
//            if (pt != null) geoPoints.add(pt);
//        }
//
//        if (geoPoints.size() < 2) {
//            Log.e("MapDebug", "❌ Недостаточно точек для маршрута");
//            return;
//        }
//
//        // Сохраняем в Firebase
//        DatabaseReference ref = FirebaseDatabase.getInstance("https://zayn-e9f9c-default-rtdb.europe-west1.firebasedatabase.app")
//                .getReference("chats").child(chatId).child("route");
//        ref.setValue(placeNames);
//
//        // Чистим карту
//        mapView.getOverlays().removeIf(o -> o instanceof Polyline || o instanceof Marker);
//
//        // Добавляем маркеры
//        for (int i = 0; i < geoPoints.size(); i++) {
//            Marker marker = new Marker(mapView);
//            marker.setPosition(geoPoints.get(i));
//            marker.setTitle(placeNames.get(i));
//            mapView.getOverlays().add(marker);
//        }
//
//        // Получаем маршрут через OSRM
//        requestRouteFromOSRM(geoPoints);
//    }
//
//    private void requestRouteFromOSRM(List<GeoPoint> geoPoints) {
//        StringBuilder coordsBuilder = new StringBuilder();
//        for (GeoPoint pt : geoPoints) {
//            coordsBuilder.append(pt.getLongitude()).append(",").append(pt.getLatitude()).append(";");
//        }
//        if (coordsBuilder.length() > 0) coordsBuilder.setLength(coordsBuilder.length() - 1);
//
//        String url = "http://router.project-osrm.org/route/v1/driving/" + coordsBuilder + "?overview=full&geometries=geojson";
//
//        Request request = new Request.Builder().url(url).build();
//        httpClient.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                Log.e("OSRM", "❌ Запрос не удался: " + e.getMessage());
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if (!response.isSuccessful()) {
//                    Log.e("OSRM", "❌ Сервер OSRM вернул ошибку");
//                    return;
//                }
//
//                try {
//                    JSONObject json = new JSONObject(response.body().string());
//                    JSONArray coords = json.getJSONArray("routes")
//                            .getJSONObject(0)
//                            .getJSONObject("geometry")
//                            .getJSONArray("coordinates");
//
//                    List<GeoPoint> routePoints = new ArrayList<>();
//                    for (int i = 0; i < coords.length(); i++) {
//                        JSONArray coord = coords.getJSONArray(i);
//                        double lon = coord.getDouble(0);
//                        double lat = coord.getDouble(1);
//                        routePoints.add(new GeoPoint(lat, lon));
//                    }
//
//                    requireActivity().runOnUiThread(() -> {
//                        Polyline routeLine = new Polyline();
//                        routeLine.setColor(Color.BLUE);
//                        routeLine.setWidth(8f);
//                        routeLine.setPoints(routePoints);
//                        mapView.getOverlays().add(routeLine);
//                        mapView.invalidate();
//                        Log.d("OSRM", "✅ Маршрут по дорогам построен");
//                    });
//
//                } catch (Exception e) {
//                    Log.e("OSRM", "❌ Ошибка парсинга: " + e.getMessage());
//                }
//            }
//        });
//    }
//
//    private void loadRouteIfExists(String chatId) {
//        if (chatId == null) return;
//        DatabaseReference ref = FirebaseDatabase.getInstance("https://zayn-e9f9c-default-rtdb.europe-west1.firebasedatabase.app")
//                .getReference("chats").child(chatId).child("route");
//        ref.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (!snapshot.exists()) return;
//
//                List<String> placeNames = new ArrayList<>();
//                for (DataSnapshot snap : snapshot.getChildren()) {
//                    String name = snap.getValue(String.class);
//                    if (name != null) placeNames.add(name);
//                }
//
//                List<GeoPoint> geoPoints = new ArrayList<>();
//                for (String name : placeNames) {
//                    GeoPoint pt = knownPoints.get(name);
//                    if (pt != null) geoPoints.add(pt);
//                }
//
//                if (geoPoints.size() < 2) return;
//
//                mapView.getOverlays().removeIf(o -> o instanceof Polyline || o instanceof Marker);
//
//                for (int i = 0; i < geoPoints.size(); i++) {
//                    Marker marker = new Marker(mapView);
//                    marker.setPosition(geoPoints.get(i));
//                    marker.setTitle(placeNames.get(i));
//                    mapView.getOverlays().add(marker);
//                }
//
//                requestRouteFromOSRM(geoPoints);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(getContext(), "Failed to load route", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    public static MapFragment newInstance(String chatId) {
//        MapFragment fragment = new MapFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_CHAT_ID, chatId);
//        fragment.setArguments(args);
//        return fragment;
//    }
//}