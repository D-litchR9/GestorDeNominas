package bean;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named
@ApplicationScoped
public class TestCdiBean {
    public String getSaludo() {
        return "CDI funciona";
    }
}