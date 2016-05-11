package com.devmel.apps.reprapcontrol;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.Toast;

import com.devmel.apps.reprapcontrol.datas.SharedData;
import com.devmel.apps.reprapcontrol.tools.GCodeControl;
import com.devmel.apps.reprapcontrol.view.AxisFragment;
import com.devmel.apps.reprapcontrol.view.MachineFragment;
import com.devmel.apps.reprapcontrol.view.SdcardFragment;
import com.devmel.communication.IUart;
import com.devmel.communication.android.UartBluetooth;
import com.devmel.communication.android.UartUsbOTG;
import com.devmel.communication.linkbus.Usart;
import com.devmel.storage.Node;
import com.devmel.storage.SimpleIPConfig;
import com.devmel.storage.android.UserPrefs;

import java.io.IOException;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    public final static String sharedPreferencesName = "com.devmel.apps.reprapcontrol";
    public final GCodeControl gcodeControl = new GCodeControl();
    private final GCodeListener listener = new GCodeListener();
    public final SharedData sharedData = new SharedData();
    private IUart device = null;
    private UserPrefs userPrefs;
    private LocalTimerLoop timer;

    private MachineFragment machine = null;
    private AxisFragment axis = null;
    private SdcardFragment sdcard = null;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initPreferences();
        sharedData.load(userPrefs);
        deviceSelect();
        if(gcodeControl.isConnected()){
            if(timer!=null)
                timer.interrupt();
            timer = new LocalTimerLoop();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        sharedData.save(userPrefs);
        if(timer!=null)
            timer.interrupt();
        timer = null;
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
        if (id == R.id.action_settings) {
            portSelect();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void portSelect() {
        Intent intent = new Intent(this, PortSelect.class);
        startActivity(intent);
    }

    public void vtgToggle(boolean isChecked) {
        IUart dev = gcodeControl.getDevice();
        if (dev != null && dev instanceof Usart) {
            try {
                ((Usart) dev).setVTG(isChecked);
                sharedData.linkbusStatus[0] = true;
                sharedData.linkbusStatus[1] = isChecked;
            } catch (IOException e) {
                sharedData.linkbusStatus[0] = false;
            }
        }
    }
    public void resetToggle(boolean isChecked){
        IUart dev = gcodeControl.getDevice();
        if (dev != null && dev instanceof Usart) {
            try {
                ((Usart) dev).setReset(isChecked);
                sharedData.linkbusStatus[0] = true;
                sharedData.linkbusStatus[2] = isChecked;
            } catch (IOException e) {
                sharedData.linkbusStatus[0] = false;
            }
        }
    }

    public void machineBusyMsg(){
        displayToast(getString(R.string.machineIsBusy));
    }

    public void connectClick(){
        gcodeControl.setListener(listener);
        if(!gcodeControl.isConnected()) {
            if(device == null){
                portSelect();
            }else {
                sharedData.connect = true;
                if (machine != null)
                    machine.refresh();
                try {
                    device.setParameters(sharedData.getBaudrate(), sharedData.getDatabits(), sharedData.getStopbits(), sharedData.getParity());
                } catch (Exception e) {
                }
                gcodeControl.resetBuffer();
                sharedData.connect = gcodeControl.connect(device, false);
                //Refresh tab layout
                TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
                tabLayout.setupWithViewPager(mViewPager);
                if (machine != null)
                    machine.refresh();
            }
        }else{
            gcodeControl.disconnect();
        }
    }

    private void deviceSelect(){
        try {
            //Select Device
            String type = userPrefs.getString("selectedType");
            String name = userPrefs.getString("selectedName");
            if (type != null && name != null) {
                if (type.equals("LB")) {
                    if (name.contains(" - ")) {
                        String[] names = name.split(" - ");
                        if (names != null && names.length > 0) {
                            Node devices = new Node(this.userPrefs, "Linkbus");
                            String[] ipDeviceList = devices.getChildNames();
                            if (ipDeviceList != null) {
                                for (String devStr : ipDeviceList) {
                                    if (devStr.equals(names[0])) {
                                        SimpleIPConfig ipDevice = SimpleIPConfig.createFromNode(devices, devStr);
                                        Usart uart = new Usart(ipDevice);
                                        //uart.setLock(userPrefs.getInt("lock")==1 ? true : false);
                                        uart.setMode(Usart.MODE_ASYNCHRONOUS);
                                        uart.setInterruptMode(true, 1000);
                                        device = uart;
                                        sharedData.setPortName(devStr);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } else if (type.equals("USB")) {
                    String[] usbDeviceList = UartUsbOTG.list(getBaseContext());
                    for (String devStr : usbDeviceList) {
                        if (devStr.equals(name)) {
                            device = new UartUsbOTG(devStr, getBaseContext());
                            sharedData.setPortName(devStr);
                            break;
                        }
                    }
                } else if (type.equals("BT")) {
                    String[] btDeviceList = UartBluetooth.list();
                    for (String devStr : btDeviceList) {
                        if (devStr.equals(name)) {
                            device = new UartBluetooth(devStr);
                            sharedData.setPortName(devStr);
                            break;
                        }
                    }
                }
            }
        }catch(Exception e){
//            e.printStackTrace();
            device = null;
            sharedData.setPortName(null);
        }
    }

    private void initPreferences(){
        if(userPrefs==null){
            userPrefs = new UserPrefs(getSharedPreferences(MainActivity.sharedPreferencesName, Context.MODE_PRIVATE));
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position==0){
                    machine = new MachineFragment();
                return machine;
            }
            else if(position==1){
                    axis = new AxisFragment();
                return axis;
            }
            else if(position==2){
                    sdcard = new SdcardFragment();
                return sdcard;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    private class LocalTimerLoop extends Thread{
        private boolean run = true;
        private long start = System.currentTimeMillis();

        private LocalTimerLoop(){
            this.start();
        }
        @Override
        public void interrupt(){
            run = false;
            super.interrupt();
        }
        @Override
        public void run(){
            int counter = 0;
            long lastCommand = 0;
            while(run == true){
                //Try firmware first
                long curTime = System.currentTimeMillis();
                if(sharedData.firmware == null && gcodeControl.isBusy() == false){
                    if(lastCommand + 3000 < curTime){
                        gcodeControl.command("M115");
                        lastCommand = curTime;
                    }
                    if(start + 12000 < curTime){
                        gcodeControl.disconnect();
                    }
                }else if(gcodeControl.isEmpty()){
                    //Get status loop
                    if (counter == 0)
                        gcodeControl.command("M114");
                    if (counter == 1)
                        gcodeControl.command("M105");
                    if (counter == 2) {
                        gcodeControl.command("M27");
                        counter = 0;
                    } else {
                        counter++;
                    }
                }
                try {
                    Thread.sleep(1800);
                } catch (InterruptedException e) {
//                    e.printStackTrace();
                }
            }
        }
    }

    private class GCodeListener implements GCodeControl.Listener{
        @Override
        public void onConnect(){
            //Ask machine info
            gcodeControl.command(" ");
            //Get linkbus info
            IUart dev = gcodeControl.getDevice();
            if(dev != null && dev instanceof Usart){
                sharedData.linkbusStatus[0] = true;
                try {
                    sharedData.linkbusStatus[1] = ((Usart) dev).isVTGOn();
                    sharedData.linkbusStatus[2] = ((Usart) dev).isResetOn();
                } catch (IOException e) {
                    sharedData.linkbusStatus[0] = false;
                }
            }
            refreshAll();
            timer = new LocalTimerLoop();
        }

        @Override
        public void onDisconnect() {
            sharedData.linkbusStatus[0] = false;
            sharedData.connect = false;
            sharedData.firmware=null;
            if(timer!=null)
                timer.interrupt();
            timer = null;
            refreshAll();
        }

        @Override
        public void onOpen(final boolean open) {
            if(open == false && machine != null)
                displayToast(getString(R.string.device_not_found));
        }

        @Override
        public void onDeviceException(final IOException exception) {
            if(exception != null)
                displayToast(exception.getMessage());
        }

        @Override
        public void onResend(int line) {}

        @Override
        public void onError(final String error) {
            if(sharedData.firmware != null)
                displayToast(error);
        }

        @Override
        public void onEcho(final String echo) {
            if(sharedData.firmware != null)
                displayToast(echo);
        }
        @Override
        public void onMessage(final String command, final String msg) {
            System.err.println(command+" : "+msg);
            boolean isdecoded = false;
            //Decode position
            try {
                Pattern pattern = Pattern.compile("^X:([0-9\\.\\-]+)(.*)Y:([0-9\\.\\-]+)(.*)Z:([0-9\\.\\-]+)(.*)E:([0-9\\.\\-]+)(.*)$");
                Matcher matcher = pattern.matcher(msg);
                if (matcher.matches()) {
                    sharedData.setPosition(Double.parseDouble(matcher.group(1)), Double.parseDouble(matcher.group(3)), Double.parseDouble(matcher.group(5)), Double.parseDouble(matcher.group(7)));
                    isdecoded = true;
                }
            }catch(Exception e){
            }
            //Decode temperature
            if(!isdecoded){
                try {
                    Pattern pattern = Pattern.compile("^(.*)T:([0-9\\.\\- ]+)\\/([0-9\\.\\- ]+)(.*)$");
                    Matcher matcher = pattern.matcher(msg);
                    Pattern pattern1 = Pattern.compile("^(.*)B:([0-9\\.\\- ]+)\\/([0-9\\.\\- ]+)(.*)$");
                    Matcher matcher1 = pattern1.matcher(msg);
                    if (matcher.matches() && matcher1.matches()) {
                        sharedData.setTemperatureExtruder(Double.parseDouble(matcher.group(2).trim()), Double.parseDouble(matcher.group(3).trim()));
                        sharedData.setTemperatureBed(Double.parseDouble(matcher1.group(2).trim()), Double.parseDouble(matcher1.group(3).trim()));
                        isdecoded = true;
                    }
                }catch(Exception e){
                }
            }
            //Decode temperature heating busy
            if(!isdecoded){
                double extTemp = extractTemperature(msg, "T");
                double bedTemp = extractTemperature(msg, "B");
                if(extTemp >= 0){
                    sharedData.setTemperatureExtruder(extTemp);
                }
                if(bedTemp >= 0) {
                    sharedData.setTemperatureBed(bedTemp);
                }
                if(extTemp >= 0 || bedTemp >= 0){
                    gcodeControl.commandBusy(3500);
                    isdecoded = true;
                }
            }
            //Decode SD status
            if(!isdecoded){
                if(msg != null){
                    if(msg.startsWith("Not")) {
                        sharedData.setSDStatus(0, 0);
                        isdecoded = true;
                    }else {
                        try {
                            Pattern pattern = Pattern.compile("^(.+) ([0-9]+)/([0-9]+)$");
                            Matcher matcher = pattern.matcher(msg);
                            if (matcher.matches()) {
                                int curPos = Integer.decode(matcher.group(2));
                                int total = Integer.decode(matcher.group(3));
                                sharedData.setSDStatus(curPos, total);
                                isdecoded = true;
                            }
                        } catch (Exception e) {
                        }
                    }
                }
            }
            if(!isdecoded && command!=null){
                if(command.equals("M115")) {
                    if (sharedData.firmware == null) {
                        sharedData.firmware = msg;
                        gcodeControl.command("M20");
                        isdecoded = true;
                    }
                }
                else if(command.equals("M20")){ //SD files list
                    if(msg != null) {
                        if (msg.equals("Begin file list")) {
                            sharedData.sdFiles.clear();
                            sharedData.sdFilesRecord = true;
                        } else if (msg.equals("End file list")) {
                            isdecoded = true;
                            sharedData.sdFilesRecord = false;
                        } else if(sharedData.sdFilesRecord == true) {
                            sharedData.sdFiles.add(msg.toLowerCase());
                        }
                    }
                }
                else if(command.startsWith("M23")) { //SD file selected
                    if (msg != null && msg.startsWith("File opened")){
                        String[] next = msg.split("File opened:");
                        if(next != null && next.length > 1){
                            next[1] = next[1].trim();
                            next = next[1].split(" ");
                            if(next != null && next.length > 0) {
                                isdecoded = true;
                                sharedData.setSDFileSelected(next[0].toLowerCase());
                            }
                        }
                    }
                }
                else if(msg.contains("stop"))
                    displayToast(msg);
            }
            if(isdecoded) {
                refreshAll();
            }
        }
        private double extractTemperature(String msg, String letter){
            double temp = -1.0;
            try {
                Pattern pattern = Pattern.compile("^"+ letter +":([0-9\\.\\- ]+)(.*)$");
                Matcher matcher = pattern.matcher(msg);
                if (matcher.matches()) {
                    temp = Double.parseDouble(matcher.group(1).trim());
                }else{
                    pattern = Pattern.compile("^(.*) "+ letter +":([0-9\\.\\- ]+)(.*)$");
                    matcher = pattern.matcher(msg);
                    if (matcher.matches()) {
                        temp = Double.parseDouble(matcher.group(2).trim());
                    }
                }
            }catch(Exception e){
            }
            return temp;
        }
    }

    private void refreshAll(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(axis != null)
                    axis.refresh();
                if(sdcard != null)
                    sdcard.refresh();
                try {
                    Thread.sleep(60);
                } catch (InterruptedException e) {
                //    e.printStackTrace();
                }
                if(machine != null)
                    machine.refresh();
            }
        });
    }

    private void displayToast(final String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
