package Utils;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.LogConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestLogSpecification;
import io.restassured.specification.RequestSpecification;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;

public class TrelloUtils {

    public static RequestSpecification request;

    public TrelloUtils(String token,String APIKey){
        RequestSpecBuilder requestSpecBuilder=new RequestSpecBuilder();
        PrintStream fileOutPutStream ;
        try {
            fileOutPutStream= new PrintStream(new File("Logs\\logFile.txt"));
        } catch (FileNotFoundException e) {
            fileOutPutStream=null;
        }
        RestAssured.config = RestAssured.config().logConfig(new LogConfig().defaultStream(fileOutPutStream));

        requestSpecBuilder.setBaseUri("https://api.trello.com/1");
        requestSpecBuilder.addQueryParam("token",token);
        requestSpecBuilder.addQueryParam("key",APIKey);
        requestSpecBuilder.setConfig(RestAssured.config);
        request= RestAssured.given().spec(requestSpecBuilder.build());
    }

    public Response GetTrelloRequest(String url) {

        return request.when()
                .get(url);
    }

    public Response PostRequest(String url, HashMap<String,Object> data ){
        return request.when()
                .contentType(ContentType.JSON)
                .body(data)
                .post(url);
    }

    public Response DeleteRequest(String url){
        return request.when()
                .delete(url);
    }

    public Response PostUsingQueryParamRequest(String url,HashMap<String,String> Params){
        return request.when()
                .body(Params)
                .post(url);
    }

}
