package tw.edu.bmilab.healthkeeper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

    //Notification
    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";
    private NotificationManager mNotifyManager;
    private static final int NOTIFICATION_ID = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        createNotificationChannel();

        textView_status = findViewById(R.id.textView_status);
        textView_amount = findViewById(R.id.textView_amount);
        textView_remark = findViewById(R.id.textView_remark);
        button_taken = findViewById(R.id.button_taken);

        DH = new SQLdata(this);
        db = DH.getWritableDatabase();
        queryDB();


    }

    public void takeDrug(View view) {
        add(Integer.parseInt(textView_amount.getText().toString()), 0);
        view.setEnabled(false);
    }

    private void add(int amount, int sex) {
        values = new ContentValues();
        values.put("Timestamp", LocalDateTime.now().atZone(ZoneId.of("UTC+08:00")).format(formatter));
        values.put("Amount", amount);
        values.put("Sex", sex);
        db.insert("Drug", null, values);
    }

    private void queryDB() {
        String remark = "";
        String now = LocalDateTime.now().atZone(ZoneId.of("UTC+08:00")).format(formatter);
        String oneDayAgo = LocalDateTime.now().atZone(ZoneId.of("UTC+08:00")).minusDays(1L).format(formatter);
        String twoDayAgo = LocalDateTime.now().atZone(ZoneId.of("UTC+08:00")).minusDays(2L).format(formatter);
        String sql = "SELECT * FROM Drug WHERE Amount > 0 AND Timestamp BETWEEN '" + oneDayAgo + "' AND '" + now + "' ORDER BY Timestamp DESC";
        String sql2 = "SELECT * FROM Drug WHERE Amount > 0 AND Timestamp BETWEEN '" + twoDayAgo + "' AND '" + oneDayAgo + "' ORDER BY Timestamp DESC";
        cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            //24hr內有吃藥
            timestamp = cursor.getString(1);
            amount = cursor.getInt(2);
            if (cursor.getInt(3) == 1) {
                sex = true;
            } else {
                sex = false;
            }
            textView_status.setText("You are at Low Risk");
            textView_status.setTextColor(Color.GREEN);

            //2小時前才可吃藥
            if (LocalDateTime.parse(timestamp, formatter).plusDays(1L).minusHours(2L).isBefore(LocalDateTime.now().atZone(ZoneId.of("UTC+08:00")).toLocalDateTime())) {
                textView_amount.setText("1");
                remark += "Take the drug before UTC+8 " + LocalDateTime.parse(timestamp, formatter).plusDays(1L).format(formatter) + "\n\n";
            } else {
                textView_amount.setVisibility(View.INVISIBLE);
                button_taken.setText("Not yet");
                button_taken.setEnabled(false);
                remark += "Next dose: UTC+8 " + LocalDateTime.parse(timestamp, formatter).plusDays(1L).format(formatter) + "\n\n";
            }
            remark += "Last dose: UTC+8 " + LocalDateTime.parse(timestamp, formatter).format(formatter) + "\n\n";
            textView_remark.setText(remark);
            textView_remark.setTextColor(Color.BLUE);
        } else if (db.rawQuery(sql2, null).moveToFirst()) {
            //前48-24hr內有吃藥->補吃2顆?
            timestamp = cursor.getString(1);
            amount = cursor.getInt(2);
            if (cursor.getInt(3) == 1) {
                sex = true;
            } else {
                sex = false;
            }
            textView_status.setText("You are at High risk");
            textView_status.setTextColor(Color.RED);
            textView_remark.setText("Last drug taken at UTC+8 " + timestamp);
            textView_amount.setText("2");
        } else {
            textView_remark.setText("No drug taken within 48 hr.");
            textView_status.setText("You are at High risk");
            textView_status.setTextColor(Color.RED);
            textView_amount.setText("2");
            sendNotification();
        }
    }

    public void sendNotification() {
        NotificationCompat.Builder notifyBuilder = getNotificationBuilder();
        mNotifyManager.notify(NOTIFICATION_ID, notifyBuilder.build());
    }

    public void createNotificationChannel() {
        mNotifyManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.O) {
            // Create a NotificationChannel
            NotificationChannel notificationChannel = new NotificationChannel(PRIMARY_CHANNEL_ID,
                    "Prep Notification", NotificationManager
                    .IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("Time to take drug");
            mNotifyManager.createNotificationChannel(notificationChannel);
        }
    }

    private NotificationCompat.Builder getNotificationBuilder() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(this,
                NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID)
                .setContentTitle("It's time")
                .setContentText("Time to take drug")
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL);
        return notifyBuilder;
    }


    private static final int TIME_INTERVAL = 2000; // # milliseconds, desired time passed between two back presses.
    private long mBackPressed;

    @Override
    public void onBackPressed() {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed();
            return;
        } else {
            Toast.makeText(getBaseContext(), "Press back button again to exit", Toast.LENGTH_SHORT).show();
        }
        mBackPressed = System.currentTimeMillis();
    }
}
