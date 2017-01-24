import com.tayo.centos.util.CentosUtils;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Created by temitayo on 1/23/17.
 */
public class TestS3FileTokens extends TestCase
{

    String one = "8,abigailphillips@byrd.com,Danielle Glass,F,married,2017-01-19 16:22:44,CommentDisliked,http://macebook.com/search/main";
    String two =  "13,ghiggins@yahoo.com,Christopher Chavez,O,have cats,2017-01-19 16:22:44,ProfileUpdated,http://macebook.com/login/tags/tag/list";
    String  three = "103,mmack@cervantes.net,Anna Hoffman,O,single,2017-01-19 16:22:04,TopicViewed,http://macebook.com/category/list/app/tag";
    String four  = "8000,scotttracy@hotmail.com,Jodi Davis,F,married,2017-01-19 16:21:02,TopicViewed,http://macebook.com/terms/main/posts/posts";

    public void setUp() throws Exception
    {
        super.setUp();

    }

    public void testRemovalOfUniqueId() throws Exception
    {
        String result = one.substring(one.indexOf(",")+1, one.length());

        Assert.assertEquals(CentosUtils.removeUnwantedToken(one), "abigailphillips@byrd.com,Danielle Glass,F,married,2017-01-19 16:22:44,CommentDisliked,http://macebook.com/search/main");
        Assert.assertEquals(CentosUtils.removeUnwantedToken(two), "ghiggins@yahoo.com,Christopher Chavez,O,have cats,2017-01-19 16:22:44,ProfileUpdated,http://macebook.com/login/tags/tag/list");
        Assert.assertEquals(CentosUtils.removeUnwantedToken(three), "mmack@cervantes.net,Anna Hoffman,O,single,2017-01-19 16:22:04,TopicViewed,http://macebook.com/category/list/app/tag");
        Assert.assertEquals(CentosUtils.removeUnwantedToken(four), "scotttracy@hotmail.com,Jodi Davis,F,married,2017-01-19 16:21:02,TopicViewed,http://macebook.com/terms/main/posts/posts");
    }


    public void tearDown() throws Exception
    {

        super.tearDown();
    }
}
