package api;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static api.ReqresTest.URL;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class ReqresNoPojoTest {

    @Test
    public void checkAvatars() {
        Specifications.installSpecification(Specifications.responseSpec_OK200(), Specifications.requestSpec(URL));
        Response response = given()
                .when()
                .get("api/users?page=2")
                .then().log().all()
                .body("page", equalTo(2))
                .body("data.id", notNullValue())
                .body("data.email", notNullValue())
                .body("data.first_name", notNullValue())
                .body("data.last_name", notNullValue())
                .body("data.avatar", notNullValue())
                .extract().response();

        JsonPath jsonPath = response.jsonPath();
        List<String> email = jsonPath.get("data.email");
        List<Integer> id = jsonPath.get("data.id");
        List<String> avatars = jsonPath.get("data.avatar");

        for (int i=0; i<avatars.size();i++){
            Assert.assertTrue(avatars.get(i).contains(id.get(i).toString()));
        }
        Assert.assertTrue(email.stream().allMatch(x->x.endsWith("@reqres.in")));
    }

    @Test
    public void successUserReqTestNoPojo(){
        Specifications.installSpecification(Specifications.responseSpec_OK200(), Specifications.requestSpec(URL));
        Map<String, String> user = new HashMap<>();
        user.put("email", "eve.holt@reqres.in");
        user.put("password", "pistol");
//        given()
//                .body(user)
//                .when()
//                .post("api/register")
//                .then().log().all()
//                .body("id", equalTo(4))
//                .body("token", equalTo("QpwL5tke4Pnpja7X4"));
        Response response = given()
                .body(user)
                .when()
                .post("api/register")
                .then().log().all()
                .body("id", equalTo(4))
                .body("token", equalTo("QpwL5tke4Pnpja7X4"))
                .extract().response();
        JsonPath jsonPath = response.jsonPath();
        int id = jsonPath.get("id");
        String token = jsonPath.get("token");
        Assert.assertEquals(4, id);
        Assert.assertEquals("QpwL5tke4Pnpja7X4", token);
    }

    @Test
    public void unSuccessTestNoPojo(){
        Specifications.installSpecification(Specifications.responseSpec_ERROR400(), Specifications.requestSpec(URL));
        Map<String, String> users = new HashMap<>();
        users.put("email", "sydney@fife");
        given()
                .body(users)
                .when()
                .post("api/register")
                .then().log().all()
                .body("error", equalTo("Missing password"));
    }
}
