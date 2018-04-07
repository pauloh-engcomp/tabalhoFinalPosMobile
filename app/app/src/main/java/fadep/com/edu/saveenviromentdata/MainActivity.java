package fadep.com.edu.saveenviromentdata;

import android.Manifest;
import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private UsbService usbService;
    private MyHandler mHandler;
    private UsbManager usbManager;

    private ApiRestService apiRestService;
    private final String TAG = this.getClass().getSimpleName();

    private String temperatura;
    private String luminosidade;

    private AlertDialog dialog;

    MyAdapterRoom.OnItemClickListener listenerClick = new MyAdapterRoom.OnItemClickListener() {
        @Override public void onItemClick(Place plac) {
            if(usbManager.getDeviceList().isEmpty())
                Toast.makeText(getApplicationContext(), "USB desconectada", Toast.LENGTH_LONG).show();
            else {
                place = plac;
                usbService.write("s".getBytes());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new MyHandler(this);
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

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
            } else Toast.makeText(getBaseContext(), "GPS desligado ou algo do tipo...", Toast.LENGTH_LONG).show();
        } else {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 600,100, mLocationListener);
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

        dialog = mBuilder.create();
        final TextView txtTemperatura = (TextView) mView.findViewById(R.id.tvTemp);
        final TextView txtLuminosidade = (TextView) mView.findViewById(R.id.tvLum);

        txtTemperatura.setText(temperatura);
        txtLuminosidade.setText(luminosidade);

        Button btnSave = (Button) mView.findViewById(R.id.btnSave);
        Button btnCancel = (Button) mView.findViewById(R.id.btnCancel);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveInfo(place, temperatura, luminosidade);
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

    @Override
    public void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    public void onPause() {
        super.onPause();
        unbindService(usbConnection);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mUsbReceiver);
        stopService(new Intent(this, UsbService.class));
    }



    /*
     * Notifications from UsbService will be received here.
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Pronta", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB desconectada", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
                default: Toast.makeText(context, "Alguma coisa...", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    private static class MyHandler extends Handler {
        private final MainActivity mActivity;
        private Pattern modeloPonto = Pattern.compile("(?:\\{\\\"temp\\\"\\: )([.0-9]*)(?: \\, \\\"lum\\\"\\: )([0-9]*)(?: \\})(.*)");
        private Pattern modeloErro = Pattern.compile("([^\\}]*\\})(.*)");
        private Matcher m;
        private String buffer = "";

        public MyHandler(MainActivity activity) {
            this.mActivity = activity;
            this.buffer = "";
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    buffer = (String) msg.obj;
                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity, "CTS_CHANGE",Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity, "DSR_CHANGE",Toast.LENGTH_LONG).show();
                    break;
                case UsbService.SYNC_READ:
                    //timer.schedule(new limpaBuffer(), 1000);
                    buffer += (String) msg.obj;

                    // Verificar o padrão
                    m = modeloPonto.matcher(buffer);
                    // Enquanto tiver ponto a ser colocado...

                    while(m.matches()){
                        mActivity.inserirPonto(Double.valueOf(m.group(1)), Integer.valueOf(m.group(2)));
                        buffer = ("".equals(m.group(3)) ? "" : m.group(3));
                        m = modeloPonto.matcher(buffer);
                    }

                    // Elimina os erros
                    m = modeloErro.matcher(buffer);
                    if(m.matches())
                        buffer = ("".equals(m.group(2)) ? "" : "("+m.group(2));
                    break;
            }
        }
    }

    public void inserirPonto(Double temp, Integer lum) {
        usbService.write("t".getBytes());
        if((dialog == null) || (!dialog.isShowing())) {
            temperatura = String.valueOf(temp);
            luminosidade = String.valueOf(lum);
            openDialogGetInfos(place);
        } else Toast.makeText(this, "Errou...", Toast.LENGTH_LONG).show();
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
