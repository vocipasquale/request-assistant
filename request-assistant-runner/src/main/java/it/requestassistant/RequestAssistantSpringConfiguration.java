package it.requestassistant;

import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RequestAssistantSpringConfiguration {

	@Bean
	FxWeaver fxWeaver(ConfigurableApplicationContext applicationContext) {
		return new FxWeaver(applicationContext::getBean, applicationContext::close);
	}
}
