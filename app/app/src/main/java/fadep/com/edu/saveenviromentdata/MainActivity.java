package fadep.com.edu.saveenviromentdata;

import android.Manifest;
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
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fadep.com.edu.saveenviromentdata.dao.AppDatabase;
import fadep.com.edu.saveenviromentdata.model.Info;
import fadep.com.edu.saveenviromentdata.model.Place;
import fadep.com.edu.saveenviromentdata.service.ApiRestService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private AppDatabase appDatabase;
    private Place place = new Place();
    private Location local;
    private RecyclerView recyclerViewRoom;
    private List<Place> places = new ArrayList<>();
    private ApiRestService apiRestService;
    private final String TAG = this.getClass().getSimpleName();

    MyAdapterRoom.OnItemClickListener listenerClick = new MyAdapterRoom.OnItemClickListener() {
        @Override public void onItemClick(Place place) {
            openDialogGetInfos(place);
        }
    };

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
        this.apiRestService = retrofit.create(ApiRestService.class);


        LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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
        recyclerViewRoom.setAdapter(new MyAdapterRoom(places, listenerClick));

        getAllPlaces();

    }

    private void openDialogGetInfos(final Place place) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_get_infos, null);
        mBuilder.setView(mView);

        final AlertDialog dialog = mBuilder.create();
        final TextView temperatura = (TextView) mView.findViewById(R.id.tvTemp);
        final TextView luminosidade = (TextView) mView.findViewById(R.id.tvLum);
        final String temp = "15";
        final String lum = "10";
        temperatura.setText(temp);
        luminosidade.setText(lum);

        Button btnSave = (Button) mView.findViewById(R.id.btnSave);
        Button btnCancel = (Button) mView.findViewById(R.id.btnCancel);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveInfo(place, temp, lum);
                Toast.makeText(MainActivity.this, R.string.success_save, Toast.LENGTH_SHORT).show();
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

    private void saveInfo(Place place, String temperatura, String luminosidade) {
        try {
            final Info info = new Info();
            info.setDate(new Date());
            info.setIdPlace(place.getId());
            info.setLum(luminosidade);
            info.setTemp(temperatura);
            Thread rn = new Thread(new Runnable() {
                @Override
                public void run() {
                    appDatabase.infoDao().create(info);
                    createInfoOnServer(info);
                }
            });
            rn.start();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, R.string.save_error, Toast.LENGTH_SHORT).show();
        }
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
                    createPlace(place);
                }
            });
            rn.start();
            getAllPlaces();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, R.string.save_error, Toast.LENGTH_SHORT).show();
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
            recyclerViewRoom.setAdapter(new MyAdapterRoom(places, listenerClick));
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
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.dashboard) {
            Intent intent = new Intent(this, DashBoardActivity.class);
            startActivity(intent);
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


    /**
     * Salva uma informação no servidor.
     * @param info
     */
    private void createInfoOnServer(Info info) {
        Call<Info> call = this.apiRestService.createInfo(info);
        call.enqueue(new Callback<Info>() {
            @Override
            public void onResponse(Call<Info> call, Response<Info> response) {
                //displayInfo(response.body());
                //localizacoes = li
                Log.e(TAG, response.body().toString());
            }

            @Override
            public void onFailure(Call<Info> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Unable to create post" , Toast.LENGTH_LONG).show();
                Log.e(TAG,t.toString());
            }
        });
    }

    /**
     * Busca as informações online.
     */
    private void getAllInfoOnServer() {
        Call<List<Info>> getAllInfosCall = this.apiRestService.getAllinfo();

        getAllInfosCall.enqueue(new Callback<List<Info>>() {
            @Override
            public void onResponse(Call<List<Info>> call, Response<List<Info>> response) {
                //displayInfodisplayInfo(response.body().get(0));
                System.out.println(response.body());
            }

            @Override
            public void onFailure(Call<List<Info>> call, Throwable t) {
                Log.e(TAG, "Error occured while fetching post.");
            }
        });
    }


    /**
     * Salva um local no servidor.
     * @param place
     */
    private void createPlace(Place place) {
        Call<Place> call = this.apiRestService.createPlace(place);
        call.enqueue(new Callback<Place>() {
            @Override
            public void onResponse(Call<Place> call, Response<Place> response) {
                //displayPlace(response.body());
                //localizacoes = li
                Log.e(TAG, response.body().toString());
            }

            @Override
            public void onFailure(Call<Place> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Unable to create post" , Toast.LENGTH_LONG).show();
                Log.e(TAG,t.toString());
            }
        });
    }

    /**
     * Busca as informações online.
     */
    private void getAllPlace() {
        Call<List<Place>> getAllPlacesCall = this.apiRestService.getAllplace();

        getAllPlacesCall.enqueue(new Callback<List<Place>>() {
            @Override
            public void onResponse(Call<List<Place>> call, Response<List<Place>> response) {
                System.out.println(response.body());
            }

            @Override
            public void onFailure(Call<List<Place>> call, Throwable t) {
                Log.e(TAG, "Error occured while fetching post.");
            }
        });
    }
}
