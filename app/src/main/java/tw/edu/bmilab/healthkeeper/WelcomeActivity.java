package tw.edu.bmilab.healthkeeper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class WelcomeActivity extends AppCompatActivity {
    private TextView textView_status;
    private TextView textView_amount;
    private TextView textView_remark;
    private Button button_taken;

    //SQLite
    SQLiteDatabase db;
    ContentValues values;
    Cursor cursor;
    public SQLdata DH = null;
    private String timestamp;
    private int amount;
    private boolean sex;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        textView_status = findViewById(R.id.textView_status);
        textView_amount = findViewById(R.id.textView_amount);
        textView_remark = findViewById(R.id.textView_remark);
        button_taken = findViewById(R.id.button_taken);

        DH = new SQLdata(this);
        db = DH.getWritableDatabase();
        queryDB();
    }

    private void add(int amount, int sex) {
        values = new ContentValues();
        values.put("Timestamp", LocalDateTime.now().atZone(ZoneId.of("UTC+08:00")).format(formatter));
//        values.put("Amount", );
        values.put("Sex", LocalDateTime.now().atZone(ZoneId.of("UTC+08:00")).format(formatter));
        db.insert("TB2018", null, values);
    }

    private void queryDB() {
        String now = LocalDateTime.now().atZone(ZoneId.of("UTC+08:00")).format(formatter);
        String oneDaysAgo = LocalDateTime.now().atZone(ZoneId.of("UTC+08:00")).minusDays(1L).format(formatter);
        String sql = "SELECT * FROM Drug WHERE Amount > 0 AND Timestamp BETWEEN " + oneDaysAgo + " AND " + now + " ORDER BY Timestamp ASC";
        cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            timestamp = cursor.getString(0);
            amount = cursor.getInt(1);
            if (cursor.getInt(2) == 1) {
                sex = true;
            } else {
                sex = false;
            }
        }
    }
}
