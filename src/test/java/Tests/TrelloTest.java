package Tests;

import Utils.TrelloUtils;
import com.sun.xml.bind.v2.runtime.output.FastInfosetStreamWriterOutput;
import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.config;
import static org.hamcrest.Matchers.*;

public class TrelloTest {
    TrelloUtils tu;
    String BoardId;
    List<String> ListsOnBoard;
    @BeforeClass
    public void init(){
        tu=new TrelloUtils();
        BoardId="";
        ListsOnBoard=new ArrayList<>();
    }


    @Test(description = "001.Verify Board Exists",priority = 0)
    public void verifyGetBoard()  {
        System.out.println("thread id:" + Thread.currentThread().getId() + "Timestamp :" + LocalDateTime.now());
        Response response=tu.GetTrelloRequest("boards/ieGI2Zmn");
        Reporter.log("made request to default board 'boards/ieGI2Zmn'",true);
        response.then().statusCode(200).log().ifValidationFails(LogDetail.BODY)
                .body("id",equalTo("5de9494a947c304480effc88"));

        }

    @Test(description = "002.verify board name is mandatory",priority = 1)
    public void postBoard(){
        System.out.println("thread id:" + Thread.currentThread().getId() + "Timestamp :" + LocalDateTime.now());
        HashMap<String,Object> CreateBoardMessage=new HashMap<>();
        CreateBoardMessage.put("defaultLists",false);
        Reporter.log("making request to create a board on Trello",true);
        Response response=tu.PostRequest("boards",CreateBoardMessage);
        Reporter.log("Request to create a board on Trello completed",true);
        response.then()
                .statusCode(400)
                .statusLine("HTTP/1.1 400 Bad Request")
                .contentType("text/plain")
                .log().ifValidationFails( LogDetail.BODY);
        Assert.assertEquals(response.getBody().asString(),"invalid value for name");
    }

    @Test(description = "003.Verify user is able to create board with Mandatory Field",priority = 2)
    public void postValidBoard(){
        System.out.println("thread id:" + Thread.currentThread().getId() + "Timestamp :" + LocalDateTime.now());
        HashMap<String,Object> CreateBoardMessage=new HashMap<>();
        CreateBoardMessage.put("name","My Temporary Board");
        CreateBoardMessage.put("defaultLists",true);
        Response response=tu.PostRequest("boards",CreateBoardMessage);
        response.then()
                .statusCode(200)
                .contentType("application/json")
                .log().ifValidationFails( LogDetail.BODY);
        System.out.println(response.jsonPath().get("id").toString());
        BoardId=response.jsonPath().get("id").toString();
    }

    @Test(description = "004.Verify Lists On Board",priority = 3,dependsOnMethods = "postValidBoard")
    public void getListsOnBoard(){
        System.out.println("thread id:" + Thread.currentThread().getId() + "Timestamp :" + LocalDateTime.now());
        Response response=tu.GetTrelloRequest("boards/"+BoardId+"/Lists");
        response.then()
                .statusCode(200)
                .contentType("application/json")
                .log().ifValidationFails( LogDetail.BODY)
                .body("name",hasItems("To Do","Doing","Done"))
                .body("idBoard",hasItems(BoardId,BoardId,BoardId));
        System.out.println(response.jsonPath().getList("id"));
        System.out.println(response.jsonPath().getList("name"));
        ListsOnBoard=response.jsonPath().getList("id");
    }

    @Test(description = "005.Verify user is able to delete a Board",priority = 4,dependsOnMethods = "postValidBoard")
    public void DeleteValidBoard(){
        System.out.println("thread id:" + Thread.currentThread().getId() + "Timestamp :" + LocalDateTime.now());
        Response response=tu.DeleteRequest("boards/"+BoardId);
        response.then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .log().ifValidationFails( LogDetail.BODY)
                .body("_value",equalTo(null));
    }

    @Test(description = "006.Verify Board Not Found on Deleting Board",priority = 5,dependsOnMethods = "DeleteValidBoard")
    public void BoardNotFound(){
        System.out.println("thread id:" + Thread.currentThread().getId() + "Timestamp :" + LocalDateTime.now());
            Response response=tu.GetTrelloRequest("boards/"+BoardId+"/lists");
            response.then()
                    .statusCode(404)
                    .statusLine("HTTP/1.1 404 Not Found")
                    .contentType("text/plain")
                    .log().ifValidationFails( LogDetail.ALL)
                    .assertThat()
                    .extract().body().asString().equals("board not found");

    }

    @Test(description = "007.Verify Lists are also deleted on deleting the board",priority = 6,dependsOnMethods = "DeleteValidBoard")
    public void ListsAreDeleted(){
        System.out.println("thread id:" + Thread.currentThread().getId() + "Timestamp :" + LocalDateTime.now());
        for(String list_id:ListsOnBoard){
            Response response=tu.GetTrelloRequest("lists/"+list_id);
            response.then()
                    .statusCode(404)
                    .statusLine("HTTP/1.1 404 Not Found")
                    .contentType("text/plain")
                    .log().ifValidationFails( LogDetail.ALL)
                    .assertThat()
                    .extract().body().asString().equals("model not found");
        }

    }


}
