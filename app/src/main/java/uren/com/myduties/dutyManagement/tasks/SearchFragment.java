package uren.com.myduties.dutyManagement.tasks;


import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import uren.com.myduties.R;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.dutyManagement.NextActivity;
import uren.com.myduties.dutyManagement.tasks.adapters.SearchResultAdapter;
import uren.com.myduties.dutyManagement.tasks.helper.SearchResultsJsonParser;
import uren.com.myduties.dutyManagement.tasks.model.HighlightedResult;
import uren.com.myduties.models.Friend;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.ClickableImage.ClickableImageView;
import uren.com.myduties.utils.CommonUtils;

import static uren.com.myduties.constants.StringConstants.ALGOLIA_APP_ID;
import static uren.com.myduties.constants.StringConstants.ALGOLIA_INDEX_NAME;
import static uren.com.myduties.constants.StringConstants.ALGOLIA_SEARCH_API_KEY;
import static uren.com.myduties.constants.StringConstants.fb_child_email;
import static uren.com.myduties.constants.StringConstants.fb_child_name;
import static uren.com.myduties.constants.StringConstants.fb_child_profilePhotoUrl;
import static uren.com.myduties.constants.StringConstants.fb_child_username;

public class SearchFragment extends BaseFragment {

    View mView;

    @BindView(R.id.searchEdittext)
    EditText searchEdittext;
    @BindView(R.id.search_recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.commonToolbarbackImgv)
    ClickableImageView commonToolbarbackImgv;
    @BindView(R.id.toolbarTitleTv)
    AppCompatTextView toolbarTitleTv;
    @BindView(R.id.searchCancelImgv)
    ImageView searchCancelImgv;
    @BindView(R.id.searchResultTv)
    AppCompatTextView searchResultTv;

    Index index;

    SearchResultAdapter searchResultAdapter;
    List<Friend> friendList;

    private static final int LOAD_MORE_THRESHOLD = 5;
    private static final int HITS_PER_PAGE = 20;

    public SearchFragment() {
    }

    @Override
    public void onStart() {
        Objects.requireNonNull(getActivity()).findViewById(R.id.tabMainLayout).setVisibility(View.GONE);
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

        commonToolbarbackImgv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtils.showKeyboard(getContext(), false, searchEdittext);
                Objects.requireNonNull(getActivity()).onBackPressed();
            }
        });

        searchCancelImgv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEdittext.setText("");
                searchCancelImgv.setVisibility(View.GONE);
                CommonUtils.showKeyboard(getContext(), false, searchEdittext);
            }
        });

        searchEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s != null && s.toString() != null) {
                    if (!s.toString().trim().isEmpty()) {
                        searchCancelImgv.setVisibility(View.VISIBLE);
                    } else {
                        searchCancelImgv.setVisibility(View.GONE);
                    }
                } else {
                    searchCancelImgv.setVisibility(View.GONE);
                }

                if (s != null && s.toString() != null) {
                    Query query = new Query(s.toString())
                            .setAttributesToRetrieve(fb_child_name, fb_child_username, fb_child_profilePhotoUrl, fb_child_email)
                            .setHitsPerPage(50);
                    index.searchAsync(query, new CompletionHandler() {
                        @Override
                        public void requestCompleted(JSONObject content, AlgoliaException error) {
                            SearchResultsJsonParser resultsParser = new SearchResultsJsonParser();
                            List<HighlightedResult<User>> resultList = resultsParser.parseResults(content);
                            updateAdapterItems(resultList);
                        }
                    });
                }
            }
        });
    }

    private void updateAdapterItems(List<HighlightedResult<User>> resultList) {
        friendList.clear();

        for (HighlightedResult<User> result : resultList) {
            User user = result.getResult();
            friendList.add(new Friend(user, ""));
        }

        searchResultAdapter.updateListItems(friendList);

        if (friendList.size() == 0)
            searchResultTv.setVisibility(View.VISIBLE);
        else
            searchResultTv.setVisibility(View.GONE);
    }

    private void initVariables() {
        searchEdittext.requestFocus();
        searchEdittext.setHint(getContext().getResources().getString(R.string.searchUser));
        searchResultTv.setText(getContext().getResources().getString(R.string.USER_NOT_FOUND));
        friendList = new ArrayList<>();
        toolbarTitleTv.setText(getContext().getResources().getString(R.string.searchUsers));
        CommonUtils.showKeyboard(getContext(), true, searchEdittext);
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
        searchResultAdapter = new SearchResultAdapter(getContext(), mFragmentNavigation);
        searchResultAdapter.setHasStableIds(true);
        recyclerView.setAdapter(searchResultAdapter);
        recyclerView.getItemAnimator().setChangeDuration(0);
    }

    private void setupAlgoliaSearch() throws JSONException {

        Client client = new Client(ALGOLIA_APP_ID, ALGOLIA_SEARCH_API_KEY);
        index = client.getIndex(ALGOLIA_INDEX_NAME);

        JSONObject settings = new JSONObject()
                .put("searchableAttributes", fb_child_name)
                .put("searchableAttributes", fb_child_username);
        index.setSettingsAsync(settings, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((NextActivity) Objects.requireNonNull(getActivity())).ANIMATION_TAG = null;
    }
}
