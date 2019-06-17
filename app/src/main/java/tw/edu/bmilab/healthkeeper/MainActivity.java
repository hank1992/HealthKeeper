package tw.edu.bmilab.healthkeeper;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private String gender;
    private TextView DOB;
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
            gender = mPreferences.getString("gender", null);
            if (gender != null) {
                Intent welcomeIntent = new Intent(this, WelcomeActivity.class);
                this.finish();
                startActivity(welcomeIntent);
            }
        }

        DOB = findViewById(R.id.textView_EditDOB);

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

    public void datePicker(View v) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        new DatePickerDialog(v.getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                String dateTime = String.valueOf(year) + "-" + String.valueOf(month + 1) + "-" + String.valueOf(day);
                DOB.setText(dateTime);
                Calendar userCalendar = Calendar.getInstance();
                userCalendar.set(year, month, day);
                Calendar eighteenYearAgr = Calendar.getInstance();
                eighteenYearAgr.add(Calendar.YEAR, -18);
                if (eighteenYearAgr.before(userCalendar)) {
                    Toast.makeText(view.getContext(), "Truvada not recommended for under 18", Toast.LENGTH_LONG).show();
                }
            }

        }, year, month, day).show();
    }

    public void onClick(View view) {
        String errCol = "";
        if (gender.equals("")) {
            errCol += "Please enter Gender\n";
        }
        if (DOB.getText().toString().equals("")) {
            errCol += "Please enter DOB\n";
        }
        if (weight.getText().toString().equals("")) {
            errCol += "Please enter Weight\n";
        }

        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        if (errCol.equals("")) {
            preferencesEditor.putString("gender", gender);
            preferencesEditor.putString("DOB", DOB.getText().toString());
            try {
                preferencesEditor.putFloat("weight", Float.parseFloat(weight.getText().toString()));
            } catch (NumberFormatException Nfe) {
                errCol += "Weight format exception\n";
            }
        }
        if (errCol.equals("")) {
            preferencesEditor.apply();
            Intent welcomeIntent = new Intent(this, WelcomeActivity.class);
            startActivity(welcomeIntent);
        } else {
            Toast.makeText(this, errCol, Toast.LENGTH_SHORT).show();
        }
    }
}
