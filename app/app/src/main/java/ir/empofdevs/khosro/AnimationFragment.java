package ir.empofdevs.khosro;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AnimationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AnimationFragment extends Fragment {

    private GridView gridView;

    public AnimationFragment() {
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
        return inflater.inflate(R.layout.fragment_animation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        gridView = (GridView) view.findViewById(R.id.animation_grid_view);

        ArrayList<OptionsModel> list = new ArrayList<>();
        list.add(new OptionsModel(R.drawable.baseline_highlight_off_24, "OFF", "/led/mode/off", new HashMap<String, Class<?>>()));
        list.add(new OptionsModel(R.drawable.breathe_animation, "Breathe", "/led/mode/breathe", new HashMap<String, Class<?>>() {{
            put("Duration", Integer.class);
        }}));
        list.add(new OptionsModel(R.drawable.baseline_keyboard_double_arrow_right_24, "Fill From Left", "/led/mode/ffl", new HashMap<String, Class<?>>() {{
            put("Duration", Integer.class);
        }}));
        list.add(new OptionsModel(R.drawable.baseline_keyboard_double_arrow_left_24, "Fill From Right", "/led/mode/ffr", new HashMap<String, Class<?>>() {{
            put("Duration", Integer.class);
        }}));
        list.add(new OptionsModel(R.drawable.baseline_compare_arrows_24, "Fill From Sides", "/led/mode/ffb", new HashMap<String, Class<?>>() {{
            put("Duration", Integer.class);
        }}));
        list.add(new OptionsModel(R.drawable.baseline_align_horizontal_center_24, "Fill From Center", "/led/mode/ffc", new HashMap<String, Class<?>>() {{
            put("Duration", Integer.class);
        }}));
        list.add(new OptionsModel(R.drawable.slide, "Slide Over", "/led/mode/so", new HashMap<String, Class<?>>() {{
            put("Duration", Integer.class);
            put("N", Integer.class);
        }}));
        list.add(new OptionsModel(R.drawable.wave_animation, "Wave Over", "/led/mode/wo", new HashMap<String, Class<?>>() {{
            put("Duration", Integer.class);
            put("N", Integer.class);
        }}));
        list.add(new OptionsModel(R.drawable.piece, "Piece By Piece", "/led/mode/pbp", new HashMap<String, Class<?>>() {{
            put("Duration", Integer.class);
            put("RTL", Boolean.class);
        }}));

        OptionsViewAdapter adapter = new OptionsViewAdapter(view.getContext(), list);
        gridView.setAdapter(adapter);

    }
}