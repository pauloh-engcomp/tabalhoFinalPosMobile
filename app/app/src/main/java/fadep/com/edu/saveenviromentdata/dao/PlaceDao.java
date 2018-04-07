package fadep.com.edu.saveenviromentdata.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import fadep.com.edu.saveenviromentdata.model.Place;

@Dao
public interface PlaceDao {
    @Insert
    public void create(Place place);
    @Query("SELECT * FROM PLACE")
    public List<Place> read();
    @Update
    public void update(Place place);
    @Delete
    public void delete(Place place);
}