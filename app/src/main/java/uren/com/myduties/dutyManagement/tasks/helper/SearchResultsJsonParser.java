package uren.com.myduties.dutyManagement.tasks.helper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import uren.com.myduties.dutyManagement.tasks.model.Highlight;
import uren.com.myduties.dutyManagement.tasks.model.HighlightedResult;
import uren.com.myduties.models.User;

public class SearchResultsJsonParser {
    private UserJsonParser userJsonParser = new UserJsonParser();

    /**
     * Parse the root result JSON object into a list of results.
     *
     * @param jsonObject The result's root object.
     * @return A list of results (potentially empty), or null in case of error.
     */
    public List<HighlightedResult<User>> parseResults(JSONObject jsonObject) {
        if (jsonObject == null) return null;

        List<HighlightedResult<User>> results = new ArrayList<>();
        JSONArray hits = jsonObject.optJSONArray("hits");
        if (hits == null) return null;

        for (int i = 0; i < hits.length(); ++i) {
            JSONObject hit = hits.optJSONObject(i);
            if (hit == null) continue;

            User user = userJsonParser.parse(hit);
            if (user == null) continue;

            /*JSONObject highlightResult = hit.optJSONObject("_highlightResult");
            if (highlightResult == null) continue;

            JSONObject highlightTitle = highlightResult.optJSONObject("title");
            if (highlightTitle == null) continue;

            String value = highlightTitle.optString("value");
            if (value == null) continue;*/

            JSONObject highlightResult = null;
            try {
                highlightResult = hit.optJSONObject("_highlightResult");
            } catch (Exception e) {
                e.printStackTrace();
            }

            JSONObject highlightTitle = null;
            try {
                highlightTitle = highlightResult.optJSONObject("title");
            } catch (Exception e) {
                e.printStackTrace();
            }

            String value = null;
            try {
                value = highlightTitle.optString("value");
            } catch (Exception e) {
                e.printStackTrace();
            }

            HighlightedResult<User> result = new HighlightedResult<>(user);
            result.addHighlight("title", new Highlight("title", value));
            results.add(result);
        }
        return results;
    }
}
