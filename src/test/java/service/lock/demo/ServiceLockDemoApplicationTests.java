package service.lock.demo;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.UriBuilder;

import org.ff4j.FF4j;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ServiceLockDemoApplication.class)
@WebIntegrationTest({ "server.port=0", "management.port=0" })
public class ServiceLockDemoApplicationTests {

    private static final String WRONG_CODE = "0000";

    private static final String RIGHT_CODE = "1234";

    @Value("${local.server.port}")
    int port;

    RestTemplate rest = new RestTemplate();

    @Autowired
    FF4j ff4j;

    @Test
    public void contextLoads() {
    }

    @Test
    public void lockAfter5Goes() throws URISyntaxException {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>(1);
        body.add("scvid", "123456");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        RequestEntity<MultiValueMap<String, String>> requestEntity = new RequestEntity<MultiValueMap<String, String>>(
                body, headers, HttpMethod.POST, new URI("http://localhost:"
                        + port + "/app/sendcode"));
        rest.exchange(requestEntity, Model.class);
        rest.exchange(requestEntity, Model.class);
        rest.exchange(requestEntity, Model.class);
        rest.exchange(requestEntity, Model.class);
        rest.exchange(requestEntity, Model.class);
        try {
            rest.exchange(requestEntity, Model.class);
            Assert.fail("expected 403");
        } catch (HttpClientErrorException e) {
            Assert.assertThat(e.getStatusCode(),
                    Matchers.equalTo(HttpStatus.FORBIDDEN));
        }
    }

    @Test
    public void lockAfter5GoesAfterSucceeded() throws URISyntaxException {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>(1);
        body.add("scvid", "999999");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        RequestEntity<MultiValueMap<String, String>> requestEntity = new RequestEntity<MultiValueMap<String, String>>(
                body, headers, HttpMethod.POST, new URI("http://localhost:"
                        + port + "/app/sendcode"));
        rest.exchange(requestEntity, Model.class);

        URI checkCodeUri = UriBuilder
                .fromUri("http://localhost:" + port + "/app/checkcode")
                .queryParam("scvid", "999999").queryParam("code", RIGHT_CODE)
                .build();
        rest.getForEntity(checkCodeUri, Model.class);

        rest.exchange(requestEntity, Model.class);
        rest.exchange(requestEntity, Model.class);
        rest.exchange(requestEntity, Model.class);
        rest.exchange(requestEntity, Model.class);
        rest.exchange(requestEntity, Model.class);
        try {
            rest.exchange(requestEntity, Model.class);
            Assert.fail("expected 403");
        } catch (HttpClientErrorException e) {
            Assert.assertThat(e.getStatusCode(),
                    Matchers.equalTo(HttpStatus.FORBIDDEN));
        }
    }

    @Test
    public void lockAfter3FailedChecks() throws URISyntaxException, IOException {
        URI checkCodeUri = UriBuilder
                .fromUri("http://localhost:" + port + "/app/checkcode")
                .queryParam("scvid", "333333").queryParam("code", WRONG_CODE)
                .build();
        try {
            rest.getForEntity(checkCodeUri, Model.class);
            Assert.fail("expected 400");
        } catch (HttpClientErrorException e) {
            Assert.assertThat(e.getStatusCode(),
                    Matchers.equalTo(HttpStatus.BAD_REQUEST));
        }
        try {
            rest.getForEntity(checkCodeUri, Model.class);
            Assert.fail("expected 400");
        } catch (HttpClientErrorException e) {
            Assert.assertThat(e.getStatusCode(),
                    Matchers.equalTo(HttpStatus.BAD_REQUEST));
        }
        try {
            rest.getForEntity(checkCodeUri, Model.class);
            Assert.fail("expected 400");
        } catch (HttpClientErrorException e) {
            Assert.assertThat(e.getStatusCode(),
                    Matchers.equalTo(HttpStatus.BAD_REQUEST));
        }

        try {
            rest.getForEntity(checkCodeUri, Model.class);
            Assert.fail("expected 403");
        } catch (HttpClientErrorException e) {
            Assert.assertThat(e.getStatusCode(),
                    Matchers.equalTo(HttpStatus.FORBIDDEN));
        }

        // Nope! Can't check any more, event with the right code
        checkCodeUri = UriBuilder
                .fromUri("http://localhost:" + port + "/app/checkcode")
                .queryParam("scvid", "333333").queryParam("code", RIGHT_CODE)
                .build();
        try {
            rest.getForEntity(checkCodeUri, Model.class);
            Assert.fail("expected 403");
        } catch (HttpClientErrorException e) {
            Assert.assertThat(e.getStatusCode(),
                    Matchers.equalTo(HttpStatus.FORBIDDEN));
        }

        // ok, we'll enable it and you can check again 3 more times
        ff4j.delete("CheckCode.Locked.333333");

        checkCodeUri = UriBuilder
                .fromUri("http://localhost:" + port + "/app/checkcode")
                .queryParam("scvid", "333333").queryParam("code", RIGHT_CODE)
                .build();

        Assert.assertThat(rest.getForEntity(checkCodeUri, Model.class)
                .getBody().getContent(), Matchers.equalTo("ok"));

        checkCodeUri = UriBuilder
                .fromUri("http://localhost:" + port + "/app/checkcode")
                .queryParam("scvid", "333333").queryParam("code", WRONG_CODE)
                .build();

        try {
            rest.getForEntity(checkCodeUri, Model.class);
            Assert.fail("expected 400");
        } catch (HttpClientErrorException e) {
            Assert.assertThat(e.getStatusCode(),
                    Matchers.equalTo(HttpStatus.BAD_REQUEST));
        }
        try {
            rest.getForEntity(checkCodeUri, Model.class);
            Assert.fail("expected 400");
        } catch (HttpClientErrorException e) {
            Assert.assertThat(e.getStatusCode(),
                    Matchers.equalTo(HttpStatus.BAD_REQUEST));
        }
        try {
            rest.getForEntity(checkCodeUri, Model.class);
            Assert.fail("expected 400");
        } catch (HttpClientErrorException e) {
            Assert.assertThat(e.getStatusCode(),
                    Matchers.equalTo(HttpStatus.BAD_REQUEST));
        }
        try {
            rest.getForEntity(checkCodeUri, Model.class);
            Assert.fail("expected 403");
        } catch (HttpClientErrorException e) {
            Assert.assertThat(e.getStatusCode(),
                    Matchers.equalTo(HttpStatus.FORBIDDEN));
        }

    }

}
