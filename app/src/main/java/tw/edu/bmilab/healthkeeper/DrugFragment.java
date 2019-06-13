package tw.edu.bmilab.healthkeeper;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
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
    private Button button_eval;
    private Button button_takeNow;

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
    private int sex = 2;
    //0 無 1有 2不適用
    private String remark = "";
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
        textView_amount.setVisibility(View.INVISIBLE);
        textView_remark = view.findViewById(R.id.textView_remark);
        button_eval = view.findViewById(R.id.button_eval);
        button_takeNow = view.findViewById(R.id.button_takeNow);
        button_takeNow.setVisibility(View.INVISIBLE);

        DH = new SQLdata(getContext());
        db = DH.getWritableDatabase();
        queryDB();

        button_takeNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add(Integer.parseInt(textView_amount.getText().toString()), sex);
                queryDB();
                button_takeNow.setEnabled(false);
                button_eval.setVisibility(View.INVISIBLE);
            }
        });

        button_eval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (textView_status.getText().toString().equals("You are not on PrEP yet") ||
                        textView_status.getText().toString().equals("No need to take medicine now") ||
                        textView_status.getText().toString().equals("Take medicine now and have fun 2 hours later")) {
                    new AlertDialog.Builder(getContext())
                            .setMessage("Sex in the next 24 hours?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    textView_status.setText("Take medicine now and have fun 2 hours later");
                                    textView_amount.setText("2");
                                    textView_amount.setVisibility(View.VISIBLE);
                                    button_takeNow.setVisibility(View.VISIBLE);
//                                    sex = true;
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    textView_status.setText("No need to take medicine now");
                                    textView_amount.setVisibility(View.INVISIBLE);
                                    button_takeNow.setVisibility(View.INVISIBLE);
//                                                textView_remark.setVisibility(View.INVISIBLE);
//                                    sex = false;
                                }
                            }).show();
                } else {
                    new AlertDialog.Builder(getContext())
                            .setMessage("Sex in the past 24 hours?")
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    new AlertDialog.Builder(getContext())
                                            .setMessage("Sex in the next 24 hours?")
                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    textView_status.setText("Take medicine now.");
                                                    textView_amount.setText("1");
                                                    textView_amount.setVisibility(View.VISIBLE);
                                                    button_takeNow.setVisibility(View.VISIBLE);
                                                    sex = 1;
                                                }
                                            })
                                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    textView_status.setText("PrEP end.");
                                                    button_takeNow.setVisibility(View.INVISIBLE);
//                                                textView_remark.setVisibility(View.INVISIBLE);
                                                    sex = 0;
                                                }
                                            }).show();
                                }
                            })
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    sex = 1;
                                    new AlertDialog.Builder(getContext())
                                            .setMessage("Sex in the next 24 hours?")
                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    textView_status.setText("Take medicine now.");
                                                    textView_amount.setText("1");
                                                    button_takeNow.setVisibility(View.VISIBLE);
                                                }
                                            })
                                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    textView_status.setText("Take medicine now.");
                                                    button_takeNow.setVisibility(View.INVISIBLE);
//                                                textView_remark.setVisibility(View.INVISIBLE);
                                                    remark += "It's your last dose if no sex in the next 24hr.";
                                                }
                                            }).show();
                                }
                            }).show();
                }
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
        String now = LocalDateTime.now().atZone(ZoneId.of("UTC+08:00")).format(formatter);
        String oneDayAgo = LocalDateTime.now().atZone(ZoneId.of("UTC+08:00")).minusDays(1L).format(formatter);
        String twoDayAgo = LocalDateTime.now().atZone(ZoneId.of("UTC+08:00")).minusDays(2L).format(formatter);
        String beginSql = "SELECT Amount FROM Drug";
        String sql = "SELECT * FROM Drug WHERE Amount > 0 AND Timestamp BETWEEN '" + oneDayAgo + "' AND '" + now + "' ORDER BY Timestamp DESC";
//        String sql2 = "SELECT * FROM Drug WHERE Amount > 0 AND Timestamp BETWEEN '" + twoDayAgo + "' AND '" + oneDayAgo + "' ORDER BY Timestamp DESC";
        cursor = db.rawQuery(beginSql, null);
        if (!cursor.moveToFirst()) {
            textView_remark.setText("No drug taken within 24 hr.");
            textView_status.setText("You are not on PrEP yet");
            textView_status.setTextColor(Color.RED);
            button_eval.setText("Should I take medicine?");
        } else {
            cursor = db.rawQuery(sql, null);
            if (cursor.moveToFirst()) {
                //24hr內有吃藥
                timestamp = cursor.getString(1);
                amount = cursor.getInt(2);
                if (cursor.getInt(3) == 1) {
                    remark += "Had sex in the past 24 hr.\n\n";
                } else if (cursor.getInt(3) == 0) {
                    remark += "No sex in the past 24 hr.\n\n";
                }
                textView_status.setText("You are at Low Risk");
                textView_status.setTextColor(Color.GREEN);

                //1小時前才可吃藥
                if (LocalDateTime.parse(timestamp, formatter).plusDays(1L).minusHours(1L).isBefore(LocalDateTime.now().atZone(ZoneId.of("UTC+08:00")).toLocalDateTime())) {
                    button_eval.setVisibility(View.INVISIBLE);
                    remark += "Come back at " + LocalDateTime.parse(timestamp, formatter).plusDays(1L).minusHours(1L).format(formatter) + "\n\n";
//                    textView_amount.setText("1");
//                    remark += "Take the drug before UTC+8 " + LocalDateTime.parse(timestamp, formatter).plusDays(1L).format(formatter) + "\n\n";
                    remark += "Last dose: UTC+8 " + LocalDateTime.parse(timestamp, formatter).format(formatter) + "\n\n";
                    remark += "Next dose: UTC+8 " + LocalDateTime.parse(timestamp, formatter).plusDays(1L).format(formatter) + "\n\n";
                    textView_remark.setText(remark);
                    textView_remark.setTextColor(Color.BLUE);
                } else if (LocalDateTime.parse(timestamp, formatter).plusDays(1L).plusHours(1L).isAfter(LocalDateTime.now().atZone(ZoneId.of("UTC+08:00")).toLocalDateTime())) {
//                    button_eval.setVisibility(View.INVISIBLE);
//                    button_eval.setText("Not yet");
//                    button_eval.setEnabled(false);
//                    remark += "Take medicine before " + LocalDateTime.parse(timestamp, formatter).plusDays(1L).plusHours(1L).format(formatter) + "\n\n";
                    remark += "Last dose: UTC+8 " + LocalDateTime.parse(timestamp, formatter).format(formatter) + "\n\n";
                    remark += "Next dose: UTC+8 " + LocalDateTime.parse(timestamp, formatter).plusDays(1L).format(formatter) + "\n\n";
                    textView_remark.setText(remark);
                    textView_remark.setTextColor(Color.BLUE);
                } else {
                    textView_status.setText("PrEP failed! Contact your Case Manager ASAP !");
                    textView_status.setTextColor(Color.RED);
                    button_eval.setVisibility(View.INVISIBLE);
                    textView_remark.setText("Last dose: UTC+8 " + LocalDateTime.parse(timestamp, formatter).format(formatter));
                }

            }
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
