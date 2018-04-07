package fadep.com.edu.saveenviromentdata.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import java.util.Date;
import static android.arch.persistence.room.ForeignKey.CASCADE;

/**
 * Created by lucas on 04/04/18.
 */
@Entity(foreignKeys = @ForeignKey(entity = Place.class,
        parentColumns = "id",
        childColumns = "idPlace",
        onDelete = CASCADE
))
public class Info {
    @PrimaryKey(autoGenerate = true)
    public long id;

    private String temp;

    private String lum;

    private Date date;

    private long idPlace;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public String getLum() { return lum; }

    public void setLum(String lum) { this.lum = lum; }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getIdPlace() {
        return idPlace;
    }

    public void setIdPlace(long idPlace) {
        this.idPlace = idPlace;
    }

    public Info(){
        super();
    }

    public Info(long id, String temp, String humi, Date date, Long idPlace) {
        this.id = id;
        this.temp = temp;
        this.lum = humi;
        this.date = date;
        this.idPlace = idPlace;
    }
}


