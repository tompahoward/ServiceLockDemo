package service.lock.demo;

import java.io.IOException;

import org.ff4j.FF4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "")
public class ServiceLockDemoController {
    public final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private FF4j ff4j;

    @RequestMapping(method = RequestMethod.POST, value = "/sendcode")
    @ResponseBody
    public ResponseEntity<?> hello(@RequestParam("scvid") String scvid) {
        String featureName = "SendCode.Locked." + scvid;
        if (ff4j.exist(featureName)) {
            if (ff4j.check(featureName)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        CounterFlipStrategy.createCounter(ff4j, featureName, 5);

        ResponseEntity<Model> responseEntity = new ResponseEntity<Model>(
                new Model("sent"), HttpStatus.OK);

        return responseEntity;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/checkcode")
    @ResponseBody
    public ResponseEntity<?> hello(@RequestParam("scvid") String scvid,
            @RequestParam("code") String code) throws IOException {
        String featureName = "CheckCode.Locked." + scvid;
        if (ff4j.exist(featureName)) {
            if (ff4j.check(featureName)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        if ("1234".equals(code)) {
            if (ff4j.exist(featureName)) {
                ff4j.delete(featureName);
            }
            String sendFeatureName = "SendCode.Locked." + scvid;
            if (ff4j.exist(sendFeatureName)) {
                ff4j.delete(sendFeatureName);
            }
            return new ResponseEntity<Model>(new Model("ok"), HttpStatus.OK);
        }

        CounterFlipStrategy.createCounter(ff4j, featureName, 3);

        return ResponseEntity.badRequest().build();
    }
}
