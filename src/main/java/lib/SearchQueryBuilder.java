package lib;

/**
 * Created by anupama.agarwal on 09/01/17.
 */
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Created by gaurav.gupta on 31/05/16.
 */
public class SearchQueryBuilder {

    private ArrayList<String> condition = new ArrayList<String>();
    private ArrayList<String> orConditon = new ArrayList<String>();

    public String  construct(HashMap<String,Object> queryParam) {
        condition = new ArrayList<String>();
        orConditon = new ArrayList<String>();
        HashMap<String, Object> orMap = new HashMap<String, Object>();
        if (queryParam.containsKey("negate")) {
            constructNegateCondition(queryParam.get("negate"));
            queryParam.remove("negate");
        }
        if(queryParam.containsKey("or")) {
            orMap = (HashMap<String, Object>) queryParam.get("or");
            queryParam.remove("or");
        }
        constructCondition(queryParam);
        String queryString = "";
        for(String element : condition) {
            queryString += element + " and ";
        }
        String andConditionString = queryString.substring(0, queryString.length() - 5);
        String orConditionString = "";
        if(orMap.size() >= 1){
            condition = new ArrayList<String>();
            orConditon = new ArrayList<String>();
            constructCondition(orMap);
            for(String element : condition) {
                orConditionString +=  " or " + element;
            }

        }
        return "( " + andConditionString  + orConditionString + " )";
    }


    private void constructCondition(HashMap<String, Object> paramHash) {
        for (Entry<String, Object> params : paramHash.entrySet()) {
            String key = params.getKey();
            Object value = params.getValue();
            ArrayList<String> valueList;
            if(value instanceof ArrayList) {
                valueList = (ArrayList<String>) value;
                buildQueryFromListAndCommaSeparate(key,valueList,"IN");
            } else {
                String valueString = (String) value;
                valueList = new ArrayList<String>();
                for (String re : valueString.split(","))
                    valueList.add(re);
                if (valueList.size() > 1)
                    buildQueryFromListAndCommaSeparate(key, valueList, "IN");
                else if (valueString.equals("NULL"))
                    condition.add(key + " IS  NULL");
                else if (valueString.equals("NOTNULL"))
                    condition.add(key + " IS NOT NULL");
                else if (valueString.indexOf("..") != -1) {
                    constructRangeCondition(key, valueString, false);
                }
                else {
                    try {
                        constructDateCondition(key, valueString, false);
                    } catch (ParseException e) {
                        condition.add(key + " = '" + valueString + "'");
                    }
                }
            }
        }
    }

    private void constructNegateCondition(Object queryParam){

        if(queryParam instanceof HashMap) {
            HashMap<String,Object> paramHash = ( HashMap<String,Object>) queryParam;
            for (Entry<String, Object> params : paramHash.entrySet()) {
                String key = params.getKey();
                Object value = params.getValue();
                ArrayList<String> valueList;
                if(value instanceof ArrayList) {
                    valueList = (ArrayList<String>) value;
                    buildQueryFromListAndCommaSeparate(key,valueList,"NOT IN");
                } else {
                    String valueString = (String) value;
                    valueList = new ArrayList<String>();
                    for (String re: valueString.split(","))
                        valueList.add(re);
                    if(valueList.size() > 1) {
                        buildQueryFromListAndCommaSeparate(key, valueList, "NOT IN");
                    }
                    else if(valueString.equals("NULL"))
                        condition.add(key +" IS NOT NULL");
                    else if(valueString.equals("NOTNULL"))
                        condition.add(key + " IS NULL");
                    else if(valueString.indexOf("..") != -1)
                        constructRangeCondition(key,valueString,true);
                    else {
                        try {
                            constructDateCondition(key, valueString, true);
                        } catch (ParseException e) {
                            condition.add(key + " <> '" + valueString + "'");
                        }
                    }
                }

            }
        }
    }


    private void buildQueryFromListAndCommaSeparate(String key, ArrayList<String> valueList, String negate) {
        String query = key + " "+ negate + " (";
        for(String element : valueList) {
            query += "'" + element + "',";
        }
        condition.add(query.substring(0, query.length() - 1) + ")");
    }
    private void constructRangeCondition(String key, String valueString, boolean negate) {
        ArrayList<String> valueList = new ArrayList<String>();
        for (String re: valueString.split("\\.."))
            valueList.add(re);

        if(valueList.size() <= 0 || valueList.size() > 2)
            return;

        String minValue = valueList.get(0);
        String maxValue = valueList.size() == 1 ? "NULL" : valueList.get(1);
        if(minValue != "NULL") {
            try {
                constructDateMinRangeCondition(key, minValue, negate);
            } catch (ParseException e) {
                if (negate)
                    orConditon.add(key + " < '" + minValue + "'");
                else
                    condition.add(key + " >= '" + minValue + "'");
            }
        }

        if(maxValue != "NULL") {
            try {
                constructDateMaxRangeCondition(key, maxValue, negate);
            } catch (ParseException e) {
                if (negate)
                    orConditon.add(key + " > '" + maxValue + "'");
                else
                    condition.add(key + " <= '" + maxValue + "'");
            }
        }

        if(negate) {
            String orString = "(";
            for(String element : orConditon) {
                orString += element + " or ";
            }
            condition.add(orString.substring(0, orString.length() - 4) + " )");
            orConditon.clear();
        }
    }

    private void constructDateMinRangeCondition(String key, String value, boolean negate) throws ParseException{
        ArrayList<String> dateList = new ArrayList<String>();
        for (String re: value.split("T"))
            dateList.add(re);

        if(dateList.size() > 1) {
            String date = formatDate(value,"yyyy-MM-dd'T'HH:mm:ss",false);
            if(negate)
                orConditon.add(key + " < '" + date + "'");
            else
                condition.add(key + " >= '" + date + "'");
        }
        else {
            String date = formatDate(value,"yyyy-MM-dd",false);
            if(negate)
                orConditon.add(key + " < '" + date + "'");
            else
                condition.add(key + " >= '" + date + "'");
        }
    }

    private void constructDateMaxRangeCondition(String key, String value, boolean negate) throws ParseException{
        ArrayList<String> dateList = new ArrayList<String>();
        for (String re: value.split("T"))
            dateList.add(re);

        if(dateList.size() > 1) {
            String date = formatDate(value,"yyyy-MM-dd'T'HH:mm:ss",false);
            if(negate)
                orConditon.add(key + " > '" + date + "'");
            else
                condition.add(key + " <= '" + date + "'");
        }
        else {
            String date = formatDate(value,"yyyy-MM-dd",true);
            if(negate)
                orConditon.add(key + " >= '" + date + "'");
            else
                condition.add(key + " < '" + date + "'");
        }
    }

    private String formatDate(String value, String Format, boolean plusOne) throws ParseException{
        SimpleDateFormat dateFormat = new SimpleDateFormat(Format);
        Date date = dateFormat.parse(value);
        if(plusOne) {
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.add(Calendar.DATE, 1);
            return dateFormat.format(c.getTime());
        }
        return dateFormat.format(date);
    }

    private void constructDateCondition(String key, String value, boolean negate)  throws ParseException{
        ArrayList<String> dateList = new ArrayList<String>();
        for (String re: value.split("T"))
            dateList.add(re);

        if(dateList.size() > 1) {
            String date = formatDate(value,"yyyy-MM-dd'T'HH:mm:ss",false);
            if(negate)
                condition.add(key +  " <> '" + date + "'");
            else
                condition.add(key + " = '" + date + "'");
        } else {
            String startDate = formatDate(value,"yyyy-MM-dd",false);
            String endDate = formatDate(value,"yyyy-MM-dd",true);

            if(negate)
                condition.add("(" + key + " < '" + startDate + "' or " + key + " >= '" + endDate +"')");
            else {
                condition.add(key + " >= '" + startDate + "'");
                condition.add(key + " < '" + endDate + "'");
            }
        }
    }
}