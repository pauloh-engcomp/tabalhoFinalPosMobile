package fadep.com.edu.saveenviromentdata.model;

/**
 * Created by lucas on 04/04/18.
 */

@Table(name = "Place")

public class Place extends Model {

    @Column(name = "id", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public long id;

    @Column(name = "name")
    public String name;

    @Column(name = "lat")
    public double lat;

    @Column(name = "lng")
    public double lng;

    public List<Info> informacoes() {
        return getMany(Info.class, "Info");
    }

    public Place(){
        super();
    }

    public Place(long id, String name, double lat, double lng) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
    }
}


