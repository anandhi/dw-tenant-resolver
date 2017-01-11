/**
 * Created by anupama.agarwal on 09/01/17.
 */
import lib.SearchQueryBuilder;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

public class SearchQueryBuilderTest {

    @Test
    public void function() {
        HashMap<String,Object> paramHash = new HashMap<String, Object>();
        paramHash.put("id","80010714");
        paramHash.put("amount","18..");
        paramHash.put("party_id_to","WSR,Customer");
        paramHash.put("place","NULL");
        paramHash.put("origin","NOTNULL");
        paramHash.put("age","1..10");
        paramHash.put("date","2013-01-21T00:01:02..2013-12-13T04:05:06");
        paramHash.put("update_date","2013-01-21..2013-12-13");
        paramHash.put("delete_date","2013-01-21");
        paramHash.put("due_date","2013-01-21T00:01:02");
        ArrayList<String> client_ref_id = new ArrayList<String>();
        client_ref_id.add("aaa");
        client_ref_id.add("bbb");
        client_ref_id.add("ccc");
        paramHash.put("client_ref_id",client_ref_id);



        HashMap<String,Object> paramHashNegate = new HashMap<String, Object>();
        paramHashNegate.put("party_id_from","from_party");

        HashMap<String,Object> negate = new HashMap<String, Object>();
        negate.put("party_id_to","WSR,Customer");
        negate.put("amount","18..");
        negate.put("place","NULL");
        negate.put("origin","NOTNULL");
        negate.put("age","1..10");
        negate.put("id","123456");

        negate.put("date","2013-01-21T00:01:02..2013-12-13T04:05:06");
        negate.put("update_date","2013-01-21..2013-12-13");
        negate.put("delete_date","2013-01-21");
        negate.put("due_date","2013-01-21T00:01:02");
        ArrayList<String> client_ref_id_negate = new ArrayList<String>();
        client_ref_id_negate.add("aaa");
        client_ref_id_negate.add("bbb");
        client_ref_id_negate.add("ccc");
        negate.put("client_ref_id",client_ref_id_negate);
        paramHashNegate.put("negate",negate);


        SearchQueryBuilder searchQueryBuilder = new SearchQueryBuilder();
        System.out.println(searchQueryBuilder.construct(paramHash));

        System.out.println(" negate Condition Output\n\n");
        System.out.println(searchQueryBuilder.construct(paramHashNegate));


    }

}