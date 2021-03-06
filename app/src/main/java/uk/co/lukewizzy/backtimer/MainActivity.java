package uk.co.lukewizzy.backtimer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.codetroopers.betterpickers.hmspicker.HmsPickerBuilder;
import com.codetroopers.betterpickers.hmspicker.HmsPickerDialogFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements HmsPickerDialogFragment.HmsPickerDialogHandler {
    private static MainActivity instance;
    List<Integer> times;

    public static MainActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        instance = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        times = new ArrayList<>();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HmsPickerBuilder hms = new HmsPickerBuilder()
                        .setFragmentManager(getSupportFragmentManager())
                        .setStyleResId(R.style.BetterPickersDialogFragment);
                hms.show();
            }
        });

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Calendar cal = Calendar.getInstance();
                        ((TextView) findViewById(R.id.timeText))
                                .setText(String.format("%02d", cal.get(Calendar.HOUR_OF_DAY))
                                        + ":" + String.format("%02d", cal.get(Calendar.MINUTE))
                                        + ":" + String.format("%02d", cal.get(Calendar.SECOND)));
                    }
                });
            }
        }, 0, 1000);

        refreshList();
        ListView timeList = (ListView)findViewById(R.id.timeList);
        timeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.getInstance());
                builder.setTitle("Are you sure you want to remove this item?");

                builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        times.remove(position);
                        refreshList();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menuItemReset) {
            resetList();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void refreshList() {
        List<String[]> strs = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, 1);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        /*// If nothing in list
        if (times.size() == 0) {
            strs.add(new String[] {"60:00", "00:00"});
        }*/

        for (int i : times) {
            cal.add(Calendar.SECOND, i * -1);
            String[] s = new String[2];
            int mins = i / 60;
            int secs = i % 60;
            s[0] = String.format("%02d", cal.get(Calendar.MINUTE)) + ":" + String.format("%02d", cal.get(Calendar.SECOND));
            s[1] = String.format("%02d", mins) + ":" + String.format("%02d", secs);
            strs.add(s);
        }

        ListView list = ((ListView) findViewById(R.id.timeList));
        list.setAdapter(new BackTimeListAdapter(getApplicationContext(), strs));
        list.deferNotifyDataSetChanged();
    }

    public void resetList() {
        ListView list = (ListView) findViewById(R.id.timeList);
        if (list.getCount() > 0) {
            Snackbar.make(getCurrentFocus(), "List cleared.", Snackbar.LENGTH_LONG).show();
        }
        list.setAdapter(null);
        times.clear();
    }

    @Override
    public void onDialogHmsSet(int reference, int hours, int minutes, int seconds) {
        times.add((minutes * 60) + seconds);
        refreshList();
    }
}
