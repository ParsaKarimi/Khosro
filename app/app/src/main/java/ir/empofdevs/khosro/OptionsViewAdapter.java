package ir.empofdevs.khosro;

import static androidx.core.content.ContentProviderCompat.requireContext;
import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class OptionsViewAdapter extends ArrayAdapter<OptionsModel> {

    public OptionsViewAdapter(@NonNull Context context, @NonNull ArrayList<OptionsModel> list) {
        super(context, 0, list);
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_option, parent, false);
        }

        OptionsModel model = getItem(position);

        TextView textView = itemView.findViewById(R.id.option_name);
        ImageView imageView = itemView.findViewById(R.id.option_img);

        if (model != null) {
            textView.setText(model.getName());
            imageView.setImageResource(model.getImage());
        }

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), OptionActivity.class);
                intent.putExtra("name", model.getName());
                intent.putExtra("url", model.getUrl());
                intent.putExtra("parameters", model.getParameters());
                getContext().startActivity(intent);
            }
        });

        return itemView;
    }
}
