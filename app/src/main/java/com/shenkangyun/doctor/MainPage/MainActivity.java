package com.shenkangyun.doctor.MainPage;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.jaeger.library.StatusBarUtil;
import com.shenkangyun.doctor.BaseFolder.AppConst;
import com.shenkangyun.doctor.BaseFolder.Base;
import com.shenkangyun.doctor.BeanFolder.DeleteBean;
import com.shenkangyun.doctor.BeanFolder.PatientBean;
import com.shenkangyun.doctor.LoginPage.LoginActivity;
import com.shenkangyun.doctor.MainPage.adapter.PatientListAdapter;
import com.shenkangyun.doctor.PatientPage.PatientInfoActivity;
import com.shenkangyun.doctor.R;
import com.shenkangyun.doctor.UtilsFolder.ExampleUtil;
import com.shenkangyun.doctor.UtilsFolder.GsonCallBack;
import com.shenkangyun.doctor.UtilsFolder.LocalBroadcastManager;
import com.shenkangyun.doctor.UtilsFolder.NetworkChangeReceiver;
import com.shenkangyun.doctor.UtilsFolder.RecyclerViewDivider;
import com.zhy.http.okhttp.OkHttpUtils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolBar_title)
    TextView toolBarTitle;
    @BindView(R.id.toolBar)
    Toolbar toolBar;
    @BindView(R.id.patientRecycler)
    RecyclerView patientRecycler;
    @BindView(R.id.nav_view)
    NavigationView navView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.easyLayout)
    SwipeRefreshLayout easyLayout;
    private IntentFilter intentFilter;
    private NetworkChangeReceiver changeReceiver;
    private SharedPreferences sp;
    public static boolean isForeground = false;

    private MessageReceiver mMessageReceiver;
    public static final String MESSAGE_RECEIVED_ACTION = "cn.jpush.android.intent.NOTIFICATION_RECEIVED";
    public static final String KEY_TITLE = "title";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_EXTRAS = "extras";

    private List<PatientBean.DataBean.ListBean> totalList = new ArrayList<>();
    private LinearLayoutManager manager;
    private PatientListAdapter patientListAdapter;

    private int pageNo = 0;
    private int pageCount = 10;

    private int size;
    private String idCard;
    private String name;
    private int age;
    private String birthday;
    private String sex;
    private String mobile;
    private String num;
    private int id;
    private String loginName;
    private long createTime;
    private long updateTime;
    private String provinceID;
    private String cityID;
    private String regionID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        StatusBarUtil.setColor(this, getResources().getColor(R.color.home_red));
        registerMessageReceiver();
        setToolBar();
        initView();
        initData();
        initRefresh();
    }

    private void initView() {
        sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        patientListAdapter = new PatientListAdapter();
        manager = new LinearLayoutManager(MainActivity.this);
        patientRecycler.addItemDecoration(new RecyclerViewDivider(this, LinearLayoutManager.VERTICAL,
                10, getResources().getColor(R.color.white)));
        patientRecycler.setLayoutManager(manager);
        patientRecycler.setAdapter(patientListAdapter);
    }

    //设置标题栏toolBar
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setToolBar() {
        setSupportActionBar(toolBar);   //必须使用
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
        //添加左边图标点击事件
        toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        navView.setCheckedItem(R.id.nav_info);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_info:
                        Intent intentPersonal = new Intent(MainActivity.this, PersonalActivity.class);
                        startActivity(intentPersonal);
                        break;
                    case R.id.nav_exit:
                        SharedPreferences.Editor edit = sp.edit();
                        edit.putString(AppConst.LOGING_PASSWORD, "");
                        edit.commit();
                        Intent intentExit = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intentExit);
                        finish();
                        break;
                }
                drawerLayout.closeDrawers();
                return true;
            }
        });
        //添加menu项点击事件
        toolBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.toolbar_add:
                        Intent intentAdd = new Intent(MainActivity.this, AddPatientActivity.class);
                        startActivityForResult(intentAdd, 1);
                        break;
                    case R.id.toolbar_search:
                        Intent intentSearch = new Intent(MainActivity.this, SearchActivity.class);
                        startActivity(intentSearch);
                        break;
                    case R.id.toolbar_science:
                        Intent intentScience = new Intent(MainActivity.this, ScienceActivity.class);
                        startActivity(intentScience);
                        break;
                }
                return true;    //返回为true
            }
        });
    }

    //设置menu（右边图标）
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu); //解析menu布局文件到menu
        return true;
    }

    private void initData() {
        pageNo = 0;
        pageCount = 10;
        totalList.clear();
        final List<PatientBean.DataBean.ListBean> PatientList = new ArrayList<>();
        OkHttpUtils.post()
                .url(Base.URL)
                .addParams("act", "patientslist")
                .addParams("data", new patientslist(Base.appKey, Base.timeSpan, "1", "2", String.valueOf(pageNo), String.valueOf(pageCount),
                        "", "").toJson())
                .build()
                .execute(new GsonCallBack<PatientBean>() {
                    @Override
                    public void onSuccess(PatientBean response) {
                        size = response.getData().getList().size();
                        for (int i = 0; i < response.getData().getList().size(); i++) {
                            PatientBean.DataBean.ListBean listBean = new PatientBean.DataBean.ListBean();
                            age = response.getData().getList().get(i).getAge();
                            birthday = response.getData().getList().get(i).getBrithday();
                            id = response.getData().getList().get(i).getId();
                            idCard = response.getData().getList().get(i).getIdCard();
                            mobile = response.getData().getList().get(i).getMobile();
                            name = response.getData().getList().get(i).getName();
                            num = response.getData().getList().get(i).getNum();
                            sex = response.getData().getList().get(i).getSex();
                            cityID = response.getData().getList().get(i).getCityID();
                            regionID = response.getData().getList().get(i).getRegionID();
                            loginName = response.getData().getList().get(i).getLoginName();
                            provinceID = response.getData().getList().get(i).getProvinceID();
                            createTime = response.getData().getList().get(i).getCreateTime();
                            updateTime = response.getData().getList().get(i).getUpdateTime();
                            listBean.setAge(age);
                            listBean.setBrithday(birthday);
                            listBean.setId(id);
                            listBean.setIdCard(idCard);
                            listBean.setMobile(mobile);
                            listBean.setName(name);
                            listBean.setNum(num);
                            listBean.setSex(sex);
                            listBean.setLoginName(loginName);
                            listBean.setCreateTime(createTime);
                            listBean.setUpdateTime(updateTime);
                            listBean.setProvinceID(provinceID);
                            listBean.setCityID(cityID);
                            listBean.setRegionID(regionID);

                            PatientList.add(listBean);
                            totalList.add(listBean);
                        }
                        patientListAdapter.setNewData(PatientList);
                        if (easyLayout.isRefreshing()) {
                            easyLayout.setRefreshing(false);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                    }
                });
        initLoadMore();
        initClick();
    }

    private void initLoadMore() {
        patientListAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                patientRecycler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        final List<PatientBean.DataBean.ListBean> PatientList = new ArrayList<>();
                        if (!(size < pageCount)) {
                            pageNo = pageNo + size;
                            OkHttpUtils.post().url(Base.URL)
                                    .addParams("act", "patientslist")
                                    .addParams("data", new patientslist(Base.getMD5Str(), Base.getTimeSpan(), "1", "2", String.valueOf(pageNo), String.valueOf(pageCount)
                                            , "", "").toJson())
                                    .build().execute(new GsonCallBack<PatientBean>() {
                                @Override
                                public void onSuccess(final PatientBean response) throws JSONException {
                                    size = response.getData().getList().size();
                                    for (int i = 0; i < response.getData().getList().size(); i++) {
                                        PatientBean.DataBean.ListBean listBean = new PatientBean.DataBean.ListBean();
                                        age = response.getData().getList().get(i).getAge();
                                        birthday = response.getData().getList().get(i).getBrithday();
                                        id = response.getData().getList().get(i).getId();
                                        idCard = response.getData().getList().get(i).getIdCard();
                                        mobile = response.getData().getList().get(i).getMobile();
                                        name = response.getData().getList().get(i).getName();
                                        num = response.getData().getList().get(i).getNum();
                                        sex = response.getData().getList().get(i).getSex();
                                        cityID = response.getData().getList().get(i).getCityID();
                                        regionID = response.getData().getList().get(i).getRegionID();
                                        loginName = response.getData().getList().get(i).getLoginName();
                                        provinceID = response.getData().getList().get(i).getProvinceID();
                                        createTime = response.getData().getList().get(i).getCreateTime();
                                        updateTime = response.getData().getList().get(i).getUpdateTime();
                                        listBean.setAge(age);
                                        listBean.setBrithday(birthday);
                                        listBean.setId(id);
                                        listBean.setIdCard(idCard);
                                        listBean.setMobile(mobile);
                                        listBean.setName(name);
                                        listBean.setNum(num);
                                        listBean.setSex(sex);
                                        listBean.setLoginName(loginName);
                                        listBean.setCreateTime(createTime);
                                        listBean.setUpdateTime(updateTime);
                                        listBean.setProvinceID(provinceID);
                                        listBean.setCityID(cityID);
                                        listBean.setRegionID(regionID);

                                        PatientList.add(listBean);
                                        totalList.add(listBean);
                                    }
                                    patientListAdapter.addData(PatientList);
                                    patientListAdapter.loadMoreComplete();
                                }

                                @Override
                                public void onError(Exception e) {

                                }
                            });
                        } else {
                            patientListAdapter.loadMoreEnd();
                        }
                    }
                }, 2000);

            }
        }, patientRecycler);
    }

    private void initRefresh() {
        easyLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initData();
            }
        });
    }

    private void initClick() {
        patientListAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                switch (view.getId()) {
                    case R.id.content:
                        Intent intent = new Intent(MainActivity.this, PatientInfoActivity.class);
                        intent.putExtra("id", totalList.get(position).getId());
                        intent.putExtra("idCard", totalList.get(position).getIdCard());
                        intent.putExtra("name", totalList.get(position).getName());
                        intent.putExtra("age", totalList.get(position).getAge());
                        intent.putExtra("sex", totalList.get(position).getSex());
                        intent.putExtra("birthday", totalList.get(position).getBrithday());
                        intent.putExtra("mobile", totalList.get(position).getMobile());
                        startActivity(intent);
                        break;
                    case R.id.right:
                        initDelete(position);
                        patientListAdapter.remove(position);
                        patientListAdapter.notifyDataSetChanged();
                        break;
                }
            }
        });
    }

    private void initDelete(int position) {
        int idDel = totalList.get(position).getId();
        OkHttpUtils.post().url(Base.URL)
                .addParams("act", "DeletePatient")
                .addParams("data", new DeletePatient(Base.getMD5Str(), Base.getTimeSpan(), "1", "2", String.valueOf(idDel)).toJson())
                .build().execute(new GsonCallBack<DeleteBean>() {
            @Override
            public void onSuccess(DeleteBean response) throws JSONException {
                String status = response.getStatus();
                if ("0".equals(status)) {
                    Toast.makeText(MainActivity.this, response.getData().getData(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, response.getData().getData(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    public void registerMessageReceiver() {
        intentFilter = new IntentFilter();
        changeReceiver = new NetworkChangeReceiver();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(changeReceiver, intentFilter);

        mMessageReceiver = new MessageReceiver();
        intentFilter = new IntentFilter();
        intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        intentFilter.addAction(MESSAGE_RECEIVED_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, intentFilter);
    }

    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {
                    String messge = intent.getStringExtra(KEY_MESSAGE);
                    String extras = intent.getStringExtra(KEY_EXTRAS);
                    Log.i("JIGUANG-Example", "onReceive: " + messge);
                    StringBuilder showMsg = new StringBuilder();
                    showMsg.append(KEY_MESSAGE + " : " + messge + "\n");
                    if (!ExampleUtil.isEmpty(extras)) {
                        showMsg.append(KEY_EXTRAS + " : " + extras + "\n");
                        Log.i("1234567", "onReceive: " + showMsg.toString());
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(changeReceiver);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == 1) {
            finish();
        }
        if (requestCode == 1 && resultCode == 2) {
            initData();
        }
    }

    static class patientslist {
        private String appKey;
        private String timeSpan;
        private String mobileType;
        private String appType;
        private String pageNo;
        private String pageCount;
        private String name;
        private String diseasesID;

        public patientslist(String appKey, String timeSpan, String mobileType, String appType, String pageNo,
                            String pageCount, String name, String diseasesID) {
            this.appKey = appKey;
            this.timeSpan = timeSpan;
            this.mobileType = mobileType;
            this.appType = appType;
            this.pageNo = pageNo;
            this.pageCount = pageCount;
            this.name = name;
            this.diseasesID = diseasesID;
        }

        public String toJson() {
            return new Gson().toJson(this);
        }
    }

    static class DeletePatient {
        private String appKey;
        private String timeSpan;
        private String mobileType;
        private String appType;
        private String id;

        public DeletePatient(String appKey, String timeSpan, String mobileType, String appType, String id) {
            this.appKey = appKey;
            this.timeSpan = timeSpan;
            this.mobileType = mobileType;
            this.appType = appType;
            this.id = id;
        }

        public String toJson() {
            return new Gson().toJson(this);
        }
    }
}
