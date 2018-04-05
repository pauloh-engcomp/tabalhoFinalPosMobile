package fadep.com.edu.saveenviromentdata.model;

/**
 * Created by lucas on 04/04/18.
 */

public class Place {
    private Long id;
    private String nome;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Place)) return false;

        Place place = (Place) o;

        if (getId() != null ? !getId().equals(place.getId()) : place.getId() != null) return false;
        return nome != null ? nome.equals(place.nome) : place.nome == null;
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (nome != null ? nome.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return nome;
    }
}


