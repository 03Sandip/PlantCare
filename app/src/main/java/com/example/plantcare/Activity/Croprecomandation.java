package com.example.plantcare.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.plantcare.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Croprecomandation extends AppCompatActivity {
    EditText N, P, K, temperature, humidity, ph, rainfall;
    Button predict;
    TextView result;
    String url = "https://crop-recomandation-c72c.onrender.com/predict"; // Corrected URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_croprecomandation);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        N = findViewById(R.id.n_ratio);
        P = findViewById(R.id.p_ratio);
        K = findViewById(R.id.k_ratio);
        temperature = findViewById(R.id.temprature);
        humidity = findViewById(R.id.humidity);
        ph = findViewById(R.id.ph_ratio);
        rainfall = findViewById(R.id.rainfall);
        predict = findViewById(R.id.mDetectButton); // Initialize your predict button
        result = findViewById(R.id.result); // Initialize the result TextView

        predict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    // Log and display the raw response for debugging
                                    Toast.makeText(Croprecomandation.this, "Response: " + response, Toast.LENGTH_LONG).show();

                                    JSONObject jsonObject = new JSONObject(response);
                                    String data = jsonObject.optString("crop", "Unknown crop"); // Use optString to avoid errors

                                    // Update the result TextView with the response data
                                    result.setText("Recommended crop: " + data);

                                } catch (JSONException e) {
                                    Toast.makeText(Croprecomandation.this, "JSON error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(Croprecomandation.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("N", N.getText().toString());
                        params.put("P", P.getText().toString());
                        params.put("K", K.getText().toString());
                        params.put("temperature", temperature.getText().toString());
                        params.put("humidity", humidity.getText().toString());
                        params.put("ph", ph.getText().toString());
                        params.put("rainfall", rainfall.getText().toString());

                        return params;
                    }
                };

                RequestQueue queue = Volley.newRequestQueue(Croprecomandation.this);
                queue.add(stringRequest);
            }
        });
    }
}
