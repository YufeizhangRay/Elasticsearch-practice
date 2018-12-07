package cn.zyf.elasticsearch.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:my-point.properties")
@Data
public class MyPointConfig {
    @Value("${my.point.lon}")
    private double lon;

    @Value("${my.point.lat}")
    private double lat;
}
