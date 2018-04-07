package fadep.com.edu.saveenviromentdata;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import fadep.com.edu.saveenviromentdata.model.Place;

public class MyAdapterRoom extends RecyclerView.Adapter<MyAdapterRoom.ViewHolder> {
    private List<Place> mDataset;

    public MyAdapterRoom(){
        this.mDataset = new ArrayList<Place>();
    }

    public void add(Place resp){
        this.mDataset.add(resp);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView txtNome;

        public ViewHolder(View v) {
            super(v);
            txtNome = v.findViewById(R.id.txtNome);
        }
    }

    public MyAdapterRoom(List<Place> myDataset) {
        mDataset = myDataset;
    }

    @Override
    public MyAdapterRoom.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lista, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.txtNome.setText(mDataset.get(position).getName());

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}