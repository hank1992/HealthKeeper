package tw.edu.bmilab.healthkeeper;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private String gender;
    private EditText DOB;
    private EditText weight;
    private SharedPreferences mPreferences;
    private String sharedPrefFile =
            "tw.edu.bmilab.healthkeeper";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        if (mPreferences != null) {
            gender = mPreferences.getString("gender",null);
            if (gender != null) {
                Intent welcomeIntent = new Intent(this, WelcomeActivity.class);
                startActivity(welcomeIntent);
            }
        }

        DOB = findViewById(R.id.editDate);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Spinner spinner = findViewById(R.id.spinner_gender);
        //建立一個ArrayAdapter物件，並放置下拉選單的內容
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, new String[]{"Male", "Female"});
        //設定下拉選單的樣式
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        //設定項目被選取之後的動作
        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView adapterView, View view, int position, long id) {
                gender = adapterView.getSelectedItem().toString();
            }

            public void onNothingSelected(AdapterView arg0) {
            }
        });

        weight = findViewById(R.id.editText_weight);

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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void datePicker(View v) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        new DatePickerDialog(v.getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                String dateTime = String.valueOf(year) + "-" + String.valueOf(month) + "-" + String.valueOf(day);
                DOB.setText(dateTime);
            }

        }, year, month, day).show();
    }

    public void onClick(View view) {
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.putString("gender", gender);
        preferencesEditor.putString("DOB", DOB.getText().toString());
        preferencesEditor.putFloat("weight", Float.parseFloat(weight.getText().toString()));
        preferencesEditor.apply();
        Intent welcomeIntent = new Intent(this, WelcomeActivity.class);
        startActivity(welcomeIntent);
    }
}
