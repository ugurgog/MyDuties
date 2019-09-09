package uren.com.myduties.dutyManagement.tasks;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import uren.com.myduties.R;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.dutyManagement.NextActivity;
import uren.com.myduties.dutyManagement.tasks.adapters.SearchResultAdapter;
import uren.com.myduties.dutyManagement.tasks.helper.SearchResultsJsonParser;
import uren.com.myduties.dutyManagement.tasks.helper.UserJsonParser;
import uren.com.myduties.dutyManagement.tasks.model.HighlightedResult;
import uren.com.myduties.models.User;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;
import com.algolia.search.saas.RequestOptions;

import org.json.JSONException;
import org.json.JSONObject;

import static uren.com.myduties.constants.StringConstants.ALGOLIA_APP_ID;
import static uren.com.myduties.constants.StringConstants.ALGOLIA_INDEX_NAME;
import static uren.com.myduties.constants.StringConstants.ALGOLIA_SEARCH_API_KEY;
import static uren.com.myduties.constants.StringConstants.fb_child_name;
import static uren.com.myduties.constants.StringConstants.fb_child_username;
import static uren.com.myduties.constants.StringConstants.fb_child_users;

public class SearchFragment extends BaseFragment {

    View mView;

    @BindView(R.id.editTextSearch)
    EditText editTextSearch;
    @BindView(R.id.txtResult)
    TextView txtResult;
    @BindView(R.id.imgCancelSearch)
    ImageView imgCancelSearch;
    @BindView(R.id.searchToolbarBackImgv)
    ImageView searchToolbarBackImgv;
    @BindView(R.id.search_recyclerView)
    RecyclerView recyclerView;

    Index index;
    CompletionHandler completionHandler;

    SearchResultAdapter searchResultAdapter;


    private static final int LOAD_MORE_THRESHOLD = 5;
    private static final int HITS_PER_PAGE = 20;

    public SearchFragment() {
    }

    @Override
    public void onStart() {
        Objects.requireNonNull(getActivity()).findViewById(R.id.tabMainLayout).setVisibility(View.VISIBLE);
        super.onStart();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_search, container, false);
            ButterKnife.bind(this, mView);
            initVariables();
            initListeners();
            try {
                setupAlgoliaSearch();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        return mView;
    }

    private void initListeners() {
        editTextSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextSearch.requestFocus();
                showKeyboard(true);
            }
        });

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s != null && s.toString() != null && !s.toString().isEmpty()) {
                    imgCancelSearch.setVisibility(View.VISIBLE);
                    searchToolbarBackImgv.setVisibility(View.GONE);
                } else {
                    imgCancelSearch.setVisibility(View.GONE);
                    searchToolbarBackImgv.setVisibility(View.VISIBLE);
                }

                if(s!= null && s.toString() != null){
                    Query query = new Query(s.toString())
                            .setAttributesToRetrieve(fb_child_name, fb_child_username)
                            .setHitsPerPage(50);
                    index.searchAsync(query, new CompletionHandler() {
                        @Override
                        public void requestCompleted(JSONObject content, AlgoliaException error) {
                            Log.i("jsonObject:" , content.toString());

                            SearchResultsJsonParser resultsParser = new SearchResultsJsonParser();
                            List<HighlightedResult<User>> resultList = resultsParser.parseResults(content);

                            // TODO: 2019-09-09 devam edecegiz 
                            //User user = UserJsonParser.parse(content);
                            Log.i("user:" , "");
                        }
                    });
                }
            }
        });
    }

    private void initVariables() {
        editTextSearch.requestFocus();
        showKeyboard(true);
        initRecyclerView();
    }

    private void initRecyclerView() {
        setLayoutManager();
        setAdapter();
    }

    private void setLayoutManager() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
    }
    private void setAdapter() {
        searchResultAdapter = new SearchResultAdapter(getContext());
        recyclerView.setAdapter(searchResultAdapter);
    }

    private void setupAlgoliaSearch() throws JSONException {

        Client client = new Client(ALGOLIA_APP_ID, ALGOLIA_SEARCH_API_KEY);
        index = client.getIndex(ALGOLIA_INDEX_NAME);

        JSONObject settings = new JSONObject()
                .put("searchableAttributes", fb_child_name)
                .put("searchableAttributes", fb_child_username);
        index.setSettingsAsync(settings, null);
    }

    private void showKeyboard(boolean showKeyboard) {

        if (showKeyboard) {
            InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getContext()).getSystemService(Context.INPUT_METHOD_SERVICE);
            Objects.requireNonNull(imm).toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        } else {
            InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getContext()).getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            Objects.requireNonNull(imm).hideSoftInputFromWindow(editTextSearch.getWindowToken(), 0);
            editTextSearch.setFocusable(false);
            editTextSearch.setFocusableInTouchMode(true);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((NextActivity) Objects.requireNonNull(getActivity())).ANIMATION_TAG = null;
    }
}
