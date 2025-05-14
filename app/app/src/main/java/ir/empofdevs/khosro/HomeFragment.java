package ir.empofdevs.khosro;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import saman.zamani.persiandate.PersianDate;
import saman.zamani.persiandate.PersianDateFormat;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    private static final OkHttpClient client = new OkHttpClient();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private PersianDateFormat pdformater = new PersianDateFormat("m/d H:i:s");
    private Dialog dialog;
    private Calendar current_date;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.layout_loading_screen);
        dialog.setCancelable(false); // Prevent closing by clicking outside
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        fetchDataInBackground();
        ((SwipeRefreshLayout)view.findViewById(R.id.refresh)).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ((SwipeRefreshLayout)view.findViewById(R.id.refresh)).setRefreshing(false);
                fetchDataInBackground();
            }
        });
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run(){
                if (current_date == null) return;
                current_date.add(Calendar.SECOND, 1);
                requireActivity().runOnUiThread(()->((TextView)requireView().findViewById(R.id.CurrentTime)).setText(pdformater.format(new PersianDate(current_date.getTime()))));
            }
        },0,1000);
    }

    private void fetchDataInBackground() {
        dialog.show();
        executor.execute(() -> {
            while (true) {
                try {
                    client.newCall(new Request.Builder().url(getResources().getString(R.string.domain)+"/clock").post(new FormBody.Builder().add("time", Long.toString(System.currentTimeMillis() / 1000)).build()).build()).execute();
                } catch (IOException e) { }
                String current_time = fetchData(getResources().getString(R.string.domain)+"/clock");
                String watering_time = fetchData(getResources().getString(R.string.domain)+"/water");
                String speaker = fetchData(getResources().getString(R.string.domain)+"/speaker");
                if (current_time == null || watering_time == null || speaker==null) {
                    try { Thread.sleep(5000); } catch (InterruptedException e) { }
                    continue;
                };

                current_time = current_time.substring(8, current_time.length()-2);
                watering_time = watering_time.substring(8, watering_time.length()-2);

                Calendar current_date = Calendar.getInstance();
                current_date.setTime(new Date(Long.parseLong(current_time)*1000L));
                this.current_date = current_date;

                Calendar watering_date = Calendar.getInstance();
                watering_date.setTime(new Date(Long.parseLong(watering_time)*1000L));

                requireActivity().runOnUiThread(() -> {
                    ((Switch)requireView().findViewById(R.id.speaker)).setChecked(speaker.equals("{ state: true }"));
                    ((Switch)requireView().findViewById(R.id.speaker)).setOnCheckedChangeListener((compoundButton, b) -> speakerClicked((Switch) compoundButton, b));

                    ((TextView)requireView().findViewById(R.id.CurrentTime)).setText(pdformater.format(new PersianDate(current_date.getTime())));
                    ((TextView)requireView().findViewById(R.id.NextWatering)).setText(pdformater.format(new PersianDate(watering_date.getTime())));

                    dialog.hide();
                });
                break;
            }
        });
    }
    private String fetchData(String url) {
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return response.body().string();
        } catch (IOException e) {
            Log.e("state", e.toString());
            return null;
        }
    }

    public void speakerClicked(Switch v, Boolean state) {
        dialog.show();

        Request request = new Request.Builder()
                .url(getResources().getString(R.string.domain) + "/speaker")
                .post(new FormBody.Builder().build())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                v.setChecked(!state);
                getActivity().runOnUiThread(dialog::hide);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) v.setChecked(!state);
                getActivity().runOnUiThread(dialog::hide);
            }
        });
    }

}