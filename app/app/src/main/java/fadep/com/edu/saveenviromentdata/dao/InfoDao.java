package fadep.com.edu.saveenviromentdata.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;
import java.util.Locale;

import fadep.com.edu.saveenviromentdata.model.Info;

@Dao
public interface InfoDao {
    @Insert
    public void create(Info info);
    @Query("SELECT * FROM INFO")
    public List<Info> read();
    @Update
    public void update(Info info);
    @Delete
    public void delete(Info info);
}