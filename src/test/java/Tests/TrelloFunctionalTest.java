package Tests;

import Utils.TrelloUtils;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.*;

import static org.hamcrest.Matchers.*;

import java.util.*;

public class TrelloFunctionalTest {
    TrelloUtils trelloUtils=null;
    HashMap<String,String> ListsId;
    String BoardId;
    String cardId;

    @Parameters({"token","apikey"})
    @BeforeTest
    public void init(String token,String apiKey){
        trelloUtils=new TrelloUtils(token,apiKey);
        BoardId="";
        ListsId=new HashMap<>();
        cardId="";
    }

    @BeforeClass
    public void createBasicBoard() {
        HashMap<String, Object> basicBoardDetails = new HashMap<>();
        basicBoardDetails.put("name", "My Basic Board");
        basicBoardDetails.put("desc", "this basic board will be used to create Lists, Cards and perform various operations");
        basicBoardDetails.put("defaultLists", false);
        basicBoardDetails.put("prefs_background","grey");
        Response response = trelloUtils
                .PostRequest("boards", basicBoardDetails);

        BoardId=response.then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .assertThat()
                .body("name", equalTo("My Basic Board"))
                .body("desc", containsString("Lists, Cards and perform various operations"))
                .body("prefs.background",equalTo("grey"))
                .log().ifValidationFails(LogDetail.BODY)
                .extract().body().jsonPath().get("id").toString();
    }

    @Test(description = "001. Verify Default Lists are not present",priority = 1)
    public void verifyDefaultListsOnBoard(){
        Response response=trelloUtils.GetTrelloRequest("boards/"+BoardId+"/lists");
        System.out.println(response.getBody().prettyPrint());
        response.then().extract().body().asString().equals("[]");
    }

    @Test(description = "002. Create Lists On Board",priority = 2)
    public void CreateListOnBoard(){
        HashMap<String,Object> queryBody;
        ArrayList<String> ListNames=new ArrayList<>(Arrays.asList("To Do","Doing","Done"));

        for(int i=0;i<3;i++){
            queryBody=new HashMap<>();
            queryBody.put("name",ListNames.get(i));
            queryBody.put("idBoard",BoardId);
            Response response=trelloUtils.PostRequest("lists",queryBody);
            System.out.println(response.getBody());
            String ListId=response.then()
                    .contentType(ContentType.JSON)
                    .statusCode(200)
                    .body("name",equalTo(ListNames.get(i)))
                    .body("idBoard",equalTo(BoardId))
                    .log().ifValidationFails(LogDetail.BODY)
                    .extract().body().jsonPath().get("id").toString();
            ListsId.put(ListNames.get(i),ListId);
        }
        System.out.println(ListsId.values());
    }

    @Test(description = "003. Verify ListId is required while Creating card",priority = 3)
    public void VerifyMandatoryFieldsForCard(){
        HashMap<String,Object> queryBody=new HashMap<>();
        queryBody.put("name","Flight Booking");
        Response response=trelloUtils.PostRequest("cards",queryBody);
        response.then()
                .contentType(ContentType.TEXT)
                .statusCode(400)
                .log().ifValidationFails(LogDetail.BODY)
                .extract().body().asString().equals("invalid value for idList");
    }


    @Test(description = "004. Verify user is able to create card",priority = 4,dependsOnMethods = "CreateListOnBoard")
    public void VerifyCardCreation(){
        HashMap<String,Object> queryBody=new HashMap<>();
        queryBody.put("name","Flight Booking");
        queryBody.put("idList",ListsId.get("To Do"));
        Response response=trelloUtils.PostRequest("cards",queryBody);
        cardId=response.then()
                .contentType("application/json")
                .statusCode(200)
                .body("name",equalTo("Flight Booking"))
                .body("idBoard",equalTo(BoardId))
                .log().ifValidationFails(LogDetail.BODY)
                .extract().body().jsonPath().get("id");
    }

    @AfterClass
    public void DeleteBoard(){
        Response response=trelloUtils.DeleteRequest("boards/"+BoardId);
        response.then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .log().ifValidationFails( LogDetail.BODY)
                .body("_value",equalTo(null));
    }

}
