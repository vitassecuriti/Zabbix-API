package application.utils;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by VSKryukov on 03.02.2016.
 */
public class Utils {

    public static class getGraphsElemets {

        public static Integer getGraphIdByName (JSONObject response, String graphName){


            for (int i=0; i < response.getJSONArray("result").size(); i++){

                if (response.getJSONArray("result").getJSONObject(i).getString("name").equals(graphName)){

                    return Integer.parseInt(response.getJSONArray("result").getJSONObject(i).getString("graphid"));
                }
            }

            return 0;
        }
    }
}
