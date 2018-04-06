package fadep.com.edu.saveenviromentdata.model;

/**
 * Created by lucas on 04/04/18.
 */

@Table(name = "Info")

public class Info extends Model {

    @Column(name = "id", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public long id;

    @Column(name= "temp")
    public String temp;

    @Column(name= "humi")
    public String humi;

    @Column(name= "data")
    public Date date;

    @Column(name = "Place", onUpdate = ForeignKeyAction.CASCADE, onDelete = ForeignKeyAction.CASCADE)
    public Place category;

    public Info(){
        super();
    }

    public Info(long id, String temp, String humi, Date data, Place category) {
        this.id = id;
        this.temp = temp;
        this.humi = humi;
        this.data = data;
        this.category = category;
    }
}


