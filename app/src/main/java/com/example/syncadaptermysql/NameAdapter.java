package com.example.syncadaptermysql;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class NameAdapter extends ArrayAdapter<Name> {

    //Para almacenar todos los nombres
    private List<Name> names;
    private Context context;

    //Constructor de la clase
    public NameAdapter(Context context, int resource, List<Name> names){
        super(context, resource, names);
        this.context = context;
        this.names = names;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //Para obtener los items
        View listViewItem = inflater.inflate(R.layout.names, null, true);
        TextView textViewName = (TextView) listViewItem.findViewById(R.id.textViewName);
        ImageView imageViewStatus = (ImageView) listViewItem.findViewById(R.id.imageViewStatus);

        //Obteniendo el nombre y teléfono actual
        Name name = names.get(position);

        //Colocando el nombre y teléfono en el textView
        textViewName.setText(name.getName()+ " - "+name.getPhone());

        //Para asignar el icono del registro de acuerdo a si está sincronizado o no
        if (name.getStatus() == 0 )
            imageViewStatus.setBackgroundResource(R.drawable.stopwatch);
        else
            imageViewStatus.setBackgroundResource(R.drawable.success);
        return listViewItem;
    }
}
