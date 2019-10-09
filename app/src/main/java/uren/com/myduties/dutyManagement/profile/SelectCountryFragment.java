package uren.com.myduties.dutyManagement.profile;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uren.com.myduties.R;
import uren.com.myduties.interfaces.ItemClickListener;
import uren.com.myduties.models.Country;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.ShapeUtil;

public class SelectCountryFragment extends Fragment implements Filterable {

    View mView;

    @BindView(R.id.selectCountryEt)
    EditText selectCountryEt;
    @BindView(R.id.countryListView)
    ListView countryListView;
    @BindView(R.id.mainLinearLayout)
    LinearLayout mainLinearLayout;

    ArrayAdapter<String> countryAdapter;
    List<String> orgCountryList;
    List<String> countryList;
    ItemClickListener listener;
    //CountryListResponse countryListResponse;
    Country myCountry;
    Country[] countries;

    public SelectCountryFragment(ItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_select_country, container, false);
            ButterKnife.bind(this, mView);
        }

        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
        addListeners();
        getCountryList();
    }

    private void init() {
        setShapes();
    }

    private void setShapes() {
        GradientDrawable shape = ShapeUtil.getShape(getActivity().getResources().getColor(R.color.White),
                0, GradientDrawable.RECTANGLE, 15, 0);
        mainLinearLayout.setBackground(shape);
    }

    public void addListeners() {
        selectCountryEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                countryAdapter.getFilter().filter(s.toString().trim());
            }
        });

        countryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedCountry = (String) countryListView.getItemAtPosition(position);
                parseSelectedCountry(selectedCountry);
                getActivity().onBackPressed();
            }
        });
    }

    public void parseSelectedCountry(String selectedCountry) {
        String[] parts = selectedCountry.split("\\(");

        String[] parts2 = parts[1].split("\\)");
        String countryDialCode = parts2[0];

        for (Country country : countries) {
            if (country != null && country.getDialCode() != null && !country.getDialCode().trim().isEmpty()) {
                if (countryDialCode.trim().equals(country.getDialCode())) {
                    myCountry = country;
                    break;
                }
            }
        }
        listener.onClick(myCountry, 0);
    }

    public void getCountryList() {

        String countryCodesStr = CommonUtils.readCountryCodes(getContext());
        Gson gson = new Gson();
        countries = gson.fromJson(countryCodesStr, Country[].class);
        fillCountryList();
    }

    public void fillCountryList() {
        orgCountryList = new ArrayList<String>();

        for (Country country : countries) {
            String countryItem = "";

            if (country.getName() != null && !country.getName().trim().isEmpty() &&
                    country.getDialCode() != null && !country.getDialCode().trim().isEmpty()) {
                countryItem = country.getName() + "(" + country.getDialCode() + ")";
                orgCountryList.add(countryItem);
            }
        }

        countryAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                orgCountryList);

        countryListView.setAdapter(countryAdapter);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String searchString = charSequence.toString();
                if (searchString.trim().isEmpty())
                    countryList = orgCountryList;
                else {
                    List<String> tempCountryList = new ArrayList<>();

                    for (String country : orgCountryList) {
                        if (country.toLowerCase().contains(searchString.toLowerCase()))
                            tempCountryList.add(country);
                    }
                    countryList = tempCountryList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = countryList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                countryList = (List<String>) filterResults.values;
                countryAdapter.notifyDataSetChanged();
            }
        };
    }
}
