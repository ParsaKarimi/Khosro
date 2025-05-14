package ir.empofdevs.khosro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OptionActivity extends AppCompatActivity {

    private LinearLayout parameters_wrapper;
    private OkHttpClient client = new OkHttpClient();
    private String url;
    private JSONObject jsonObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

        parameters_wrapper = findViewById(R.id.parameters_wrapper);
        Bundle extras = getIntent().getExtras();

        url = extras.getString("url");
        jsonObject = loadConfig(this);

        ((TextView)(findViewById(R.id.section_name))).setText(extras.getString("name"));
        HashMap<String, Class<?>> parameters = (HashMap<String, Class<?>>) extras.get("parameters");
        for (String parameter: parameters.keySet()) {
            try {
                addParameter(new Pair<>(parameter, parameters.get(parameter)));
            } catch (JSONException e) {
                Log.d(e.toString(), "config");
            }
        }
    }

    private void addParameter(Pair<String, Class<?>> parameter) throws JSONException {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        TextView textView = new TextView(this);
        textView.setText(parameter.first);
        textView.setTextSize(18);
        textView.setTypeface(ResourcesCompat.getFont(this, R.font.roboto_bold));
        textView.setPadding(20, 20, 20, 20);
        textView.setWidth(250);
        layout.addView(textView);

        if (parameter.second == Color.class) {
            EditText field = new EditText(this);
            field.setTextSize(18);
            field.setTypeface(ResourcesCompat.getFont(this, R.font.roboto_bold));
            field.setPadding(20, 20, 20, 20);
            field.setText(jsonObject.getJSONObject(url).getString(parameter.first));
            field.setWidth(250);
            field.setFilters(new InputFilter[]{
                    new InputFilter.LengthFilter(8)
            });
            field.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus && !field.getText().toString().startsWith("0x")) {
                    field.setText("0x");
                    field.setSelection(2); // Move cursor after "0x"
                }
            });
            layout.addView(field);
        } else if (parameter.second == Integer.class) {
            EditText field = new EditText(this);
            field.setTextSize(18);
            field.setTypeface(ResourcesCompat.getFont(this, R.font.roboto_bold));
            field.setPadding(20, 20, 20, 20);
            field.setText(jsonObject.getJSONObject(url).getString(parameter.first));
            field.setWidth(400);
            field.setFilters(new InputFilter[]{
                    new InputFilter() {
                        @Override
                        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                            if (source.toString().matches("[0-9]+")) {
                                return source;
                            }
                            return "";
                        }
                    }
            });
            layout.addView(field);
        } else if (parameter.second == Boolean.class) {
            Switch field = new Switch(this);
            field.setThumbTintList(ContextCompat.getColorStateList(this, R.color.switch_thumb));
            field.setTrackTintList(ContextCompat.getColorStateList(this, R.color.switch_track));
            field.setChecked(jsonObject.getJSONObject(url).getString(parameter.first) == "true");
            layout.addView(field);
        }

        parameters_wrapper.addView(layout);
    }

    public void submitClicked(View v) throws JSONException {

        FormBody.Builder body = new FormBody.Builder();
        for (int i = 0; i < parameters_wrapper.getChildCount(); i++) {
            TextView textView = ((TextView)((LinearLayout)parameters_wrapper.getChildAt(i)).getChildAt(0));
            View field = ((LinearLayout)parameters_wrapper.getChildAt(i)).getChildAt(1);
            if (field instanceof Switch) {
                jsonObject.getJSONObject(url).put(textView.getText().toString(), ((Switch)field).isChecked()?"true":"false");
                body.add(textView.getText().toString().toLowerCase(), ((Switch)field).isChecked()?"true":"false");
            } else {
                jsonObject.getJSONObject(url).put(textView.getText().toString(), ((EditText)field).getText().toString());
                body.add(textView.getText().toString().toLowerCase(), ((EditText)field).getText().toString());
            }
        }


        Request request = new Request.Builder()
                .url(getResources().getString(R.string.domain) + this.url)
                .post(body.build())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d("POST_RESPONSE", responseData);
                } else {
                    Log.e("POST_ERROR", "Request failed: " + response.code());
                }
            }
        });

        saveConfig(this, jsonObject);
        finish();
    }

    public void cancelClicked(View v) {
        finish();
    }


    public void saveConfig(Context context, JSONObject jsonObject) {
        String filename = "config.json";
        try {
            FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(jsonObject.toString().getBytes(StandardCharsets.UTF_8));
            fos.close();
            Log.d("config", "File written successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public JSONObject loadConfig(Context context) {
        String filename = "config.json";
        StringBuilder json = new StringBuilder();

        try {
            InputStream fis;
            if (new File(context.getFilesDir(), filename).exists()) fis = context.openFileInput(filename);
            else fis = context.getAssets().open(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));

            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }

            reader.close();
            return new JSONObject(json.toString());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}