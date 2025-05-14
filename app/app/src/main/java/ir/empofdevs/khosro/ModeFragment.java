package ir.empofdevs.khosro;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ModeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ModeFragment extends Fragment {

    private GridView gridView;

    public ModeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mode, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        gridView = (GridView) view.findViewById(R.id.mode_grid_view);

        ArrayList<OptionsModel> list = new ArrayList<>();
        list.add(new OptionsModel(R.drawable.baseline_highlight_off_24, "OFF", "/led/color/off", new HashMap<String, Class<?>>()));
        list.add(new OptionsModel(R.drawable.palette, "Constant", "/led/color/constant", new HashMap<String, Class<?>>() {{
            put("Color", Color.class);
        }}));
        list.add(new OptionsModel(R.drawable.rainbow, "Rainbow", "/led/color/rainbow", new HashMap<String, Class<?>>() {{
            put("N", Integer.class);
        }}));
        list.add(new OptionsModel(R.drawable.baseline_waves_24, "Digital Wave", "/led/color/dw", new HashMap<String, Class<?>>() {{
            put("N", Integer.class);
            put("Duration", Integer.class);
        }}));
        list.add(new OptionsModel(R.drawable.baseline_recycling_24, "Portion", "/led/color/portion", new HashMap<String, Class<?>>(){{
            put("N", Integer.class);
            put("Duration", Integer.class);
        }}));
        list.add(new OptionsModel(R.drawable.pie_chart, "Color Wave", "/led/color/cw", new HashMap<String, Class<?>>(){{
            put("N", Integer.class);
            put("Duration", Integer.class);
            put("RTL", Boolean.class);
        }}));

        OptionsViewAdapter adapter = new OptionsViewAdapter(view.getContext(), list);
        gridView.setAdapter(adapter);

    }
}