package fadep.com.edu.saveenviromentdata;

import android.Manifest;
import android.app.Dialog;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import fadep.com.edu.saveenviromentdata.dao.AppDatabase;
import fadep.com.edu.saveenviromentdata.model.Place;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private AppDatabase appDatabase;
    private Place place = new Place();
    private Location local;
    private RecyclerView recyclerViewRoom;
    private List<Place> places = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://localhost:1337/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        //this.locationService = retrofit.create(LocalizacaoService.class);


        /*LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if ( ! lm.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                showAlert();
            }
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, this);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);*/
        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if ( ! mLocationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                showAlert();
                Toast.makeText(getBaseContext(), "Aqui", Toast.LENGTH_LONG).show();
            } else Toast.makeText(getBaseContext(), "Ali", Toast.LENGTH_LONG).show();
        } else {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000,100, mLocationListener);
        }

        appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "saveenviromentdata").build();
        recyclerViewRoom = (RecyclerView) findViewById(R.id.listPlaces);
        recyclerViewRoom.setHasFixedSize(true);

        LinearLayoutManager mLayoutManagerRoom = new LinearLayoutManager(getBaseContext());
        recyclerViewRoom.setLayoutManager(mLayoutManagerRoom);

        MyAdapterRoom mAdapterRoom = new MyAdapterRoom(new ArrayList<Place>());
        recyclerViewRoom.setAdapter(mAdapterRoom);

        getAllPlaces();
    }

    /**
     * Abre o dialog responsável por ler as informações para um novo local
     */
    private void openDialog() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_place, null);
        mBuilder.setView(mView);

        final AlertDialog dialog = mBuilder.create();
        final EditText name = (EditText) mView.findViewById(R.id.etName);
        Button btnSave = (Button) mView.findViewById(R.id.btnSave);
        Button btnCancel = (Button) mView.findViewById(R.id.btnCancel);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!name.getText().toString().isEmpty()) {
                    place.setName(name.getText().toString());
                    save(place);
                    dialog.cancel();
                } else {
                    Toast.makeText(MainActivity.this, R.string.error_form_invalid, Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        dialog.show();
    }

    /**
     * Salva um novo local na memória do celular e na API rest, caso possua conexão com a internet.
     */
    private void save(final Place place) {
        try {
            // Verificar se o ponto está preenchido...
            if(this.local != null){
                place.setLat(this.local.getLatitude());
                place.setLng(this.local.getLongitude());
            } else {

                place.setLat(0L);
                place.setLng(0L);
            }

            Thread rn = new Thread(new Runnable() {
                @Override
                public void run() {
                    appDatabase.placeDao().create(place);
                }
            });
            rn.start();
            getAllPlaces();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, R.string.success_save, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Busca todas os locais salvos no banco do celular.
     */
    public void getAllPlaces() {
        Thread rn = new Thread(new Runnable() {
            @Override
            public void run() {
                places = appDatabase.placeDao().read();
            }
        });

        try{
            rn.start();
            while(rn.getState() != Thread.State.TERMINATED);
            recyclerViewRoom.setAdapter(new MyAdapterRoom(places));
        } catch (Exception e) {
            Log.i("Atualiza Places","Erro" );
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.places) {
            System.out.print("Places");
        } else if (id == R.id.dashboard) {
            System.out.print("DashBoard");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            local = location;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    public void showAlert() {
        AlertDialog.Builder alerta = new AlertDialog.Builder( this );
        alerta.setTitle( "Atenção" );
        alerta.setMessage( "GPS não habilitado. Deseja Habilitar?" );
        alerta.setCancelable( false );
        alerta.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS );
                startActivity( i );
            }
        } );
        alerta.setNegativeButton( "Cancelar", null );
        alerta.show();
    }
}
