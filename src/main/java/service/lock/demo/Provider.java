package service.lock.demo;

import org.ff4j.FF4j;
import org.ff4j.web.api.FF4JProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

public class Provider implements FF4JProvider {

    @Autowired
    private FF4j ff4j;

    public Provider() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    @Override
    public FF4j getFF4j() {

        return ff4j;
    }

}
