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

    public ArrayList<String> condition = new ArrayList<String>();
    public ArrayList<String> orConditon = new ArrayList<String>();

    public String  construct(HashMap<String,Object> queryParam) {
        condition = new ArrayList<String>();
        orConditon = new ArrayList<String>();
        if (queryParam.containsKey("negate")) {
            construct_negate_condition(queryParam.get("negate"));
            queryParam.remove("negate");
        }

        construct_condition(queryParam);
        String query_string = "(";
        for(String element : condition) {
            query_string += element + " and ";
        }
        return query_string.substring(0, query_string.length() - 5) + " )";
    }

    public  void construct_condition(HashMap<String,Object> paramHash) {
        for (Entry<String, Object> params : paramHash.entrySet()) {
            String key = params.getKey();
            Object value = params.getValue();
            ArrayList<String> valueList;
            if(value instanceof ArrayList) {
                valueList = (ArrayList<String>) value;
                build_query_from_list_and_comma_separete(key,valueList,"IN");
            } else {
                String value_string = (String) value;
                valueList = new ArrayList<String>();
                for (String re : value_string.split(","))
                    valueList.add(re);
                if (valueList.size() > 1)
                    build_query_from_list_and_comma_separete(key, valueList, "IN");
                else if (value_string == "NULL")
                    condition.add(key + " IS  NULL");
                else if (value_string == "NOTNULL")
                    condition.add(key + " IS NOT NULL");
                else if (value_string.indexOf("..") != -1) {
                    construct_range_condition(key, value_string, false);
                }
                else {
                    try {
                        construct_date_condition(key, value_string, false);
                    } catch (ParseException e) {
                        condition.add(key + " = '" + value_string + "'");
                    }
                }
            }
        }
    }

    public void construct_negate_condition(Object queryParam){

        if(queryParam instanceof HashMap) {
            HashMap<String,Object> paramHash = ( HashMap<String,Object>) queryParam;
            for (Entry<String, Object> params : paramHash.entrySet()) {
                String key = params.getKey();
                Object value = params.getValue();
                ArrayList<String> valueList;
                if(value instanceof ArrayList) {
                    valueList = (ArrayList<String>) value;
                    build_query_from_list_and_comma_separete(key,valueList,"NOT IN");
                } else {
                    String value_string = (String) value;
                    valueList = new ArrayList<String>();
                    for (String re: value_string.split(","))
                        valueList.add(re);
                    if(valueList.size() > 1) {
                        build_query_from_list_and_comma_separete(key, valueList, "NOT IN");
                    }
                    else if(value_string == "NULL")
                        condition.add(key +" IS NOT NULL");
                    else if(value_string == "NOTNULL")
                        condition.add(key + " IS NULL");
                    else if(value_string.indexOf("..") != -1)
                        construct_range_condition(key,value_string,true);
                    else {
                        try {
                            construct_date_condition(key, value_string, true);
                        } catch (ParseException e) {
                            condition.add(key + " <> '" + value_string + "'");
                        }
                    }
                }

            }
        }
    }


    public void build_query_from_list_and_comma_separete(String key, ArrayList<String> valueList, String negate) {
        String query = key + " "+ negate + " (";
        for(String element : valueList) {
            query += "'" + element + "',";
        }
        condition.add(query.substring(0, query.length() - 1) + ")");
    }
    public void construct_range_condition(String key,String value_string, boolean negate) {
        ArrayList<String> valueList = new ArrayList<String>();
        for (String re: value_string.split("\\.."))
            valueList.add(re);

        if(valueList.size() <= 0 || valueList.size() > 2)
            return;

        String min_value = valueList.get(0);
        String max_value = valueList.size() == 1 ? "NULL" : valueList.get(1);
        if(min_value != "NULL") {
            try {
                construct_date_min_range_condition(key, min_value, negate);
            } catch (ParseException e) {
                if (negate)
                    orConditon.add(key + " < '" + min_value + "'");
                else
                    condition.add(key + " >= '" + min_value + "'");
            }
        }

        if(max_value != "NULL") {
            try {
                construct_date_max_range_condition(key, max_value, negate);
            } catch (ParseException e) {
                if (negate)
                    orConditon.add(key + " > '" + max_value + "'");
                else
                    condition.add(key + " <= '" + max_value + "'");
            }
        }

        if(negate) {
            String or_string = "(";
            for(String element : orConditon) {
                or_string += element + " or ";
            }
            condition.add(or_string.substring(0, or_string.length() - 4) + " )");
            orConditon.clear();
        }
    }

    public void construct_date_min_range_condition(String key,String value,boolean negate) throws ParseException{
        ArrayList<String> dateList = new ArrayList<String>();
        for (String re: value.split("T"))
            dateList.add(re);

        if(dateList.size() > 1) {
            String date = format_date(value,"yyyy-MM-dd'T'HH:mm:ss",false);
            if(negate)
                orConditon.add(key + " < '" + date + "'");
            else
                condition.add(key + " >= '" + date + "'");
        }
        else {
            String date = format_date(value,"yyyy-MM-dd",false);
            if(negate)
                orConditon.add(key + " < '" + date + "'");
            else
                condition.add(key + " >= '" + date + "'");
        }
    }

    public void construct_date_max_range_condition(String key,String value,boolean negate) throws ParseException{
        ArrayList<String> dateList = new ArrayList<String>();
        for (String re: value.split("T"))
            dateList.add(re);

        if(dateList.size() > 1) {
            String date = format_date(value,"yyyy-MM-dd'T'HH:mm:ss",false);
            if(negate)
                orConditon.add(key + " > '" + date + "'");
            else
                condition.add(key + " <= '" + date + "'");
        }
        else {
            String date = format_date(value,"yyyy-MM-dd",true);
            if(negate)
                orConditon.add(key + " >= '" + date + "'");
            else
                condition.add(key + " < '" + date + "'");
        }
    }

    public String format_date(String value,String Format,boolean plusOne) throws ParseException{
        SimpleDateFormat dateFormat = new SimpleDateFormat(Format);
        Date date = dateFormat.parse(value);
        if(plusOne) {
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.add(Calendar.DATE, 1);
            String formated_date = dateFormat.format(c.getTime());
            return formated_date;
        }

        String formated_date = dateFormat.format(date);
        return formated_date;
    }

    public void construct_date_condition(String key,String value, boolean negate)  throws ParseException{
        ArrayList<String> dateList = new ArrayList<String>();
        for (String re: value.split("T"))
            dateList.add(re);

        if(dateList.size() > 1) {
            String date = format_date(value,"yyyy-MM-dd'T'HH:mm:ss",false);
            if(negate)
                condition.add(key +  " <> '" + date + "'");
            else
                condition.add(key + " = '" + date + "'");
        } else {
            String start_date = format_date(value,"yyyy-MM-dd",false);
            String end_date = format_date(value,"yyyy-MM-dd",true);

            if(negate)
                condition.add("(" + key + " < '" + start_date + "' or " + key + " >= '" + end_date +"')");
            else {
                condition.add(key + " >= '" + start_date + "'");
                condition.add(key + " < '" + end_date + "'");
            }
        }
    }
}