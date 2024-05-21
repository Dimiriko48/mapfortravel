package com.example.samsungproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int PICK_IMAGE_REQUEST = 2;
    private static final int FINE_PERMISSION_CODE = 100;
    private GoogleMap mMap;
    private boolean isSatelliteView = false;
    private boolean isAddingMarker = false;
    private FusedLocationProviderClient fusedLocationClient;
    private Uri selectedImageUri;
    private ImageView photoImageView;
    private Marker selectedMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // "Инициализация карты"
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Button addMarkerButton = findViewById(R.id.add_marker_button);
        addMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAddingMarker = true;
                Toast.makeText(MainActivity.this, "Tap on the map to add a marker", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnChangeStyle = findViewById(R.id.btnChangeStyle);
        btnChangeStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSatelliteView) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                } else {
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                }
                isSatelliteView = !isSatelliteView;
            }
        });

        Button deleteMarkerButton = findViewById(R.id.delete_marker_button);
        deleteMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedMarker != null) {
                    showDeleteMarkerConfirmation();
                } else {
                    Toast.makeText(MainActivity.this, "No marker selected to delete", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // "Проверка и запрос разрешений"
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // "Установка начального стиля карты"
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE); // Устанавливаем спутниковый вид по умолчанию

        // "Проверка разрешений"
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return;
        }
        mMap.setMyLocationEnabled(true);

        // "Получение текущей локации и установка метки"
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                        }
                    }
                });

        // "Обработка касания карты для добавления метки"
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                if (isAddingMarker) {
                    isAddingMarker = false;
                    showMarkerDialog(point);
                }
            }
        });

        // "Добавление слушателя щелчка по маркеру для отображения фотографии"
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                selectedMarker = marker; // Сохраняем выбранную метку для удаления
                String tag = (String) marker.getTag();
                if (tag != null) {
                    Uri tagUri = Uri.parse(tag);
                    showPhotoDialog(tagUri);
                }
                showDeleteMarkerToast(); // Показываем тост при выборе метки
                return true;
            }

        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // "Разрешение предоставлено, включаем myLocation layer"
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);

                    // "Получение текущей локации и установка метки"
                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    if (location != null) {
                                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                                    }
                                }
                            });
                }
            } else {
                // "Разрешение не предоставлено, выводим сообщение пользователю"
                Toast.makeText(this, "Location permission is required to show your location on the map.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showMarkerDialog(final LatLng point) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.marker_dialog, null);
        builder.setView(dialogView);

        EditText noteEditText = dialogView.findViewById(R.id.note_edit_text);
        Button addPhotoButton = dialogView.findViewById(R.id.add_photo_button);
        photoImageView = dialogView.findViewById(R.id.photo_image_view);

        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        builder.setPositiveButton("Add marker", (dialog, id) -> {
            String note = noteEditText.getText().toString();
            addMarker(point, note, selectedImageUri);
            selectedImageUri = null; // Сброс выбранного изображения после добавления метки
        });

        builder.setNegativeButton("Cancel", (dialog, id) -> {
            selectedImageUri = null; // Сброс выбранного изображения при отмене
            dialog.cancel();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                photoImageView.setImageBitmap(bitmap);
                photoImageView.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addMarker(LatLng point, String note, Uri imageUri) {
        if (mMap != null) {
            MarkerOptions markerOptions = new MarkerOptions().position(point).title(note);
            if (imageUri != null) {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            } else {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            }
            Marker marker = mMap.addMarker(markerOptions);
            if (marker != null) {
                marker.setTag(imageUri != null ? imageUri.toString() : null);
            }
        }
    }

    private void showPhotoDialog(Uri imageUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.photo_dialog, null);
        builder.setView(dialogView);

        ImageView imageView = dialogView.findViewById(R.id.dialog_photo_image_view);
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            imageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        builder.setPositiveButton("Close", (dialog, id) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDeleteMarkerToast() {
        Toast.makeText(this, "Marker selected.", Toast.LENGTH_SHORT).show();
    }

    private void showDeleteMarkerConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this marker?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (selectedMarker != null) {
                            selectedMarker.remove();
                            selectedMarker = null; // Сброс выбранной метки после удаления
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }
}
