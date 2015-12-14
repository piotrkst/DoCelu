package com.engineer.docelu;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

public class MainActivity extends AppCompatActivity {

    public static String TEST_URL = "https://www.peka.poznan.pl/vm/method.vm?ts=";
    private ArrayList<Departure> arrayOfDepartures = new ArrayList<>();
    private ArrayList<String> arrayOfDirections = new ArrayList<>();
    private ArrayList<DirectionGroup> arrayOfReadyDirections = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final TextView bollard = (TextView) findViewById(R.id.bollard);
        final EditText bollardName = (EditText) findViewById(R.id.bollard_name);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setColorFilter(Color.parseColor("#ffffff"));
        fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#177F42")));
        fab.setRippleColor(Color.parseColor("#0C4021"));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, R.string.downloading_schedule, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                new ExecuteNetworkOperation("getBollardsByStopPoint", "{\"name\":\"" + bollardName.getText() + "\"}").execute();
                //Log.i("test", "getBollardsByStopPoint, {\"name\":\"" + bollardName.getText() + "\"}");
            }
        });
    }

    private AlertDialog DirectionDialog(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Ustal kierunek podróży:");
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, R.layout.direction_row, R.id.element, arrayOfDirections);
        dialog.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                Log.i("TEST DIALOGU", "UZYTO: " + arrayOfReadyDirections.get(item).getSymbol());
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
        String ts = tsLong.toString();
        Log.i("TS TEST", "TS: " + ts);
        return ts;
    }

    private void showSchedule(){
        ScheduleAdapter adapter = new ScheduleAdapter(MainActivity.this, arrayOfDepartures);
        ListView departuresView = (ListView) findViewById(R.id.schedule);
        departuresView.setAdapter(adapter);
    }

    public class ScheduleAdapter extends ArrayAdapter<Departure> {

        public ScheduleAdapter(Context context, ArrayList<Departure> items) {
            super(context, 0, items);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final Departure departure = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.content_main_listview, parent, false);
            }

            final TextView lineMenu = (TextView) convertView.findViewById(R.id.line_menu);
            final TextView line = (TextView) convertView.findViewById(R.id.line);
            final TextView minutesMenu = (TextView) convertView.findViewById(R.id.minutes_menu);
            final TextView minutes = (TextView) convertView.findViewById(R.id.minutes);

            line.setText(departure.getLine()+"");
            if (departure.getRealTime()){
                minutes.setTextColor(ColorStateList.valueOf(Color.parseColor("#177F42")));
                minutes.setText(departure.getMinutes()+"");
            } else {
                minutes.setText(departure.getDeparture().substring(11, 16)+"");
            }
            return convertView;
        }

        public Departure getItem(int position) {
            return arrayOfDepartures.get(position);
        }

        public final int getCount() {
            return arrayOfDepartures.size();
        }

        public final long getItemId(int position) {
            return position;
        }
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
                if (method == "getTimes") {
                    //Toast.makeText(MainActivity.this, R.string.downloading_success, Toast.LENGTH_SHORT).show();
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
                            AlertDialog directionDialog = DirectionDialog();
                            directionDialog.show();
                        } else {
                            Toast.makeText(MainActivity.this, "Nie znaleziono przystanku", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Nie znaleziono przystanku", Toast.LENGTH_SHORT).show();
                    }
                    Log.i("TEST ARRAY", "array: " + arrayOfDirections);
//                Log.i("TEST ARRAY", "array first object: " + arrayOfDirections.get(0));
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
//      TO DO
//    curl "https://www.peka.poznan.pl/vm/method.vm?ts=1449522087323"
//            -H "Cookie: JSESSIONID=SJj8n9FGP4Egc6RMXWTOmJgY.undefined"
//            -H "Origin: https://www.peka.poznan.pl"
//            -H "Accept-Encoding: gzip, deflate"
//            -H "Accept-Language: en-GB,en;q=0.8,en-US;q=0.6,pl;q=0.4"
//            -H "User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.23 Safari/537.36"
//            -H "Content-type: application/x-www-form-urlencoded; charset=UTF-8"
//            -H "Accept: text/javascript, text/html, application/xml, text/xml, */*"
//            -H "X-Prototype-Version: 1.7"
//            -H "X-Requested-With: XMLHttpRequest"
//            -H "Connection: keep-alive"
//            -H "Referer: https://www.peka.poznan.pl/vm/"
//            --data "method=getStopPoints&p0="%"7B"%"22pattern"%"22"%"3A"%"22kupa"%"22"%"7D"
//            --compressed
//            --insecure

    public String sendToServer(String var1, String var2) throws IOException {
        final String method = var1;
        final String p0 = var2;
        String timeStamp = getTimeStamp();
        HttpURLConnection conn = null;
        Integer responseCode;
        String urlParameters = "method=" + URLEncoder.encode(method,"UTF-8") + "&p0=" + URLEncoder.encode(p0, "UTF-8");

        Log.i("URLEncoder", "URLEncoder: " + URLEncoder.encode("{\"symbol\":\"WICH02\"}", "UTF-8"));
        try {
            // create HttpURLConnection
            URL url = new URL(TEST_URL + timeStamp);
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
