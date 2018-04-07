package fadep.com.edu.saveenviromentdata.dao;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import fadep.com.edu.saveenviromentdata.converter.Converters;
import fadep.com.edu.saveenviromentdata.model.Info;
import fadep.com.edu.saveenviromentdata.model.Place;

@Database(entities = {Place.class, Info.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract PlaceDao placeDao();
    public abstract InfoDao infoDao();
}