package com.engineer.docelu.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.engineer.docelu.Models.Departure;
import com.engineer.docelu.Models.Direction;
import com.engineer.docelu.Models.DirectionGroup;
import com.engineer.docelu.R;
import com.engineer.docelu.Models.StopPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    public String PEKA_URL;
    public String inputBollardName;
    private ArrayList<StopPoint> arrayOfStopPoints = new ArrayList<>();
    private ArrayList<String> arrayOfInputs = new ArrayList<>(5);
    private ArrayList<String> arrayOfHintInputs = new ArrayList<>();
    private ArrayList<String> arrayOfReadyInputs = new ArrayList<>();
    private ArrayList<Departure> arrayOfDepartures = new ArrayList<>();
    private ArrayList<String> arrayOfDirections = new ArrayList<>();
    private ArrayList<DirectionGroup> arrayOfReadyDirections = new ArrayList<>();
    public EditText editText;
    public ListAdapter adapter;
    ListView departuresView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PEKA_URL = getString(R.string.peka_url);

        final EditText bollardName = (EditText) findViewById(R.id.bollard_name);
        final Button eraseText = (Button) findViewById(R.id.erase_text);
        final TextView lastInputs = (TextView) findViewById(R.id.lastInputs);

        eraseText.setVisibility(View.INVISIBLE);

        editText = bollardName;

        departuresView = (ListView) findViewById(R.id.inputs);

        eraseText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bollardName.setText("");
            }
        });

        bollardName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (bollardName.getText().length() <= 2) {
                    lastInputs.setText(getString(R.string.last_search));
                    setAdapter(1);
                }
                if (bollardName.getText().length() > 2) {
                    lastInputs.setText(getString(R.string.search_hint));
                    new ExecuteNetworkOperation("getStopPoints", "{\"pattern\":\"" + bollardName.getText() + "\"}").execute();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (bollardName.getText().length() > 0) {
                    eraseText.setVisibility(View.VISIBLE);
                } else eraseText.setVisibility(View.INVISIBLE);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setColorFilter(Color.parseColor("#ffffff"));
        fab.setRippleColor(Color.parseColor("#ffffff"));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, R.string.downloading_schedule, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                inputBollardName = bollardName.getText().toString();
                new ExecuteNetworkOperation("getBollardsByStopPoint", "{\"name\":\"" + trimEnd(inputBollardName) + "\"}").execute();
            }
        });
        setInputs();
        setAdapter(0);
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences prefs = this.getSharedPreferences(getString(R.string.extras_do_celu), this.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Set<String> set = new HashSet<>();
        set.addAll(arrayOfInputs);
        editor.putStringSet(getString(R.string.extras_input), set);
        editor.commit();
    }

    private void setInputs() {
        SharedPreferences prefs = this.getSharedPreferences(getString(R.string.extras_do_celu),Context.MODE_PRIVATE);
        Set<String> set = prefs.getStringSet(getString(R.string.extras_input), null);
        if(set != null) { arrayOfInputs = new ArrayList<>(set); }
    }

    private void setAdapter(Integer flag) {
        if (flag == 0) {
            //initialize
            arrayOfReadyInputs.addAll(arrayOfInputs);
            adapter = new ListAdapter(MainActivity.this, arrayOfReadyInputs);
            departuresView.setDividerHeight(0);
            departuresView.setAdapter(adapter);

        }
        if (flag == 1) {
            //update arrayOfInputs
            arrayOfReadyInputs.clear();
            arrayOfReadyInputs.addAll(arrayOfInputs);
            adapter.notifyDataSetChanged();
        }
        if (flag == 2) {
            //update arrayOfHintInputs
            arrayOfReadyInputs.clear();
            arrayOfReadyInputs.addAll(arrayOfHintInputs);
            adapter.notifyDataSetChanged();
        }
    }

    public class ListAdapter extends ArrayAdapter<String> {

        public ListAdapter(Context context, ArrayList<String> items) {
            super(context, 0, items);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final String string = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.input_hint_listview, parent, false);
            }

            final TextView input = (TextView) convertView.findViewById(R.id.input);

            input.setText(string);

            input.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editText.setText(string);
                }
            });

            return convertView;
        }
    }

    public String trimEnd( String myString ) {

        for ( int i = myString.length() - 1; i >= 0; --i ) {
            if ( myString.charAt(i) == ' ' ) {
                continue;
            } else {
                myString = myString.substring( 0, ( i + 1 ) );
                break;
            }
        }
        return myString;
    }

    private AlertDialog DirectionDialog(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.set_direction));
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, R.layout.direction_row, R.id.element, arrayOfDirections);
        dialog.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                Log.i("DIALOG TEST", "Dialog - Użyto tablicy: " + arrayOfReadyDirections.get(item).getSymbol());
                new ExecuteNetworkOperation("getTimes", "{\"symbol\":\"" + arrayOfReadyDirections.get(item).getSymbol() + "\"}").execute();
                dialog.dismiss();
                arrayOfReadyDirections.clear();
                arrayOfDirections.clear();
            }
        });
        return dialog.create();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public String getTimeStamp(){
        Long tsLong = System.currentTimeMillis();
        return tsLong.toString();
    }

    private void saveInput(String input){
        input = trimEnd(input);
        if(input != null){
            if(!arrayOfInputs.contains(input)){
                if(arrayOfInputs.size() == 5){
                    arrayOfInputs.set(4, arrayOfInputs.get(3));
                    arrayOfInputs.set(3, arrayOfInputs.get(2));
                    arrayOfInputs.set(2, arrayOfInputs.get(1));
                    arrayOfInputs.set(1, arrayOfInputs.get(0));
                    arrayOfInputs.set(0, input);
                } else {
                    arrayOfInputs.add(input);
                }
            }
        }
    }
    private void showSchedule(){
        Intent i = new Intent(this, ScheduleActivity.class);
        i.putExtra(getString(R.string.extras_departure_array), arrayOfDepartures);
        i.putExtra(getString(R.string.extras_bollard), inputBollardName);
        startActivity(i);
    }

    public class ExecuteNetworkOperation extends AsyncTask<Void, Void, String> {

        public String method, p0;

        public ExecuteNetworkOperation(String var, String var2) {
            super();
            method = var;
            p0 = var2;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!isOnline()){
                Toast.makeText(MainActivity.this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return sendToServer(method, p0);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                if (method == "getStopPoints"){
                    try {
                        JSONObject responseJson = new JSONObject(result);
                        JSONArray responseArray = responseJson.getJSONArray("success");
                        arrayOfStopPoints = getStopPointsFromJson(responseArray);
                        arrayOfHintInputs.clear();
                        for (int i = 0; i < arrayOfStopPoints.size(); i++) {
                            arrayOfHintInputs.add(arrayOfStopPoints.get(i).getName());
                        }
                        setAdapter(2);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                if (method == "getTimes") {
                    try {
                        JSONObject responseJson = new JSONObject(result);
                        JSONArray responseArray = responseJson.getJSONObject("success").getJSONArray("times");
                        arrayOfDepartures = getDeparturesFromJson(responseArray);
                        showSchedule();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (method == "getBollardsByStopPoint") {
                    try {
                        JSONObject responseJson = new JSONObject(result);
                        JSONArray responseArray = responseJson.getJSONObject("success").getJSONArray("bollards");
                        arrayOfDirections = getDirectionFromJson(responseArray);
                        if (!arrayOfDirections.isEmpty()) {
                            saveInput(inputBollardName);
                            AlertDialog directionDialog = DirectionDialog();
                            directionDialog.show();
                        } else {
                            Toast.makeText(MainActivity.this, getString(R.string.stop_point_not_found), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, getString(R.string.stop_point_not_found), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public String sendToServer(String var1, String var2) throws IOException {
        final String method = var1;
        final String p0 = var2;
        String timeStamp = getTimeStamp();
        HttpURLConnection conn = null;
        Integer responseCode;
        String urlParameters = "method=" + URLEncoder.encode(method,"UTF-8") + "&p0=" + URLEncoder.encode(p0, "UTF-8");

        try {
            // create HttpURLConnection
            URL url = new URL(PEKA_URL + timeStamp);
            conn = (HttpURLConnection) url.openConnection();

            // make POST request to the given URL
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Length", "" +
                    Integer.toString(urlParameters.getBytes().length));
            conn.setRequestProperty("Cookie", "JSESSIONID=+FWYRC2+Tz9JLGAjqfxDjPnt.undefined; JSESSIONID=7093FDAEF67212D456793FCC8BD723FF.app1; __utmt=1; __utma=200167215.1491166330.1448740346.1448740346.1448740346.1; __utmb=200167215.1.10.1448740346; __utmc=200167215; __utmz=200167215.1448740346.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); cb-enabled=enabled");
            conn.setRequestProperty("Origin", "https://www.peka.poznan.pl");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
            conn.setRequestProperty("Accept-Language", "en-GB,en;q=0.8,en-US;q=0.6,pl;q=0.4");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.73 Safari/537.36");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            conn.setRequestProperty("Accept", "text/javascript, text/html, application/xml, text/xml, */*");
            conn.setRequestProperty("X-Prototype-Version", "1.7");
            conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Referer", "https://www.peka.poznan.pl/vm/");

            conn.setDoOutput(true);
            conn.setDoInput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream (
                    conn.getOutputStream ());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            InputStream is = new BufferedInputStream(conn.getInputStream());

            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();

            responseCode = conn.getResponseCode();
            Log.i("ResponseFromServer", "code: " + responseCode);
            Log.i("ResponseFromServer", "response: " + response);

            return response.toString();
        }

        catch (IOException e) {
            Log.i("ExceptionError", "Exception appeared");
            e.printStackTrace();
        } finally {

            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }

    private ArrayList<StopPoint> getStopPointsFromJson(JSONArray jArray) throws JSONException {
        ArrayList<StopPoint> stopPointList = new ArrayList<>();
        stopPointList.clear();

        for (int i = 0; i < jArray.length(); i++) {
            JSONObject jsonData = jArray.getJSONObject(i);
            StopPoint stopPoint = getStopPoints(jsonData);
            stopPointList.add(stopPoint);
        }
        return stopPointList;
    }

    private StopPoint getStopPoints(JSONObject jObject) {
        return new StopPoint(jObject.optString("symbol"),
                jObject.optString("name"));
    }

    private ArrayList<Departure> getDeparturesFromJson(JSONArray jArray) throws JSONException {
        ArrayList<Departure> departureList = new ArrayList<>();
        departureList.clear();

        for (int i = 0; i < jArray.length(); i++) {
            JSONObject jsonData = jArray.getJSONObject(i);
            Departure departure = getDepartures(jsonData);
            departureList.add(departure);
        }
        return departureList;
    }

    private Departure getDepartures(JSONObject jObject) {
        return new Departure(jObject.optBoolean("realTime"),
                jObject.optString("line"),
                jObject.optInt("minutes"),
                jObject.optString("departure"),
                jObject.optBoolean("onStopPoint"));
    }

    private ArrayList<String> getDirectionFromJson(JSONArray jArray) throws JSONException {
        ArrayList<Direction> directionList = new ArrayList<>();
        ArrayList<String> directionStringArray = new ArrayList<>();
        ArrayList<String> directionsArray = new ArrayList<>();
        directionList.clear();

        for (int i = 0; i < jArray.length(); i++) {
            JSONArray cutJson = jArray.getJSONObject(i).getJSONArray("directions");
            for (int j = 0; j < cutJson.length(); j++) {
                JSONObject jsonData = cutJson.getJSONObject(j);
                Direction direction = getDirection(jsonData);
                directionStringArray.add(direction.getLineName() + " -> " + direction.getDirection());
            }
            directionsArray.add(directionStringArray.toString());
            DirectionGroup directionGroup = new DirectionGroup(directionStringArray.toString(), jArray.getJSONObject(i).getJSONObject("bollard").getString("symbol"));
            arrayOfReadyDirections.add(directionGroup);
            directionStringArray.clear();
        }
        return directionsArray;
    }

    private Direction getDirection(JSONObject jObject) {
        return new Direction(jObject.optString("returnVariant"),
                jObject.optString("direction"),
                jObject.optString("lineName"));
    }
}
