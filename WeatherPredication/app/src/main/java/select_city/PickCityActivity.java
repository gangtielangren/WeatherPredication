package select_city;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.github.promeg.pinyinhelper.Pinyin;
import com.weather.douchengfeng.weatherpredication.R;

import java.util.LinkedList;
import java.util.List;

import app.WeatherPredication;
import model.CityEntity;
import select_city.adapter.CitiesAdapter;
import select_city.bean.CitiesBean;
import select_city.view.QuickIndexView;

/**
 * Created by kun on 2016/10/26.
 */
public class PickCityActivity extends Activity {

    private QuickIndexView quickIndexView;
    private RecyclerView recyclerView;
    private CitiesAdapter adapter;
    private List<CityEntity> cityEntities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pick_city);
        initView();
        initEvent();

    }

    private void initView() {
        ImageView back = findViewById(R.id.back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("cityCode", "null");
                setResult(2, intent);
                finish();
            }
        });

        quickIndexView = findViewById(R.id.quickIndexView);

        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        cityEntities = ((WeatherPredication) getApplication()).getCityList();
        String cityName = getIntent().getStringExtra("currentCity");
        ((TextView) findViewById(R.id.cur_city)).setText(String.format("当前城市%s", cityName));

        setAdapter(cityEntities);
    }

    private void setAdapter(List<CityEntity> cityEntities) {
        adapter = new CitiesAdapter(this, entity2DataBean(cityEntities));
        recyclerView.setAdapter(adapter);
    }

    private List<CitiesBean.DataBean> entity2DataBean(List<CityEntity> cityEntities) {
        List<CitiesBean.DataBean> dataBeans = new LinkedList<>();

        String preAlf = "";
        CitiesBean.DataBean curBean;
        List<CitiesBean.DataBean.AddressListBean> addressListBeans = new LinkedList<>();

        for (CityEntity cityEntity : cityEntities) {
            String curAlf = cityEntity.getPinyin();
            if (!curAlf.equals(preAlf)) {
                curBean = new CitiesBean.DataBean();
                curBean.setAlifName(curAlf);

                addressListBeans = new LinkedList<>();
                curBean.setAddressList(addressListBeans);
                dataBeans.add(curBean);
                preAlf = curAlf;
            }

            CitiesBean.DataBean.AddressListBean addressListBean = new CitiesBean.DataBean.AddressListBean();
            addressListBean.setId(Integer.parseInt(cityEntity.getCityCode()));
            addressListBean.setName(cityEntity.getCityName());

            addressListBeans.add(addressListBean);
        }

        return dataBeans;
    }

    private void initEvent() {
        quickIndexView.setOnIndexChangeListener(new QuickIndexView.OnIndexChangeListener() {
            @Override
            public void onIndexChange(String words) {
                List<CitiesBean.DataBean> data = adapter.getData();
                if (data != null && data.size() > 0) {
                    int count = 0;
                    for (CitiesBean.DataBean dataBean : data) {
                        if (dataBean.getAlifName().equals(words)) {
                            LinearLayoutManager llm = (LinearLayoutManager) recyclerView
                                    .getLayoutManager();
                            llm.scrollToPositionWithOffset(count + 1, 0);
                            return;
                        }
                        count += dataBean.getAddressList().size() + 1;
                    }
                }
            }
        });


        SearchView searchView = findViewById(R.id.search_city);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                newText = newText.replace(" ", "");

                if (newText.length() == 0) {
                    setAdapter(cityEntities);
                    return true;
                }


                List<CityEntity> target = new LinkedList<>();
                for (CityEntity entity : cityEntities) {
                    if (matchName(newText, entity.getCityName())) {
                        target.add(entity);
                    }
                }

                setAdapter(target);

                return true;
            }

            private boolean matchName(String text, String cityName) {
                text = text.toLowerCase();
                if (cityName.contains(text)) {
                    return true;
                }

                for (int i = 0; i < text.length(); i++) {
                    if (text.charAt(i) == cityName.charAt(i)) { // 如果字符相同
                        continue;
                    }

                    String pinyin = Pinyin.toPinyin(cityName.charAt(i)).toLowerCase();

                    if (text.startsWith(pinyin)) { // 如果包含拼音
                        text = text.substring(pinyin.length());
                        continue;
                    }

                    if (text.charAt(i) <= 'z' && text.charAt(i) >= 'a') { // 如果包含拼音首字母
                        if (text.charAt(i) == pinyin.charAt(0)) {
                            continue;
                        }
                    }

                    return false;
                }

                return true;
            }
        });
    }
}