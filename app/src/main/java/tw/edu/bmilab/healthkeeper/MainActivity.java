package tw.edu.bmilab.healthkeeper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {


    private SharedPreferences mPreferences;
    private String sharedPrefFile =
            "tw.edu.bmilab.healthkeeper";
    private String gender;

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
    }

    public void next(View view) {
        Intent profileIntent = new Intent(this, ProfileActivity.class);
        this.finish();
        startActivity(profileIntent);
    }
}
