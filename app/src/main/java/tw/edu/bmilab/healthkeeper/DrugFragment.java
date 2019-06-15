package tw.edu.bmilab.healthkeeper;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.TimeZone;

import static android.content.Context.ALARM_SERVICE;


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
    private static final String TIMESTAMP = "drugTimestamp";

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
    Cursor beginCursor;
    Cursor amountCursor;
    Cursor sexCursor;
    public SQLdata DH = null;
    private String drugTimestamp = "";
    private String sexTimestamp = "";
    private int amount = 999;
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
        updateUI();

        button_takeNow.setOnClickListener(new View.OnClickListener() {
                                              @Override
                                              public void onClick(View v) {
                                                  add(Integer.parseInt(textView_amount.getText().toString()), 2);
                                                  queryDB();
                                                  updateUI();
                                                  button_takeNow.setEnabled(false);
                                                  button_eval.setVisibility(View.INVISIBLE);
                                                  Calendar tmr = Calendar.getInstance(TimeZone.getTimeZone("GMT + 8"));
                                                  tmr.add(Calendar.HOUR, 23);
                                                  setReminder(getContext(), AlarmReceiver.class, tmr);
                                              }
                                          }
        );

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
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    textView_status.setText("No need to take medicine now");
                                    textView_amount.setVisibility(View.INVISIBLE);
                                    button_takeNow.setVisibility(View.INVISIBLE);
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
                                                }
                                            })
                                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    textView_status.setText("PrEP end.");
                                                    button_takeNow.setVisibility(View.INVISIBLE);
                                                    textView_remark.setVisibility(View.INVISIBLE);
                                                    button_eval.setVisibility(View.INVISIBLE);
                                                    deleteAll();
                                                }
                                            }).show();
                                }
                            })
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    sex = 1;
                                    add(0, 1);
                                    new AlertDialog.Builder(getContext())
                                            .setMessage("Sex in the next 24 hours?")
                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    textView_status.setText("Take medicine now.");
                                                    textView_amount.setText("1");
                                                    textView_amount.setVisibility(View.VISIBLE);
                                                    button_takeNow.setVisibility(View.VISIBLE);
                                                }
                                            })
                                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    textView_status.setText("Take last dose now");
                                                    button_takeNow.setVisibility(View.VISIBLE);
                                                    textView_amount.setText("1");
                                                    textView_amount.setVisibility(View.VISIBLE);
                                                }
                                            }).show();
                                }
                            }).show();
                }
            }
        });
        return view;
    }

    public static void setReminder(Context context, Class<?> cls, Calendar cal) {
        Calendar calendar = Calendar.getInstance();
        Calendar setcalendar = cal;

        if (setcalendar.before(calendar))
            setcalendar.add(Calendar.MINUTE, 1);

        // Enable a receiver
        ComponentName receiver = new ComponentName(context, cls);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        Intent intent1 = new Intent(context, cls);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                0, intent1,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, setcalendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent);
    }

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
        String now = LocalDateTime.now(ZoneId.of("UTC+08:00")).format(formatter);
        String oneDayAgo = LocalDateTime.now(ZoneId.of("UTC+08:00")).minusDays(1L).minusHours(1L).format(formatter);
        String beginSql = "SELECT Amount FROM Drug";
        String sqlAmount = "SELECT _id, Timestamp, Amount FROM Drug WHERE Amount > 0 AND Timestamp BETWEEN '" + oneDayAgo + "' AND '" + now + "' ORDER BY Timestamp DESC";
        String sqlSex = "SELECT _id, Timestamp, Sex FROM Drug WHERE Sex == 1 AND Timestamp BETWEEN '" + oneDayAgo + "' AND '" + now + "' ORDER BY Timestamp DESC";
        beginCursor = db.rawQuery(beginSql, null);
        amountCursor = db.rawQuery(sqlAmount, null);
        sexCursor = db.rawQuery(sqlSex, null);
        if (!beginCursor.moveToFirst()) {
            textView_remark.setText("No drug taken ever.");
            textView_status.setText("You are not on PrEP yet");
            textView_status.setTextColor(Color.RED);
            button_eval.setText("Should I take medicine?");
        }

        if (amountCursor.moveToFirst()) {
            drugTimestamp = amountCursor.getString(1);
        }

        if (sexCursor.moveToFirst()) {
            sexTimestamp = sexCursor.getString(1);
        }
    }

    private void updateUI() {
        String remark = "";
        if (drugTimestamp.equals("")) {
            remark += "No drug taken within 24 hr.\n\n";
            textView_remark.setText(remark);
            textView_remark.setTextColor(Color.RED);
        } else {
            remark += "Last dose: " + drugTimestamp + "\n\n";
            if (!sexTimestamp.equals("")) {
                remark += "Had sex within 24hr.\n\n";
            }
            if (LocalDateTime.now(ZoneId.of("UTC+08:00")).isBefore(LocalDateTime.parse(drugTimestamp, formatter).plusDays(1L).plusHours(1L))) {
                textView_status.setText("Low Risk");
                textView_status.setTextColor(Color.GREEN);
                remark += "Next dose: " + LocalDateTime.parse(drugTimestamp, formatter).plusDays(1L).format(formatter) + "\n\n";
                textView_remark.setText(remark);
                textView_remark.setTextColor(Color.BLUE);
                if (LocalDateTime.now(ZoneId.of("UTC+08:00")).isBefore(LocalDateTime.parse(drugTimestamp, formatter).plusDays(1L).minusHours(1L))) {
                    button_eval.setVisibility(View.INVISIBLE);
                }
            } else {
                textView_status.setText("PrEP Failed");
                textView_status.setTextColor(Color.RED);
                button_eval.setVisibility(View.INVISIBLE);
                button_takeNow.setVisibility(View.INVISIBLE);
                if (!sexTimestamp.equals("")) {
                    remark += "Had sex within 24hr.\n\n";
                }
                remark += "Contact Case Manager ASAP.\n\n";
                textView_remark.setText(remark);
                textView_remark.setTextColor(Color.RED);
            }
        }
    }

    private void deleteAll() {
        db.execSQL("DELETE FROM Drug");
    }
}
