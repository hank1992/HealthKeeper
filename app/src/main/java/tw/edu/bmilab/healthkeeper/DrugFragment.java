package tw.edu.bmilab.healthkeeper;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static android.content.Context.NOTIFICATION_SERVICE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DrugFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DrugFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DrugFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String STATUS = "status";
    private static final String AMOUNT = "999";
    private static final String REMARK = "remark";
    private static final String TIMESTAMP = "timestamp";

    // TODO: Rename and change types of parameters
    private String mStatus;
    private int mAmount;
    private String mRemark;
    private String mTimestamp;

    private TextView textView_status;
    private TextView textView_amount;
    private TextView textView_remark;
    private Button button_taken;

    //Notification
    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";
    private NotificationManager mNotifyManager;
    private static final int NOTIFICATION_ID = 0;

    //SQLite
    SQLiteDatabase db;
    ContentValues values;
    Cursor cursor;
    public SQLdata DH = null;
    private String timestamp;
    private int amount;
    private boolean sex;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private OnFragmentInteractionListener mListener;

    public DrugFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static DrugFragment newInstance() {
        DrugFragment fragment = new DrugFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStatus = getArguments().getString(STATUS);
            mAmount = getArguments().getInt(AMOUNT);
            mRemark = getArguments().getString(REMARK);
            mTimestamp = getArguments().getString(TIMESTAMP);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_drug, container, false);
        textView_status = view.findViewById(R.id.textView_status);
        textView_amount = view.findViewById(R.id.textView_amount);
        textView_remark = view.findViewById(R.id.textView_remark);
        button_taken = view.findViewById(R.id.button_taken);

        DH = new SQLdata(getContext());
        db = DH.getWritableDatabase();
        queryDB();

        button_taken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                takeDrug("takeDrug");
                add(Integer.parseInt(textView_amount.getText().toString()), 0);
                view.setEnabled(false);
                queryDB();
            }
        });
        return view;
    }

//    // TODO: Rename method, update argument and hook method into UI event
//    public void takeDrug(String sendBackText) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(sendBackText);
//        }
//    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
//        else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(String sendBackText);
    }

//    public void takeDrug(View view) {
//        add(Integer.parseInt(textView_amount.getText().toString()), 0);
//        view.setEnabled(false);
//        queryDB();
//    }

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
        Intent notificationIntent = new Intent(getContext(), MainActivity.class);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(getContext(),
                NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(getContext(), PRIMARY_CHANNEL_ID)
                .setContentTitle("It's time")
                .setContentText("Time to take drug")
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL);
        mNotifyManager = (NotificationManager)
                getContext().getSystemService(NOTIFICATION_SERVICE);
        mNotifyManager.notify(NOTIFICATION_ID, notifyBuilder.build());
    }
}
