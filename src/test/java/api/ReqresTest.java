package api;

import com.fasterxml.jackson.databind.SerializationFeature;
import io.restassured.mapper.ObjectMapper;
import io.restassured.mapper.ObjectMapperDeserializationContext;
import io.restassured.mapper.ObjectMapperSerializationContext;
import org.junit.Assert;
import org.junit.Test;

import java.time.Clock;
import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;

public class ReqresTest {
    protected static final String URL = "https://reqres.in/";

    @Test
    public void checkAvatarAndTest() {
        Specifications.installSpecification(Specifications.responseSpec_OK200(), Specifications.requestSpec(URL));
        List<UserData> users = given()
                .when()
//                .contentType(ContentType.JSON)
                .get("api/users?page=2")//URL добавляем если нет спецификации
                .then().log().all()
                .extract().body().jsonPath().getList("data", UserData.class);
        users.stream().forEach(e -> Assert.assertTrue(e.getAvatar().contains(e.getId().toString())));

        Assert.assertTrue(users.stream().allMatch(x -> x.getEmail().endsWith("@reqres.in")));

        List<String> avatars = users.stream().map(UserData::getAvatar).collect(Collectors.toList());
        List<String> id = users.stream().map(x -> x.getId().toString()).collect(Collectors.toList());
        for (int i = 0; i < avatars.size(); i++) {
            Assert.assertTrue(avatars.get(i).contains(id.get(i)));
        }
    }

    @Test()
    public void successRegTest() {
        Specifications.installSpecification(Specifications.responseSpec_OK200(), Specifications.requestSpec(URL));
        Integer id = 4;
        String token = "QpwL5tke4Pnpja7X4";
        Register user = new Register("eve.holt@reqres.in", "pistol");
        SuccessRegistration successRegistration = given()
                .body(user)
                .when()
                .post("api/register")
                .then().log().all()
                .extract().as(SuccessRegistration.class);

        Assert.assertNotNull(successRegistration.getId());
        Assert.assertNotNull(successRegistration.getToken());

        Assert.assertEquals(id, successRegistration.getId());
        Assert.assertEquals(token, successRegistration.getToken());
    }

    @Test
    public void unSuccessTest(){
        Specifications.installSpecification(Specifications.responseSpec_ERROR400(), Specifications.requestSpec(URL));
        Register user = new Register("sydney@fife", "");
        UnSuccessReg unSuccessReg = given()
                .body(user)
                .post("api/register")
                .then().log().all()
                .extract().as(UnSuccessReg.class);
        Assert.assertEquals("Missing password", unSuccessReg.getError());
    }

    @Test
    public void sortedYears(){
        Specifications.installSpecification(Specifications.responseSpec_OK200(), Specifications.requestSpec(URL));
        List<ColorsData> colorsData = given()
                .when()
                .get("api/unknown")
                .then().log().all()
                .extract().body().jsonPath().getList("data", ColorsData.class);
        List<Integer> years = colorsData.stream().map(ColorsData::getYear).collect(Collectors.toList());
        List<Integer> sortedYears = years.stream().sorted().collect(Collectors.toList());
        Assert.assertEquals(sortedYears, years);
    }

    @Test
    public void deletedUserTest(){
        Specifications.installSpecification(Specifications.responseSpecUnique(204) ,Specifications.requestSpec(URL));
        given()
                .when()
                .delete("api/users/2")
                .then().log().all();
    }


    @Test
    public void TimeTest(){
        Specifications.installSpecification(Specifications.responseSpec_OK200(), Specifications.requestSpec(URL));
        UserTime userTime = new UserTime("morpheus", "zion resident");
        UserTimeResponse response = given()
                .body(userTime)
                .when()
                .put("api/users/2")
                .then().log().all()
                .extract().as(UserTimeResponse.class);
        String regex = "(.{8})$";
        String regex1 = "(.{5})$";
        String currentTime = Clock.systemUTC().instant().toString().replaceAll(regex, "");
        Assert.assertEquals(currentTime, response.getUpdatedAt().replaceAll(regex1, ""));
    }
}
