package fadep.com.edu.saveenviromentdata;

import android.arch.persistence.room.Room;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.util.List;

import fadep.com.edu.saveenviromentdata.dao.AppDatabase;
import fadep.com.edu.saveenviromentdata.model.Info;

public class DashBoardActivity extends AppCompatActivity {

    private AppDatabase appDatabase;
    private List<Info> infos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "saveenviromentdata").build();
        getAllInfos();
    }


    /**
     * Busca todas os locais salvos no banco do celular.
     */
    public void getAllInfos() {
        Thread rn = new Thread(new Runnable() {
            @Override
            public void run() {
                infos = appDatabase.infoDao().read();
                EditText editText = (EditText) findViewById(R.id.editText);
                editText.setText(infos.toString());
            }
        });

        try{
            rn.start();
            while(rn.getState() != Thread.State.TERMINATED);
        } catch (Exception e) {
            Log.i("Atualiza Places","Erro" );
        }
    }

}
